package info.jallaix.message.config;

import info.jallaix.message.dto.Domain;
import lombok.Setter;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.GetQuery;
import org.springframework.data.elasticsearch.core.query.IndexQuery;

import java.util.Collection;
import java.util.Collections;

/**
 * The i18n domain holder gets the application domain from the ES index or create the domain if it doesn't exist.
 */
public class I18nDomainHolder implements DomainHolder {

    public static final String DOMAIN_DESCRIPTION_TYPE = Domain.class.getName() + ".description";
    public static final String DOMAIN_CODE = "i18n.message";

    private ElasticsearchOperations esOperations;
    private Domain messageDomain = null;

    public I18nDomainHolder(ElasticsearchOperations esOperations) {
        this.esOperations = esOperations;
    }

    @Setter
    protected String domainCode = DOMAIN_CODE;

    @Setter
    protected String domainDefaultLanguageTag = "en-US";

    @Setter
    protected Collection<String> domainAvailableLanguageTags = Collections.singleton("en-US");

    /**
     * Get the internationalized domain of the application from the ES index.
     * If it doesn't exist, create the domain in the index.
     *
     * @return The found domain
     */
    @Override
    public Domain getDomain() {

        if (messageDomain != null)
            return messageDomain;

        // Get the message domain
        if (esOperations.indexExists(Domain.class))
            messageDomain = esOperations.queryForObject(new CriteriaQuery(new Criteria("code").is(DOMAIN_CODE)), Domain.class);

        // Index the message domain if it's unavailable in the ES index
        if (messageDomain == null) {

            IndexQuery indexQuery = new IndexQuery();
            indexQuery.setObject(new Domain(null, domainCode, DOMAIN_DESCRIPTION_TYPE, domainDefaultLanguageTag, domainAvailableLanguageTags));
            String messageDomainId = esOperations.index(indexQuery);

            GetQuery getQuery = new GetQuery();
            getQuery.setId(messageDomainId);
            messageDomain = esOperations.queryForObject(getQuery, Domain.class);
        }

        return messageDomain;
    }
}
