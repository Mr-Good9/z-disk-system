package com.good.zdisksystem.entity.vo;

import lombok.Data;

import java.util.List;

/**
 * @author chris
 * @since 2025/1/15 15:07
 */
@Data
public class UserInfoVo {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String avatar;
    private String phone;
    private String role;
    private String createTime;
    private String updateTime;
    private String status;

    private List<String> roles;

    // 添加存储相关字段
    private StorageInfo storage;

    @Data
    public static class StorageInfo {
        private Long used;    // 已使用空间(bytes)
        private Long total;   // 总空间(bytes)
    }
}
