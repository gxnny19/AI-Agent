package kr.jgg.mealgpt.recipe;

import com.fasterxml.jackson.databind.JsonNode;
import kr.jgg.mealgpt.config.MealGptProperties;
import kr.jgg.mealgpt.config.SecretResolver;
import kr.jgg.mealgpt.common.TextUtils;
import kr.jgg.mealgpt.pantry.PantryUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RecipeService {
    private static final String FIND_BY_INGREDIENTS_URL = "https://api.spoonacular.com/recipes/findByIngredients";

    private final MealGptProperties properties;
    private final SecretResolver secretResolver;
    private final RestTemplate restTemplate = new RestTemplate();

    public RecipeService(MealGptProperties properties, SecretResolver secretResolver) {
        this.properties = properties;
        this.secretResolver = secretResolver;
    }

    public List<RecipeDto> recommend(List<String> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return new ArrayList<>();
        }

        List<JsonNode> recipes = searchRecipes(ingredients);
        List<JsonNode> ranked = rankAndFilter(recipes, 5);
        List<RecipeDto> normalized = new ArrayList<>();
        for (JsonNode recipe : ranked) {
            normalized.add(normalizeRecipe(recipe, ingredients));
        }
        return normalized;
    }

    public List<JsonNode> searchRecipes(List<String> ingredients) {
        List<String> cleaned = validateIngredients(ingredients);
        String apiKey = spoonacularApiKey();
        URI uri = UriComponentsBuilder.fromHttpUrl(FIND_BY_INGREDIENTS_URL)
                .queryParam("apiKey", apiKey)
                .queryParam("ingredients", String.join(",", cleaned))
                .queryParam("number", 20)
                .queryParam("ranking", 1)
                .queryParam("ignorePantry", true)
                .build()
                .encode()
                .toUri();

        try {
            JsonNode recipes = restTemplate.getForObject(uri, JsonNode.class);
            if (recipes == null || !recipes.isArray()) {
                throw new IllegalArgumentException("Spoonacular recipe search must return a list");
            }
            List<JsonNode> results = new ArrayList<>();
            for (JsonNode recipe : recipes) {
                results.add(recipe);
            }
            return results;
        } catch (RestClientException ex) {
            throw new IllegalArgumentException("failed to search recipes: " + ex.getMessage(), ex);
        }
    }

    public List<JsonNode> rankAndFilter(List<JsonNode> recipes, int topN) {
        if (recipes == null) {
            throw new IllegalArgumentException("recipes must be a list");
        }
        if (topN <= 0) {
            throw new IllegalArgumentException("top_n must be greater than 0");
        }
        List<JsonNode> ranked = new ArrayList<>(recipes);
        ranked.sort(Comparator.comparingDouble(this::calculateScore).reversed());
        return ranked.size() > topN ? ranked.subList(0, topN) : ranked;
    }

    private RecipeDto normalizeRecipe(JsonNode recipe, List<String> available) {
        RecipeDto dto = new RecipeDto();
        dto.setId(recipe.path("id").isNumber() ? recipe.path("id").asInt() : null);
        dto.setName(text(recipe.path("title"), "Recommended Recipe"));
        dto.setImage(text(recipe.path("image"), ""));
        dto.setSourceUrl(text(recipe.path("sourceUrl"), ""));
        dto.setDescription("Matched " + recipe.path("usedIngredientCount").asInt(0)
                + " ingredient(s), missing " + recipe.path("missedIngredientCount").asInt(0) + ".");
        dto.setIngredients(TextUtils.cleanTextList(toKorean(readIngredientNames(recipe.path("usedIngredients"), recipe.path("missedIngredients")))));
        dto.setMissingIngredients(readMissingIngredientNames(recipe.path("missedIngredients"), available));
        dto.setScore(calculateScore(recipe));
        return dto;
    }

    private List<String> readIngredientNames(JsonNode used, JsonNode missed) {
        List<String> names = new ArrayList<>();
        appendIngredientNames(names, used);
        appendIngredientNames(names, missed);
        return names;
    }

    private List<String> readMissingIngredientNames(JsonNode missed, List<String> available) {
        List<String> names = new ArrayList<>();
        appendIngredientNames(names, missed);
        List<String> missing = new ArrayList<>();
        for (String name : names) {
            String itemName = PantryUtils.toKoreanItemName(name);
            if (itemName.isEmpty() || PantryUtils.isPantryItem(itemName)) {
                continue;
            }
            if (!PantryUtils.isAvailableItem(itemName, available) && !missing.contains(itemName)) {
                missing.add(itemName);
            }
        }
        return missing;
    }

    private void appendIngredientNames(List<String> names, JsonNode nodes) {
        if (nodes == null || !nodes.isArray()) {
            return;
        }
        for (JsonNode node : nodes) {
            String name = text(node.path("name"), node.asText(""));
            if (!name.isEmpty() && !names.contains(name)) {
                names.add(name);
            }
        }
    }

    public double calculateScore(JsonNode recipe) {
        if (recipe == null || !recipe.isObject()) {
            throw new IllegalArgumentException("recipe must be a dict");
        }
        double used = recipe.path("usedIngredientCount").asDouble(0);
        double missed = recipe.path("missedIngredientCount").asDouble(0);
        double total = used + missed;
        if (total <= 0) {
            throw new IllegalArgumentException("recipe must include ingredient match counts");
        }
        double matchRate = used / total;
        double popularity = Math.min(recipe.path("likes").asDouble(0) / 100.0, 1.0);
        double readyMinutes = recipe.path("readyInMinutes").asDouble(30.0);
        double ingredientCount = recipe.path("extendedIngredientCount").asDouble(total);
        double timeDifficulty = Math.min(Math.max(readyMinutes, 0.0) / 90.0, 1.0);
        double ingredientDifficulty = Math.min(Math.max(ingredientCount, 0.0) / 15.0, 1.0);
        double difficultyScore = 1.0 - ((timeDifficulty + ingredientDifficulty) / 2.0);
        return Math.round((matchRate * 0.55 + popularity * 0.25 + difficultyScore * 0.20) * 10000.0) / 100.0;
    }

    private List<String> validateIngredients(List<String> ingredients) {
        List<String> cleaned = TextUtils.cleanTextList(ingredients);
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("ingredients must contain at least one item");
        }
        if (isBlank(spoonacularApiKey())) {
            throw new IllegalArgumentException("SPOONACULAR_API_KEY is required");
        }
        return cleaned;
    }

    public boolean isSpoonacularConfigured() {
        return !isBlank(spoonacularApiKey());
    }

    private String spoonacularApiKey() {
        return secretResolver.resolve("SPOONACULAR_API_KEY", properties.getSpoonacular().getApiKey());
    }

    private List<String> toKorean(List<String> items) {
        List<String> result = new ArrayList<>();
        for (String item : items) {
            result.add(PantryUtils.toKoreanItemName(item));
        }
        return result;
    }

    private String text(JsonNode node, String fallback) {
        String value = node == null ? "" : node.asText("");
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
