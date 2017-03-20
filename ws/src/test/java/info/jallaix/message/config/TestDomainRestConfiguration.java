package info.jallaix.message.config;

import info.jallaix.message.bean.Domain;
import info.jallaix.message.dao.DomainDao;
import info.jallaix.message.dao.RestElasticsearchRepositoryFactoryBean;
import info.jallaix.message.service.DomainController;
import info.jallaix.message.service.GenericExceptionHandler;
import info.jallaix.message.service.hateoas.DomainsResourceProcessor;
import info.jallaix.spring.data.es.test.SpringDataEsTestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.rest.webmvc.RepositorySearchesResource;
import org.springframework.hateoas.*;

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
    ResourceProcessor<RepositorySearchesResource> domainSearchResourceProcessor() {

        return new ResourceProcessor<RepositorySearchesResource>() {

            @Autowired
            private EntityLinks entityLinks;

            @Override
            public RepositorySearchesResource process(RepositorySearchesResource domain) {

                if(Domain.class.equals(domain.getDomainType())) {

                    final String search = domain.getId().getHref();
                    final Link customLink = entityLinks.linkForSingleResource(Domain.class, domain.getId()).slash("search/findByCode{?code}").withRel("findByCode");
                    domain.add(customLink);
                }

                return domain;
            }
        };
    }
}
