package com.good.zdisksystem.entity.vo;

import lombok.Data;

@Data
public class SystemStatisticsVO {
    private Long totalUsers;        // 总用户数
    private Long activeUsers;       // 活跃用户数
    private Long totalFiles;        // 总文件数
    private Long totalStorage;      // 总存储空间
    private Long usedStorage;       // 已用存储空间
    private Integer onlineUsers;    // 在线用户数
    private Double cpuUsage;        // CPU使用率
    private Double memoryUsage;     // 内存使用率
    private String systemUptime;    // 系统运行时间
}
