package info.jallaix.message;

import info.jallaix.message.service.LanguageValidator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.Validator;

/**
 * Spring boot application main class
 */
@SpringBootApplication
public class Application {

    @Bean
    public Validator beforeCreateLanguageValidator() {
        return new LanguageValidator();
    }

    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);
    }
}
