package info.jallaix.message.config;

import info.jallaix.message.bean.Domain;
import info.jallaix.message.dao.DomainDao;
import info.jallaix.message.dao.RestElasticsearchRepositoryFactoryBean;
import info.jallaix.message.service.DomainController;
import info.jallaix.message.service.GenericExceptionHandler;
import info.jallaix.spring.data.es.test.SpringDataEsTestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.Resources;

/**
 * Configuration for testing the Domain repository with REST.
 */
@Configuration
@EnableAutoConfiguration
@Import({
        SpringDataEsTestConfiguration.class,
        TestProjectConfiguration.class,
        TestRepositoryConfiguration.class,
        DomainController.class,
        GenericExceptionHandler.class,
        WebMvcConfiguration.class})
@EnableElasticsearchRepositories(basePackageClasses = DomainDao.class, repositoryFactoryBeanClass = RestElasticsearchRepositoryFactoryBean.class)
public class TestDomainRestConfiguration extends RepositoryRestConfiguration {

    @Bean
    ResourceProcessor<Resources<Domain>> getResources() {
        return new ResourceProcessor<Resources<Domain>>() {

            @Autowired
            private EntityLinks entityLinks;

            @Override
            public Resources<Domain> process(Resources<Domain> domains) {
                domains.add(entityLinks.linkFor(Domain.class).slash("/search").withRel("self"));
                return domains;
            }
        };
    }
}
