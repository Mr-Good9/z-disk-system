package com.good.zdisksystem.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author chris
 * @since 2025/4/16 15:29
 */
@Data
public class MessageBoardManagerDTO {
    private Long id;
    private Long userId;
    private String content;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String username;
    private String nickname;
    private String avatar;
    private String userType;
    private Integer likeCount;
    private Integer replyCount;
    private Boolean isLiked;
}
