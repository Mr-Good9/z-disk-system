// src/main/java/com/good/zdisksystem/mapper/MessageBoardMapper.java
package com.good.zdisksystem.mapper;

import com.good.zdisksystem.entity.model.MessageBoard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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

    // 管理员功能新增方法
    @Select("<script>" +
            "SELECT m.*, u.username, u.nickname, u.avatar " +
            "FROM message_board m " +
            "LEFT JOIN user u ON m.user_id = u.id " +
            "WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "  AND (m.content LIKE CONCAT('%',#{keyword},'%') " +
            "       OR u.username LIKE CONCAT('%',#{keyword},'%') " +
            "       OR u.nickname LIKE CONCAT('%',#{keyword},'%'))" +
            "</if>" +
            "<if test='status != null'>" +
            "  AND m.status = #{status}" +
            "</if>" +
            "<if test='startTime != null and startTime != \"\"'>" +
            "  AND m.create_time >= #{startTime}" +
            "</if>" +
            "<if test='endTime != null and endTime != \"\"'>" +
            "  AND m.create_time &lt;= #{endTime}" +
            "</if>" +
            " ORDER BY m.create_time DESC" +
            "</script>")
    List<MessageBoard> selectMessagesByCondition(@Param("keyword") String keyword,
                                              @Param("status") Integer status,
                                              @Param("startTime") String startTime,
                                              @Param("endTime") String endTime);

    @Update("UPDATE message_board SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("<script>" +
            "UPDATE message_board SET status = #{status}, update_time = NOW() " +
            "WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") Integer status);

    int countReplies(Long id);

    MessageBoard selectDeleteById(Long id);
}
