package kr.jgg.mealgpt.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextUtils {
    private TextUtils() {
    }

    public static JsonNode extractJsonObject(String content, ObjectMapper objectMapper) {
        String text = content == null ? "" : content.trim();
        text = stripCodeFence(text);

        try {
            return objectMapper.readTree(text);
        } catch (Exception ignored) {
            Matcher matcher = Pattern.compile("\\{.*\\}", Pattern.DOTALL).matcher(text);
            if (!matcher.find()) {
                return objectMapper.createObjectNode();
            }
            try {
                return objectMapper.readTree(matcher.group(0));
            } catch (Exception secondIgnored) {
                return objectMapper.createObjectNode();
            }
        }
    }

    public static String stripCodeFence(String content) {
        if (content == null) {
            return "";
        }
        return content.trim().replaceAll("(?is)^```(?:json)?\\s*|\\s*```$", "").trim();
    }

    public static List<String> cleanTextList(Iterable<?> items) {
        List<String> cleaned = new ArrayList<>();
        if (items == null) {
            return cleaned;
        }
        for (Object item : items) {
            String text = String.valueOf(item).trim();
            if (!text.isEmpty() && !cleaned.contains(text)) {
                cleaned.add(text);
            }
        }
        return cleaned;
    }
}
