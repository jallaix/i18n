package info.jallaix.message.config;

import info.jallaix.spring.data.es.test.SpringDataEsTestConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Configuration for testing the Domain repository.
 */
@Configuration
@Import({TestProjectConfiguration.class, TestRepositoryConfiguration.class, SpringDataEsTestConfiguration.class})
public class TestDomainDaoConfiguration extends ProjectConfiguration {
}
