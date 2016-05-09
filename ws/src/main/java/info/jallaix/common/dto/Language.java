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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Language)) return false;

        Language language = (Language) o;

        if (code != null ? !code.equals(language.code) : language.code != null) return false;
        if (label != null ? !label.equals(language.label) : language.label != null) return false;
        return englishLabel != null ? englishLabel.equals(language.englishLabel) : language.englishLabel == null;

    }

    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (englishLabel != null ? englishLabel.hashCode() : 0);
        return result;
    }
}
