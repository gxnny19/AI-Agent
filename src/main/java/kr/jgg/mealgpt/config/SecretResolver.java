package kr.jgg.mealgpt.config;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class SecretResolver {
    public String resolve(String key, String configuredValue) {
        if (!isBlank(configuredValue)) {
            return configuredValue.trim();
        }

        String systemProperty = System.getProperty(key);
        if (!isBlank(systemProperty)) {
            return systemProperty.trim();
        }

        String environmentValue = System.getenv(key);
        if (!isBlank(environmentValue)) {
            return environmentValue.trim();
        }

        return readDotEnv(key);
    }

    public boolean has(String key, String configuredValue) {
        return !isBlank(resolve(key, configuredValue));
    }

    private String readDotEnv(String key) {
        Path dotenv = Paths.get(".env").toAbsolutePath().normalize();
        if (!Files.exists(dotenv)) {
            return "";
        }

        try {
            List<String> lines = Files.readAllLines(dotenv, StandardCharsets.UTF_8);
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int separator = trimmed.indexOf('=');
                if (separator <= 0) {
                    continue;
                }
                String name = trimmed.substring(0, separator).trim();
                if (!key.equals(name)) {
                    continue;
                }
                return unquote(trimmed.substring(separator + 1).trim());
            }
        } catch (IOException ignored) {
            return "";
        }

        return "";
    }

    private String unquote(String value) {
        if ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
