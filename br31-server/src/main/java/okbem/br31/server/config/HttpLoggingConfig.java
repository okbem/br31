
package okbem.br31.server.config;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import okbem.br31.server.util.httplog.HttpLoggingFilter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class HttpLoggingConfig {

    @Bean
    public FilterRegistrationBean<HttpLoggingFilter> httpLoggingFilter() {
        FilterRegistrationBean<HttpLoggingFilter> filterRegistrationBean
            = new FilterRegistrationBean<>();

        filterRegistrationBean.setFilter(new HttpLoggingFilter());
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.setDispatcherTypes(
            EnumSet.allOf(DispatcherType.class)
        );

        return filterRegistrationBean;
    }

}

