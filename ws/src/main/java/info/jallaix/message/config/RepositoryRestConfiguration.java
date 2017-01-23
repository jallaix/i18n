package info.jallaix.message.config;

import info.jallaix.message.dao.DomainDao;
import info.jallaix.message.dao.MessageDao;
import info.jallaix.message.dto.Domain;
import info.jallaix.message.service.validator.DomainValidatorOnCreateOrUpdate;
import info.jallaix.message.service.validator.LanguageValidatorOnCreateOrUpdate;
import info.jallaix.message.service.validator.LanguageValidatorOnDelete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;

/**
 * Repository REST configuration
 */
@Configuration
public class RepositoryRestConfiguration extends RepositoryRestConfigurerAdapter {

    @Autowired
    private DomainDao domainDao;
    @Autowired
    private MessageDao messageDao;

    /**
     * Configure validators for POST, PUT and DELETE requests
     *
     * @param validatingRepositoryEventListener Event listener to register validators
     */
    @Override
    public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener validatingRepositoryEventListener) {

        validatingRepositoryEventListener.addValidator("beforeCreate", new LanguageValidatorOnCreateOrUpdate());
        validatingRepositoryEventListener.addValidator("beforeSave", new LanguageValidatorOnCreateOrUpdate());
        validatingRepositoryEventListener.addValidator("beforeCreate", new DomainValidatorOnCreateOrUpdate());
        validatingRepositoryEventListener.addValidator("beforeSave", new DomainValidatorOnCreateOrUpdate());
        validatingRepositoryEventListener.addValidator("beforeDelete", new LanguageValidatorOnDelete(domainDao));
    }

    /**
     * Replace message code by language
     * @return
     */
    @Bean
    public ResourceProcessor<Resource<Domain>> languageProcessor() {

        return new ResourceProcessor<Resource<Domain>>() {

            @Override
            public Resource<Domain> process(Resource<Domain> resource) {

                //messageDao.findByCodeAndLanguageId(resource.getContent().getCode(), "");
                //TODO Find required message
                return resource;
            }
        };
    }
}
