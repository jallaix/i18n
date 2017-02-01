package info.jallaix.message.dao.interceptor;

/**
 * This exception is thrown when a locale is not supported by a domain.
 */
public class UnsupportedLanguageException extends RuntimeException {

    /**
     * Constructor with language tag an domain code.
     *
     * @param languageTag The unsupported language tag
     * @param domainCode The code of the domain that doesn't support the language tag
     */
    public UnsupportedLanguageException(String languageTag, String domainCode) {
        super("The " + languageTag + "is not supported by the " + domainCode + "domain.");
    }
}
