package com.taipeigo.backend.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BackendFilterConfig {

	@Bean
	public FilterRegistrationBean<BackendLoginFilter> loginFilter(){
	
	        FilterRegistrationBean<BackendLoginFilter> registrationBean = new FilterRegistrationBean<>();
	        registrationBean.setFilter(new BackendLoginFilter());
	        
	        //要測試強登先開這行
	        //registrationBean.addUrlPatterns("/backend/*"); 
	        registrationBean.setOrder(1);
			
			  return registrationBean;
	}

	
}

