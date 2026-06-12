from collections import Counter
from urllib.parse import quote, quote_plus

from features.pantry import is_pantry_item, to_korean_item_name


CART: dict[str, dict[str, str | int]] = {}


def coupang_search_url(name: str) -> str:
    return f"https://www.coupang.com/np/search?q={quote_plus(name)}"


def buy_route_url(name: str) -> str:
    return f"/buy/{quote(name)}"


def add_item_to_cart(name: str, quantity: int = 1) -> list[dict]:
    item_name = to_korean_item_name(name)
    if not item_name or is_pantry_item(item_name):
        return get_cart_items()

    CART[item_name] = {
        "name": item_name,
        "quantity": int(CART.get(item_name, {}).get("quantity", 0)) + max(1, int(quantity)),
        "buy_url": buy_route_url(item_name),
        "coupang_url": coupang_search_url(item_name),
    }

    return get_cart_items()


def add_missing_items_to_cart(recipes: list[dict]) -> list[dict]:
    missing_counts = Counter()

    for recipe in recipes:
        missing_counts.update(
            item
            for item in (to_korean_item_name(item) for item in recipe.get("missing_ingredients", []))
            if item and not is_pantry_item(item)
        )

    for name, quantity in missing_counts.items():
        add_item_to_cart(name, quantity)

    return get_cart_items()


def get_cart_items() -> list[dict]:
    return list(CART.values())


def clear_cart_items() -> list[dict]:
    CART.clear()
    return []
