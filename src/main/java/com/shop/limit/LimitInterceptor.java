package com.shop.limit;

import com.alibaba.fastjson.JSON;
import com.shop.entity.User;
import com.shop.message.ErrorMsg;
import com.shop.message.ResultMsg;
import com.shop.redis.RedisService;
import com.shop.redis.prefix.LimitPrefix;
import com.shop.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * 通过拦截器，拦截每个handler，如果有limit注解，则需要在redis中查询用户在当前时间周期请求量是否已到达限制
 * 如果是，返回SECKILL_LIMIT错误提示。
 */
@Component
public class LimitInterceptor extends HandlerInterceptorAdapter {
	
	@Autowired
	UserService userService;
	
	@Autowired
	RedisService redisService;
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
		if(handler instanceof HandlerMethod) {
			User user = getUser(request, response);
			HandlerMethod handlerMethod = (HandlerMethod) handler;
			Limit limit = handlerMethod.getMethodAnnotation(Limit.class);
			if(limit == null) {    // 没有limit注解直接返回true
				return true;
			}
			int seconds = limit.seconds();
			int maxCount = limit.maxCount();
			boolean loginCheck = limit.loginCheck();
			String key = request.getRequestURI();
			if(loginCheck) {      // 需要登录检查，进行登录检查，防止非法用户请求
				if(user == null) {
					render(response, ErrorMsg.SESSION_ERROR);
					return false;
				}
				key += "_" + user.getId();
			}
			LimitPrefix limitPrefix = LimitPrefix.expireLimit(seconds);
			String count = redisService.get(limitPrefix, key);
	    	if(count  == null) {
	    		 redisService.set(limitPrefix, key, "1");
	    	}else if(Integer.parseInt(count) < maxCount) {
	    		 redisService.incr(limitPrefix, key);
	    	}else {
	    		render(response, ErrorMsg.SECKILL_LIMIT);
	    		return false;
	    	}
		}
		return true;
	}

	private void render(HttpServletResponse response, ErrorMsg errorMsg)throws IOException {
		response.setContentType("application/json;charset=UTF-8");
		OutputStream out = response.getOutputStream();
		String str  = JSON.toJSONString(ResultMsg.error(errorMsg));
		out.write(str.getBytes(StandardCharsets.UTF_8));
		out.flush();
		out.close();
	}

	private User getUser(HttpServletRequest request, HttpServletResponse response) {
		String paramToken = request.getParameter(UserService.COOKIE_TOKEN);
		String cookieToken = getCookieValue(request);
		if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
			return null;
		}
		String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
		return userService.getByToken(response, token);
	}
	
	private String getCookieValue(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if(cookies == null || cookies.length <= 0){
			return null;
		}
		for(Cookie cookie : cookies) {
			if(cookie.getName().equals(UserService.COOKIE_TOKEN)) {
				return cookie.getValue();
			}
		}
		return null;
	}
}
