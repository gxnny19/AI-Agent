package kr.jgg.mealgpt.cart;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CartItem {
    private String name;
    private int quantity;
    private String buyUrl;
    private String coupangUrl;

    public CartItem() {
    }

    public CartItem(String name, int quantity, String buyUrl, String coupangUrl) {
        this.name = name;
        this.quantity = quantity;
        this.buyUrl = buyUrl;
        this.coupangUrl = coupangUrl;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    @JsonProperty("buy_url")
    public String getBuyUrl() {
        return buyUrl;
    }

    @JsonProperty("coupang_url")
    public String getCoupangUrl() {
        return coupangUrl;
    }
}
