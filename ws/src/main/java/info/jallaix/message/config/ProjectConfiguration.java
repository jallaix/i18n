package info.jallaix.message.config;

import info.jallaix.message.dao.DomainDao;
import info.jallaix.message.dto.Domain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.GetQuery;
import org.springframework.data.elasticsearch.core.query.IndexQuery;

import java.util.Collections;

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
     * The message domain bean contains the domain data for the current application.
     *
     * @return The message domain
     */
    @Bean
    public Domain messageDomain() {

        // Get the message domain
        Domain messageDomain = null;
        if (esOperations.indexExists(Domain.class))
            messageDomain = esOperations.queryForObject(new CriteriaQuery(new Criteria("code").is("i18n.message")), Domain.class);

        // Index the message domain if it's unavailable in the ES index
        if (messageDomain == null) {

            IndexQuery indexQuery = new IndexQuery();
            indexQuery.setObject(new Domain(null, "i18n.message", "Internationalized messages", "en-US", Collections.singleton("en-US")));
            String messageDomainId = esOperations.index(indexQuery);

            GetQuery getQuery = new GetQuery();
            getQuery.setId(messageDomainId);
            messageDomain = esOperations.queryForObject(getQuery, Domain.class);
        }

        return messageDomain;
    }
}
