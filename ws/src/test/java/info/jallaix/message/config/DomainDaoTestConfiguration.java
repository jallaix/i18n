package info.jallaix.message.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import java.util.Arrays;

/**
 * Configuration for testing the
 */
@Configuration
public class DomainDaoTestConfiguration extends ProjectConfiguration {

    /**
     * Elasticsearch operations
     */
    @Autowired
    private ElasticsearchOperations esOperations;


    /**
     * The i18n domain holder gives access to the test domain data for the current application.
     *
     * @return The i18n domain holder
     */
    @Override
    public DomainHolder i18nDomainHolder() {

        I18nDomainHolder domainHolder = new I18nDomainHolder(esOperations);
        domainHolder.setDomainAvailableLanguageTags(Arrays.asList("en", "fr", "es"));

        return domainHolder;
    }
}
