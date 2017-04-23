package info.jallaix.message.dao.impl;

import info.jallaix.message.bean.KeyMessage;
import info.jallaix.message.dao.KeyMessageDaoCustom;
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
public class KeyMessageDaoImpl implements KeyMessageDaoCustom {

    /**
     * Elasticsearch operations
     */
    @Autowired
    private ElasticsearchOperations operations;


    /**
     * Empty constructor
     */
    public KeyMessageDaoImpl() {
    }

    /**
     * Constructor with operations
     *
     * @param operations Elasticsearch operations
     */
    public KeyMessageDaoImpl(ElasticsearchOperations operations) {
        this.operations = operations;
    }


    /**
     * Find a message for the specified arguments.
     *
     * @param domainId    Domain identifier to filter messages
     * @param key         Message key
     * @param languageTag Language tag to filter messages
     * @return The matched message or {@code null}
     */
    @Override
    public KeyMessage findOne(final String domainId, final String key, final String languageTag) {

        // Define the filter
        final List<KeyMessage> messages = operations.queryForList(
                new NativeSearchQueryBuilder()
                        .withQuery(
                                QueryBuilders.constantScoreQuery(
                                        QueryBuilders.boolQuery()
                                                .must(QueryBuilders.termQuery(KeyMessage.FIELD_DOMAIN_ID.getName(), domainId))
                                                .must(QueryBuilders.termQuery(KeyMessage.FIELD_KEY.getName(), key))
                                                .must(QueryBuilders.termQuery(KeyMessage.FIELD_LANGUAGE_TAG.getName(), languageTag))))
                        .build(), KeyMessage.class);

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
     * @param languageTag Language tag to filter messages
     * @param content     Content to match
     * @return The array of matching entity messages
     */
    @Override
    public Iterable<KeyMessage> findByContent(final String domainId, final String languageTag, final String content) {

        // Define the filter depending on the provided parameters
        BoolFilterBuilder filterBuilder = boolFilter();
        if (domainId != null)
            filterBuilder.must(termFilter(KeyMessage.FIELD_DOMAIN_ID.getName(), domainId));
        if (languageTag != null)
            filterBuilder.must(termFilter(KeyMessage.FIELD_LANGUAGE_TAG.getName(), languageTag));

        // Define the query for matching the provided content with the filter
        QueryBuilder queryBuilder = filteredQuery(
                boolQuery()
                        .must(
                                matchQuery(KeyMessage.FIELD_CONTENT.getName(), content)),
                filterBuilder
        );

        // Get matching messages
        return operations.queryForList(
                new NativeSearchQueryBuilder()
                        .withQuery(queryBuilder)
                        .build(),
                KeyMessage.class);
    }
}
