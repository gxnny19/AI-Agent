package kr.jgg.mealgpt.ingredient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.jgg.mealgpt.common.TextUtils;
import kr.jgg.mealgpt.ollama.OllamaClient;
import kr.jgg.mealgpt.pantry.PantryUtils;
import kr.jgg.mealgpt.upload.UploadService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;

@Service
public class IngredientService {
    private static final Pattern NAME_FIELD_PATTERN = Pattern.compile("\"(?:name|ingredient|이름|재료)\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern ARRAY_VALUE_PATTERN = Pattern.compile("\"([^\"]+)\"");
    private static final Set<String> NON_INGREDIENTS = new HashSet<>(Arrays.asList(
            "접시", "그릇", "컵", "쟁반", "냄비", "비닐", "포장", "용기", "봉지",
            "plate", "bowl", "cup", "tray", "pot", "plastic", "package", "container", "bag"
    ));

    private final OllamaClient ollamaClient;
    private final ObjectMapper objectMapper;
    private final UploadService uploadService;

    public IngredientService(OllamaClient ollamaClient, ObjectMapper objectMapper, UploadService uploadService) {
        this.ollamaClient = ollamaClient;
        this.objectMapper = objectMapper;
        this.uploadService = uploadService;
    }

    public IngredientAnalysis analyze(MultipartFile file) {
        try {
            byte[] imageBytes = file.getBytes();
            uploadService.save(file);
            return analyzeBytes(imageBytes);
        } catch (Exception ex) {
            throw new IllegalStateException("이미지 재료 분석에 실패했습니다: " + ex.getMessage(), ex);
        }
    }

    public IngredientAnalysis analyzeUploadedFile(Path file) {
        try {
            return analyzeBytes(Files.readAllBytes(file));
        } catch (Exception ex) {
            throw new IllegalStateException("업로드 이미지 재료 분석에 실패했습니다: " + ex.getMessage(), ex);
        }
    }

    private IngredientAnalysis analyzeBytes(byte[] imageBytes) throws Exception {
        String raw = ollamaClient.chat(visionPrompt(), Arrays.asList(imageBytes));
        return new IngredientAnalysis(extractIngredients(raw), raw);
    }

    private List<String> extractIngredients(String content) throws Exception {
        JsonNode root = TextUtils.extractJsonObject(content, objectMapper);
        JsonNode items = root.path("ingredients");

        LinkedHashSet<String> ingredients = new LinkedHashSet<>();
        if (items.isArray()) {
            for (JsonNode item : items) {
                if (item.isObject()) {
                    addIngredient(ingredients, firstText(item, "name", "ingredient", "이름", "재료"));
                } else {
                    addIngredient(ingredients, item.asText());
                }
            }
        }

        if (ingredients.isEmpty()) {
            extractByFieldName(ingredients, content);
        }

        if (ingredients.isEmpty()) {
            extractByIngredientsArray(ingredients, content);
        }

        return new ArrayList<>(ingredients);
    }

    private void extractByFieldName(Set<String> ingredients, String content) {
        Matcher matcher = NAME_FIELD_PATTERN.matcher(content == null ? "" : content);
        while (matcher.find()) {
            addIngredient(ingredients, matcher.group(1));
        }
    }

    private void extractByIngredientsArray(Set<String> ingredients, String content) {
        String text = TextUtils.stripCodeFence(content);
        int keyIndex = text.indexOf("\"ingredients\"");
        if (keyIndex < 0) {
            return;
        }
        int start = text.indexOf('[', keyIndex);
        int end = text.indexOf(']', start);
        if (start < 0 || end <= start) {
            return;
        }
        Matcher matcher = ARRAY_VALUE_PATTERN.matcher(text.substring(start + 1, end));
        while (matcher.find()) {
            addIngredient(ingredients, matcher.group(1));
        }
    }

    private void addIngredient(Set<String> ingredients, String rawName) {
        String value = PantryUtils.toKoreanItemName(rawName);
        if (value == null) {
            return;
        }
        // Filter out empty, non-ingredient and likely-hallucinated values.
        if (value.isEmpty() || isNonIngredient(value)) {
            return;
        }
        // If the item is not a known mapping and the returned value is not Korean,
        // treat it as potentially hallucinated and skip it. This reduces false
        // positives like "plate", "spoon" or model guesses.
        boolean hasMapping = PantryUtils.hasMapping(rawName);
        if (!hasMapping && !value.matches(".*[가-힣].*")) {
            return;
        }
        ingredients.add(value);
    }

    private boolean isNonIngredient(String value) {
        if (NON_INGREDIENTS.contains(value)) {
            return true;
        }
        String normalized = PantryUtils.normalizeItemName(value);
        for (String nonIngredient : NON_INGREDIENTS) {
            if (normalized.equals(PantryUtils.normalizeItemName(nonIngredient))) {
                return true;
            }
        }
        return false;
    }

    private String firstText(JsonNode node, String... keys) {
        for (String key : keys) {
            String value = node.path(key).asText("");
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return "";
    }

    private String visionPrompt() {
        return "You are an ingredient extraction engine for refrigerator photos.\n"
                + "Look at the uploaded image and extract only visible edible ingredients.\n"
                + "Return Korean common ingredient names only.\n"
                + "Do not include quantities, units, descriptions, brands, containers, packaging, dishes, cups, trays, tools, or uncertain guesses.\n"
                + "Do not return objects. Do not return markdown. Do not add explanations.\n"
                + "Return exactly one valid JSON object with this schema:\n"
                + "{\"ingredients\":[\"계란\",\"양상추\",\"파프리카\"]}\n"
                + "If no ingredient is visible, return {\"ingredients\":[]}.";
    }

    public static class IngredientAnalysis {
        private final List<String> ingredients;
        private final String raw;

        public IngredientAnalysis(List<String> ingredients, String raw) {
            this.ingredients = ingredients;
            this.raw = raw;
        }

        public List<String> getIngredients() {
            return ingredients;
        }

        public String getRaw() {
            return raw;
        }
    }
}
