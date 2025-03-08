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

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User login(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return user;
            } else {
                log.warn("Password incorrect for user: {}", email);
                return null;
            }
        }
        return null;
    }

    public boolean register(String email, String password, String verificationCode) {
        if (userRepository.existsByEmail(email)) {
            log.warn("注册失败，邮箱已存在: email={}", email);
            return false;
        }

        if (!verificationCodeService.verifyCode(email, verificationCode)) {
            log.warn("注册失败，验证码验证失败: email={}", email);
            return false;
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        
        userRepository.save(user);
        log.info("用户注册成功: email={}", email);
        return true;
    }

    public void sendVerificationCode(String email) {
        String code = generateVerificationCode();
        verificationCodeService.storeCode(email, code);
        emailService.sendVerificationCode(email, code);
    }

    private String generateVerificationCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }
}