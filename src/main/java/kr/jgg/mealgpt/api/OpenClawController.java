package kr.jgg.mealgpt.api;

import kr.jgg.mealgpt.cart.CartItem;
import kr.jgg.mealgpt.cart.CartService;
import kr.jgg.mealgpt.config.MealGptProperties;
import kr.jgg.mealgpt.ingredient.IngredientService;
import kr.jgg.mealgpt.ollama.OllamaClient;
import kr.jgg.mealgpt.recipe.RecipeDto;
import kr.jgg.mealgpt.recipe.RecipeService;
import kr.jgg.mealgpt.upload.UploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/openclaw")
public class OpenClawController {
    private final OllamaClient ollamaClient;
    private final MealGptProperties properties;
    private final UploadService uploadService;
    private final IngredientService ingredientService;
    private final RecipeService recipeService;
    private final CartService cartService;

    public OpenClawController(
            OllamaClient ollamaClient,
            MealGptProperties properties,
            UploadService uploadService,
            IngredientService ingredientService,
            RecipeService recipeService,
            CartService cartService
    ) {
        this.ollamaClient = ollamaClient;
        this.properties = properties;
        this.uploadService = uploadService;
        this.ingredientService = ingredientService;
        this.recipeService = recipeService;
        this.cartService = cartService;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        Map<String, Object> body = new HashMap<>();
        body.put("status", "ok");
        body.put("ollama_model", properties.getOllama().getModel());
        body.put("ollama_base_url", properties.getOllama().getBaseUrl());
        body.put("spoonacular_configured", recipeService.isSpoonacularConfigured());
        body.put("latest_upload", uploadService.latestUpload().map(path -> path.getFileName().toString()).orElse(null));
        body.put("cart", cartService.items());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/message")
    public ResponseEntity<Map<String, Object>> message(
            @RequestHeader(value = "X-OpenClaw-Secret", required = false) String secret,
            @RequestBody OpenClawMessage request
    ) {
        if (!authorized(secret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "invalid OpenClaw secret"));
        }

        String prompt = request.getMessage() == null ? "" : request.getMessage().trim();
        if (prompt.isEmpty()) {
            prompt = "MealGPT 상태를 짧게 알려줘.";
        }

        String answer = ollamaClient.chat(prompt);
        Map<String, Object> body = new HashMap<>();
        body.put("reply", answer);
        body.put("model", properties.getOllama().getModel());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/analyze-upload")
    public ResponseEntity<Map<String, Object>> analyzeUpload(
            @RequestHeader(value = "X-OpenClaw-Secret", required = false) String secret,
            @RequestBody OpenClawAnalyzeUploadRequest request
    ) {
        if (!authorized(secret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "invalid OpenClaw secret"));
        }

        java.nio.file.Path file = uploadService.resolveUploadedFile(request == null ? null : request.getFilename());
        IngredientService.IngredientAnalysis analysis = ingredientService.analyzeUploadedFile(file);

        List<RecipeDto> recipes;
        String warning = null;
        try {
            recipes = recipeService.recommend(analysis.getIngredients());
        } catch (RuntimeException ex) {
            recipes = Collections.emptyList();
            warning = "레시피 추천은 건너뜀: " + ex.getMessage();
        }

        List<CartItem> cart = cartService.addMissingFromRecipes(recipes);
        Map<String, Object> body = new HashMap<>();
        body.put("filename", file.getFileName().toString());
        body.put("ingredients", analysis.getIngredients());
        body.put("recipes", recipes);
        body.put("cart", cart);
        body.put("raw", analysis.getRaw());
        if (warning != null) {
            body.put("warning", warning);
        }
        return ResponseEntity.ok(body);
    }

    private boolean authorized(String secret) {
        String expected = properties.getOpenclaw().getSharedSecret();
        return expected == null || expected.trim().isEmpty() || expected.equals(secret);
    }

    public static class OpenClawMessage {
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class OpenClawAnalyzeUploadRequest {
        private String filename;

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }
    }
}
