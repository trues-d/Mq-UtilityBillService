package com.example.consumer.filter;


import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.example.consumer.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

@Slf4j
public class AuthGlobalFilter implements Filter {
    private static final String authorization = "authorization";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws UnauthorizedException, IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String token = req.getHeader(authorization);
        String url = req.getRequestURI();
        if (url.startsWith("/utilityBill/signUp/")) {
            filterChain.doFilter(request, response);
            return;
        }
        if (StrUtil.isNotBlank(token)) {
            filterChain.doFilter(request, response);
        } else {
//            throw new UnauthorizedException("没有登录 请先登录");
            resp.setStatus(401);
            resp.setContentType("application/json;charset=utf-8");
            PrintWriter writer = resp.getWriter();
            HashMap<String, String> resultMap = new HashMap<>();
            resultMap.put("code", "100399");
            resultMap.put("msg", "token 缺失");
            String jsonString = JSON.toJSONString(resultMap);
            writer.write(jsonString);
        }
    }
}





