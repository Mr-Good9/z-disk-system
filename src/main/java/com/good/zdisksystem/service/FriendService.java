package com.good.zdisksystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.good.zdisksystem.entity.model.Friend;
import com.good.zdisksystem.entity.model.FriendGroup;
import com.good.zdisksystem.entity.param.AddFriendParam;
import com.good.zdisksystem.entity.vo.FriendRequestVO;
import com.good.zdisksystem.entity.vo.UserFriendVO;
import com.good.zdisksystem.entity.vo.UserVO;

import java.util.List;

public interface FriendService extends IService<Friend> {

    // 获取好友列表
    List<UserFriendVO> getFriendList();

    // 搜索好友
    List<UserVO> searchFriends(String keyword);

    // 获取好友详情
    UserFriendVO getFriendDetail(Long friendId);

    // 添加好友请求
    void addFriendRequest(AddFriendParam param);

    // 获取收到的好友请求
    List<FriendRequestVO> getReceivedRequests();

    // 获取发送的好友请求
    List<FriendRequestVO> getSentRequests() ;

    // 处理好友请求
    void handleFriendRequest(Long requestId, Boolean accept);

    // 删除好友
    void deleteFriend(Long friendId) ;

    // 修改好友备注
    void updateFriendRemark(Long friendId, String remark);

    // 移动好友分组
    void moveFriendGroup(Long friendId, Long groupId);

    /**
     * 获取指定分组的好友列表
     * @param userId
     * @param groupId
     * @return
     */
    List<Friend> getFriendsByGroup(Long userId, Long groupId);

    /**
     * 获取好友分组列表
     * @param userId
     * @return
     */
    List<FriendGroup> getFriendGroups(Long userId);
}
