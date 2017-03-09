package info.jallaix.message;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring boot application main class that runs the I18N micro-service.
 */
@SpringBootApplication
public class Application {

    /**
     * Run the application
     *
     * @param args Application arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
