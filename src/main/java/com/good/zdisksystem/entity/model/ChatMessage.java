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
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * <p>
 * 聊天消息表
 * </p>
 *
 * @author chris
 * @since 2025-02-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("chat_message")
@ApiModel(value="ChatMessage对象", description="聊天消息表")
public class ChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "发送者用户ID")
    private Long fromUserId;

    @ApiModelProperty(value = "接收者用户ID")
    private Long toUserId;

    @ApiModelProperty(value = "消息内容")
    private String content;

    @ApiModelProperty(value = "消息类型：1文本、2图片、3文件")
    private Integer type;

    @ApiModelProperty(value = "状态：0未读、1已读")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String senderNickname;
    @TableField(exist = false)
    private String senderUsername;
    @TableField(exist = false)
    private String senderAvatar;
    @TableField(exist = false)
    private String receiverNickname;
    @TableField(exist = false)
    private String receiverUsername;
    @TableField(exist = false)
    private String receiverAvatar;
}
