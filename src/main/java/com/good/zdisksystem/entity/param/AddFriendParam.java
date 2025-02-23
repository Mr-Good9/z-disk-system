package com.good.zdisksystem.entity.param;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class AddFriendParam {
    @NotNull(message = "好友ID不能为空")
    private Long friendId;
    
    private String remark;
    
    private Long groupId;
} 