package info.jallaix.message.dao.interceptor;

import info.jallaix.message.dao.DomainDao;
import info.jallaix.message.dto.Domain;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Locale;

/**
 * <p>
 * This bean holds locale data linked to the current thread.
 * </p>
 * <p>
 * Locale data are :
 * <ul>
 *     <li>Input locale : used by {@link DomainDaoInterceptor} when saving a localized document</li>
 *     <li>Output locale : used by {@link DomainDaoInterceptor} when getting a localized document</li>
 * </ul>
 * </p>
 */
@Component
@NoArgsConstructor
public class ThreadLocaleHolder {

    /**
     * The message domain
     */
    @Value("${i18n.message.domain ?: 'default'}")
    private String messageDomain;

    @Autowired
    private DomainDao domainDao;

    /**
     * Domain's available locales
     */
    private Collection<Locale> domainLocales;


    /**
     * Locale used when storing data
     */
    private ThreadLocal<Locale> inputLocale = new ThreadLocal<>();

    /**
     * Locale used when getting data
     */
    private ThreadLocal<Locale> outputLocale = new ThreadLocal<>();


    /**
     * Set the input locale into a thread variable
     *
     * @param inputLocale The input locale to set
     */
    public void setInputLocale(Locale inputLocale) {
        this.inputLocale.set(inputLocale);
    }

    /**
     * Get the input locale from a thread variable
     *
     * @return The input locale
     */
    public Locale getInputLocale() {
        return inputLocale.get();
    }

    /**
     * Set the output locale into a thread variable
     *
     * @param outputLocale The output locale to set
     */
    public void setOutputLocale(Locale outputLocale) {
        this.outputLocale.set(outputLocale);
    }

    /**
     * Get the output locale from a thread variable
     *
     * @return The output locale
     */
    public Locale getOutputLocale() {
        return outputLocale.get();
    }

    /**
     * Clear locales previously set
     */
    public void clear() {

        inputLocale.set(null);
        outputLocale.set(null);
    }

    /**
     *
     * @param inputLocale
     * @throws UnsupportedLanguageException
     */
    private void checkInputLocale(Locale inputLocale) throws UnsupportedLanguageException {

        Domain domain = domainDao.findByCode(messageDomain);
        if (!domain.getAvailableLanguageTags().contains(inputLocale.getLanguage()))
            throw new UnsupportedLanguageException(inputLocale.getLanguage(), messageDomain);
    }
}
