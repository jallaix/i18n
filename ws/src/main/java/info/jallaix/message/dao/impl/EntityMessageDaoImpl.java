package info.jallaix.message.dao.impl;

import info.jallaix.message.dao.EntityMessageDaoCustom;
import info.jallaix.message.bean.Domain;
import info.jallaix.message.bean.EntityMessage;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;

import static org.elasticsearch.index.query.FilterBuilders.boolFilter;
import static org.elasticsearch.index.query.FilterBuilders.termFilter;
import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * This class implements custom datasource accesses related to a message.
 */
public class EntityMessageDaoImpl implements EntityMessageDaoCustom {

    /**
     * Elasticsearch operations
     */
    @Autowired
    private ElasticsearchOperations operations;

    /**
     * Indicate if a language is used in any message.
     *
     * @param languageId The language identifier
     * @return {@code true} if the language is used, else {@code false}
     */
    @Override
    public boolean isLanguageUsed(String languageId) {

        QueryBuilder queryBuilder = boolQuery().must(matchQuery("languageTag", languageId));
        SearchQuery query = new NativeSearchQuery(queryBuilder);
        return operations.count(query, Domain.class) > 0;
    }

    /**
     * <p>Find messages with content matching the provided one.</p>
     * <p>The results may be filtered by domain identifier, message type and language tag.</p>
     *
     * @param domainId    Domain identifier to filter messages
     * @param type        Type to filter messages
     * @param languageTag Language tag to filter messages
     * @param content     Content to match
     * @return The array of matching entity messages
     */
    @Override
    public Iterable<EntityMessage> findByContent(final String domainId, final String type, final String languageTag, final String content) {

        // Define the filter depending on the provided parameters
        BoolFilterBuilder filterBuilder = boolFilter();
        if (domainId != null)
            filterBuilder.must(termFilter(EntityMessage.FIELD_DOMAIN_ID.getName(), domainId));
        if (type != null)
            filterBuilder.must(termFilter(EntityMessage.FIELD_TYPE.getName(), type));
        if (languageTag != null)
            filterBuilder.must(termFilter(EntityMessage.FIELD_LANGUAGE_TAG.getName(), languageTag));

        // Define the query for matching the provided content with the filter
        QueryBuilder queryBuilder = filteredQuery(
                boolQuery()
                        .must(
                                matchQuery(EntityMessage.FIELD_CONTENT.getName(), content)),
                filterBuilder
        );

        // Get matching messages
        return operations.queryForList(
                new NativeSearchQueryBuilder()
                        .withQuery(queryBuilder)
                        .build(),
                EntityMessage.class);
    }
}
