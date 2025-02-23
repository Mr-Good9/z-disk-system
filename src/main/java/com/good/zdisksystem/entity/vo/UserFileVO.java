package com.good.zdisksystem.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(description = "用户文件视图对象")
public class UserFileVO {
    @ApiModelProperty("文件ID")
    private Long id;

    @ApiModelProperty("文件名")
    private String name;

    @ApiModelProperty("文件类型")
    private String type;

    @ApiModelProperty("文件大小")
    private Long size;

    @ApiModelProperty("是否是文件夹")
    private Integer isFolder;

    @ApiModelProperty("父文件夹ID")
    private Long parentId;

    @ApiModelProperty("文件路径")
    private String path;

    @ApiModelProperty("文件所有者名称")
    private String ownerName;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @ApiModelProperty("删除时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deleteTime;

    @ApiModelProperty("删除时间字符串")
    private String deleteTimeStr;

    @ApiModelProperty("是否已删除")
    private Integer isDeleted;
} 