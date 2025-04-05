package com.good.zdisksystem.utils;

import com.good.zdisksystem.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chris
 * @since 2025/4/5 17:46
 */
@Component
public class WSJwtUtils {

    @Autowired
    private UserService userService;

    // 解析token成用户信息
    public com.good.zdisksystem.entity.model.User parseToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            String username = claims.getSubject();
            // 查询获取用户信息
            return userService.findByUsername(username);
        } catch (Exception e) {
            return null;
        }
    }

}
