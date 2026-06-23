package kr.jgg.mealgpt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "meal-gpt")
public class MealGptProperties {
    private final Ollama ollama = new Ollama();
    private final Spoonacular spoonacular = new Spoonacular();
    private final OpenClaw openclaw = new OpenClaw();
    private String uploadDir = "uploads";

    public Ollama getOllama() {
        return ollama;
    }

    public Spoonacular getSpoonacular() {
        return spoonacular;
    }

    public OpenClaw getOpenclaw() {
        return openclaw;
    }

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public static class Ollama {
        private String baseUrl = "http://127.0.0.1:11434";
        private String model = "llama3.2-vision:latest";

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }
    }

    public static class Spoonacular {
        private String apiKey = "";

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }

    public static class OpenClaw {
        private String sharedSecret = "";

        public String getSharedSecret() {
            return sharedSecret;
        }

        public void setSharedSecret(String sharedSecret) {
            this.sharedSecret = sharedSecret;
        }
    }
}
