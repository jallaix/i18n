package info.jallaix.message.dao.impl;

import info.jallaix.message.bean.EntityMessage;
import info.jallaix.message.dao.EntityMessageDaoCustom;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.util.List;

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
     * Empty constructor
     */
    public EntityMessageDaoImpl() {
    }

    /**
     * Constructor with operations
     *
     * @param operations Elasticsearch operations
     */
    public EntityMessageDaoImpl(ElasticsearchOperations operations) {
        this.operations = operations;
    }


    /**
     * Find a message for the specified arguments.
     *
     * @param domainId    Domain identifier to filter messages
     * @param type        Type to filter messages
     * @param entityId    Entity identifier to filter messages
     * @param languageTag Language tag to filter messages
     * @return The matched message or {@code null}
     */
    @Override
    public EntityMessage findOne(final String domainId, final String type, final String entityId, final String languageTag) {

        // Define the filter
        final List<EntityMessage> messages = operations.queryForList(
                new NativeSearchQueryBuilder()
                        .withQuery(
                                QueryBuilders.constantScoreQuery(
                                        QueryBuilders.boolQuery()
                                                .must(QueryBuilders.termQuery(EntityMessage.FIELD_DOMAIN_ID.getName(), domainId))
                                                .must(QueryBuilders.termQuery(EntityMessage.FIELD_TYPE.getName(), type))
                                                .must(QueryBuilders.termQuery(EntityMessage.FIELD_ENTITY_ID.getName(), entityId))
                                                .must(QueryBuilders.termQuery(EntityMessage.FIELD_LANGUAGE_TAG.getName(), languageTag))))
                        .build(), EntityMessage.class);

        if (messages.isEmpty())
            return null;
        else if (messages.size() > 1)
            throw new RuntimeException("At most one message should be found given the criteria.");
        else
            return messages.get(0);
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
