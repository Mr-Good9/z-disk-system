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
 * 分享接收记录表
 * </p>
 *
 * @author chris
 * @since 2025-02-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("share_receive")
@ApiModel(value="ShareReceive对象", description="分享接收记录表")
public class ShareReceive implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "分享ID")
    private Long shareId;

    @ApiModelProperty(value = "接收者ID")
    private Long receiverId;

    @ApiModelProperty(value = "接收时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "状态(0:未保存 1:已保存)")
    private Integer status;

    @ApiModelProperty(value = "是否删除")
    private Integer isDeleted;


}
