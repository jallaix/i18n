package info.jallaix.message.dao.interceptor;

import info.jallaix.message.config.DomainHolder;
import lombok.NoArgsConstructor;

import java.util.Locale;

/**
 * <p>
 * This bean holds locale data linked to the current thread.
 * </p>
 * <p>
 * Locale data are :
 * <ul>
 * <li>Input locale : used by {@link DomainDaoInterceptor} when saving a localized document</li>
 * <li>Output locale : used by {@link DomainDaoInterceptor} when getting a localized document</li>
 * </ul>
 * </p>
 */
@NoArgsConstructor
public class ThreadLocaleHolder {

    /**
     * The message domain
     */
    private DomainHolder i18nDomainHolder;


    /**
     *
     * @param i18nDomainHolder
     */
    public ThreadLocaleHolder(DomainHolder i18nDomainHolder) {
        this.i18nDomainHolder = i18nDomainHolder;
    }

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
        return inputLocale.get() == null ? getDefaultLocale() : inputLocale.get();
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
        return outputLocale.get() == null ? getDefaultLocale() : outputLocale.get();
    }

    /**
     * Clear locales previously set
     */
    public void clear() {

        inputLocale.set(null);
        outputLocale.set(null);
    }

    /**
     * Get the domain's default locale
     *
     * @return The domain's default locale
     */
    private Locale getDefaultLocale() {
        return Locale.forLanguageTag(i18nDomainHolder.getDomain().getDefaultLanguageTag());
    }
}
