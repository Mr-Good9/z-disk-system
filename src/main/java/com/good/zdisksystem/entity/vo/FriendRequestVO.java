package com.good.zdisksystem.entity.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FriendRequestVO {
    private Long id;
    private Long fromUserId;
    private Long toUserId;
    private String fromUsername;
    private String fromNickname;
    private String fromAvatar;
    private String remark;
    private Integer status;
    private LocalDateTime createTime;
    private String createTimeStr;
}
