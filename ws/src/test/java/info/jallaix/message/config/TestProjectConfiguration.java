package info.jallaix.message.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import java.util.Arrays;

/**
 * Project configuration
 */
@Configuration
//@PropertySource("classpath:/info/jallaix/message/config/project.properties}")
public class TestProjectConfiguration extends ProjectConfiguration {

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
