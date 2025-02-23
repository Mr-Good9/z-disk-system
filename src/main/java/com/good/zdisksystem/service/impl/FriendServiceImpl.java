package com.good.zdisksystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.good.zdisksystem.common.exception.CustomException;
import com.good.zdisksystem.common.utils.RequestUser;
import com.good.zdisksystem.entity.model.Friend;
import com.good.zdisksystem.entity.model.FriendRequest;
import com.good.zdisksystem.entity.param.AddFriendParam;
import com.good.zdisksystem.entity.vo.FriendRequestVO;
import com.good.zdisksystem.entity.vo.UserFriendVO;
import com.good.zdisksystem.entity.vo.UserVO;
import com.good.zdisksystem.mapper.FriendMapper;
import com.good.zdisksystem.mapper.FriendRequestMapper;
import com.good.zdisksystem.mapper.UserFriendMapper;
import com.good.zdisksystem.mapper.FriendGroupMapper;
import com.good.zdisksystem.entity.model.FriendGroup;
import com.good.zdisksystem.service.FriendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend> implements FriendService {

    private final UserFriendMapper userFriendMapper;
    private final FriendRequestMapper friendRequestMapper;
    private final FriendGroupMapper friendGroupMapper;
    private final FriendMapper friendMapper;

    @Override
    public List<UserFriendVO> getFriendList() {
        Long userId = RequestUser.getUser().getId();
        return userFriendMapper.getFriendList(userId);
    }

    @Override
    public List<UserVO> searchFriends(String keyword) {
        Long userId = RequestUser.getUser().getId();
        return userFriendMapper.searchFriends(userId, keyword);
    }

    @Override
    public UserFriendVO getFriendDetail(Long friendId) {
        Long userId = RequestUser.getUser().getId();
        return userFriendMapper.getFriendDetail(userId, friendId);
    }

    @Override
    public void addFriendRequest(AddFriendParam param) {
        Long userId = RequestUser.getUser().getId();

        // 检查是否已经是好友
        UserFriendVO existingFriend = userFriendMapper.getFriendDetail(userId, param.getFriendId());
        if (existingFriend != null) {
            throw new CustomException("该用户已经是你的好友");
        }

        // 检查是否已经发送过请求
        LambdaQueryWrapper<FriendRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendRequest::getFromUserId, userId)
                .eq(FriendRequest::getToUserId, param.getFriendId())
                .eq(FriendRequest::getStatus, 0);
        if (friendRequestMapper.selectCount(wrapper) > 0) {
            throw new CustomException("已经发送过好友请求，请等待对方处理");
        }

        // 创建好友请求
        FriendRequest request = new FriendRequest();
        request.setFromUserId(userId);
        request.setToUserId(param.getFriendId());
        request.setRemark(param.getRemark());
        request.setStatus(0);

        friendRequestMapper.insert(request);
        log.info("发送好友请求: fromUserId={}, toUserId={}", userId, param.getFriendId());
    }

    @Override
    public List<FriendRequestVO> getReceivedRequests() {
        Long userId = RequestUser.getUser().getId();
        return friendRequestMapper.getReceivedRequests(userId);
    }

    @Override
    public List<FriendRequestVO> getSentRequests() {
        Long userId = RequestUser.getUser().getId();
        return friendRequestMapper.getSentRequests(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleFriendRequest(Long requestId, Boolean accept) {
        Long userId = RequestUser.getUser().getId();

        // 获取请求详情
        FriendRequestVO request = friendRequestMapper.getRequestDetail(requestId);
        if (request == null) {
            throw new CustomException("好友请求不存在");
        }

        // 检查权限
        if (!userId.equals(request.getToUserId())) {
            throw new CustomException("无权处理该请求");
        }

        // 更新请求状态
        int status = accept ? 1 : 2;
        friendRequestMapper.updateRequestStatus(requestId, status);

        if (accept) {
            // 互相添加好友
            addFriendRelation(userId, request.getFromUserId());
            addFriendRelation(request.getFromUserId(), userId);
            log.info("接受好友请求: requestId={}, fromUserId={}, toUserId={}",
                    requestId, request.getFromUserId(), userId);
        } else {
            log.info("拒绝好友请求: requestId={}, fromUserId={}, toUserId={}",
                    requestId, request.getFromUserId(), userId);
        }
    }

    private void addFriendRelation(Long userId, Long friendId) {
        Friend friend = new Friend();
        friend.setUserId(userId);
        friend.setFriendId(friendId);
        friend.setStatus(1);
        userFriendMapper.insert(friend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFriend(Long friendId) {
        Long userId = RequestUser.getUser().getId();

        // 更新好友状态为已删除
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, friendId);

        Friend friend = new Friend();
        friend.setStatus(0);
        userFriendMapper.update(friend, wrapper);

        log.info("删除好友关系: userId={}, friendId={}", userId, friendId);
    }

    @Override
    public void updateFriendRemark(Long friendId, String remark) {
        Long userId = RequestUser.getUser().getId();

        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, friendId);

        Friend friend = new Friend();
        friend.setRemark(remark);
        userFriendMapper.update(friend, wrapper);

        log.info("更新好友备注: userId={}, friendId={}, remark={}", userId, friendId, remark);
    }

    @Override
    public void moveFriendGroup(Long friendId, Long groupId) {
        Long userId = RequestUser.getUser().getId();

        // 使用 LambdaUpdateWrapper 构建更新条件
        LambdaUpdateWrapper<Friend> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, friendId)
                .set(Friend::getGroupId, groupId)
                .set(Friend::getUpdateTime, LocalDateTime.now());

        // 执行更新
        boolean success = this.update(updateWrapper);
        if (!success) {
            throw new CustomException("更新失败，好友关系不存在");
        }

        log.info("移动好友分组: userId={}, friendId={}, groupId={}", userId, friendId, groupId);
    }

    @Override
    public List<FriendGroup> getFriendGroups(Long userId) {
        return friendGroupMapper.selectList(
            new LambdaQueryWrapper<FriendGroup>()
                .eq(FriendGroup::getUserId, userId)
                .orderByAsc(FriendGroup::getSort)
        );
    }
    
    @Override
    public List<Friend> getFriendsByGroup(Long userId, Long groupId) {
        // 首先验证分组是否存在且属于该用户
        FriendGroup group = friendGroupMapper.selectOne(
            new LambdaQueryWrapper<FriendGroup>()
                .eq(FriendGroup::getId, groupId)
                .eq(FriendGroup::getUserId, userId)
        );
        
        if (group == null) {
            throw new CustomException("好友分组不存在");
        }
        
        // 查询分组下的好友列表
        return friendMapper.selectList(
            new LambdaQueryWrapper<Friend>()
                .eq(Friend::getUserId, userId)
                .eq(Friend::getGroupId, groupId)
                .orderByAsc(Friend::getCreateTime)
        );
    }
}
