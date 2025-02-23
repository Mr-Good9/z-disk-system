package com.good.zdisksystem.entity.param;

import lombok.Data;

@Data
public class UserQueryParam {
    private String keyword;     // 搜索关键词(用户名/昵称/邮箱)
    private Integer status;     // 用户状态
    private String role;        // 用户角色
    private Integer pageNum = 1;
    private Integer pageSize = 10;
} 