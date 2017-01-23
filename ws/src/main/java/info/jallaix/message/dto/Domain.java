package info.jallaix.message.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.Collection;

/**
 * This bean contains persistent data related to a domain:
 * <ul>
 *    <li>{@code code} - {@link String} - Domain code</li>
 *    <li>{@code description} - {@link String} - Message code for the domain description</li>
 *    <li>{@code defaultLanguageTag} - {@link String} - Linked default language identifier</li>
 *    <li>{@code availableLanguageTags} - {@link Collection<String>} - Collection of available language tags (in BCP 47 format) for the domain</li>
 * </ul>
 */
@Document(indexName = "message", type = "domain", shards = 1, replicas = 0)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Domain {

    /**
     * Identifier
     */
    @Id
    private String id;

    /**
     * Domain code
     */
    private String code;

    /**
     * Message code for the domain description
     */
    private String description;

    /**
     * Domain default language tag (in BCP 47 format)
     */
    private String defaultLanguageTag;

    /**
     * Collection of available language tags (in BCP 47 format) for the domain
     */
    private Collection<String> availableLanguageTags;
}
