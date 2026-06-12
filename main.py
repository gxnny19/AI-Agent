from fastapi import FastAPI, File, UploadFile
from fastapi.responses import HTMLResponse, RedirectResponse
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel

from features.cart import (
    add_item_to_cart,
    add_missing_items_to_cart,
    clear_cart_items,
    coupang_search_url,
    get_cart_items,
)
from features.ingredients import analyze_ingredients_from_image
from features.recipes import recommend_recipes
from features.uploads import save_upload_file
from pages.fridge_page import HTML_PAGE


class CartItemRequest(BaseModel):
    name: str
    quantity: int = 1


app = FastAPI()
app.mount("/static", StaticFiles(directory="static"), name="static")


@app.get("/")
def root():
    return {"message": "MealGPT Running"}


@app.get("/ai-api")
def ai_api():
    return {
        "message": "사진을 분석하려면 multipart/form-data로 file을 담아 POST /ai-api 또는 POST /analyze를 호출하세요.",
        "example": {
            "method": "POST",
            "path": "/ai-api",
            "field": "file",
        },
    }


@app.post("/ai-api")
async def analyze_ingredients_api(file: UploadFile = File(...)):
    try:
        analysis = analyze_ingredients_from_image(file)

        return {
            "ingredients": analysis["ingredients"],
            "raw": analysis["raw"],
        }

    except Exception as e:
        print("ERROR:", e)
        return {"ingredients": [], "error": str(e)}


@app.get("/ai-test", response_class=HTMLResponse)
def test_page():
    return HTML_PAGE


@app.post("/upload")
async def upload_image(file: UploadFile = File(...)):
    file_path = save_upload_file(file)

    return {
        "filename": file.filename,
        "saved_to": file_path,
    }


@app.post("/analyze")
async def analyze_image(file: UploadFile = File(...)):
    try:
        analysis = analyze_ingredients_from_image(file)
        ingredients = analysis["ingredients"]
        recipes = recommend_recipes(ingredients)
        cart = add_missing_items_to_cart(recipes)

        return {
            "ingredients": ingredients,
            "recipes": recipes,
            "cart": cart,
            "raw": analysis["raw"],
        }

    except Exception as e:
        print("ERROR:", e)
        return {"ingredients": [], "error": str(e)}


@app.get("/cart")
def get_cart():
    return {"cart": get_cart_items()}


@app.post("/cart/items")
def add_cart_item(item: CartItemRequest):
    return {"cart": add_item_to_cart(item.name, item.quantity)}


@app.delete("/cart")
def clear_cart():
    return {"cart": clear_cart_items()}


@app.api_route("/buy/{item_name}", methods=["GET", "HEAD"])
def buy_item(item_name: str):
    return RedirectResponse(coupang_search_url(item_name))
