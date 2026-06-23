package kr.jgg.mealgpt.pantry;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class PantryUtils {
    private static final Pattern PARENS = Pattern.compile("\\([^)]*\\)");
    private static final Pattern NON_ITEM_CHARS = Pattern.compile("[^0-9a-z가-힣]+");
    private static final Pattern SPLIT_PURCHASE = Pattern.compile("\\s*(?:또는|/|,|·)\\s*");
    private static final Pattern SPACES = Pattern.compile("\\s+");

    private static final Set<String> PANTRY_ITEMS = new HashSet<>(Arrays.asList(
            "물", "소금", "설탕", "후추", "식용유", "참기름", "간장", "고추장", "된장", "식초",
            "water", "salt", "sugar", "pepper", "oil", "cooking oil", "soy sauce", "vinegar"
    ));

    private static final Map<String, String> KOREAN_ITEM_NAMES = new HashMap<>();
    private static final Map<String, String> KOREAN_ALIASES = new HashMap<>();
    private static final Map<String, String> COMPACT_NAMES = new HashMap<>();

    static {
        put("egg", "계란");
        put("eggs", "계란");
        put("onion", "양파");
        put("onions", "양파");
        put("milk", "우유");
        put("berry", "베리류");
        put("berries", "베리류");
        put("strawberry", "딸기");
        put("strawberries", "딸기");
        put("blueberry", "블루베리");
        put("blueberries", "블루베리");
        put("grape", "포도");
        put("grapes", "포도");
        put("apple", "사과");
        put("apples", "사과");
        put("orange", "오렌지");
        put("oranges", "오렌지");
        put("pineapple", "파인애플");
        put("banana", "바나나");
        put("bananas", "바나나");
        put("lettuce", "상추");
        put("romaine", "로메인");
        put("romaine lettuce", "로메인");
        put("iceberg lettuce", "양상추");
        put("cabbage", "양배추");
        put("tomato", "토마토");
        put("tomatoes", "토마토");
        put("spinach", "시금치");
        put("kale", "케일");
        put("carrot", "당근");
        put("carrots", "당근");
        put("cucumber", "오이");
        put("cheese", "치즈");
        put("fruit salad", "과일 샐러드");
        put("bread", "빵");
        put("toast", "식빵");
        put("butter", "버터");
        put("rice", "밥");
        put("green onion", "대파");
        put("scallion", "대파");
        put("garlic", "마늘");
        put("garlic bulb", "마늘");
        put("potato", "감자");
        put("potatoes", "감자");
        put("tofu", "두부");
        put("chicken", "닭고기");
        put("chicken breast", "닭가슴살");
        put("paprika", "파프리카");
        put("bell pepper", "파프리카");
        put("red bell pepper", "파프리카");
        put("yellow bell pepper", "파프리카");
        put("green bell pepper", "피망");
        put("pepper", "피망");
        put("meat", "고기");
        put("vegetable", "채소");
        put("vegetables", "채소");
        put("salad greens", "샐러드 채소");
        put("leafy greens", "잎채소");
        put("celery", "셀러리");
        put("celery stalks", "셀러리");
        put("corn", "옥수수");
        put("broccoli", "브로콜리");
        put("broccoli head", "브로콜리");
        put("red cabbage", "적양배추");
        put("seaweed", "해조류");
        put("seasoning", "양념");

        alias("오렌", "오렌지");
        alias("오랜지", "오렌지");
        alias("양상치", "양상추");
        alias("상치", "상추");
        alias("파프리카", "파프리카");
        alias("피망", "피망");
        alias("브로콜리", "브로콜리");
        alias("시금치", "시금치");
        alias("시간치", "시금치");
        alias("오아이", "오이");
        alias("오의", "오이");

        for (Map.Entry<String, String> entry : KOREAN_ITEM_NAMES.entrySet()) {
            COMPACT_NAMES.put(normalizeItemName(entry.getKey()), entry.getValue());
        }
        for (Map.Entry<String, String> entry : KOREAN_ALIASES.entrySet()) {
            COMPACT_NAMES.put(normalizeItemName(entry.getKey()), entry.getValue());
        }
    }

    private PantryUtils() {
    }

    public static String normalizeItemName(String item) {
        String text = item == null ? "" : item.toLowerCase(Locale.ROOT);
        text = PARENS.matcher(text).replaceAll("");
        text = NON_ITEM_CHARS.matcher(text).replaceAll("");
        if (text.length() > 3 && text.endsWith("s")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }

    public static String toKoreanItemName(String item) {
        String text = normalizePurchaseItem(item);
        if (text.isEmpty()) {
            return "";
        }

        String normalized = normalizeItemName(text);
        if (KOREAN_ALIASES.containsKey(text)) {
            return KOREAN_ALIASES.get(text);
        }

        if (COMPACT_NAMES.containsKey(normalized)) {
            return COMPACT_NAMES.get(normalized);
        }

        for (Map.Entry<String, String> entry : COMPACT_NAMES.entrySet()) {
            if (!entry.getKey().isEmpty() && normalized.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return text;
    }

    public static boolean isPantryItem(String item) {
        String normalized = normalizeItemName(item);
        for (String pantry : PANTRY_ITEMS) {
            if (normalized.equals(normalizeItemName(pantry))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAvailableItem(String item, List<String> available) {
        String target = normalizeItemName(item);
        if (target.isEmpty() || available == null) {
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

    public static String normalizePurchaseItem(String item) {
        String text = item == null ? "" : String.valueOf(item).trim();
        text = PARENS.matcher(text).replaceAll("").trim();
        text = SPLIT_PURCHASE.split(text, 2)[0].trim();
        text = SPACES.matcher(text).replaceAll(" ");
        if (text.isEmpty() || text.contains("기타") || text.contains("선택") || text.contains("옵션") || text.contains("적당량")) {
            return "";
        }
        return text;
    }

    private static void put(String english, String korean) {
        KOREAN_ITEM_NAMES.put(english, korean);
    }

    private static void alias(String source, String target) {
        KOREAN_ALIASES.put(source, target);
    }
}
