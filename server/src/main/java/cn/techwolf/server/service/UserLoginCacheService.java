package cn.techwolf.server.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import cn.techwolf.server.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserLoginCacheService {

    private final Cache<String, User> loginCache;

    public UserLoginCacheService() {
        // 创建缓存，设置过期时间为30分钟
        loginCache = CacheBuilder.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .maximumSize(10000)
                .build();
    }

    /**
     * 缓存用户登录状态
     *
     * @param email 用户邮箱
     * @param user 用户信息
     */
    public void cacheLoginStatus(String email, User user) {
        loginCache.put(email, user);
        log.info("用户登录状态已缓存: email={}", email);
    }

    /**
     * 验证用户是否已登录
     *
     * @param email 用户邮箱
     * @return 用户信息，如果未登录则返回null
     */
    public User getLoginStatus(String email) {
        return loginCache.getIfPresent(email);
    }

    /**
     * 清除用户登录状态
     *
     * @param email 用户邮箱
     */
    public void clearLoginStatus(String email) {
        loginCache.invalidate(email);
        log.info("用户登录状态已清除: email={}", email);
    }

    /**
     * 清除所有登录状态
     */
    public void clearAllLoginStatus() {
        loginCache.invalidateAll();
        log.info("所有用户登录状态已清除");
    }
}