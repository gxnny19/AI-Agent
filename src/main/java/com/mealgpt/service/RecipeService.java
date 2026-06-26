package com.mealgpt.service;

import com.mealgpt.config.MealGptProperties;
import com.mealgpt.model.Recipe;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecipeService {
    private static final String SPOONACULAR_FIND_BY_INGREDIENTS_URL =
            "https://api.spoonacular.com/recipes/findByIngredients";

    private final MealGptProperties properties;
    private final PantryService pantryService;
    private final TextUtils textUtils;
    private final RestClient restClient;

    public RecipeService(MealGptProperties properties, PantryService pantryService, TextUtils textUtils, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.pantryService = pantryService;
        this.textUtils = textUtils;
        this.restClient = restClientBuilder.build();
    }

    public List<Map<String, Object>> searchRecipes(List<String> ingredients) {
        List<String> cleaned = validateIngredients(ingredients);
        String apiKey = properties.getSpoonacularApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("SPOONACULAR_API_KEY is required");
        }

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.spoonacular.com")
                        .path("/recipes/findByIngredients")
                        .queryParam("apiKey", apiKey)
                        .queryParam("ingredients", String.join(",", cleaned))
                        .queryParam("number", 20)
                        .queryParam("ranking", 1)
                        .queryParam("ignorePantry", true)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public double calculateScore(Map<String, Object> recipe) {
        if (recipe == null) {
            throw new IllegalArgumentException("recipe must be a map");
        }

        double usedCount = asNumber(recipe.get("usedIngredientCount"), 0.0);
        double missedCount = asNumber(recipe.get("missedIngredientCount"), 0.0);
        double totalCount = usedCount + missedCount;
        if (totalCount <= 0) {
            throw new IllegalArgumentException("recipe must include ingredient match counts");
        }

        double matchRate = usedCount / totalCount;
        double popularityScore = Math.min(asNumber(recipe.get("likes"), 0.0) / 100.0, 1.0);
        double readyMinutes = asNumber(recipe.get("readyInMinutes"), 30.0);
        double ingredientCount = asNumber(recipe.get("extendedIngredientCount"), totalCount);
        double timeDifficulty = Math.min(Math.max(readyMinutes, 0.0) / 90.0, 1.0);
        double ingredientDifficulty = Math.min(Math.max(ingredientCount, 0.0) / 15.0, 1.0);
        double difficultyScore = 1.0 - ((timeDifficulty + ingredientDifficulty) / 2.0);

        return Math.round((matchRate * 0.55 + popularityScore * 0.25 + difficultyScore * 0.20) * 10000.0) / 100.0;
    }

    public List<Map<String, Object>> rankAndFilter(List<Map<String, Object>> recipes, int topN) {
        if (recipes == null) {
            throw new IllegalArgumentException("recipes must be a list");
        }
        if (topN <= 0) {
            throw new IllegalArgumentException("topN must be greater than 0");
        }

        List<Map<String, Object>> scored = new ArrayList<>();
        for (Map<String, Object> recipe : recipes) {
            Map<String, Object> copy = new LinkedHashMap<>(recipe);
            copy.put("score", calculateScore(recipe));
            scored.add(copy);
        }
        scored.sort(Comparator.comparingDouble(item -> -asNumber(item.get("score"), 0.0)));
        return scored.stream().limit(topN).toList();
    }

    public List<Recipe> recommendRecipes(List<String> ingredients) {
        return recommendRecipes(ingredients, 5);
    }

    public List<Recipe> recommendRecipes(List<String> ingredients, int topN) {
        if (ingredients == null || ingredients.isEmpty()) {
            return List.of();
        }
        List<Map<String, Object>> ranked = rankAndFilter(searchRecipes(ingredients), topN);
        return ranked.stream().map(recipe -> normalizeSpoonacularRecipe(recipe, ingredients)).toList();
    }

    private Recipe normalizeSpoonacularRecipe(Map<String, Object> recipe, List<String> available) {
        List<String> usedIngredients = ingredientNames(recipe.get("usedIngredients"));
        List<String> missedIngredients = ingredientNames(recipe.get("missedIngredients"));
        List<String> allIngredients = textUtils.cleanTextList(join(usedIngredients, missedIngredients));

        List<String> missing = new ArrayList<>();
        for (String item : missedIngredients) {
            String itemName = pantryService.toKoreanItemName(item);
            if (itemName.isBlank() || pantryService.isPantryItem(itemName)) {
                continue;
            }
            if (!pantryService.isAvailableItem(itemName, available) && !missing.contains(itemName)) {
                missing.add(itemName);
            }
        }

        return new Recipe(
                recipe.get("id"),
                String.valueOf(recipe.getOrDefault("title", "Recommended Recipe")).trim(),
                "Matched " + (int) asNumber(recipe.get("usedIngredientCount"), 0.0)
                        + " ingredient(s), missing " + (int) asNumber(recipe.get("missedIngredientCount"), 0.0) + ".",
                textUtils.cleanTextList(allIngredients.stream().map(pantryService::toKoreanItemName).toList()),
                missing,
                List.of(),
                String.valueOf(recipe.getOrDefault("image", "")),
                String.valueOf(recipe.getOrDefault("sourceUrl", "")),
                calculateScore(recipe)
        );
    }

    private List<String> validateIngredients(List<String> ingredients) {
        if (ingredients == null) {
            throw new IllegalArgumentException("ingredients must be a list");
        }
        List<String> cleaned = textUtils.cleanTextList(ingredients.stream().map(String::trim).toList());
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("ingredients must contain at least one item");
        }
        return cleaned;
    }

    private double asNumber(Object value, double defaultValue) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private List<String> ingredientNames(Object ingredients) {
        if (!(ingredients instanceof List<?> list)) {
            return List.of();
        }
        List<String> names = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                Object name = map.get("name");
                names.add(name == null ? "" : String.valueOf(name));
            } else if (item != null) {
                names.add(String.valueOf(item));
            }
        }
        return names;
    }

    private List<String> join(List<String> first, List<String> second) {
        List<String> joined = new ArrayList<>(first);
        joined.addAll(second);
        return joined;
    }

    public String getSpoonacularFindByIngredientsUrl() {
        return SPOONACULAR_FIND_BY_INGREDIENTS_URL;
    }
}
