package info.jallaix.message.config;

import info.jallaix.message.dao.interceptor.ThreadLocaleHolder;
import info.jallaix.message.service.LanguageHandlerInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.MappedInterceptor;

/**
 * Created by Julien on 22/01/2017.
 */
@Configuration
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {

    /**
     * Locale data holder
     */
    @Autowired
    private ThreadLocaleHolder threadLocaleHolder;


    /**
     * Define a mapped interceptor with a language handler interceptor.&'
     *
     * @return A mapped interceptor with a language handler interceptor
     */
    @Bean
    public MappedInterceptor messageHandlerInterceptor() {
        return new MappedInterceptor(new String[]{"/**"}, new LanguageHandlerInterceptor(threadLocaleHolder));
    }
}
