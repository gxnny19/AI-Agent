package com.mealgpt.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TextUtils {
    private final ObjectMapper objectMapper;

    public TextUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> extractJsonObject(String content) {
        if (content == null || content.isBlank()) {
            return Collections.emptyMap();
        }

        String cleaned = content.strip().replaceAll("(?is)^```(?:json)?\\s*|\\s*```$", "");
        Map<String, Object> direct = parseObject(cleaned);
        if (!direct.isEmpty()) {
            return direct;
        }

        Matcher matcher = Pattern.compile("\\{.*}", Pattern.DOTALL).matcher(cleaned);
        if (!matcher.find()) {
            return Collections.emptyMap();
        }
        return parseObject(matcher.group());
    }

    public List<String> cleanTextList(Object items) {
        if (!(items instanceof List<?> list)) {
            return List.of();
        }

        List<String> cleaned = new ArrayList<>();
        for (Object item : list) {
            String text = String.valueOf(item).trim();
            if (!text.isBlank() && !cleaned.contains(text)) {
                cleaned.add(text);
            }
        }
        return cleaned;
    }

    private Map<String, Object> parseObject(String content) {
        try {
            return objectMapper.readValue(content, new TypeReference<LinkedHashMap<String, Object>>() {
            });
        } catch (Exception ignored) {
            return Collections.emptyMap();
        }
    }
}
