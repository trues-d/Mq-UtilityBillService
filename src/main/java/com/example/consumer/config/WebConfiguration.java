package com.example.consumer.config;

import com.example.consumer.filter.AuthGlobalFilter;
import com.example.consumer.intercepter.AuthInterceptor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {
    @Resource
    private AuthInterceptor authInterceptor;

    @Bean
    public FilterRegistrationBean<Filter> webVisitFilterConfigRegistration() {
        //匹配拦截 URL
        String urlPatterns = "/utilityBill/*";
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();

        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.setFilter(new AuthGlobalFilter());
        registration.addUrlPatterns(urlPatterns);
        //设置名称
        registration.setName("webVisitFilter");
        //设置过滤器链执行顺序
        registration.setOrder(3);
        return registration;
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/utilityBill/service/**")
                .excludePathPatterns("/utilityBill/signUp/**");
    }
}

