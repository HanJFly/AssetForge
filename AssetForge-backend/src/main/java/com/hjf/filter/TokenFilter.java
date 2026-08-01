package com.hjf.filter;


import com.hjf.context.LoginUserIRoleUtile;
import com.hjf.context.LoginUserInfoUtile;
import com.hjf.param.LoginUserContext;
import com.hjf.util.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.io.IOException;


@Slf4j
@WebFilter(urlPatterns = "/*")
public class TokenFilter implements Filter {

    @Autowired
    private JwtUtils jwtUtils;


    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        //获取请求url
        String url = request.getRequestURL().toString();

        //判断请求url中是否包含login，如果包含，说明是登录操作，放行
        if (url.contains("login")) {
            chain.doFilter(request, response);
            return;
        }

        //获取请求头中的令牌token
        String jwt = request.getHeader("token");

        //4. 判断令牌是否存在，如果不存在，返回错误结果（未登录）。
        if (!StringUtils.hasLength(jwt)) {
            log.warn("JWT Token 为空，请求路径: {}", request.getRequestURI());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未授权，请重新登录\"}");
            return;
        }

        // 剥离 Bearer 前缀
        if (jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        }

        //5. 解析token，如果解析失败，返回错误结果（未登录）。
        try {
            Claims claims = jwtUtils.parseJWT(jwt);
            Long userId = claims.get("userId", Long.class);

            String username = claims.get("username", String.class);

            String real_name = claims.get("real_name", String.class);

            // 将用户信息存储到ThreadLocal中
            LoginUserContext loginUserContext = new LoginUserContext();
            loginUserContext.setId(userId);
            loginUserContext.setUsername(username);
            loginUserContext.setRealName(real_name);
            LoginUserInfoUtile.set(loginUserContext);




        } catch (Exception e) {
            log.error("JWT Token 解析失败，请求路径", e);

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        //6. 放行。

        chain.doFilter(request , response);

    }
}
