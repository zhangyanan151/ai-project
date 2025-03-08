package cn.techwolf.server.controller;

import cn.techwolf.server.common.ApiResponse;
import cn.techwolf.server.model.User;
import cn.techwolf.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ApiResponse<User> login(@RequestParam String email, @RequestParam String password) {
        User user = userService.login(email, password);
        if (user != null) {
            return ApiResponse.success(user);
        }
        return ApiResponse.error("邮箱或密码错误");
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
        boolean success = userService.register(email, password, verificationCode);
        if (success) {
            return ApiResponse.success(null);
        }
        return ApiResponse.error("注册失败，邮箱已存在");
    }
}