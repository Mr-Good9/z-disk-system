package com.good.zdisksystem.common.utils;

import com.good.zdisksystem.entity.model.User;
import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * @author chris
 * @since 2025/1/13 11:34
 */
@Component
@Data
public class RequestUser {
    private static final ThreadLocal<User> userHolder = new ThreadLocal<>();

    public static void setUser(User user) {
        userHolder.set(user);
    }

    public static User getUser() {
        return userHolder.get();
    }

    public static void remove() {
        userHolder.remove();
    }
}
