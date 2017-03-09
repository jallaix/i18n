package info.jallaix.message.dto;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;

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
     * Message type for the domain description
     */
    @Setter(AccessLevel.NONE) @Getter(AccessLevel.NONE)
    public static final String DOMAIN_DESCRIPTION_TYPE = Domain.class.getName() + ".description";

    /**
     * Identifier
     */
    @Id
    private String id;

    /**
     * Domain code
     */
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String code;

    /**
     * Message code for the domain description
     */
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String description;

    /**
     * Default language tag (in BCP 47 format) for the domain
     */
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String defaultLanguageTag;

    /**
     * Collection of available language tags (in BCP 47 format) for the domain
     */
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private Collection<String> availableLanguageTags;
}
