package info.jallaix.message.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.ResourceSupport;

/**
 * This bean contains transfer data related to a language:
 * <ul>
 *    <li>{@code code} - {@link String} - Language code
 *    <li>{@code label} - {@link String} - Language label
 *    <li>{@code englishLabel} - {@link String} - Language english label
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LanguageResource extends ResourceSupport {

    /**
     * Language code
     */
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
