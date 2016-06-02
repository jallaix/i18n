package info.jallaix.message;

import info.jallaix.message.dao.RestElasticsearchRepositoryFactoryBean;
import info.jallaix.message.dto.Language;
import info.jallaix.message.service.GenericExceptionHandler;
import info.jallaix.message.service.LanguageController;
import info.jallaix.message.service.LanguageValidator;
import info.jallaix.spring.data.es.test.SpringDataEsTestConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

/**
 * Created by Julien on 02/06/2016.
 */
@Configuration
@EnableAutoConfiguration
@Import({LanguageController.class,
        SpringDataEsTestConfiguration.class,
        GenericExceptionHandler.class})
@EnableElasticsearchRepositories(repositoryFactoryBeanClass = RestElasticsearchRepositoryFactoryBean.class)
public class ApplicationMock extends RepositoryRestConfigurerAdapter {

    @Bean
    public Validator beforeCreateLanguageValidator() {
        return new LanguageValidator();
    }

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
    }

    @Override
    public void configureExceptionHandlerExceptionResolver(ExceptionHandlerExceptionResolver exceptionResolver) {
    }

    @Override
    public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener validatingListener) {
        //validatingListener.addValidator("beforeCreate", new LanguageValidator());
    }
}
