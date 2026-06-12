import json

from ollama import chat

from config import MODEL_NAME
from features.pantry import (
    is_available_item,
    is_pantry_item,
    normalize_purchase_item,
    to_korean_item_name,
)
from features.text_utils import clean_text_list, extract_json_object


FALLBACK_RECIPES = [
    {
        "name": "계란 양파 볶음밥",
        "description": "냉장고 기본 재료로 빠르게 만들 수 있는 한 그릇 메뉴",
        "ingredients": ["밥", "계란", "양파", "대파", "간장", "식용유"],
        "steps": ["양파와 대파를 잘게 썬다.", "팬에 식용유를 두르고 채소를 볶는다.", "계란과 밥을 넣고 간장으로 간한다."],
    },
    {
        "name": "우유 계란 프렌치토스트",
        "description": "우유와 계란이 보이면 추천하기 좋은 간단한 브런치",
        "ingredients": ["식빵", "계란", "우유", "설탕", "버터"],
        "steps": ["계란, 우유, 설탕을 섞는다.", "식빵을 적신 뒤 팬에 굽는다.", "버터를 올려 마무리한다."],
    },
    {
        "name": "감자 양파 된장국",
        "description": "채소가 남았을 때 끓이기 좋은 따뜻한 국물 요리",
        "ingredients": ["감자", "양파", "두부", "된장", "대파"],
        "steps": ["물에 된장을 풀고 끓인다.", "감자와 양파를 넣어 익힌다.", "두부와 대파를 넣고 한 번 더 끓인다."],
    },
    {
        "name": "닭가슴살 채소 볶음",
        "description": "단백질과 채소를 같이 쓰는 담백한 반찬",
        "ingredients": ["닭가슴살", "양파", "파프리카", "마늘", "간장"],
        "steps": ["재료를 먹기 좋게 썬다.", "닭가슴살을 먼저 익힌다.", "채소와 양념을 넣고 빠르게 볶는다."],
    },
    {
        "name": "토마토 계란 볶음",
        "description": "토마토와 계란만 있어도 맛이 잘 나는 초간단 요리",
        "ingredients": ["토마토", "계란", "대파", "소금", "후추"],
        "steps": ["계란을 부드럽게 스크램블한다.", "토마토와 대파를 볶는다.", "계란을 다시 넣고 소금, 후추로 간한다."],
    },
]


def missing_ingredients(required: list[str], available: list[str]) -> list[str]:
    missing = []

    for item in required:
        item = to_korean_item_name(item)
        if not item or is_pantry_item(item):
            continue
        if not is_available_item(item, available) and item not in missing:
            missing.append(item)

    return missing


def normalize_recipe(recipe: dict, available: list[str]) -> dict:
    ingredients = clean_text_list([
        to_korean_item_name(item)
        for item in clean_text_list(recipe.get("ingredients", []))
    ])
    steps = clean_text_list(recipe.get("steps", []))
    missing = clean_text_list([
        to_korean_item_name(item)
        for item in clean_text_list(recipe.get("missing_ingredients", []))
    ])

    if not missing:
        missing = missing_ingredients(ingredients, available)
    else:
        filtered_missing = []
        for item in missing:
            item = to_korean_item_name(item)
            if not item or is_pantry_item(item) or is_available_item(item, available):
                continue
            if item not in filtered_missing:
                filtered_missing.append(item)
        missing = filtered_missing

    return {
        "name": str(recipe.get("name", "추천 레시피")).strip() or "추천 레시피",
        "description": str(recipe.get("description", "")).strip(),
        "ingredients": ingredients,
        "missing_ingredients": missing,
        "steps": steps,
    }


def fallback_recommend_recipes(ingredients: list[str]) -> list[dict]:
    ingredient_set = {item.lower() for item in ingredients}
    scored = []

    for recipe in FALLBACK_RECIPES:
        required = recipe["ingredients"]
        overlap = len({item.lower() for item in required} & ingredient_set)
        missing = missing_ingredients(required, ingredients)
        score = overlap * 3 - len(missing)
        scored.append((score, normalize_recipe({**recipe, "missing_ingredients": missing}, ingredients)))

    scored.sort(key=lambda item: item[0], reverse=True)
    return [recipe for _, recipe in scored[:3]]


def recommend_recipes(ingredients: list[str]) -> list[dict]:
    if not ingredients:
        return []

    try:
        response = chat(
            model=MODEL_NAME,
            messages=[
                {
                    "role": "user",
                    "content": f"""
아래 냉장고 식재료로 만들기 좋은 레시피 3개를 추천해라.
부족한 재료가 있으면 missing_ingredients에 넣어라.
소금, 설탕, 후추, 물, 식용유, 간장, 고추장, 된장 같은 기본 양념은 부족 재료에서 제외해라.
이미 냉장고 식재료에 있는 재료는 missing_ingredients에 넣지 마라.
모든 값은 한국어로 답변해라.

냉장고 식재료:
{json.dumps(ingredients, ensure_ascii=False)}

반드시 아래 JSON 형식으로만 답변해라.

{{
  "recipes": [
    {{
      "name": "레시피명",
      "description": "짧은 설명",
      "ingredients": ["필요 재료"],
      "missing_ingredients": ["부족 재료"],
      "steps": ["조리 단계"]
    }}
  ]
}}
""",
                }
            ],
        )

        data = extract_json_object(response.message.content)
        recipes = data.get("recipes", [])
        if not isinstance(recipes, list):
            return fallback_recommend_recipes(ingredients)

        normalized = [
            normalize_recipe(recipe, ingredients)
            for recipe in recipes
            if isinstance(recipe, dict)
        ]

        return normalized[:3] or fallback_recommend_recipes(ingredients)

    except Exception as e:
        print("RECIPE ERROR:", e)
        return fallback_recommend_recipes(ingredients)
