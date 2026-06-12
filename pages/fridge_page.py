HTML_PAGE = """
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>MealGPT</title>
    <link rel="icon" href="/static/assets/meal-gpt-logo.svg" type="image/svg+xml">
    <style>
        :root {
            color-scheme: light;
            font-family: Inter, Arial, "Malgun Gothic", sans-serif;
            --bg: #eaf3ff;
            --surface: #fafcff;
            --surface-strong: #ffffff;
            --ink: #102e55;
            --muted: #5b7291;
            --line: #bbd8ff;
            --primary: #1D6BF3;
            --primary-dark: #1557c8;
            --blue-soft: #d7e8ff;
            --blue-softer: #f1f7ff;
            --accent-soft: #e5f0ff;
            --accent-ink: #164eaa;
            --shadow: 0 16px 42px rgba(29, 107, 243, 0.1);
        }

        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            min-height: 100vh;
            background:
                radial-gradient(circle at 18% 8%, rgba(29, 107, 243, 0.12), transparent 28%),
                linear-gradient(180deg, #f7fbff 0%, var(--bg) 100%);
            color: var(--ink);
        }

        button,
        input {
            font: inherit;
        }

        button {
            min-height: 44px;
            border: 0;
            border-radius: 8px;
            padding: 0 16px;
            background: var(--primary);
            color: #ffffff;
            font-weight: 800;
            cursor: pointer;
        }

        button:hover {
            background: var(--primary-dark);
        }

        button.secondary {
            background: var(--blue-soft);
            color: var(--primary-dark);
        }

        button.secondary:hover {
            background: #c8ddff;
        }

        button:disabled {
            opacity: 0.6;
            cursor: wait;
        }

        .app {
            min-height: 100vh;
            display: grid;
            grid-template-columns: 260px 1fr;
        }

        .sidebar {
            position: sticky;
            top: 0;
            height: 100vh;
            padding: 24px;
            background: linear-gradient(180deg, #d7e8ff 0%, #f5faff 76%);
            border-right: 1px solid var(--line);
            display: flex;
            flex-direction: column;
            gap: 22px;
        }

        .brand {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .brand-mark {
            width: 42px;
            height: 42px;
            border-radius: 12px;
            background: #ffffff;
            border: 1px solid var(--line);
            display: grid;
            place-items: center;
            padding: 4px;
        }

        .brand-mark img {
            width: 100%;
            height: 100%;
            display: block;
        }

        .brand h1 {
            margin: 0;
            font-size: 22px;
            letter-spacing: 0;
        }

        .brand span,
        .sidebar-note,
        .step span,
        .muted {
            color: var(--muted);
        }

        .sidebar-note {
            margin: 0;
            line-height: 1.55;
            font-size: 14px;
        }

        .steps {
            display: grid;
            gap: 10px;
        }

        .step {
            display: grid;
            grid-template-columns: 28px 1fr;
            gap: 10px;
            align-items: center;
            padding: 11px;
            border-radius: 8px;
            background: rgba(250, 253, 255, 0.82);
            border: 1px solid var(--line);
        }

        .step-index {
            width: 28px;
            height: 28px;
            border-radius: 999px;
            background: var(--blue-soft);
            color: var(--primary);
            display: grid;
            place-items: center;
            font-size: 13px;
            font-weight: 800;
        }

        .step strong {
            display: block;
            font-size: 14px;
        }

        .main {
            padding: 28px clamp(18px, 4vw, 46px) 42px;
        }

        .topbar {
            display: flex;
            justify-content: space-between;
            gap: 18px;
            align-items: flex-start;
            margin-bottom: 22px;
        }

        .eyebrow {
            display: inline-flex;
            align-items: center;
            min-height: 28px;
            padding: 4px 10px;
            margin-bottom: 10px;
            border-radius: 999px;
            background: var(--blue-soft);
            color: var(--primary-dark);
            font-size: 13px;
            font-weight: 900;
        }

        .topbar h2 {
            margin: 0;
            font-size: clamp(28px, 4vw, 38px);
            letter-spacing: 0;
        }

        .topbar p {
            margin: 8px 0 0;
            max-width: 720px;
            color: var(--muted);
            line-height: 1.55;
        }

        .status-pill {
            min-width: 168px;
            padding: 10px 14px;
            border-radius: 999px;
            background: var(--blue-softer);
            border: 1px solid var(--line);
            color: var(--primary-dark);
            font-weight: 800;
            text-align: center;
        }

        .hero-upload {
            max-width: 760px;
            margin: 0 auto 24px;
        }

        .upload-panel {
            background: var(--surface-strong);
            border: 1px solid var(--line);
            border-radius: 8px;
            box-shadow: var(--shadow);
            overflow: hidden;
        }

        .upload-panel-header {
            padding: 18px 20px 0;
            display: flex;
            justify-content: space-between;
            gap: 12px;
            align-items: center;
        }

        .panel-title {
            margin: 0;
            font-size: 18px;
            letter-spacing: 0;
        }

        .panel-body {
            padding: 18px;
        }

        .upload-zone {
            position: relative;
            display: grid;
            place-items: center;
            min-height: clamp(260px, 42vh, 420px);
            border: 1px dashed #91bdfb;
            border-radius: 8px;
            background: linear-gradient(180deg, #f4f9ff, #d7e8ff);
            overflow: hidden;
        }

        .upload-zone.has-preview .upload-copy {
            display: none;
        }

        .upload-zone.is-loading::after {
            content: "";
            position: absolute;
            inset: 0;
            background: linear-gradient(180deg, rgba(29, 107, 243, 0.12), rgba(21, 87, 200, 0.42));
            z-index: 2;
        }

        .upload-zone input {
            position: absolute;
            inset: 0;
            opacity: 0;
            cursor: pointer;
            z-index: 5;
        }

        .upload-zone.is-loading input {
            pointer-events: none;
        }

        .upload-copy {
            text-align: center;
            padding: 28px;
        }

        .upload-copy strong {
            display: block;
            font-size: 22px;
            margin-bottom: 8px;
        }

        .upload-copy span {
            display: block;
            color: var(--muted);
            font-size: 14px;
            line-height: 1.5;
        }

        #preview {
            width: 100%;
            height: 100%;
            object-fit: cover;
            display: none;
            position: absolute;
            inset: 0;
        }

        .loading-ui {
            position: absolute;
            inset: 0;
            z-index: 3;
            display: none;
            place-items: center;
            padding: 18px;
            color: #ffffff;
            pointer-events: none;
        }

        .upload-zone.is-loading .loading-ui {
            display: grid;
        }

        .scanner-line {
            position: absolute;
            left: 10px;
            right: 10px;
            height: 3px;
            border-radius: 999px;
            background: linear-gradient(90deg, transparent, #ffffff, #8bbafa, transparent);
            box-shadow: 0 0 22px rgba(147, 197, 253, 0.78);
            animation: scanImage 2.2s ease-in-out infinite;
        }

        .loading-card {
            width: min(280px, 90%);
            padding: 18px;
            border: 1px solid rgba(255, 255, 255, 0.32);
            border-radius: 8px;
            background: rgba(16, 46, 85, 0.74);
            backdrop-filter: blur(12px);
            box-shadow: 0 12px 26px rgba(37, 99, 235, 0.18);
            text-align: center;
        }

        .loader-orbit {
            width: 76px;
            height: 76px;
            margin: 0 auto 12px;
            border-radius: 999px;
            display: grid;
            place-items: center;
            position: relative;
        }

        .loader-orbit::before {
            content: "";
            position: absolute;
            inset: 0;
            border-radius: inherit;
            border: 3px solid rgba(255, 255, 255, 0.22);
            border-top-color: #bdd7ff;
            border-right-color: var(--primary);
            animation: spin 1.05s linear infinite;
        }

        .loader-orbit img {
            width: 48px;
            height: 48px;
            border-radius: 8px;
            background: #ffffff;
            padding: 5px;
        }

        .loading-card strong {
            display: block;
            font-size: 17px;
            margin-bottom: 6px;
        }

        .loading-card span {
            display: block;
            color: #dbeafe;
            font-size: 13px;
            line-height: 1.45;
        }

        .loading-dots {
            display: inline-flex;
            gap: 4px;
            margin-top: 12px;
        }

        .loading-dots i {
            width: 7px;
            height: 7px;
            border-radius: 999px;
            background: #ffffff;
            opacity: 0.45;
            animation: dotPulse 1.2s ease-in-out infinite;
        }

        .loading-dots i:nth-child(2) {
            animation-delay: 0.16s;
        }

        .loading-dots i:nth-child(3) {
            animation-delay: 0.32s;
        }

        .actions {
            display: grid;
            grid-template-columns: 1fr auto;
            gap: 10px;
            margin-top: 14px;
        }

        #status {
            min-height: 42px;
            margin-top: 14px;
            padding: 12px;
            border-radius: 8px;
            background: var(--blue-softer);
            color: var(--muted);
            line-height: 1.45;
        }

        .status-pill.is-loading {
            color: var(--primary);
            background: var(--blue-soft);
        }

        .status-pill.is-loading::after {
            content: "";
            display: inline-block;
            width: 7px;
            height: 7px;
            margin-left: 8px;
            border-radius: 999px;
            background: currentColor;
            animation: dotPulse 1s ease-in-out infinite;
        }

        .stats {
            display: grid;
            grid-template-columns: repeat(3, minmax(0, 1fr));
            gap: 12px;
            max-width: 960px;
            margin: 0 auto 18px;
        }

        .stat {
            padding: 16px;
            background: var(--surface-strong);
            border: 1px solid var(--line);
            border-radius: 8px;
        }

        .stat span {
            display: block;
            color: var(--muted);
            font-size: 13px;
            margin-bottom: 8px;
        }

        .stat strong {
            font-size: 28px;
            letter-spacing: 0;
        }

        .results-grid {
            display: grid;
            grid-template-columns: minmax(260px, 0.85fr) minmax(320px, 1.15fr) minmax(260px, 0.9fr);
            gap: 18px;
            align-items: start;
        }

        .panel {
            background: var(--surface-strong);
            border: 1px solid var(--line);
            border-radius: 8px;
            box-shadow: 0 1px 2px rgba(29, 107, 243, 0.08);
        }

        .panel-header {
            padding: 18px 18px 0;
            display: flex;
            justify-content: space-between;
            gap: 12px;
            align-items: center;
        }

        .chips {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
            min-height: 36px;
        }

        .chip {
            display: inline-flex;
            align-items: center;
            min-height: 32px;
            padding: 6px 10px;
            border: 0;
            border-radius: 999px;
            background: var(--blue-soft);
            color: var(--primary);
            font-size: 14px;
            font-weight: 800;
            text-decoration: none;
        }

        button.chip {
            min-height: 32px;
            cursor: pointer;
        }

        button.chip:hover {
            background: var(--primary);
            color: #ffffff;
        }

        .chip.missing {
            background: var(--accent-soft);
            color: var(--accent-ink);
        }

        .hint {
            display: block;
            margin-top: 12px;
            color: var(--muted);
            font-size: 13px;
            line-height: 1.45;
        }

        .recipe-list {
            display: grid;
            gap: 12px;
        }

        .recipe {
            border: 1px solid var(--line);
            border-radius: 8px;
            padding: 16px;
            background: var(--blue-softer);
        }

        .recipe-head {
            display: flex;
            justify-content: space-between;
            gap: 12px;
            align-items: flex-start;
        }

        .recipe h3 {
            margin: 0;
            font-size: 19px;
        }

        .recipe p {
            margin: 8px 0 14px;
            color: var(--muted);
            line-height: 1.45;
        }

        .badge {
            flex: 0 0 auto;
            padding: 5px 9px;
            border-radius: 999px;
            background: var(--blue-soft);
            color: var(--primary);
            font-size: 12px;
            font-weight: 900;
        }

        .recipe ol {
            margin: 14px 0 0;
            padding-left: 22px;
            color: var(--accent-ink);
        }

        .recipe li {
            margin: 5px 0;
            line-height: 1.45;
        }

        .cart-item {
            display: grid;
            grid-template-columns: 1fr auto;
            gap: 12px;
            align-items: center;
            padding: 12px 0;
            border-bottom: 1px solid var(--line);
        }

        .cart-item:last-child {
            border-bottom: 0;
        }

        .cart-item strong {
            display: block;
            margin-bottom: 4px;
        }

        .cart-item span {
            color: var(--muted);
            font-size: 13px;
        }

        .cart-item a {
            min-width: 92px;
            min-height: 36px;
            display: inline-grid;
            place-items: center;
            border-radius: 8px;
            background: var(--primary);
            color: #ffffff;
            font-size: 14px;
            font-weight: 800;
            text-decoration: none;
        }

        .cart-item a:hover {
            background: var(--primary-dark);
        }

        .empty {
            display: block;
            color: var(--muted);
            line-height: 1.55;
        }

        @keyframes scanImage {
            0% {
                top: 10%;
                opacity: 0;
            }
            15% {
                opacity: 1;
            }
            50% {
                top: 88%;
                opacity: 1;
            }
            100% {
                top: 10%;
                opacity: 0;
            }
        }

        @keyframes spin {
            to {
                transform: rotate(360deg);
            }
        }

        @keyframes dotPulse {
            0%,
            100% {
                transform: translateY(0);
                opacity: 0.35;
            }
            50% {
                transform: translateY(-5px);
                opacity: 1;
            }
        }

        @media (max-width: 1180px) {
            .results-grid {
                grid-template-columns: 1fr 1fr;
            }

            .cart-panel {
                grid-column: 1 / -1;
            }
        }

        @media (max-width: 900px) {
            .app,
            .results-grid {
                grid-template-columns: 1fr;
            }

            .sidebar {
                position: static;
                height: auto;
            }
        }

        @media (max-width: 640px) {
            .main {
                padding: 18px;
            }

            .topbar {
                display: grid;
            }

            .status-pill {
                min-width: 0;
            }

            .stats,
            .actions {
                grid-template-columns: 1fr;
            }

            button.secondary {
                width: 100%;
            }
        }
    </style>
</head>

<body>
    <div class="app">
        <aside class="sidebar">
            <div class="brand">
                <div class="brand-mark">
                    <img src="/static/assets/meal-gpt-logo.svg" alt="MealGPT 로고">
                </div>
                <div>
                    <h1>MealGPT</h1>
                    <span>AI meal planner</span>
                </div>
            </div>

            <p class="sidebar-note">냉장고 사진에서 식재료를 인식하고, 만들 수 있는 레시피와 바로 구매할 재료를 정리합니다.</p>

            <div class="steps">
                <div class="step">
                    <div class="step-index">1</div>
                    <div><strong>사진 업로드</strong><span>메인 중앙에서 선택</span></div>
                </div>
                <div class="step">
                    <div class="step-index">2</div>
                    <div><strong>식재료 인식</strong><span>보이는 재료만 추출</span></div>
                </div>
                <div class="step">
                    <div class="step-index">3</div>
                    <div><strong>장바구니 연결</strong><span>클릭하면 쿠팡 구매로 이동</span></div>
                </div>
            </div>
        </aside>

        <main class="main">
            <div class="topbar">
                <div>
                    <div class="eyebrow">MealGPT Kitchen OS</div>
                    <h2>냉장고 사진으로 오늘의 식사를 정리하세요</h2>
                    <p>사진을 올리면 식재료를 인식하고 레시피를 추천합니다. 인식된 재료 칩을 누르면 장바구니에 바로 담기고, 구매 버튼은 쿠팡 검색으로 연결됩니다.</p>
                </div>
                <div id="appStatus" class="status-pill">준비됨</div>
            </div>

            <section class="hero-upload">
                <div class="upload-panel">
                    <div class="upload-panel-header">
                        <h3 class="panel-title">냉장고 사진 업로드</h3>
                        <span class="muted">JPG, PNG 지원</span>
                    </div>
                    <div class="panel-body">
                        <label class="upload-zone">
                            <img id="preview" alt="업로드한 냉장고 사진 미리보기">
                            <input type="file" id="imageFile" accept="image/*">
                            <span class="upload-copy">
                                <strong id="uploadTitle">사진을 중앙에 끌어오거나 클릭하세요</strong>
                                <span id="uploadSubtitle">MealGPT가 실제 이미지에서 식재료를 인식합니다.</span>
                            </span>
                            <span class="loading-ui" aria-hidden="true">
                                <span class="scanner-line"></span>
                                <span class="loading-card">
                                    <span class="loader-orbit">
                                        <img src="/static/assets/meal-gpt-logo.svg" alt="">
                                    </span>
                                    <strong id="loadingTitle">사진을 분석하고 있어요</strong>
                                    <span id="loadingSubtitle">보이는 식재료만 골라내는 중입니다.</span>
                                    <span class="loading-dots"><i></i><i></i><i></i></span>
                                </span>
                            </span>
                        </label>

                        <div class="actions">
                            <button id="analyzeButton" onclick="analyzeImage()">분석 시작</button>
                            <button class="secondary" onclick="clearCart()">장바구니 비우기</button>
                        </div>

                        <div id="status">분석할 사진을 선택하세요.</div>
                    </div>
                </div>
            </section>

            <div class="stats">
                <div class="stat">
                    <span>인식 식재료</span>
                    <strong id="ingredientCount">0</strong>
                </div>
                <div class="stat">
                    <span>추천 레시피</span>
                    <strong id="recipeCount">0</strong>
                </div>
                <div class="stat">
                    <span>장바구니</span>
                    <strong id="cartCount">0</strong>
                </div>
            </div>

            <div class="results-grid">
                <section class="panel">
                    <div class="panel-header">
                        <h3 class="panel-title">인식한 식재료</h3>
                    </div>
                    <div class="panel-body">
                        <div id="ingredients" class="chips">
                            <span class="empty">아직 인식한 재료가 없습니다.</span>
                        </div>
                        <span class="hint">재료를 클릭하면 자동으로 장바구니에 담깁니다.</span>
                    </div>
                </section>

                <section class="panel">
                    <div class="panel-header">
                        <h3 class="panel-title">추천 레시피</h3>
                    </div>
                    <div class="panel-body">
                        <div id="recipes" class="recipe-list">
                            <span class="empty">분석 후 레시피가 표시됩니다.</span>
                        </div>
                    </div>
                </section>

                <section class="panel cart-panel">
                    <div class="panel-header">
                        <h3 class="panel-title">장바구니</h3>
                    </div>
                    <div class="panel-body">
                        <div id="cart">
                            <span class="empty">담긴 재료가 없습니다.</span>
                        </div>
                    </div>
                </section>
            </div>
        </main>
    </div>

    <script>
    const fileInput = document.getElementById("imageFile");
    const preview = document.getElementById("preview");
    const statusElement = document.getElementById("status");
    const appStatus = document.getElementById("appStatus");
    const analyzeButton = document.getElementById("analyzeButton");
    const uploadTitle = document.getElementById("uploadTitle");
    const uploadSubtitle = document.getElementById("uploadSubtitle");
    const uploadZone = document.querySelector(".upload-zone");
    const loadingTitle = document.getElementById("loadingTitle");
    const loadingSubtitle = document.getElementById("loadingSubtitle");
    const ingredientCount = document.getElementById("ingredientCount");
    const recipeCount = document.getElementById("recipeCount");
    const cartCount = document.getElementById("cartCount");
    const loadingSteps = [
        ["사진을 스캔하고 있어요", "냉장고 사진에서 실제 식재료 후보를 찾고 있습니다."],
        ["재료 이름을 정리하고 있어요", "도구와 포장지는 제외하고 한국어 재료명으로 바꾸는 중입니다."],
        ["레시피를 고르고 있어요", "보유한 재료로 만들기 좋은 메뉴를 우선 추천합니다."],
        ["장바구니를 준비하고 있어요", "부족한 재료는 쿠팡 구매 링크로 연결합니다."]
    ];
    let loadingTimer = null;

    fileInput.addEventListener("change", () => {
        const file = fileInput.files[0];
        if (!file) {
            uploadZone.classList.remove("has-preview");
            preview.style.display = "none";
            preview.removeAttribute("src");
            uploadTitle.innerText = "사진을 중앙에 끌어오거나 클릭하세요";
            uploadSubtitle.innerText = "MealGPT가 실제 이미지에서 식재료를 인식합니다.";
            return;
        }

        uploadZone.classList.add("has-preview");
        preview.src = URL.createObjectURL(file);
        preview.style.display = "block";
        uploadTitle.innerText = "사진 선택 완료";
        uploadSubtitle.innerText = file.name;
        setStatus("사진이 준비됐습니다. 분석 시작을 누르세요.", "대기");
    });

    function setStatus(message, shortStatus = "진행 중") {
        statusElement.innerText = message || "";
        appStatus.innerText = shortStatus;
    }

    function setLoading(active) {
        if (loadingTimer) {
            clearInterval(loadingTimer);
            loadingTimer = null;
        }

        uploadZone.classList.toggle("is-loading", active);
        appStatus.classList.toggle("is-loading", active);

        if (!active) {
            return;
        }

        let stepIndex = 0;
        const applyStep = () => {
            const [title, subtitle] = loadingSteps[stepIndex % loadingSteps.length];
            loadingTitle.innerText = title;
            loadingSubtitle.innerText = subtitle;
            setStatus(subtitle, "분석 중");
            stepIndex += 1;
        };

        applyStep();
        loadingTimer = setInterval(applyStep, 2600);
    }

    function setCounters(ingredients = [], recipes = [], cartItems = []) {
        ingredientCount.innerText = Array.isArray(ingredients) ? ingredients.length : 0;
        recipeCount.innerText = Array.isArray(recipes) ? recipes.length : 0;
        cartCount.innerText = Array.isArray(cartItems) ? cartItems.length : 0;
    }

    function renderIngredients(items) {
        const element = document.getElementById("ingredients");
        element.innerHTML = "";

        if (!Array.isArray(items) || items.length === 0) {
            element.innerHTML = '<span class="empty">아직 인식한 재료가 없습니다.</span>';
            return;
        }

        items.forEach(item => {
            const button = document.createElement("button");
            button.type = "button";
            button.className = "chip";
            button.innerText = item;
            button.title = `${item} 장바구니에 담기`;
            button.addEventListener("click", () => addIngredientToCart(item));
            element.appendChild(button);
        });
    }

    function renderRecipeChips(container, items, missingItems) {
        (items || []).forEach(item => {
            const chip = document.createElement("button");
            chip.type = "button";
            chip.className = (missingItems || []).includes(item) ? "chip missing" : "chip";
            chip.innerText = item;
            chip.title = `${item} 장바구니에 담기`;
            chip.addEventListener("click", () => addIngredientToCart(item));
            container.appendChild(chip);
        });
    }

    function renderRecipes(recipes) {
        const recipesElement = document.getElementById("recipes");
        recipesElement.innerHTML = "";

        if (!Array.isArray(recipes) || recipes.length === 0) {
            recipesElement.innerHTML = '<span class="empty">추천할 레시피가 없습니다.</span>';
            return;
        }

        recipes.forEach((recipe, index) => {
            const article = document.createElement("article");
            article.className = "recipe";

            const head = document.createElement("div");
            head.className = "recipe-head";

            const title = document.createElement("h3");
            title.innerText = recipe.name;
            head.appendChild(title);

            const badge = document.createElement("span");
            badge.className = "badge";
            badge.innerText = `${index + 1}순위`;
            head.appendChild(badge);
            article.appendChild(head);

            const description = document.createElement("p");
            description.innerText = recipe.description || "냉장고 재료를 활용한 추천 레시피입니다.";
            article.appendChild(description);

            const ingredients = document.createElement("div");
            ingredients.className = "chips";
            renderRecipeChips(ingredients, recipe.ingredients, recipe.missing_ingredients);
            article.appendChild(ingredients);

            if (recipe.steps && recipe.steps.length > 0) {
                const steps = document.createElement("ol");
                recipe.steps.forEach(step => {
                    const li = document.createElement("li");
                    li.innerText = step;
                    steps.appendChild(li);
                });
                article.appendChild(steps);
            }

            recipesElement.appendChild(article);
        });
    }

    function renderCart(cartItems) {
        const cartElement = document.getElementById("cart");
        cartElement.innerHTML = "";

        if (!Array.isArray(cartItems) || cartItems.length === 0) {
            cartElement.innerHTML = '<span class="empty">담긴 재료가 없습니다.</span>';
            return;
        }

        cartItems.forEach(item => {
            const row = document.createElement("div");
            row.className = "cart-item";

            const text = document.createElement("div");
            const quantity = Number(item.quantity || 1);
            const name = document.createElement("strong");
            name.innerText = item.name;
            const count = document.createElement("span");
            count.innerText = `${quantity}개 담김`;
            text.appendChild(name);
            text.appendChild(count);
            row.appendChild(text);

            const link = document.createElement("a");
            link.href = item.buy_url;
            link.target = "_blank";
            link.rel = "noreferrer";
            link.innerText = "쿠팡 구매";
            row.appendChild(link);

            cartElement.appendChild(row);
        });
    }

    async function addIngredientToCart(name) {
        try {
            const response = await fetch("/cart/items", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ name, quantity: 1 })
            });
            const data = await response.json();
            renderCart(data.cart);
            cartCount.innerText = Array.isArray(data.cart) ? data.cart.length : 0;
            setStatus(`${name}을 장바구니에 담았습니다. 쿠팡 구매 버튼으로 바로 이동할 수 있어요.`, "담김");
        } catch (error) {
            setStatus("장바구니에 담지 못했습니다.", "오류");
        }
    }

    async function analyzeImage() {
        const file = fileInput.files[0];

        if (!file) {
            alert("이미지를 선택하세요.");
            return;
        }

        const formData = new FormData();
        formData.append("file", file);

        analyzeButton.disabled = true;
        setLoading(true);

        try {
            const response = await fetch("/analyze", {
                method: "POST",
                body: formData
            });

            const data = await response.json();

            if (!response.ok || data.error) {
                setStatus(data.error || "분석에 실패했습니다.", "오류");
                return;
            }

            renderIngredients(data.ingredients);
            renderRecipes(data.recipes);
            renderCart(data.cart);
            setCounters(data.ingredients, data.recipes, data.cart);
            setStatus("분석이 완료됐습니다. 식재료를 클릭하면 장바구니에 추가됩니다.", "완료");
        } catch (error) {
            setStatus("서버와 통신하지 못했습니다.", "오류");
        } finally {
            analyzeButton.disabled = false;
            setLoading(false);
        }
    }

    async function clearCart() {
        await fetch("/cart", { method: "DELETE" });
        renderCart([]);
        cartCount.innerText = 0;
        setStatus("장바구니를 비웠습니다.", "대기");
    }

    async function loadCart() {
        const response = await fetch("/cart");
        const data = await response.json();
        renderCart(data.cart);
        cartCount.innerText = Array.isArray(data.cart) ? data.cart.length : 0;
    }

    loadCart();
    </script>
</body>
</html>
"""
