package com.shop.config;

import com.shop.limit.LimitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

/**
 * Web配置：
 * 	 1) 添加参数解析器	——	token --> user对象
 * 	 2) 添加拦截器		——	限制单个用户秒杀请求的频率，防刷
 */
@Configuration
public class WebConfig  extends WebMvcConfigurerAdapter {
	
	@Autowired
	UserArgumentResolver userArgumentResolver;

	@Autowired
	LimitInterceptor limitInterceptor;
	
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(userArgumentResolver);
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) { registry.addInterceptor(limitInterceptor); }
}
