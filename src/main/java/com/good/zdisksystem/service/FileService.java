package com.good.zdisksystem.service;

import com.good.zdisksystem.common.result.PageResult;
import com.good.zdisksystem.entity.vo.StorageUsageVO;
import com.good.zdisksystem.model.dto.FileQueryParam;
import com.good.zdisksystem.model.vo.FileVO;
import com.good.zdisksystem.entity.vo.FileStatisticsVO;

import java.util.List;

public interface FileService {
    /**
     * 获取文件统计信息
     */
    FileStatisticsVO getFileStatistics();

    /**
     * 获取文件列表
     */
    PageResult<FileVO> getFileList(FileQueryParam param);

    /**
     * 获取存储使用情况
     */
    StorageUsageVO getStorageUsage();

    /**
     * 批量删除文件
     */
    void batchDeleteFiles(List<Long> fileIds);

    /**
     * 恢复已删除的文件
     */
    void restoreFiles(Long[] fileIds);

    /**
     * 永久删除文件
     */
    void permanentlyDeleteFiles(Long[] fileIds);

    /**
     * 更新文件状态
     */
    void updateFileStatus(Long fileId, Integer status);

    /**
     * 获取总文件数
     */
    Long countTotalFiles();

    /**
     * 获取已使用存储空间
     */
    Long getUsedStorage();

    /**
     * 获取文件预览链接
     */
    String getPreviewUrl(Long fileId);
}
