package com.example.portfolio;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@SpringBootApplication
public class PortfolioApplication {

    public static void main(String[] args) {
        loadJsonEnvironmentFile();
        SpringApplication.run(PortfolioApplication.class, args);
    }

    private static void loadJsonEnvironmentFile() {
        Path envFile = Path.of(".env.json");
        if (!Files.exists(envFile)) {
            return;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> values = mapper.readValue(envFile.toFile(), new TypeReference<>() {
            });

            for (Map.Entry<String, String> entry : values.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (key == null || key.isBlank() || value == null) {
                    continue;
                }
                if (System.getProperty(key) == null && System.getenv(key) == null) {
                    System.setProperty(key, value);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read .env.json", e);
        }
    }
}
