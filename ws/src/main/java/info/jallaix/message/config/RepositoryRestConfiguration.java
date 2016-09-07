package info.jallaix.message.config;

import info.jallaix.message.service.LanguageValidator;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;

/**
 * Repository REST configuration
 */
@Configuration
public class RepositoryRestConfiguration extends RepositoryRestConfigurerAdapter {

    @Override
    public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener validatingRepositoryEventListener) {

        // Spring Data REST looks for this validator when executing a POST request
        validatingRepositoryEventListener.addValidator("beforeCreate", new LanguageValidator());
        // Spring Data REST looks for this validator when executing a PUT request
        validatingRepositoryEventListener.addValidator("beforeSave", new LanguageValidator());
    }
}
