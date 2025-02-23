package com.good.zdisksystem.entity.vo;

import lombok.Data;

@Data
public class FileStatisticsVO {
    private Long totalFiles;      // 总文件数
    private Long totalSize;       // 总容量
    private Long sharedFiles;     // 共享文件数
    private Long deletedFiles;    // 回收站文件数
    private Double sharedRatio;   // 共享文件占比
    private Double deletedRatio;  // 回收站文件占比
    private Double storageUsage;  // 存储使用率
} 