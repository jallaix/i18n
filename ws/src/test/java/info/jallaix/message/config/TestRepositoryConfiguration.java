package info.jallaix.message.config;

import org.elasticsearch.client.Client;
import org.elasticsearch.node.NodeBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

/**
 * Test repository configuration.
 */
@Configuration
public class TestRepositoryConfiguration extends RepositoryConfiguration {

    /**
     * Define the Elasticsearch client, used by the Elasticsearch Test framework
     *
     * @return The Elasticsearch client
     */
    @Bean
    public Client elasticsearchClient() throws IOException {

        // Clean the testing Elasticsearch index (may be inconsistent)
        Path rootPath = Paths.get("target/test-data");
        if (rootPath.toFile().exists())
            Files.walk(rootPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);

        // Configure the testing Elasticsearch index
        NodeBuilder nodeBuilder = NodeBuilder.nodeBuilder();
        nodeBuilder.settings().put("path.home", "target");
        nodeBuilder.settings().put("path.data", "target/test-data");
        nodeBuilder.local(true);

        return nodeBuilder.node().client();
    }
}
