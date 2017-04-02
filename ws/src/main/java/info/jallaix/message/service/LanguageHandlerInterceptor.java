package info.jallaix.message.service;

import info.jallaix.message.dao.interceptor.ThreadLocaleHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * Intercept HTTP requests to define language ranges for the current thread.
 */
public class LanguageHandlerInterceptor extends HandlerInterceptorAdapter {

    /**
     * Locale data holder
     */
    private ThreadLocaleHolder threadLocaleHolder;


    /**
     * Constructor with thread locale holder.
     *
     * @param threadLocaleHolder
     */
    public LanguageHandlerInterceptor(ThreadLocaleHolder threadLocaleHolder) {
        this.threadLocaleHolder = threadLocaleHolder;
    }


    /**
     * Set language ranges for the current request.
     *
     * @param request  HTTP request
     * @param response HTTP response
     * @param handler  Target resource of the request
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // Clear any locale data previously linked to the current thread
        threadLocaleHolder.clear();

        // Set language ranges for the current request
        String acceptLanguage = request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        if (StringUtils.isNotBlank(acceptLanguage))
            threadLocaleHolder.setOutputLocales(Locale.LanguageRange.parse(acceptLanguage));

        return true;
    }
}
