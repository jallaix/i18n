package info.jallaix.message.config;

import info.jallaix.message.service.MessageHandlerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.MappedInterceptor;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * Created by Julien on 22/01/2017.
 */
@Configuration
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {

    @Bean
    public MappedInterceptor messageHandlerInterceptor() {
        return new MappedInterceptor(new String[]{"/**"}, new MessageHandlerInterceptor());
    }

    /**
     * Resolver for locale
     *
     * @return
     */
    @Bean
    public LocaleResolver localeResolver() {
        return new AcceptHeaderLocaleResolver() {

            @Override
            public Locale resolveLocale(HttpServletRequest request) {

                // In case no Accept-Language tag is provided by the client, locale matches the domain's default language
                /*if (StringUtils.isBlank(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE)))
                    return Locale.getDefault();

                List<Locale.LanguageRange> list = Locale.LanguageRange.parse(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE));

                return Locale.lookup(list, ApplicationConstants.LOCALES);*/
                return null;
            }
        };
    }
}