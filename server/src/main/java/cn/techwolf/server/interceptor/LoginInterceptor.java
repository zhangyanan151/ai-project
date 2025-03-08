package cn.techwolf.server.interceptor;

import cn.techwolf.server.config.CookieConfig;
import cn.techwolf.server.model.User;
import cn.techwolf.server.service.UserLoginCacheService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.io.IOException;

@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private UserLoginCacheService userLoginCacheService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (CookieConfig.LOGIN_COOKIE_NAME.equals(cookie.getName())) {
                    String email = cookie.getValue();
                    User user = userLoginCacheService.getLoginStatus(email);
                    if (user != null) {
                        // 用户已登录
                        return true;
                    }
                    break;
                }
            }
        }
        
        try {
            // 用户未登录，返回401状态码和统一的错误响应格式
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"请先登录\",\"data\":null}");
        } catch (IOException e) {
            log.error("写入响应时发生错误", e);
            throw e;
        }
        return false;
    }
}