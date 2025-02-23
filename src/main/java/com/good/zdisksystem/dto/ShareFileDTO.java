package com.good.zdisksystem.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShareFileDTO {
    private Long id;          // 分享ID
    private Long fileId;      // 文件ID
    private String fileName;  // 文件名
    private String fileType;  // 文件类型
    private Long fileSize;    // 文件大小
    private String ownerName; // 分享者名称
    private Integer shareType;// 分享类型
    private Integer expireDays;// 有效期(天)
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime expireTime; // 过期时间
    private Integer viewCount;    // 浏览次数
    private Integer downloadCount;// 下载次数
    private String shareCode;     // 分享码
    private Integer status;       // 状态
}
