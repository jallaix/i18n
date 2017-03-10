package info.jallaix.message.bean;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * This bean contains persistent data related to a message linked to an entity property:
 * <ul>
 * <li>{@code id} - {@link String} - Unique identifier of the message
 * <li>{@code domainId} - {@link String} - Identifier of the domain linked to the message, see {@link Domain}
 * <li>{@code type} - {@link String} - Type of the message, for example: "info.jallaix.message.bean.Domain.description"
 * <li>{@code entityId} - {@link String} - Identifier of the entity linked to the message
 * <li>{@code languageTag} - {@link String} - Language tag of the message
 * <li>{@code content} - {@link String} - Content of the message
 * </ul>
 * <p>
 *     A domain identifier, a message type, an entity identifier and a language tag uniquely identify a message content.
 * </p>
 */
@Document(indexName = "message", type = "entity_message", shards = 1, replicas = 0)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityMessage {

    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                 Fields introspection                                           */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Field for domain identifier
     */
    @Setter(AccessLevel.NONE) @Getter(AccessLevel.NONE)
    public final static java.lang.reflect.Field FIELD_DOMAIN_ID;

    /**
     * Field for message type
     */
    @Setter(AccessLevel.NONE) @Getter(AccessLevel.NONE)
    public final static java.lang.reflect.Field FIELD_TYPE;

    /**
     * Field for entity identifier
     */
    @Setter(AccessLevel.NONE) @Getter(AccessLevel.NONE)
    public final static java.lang.reflect.Field FIELD_ENTITY_ID;

    /**
     * Field for language tag
     */
    @Setter(AccessLevel.NONE) @Getter(AccessLevel.NONE)
    public final static java.lang.reflect.Field FIELD_LANGUAGE_TAG;

    /**
     * Field for message content
     */
    @Setter(AccessLevel.NONE) @Getter(AccessLevel.NONE)
    public final static java.lang.reflect.Field FIELD_CONTENT;

    // Get the class fields
    static {
        try {
            FIELD_DOMAIN_ID = EntityMessage.class.getDeclaredField("domainId");
            FIELD_TYPE = EntityMessage.class.getDeclaredField("type");
            FIELD_ENTITY_ID = EntityMessage.class.getDeclaredField("entityId");
            FIELD_LANGUAGE_TAG = EntityMessage.class.getDeclaredField("languageTag");
            FIELD_CONTENT = EntityMessage.class.getDeclaredField("content");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                   Document fields                                              */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Internal identifier
     */
    @Id
    private String id;

    /**
     * Domain identifier
     */
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String domainId;

    /**
     * Message type
     */
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String type;

    /**
     * Entity identifier
     */
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String entityId;

    /**
     * Language tag
     */
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String languageTag;

    /**
     * Message content
     */
    @Field(type = FieldType.String, index = FieldIndex.analyzed)
    private String content;
}
