package com.good.zdisksystem.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperationLogVO {

    private Long id;

    private Long userId;

    private String username; // 关联用户名

    private String module;

    private String action;

    private String detail;

    private LocalDateTime createTime;
}
