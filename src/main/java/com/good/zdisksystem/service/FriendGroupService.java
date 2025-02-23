package com.good.zdisksystem.service;

import com.good.zdisksystem.entity.model.FriendGroup;
import java.util.List;

public interface FriendGroupService {
    
    // 获取用户的好友分组列表
    List<FriendGroup> getUserGroups();
    
    // 创建好友分组
    FriendGroup createGroup(String name);
    
    // 更新分组名称
    void updateGroupName(Long groupId, String newName);
    
    // 更新分组排序
    void updateGroupSort(Long groupId, Integer sort);
    
    // 删除分组
    void deleteGroup(Long groupId);
    
    // 获取分组详情
    FriendGroup getGroupDetail(Long groupId);
} 