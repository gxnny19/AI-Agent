package com.mealgpt.service;

import com.mealgpt.config.MealGptProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecipeServiceTest {
    private RecipeService recipeService;
    private List<Map<String, Object>> recipes;

    @BeforeEach
    void setUp() {
        recipeService = new RecipeService(
                new MealGptProperties(),
                new PantryService(),
                new TextUtils(new com.fasterxml.jackson.databind.ObjectMapper()),
                RestClient.builder()
        );
        recipes = new ArrayList<>(List.of(
                Map.of("id", 1, "title", "Tomato Omelet", "usedIngredientCount", 3, "missedIngredientCount", 0, "likes", 80, "readyInMinutes", 15),
                Map.of("id", 2, "title", "Vegetable Soup", "usedIngredientCount", 2, "missedIngredientCount", 2, "likes", 200, "readyInMinutes", 60),
                Map.of("id", 3, "title", "Egg Toast", "usedIngredientCount", 2, "missedIngredientCount", 1, "likes", 10, "readyInMinutes", 10)
        ));
    }

    @Test
    void calculateScorePrefersHighMatchSimpleRecipe() {
        double topScore = recipeService.calculateScore(recipes.get(0));
        double lowerScore = recipeService.calculateScore(recipes.get(1));

        assertThat(topScore).isGreaterThan(lowerScore);
    }

    @Test
    void calculateScoreRejectsBadRecipe() {
        assertThatThrownBy(() -> recipeService.calculateScore(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> recipeService.calculateScore(Map.of("title", "No counts")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rankAndFilterOrdersByScore() {
        List<Map<String, Object>> ranked = recipeService.rankAndFilter(recipes, 2);

        assertThat(ranked).hasSize(2);
        assertThat(ranked.get(0).get("id")).isEqualTo(1);
        assertThat((Double) ranked.get(0).get("score")).isGreaterThanOrEqualTo((Double) ranked.get(1).get("score"));
    }

    @Test
    void rankAndFilterValidatesArguments() {
        assertThatThrownBy(() -> recipeService.rankAndFilter(null, 2))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> recipeService.rankAndFilter(List.of(), 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
