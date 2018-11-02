package net.yangwenxin.seckill.config;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import net.yangwenxin.seckill.access.UserContext;
import net.yangwenxin.seckill.domain.SeckillUser;
import net.yangwenxin.seckill.service.SeckillUserService;

@Service
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

	@Autowired
	private SeckillUserService seckillUserService;
	
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		Class<?> clazz = parameter.getParameterType();
		return clazz == SeckillUser.class;
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
//		HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
//		HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
//		String paramToken = request.getParameter(SeckillUserService.COOKIE_NAME_TOKEN);
//		String cookieToken = getCookieValue(request, SeckillUserService.COOKIE_NAME_TOKEN);
//		if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
//			return null;
//		}
//		String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;
//		return seckillUserService.getByToken(response, token);
		return UserContext.getUser();
	}

//	private String getCookieValue(HttpServletRequest request, String cookieName) {
//		Cookie[] cookies = request.getCookies();
//		if (cookies == null || cookies.length <= 0) {
//			return null;
//		}
//		for (Cookie cookie : cookies) {
//			if (cookie.getName().equals(cookieName)) {
//				return cookie.getValue();
//			}
//		}
//		return null;
//	}

}
