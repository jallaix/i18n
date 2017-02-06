package info.jallaix.message.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

/**
 * This bean contains persistent data related to a message:
 * <ul>
 * <li>{@code id} - {@link String} - Unique identifier of the message
 * <li>{@code domainId} - {@link String} - Identifier of the domain linked to the message, see {@link Domain}
 * <li>{@code type} - {@link String} - Type of the message, for example: "/info/jallaix/message/dto/Domain/description"
 * <li>{@code entityId} - {@link String} - Identifier of the entity linked to the message
 * <li>{@code languageTag} - {@link String} - Language tag of the message
 * <li>{@code content} - {@link String} - Content of the message
 * </ul>
 * <p>
 *     A domain identifier, a message type, an entity identifier and a language tag uniquely identify a message content.
 * </p>
 */
@Document(indexName = "message", type = "message", shards = 1, replicas = 0)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    /**
     * Internal identifier
     */
    @Id
    private String id;

    /**
     * Domain identifier
     */
    private String domainId;

    /**
     * Message type
     */
    private String type;

    /**
     * Entity identifier
     */
    private String entityId;

    /**
     * Language tag
     */
    private String languageTag;

    /**
     * Message content
     */
    private String content;
}
