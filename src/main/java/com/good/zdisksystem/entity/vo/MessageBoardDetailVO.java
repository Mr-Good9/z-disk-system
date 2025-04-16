package com.good.zdisksystem.entity.vo;

import com.good.zdisksystem.dto.MessageBoardDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MessageBoardDetailVO {
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
    private List<MessageBoardDTO> replies;
}
