package info.jallaix.message;

import info.jallaix.message.service.LanguageValidator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <p>
 *     Spring boot application main class that runs the Message service.
 * <p>
 *     It defines a Spring validator triggered when trying to create a {@link info.jallaix.message.dto.Language} entity.
 *     It also defines a Dozer mapper for beans conversion.
 *
 * @see LanguageValidator
 */
@SpringBootApplication
public class Application {

    /**
     * Run the application
     * @param args Application arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
