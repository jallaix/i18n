package info.jallaix.message.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

/**
 * This bean contains data related to a language.
 */
@Document(indexName = "message", type = "language", shards = 1, replicas = 0)
public class Language {

    @Id
    private String code;            // Language code
    private String label;           // Language Label
    private String englishLabel;    // Language english label


    /**
     * Empty constructor
     */
    public Language() {}

    /**
     * Constructor with property initialization
     * @param code The code to set
     * @param label The label to set
     * @param englishLabel The english label to set
     */
    public Language(String code, String label, String englishLabel) {

        this.code = code;
        this.label = label;
        this.englishLabel = englishLabel;
    }


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                           Accessors/Mutators                                                   */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Get the language code
     * @return The language code
     */
    public String getCode() {
        return code;
    }

    /**
     * Set the language code
     * @param code The language code to set
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Get the language label
     * @return The language label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Set the language label
     * @param label The language label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Get the language english label
     * @return The language english label
     */
    public String getEnglishLabel() {
        return englishLabel;
    }

    /**
     * Set the language english label
     * @param englishLabel The language english label to set
     */
    public void setEnglishLabel(String englishLabel) {
        this.englishLabel = englishLabel;
    }


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                           Standard overrides                                                   */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Define if another language equals this one. Every property must have the same value in each side.
     * @param o The object to compare with this language
     * @return <code>true</code> if the compared object is equal, else <code>false</code>
     */
    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof Language)) return false;

        Language language = (Language) o;

        if (code != null ? !code.equals(language.code) : language.code != null) return false;
        if (label != null ? !label.equals(language.label) : language.label != null) return false;
        return englishLabel != null ? englishLabel.equals(language.englishLabel) : language.englishLabel == null;
    }

    /**
     * Get the hash code of this language
     * @return The hash code of this language
     */
    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (englishLabel != null ? englishLabel.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "An error occured when calling toString : " + e.getMessage();
        }
    }
}
