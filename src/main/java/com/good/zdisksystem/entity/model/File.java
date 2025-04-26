package com.good.zdisksystem.entity.model;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("file")
public class File {
    // 文件ID
    @TableId(type = IdType.AUTO)
    private Long id;

    // 所属用户ID
    @TableField("user_id")
    private Long userId;

    // 文件名
    private String name;

    // 文件存储路径
    private String path;

    // 文件类型
    private String type;

    // 文件大小(字节)
    private Long size = 0L;

    // 是否是文件夹(0-否 1-是)
    @TableField("is_folder")
    private Integer isFolder = 0;

    // 是否共享(0-否 1-是)
    @TableField("is_shared")
    private Integer isShared = 0;

    @ApiModelProperty(value = "是否视频文件")
    private Integer isVideo;

    @ApiModelProperty(value = "视频时长(秒)")
    @TableField(exist = false)
    private Float videoDuration;

    @TableField(exist = false)
    @ApiModelProperty(value = "视频宽度")
    private Integer videoWidth;

    @TableField(exist = false)
    @ApiModelProperty(value = "视频高度")
    private Integer videoHeight;

    @TableField(exist = false)
    @ApiModelProperty(value = "视频缩略图路径")
    private String videoThumbnail;

    // 父文件夹ID，根目录为0
    @TableField("parent_id")
    private Long parentId = 0L;

    @TableField(value = "delete_time")
    private LocalDateTime deleteTime;

    // 创建时间
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    // 更新时间
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // 是否删除(0-否 1-是)
    @TableField("is_deleted")
    private Integer isDeleted = 0;
}
