from fastapi import UploadFile
from ollama import ResponseError, chat

from config import MODEL_NAME
from features.pantry import to_korean_item_name
from features.text_utils import clean_text_list, extract_json_object
from features.uploads import save_upload_file


NON_INGREDIENT_NAMES = {
    "채반",
    "컵",
    "그릇",
    "접시",
    "용기",
    "봉지",
    "캔",
    "포장",
    "비닐",
    "라벨",
}


def extract_ingredients(content: str) -> list[str]:
    data = extract_json_object(content)
    ingredients = clean_text_list([
        to_korean_item_name(item)
        for item in clean_text_list(data.get("ingredients", []))
    ])

    return [
        item
        for item in ingredients
        if item and item not in NON_INGREDIENT_NAMES
    ]


def analyze_ingredients_from_image(file: UploadFile) -> dict:
    file_path = save_upload_file(file)

    try:
        response = chat(
            model=MODEL_NAME,
            messages=[
                {
                    "role": "user",
                    "content": """
업로드된 사진을 직접 보고, 사진 안에서 실제로 보이는 식재료만 분석해라.
식재료명은 반드시 짧은 한국어 일반 명사로 답변해라.
영어 단어를 사용하지 마라.
사진에서 보이지 않는 재료를 추측하거나 예시로 채우지 마라.
확실하지 않은 항목은 제외해라.
브랜드명, 용기, 포장, 조리도구는 제외해라.
채반, 컵, 그릇, 접시, 용기 같은 도구나 포장물은 ingredients에 넣지 마라.

반드시 JSON 객체만 답변해라.
형식은 {"ingredients": []} 이다.
""",
                    "images": [file_path],
                }
            ],
        )
    except ResponseError as exc:
        if getattr(exc, "status_code", None) == 404:
            raise ValueError(
                f"Ollama model '{MODEL_NAME}' is not installed. "
                f"Run: ollama pull {MODEL_NAME}"
            ) from exc
        raise

    content = response.message.content

    return {
        "ingredients": extract_ingredients(content),
        "raw": content,
    }
