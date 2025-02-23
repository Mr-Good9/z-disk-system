package com.good.zdisksystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.good.zdisksystem.entity.model.RecycleBin;
import com.good.zdisksystem.entity.vo.UserFileVO;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface RecycleBinMapper extends BaseMapper<RecycleBin> {
    @Select({"<script>",
        "SELECT f.*, u.username as ownerName, rb.delete_time as deleteTime, rb.expire_time as expireTime",
        "FROM recycle_bin rb",
        "INNER JOIN file f ON rb.file_id = f.id",
        "LEFT JOIN user u ON f.user_id = u.id",
        "WHERE rb.user_id = #{userId}",
        "AND rb.status = 1",
        "<if test='keyword != null and keyword != \"\"'>",
        "AND f.name LIKE CONCAT('%', #{keyword}, '%')",
        "</if>",
        "ORDER BY rb.delete_time DESC",
        "LIMIT #{offset}, #{pageSize}",
        "</script>"})
    List<UserFileVO> getRecycleBinFiles(@Param("userId") Long userId,
                                       @Param("keyword") String keyword,
                                       @Param("offset") Integer offset,
                                       @Param("pageSize") Integer pageSize);

    @Select({"<script>",
        "SELECT COUNT(*)",
        "FROM recycle_bin rb",
        "INNER JOIN file f ON rb.file_id = f.id",
        "LEFT JOIN user u ON f.user_id = u.id",
        "WHERE rb.user_id = #{userId}",
        "<if test='keyword != null and keyword != \"\"'>",
        "AND f.name LIKE CONCAT('%', #{keyword}, '%')",
        "</if>",
        "</script>"})
    Long getRecycleBinCount(@Param("userId") Long userId,
                           @Param("keyword") String keyword);
} 