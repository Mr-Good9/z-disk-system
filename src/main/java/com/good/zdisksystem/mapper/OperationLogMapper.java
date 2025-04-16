// z-disk-system/z-disk-system/src/main/java/com/good/zdisksystem/mapper/OperationLogMapper.java
package com.good.zdisksystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.good.zdisksystem.entity.model.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {

    @Select("<script>" +
            "SELECT ol.*, u.username " +
            "FROM operation_log ol " +
            "LEFT JOIN user u ON ol.user_id = u.id " +
            "WHERE 1=1 " +
            "<if test='userId != null and userId != \"\"'>" +
            "  AND (ol.user_id = #{userId} OR u.username LIKE CONCAT('%',#{userId},'%'))" +
            "</if>" +
            "<if test='module != null and module != \"\"'>" +
            "  AND ol.module = #{module}" +
            "</if>" +
            "<if test='action != null and action != \"\"'>" +
            "  AND ol.action = #{action}" +
            "</if>" +
            "<if test='startTime != null and startTime != \"\"'>" +
            "  AND ol.create_time &gt;= #{startTime}" +
            "</if>" +
            "<if test='endTime != null and endTime != \"\"'>" +
            "  AND ol.create_time &lt;= #{endTime}" +
            "</if>" +
            " ORDER BY ol.create_time DESC" +
            "</script>")
    List<Map<String, Object>> selectOperationLogs(
            @Param("userId") String userId,
            @Param("module") String module,
            @Param("action") String action,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime
    );
}
