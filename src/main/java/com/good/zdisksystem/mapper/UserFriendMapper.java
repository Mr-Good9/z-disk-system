package com.good.zdisksystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.good.zdisksystem.entity.model.Friend;
import com.good.zdisksystem.entity.vo.UserFriendVO;
import com.good.zdisksystem.entity.vo.UserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserFriendMapper extends BaseMapper<Friend> {

    List<UserFriendVO> getFriendList(@Param("userId") Long userId);

    List<UserVO> searchFriends(@Param("userId") Long userId,
                              @Param("keyword") String keyword);

    UserFriendVO getFriendDetail(@Param("userId") Long userId,
                                @Param("friendId") Long friendId);
}
