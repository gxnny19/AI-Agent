# MealGPT

MealGPT는 냉장고나 식재료 사진을 업로드하면 Ollama 비전 모델로 재료를 인식하고, Spoonacular API로 추천 레시피와 부족한 재료 장바구니를 만들어 주는 Java Spring Boot 앱입니다.

## 주요 기능

- 이미지 업로드 및 미리보기
- Ollama 비전 모델 기반 식재료 인식
- 한국어 식재료명 정규화
- 보유 재료 기반 레시피 추천
- 부족한 재료 자동 장바구니 추가
- 장바구니 항목별 쿠팡 검색 링크 연결

## 기술 스택

- Java 17
- Spring Boot 3
- Maven
- Ollama HTTP API
- HTML/CSS/JavaScript

## 사전 준비

Ollama가 실행 중이어야 하며, 기본 모델은 `llama3.2-vision:latest`입니다.

```powershell
ollama pull llama3.2-vision:latest
```

Spoonacular 레시피 검색을 사용하려면 환경 변수를 설정하세요.

```powershell
$env:SPOONACULAR_API_KEY="your-api-key"
```

## 실행 방법

Java 17과 Maven을 설치한 뒤 실행합니다. 이 저장소에서는 Maven 3.9.16을 `tools/apache-maven-3.9.16`에 내려받아 사용할 수 있게 준비했습니다.

```powershell
mvn spring-boot:run
```

현재 PowerShell 세션에서 `mvn`이 바로 잡히지 않으면 아래처럼 실행하세요.

```powershell
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
$env:Path="$env:JAVA_HOME\bin;$PWD\tools\apache-maven-3.9.16\bin;$env:Path"
mvn spring-boot:run
```

브라우저에서 아래 주소를 엽니다.

```text
http://127.0.0.1:8000/ai-page
```

## API

- `GET /`: 상태 확인
- `GET /ai-test`: API 테스트 JSON
- `GET /ai-page`: MealGPT UI
- `GET /ai-api`: 이미지 분석 API 사용 안내
- `POST /ai-api`: 이미지에서 식재료만 분석
- `POST /upload`: 이미지 저장
- `POST /analyze`: 이미지 분석, 레시피 추천, 부족 재료 장바구니 추가
- `GET /cart`: 장바구니 조회
- `POST /cart/items`: 재료 수동 장바구니 추가
- `DELETE /cart`: 장바구니 비우기
- `GET /buy/{itemName}`: 쿠팡 검색 페이지로 리다이렉트

## 테스트

```powershell
mvn test
```
