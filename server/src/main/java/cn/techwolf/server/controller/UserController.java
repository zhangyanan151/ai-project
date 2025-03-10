package cn.techwolf.server.controller;

import cn.techwolf.server.common.ApiResponse;
import cn.techwolf.server.config.CookieConfig;
import cn.techwolf.server.model.User;
import cn.techwolf.server.service.UserLoginCacheService;
import cn.techwolf.server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CookieConfig cookieConfig;
    @Autowired
    private UserLoginCacheService userLoginCacheService;

    @PostMapping("/login")
    public ApiResponse<User> login(@RequestParam String email, @RequestParam String password) {
        User user = userService.login(email, password);
        if (user != null) {
            return ApiResponse.success(user);
        }
        return ApiResponse.error("邮箱或密码错误");
    }

    @PostMapping("/register/code")
    public ApiResponse<Boolean> sendVerificationCode(@RequestParam String email) {
        String errorMsg = userService.sendVerificationCode(email);
        if (errorMsg == null) {
            return ApiResponse.success(true);
        }
        return ApiResponse.error(errorMsg);
    }

    @PostMapping("/register")
    public ApiResponse<Boolean> register(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String verificationCode) {
        String errorMsg = userService.register(email, password, verificationCode);
        if (errorMsg == null) {
            return ApiResponse.success(true);
        }
        return ApiResponse.error(errorMsg);
    }

    @GetMapping("/info")
    public ApiResponse<String> getCurrentUser(@RequestParam String email) {
        try {
            User user = userLoginCacheService.getLoginStatus(email);
            if (user != null) {
                return ApiResponse.success(user.getEmail());
            }
            return ApiResponse.error("用户不存在");
        } catch (Exception e) {
            log.error("获取当前用户信息失败: email={}", email, e);
            return ApiResponse.error("获取用户信息失败");
        }
    }

}