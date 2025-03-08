package cn.techwolf.server.service;

import cn.techwolf.server.model.User;
import cn.techwolf.server.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private VerificationCodeService verificationCodeService;

    @Autowired
    private UserLoginCacheService userLoginCacheService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User login(String email, String password) {
        // 先检查缓存中是否存在登录状态
        User cachedUser = userLoginCacheService.getLoginStatus(email);
        if (cachedUser != null) {
            return cachedUser;
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                // 登录成功，缓存用户状态
                userLoginCacheService.cacheLoginStatus(email, user);
                return user;
            } else {
                log.warn("Password incorrect for user: {}", email);
                return null;
            }
        }
        return null;
    }

    public String register(String email, String password, String verificationCode) {
        int count = userRepository.existsByEmail(email);
        log.warn("注册邮箱: count={}", count);
        if (count > 0) {
            log.warn("注册失败，邮箱已存在: email={}", email);
            return "邮箱已存在";
        }

        if (!verificationCodeService.verifyCode(email, verificationCode)) {
            log.warn("注册失败，验证码验证失败: email={}", email);
            return "验证码验证失败";
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        
        userRepository.save(user);
        log.info("用户注册成功: email={}", email);
        return null;
    }

    public String sendVerificationCode(String email) {
        String errorMsg = verificationCodeService.canSendCode(email);
        if (errorMsg != null) {
            return errorMsg;
        }
        String code = generateVerificationCode();
        verificationCodeService.storeCode(email, code);
        emailService.sendVerificationCode(email, code);
        return null;
    }

    private String generateVerificationCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }
}