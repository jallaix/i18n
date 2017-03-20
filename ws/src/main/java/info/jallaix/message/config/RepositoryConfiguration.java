package info.jallaix.message.config;

/**
 * Created by JAX on 20/03/2017.
 */

import org.elasticsearch.client.Client;
import org.elasticsearch.node.NodeBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

import java.io.IOException;

/**
 * Repository configuration
 */
@Configuration
public class RepositoryConfiguration {

    /**
     * Define the Elasticsearch client, used by the Elasticsearch Test framework
     *
     * @return The Elasticsearch client
     */
    @Bean
    public Client elasticsearchClient() throws IOException {

        // Configure the testing Elasticsearch index
        NodeBuilder nodeBuilder = NodeBuilder.nodeBuilder();
        nodeBuilder.local(true);

        return nodeBuilder.node().client();
    }

    /**
     * Define the Elastic search operations template, used by the Spring Data framework
     *
     * @return The Elastic search operations template
     */
    @Bean
    public ElasticsearchOperations elasticsearchTemplate() throws IOException {
        return new ElasticsearchTemplate(elasticsearchClient());
    }
}
