package com.taipeigo.frontend.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FrontendFilterConfig {

    @Bean
    public FilterRegistrationBean<FrontendLoginFilter> frontendLoginFilter() {

        FilterRegistrationBean<FrontendLoginFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new FrontendLoginFilter());

        // 需要攔截的路徑放這
        registrationBean.addUrlPatterns(
                "/customer/*",
                "/favorite/*",
                "/frontend/customer/*", 
                "/frontend/cart/*",
                "/CustomerService/*",
                "/CustomerService"
        );

        registrationBean.setOrder(2);

        return registrationBean;
    }
}