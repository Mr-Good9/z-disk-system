package com.good.zdisksystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.good.zdisksystem.entity.model.File;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FileMapper extends BaseMapper<File> {

    /**
     * 统计总文件数
     */
//    @Select("SELECT COUNT(*) FROM file")
    Long countTotalFiles();

    /**
     * 统计文件总大小
     */
//    @Select("SELECT COALESCE(SUM(size), 0) FROM file")
    Long sumFileSize();

    /**
     * 统计共享文件数
     */
//    @Select("SELECT COUNT(*) FROM file WHERE is_shared = 1")
    Long countSharedFiles();

    /**
     * 统计回收站文件数
     */
//    @Select("SELECT COUNT(*) FROM file WHERE status = 1")
    Long countDeletedFiles();
}
