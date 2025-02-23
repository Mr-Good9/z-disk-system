package com.good.zdisksystem.entity.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String role;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime lastLoginTime;
    private String avatar;
    private Long storageSize;    // 存储空间大小
    private Long usedStorage;    // 已使用空间
}