package info.jallaix.common.language.dao;

import org.elasticsearch.node.NodeBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Created by Julien on 12/05/2016.
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "info.jallaix.common.language.dao")
public class LanguageDaoTestConfiguration {

    @Bean
    public ElasticsearchOperations elasticsearchTemplate() {

        return new ElasticsearchTemplate(NodeBuilder.nodeBuilder().local(true).node().client());
    }
}
