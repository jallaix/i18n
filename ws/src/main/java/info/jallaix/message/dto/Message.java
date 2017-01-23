package info.jallaix.message.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

/**
 * This bean contains persistent data related to a message:
 * <ul>
 *    <li>{@code code} - {@link String} - Message code
 *    <li>{@code languageTag} - {@link String} - Linked language tag
 *    <li>{@code domainCode} - {@link String} - Linked domain code
 *    <li>{@code content} - {@link String} - Message content
 * </ul>
 */
@Document(indexName = "message", type = "message", shards = 1, replicas = 0)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    /**
     * Identifier
     */
    @Id
    private String id;

    /**
     * Linked domain identifier
     */
    private String domainCode;

    /**
     * Message code
     */
    private String code;

    /**
     * Linked language identifier
     */
    private String languageTag;

    /**
     * Message content
     */
    private String content;
}
