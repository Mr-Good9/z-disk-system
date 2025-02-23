package com.good.zdisksystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.good.zdisksystem.entity.model.FriendRequest;
import com.good.zdisksystem.entity.vo.FriendRequestVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface FriendRequestMapper extends BaseMapper<FriendRequest> {
    
    // 获取收到的好友请求列表
    List<FriendRequestVO> getReceivedRequests(@Param("userId") Long userId);
    
    // 获取发送的好友请求列表
    List<FriendRequestVO> getSentRequests(@Param("userId") Long userId);
    
    // 获取好友请求详情
    FriendRequestVO getRequestDetail(@Param("requestId") Long requestId);
    
    // 更新好友请求状态
    int updateRequestStatus(@Param("requestId") Long requestId, @Param("status") Integer status);
} 