package com.good.zdisksystem.model;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class UserStatus {
    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("timestamp")
    private Long timestamp;

    // 添加无参构造函数
    public UserStatus() {}

    // 添加全参构造函数
    public UserStatus(Long userId, String status, Long timestamp) {
        this.userId = userId;
        this.status = status;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "UserStatus{" +
                "userId=" + userId +
                ", status='" + status + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
} 