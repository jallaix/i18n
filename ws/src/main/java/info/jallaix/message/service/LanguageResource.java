package info.jallaix.message.service;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.ResourceSupport;

/**
 * This bean contains transfer data related to a language:
 * <ul>
 *    <li>{@code label} - {@link String} - Language label
 *    <li>{@code englishLabel} - {@link String} - Language english label
 * </ul>
 */
@Data
@NoArgsConstructor
public class LanguageResource extends ResourceSupport {

    /**
     * Language label
     */
    private String label;

    /**
     * Language english label
     */
    private String englishLabel;
}
