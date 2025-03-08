package cn.techwolf.server.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class VerificationCodeService {

    private final Cache<String, String> codeCache;
    private final Cache<String, LocalDateTime> sendTimeCache;

    public VerificationCodeService() {
        codeCache = CacheBuilder.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();
        sendTimeCache = CacheBuilder.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();
    }

    public String canSendCode(String email) {
        LocalDateTime lastSendTime = sendTimeCache.getIfPresent(email);
        if (lastSendTime != null) {
            log.warn("邮箱{}在5分钟内已经发送过验证码", email);
            return "请等待5分钟后再次发送验证码";
        }
        return null;
    }

    public void storeCode(String email, String code) {
        log.info("Storing verification code for email: {}", email);
        codeCache.put(email, code);
        sendTimeCache.put(email, LocalDateTime.now());
    }

    public boolean verifyCode(String email, String code) {
        String storedCode = codeCache.getIfPresent(email);
        if (storedCode == null) {
            log.warn("No verification code found for email: {}", email);
            return false;
        }

        boolean isValid = storedCode.equals(code);
        if (!isValid) {
            log.warn("Invalid verification code for email: {}", email);
        }
        return isValid;
    }
}