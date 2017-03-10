package info.jallaix.message.dao.interceptor;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * This exception is thrown when a locale is not supported by a domain.
 */
public class UnsupportedLanguageException extends RuntimeException {

    /**
     * Constructor with locale an domain identifier.
     *
     * @param locale The unsupported locale
     * @param domainId The identifier of the domain that doesn't support the locale
     */
    public UnsupportedLanguageException(Locale locale, String domainId) {
        super();
        MessageFormat.format(
                "The \"{0}\" language is not supported by the domain (id={1}).",
                locale.getLanguage(),
                domainId);
    }
}
