package cn.techwolf.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;

@Configuration
public class CookieConfig {

    @Value("${cookie.max-age:1800}") // 默认30分钟
    private int cookieMaxAge;

    @Value("${cookie.secure:true}")
    private boolean secure;

    @Value("${cookie.domain:}")
    private String domain;

    @Value("${cookie.path:/}")
    private String path;

    public static final String LOGIN_COOKIE_NAME = "login_token";

    /**
     * 创建登录Cookie
     *
     * @param token 登录令牌
     * @return ResponseCookie对象
     */
    public ResponseCookie createLoginCookie(String token) {
        return ResponseCookie.from(LOGIN_COOKIE_NAME, token)
                .maxAge(cookieMaxAge)
                .httpOnly(true)
                .secure(secure)
                .path(path)
                .domain(domain.isEmpty() ? null : domain)
                .sameSite("Lax")
                .build();
    }

    /**
     * 创建用于清除登录Cookie的Cookie
     *
     * @return ResponseCookie对象
     */
    public ResponseCookie createLogoutCookie() {
        return ResponseCookie.from(LOGIN_COOKIE_NAME, "")
                .maxAge(0)
                .httpOnly(true)
                .secure(secure)
                .path(path)
                .domain(domain.isEmpty() ? null : domain)
                .sameSite("Lax")
                .build();
    }
}