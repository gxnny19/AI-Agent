package com.mealgpt.service;

import com.mealgpt.config.MealGptProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class IngredientService {
    private static final Set<String> NON_INGREDIENT_NAMES = Set.of(
            "채반", "컵", "그릇", "접시", "용기", "봉지", "캔", "포장", "비닐", "얼음"
    );

    private final MealGptProperties properties;
    private final UploadService uploadService;
    private final PantryService pantryService;
    private final TextUtils textUtils;
    private final RestClient restClient;

    public IngredientService(
            MealGptProperties properties,
            UploadService uploadService,
            PantryService pantryService,
            TextUtils textUtils,
            RestClient.Builder restClientBuilder
    ) {
        this.properties = properties;
        this.uploadService = uploadService;
        this.pantryService = pantryService;
        this.textUtils = textUtils;
        this.restClient = restClientBuilder.baseUrl("http://localhost:11434").build();
    }

    public Map<String, Object> analyzeIngredientsFromImage(MultipartFile file) throws Exception {
        Path filePath = uploadService.saveUploadFile(file);
        String base64Image = Base64.getEncoder().encodeToString(Files.readAllBytes(filePath));
        String prompt = """
                업로드된 사진을 직접 보고, 사진 안에 실제로 보이는 식재료만 분석해라.
                식재료명은 반드시 짧은 한국어 일반 명사로 응답해라.
                영어 단어를 사용하지 마라.
                사진에서 보이지 않는 재료를 추측하거나 예시로 채우지 마라.
                확실하지 않은 항목은 제외해라.
                브랜드명, 용기, 포장, 조리도구는 제외해라.
                반드시 JSON 객체만 응답해라.
                형식은 {"ingredients": []} 이다.
                """;

        Map<String, Object> request = Map.of(
                "model", properties.getOllamaModel(),
                "stream", false,
                "messages", List.of(Map.of(
                        "role", "user",
                        "content", prompt,
                        "images", List.of(base64Image)
                ))
        );

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                    .uri("/api/chat")
                    .body(request)
                    .retrieve()
                    .body(Map.class);

            Object messageValue = response == null ? Map.of() : response.get("message");
            Map<?, ?> message = messageValue instanceof Map<?, ?> map ? map : Map.of();
            Object contentValue = message.get("content");
            String content = contentValue == null ? "" : String.valueOf(contentValue);
            return Map.of("ingredients", extractIngredients(content), "raw", content);
        } catch (HttpClientErrorException exc) {
            if (exc.getStatusCode().value() == 404) {
                throw new IllegalArgumentException("Ollama model '" + properties.getOllamaModel()
                        + "' is not installed. Run: ollama pull " + properties.getOllamaModel(), exc);
            }
            throw exc;
        }
    }

    public List<String> extractIngredients(String content) {
        Map<String, Object> data = textUtils.extractJsonObject(content);
        return textUtils.cleanTextList(data.get("ingredients")).stream()
                .map(pantryService::toKoreanItemName)
                .filter(item -> !item.isBlank())
                .filter(item -> !NON_INGREDIENT_NAMES.contains(item))
                .distinct()
                .toList();
    }
}
