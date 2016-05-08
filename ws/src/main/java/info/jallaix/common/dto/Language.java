package info.jallaix.common.dto;

/**
 * Created by Julien on 08/05/2016.
 */
public class Language {

    private String code;
    private String label;
    private String englishLabel;

    public Language(String code, String label, String englishLabel) {
        this.code = code;
        this.label = label;
        this.englishLabel = englishLabel;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getEnglishLabel() {
        return englishLabel;
    }

    public void setEnglishLabel(String englishLabel) {
        this.englishLabel = englishLabel;
    }
}
