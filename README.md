# MealGPT

MealGPT는 냉장고 사진을 업로드하면 AI가 식재료를 인식하고, 보유 재료 기반 레시피를 추천하며, 필요한 재료를 장바구니에 담아 쿠팡 검색 구매 링크로 연결하는 FastAPI 앱입니다.

## 주요 기능

- 냉장고 사진 업로드 및 미리보기
- Ollama 비전 모델 기반 식재료 인식
- 한국어 식재료명 정규화
- 보유 재료 기반 레시피 추천
- 부족한 재료 자동 장바구니 추가
- 인식된 식재료 클릭 시 장바구니 추가
- 장바구니 항목별 쿠팡 검색 페이지 라우팅
- MealGPT 브랜드 UI와 로딩 모션

## 기술 스택

- Python
- FastAPI
- Ollama
- HTML/CSS/JavaScript

## 사전 준비

Ollama가 설치되어 있어야 하며, 기본 모델은 `gemma4:latest`입니다.

```powershell
ollama pull gemma4:latest
```

## 실행 방법

```powershell
python -m venv venv
.\venv\Scripts\activate
pip install -r requirements.txt
uvicorn main:app --host 127.0.0.1 --port 8000
```

브라우저에서 아래 주소를 엽니다.

```text
http://127.0.0.1:8000/ai-test
```

## API

- `GET /ai-test`: MealGPT 웹 UI
- `POST /analyze`: 이미지 분석, 레시피 추천, 부족 재료 장바구니 추가
- `POST /ai-api`: 이미지에서 식재료만 분석
- `GET /cart`: 장바구니 조회
- `POST /cart/items`: 재료 단건 장바구니 추가
- `DELETE /cart`: 장바구니 비우기
- `GET /buy/{item_name}`: 쿠팡 검색 페이지로 리다이렉트

## 프로젝트 구조

```text
.
├── main.py
├── config.py
├── features/
│   ├── cart.py
│   ├── ingredients.py
│   ├── pantry.py
│   ├── recipes.py
│   ├── text_utils.py
│   └── uploads.py
├── pages/
│   └── fridge_page.py
└── static/
    └── assets/
        └── meal-gpt-logo.svg
```

## 주의

`.env`, `venv`, 업로드 이미지, 캐시 파일은 저장소에 포함하지 않습니다.
