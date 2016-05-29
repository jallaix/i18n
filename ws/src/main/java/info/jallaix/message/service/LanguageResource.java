package info.jallaix.message.service;

import org.springframework.hateoas.ResourceSupport;

/**
 * Created by Julien on 29/05/2016.
 */
public class LanguageResource extends ResourceSupport {

    private String label;
    private String englishLabel;

    public String getEnglishLabel() {
        return englishLabel;
    }

    public void setEnglishLabel(String englishLabel) {
        this.englishLabel = englishLabel;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LanguageResource)) return false;
        if (!super.equals(o)) return false;

        LanguageResource that = (LanguageResource) o;

        if (label != null ? !label.equals(that.label) : that.label != null) return false;
        if (englishLabel != null ? !englishLabel.equals(that.englishLabel) : that.englishLabel != null) return false;

        return this.getLinks().equals(that.getLinks());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (englishLabel != null ? englishLabel.hashCode() : 0);
        return result;
    }
}
