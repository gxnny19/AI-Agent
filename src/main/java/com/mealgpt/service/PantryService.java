package com.mealgpt.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class PantryService {
    private static final List<String> PANTRY_ITEMS = List.of(
            "물", "소금", "설탕", "후추", "식용유", "참기름", "간장", "고추장", "된장", "식초",
            "water", "salt", "sugar", "pepper", "oil", "cooking oil", "soy sauce", "vinegar"
    );

    private static final Map<String, String> KOREAN_ITEM_NAMES = new LinkedHashMap<>();

    static {
        KOREAN_ITEM_NAMES.put("egg", "계란");
        KOREAN_ITEM_NAMES.put("eggs", "계란");
        KOREAN_ITEM_NAMES.put("onion", "양파");
        KOREAN_ITEM_NAMES.put("onions", "양파");
        KOREAN_ITEM_NAMES.put("milk", "우유");
        KOREAN_ITEM_NAMES.put("berry", "베리류");
        KOREAN_ITEM_NAMES.put("berries", "베리류");
        KOREAN_ITEM_NAMES.put("strawberry", "딸기");
        KOREAN_ITEM_NAMES.put("strawberries", "딸기");
        KOREAN_ITEM_NAMES.put("blueberry", "블루베리");
        KOREAN_ITEM_NAMES.put("blueberries", "블루베리");
        KOREAN_ITEM_NAMES.put("grape", "포도");
        KOREAN_ITEM_NAMES.put("grapes", "포도");
        KOREAN_ITEM_NAMES.put("tomato", "토마토");
        KOREAN_ITEM_NAMES.put("tomatoes", "토마토");
        KOREAN_ITEM_NAMES.put("spinach", "시금치");
        KOREAN_ITEM_NAMES.put("kale", "케일");
        KOREAN_ITEM_NAMES.put("carrot", "당근");
        KOREAN_ITEM_NAMES.put("carrots", "당근");
        KOREAN_ITEM_NAMES.put("cucumber", "오이");
        KOREAN_ITEM_NAMES.put("cheese", "치즈");
        KOREAN_ITEM_NAMES.put("fruit salad", "과일 샐러드");
        KOREAN_ITEM_NAMES.put("bread", "빵");
        KOREAN_ITEM_NAMES.put("toast", "토스트");
        KOREAN_ITEM_NAMES.put("butter", "버터");
        KOREAN_ITEM_NAMES.put("rice", "밥");
        KOREAN_ITEM_NAMES.put("green onion", "대파");
        KOREAN_ITEM_NAMES.put("scallion", "대파");
        KOREAN_ITEM_NAMES.put("garlic", "마늘");
        KOREAN_ITEM_NAMES.put("potato", "감자");
        KOREAN_ITEM_NAMES.put("potatoes", "감자");
        KOREAN_ITEM_NAMES.put("tofu", "두부");
        KOREAN_ITEM_NAMES.put("chicken", "닭고기");
        KOREAN_ITEM_NAMES.put("chicken breast", "닭가슴살");
        KOREAN_ITEM_NAMES.put("paprika", "파프리카");
        KOREAN_ITEM_NAMES.put("meat", "고기");
        KOREAN_ITEM_NAMES.put("vegetable", "채소");
        KOREAN_ITEM_NAMES.put("vegetables", "채소");
        KOREAN_ITEM_NAMES.put("salad greens", "샐러드 채소");
        KOREAN_ITEM_NAMES.put("seaweed", "김");
        KOREAN_ITEM_NAMES.put("seasoning", "양념");
    }

    public String normalizeItemName(String item) {
        String text = item == null ? "" : item.toLowerCase(Locale.ROOT);
        text = Pattern.compile("\\([^)]*\\)").matcher(text).replaceAll("");
        text = Pattern.compile("[^0-9a-z가-힣]+").matcher(text).replaceAll("");
        if (text.length() > 3 && text.endsWith("s")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }

    public String toKoreanItemName(Object item) {
        String text = normalizePurchaseItem(item);
        if (text.isBlank()) {
            return "";
        }

        String normalized = normalizeItemName(text);
        for (Map.Entry<String, String> entry : KOREAN_ITEM_NAMES.entrySet()) {
            if (normalized.equals(normalizeItemName(entry.getKey()))) {
                return entry.getValue();
            }
        }
        for (Map.Entry<String, String> entry : KOREAN_ITEM_NAMES.entrySet()) {
            String english = normalizeItemName(entry.getKey());
            if (!english.isBlank() && normalized.contains(english)) {
                return entry.getValue();
            }
        }
        return text;
    }

    public boolean isPantryItem(String item) {
        String normalized = normalizeItemName(item);
        return PANTRY_ITEMS.stream().anyMatch(pantry -> normalized.equals(normalizeItemName(pantry)));
    }

    public boolean isAvailableItem(String item, List<String> available) {
        String target = normalizeItemName(item);
        if (target.isBlank()) {
            return false;
        }
        for (String availableItem : available) {
            String candidate = normalizeItemName(availableItem);
            if (target.equals(candidate) || target.contains(candidate) || candidate.contains(target)) {
                return true;
            }
        }
        return false;
    }

    public String normalizePurchaseItem(Object item) {
        String text = String.valueOf(item == null ? "" : item).trim();
        text = Pattern.compile("\\([^)]*\\)").matcher(text).replaceAll("").trim();
        text = Pattern.compile("\\s*(?:또는|/|,|·)\\s*").split(text)[0].trim();
        text = Pattern.compile("\\s+").matcher(text).replaceAll(" ");
        if (text.isBlank() || text.contains("기타") || text.contains("선택") || text.contains("옵션") || text.contains("적당량")) {
            return "";
        }
        return text;
    }
}
