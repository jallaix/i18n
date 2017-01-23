package info.jallaix.message.dao.impl;

import info.jallaix.message.dao.MessageDaoCustom;
import info.jallaix.message.dto.Domain;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.SearchQuery;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

/**
 * This class implements custom datasource accesses related to a message.
 */
public class MessageDaoImpl implements MessageDaoCustom {

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
}
