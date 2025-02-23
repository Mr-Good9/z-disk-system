package com.good.zdisksystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.good.zdisksystem.common.exception.CustomException;
import com.good.zdisksystem.common.utils.RequestUser;
import com.good.zdisksystem.entity.model.Friend;
import com.good.zdisksystem.entity.model.FriendGroup;
import com.good.zdisksystem.mapper.FriendGroupMapper;
import com.good.zdisksystem.mapper.UserFriendMapper;
import com.good.zdisksystem.service.FriendGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendGroupServiceImpl implements FriendGroupService {

    private final FriendGroupMapper friendGroupMapper;
    private final UserFriendMapper userFriendMapper;

    @Override
    public List<FriendGroup> getUserGroups() {
        Long userId = RequestUser.getUser().getId();
        return friendGroupMapper.getUserGroups(userId);
    }

    @Override
    public FriendGroup createGroup(String name) {
        Long userId = RequestUser.getUser().getId();

        // 检查分组名是否已存在
        LambdaQueryWrapper<FriendGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendGroup::getUserId, userId)
               .eq(FriendGroup::getName, name);
        if (friendGroupMapper.selectCount(wrapper) > 0) {
            throw new CustomException("分组名称已存在");
        }

        // 获取最大排序值
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendGroup::getUserId, userId)
               .orderByDesc(FriendGroup::getSort)
               .last("LIMIT 1");
        FriendGroup lastGroup = friendGroupMapper.selectOne(wrapper);
        int sort = lastGroup != null ? lastGroup.getSort() + 1 : 0;

        // 创建新分组
        FriendGroup group = new FriendGroup();
        group.setUserId(userId);
        group.setName(name);
        group.setSort(sort);

        friendGroupMapper.insert(group);
        log.info("创建好友分组: userId={}, name={}, sort={}", userId, name, sort);

        return group;
    }

    @Override
    public void updateGroupName(Long groupId, String newName) {
        Long userId = RequestUser.getUser().getId();

        // 检查分组是否存在
        FriendGroup group = checkGroupExists(groupId, userId);

        // 检查新名称是否已存在
        LambdaQueryWrapper<FriendGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendGroup::getUserId, userId)
               .eq(FriendGroup::getName, newName)
               .ne(FriendGroup::getId, groupId);
        if (friendGroupMapper.selectCount(wrapper) > 0) {
            throw new CustomException("分组名称已存在");
        }

        // 更新分组名称
        group.setName(newName);
        friendGroupMapper.updateById(group);

        log.info("更新分组名称: groupId={}, newName={}", groupId, newName);
    }

    @Override
    public void updateGroupSort(Long groupId, Integer sort) {
        Long userId = RequestUser.getUser().getId();

        // 检查分组是否存在
        FriendGroup group = checkGroupExists(groupId, userId);

        // 更新排序
        group.setSort(sort);
        friendGroupMapper.updateById(group);

        log.info("更新分组排序: groupId={}, sort={}", groupId, sort);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroup(Long groupId) {
        Long userId = RequestUser.getUser().getId();

        // 检查分组是否存在
        FriendGroup group = checkGroupExists(groupId, userId);

        // 检查分组是否有好友
        LambdaQueryWrapper<Friend> friendWrapper = new LambdaQueryWrapper<>();
        friendWrapper.eq(Friend::getUserId, userId)
                    .eq(Friend::getGroupId, groupId)
                    .eq(Friend::getStatus, 1);
        if (userFriendMapper.selectCount(friendWrapper) > 0) {
            throw new CustomException("分组内还有好友，不能删除");
        }

        // 删除分组
        friendGroupMapper.deleteById(groupId);

        log.info("删除好友分组: groupId={}", groupId);
    }

    @Override
    public FriendGroup getGroupDetail(Long groupId) {
        Long userId = RequestUser.getUser().getId();
        return checkGroupExists(groupId, userId);
    }

    private FriendGroup checkGroupExists(Long groupId, Long userId) {
        LambdaQueryWrapper<FriendGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendGroup::getId, groupId)
               .eq(FriendGroup::getUserId, userId);

        FriendGroup group = friendGroupMapper.selectOne(wrapper);
        if (group == null) {
            throw new CustomException("好友分组不存在");
        }

        return group;
    }
}
