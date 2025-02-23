package com.good.zdisksystem.entity.model;

import com.baomidou.mybatisplus.annotation.*;
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

    // 父文件夹ID，根目录为0
    @TableField("parent_id")
    private Long parentId = 0L;

    @TableField(value = "delete_time")
    private LocalDateTime deleteTime;

    // 创建时间
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 更新时间
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 是否删除(0-否 1-是)
    @TableField("is_deleted")
    private Integer isDeleted = 0;
}
