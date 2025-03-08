package cn.techwolf.server.controller;

import cn.techwolf.server.common.ApiResponse;
import cn.techwolf.server.config.CookieConfig;
import cn.techwolf.server.model.User;
import cn.techwolf.server.service.UserService;
import cn.techwolf.server.service.UserLoginCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserLoginCacheService userLoginCacheService;

    @Autowired
    private CookieConfig cookieConfig;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<User>> login(@RequestParam String email, @RequestParam String password) {
        User user = userService.login(email, password);
        if (user != null) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookieConfig.createLoginCookie(email).toString())
                    .body(ApiResponse.success(user));
        }
        return ResponseEntity.ok(ApiResponse.error("邮箱或密码错误"));
    }

    @PostMapping("/register/code")
    public ApiResponse<Void> sendVerificationCode(@RequestParam String email) {
        userService.sendVerificationCode(email);
        return ApiResponse.success(null);
    }

    @PostMapping("/register")
    public ApiResponse<Void> register(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String verificationCode) {
        String errorMsg = userService.register(email, password, verificationCode);
        if (errorMsg == null) {
            return ApiResponse.success(null);
        }
        return ApiResponse.error(errorMsg);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@CookieValue(name = CookieConfig.LOGIN_COOKIE_NAME, required = false) String email) {
        if (email != null) {
            userLoginCacheService.clearLoginStatus(email);
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookieConfig.createLogoutCookie().toString())
                    .body(ApiResponse.success(null));
        }
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}