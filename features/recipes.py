import os
from typing import Any

import requests

from config import SPOONACULAR_API_KEY
from features.pantry import is_available_item, is_pantry_item, to_korean_item_name
from features.text_utils import clean_text_list


SPOONACULAR_FIND_BY_INGREDIENTS_URL = (
    "https://api.spoonacular.com/recipes/findByIngredients"
)


def _validate_ingredients(ingredients: list[str]) -> list[str]:
    if not isinstance(ingredients, list):
        raise TypeError("ingredients must be a list")

    cleaned = clean_text_list([str(item).strip() for item in ingredients])
    if not cleaned:
        raise ValueError("ingredients must contain at least one item")

    return cleaned


def _as_number(value: Any, default: float = 0.0) -> float:
    try:
        return float(value)
    except (TypeError, ValueError):
        return default


def _normalize_spoonacular_recipe(recipe: dict, available: list[str]) -> dict:
    if not isinstance(recipe, dict):
        raise TypeError("recipe must be a dict")

    used_ingredients = [
        item.get("name", item)
        for item in recipe.get("usedIngredients", [])
        if isinstance(item, (dict, str))
    ]
    missed_ingredients = [
        item.get("name", item)
        for item in recipe.get("missedIngredients", [])
        if isinstance(item, (dict, str))
    ]
    all_ingredients = clean_text_list(used_ingredients + missed_ingredients)

    missing = []
    for item in missed_ingredients:
        item_name = to_korean_item_name(item)
        if not item_name or is_pantry_item(item_name):
            continue
        if not is_available_item(item_name, available) and item_name not in missing:
            missing.append(item_name)

    score = calculate_score(recipe)

    return {
        "id": recipe.get("id"),
        "name": str(recipe.get("title", "Recommended Recipe")).strip(),
        "description": (
            f"Matched {int(_as_number(recipe.get('usedIngredientCount')))} "
            f"ingredient(s), missing {int(_as_number(recipe.get('missedIngredientCount')))}."
        ),
        "ingredients": clean_text_list([to_korean_item_name(item) for item in all_ingredients]),
        "missing_ingredients": missing,
        "steps": [],
        "image": recipe.get("image", ""),
        "source_url": recipe.get("sourceUrl", ""),
        "score": score,
    }


def search_recipes(ingredients: list[str]) -> list[dict]:
    """Search Spoonacular recipes that match the given fridge ingredients."""
    cleaned = _validate_ingredients(ingredients)
    api_key = SPOONACULAR_API_KEY or os.getenv("SPOONACULAR_API_KEY", "")
    if not api_key:
        raise ValueError("SPOONACULAR_API_KEY is required")

    try:
        response = requests.get(
            SPOONACULAR_FIND_BY_INGREDIENTS_URL,
            params={
                "apiKey": api_key,
                "ingredients": ",".join(cleaned),
                "number": 20,
                "ranking": 1,
                "ignorePantry": True,
            },
            timeout=15,
        )
        response.raise_for_status()
        data = response.json()
    except requests.RequestException as exc:
        raise ValueError(f"failed to search recipes: {exc}") from exc
    except ValueError as exc:
        raise ValueError("Spoonacular returned invalid JSON") from exc

    if not isinstance(data, list):
        raise ValueError("Spoonacular recipe search must return a list")

    return data


def calculate_score(recipe: dict) -> float:
    """Score by popularity, difficulty, and ingredient match rate."""
    if not isinstance(recipe, dict):
        raise TypeError("recipe must be a dict")

    used_count = _as_number(recipe.get("usedIngredientCount"))
    missed_count = _as_number(recipe.get("missedIngredientCount"))
    total_count = used_count + missed_count
    if total_count <= 0:
        raise ValueError("recipe must include ingredient match counts")

    match_rate = used_count / total_count

    likes = _as_number(recipe.get("likes"))
    popularity_score = min(likes / 100.0, 1.0)

    ready_minutes = _as_number(recipe.get("readyInMinutes"), 30.0)
    ingredient_count = _as_number(
        recipe.get("extendedIngredientCount"),
        total_count,
    )
    time_difficulty = min(max(ready_minutes, 0.0) / 90.0, 1.0)
    ingredient_difficulty = min(max(ingredient_count, 0.0) / 15.0, 1.0)
    difficulty_score = 1.0 - ((time_difficulty + ingredient_difficulty) / 2.0)

    return round(
        (match_rate * 0.55 + popularity_score * 0.25 + difficulty_score * 0.20)
        * 100,
        2,
    )


def rank_and_filter(recipes: list[dict], top_n: int = 5) -> list[dict]:
    if not isinstance(recipes, list):
        raise TypeError("recipes must be a list")
    if not isinstance(top_n, int):
        raise TypeError("top_n must be an int")
    if top_n <= 0:
        raise ValueError("top_n must be greater than 0")

    scored = []
    for recipe in recipes:
        if not isinstance(recipe, dict):
            raise TypeError("each recipe must be a dict")
        scored.append({**recipe, "score": calculate_score(recipe)})

    scored.sort(key=lambda item: item["score"], reverse=True)
    return scored[:top_n]


def recommend_recipes(ingredients: list[str], top_n: int = 5) -> list[dict]:
    if not ingredients:
        return []

    recipes = search_recipes(ingredients)
    ranked = rank_and_filter(recipes, top_n=top_n)
    return [_normalize_spoonacular_recipe(recipe, ingredients) for recipe in ranked]
