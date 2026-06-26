package com.mealgpt.controller;

import com.mealgpt.model.CartItemRequest;
import com.mealgpt.model.Recipe;
import com.mealgpt.service.CartService;
import com.mealgpt.service.IngredientService;
import com.mealgpt.service.RecipeService;
import com.mealgpt.service.UploadService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@RestController
public class MealGptController {
    private final IngredientService ingredientService;
    private final RecipeService recipeService;
    private final CartService cartService;
    private final UploadService uploadService;

    public MealGptController(IngredientService ingredientService, RecipeService recipeService, CartService cartService, UploadService uploadService) {
        this.ingredientService = ingredientService;
        this.recipeService = recipeService;
        this.cartService = cartService;
        this.uploadService = uploadService;
    }

    @GetMapping("/")
    public Map<String, String> root() {
        return Map.of("message", "MealGPT Running");
    }

    @GetMapping("/ai-api")
    public Map<String, Object> aiApi() {
        return Map.of(
                "message", "사진을 분석하려면 multipart/form-data로 file을 담아 POST /ai-api 또는 POST /analyze를 호출하세요.",
                "example", Map.of("method", "POST", "path", "/ai-api", "field", "file")
        );
    }

    @PostMapping("/ai-api")
    public ResponseEntity<Map<String, Object>> analyzeIngredientsApi(@RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(ingredientService.analyzeIngredientsFromImage(file));
        } catch (Exception exc) {
            return ResponseEntity.internalServerError().body(Map.of("ingredients", List.of(), "error", exc.getMessage()));
        }
    }

    @GetMapping("/ai-test")
    public Map<String, String> test() {
        return Map.of("message", "hello");
    }

    @PostMapping("/upload")
    public Map<String, String> uploadImage(@RequestParam("file") MultipartFile file) throws Exception {
        Path saved = uploadService.saveUploadFile(file);
        return Map.of("filename", String.valueOf(file.getOriginalFilename()), "saved_to", saved.toString());
    }

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeImage(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> analysis = ingredientService.analyzeIngredientsFromImage(file);
            @SuppressWarnings("unchecked")
            List<String> ingredients = (List<String>) analysis.get("ingredients");
            List<Recipe> recipes = recipeService.recommendRecipes(ingredients);
            return ResponseEntity.ok(Map.of(
                    "ingredients", ingredients,
                    "recipes", recipes,
                    "cart", cartService.addMissingItemsToCart(recipes),
                    "raw", analysis.get("raw")
            ));
        } catch (Exception exc) {
            return ResponseEntity.internalServerError().body(Map.of("ingredients", List.of(), "error", exc.getMessage()));
        }
    }

    @GetMapping("/cart")
    public Map<String, Object> getCart() {
        return Map.of("cart", cartService.getCartItems());
    }

    @PostMapping("/cart/items")
    public Map<String, Object> addCartItem(@Valid @RequestBody CartItemRequest item) {
        return Map.of("cart", cartService.addItemToCart(item.getName(), item.getQuantity()));
    }

    @DeleteMapping("/cart")
    public Map<String, Object> clearCart() {
        return Map.of("cart", cartService.clearCartItems());
    }

    @GetMapping("/buy/{itemName}")
    public RedirectView buyItem(@PathVariable String itemName) {
        return new RedirectView(cartService.coupangSearchUrl(itemName));
    }
}
