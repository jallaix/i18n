package info.jallaix.message.dao.interceptor;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * This exception is thrown when inserting a message for a complex locale (with data other than language)
 * whereas no message already exists for the simple locale (with just the language).
 */
public class MissingSimpleMessageException extends RuntimeException {

    /**
     * Constructor with complex locale and domain identifier.
     *
     * @param complexLocale The complex locale that triggered the exception
     * @param domainId      The identifier of the domain that is missing the simple message
     */
    public MissingSimpleMessageException(Locale complexLocale, String domainId) {
        super(
                MessageFormat.format(
                        "As no message is defined in the domain (id={0}) for the '{1}' language tag, it's not possible to insert a message for the '{2}' language tag.",
                        domainId,
                        complexLocale.getLanguage(),
                        complexLocale.toLanguageTag()));
    }
}
