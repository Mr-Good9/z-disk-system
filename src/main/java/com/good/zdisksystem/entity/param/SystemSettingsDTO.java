package com.good.zdisksystem.entity.param;

import lombok.Data;

@Data
public class SystemSettingsDTO {
    private Long defaultStorageSize;    // 默认存储空间大小(B)
    private Long maxFileSize;           // 单文件大小限制(B)
    private Boolean maintenanceMode;    // 维护模式
    private String maintenanceMessage;  // 维护说明
    private Integer minPasswordLength;  // 密码最小长度
    private Integer maxLoginAttempts;   // 最大登录尝试次数
}
