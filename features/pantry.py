import re


PANTRY_ITEMS = {
    "물",
    "소금",
    "설탕",
    "후추",
    "식용유",
    "참기름",
    "간장",
    "고추장",
    "된장",
    "식초",
    "water",
    "salt",
    "sugar",
    "pepper",
    "oil",
    "cooking oil",
    "soy sauce",
    "vinegar",
}

KOREAN_ITEM_NAMES = {
    "egg": "계란",
    "eggs": "계란",
    "onion": "양파",
    "onions": "양파",
    "milk": "우유",
    "berry": "베리류",
    "berries": "베리류",
    "strawberry": "딸기",
    "strawberries": "딸기",
    "blueberry": "블루베리",
    "blueberries": "블루베리",
    "grape": "포도",
    "grapes": "포도",
    "tomato": "토마토",
    "tomatoes": "토마토",
    "spinach": "시금치",
    "kale": "케일",
    "carrot": "당근",
    "carrots": "당근",
    "cucumber": "오이",
    "cheese": "치즈",
    "fruit salad": "과일 샐러드",
    "bread": "빵",
    "toast": "식빵",
    "butter": "버터",
    "rice": "밥",
    "green onion": "대파",
    "scallion": "대파",
    "garlic": "마늘",
    "potato": "감자",
    "potatoes": "감자",
    "tofu": "두부",
    "chicken": "닭고기",
    "chicken breast": "닭가슴살",
    "paprika": "파프리카",
    "meat": "고기",
    "vegetable": "채소",
    "vegetables": "채소",
    "salad greens": "샐러드 채소",
    "seaweed": "해조류",
    "seasoning": "양념",
}


def normalize_item_name(item: str) -> str:
    text = item.lower()
    text = re.sub(r"\([^)]*\)", "", text)
    text = re.sub(r"[^0-9a-z가-힣]+", "", text)
    if len(text) > 3 and text.endswith("s"):
        text = text[:-1]
    return text


def to_korean_item_name(item: str) -> str:
    text = normalize_purchase_item(item)
    if not text:
        return ""

    normalized = normalize_item_name(text)
    compact_names = {
        normalize_item_name(english): korean
        for english, korean in KOREAN_ITEM_NAMES.items()
    }

    if normalized in compact_names:
        return compact_names[normalized]

    for english, korean in compact_names.items():
        if english and english in normalized:
            return korean

    return text


def is_pantry_item(item: str) -> bool:
    normalized = normalize_item_name(item)
    return any(normalized == normalize_item_name(pantry) for pantry in PANTRY_ITEMS)


def is_available_item(item: str, available: list[str]) -> bool:
    target = normalize_item_name(item)
    if not target:
        return False

    for available_item in available:
        candidate = normalize_item_name(available_item)
        if target == candidate or target in candidate or candidate in target:
            return True

    return False


def normalize_purchase_item(item: str) -> str:
    text = str(item).strip()
    text = re.sub(r"\([^)]*\)", "", text).strip()
    text = re.split(r"\s*(?:또는|/|,|·)\s*", text)[0].strip()
    text = re.sub(r"\s+", " ", text)

    if not text or any(word in text for word in ["기타", "선택", "옵션", "적당량"]):
        return ""

    return text
