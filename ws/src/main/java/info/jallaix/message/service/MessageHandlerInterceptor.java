package info.jallaix.message.service;

import info.jallaix.message.dao.interceptor.ThreadLocaleHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * Created by Julien on 27/01/2017.
 */
public class MessageHandlerInterceptor extends HandlerInterceptorAdapter {

    /**
     * Locale data holder
     */
    private ThreadLocaleHolder threadLocaleHolder;


    public MessageHandlerInterceptor(ThreadLocaleHolder threadLocaleHolder) {
        this.threadLocaleHolder = threadLocaleHolder;
    }

    /**
     * This implementation always returns {@code true}.
     *
     * @param request
     * @param response
     * @param handler
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String acceptLanguage = request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        if (StringUtils.isNotBlank(acceptLanguage)) {
            threadLocaleHolder.setOutputLocales(Locale.LanguageRange.parse(acceptLanguage));
        }

        return super.preHandle(request, response, handler);
    }

    /**
     * This implementation is empty.
     *
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        super.postHandle(request, response, handler, modelAndView);
    }
}
