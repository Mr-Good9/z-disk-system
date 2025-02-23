package com.good.zdisksystem.entity.param;

import lombok.Data;

@Data
public class UpdateShareSettingsParam {
    private Integer expireDays;  // 过期天数，null表示永久有效

    private String accessCode;  // 访问密码，null表示无密码

    private Integer status;  // 0-已取消 1-正常

    private Integer shareType;  // 0:私密分享 1:公开分享
}
