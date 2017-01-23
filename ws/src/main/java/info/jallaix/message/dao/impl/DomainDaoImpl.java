package info.jallaix.message.dao.impl;

import info.jallaix.message.dao.DomainDaoCustom;
import info.jallaix.message.dto.Domain;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.SearchQuery;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * This class implements custom datasource accesses related to a domain.
 */
public class DomainDaoImpl implements DomainDaoCustom {

    @Autowired
    private ElasticsearchOperations operations;

    /**
     * Indicate if a language is used in any domain.
     *
     * @param languageTag The language identifier
     * @return {@code true} if the language is used, else {@code false}
     */
    @Override
    public boolean isLanguageUsed(String languageTag) {

        QueryBuilder queryBuilder = boolQuery().must(matchQuery("defaultLanguageTag", languageTag));
        SearchQuery query = new  NativeSearchQuery(queryBuilder);
        return operations.count(query, Domain.class) > 0;
    }
}
