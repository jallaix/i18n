package info.jallaix.message.dao.interceptor;

import info.jallaix.message.config.DomainHolder;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
     * Locale used when storing data
     */
    private ThreadLocal<Locale> inputLocale = new ThreadLocal<>();

    /**
     * Locale used when getting data
     */
    private ThreadLocal<List<Locale.LanguageRange>> outputLocales = new ThreadLocal<>();


    /**
     * Constructor with I18N domain holder
     *
     * @param i18nDomainHolder The I18N domain holder
     */
    public ThreadLocaleHolder(DomainHolder i18nDomainHolder) {
        this.i18nDomainHolder = i18nDomainHolder;
    }


    /**
     * Set the input locale into a thread variable
     *
     * @param inputLocale The input locale to set
     */
    public void setInputLocale(Locale inputLocale) {
        this.inputLocale.set(inputLocale);
    }

    /**
     * Get the input locale from a thread variable.
     *
     * @return The input locale
     */
    public Locale getInputLocale() {
        return inputLocale.get() == null ? getDefaultLocale() : inputLocale.get();
    }

    /**
     * Set the output locale into a thread variable.
     *
     * @param outputLocale The output locale to set
     */
    public void setOutputLocale(Locale outputLocale) {

        List<Locale.LanguageRange> languageRanges = this.outputLocales.get();

        // Create language ranges if missing, else clear the list
        if (languageRanges == null) {
            languageRanges = new ArrayList<>(1);
            this.outputLocales.set(languageRanges);
        } else
            languageRanges.clear();

        // Add the single language range
        languageRanges.add(new Locale.LanguageRange(outputLocale.toLanguageTag()));
    }

    /**
     * Set the output locales into a thread variable.
     *
     * @param outputLocales The output locales to set
     */
    public void setOutputLocales(List<Locale.LanguageRange> outputLocales) {

        List<Locale.LanguageRange> languageRanges = this.outputLocales.get();

        // Create language ranges if missing, else clear the list
        if (languageRanges == null) {
            languageRanges = new ArrayList<>(outputLocales.size());
            this.outputLocales.set(languageRanges);
        } else
            languageRanges.clear();

        // Add the language ranges
        languageRanges.addAll(outputLocales);
    }

    /**
     * Get the output locales from a thread variable.
     *
     * @return The output locales
     */
    public List<Locale.LanguageRange> getOutputLocales() {

        List<Locale.LanguageRange> languageRanges = this.outputLocales.get();

        if (CollectionUtils.isEmpty(languageRanges))
            return new ArrayList<>(
                    Collections.singletonList(
                            new Locale.LanguageRange(
                                    getDefaultLocale().toLanguageTag())));
        else
            return languageRanges;
    }

    /**
     * Clear locales previously set
     */
    public void clear() {

        inputLocale.set(null);
        outputLocales.set(null);
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
