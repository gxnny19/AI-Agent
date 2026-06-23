package kr.jgg.mealgpt.api;

import kr.jgg.mealgpt.cart.CartItem;
import kr.jgg.mealgpt.cart.CartService;
import kr.jgg.mealgpt.ingredient.IngredientService;
import kr.jgg.mealgpt.recipe.RecipeDto;
import kr.jgg.mealgpt.recipe.RecipeService;
import kr.jgg.mealgpt.upload.UploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
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
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/ai-test")
    public String aiTest() {
        return "forward:/index.html";
    }

    @GetMapping("/ai-api")
    public ResponseEntity<Map<String, Object>> aiApiInfo() {
        Map<String, Object> example = new HashMap<>();
        example.put("method", "POST");
        example.put("path", "/ai-api");
        example.put("field", "file");

        Map<String, Object> body = new HashMap<>();
        body.put("message", "사진을 분석하려면 multipart/form-data로 file을 담아 POST /ai-api 또는 POST /analyze를 호출하세요.");
        body.put("example", example);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/ai-api")
    public ResponseEntity<Map<String, Object>> analyzeIngredients(@RequestParam("file") MultipartFile file) {
        IngredientService.IngredientAnalysis analysis = ingredientService.analyze(file);
        Map<String, Object> body = new HashMap<>();
        body.put("ingredients", analysis.getIngredients());
        body.put("raw", analysis.getRaw());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("filename", file.getOriginalFilename());
        body.put("saved_to", uploadService.save(file).toString());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyze(@RequestParam("file") MultipartFile file) {
        IngredientService.IngredientAnalysis analysis = ingredientService.analyze(file);
        List<RecipeDto> recipes;
        String warning = null;
        try {
            recipes = recipeService.recommend(analysis.getIngredients());
        } catch (RuntimeException ex) {
            recipes = java.util.Collections.emptyList();
            warning = "레시피 추천은 건너뜀: " + ex.getMessage();
        }
        List<CartItem> cart = cartService.addMissingFromRecipes(recipes);

        Map<String, Object> body = new HashMap<>();
        body.put("ingredients", analysis.getIngredients());
        body.put("recipes", recipes);
        body.put("cart", cart);
        body.put("raw", analysis.getRaw());
        if (warning != null) {
            body.put("warning", warning);
        }
        return ResponseEntity.ok(body);
    }

    @GetMapping("/cart")
    public ResponseEntity<Map<String, Object>> cart() {
        Map<String, Object> body = new HashMap<>();
        body.put("cart", cartService.items());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/cart/items")
    public ResponseEntity<Map<String, Object>> addCartItem(@Valid @RequestBody CartItemRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("cart", cartService.add(request.getName(), request.getQuantity()));
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/cart")
    public ResponseEntity<Map<String, Object>> clearCart() {
        Map<String, Object> body = new HashMap<>();
        body.put("cart", cartService.clear());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/buy/{name}")
    public RedirectView buy(@PathVariable String name) {
        return new RedirectView(cartService.coupangSearchUrl(name));
    }

    public static class CartItemRequest {
        @NotBlank
        private String name;
        private int quantity = 1;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}
