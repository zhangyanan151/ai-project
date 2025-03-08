package cn.techwolf.server.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class VerificationCodeService {

    private final Cache<String, String> codeCache;

    public VerificationCodeService() {
        codeCache = CacheBuilder.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();
    }

    public void storeCode(String email, String code) {
        log.info("Storing verification code for email: {}", email);
        codeCache.put(email, code);
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