package com.mealgpt.service;

import com.mealgpt.model.CartItem;
import com.mealgpt.model.Recipe;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartService {
    private final PantryService pantryService;
    private final Map<String, CartItem> cart = new LinkedHashMap<>();

    public CartService(PantryService pantryService) {
        this.pantryService = pantryService;
    }

    public String coupangSearchUrl(String name) {
        return "https://www.coupang.com/np/search?q=" + UriUtils.encodeQueryParam(name, StandardCharsets.UTF_8);
    }

    public String buyRouteUrl(String name) {
        return "/buy/" + UriUtils.encodePathSegment(name, StandardCharsets.UTF_8);
    }

    public synchronized List<CartItem> addItemToCart(String name, int quantity) {
        String itemName = pantryService.toKoreanItemName(name);
        if (itemName.isBlank() || pantryService.isPantryItem(itemName)) {
            return getCartItems();
        }

        int nextQuantity = Math.max(1, quantity);
        CartItem current = cart.get(itemName);
        if (current != null) {
            nextQuantity += current.quantity();
        }

        cart.put(itemName, new CartItem(itemName, nextQuantity, buyRouteUrl(itemName), coupangSearchUrl(itemName)));
        return getCartItems();
    }

    public synchronized List<CartItem> addMissingItemsToCart(List<Recipe> recipes) {
        Map<String, Integer> missingCounts = new LinkedHashMap<>();
        for (Recipe recipe : recipes) {
            for (String item : recipe.missingIngredients()) {
                String name = pantryService.toKoreanItemName(item);
                if (!name.isBlank() && !pantryService.isPantryItem(name)) {
                    missingCounts.merge(name, 1, Integer::sum);
                }
            }
        }
        missingCounts.forEach(this::addItemToCart);
        return getCartItems();
    }

    public synchronized List<CartItem> getCartItems() {
        return new ArrayList<>(cart.values());
    }

    public synchronized List<CartItem> clearCartItems() {
        cart.clear();
        return List.of();
    }
}
