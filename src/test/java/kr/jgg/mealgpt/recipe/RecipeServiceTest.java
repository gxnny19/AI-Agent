package kr.jgg.mealgpt.recipe;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.jgg.mealgpt.config.MealGptProperties;
import kr.jgg.mealgpt.config.SecretResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecipeServiceTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private RecipeService recipeService;
    private List<JsonNode> recipes;

    @BeforeEach
    void setUp() throws Exception {
        MealGptProperties properties = new MealGptProperties();
        properties.getSpoonacular().setApiKey("test-key");
        recipeService = new RecipeService(properties, new SecretResolver());
        recipes = Arrays.asList(
                objectMapper.readTree("{\"id\":1,\"title\":\"Tomato Omelet\",\"usedIngredientCount\":3,\"missedIngredientCount\":0,\"likes\":80,\"readyInMinutes\":15}"),
                objectMapper.readTree("{\"id\":2,\"title\":\"Vegetable Soup\",\"usedIngredientCount\":2,\"missedIngredientCount\":2,\"likes\":200,\"readyInMinutes\":60}"),
                objectMapper.readTree("{\"id\":3,\"title\":\"Egg Toast\",\"usedIngredientCount\":2,\"missedIngredientCount\":1,\"likes\":10,\"readyInMinutes\":10}")
        );
    }

    @Test
    void calculateScorePrefersHighMatchSimpleRecipe() {
        double topScore = recipeService.calculateScore(recipes.get(0));
        double lowerScore = recipeService.calculateScore(recipes.get(1));

        assertThat(topScore).isGreaterThan(lowerScore);
    }

    @Test
    void calculateScoreRejectsBadRecipe() throws Exception {
        assertThatThrownBy(() -> recipeService.calculateScore(objectMapper.readTree("\"not a recipe\"")))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> recipeService.calculateScore(objectMapper.readTree("{\"title\":\"No counts\"}")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rankAndFilterOrdersByScore() {
        List<JsonNode> ranked = recipeService.rankAndFilter(recipes, 2);

        assertThat(ranked).hasSize(2);
        assertThat(ranked.get(0).path("id").asInt()).isEqualTo(1);
        assertThat(recipeService.calculateScore(ranked.get(0))).isGreaterThanOrEqualTo(recipeService.calculateScore(ranked.get(1)));
    }

    @Test
    void rankAndFilterValidatesArguments() {
        assertThatThrownBy(() -> recipeService.rankAndFilter(null, 2))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> recipeService.rankAndFilter(recipes, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
