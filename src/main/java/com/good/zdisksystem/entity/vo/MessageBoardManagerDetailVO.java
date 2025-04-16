package com.good.zdisksystem.entity.vo;

import com.good.zdisksystem.dto.MessageBoardManagerDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 留言板管理详情VO
 * @author chris
 * @since 2025/4/16 15:30
 */
@Data
public class MessageBoardManagerDetailVO {
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
    private List<MessageBoardManagerDTO> replies;
}
