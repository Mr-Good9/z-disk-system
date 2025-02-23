package com.good.zdisksystem.entity.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 文件分享表
 * </p>
 *
 * @author chris
 * @since 2025-02-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("file_share")
@ApiModel(value="FileShare对象", description="文件分享表")
public class FileShare implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "文件ID")
    private Long fileId;

    @ApiModelProperty(value = "分享者ID")
    private Long userId;

    @ApiModelProperty(value = "分享码")
    private String shareCode;

    @ApiModelProperty(value = "访问密码")
    private String accessCode;

    @ApiModelProperty(value = "分享类型(0:私密分享 1:公开分享)")
    private Integer shareType;

    @ApiModelProperty(value = "有效期(天)")
    private Integer expireDays;

    @ApiModelProperty(value = "过期时间")
    private LocalDateTime expireTime;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "浏览次数")
    private Integer viewCount;

    @ApiModelProperty(value = "下载次数")
    private Integer downloadCount;

    @ApiModelProperty(value = "状态(0:正常 1:已过期 2:已取消)")
    private Integer status;

    @ApiModelProperty(value = "是否删除")
    private Integer isDeleted;


}
