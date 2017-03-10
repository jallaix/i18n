package info.jallaix.message.config;

import info.jallaix.message.dao.DomainDao;
import info.jallaix.message.dao.EntityMessageDao;
import info.jallaix.message.bean.Domain;
import info.jallaix.message.service.validator.DomainValidatorOnCreate;
import info.jallaix.message.service.validator.DomainValidatorOnUpdate;
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
    private EntityMessageDao entityMessageDao;

    @Autowired
    private DomainValidatorOnCreate domainValidatorOnCreate;
    @Autowired
    private DomainValidatorOnUpdate domainValidatorOnUpdate;

    /**
     * Configure validators for POST, PUT and DELETE requests
     *
     * @param validatingRepositoryEventListener Event listener to register validators
     */
    @Override
    public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener validatingRepositoryEventListener) {

        validatingRepositoryEventListener.addValidator("beforeCreate", domainValidatorOnCreate);
        validatingRepositoryEventListener.addValidator("beforeSave", domainValidatorOnUpdate);
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

                //entityMessageDao.findByCodeAndLanguageId(resource.getContent().getCode(), "");
                //TODO Find required message
                return resource;
            }
        };
    }
    @Bean
    public String domain() { return "message"; }
}
