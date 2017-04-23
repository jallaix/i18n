package info.jallaix.message.bean;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * This bean contains persistent data related to a message linked to a custom key:
 * <ul>
 * <li>{@code id} - {@link String} - Unique identifier of the message
 * <li>{@code domainId} - {@link String} - Identifier of the domain linked to the message, see {@link Domain}
 * <li>{@code key} - {@link String} - Key of the message, for example: "/info/jallaix/message/bean/Domain/description"
 * <li>{@code languageTag} - {@link String} - Language tag of the message
 * <li>{@code content} - {@link String} - Content of the message
 * </ul>
 * <p>
 *     A domain identifier, a message key, and a language tag uniquely identify a message content.
 * </p>
 */
@Document(indexName = "message", type = "key_message", shards = 1, replicas = 0)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeyMessage {

    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                 Fields introspection                                           */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Field for domain identifier
     */
    @Setter(AccessLevel.NONE) @Getter(AccessLevel.NONE)
    public final static java.lang.reflect.Field FIELD_DOMAIN_ID;;

    /**
     * Field for message key
     */
    @Setter(AccessLevel.NONE) @Getter(AccessLevel.NONE)
    public final static java.lang.reflect.Field FIELD_KEY;

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
            FIELD_DOMAIN_ID = KeyMessage.class.getDeclaredField("domainId");
            FIELD_KEY = KeyMessage.class.getDeclaredField("key");
            FIELD_LANGUAGE_TAG = KeyMessage.class.getDeclaredField("languageTag");
            FIELD_CONTENT = KeyMessage.class.getDeclaredField("content");
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
     * Message key
     */
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String key;

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
