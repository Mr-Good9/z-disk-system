// src/main/java/com/good/zdisksystem/model/dto/MessageBoardDTO.java
package com.good.zdisksystem.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MessageBoardDTO {
    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String content;
    private Long parentId;
    private Integer likeCount;
    private Integer status;
    private LocalDateTime createTime;
    private List<MessageBoardDTO> replies;
    private Boolean isLiked = false;
}
