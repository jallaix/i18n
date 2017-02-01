package info.jallaix.message.dao;

import info.jallaix.message.dao.interceptor.DomainDaoInterceptor;
import info.jallaix.message.dto.Domain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.GetQuery;
import org.springframework.data.elasticsearch.core.query.IndexQuery;

import java.util.Arrays;

/**
 * Created by Julien on 01/02/2017.
 */
@Configuration
@Import({DomainDaoInterceptor.class})
public class DomainDaoConfiguration {

    @Autowired
    private ElasticsearchOperations esOperations;

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
            indexQuery.setObject(new Domain(null, "i18n.message", "Internationalized messages", "en-US", Arrays.asList("en-US", "fr-FR")));
            String messageDomainId = esOperations.index(indexQuery);

            GetQuery getQuery = new GetQuery();
            getQuery.setId(messageDomainId);
            messageDomain = esOperations.queryForObject(getQuery, Domain.class);
        }

        return messageDomain;
    }
}
