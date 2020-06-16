package com.shop.limit;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 限流防刷注解
 */
@Retention(RUNTIME) // 注解要保留到运行时，通过拦截器检查是否需要进行限制
@Target(METHOD)     // 注解用于方法上
public @interface Limit {
	int seconds();		 // 指定时间内
	int maxCount();		 // 最大请求次数
	boolean loginCheck() default true;   // 登录检查——默认需要检查
}

