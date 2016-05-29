package info.jallaix.message.dao;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.node.NodeBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import javax.annotation.Resource;

/**
 * Configuration d'Elasticsearch
 */
@Configuration
@PropertySource(value = "classpath:info/jallaix/message/dao/elasticsearch.properties")
@EnableElasticsearchRepositories(repositoryFactoryBeanClass = RestElasticsearchRepositoryFactoryBean.class)
public class SpringDataEsConfiguration {

    @Resource
    private Environment environment;

    @Bean
    public Client client() {

        /*TransportClient client = new TransportClient();
        TransportAddress address = new InetSocketTransportAddress(
                environment.getProperty("elasticsearch.host"),
                Integer.parseInt(environment.getProperty("elasticsearch.port")));
        client.addTransportAddress(address);

        return client;*/
        NodeBuilder nodeBuilder = NodeBuilder.nodeBuilder();
        nodeBuilder.settings().put("path.data", "target/data");
        nodeBuilder.local(true);

        return nodeBuilder.node().client();
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate() {

        return new ElasticsearchTemplate(client());
    }
}
