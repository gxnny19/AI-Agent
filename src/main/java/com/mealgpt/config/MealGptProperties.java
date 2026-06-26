package com.mealgpt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mealgpt")
public class MealGptProperties {
    private String uploadDir = "uploads";
    private String ollamaModel = "llama3.2-vision:latest";
    private String spoonacularApiKey = "";

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String getOllamaModel() {
        return ollamaModel;
    }

    public void setOllamaModel(String ollamaModel) {
        this.ollamaModel = ollamaModel;
    }

    public String getSpoonacularApiKey() {
        return spoonacularApiKey;
    }

    public void setSpoonacularApiKey(String spoonacularApiKey) {
        this.spoonacularApiKey = spoonacularApiKey;
    }
}
