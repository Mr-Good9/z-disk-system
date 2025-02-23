package com.good.zdisksystem.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(description = "分享文件视图对象")
public class ShareFileVO {
    @ApiModelProperty("分享ID")
    private Long id;          // 分享ID

    @ApiModelProperty("文件ID")
    private Long fileId;      // 文件ID

    @ApiModelProperty("分享码")
    private String shareCode;  // 分享码

    @ApiModelProperty("文件名")
    private String fileName;  // 文件名

    @ApiModelProperty("文件类型")
    private String fileType;  // 文件类型

    @ApiModelProperty("文件大小")
    private Long fileSize;    // 文件大小

    @ApiModelProperty("分享者/接收者名称")
    private String ownerName; // 分享者名称

    @ApiModelProperty("分享类型：0-私密分享，1-公开分享")
    private Integer shareType;// 分享类型

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime; // 创建时间

    @ApiModelProperty("创建时间字符串")
    private String createTimeStr;

    @ApiModelProperty("过期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime; // 过期时间

    @ApiModelProperty("过期时间字符串")
    private String expireTimeStr;

    @ApiModelProperty("浏览次数")
    private Integer viewCount;    // 浏览次数

    @ApiModelProperty("下载次数")
    private Integer downloadCount;// 下载次数

    @ApiModelProperty("状态：0-正常，1-已过期，2-已取消")
    private Integer status;       // 状态

    @ApiModelProperty("是否需要密码")
    private Boolean needPassword;
} 