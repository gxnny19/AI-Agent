package kr.jgg.mealgpt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MealGptApplication {
    public static void main(String[] args) {
        SpringApplication.run(MealGptApplication.class, args);
    }
}
