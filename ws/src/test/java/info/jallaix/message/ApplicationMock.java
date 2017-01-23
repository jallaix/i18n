package info.jallaix.message;

import info.jallaix.message.config.BeanMappingConfiguration;
import info.jallaix.message.config.RepositoryRestConfiguration;
import info.jallaix.message.dao.RestElasticsearchRepositoryFactoryBean;
import info.jallaix.message.service.*;
import info.jallaix.spring.data.es.test.SpringDataEsTestConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * <p>
 *     This is a mock for the Spring Boot application class for running tests that send HTTP requests to the Message REST service.
 * <p>
 *     It enables the automatic discovery of Elasticsearch repositories and loads Elasticsearch test configuration.
 *     Spring MVC controllers are also loaded to handle HTTP requests, as well as other required configurations.
 */
@Configuration
@EnableAutoConfiguration
@Import({DomainController.class,
        LanguageController.class,
        SpringDataEsTestConfiguration.class,
        GenericExceptionHandler.class,
        DomainResourceAssembler.class,
        LanguageResourceAssembler.class,
        RepositoryRestConfiguration.class,
        BeanMappingConfiguration.class})
@EnableElasticsearchRepositories(repositoryFactoryBeanClass = RestElasticsearchRepositoryFactoryBean.class)
public class ApplicationMock {}
