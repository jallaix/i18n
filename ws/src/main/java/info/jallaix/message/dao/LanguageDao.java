package info.jallaix.message.dao;

import info.jallaix.message.dto.Language;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * This interface manages all datasource accesses related to a language
 */
@Repository
public interface LanguageDao extends ElasticsearchRepository<Language, String> {
}
