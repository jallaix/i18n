package info.jallaix.message.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

/**
 * This bean contains persistent data related to a language:
 * <ul>
 *    <li>{@code code} - {@link String} - Language code
 *    <li>{@code label} - {@link String} - Language label
 *    <li>{@code englishLabel} - {@link String} - Language english label
 * </ul>
 */
@Document(indexName = "message", type = "language", shards = 1, replicas = 0)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Language {

    /**
     * Language code
     */
    @Id
    private String code;

    /**
     * Language label
     */
    private String label;

    /**
     * Language english label
     */
    private String englishLabel;
}
