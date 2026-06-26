package com.mealgpt.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Recipe(
        Object id,
        String name,
        String description,
        List<String> ingredients,
        @JsonProperty("missing_ingredients") List<String> missingIngredients,
        List<String> steps,
        String image,
        @JsonProperty("source_url") String sourceUrl,
        double score
) {
}
