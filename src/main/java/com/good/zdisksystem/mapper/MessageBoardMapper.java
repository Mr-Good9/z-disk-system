// src/main/java/com/good/zdisksystem/mapper/MessageBoardMapper.java
package com.good.zdisksystem.mapper;

import com.good.zdisksystem.entity.model.MessageBoard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageBoardMapper {

    // 插入留言
    int insert(MessageBoard messageBoard);

    // 更新留言
    int update(MessageBoard messageBoard);

    // 根据ID查找留言
    MessageBoard selectById(Long id);

    // 查询留言列表（分页）
    List<MessageBoard> selectList(@Param("offset") Integer offset,
                                  @Param("limit") Integer limit,
                                  @Param("order") String order);

    // 查询回复列表
    List<MessageBoard> selectReplies(Long parentId);

    // 查询留言总数
    int countTotal();

    // 增加点赞数
    int increaseLikeCount(Long id);

    // 减少点赞数
    int decreaseLikeCount(Long id);

    // 软删除留言
    int deleteById(Long id);
}
