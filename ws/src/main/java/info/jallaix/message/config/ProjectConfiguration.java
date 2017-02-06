package info.jallaix.message.config;

import info.jallaix.message.dao.DomainDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

/**
 * Project configuration
 */
@Configuration
@PropertySource("classpath:/info/jallaix/message/config/project.properties}")
public class ProjectConfiguration {

    /**
     * Repository for domain data
     */
    @Autowired
    private DomainDao domainDao;

    @Autowired
    private ElasticsearchOperations esOperations;

    /**
     * Property resource configurer that resolves ${} in @Value annotations
     *
     * @return The property resource configurer
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfig() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    /**
     * The i18n domain holder gives access to the domain data for the current application.
     *
     * @return The i18n domain holder
     */
    @Bean
    public DomainHolder i18nDomainHolder() {
        return new I18nDomainHolder(esOperations);
    }
}
