package info.jallaix.message.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.style.ToStringCreator;
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
@NoArgsConstructor
@AllArgsConstructor
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
