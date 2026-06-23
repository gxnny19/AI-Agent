package kr.jgg.mealgpt.cart;

import kr.jgg.mealgpt.recipe.RecipeDto;
import kr.jgg.mealgpt.pantry.PantryUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartService {
    private final Map<String, CartItem> cart = new LinkedHashMap<>();

    public synchronized List<CartItem> add(String name, int quantity) {
        String itemName = PantryUtils.toKoreanItemName(name);
        if (itemName.isEmpty() || PantryUtils.isPantryItem(itemName)) {
            return items();
        }

        CartItem current = cart.get(itemName);
        int nextQuantity = Math.max(1, quantity) + (current == null ? 0 : current.getQuantity());
        cart.put(itemName, new CartItem(itemName, nextQuantity, buyRouteUrl(itemName), coupangSearchUrl(itemName)));
        return items();
    }

    public synchronized List<CartItem> addMissingFromRecipes(List<RecipeDto> recipes) {
        if (recipes == null) {
            return items();
        }
        for (RecipeDto recipe : recipes) {
            for (String missing : recipe.getMissingIngredients()) {
                add(missing, 1);
            }
        }
        return items();
    }

    public synchronized List<CartItem> items() {
        return new ArrayList<>(cart.values());
    }

    public synchronized List<CartItem> clear() {
        cart.clear();
        return items();
    }

    public String coupangSearchUrl(String name) {
        return "https://www.coupang.com/np/search?q=" + UriUtils.encodeQueryParam(name, StandardCharsets.UTF_8);
    }

    private String buyRouteUrl(String name) {
        return "/buy/" + UriUtils.encodePathSegment(name, StandardCharsets.UTF_8);
    }

}
