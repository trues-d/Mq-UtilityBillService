package com.example.consumer.intercepter;

import com.example.consumer.exception.UnauthorizedException;
import com.example.consumer.utils.JwtTool;
import com.example.consumer.utils.UserContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private static final String authorization = "authorization";
    @Resource
    public JwtTool jwtTool;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws UnauthorizedException {
        String token = request.getHeader(authorization);
        String userId = jwtTool.parseToken(token);
        UserContext.setUser(userId);
        return Boolean.TRUE;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)  {
        UserContext.removeUser();
    }
}
