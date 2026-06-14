import unittest
from unittest.mock import Mock, patch

from features.recipes import calculate_score, rank_and_filter, search_recipes


class RecipeRecommendationTests(unittest.TestCase):
    def setUp(self):
        self.recipes = [
            {
                "id": 1,
                "title": "Tomato Omelet",
                "usedIngredientCount": 3,
                "missedIngredientCount": 0,
                "likes": 80,
                "readyInMinutes": 15,
            },
            {
                "id": 2,
                "title": "Vegetable Soup",
                "usedIngredientCount": 2,
                "missedIngredientCount": 2,
                "likes": 200,
                "readyInMinutes": 60,
            },
            {
                "id": 3,
                "title": "Egg Toast",
                "usedIngredientCount": 2,
                "missedIngredientCount": 1,
                "likes": 10,
                "readyInMinutes": 10,
            },
        ]

    def test_calculate_score_prefers_high_match_simple_recipe(self):
        top_score = calculate_score(self.recipes[0])
        lower_score = calculate_score(self.recipes[1])

        self.assertGreater(top_score, lower_score)
        self.assertIsInstance(top_score, float)

    def test_calculate_score_rejects_bad_recipe(self):
        with self.assertRaises(TypeError):
            calculate_score("not a recipe")

        with self.assertRaises(ValueError):
            calculate_score({"title": "No counts"})

    def test_rank_and_filter_orders_by_score(self):
        ranked = rank_and_filter(self.recipes, top_n=2)

        self.assertEqual(len(ranked), 2)
        self.assertEqual(ranked[0]["id"], 1)
        self.assertGreaterEqual(ranked[0]["score"], ranked[1]["score"])

    def test_rank_and_filter_validates_arguments(self):
        with self.assertRaises(TypeError):
            rank_and_filter("bad")

        with self.assertRaises(ValueError):
            rank_and_filter([], top_n=0)

    @patch("features.recipes.SPOONACULAR_API_KEY", "test-key")
    @patch("features.recipes.requests.get")
    def test_search_recipes_calls_spoonacular(self, mock_get):
        response = Mock()
        response.json.return_value = self.recipes
        response.raise_for_status.return_value = None
        mock_get.return_value = response

        results = search_recipes(["egg", "tomato"])

        self.assertEqual(results, self.recipes)
        mock_get.assert_called_once()
        params = mock_get.call_args.kwargs["params"]
        self.assertEqual(params["ingredients"], "egg,tomato")
        self.assertEqual(params["apiKey"], "test-key")

    def test_search_recipes_validates_ingredients(self):
        with self.assertRaises(TypeError):
            search_recipes("egg")

        with self.assertRaises(ValueError):
            search_recipes([])


if __name__ == "__main__":
    unittest.main()
