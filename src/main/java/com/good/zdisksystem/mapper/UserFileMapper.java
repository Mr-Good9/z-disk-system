package com.good.zdisksystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.good.zdisksystem.entity.model.File;
import com.good.zdisksystem.entity.vo.UserFileVO;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
@Repository
public interface UserFileMapper extends BaseMapper<File> {
    @Select("SELECT f.*, u.username as ownerName " +
            "FROM file f " +
            "LEFT JOIN user u ON f.user_id = u.id " +
            "WHERE f.user_id = #{userId} " +
            "AND f.is_deleted = 1 " +
            "AND (f.name LIKE CONCAT('%', #{keyword}, '%') OR u.username LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY f.delete_time DESC " +
            "LIMIT #{offset}, #{pageSize}")
    List<UserFileVO> getRecycleBinFiles(@Param("userId") Long userId,
                                       @Param("keyword") String keyword,
                                       @Param("offset") Integer offset,
                                       @Param("pageSize") Integer pageSize);

    @Select("SELECT COUNT(*) FROM file f " +
            "LEFT JOIN user u ON f.user_id = u.id " +
            "WHERE f.user_id = #{userId} AND f.is_deleted = 1 " +
            "AND (f.name LIKE #{keyword} OR u.username LIKE #{keyword})")
    Long getRecycleBinCount(@Param("userId") Long userId,
                           @Param("keyword") String keyword);

    @Update("UPDATE file SET is_deleted = 0, update_time = #{now} " +
            "WHERE id = #{fileId} AND user_id = #{userId}")
    int restoreFile(@Param("fileId") Long fileId,
                    @Param("userId") Long userId,
                    @Param("now") LocalDateTime now);

    @Delete("DELETE FROM file WHERE id = #{fileId} AND user_id = #{userId}")
    int deleteFileCompletely(@Param("fileId") Long fileId,
                            @Param("userId") Long userId);

    @Select("SELECT * FROM file WHERE parent_id = #{parentId} AND user_id = #{userId} AND is_deleted = 0")
    List<File> getFilesByParentId(@Param("parentId") Long parentId, @Param("userId") Long userId);
}
