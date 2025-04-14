// src/main/java/com/good/zdisksystem/model/entity/MessageLike.java
package com.good.zdisksystem.entity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("message_like")
@ApiModel(value="message_like", description="点赞记录")
public class MessageLike {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long messageId;

    private Long userId;

    private LocalDateTime createTime = LocalDateTime.now();
}
