package info.jallaix.message.config;

import info.jallaix.message.dto.Domain;
import info.jallaix.message.dto.EntityMessage;
import lombok.Setter;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.GetQuery;
import org.springframework.data.elasticsearch.core.query.IndexQuery;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

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
    protected String domainDefaultLanguageTag = "en";

    @Setter
    protected Collection<String> domainAvailableLanguageTags = Collections.singleton("en");

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

        loadI18nDomain();

        return messageDomain;
    }

    /**
     * Load the I18N domain.
     */
    private synchronized void loadI18nDomain() {

        // Get the message domain
        if (esOperations.indexExists(Domain.class))
            messageDomain = esOperations.queryForObject(new CriteriaQuery(new Criteria("code").is(DOMAIN_CODE)), Domain.class);

        // Index the message domain if it's unavailable in the ES index
        if (messageDomain == null) {

            // Define domain and message mappings
            putDomainMapping();
            putMessageMapping();

            // Insert the I18N domain
            IndexQuery indexQuery = new IndexQuery();
            indexQuery.setObject(new Domain(null, domainCode, DOMAIN_DESCRIPTION_TYPE, domainDefaultLanguageTag, domainAvailableLanguageTags));
            String messageDomainId = esOperations.index(indexQuery);

            // Get the inserted I18N domain
            GetQuery getQuery = new GetQuery();
            getQuery.setId(messageDomainId);
            messageDomain = esOperations.queryForObject(getQuery, Domain.class);
        }
    }

    /**
     * Define the mapping for the Domain entity.
     */
    private void putDomainMapping() {

        final XContentBuilder mapping;
        try {
            mapping = jsonBuilder()
                    .startObject()
                    .startObject("properties")
                        /**/.startObject("code")
                        /*    */.field("type", "string")
                        /*    */.field("index", "not_analyzed")
                        /**/.endObject()
                        /**/.startObject("description")
                        /*    */.field("type", "string")
                        /*    */.field("index", "not_analyzed")
                        /**/.endObject()
                        /**/.startObject("defaultLanguageTag")
                        /*    */.field("type", "string")
                        /*    */.field("index", "not_analyzed")
                        /**/.endObject()
                        /**/.startObject("availableLanguageTags")
                        /*    */.field("type", "string")
                        /*    */.field("index", "not_analyzed")
                        /**/.endObject()
                    .endObject()
                    .endObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        esOperations.putMapping(Domain.class, mapping);
    }

    /**
     * Define the mapping for the EntityMessage entity.
     */
    private void putMessageMapping() {

        final XContentBuilder mapping;
        try {
            mapping = jsonBuilder()
                    .startObject()
                    .startObject("properties")
                        /**/.startObject(EntityMessage.FIELD_DOMAIN_ID.getName())
                        /*    */.field("type", "string")
                        /*    */.field("index", "not_analyzed")
                        /**/.endObject()
                        /**/.startObject(EntityMessage.FIELD_TYPE.getName())
                        /*    */.field("type", "string")
                        /*    */.field("index", "not_analyzed")
                        /**/.endObject()
                        /**/.startObject(EntityMessage.FIELD_ENTITY_ID.getName())
                        /*    */.field("type", "string")
                        /*    */.field("index", "not_analyzed")
                        /**/.endObject()
                        /**/.startObject(EntityMessage.FIELD_LANGUAGE_TAG.getName())
                        /*    */.field("type", "string")
                        /*    */.field("index", "not_analyzed")
                        /**/.endObject()
                        /**/.startObject(EntityMessage.FIELD_CONTENT.getName())
                        /*    */.field("type", "string")
                        /**/.endObject()
                    .endObject()
                    .endObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        esOperations.putMapping(Domain.class, mapping);
    }
}
