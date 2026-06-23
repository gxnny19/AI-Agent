package kr.jgg.mealgpt.ollama;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.jgg.mealgpt.config.MealGptProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OllamaClient {
    private final MealGptProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public OllamaClient(MealGptProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    public String chat(String prompt) {
        return chat(prompt, Collections.emptyList());
    }

    public String chat(String prompt, List<byte[]> images) {
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        if (images != null && !images.isEmpty()) {
            List<String> encodedImages = new ArrayList<>();
            for (byte[] image : images) {
                encodedImages.add(Base64.getEncoder().encodeToString(image));
            }
            message.put("images", encodedImages);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("model", properties.getOllama().getModel());
        body.put("stream", false);
        body.put("messages", Collections.singletonList(message));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            String url = trimSlash(properties.getOllama().getBaseUrl()) + "/api/chat";
            String response = restTemplate.postForObject(url, new HttpEntity<>(body, headers), String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode content = root.path("message").path("content");
            if (content.isMissingNode()) {
                throw new IllegalStateException("Ollama response did not include message.content");
            }
            return content.asText();
        } catch (HttpStatusCodeException ex) {
            throw new IllegalStateException("Ollama request failed: " + ex.getStatusCode() + " " + ex.getResponseBodyAsString(), ex);
        } catch (Exception ex) {
            throw new IllegalStateException("Ollama request failed: " + ex.getMessage(), ex);
        }
    }

    private String trimSlash(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "http://127.0.0.1:11434";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
