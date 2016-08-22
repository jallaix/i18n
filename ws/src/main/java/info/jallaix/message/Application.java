package info.jallaix.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.jallaix.message.service.LanguageValidator;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import java.util.Arrays;
import java.util.List;

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
public class Application extends RepositoryRestConfigurerAdapter {

    @Override
    public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener validatingRepositoryEventListener) {

        // Spring Data REST looks for this validator when executing a POST request on the "/languages" resource so as to validate the argument
        validatingRepositoryEventListener.addValidator("beforeCreate", new LanguageValidator());
        // Spring Data REST looks for this validator when executing a PUT request on the "/languages" resource so as to validate the argument
        validatingRepositoryEventListener.addValidator("beforeSave", new LanguageValidator());
    }

    /**
     * Default dozer mapper
     * @return the default dozer mapper
     */
    @Bean
    public Mapper beanMapper() {
        return new DozerBeanMapper();
    }

    /**
     * Run the application
     * @param args Application arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
