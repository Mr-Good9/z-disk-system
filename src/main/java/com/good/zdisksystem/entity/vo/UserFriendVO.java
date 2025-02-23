package com.good.zdisksystem.entity.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserFriendVO {
    private Long id;
    private Long friendId;
    private String username;
    private String nickname;
    private String avatar;
    private String remark;
    private Long groupId;
    private String groupName;
    private Integer status;
    private LocalDateTime createTime;
    private String createTimeStr;
} 