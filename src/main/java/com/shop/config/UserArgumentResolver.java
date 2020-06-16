package com.shop.config;

import com.shop.entity.User;
import com.shop.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * user方法参数解析器：
 * 	  用于将cookie或url参数中的token转为user对象
 */
@Service
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

	@Autowired
	UserService userService;
	
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.getParameterType() == User.class;
	}

	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

		HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
		HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
		
		String paramToken = request.getParameter(UserService.COOKIE_TOKEN);
		String cookieToken = getCookieToken(request);
		if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
			return null;
		}
		return userService.getByToken(response, StringUtils.isEmpty(cookieToken)?paramToken:cookieToken);
	}

	private String getCookieToken(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if(cookies == null || cookies.length == 0) return null;
		for(Cookie cookie : cookies) {
			if(UserService.COOKIE_TOKEN.equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}
}
