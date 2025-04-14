// src/main/java/com/good/zdisksystem/model/dto/MessageBoardRequestDTO.java
package com.good.zdisksystem.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class MessageBoardRequestDTO {
    @NotBlank(message = "留言内容不能为空")
    private String content;

    @NotNull(message = "父留言ID不能为空")
    private Long parentId = 0L; // 0表示主留言
}
