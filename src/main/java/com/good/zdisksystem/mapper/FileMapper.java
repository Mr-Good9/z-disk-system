package com.good.zdisksystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.good.zdisksystem.entity.model.File;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FileMapper extends BaseMapper<File> {
    
    @Select("SELECT COALESCE(SUM(size), 0) FROM file")
    Long sumFileSize();
    
    @Select("SELECT COUNT(*) FROM file")
    Long countTotalFiles();
    
    @Select("SELECT COUNT(*) FROM file WHERE is_shared = 1")
    Long countSharedFiles();
    
    @Select("SELECT COUNT(*) FROM file WHERE status = 1")
    Long countDeletedFiles();
}
