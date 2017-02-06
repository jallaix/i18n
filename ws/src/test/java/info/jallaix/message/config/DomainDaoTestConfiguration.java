package info.jallaix.message.config;

import info.jallaix.message.dao.interceptor.DomainDaoInterceptor;
import info.jallaix.message.dao.interceptor.ThreadLocaleHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import java.util.Arrays;

/**
 * Created by Julien on 01/02/2017.
 */
@Configuration
@Import({DomainDaoInterceptor.class, ThreadLocaleHolder.class})
public class DomainDaoTestConfiguration {

    @Autowired
    private ElasticsearchOperations esOperations;


    /**
     * The i18n domain holder gives access to the test domain data for the current application.
     *
     * @return The i18n domain holder
     */
    @Bean
    public DomainHolder i18nDomainHolder() {

        I18nDomainHolder domainHolder = new I18nDomainHolder(esOperations);
        domainHolder.setDomainAvailableLanguageTags(Arrays.asList("en-US", "fr-FR"));

        return domainHolder;
    }
}
