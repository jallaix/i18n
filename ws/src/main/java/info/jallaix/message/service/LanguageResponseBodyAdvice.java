package info.jallaix.message.service;

import info.jallaix.message.config.DomainHolder;
import info.jallaix.message.dao.interceptor.ThreadLocaleHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Collection;
import java.util.Locale;

/**
 * Define the Content-Language header in the HTTP response.
 */
@ControllerAdvice(annotations = RepositoryRestController.class)
public class LanguageResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    /**
     * I18N domain holder
     */
    @Autowired
    private DomainHolder i18nDomainHolder;

    /**
     * Locale data holder
     */
    @Autowired
    private ThreadLocaleHolder threadLocaleHolder;


    /**
     * Support all controller method return type and selected {@code HttpMessageConverter} type.
     *
     * @param returnType    the return type
     * @param converterType the selected converter type
     * @return {@code true}
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    /**
     * Invoked after an {@code HttpMessageConverter} is selected and just before
     * its write method is invoked to set the Content-Language header in the HTTP response..
     *
     * @param body                  the body to be written
     * @param returnType            the return type of the controller method
     * @param selectedContentType   the content type selected through content negotiation
     * @param selectedConverterType the converter type selected to write to the response
     * @param request               the current request
     * @param response              the current response
     * @return the body that was passed in or a modified, possibly new instance
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {

        // Get supported language tags
        Collection<String> supportedLanguageTags = i18nDomainHolder.getDomain().getAvailableLanguageTags();

        // Find the output language tag
        String outputLanguageTag = threadLocaleHolder.getOutputLocales()
                .stream()
                .map(Locale.LanguageRange::getRange)                                                        // Get the language tag part
                .filter(tag -> supportedLanguageTags.contains(Locale.forLanguageTag(tag).getLanguage()))    // Filter for tags having a supported language
                .findFirst()                                                                                // Get the most relevant tag
                .orElse(i18nDomainHolder.getDomain().getDefaultLanguageTag());                              // Default language tag if none is found

        // Set the Content-Language header in the HTTP response
        response.getHeaders().set(HttpHeaders.CONTENT_LANGUAGE, Locale.forLanguageTag(outputLanguageTag).toLanguageTag());
        threadLocaleHolder.clear();

        return body;
    }
}
