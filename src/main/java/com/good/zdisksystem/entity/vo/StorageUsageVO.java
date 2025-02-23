package com.good.zdisksystem.entity.vo;

import lombok.Data;

@Data
public class StorageUsageVO {
    private Long totalStorage;      // 总存储空间
    private Long usedStorage;       // 已使用空间
    private Long fileCount;         // 文件总数
    private Long sharedFileCount;   // 已分享文件数
    private Long deletedFileCount;  // 回收站文件数
    private Double usagePercent;    // 使用率
} 