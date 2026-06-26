package com.mealgpt.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CartItem(
        String name,
        int quantity,
        @JsonProperty("buy_url") String buyUrl,
        @JsonProperty("coupang_url") String coupangUrl
) {
}
