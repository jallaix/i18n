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
 * <p>
 *     This is a mock for the Spring Boot application class when running tests that send HTTP requests to the Message service.
 * <p>
 *     It enables the automatic discovery of Elasticsearch repositories and loads Elasticsearch test configuration.
 *     Spring MVC controllers are also loaded to handle HTTP requests.
 */
@Configuration
@EnableAutoConfiguration
@Import({LanguageController.class,
        SpringDataEsTestConfiguration.class,
        GenericExceptionHandler.class})
@EnableElasticsearchRepositories(repositoryFactoryBeanClass = RestElasticsearchRepositoryFactoryBean.class)
public class ApplicationMock extends Application {
}
