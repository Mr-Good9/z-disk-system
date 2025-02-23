package com.good.zdisksystem.cache;

import com.good.zdisksystem.constant.AuthCacheConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author chris
 * @since 2025/1/15 15:20
 */
@Component
public class AuthUserCache {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 设置登录token
     *
     * @param key   {手机号}
     * @param value
     */
    public void setAccessToken(String key, String value) {
        redisTemplate.opsForValue().set(String.format(AuthCacheConstant.AUTH_USER_TOKEN_KEY, key), value);
    }

    /**
     * 获取登录token
     *
     * @param key
     */
    public void getAccessToken(String key) {
        redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除登录token
     *
     * @param key
     */
    public void delAccessToken(String key) {
        redisTemplate.delete(String.format(AuthCacheConstant.AUTH_USER_TOKEN_KEY, key));
    }

    /**
     * 设置验证码
     * ttl:5min
     *
     * @param key   {验证码uuid}
     * @param value
     */
    public void setCaptcha(String key, String value) {
        redisTemplate.opsForValue().set(String.format(AuthCacheConstant.AUTH_USER_CAPTCHA, key), value, 5, TimeUnit.MINUTES);
    }

    /**
     * 删除验证码
     *
     * @param key
     */
    public void delCaptcha(String key) {
        redisTemplate.delete(String.format(AuthCacheConstant.AUTH_USER_CAPTCHA, key));
    }

    /**
     * 获取验证码
     *
     * @param key
     * @return
     */
    public String getCaptcha(String key) {
        return redisTemplate.opsForValue().get(String.format(AuthCacheConstant.AUTH_USER_CAPTCHA, key));
    }

}
