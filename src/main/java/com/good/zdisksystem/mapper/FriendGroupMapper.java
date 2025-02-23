package com.good.zdisksystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.good.zdisksystem.entity.model.FriendGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface FriendGroupMapper extends BaseMapper<FriendGroup> {
    List<FriendGroup> getUserGroups(@Param("userId") Long userId);
} 