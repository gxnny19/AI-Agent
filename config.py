import os


UPLOAD_DIR = "uploads"
MODEL_NAME = os.getenv("OLLAMA_MODEL", "llama3.2-vision:latest")
SPOONACULAR_API_KEY = os.getenv("SPOONACULAR_API_KEY", "")

os.makedirs(UPLOAD_DIR, exist_ok=True)
