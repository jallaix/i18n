package info.jallaix.message.config;

import info.jallaix.message.dao.DomainDao;
import info.jallaix.message.service.validator.LanguageValidatorOnCreateOrUpdate;
import info.jallaix.message.service.validator.LanguageValidatorOnDelete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;

/**
 * Repository REST configuration
 */
@Configuration
public class RepositoryRestConfiguration extends RepositoryRestConfigurerAdapter {

    @Autowired
    private DomainDao domainDao;

    /**
     * Configure validators for POST, PUT and DELETE requests
     *
     * @param validatingRepositoryEventListener Event listener to register validators
     */
    @Override
    public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener validatingRepositoryEventListener) {

        validatingRepositoryEventListener.addValidator("beforeCreate", new LanguageValidatorOnCreateOrUpdate());
        validatingRepositoryEventListener.addValidator("beforeSave", new LanguageValidatorOnCreateOrUpdate());
        validatingRepositoryEventListener.addValidator("beforeDelete", new LanguageValidatorOnDelete(domainDao));
    }
}
