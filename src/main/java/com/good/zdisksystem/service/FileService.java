package com.good.zdisksystem.service;

import com.good.zdisksystem.common.result.PageResult;
import com.good.zdisksystem.entity.model.File;
import com.good.zdisksystem.entity.param.FileQueryParam;
import com.good.zdisksystem.entity.vo.StorageUsageVO;
import com.good.zdisksystem.entity.vo.FileVO;
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

    /**
     * 根据ID获取文件
     */
    File getById(Long fileId);

    /**
     * 更新文件共享状态
     * @param fileId 文件ID
     * @param userId 用户ID
     * @param shared 共享状态
     * @return 是否更新成功
     */
    boolean updateFileShareStatus(Long fileId, Long userId, boolean shared);

    /**
     * 获取所有共享文件
     * @param page 页码
     * @param size 每页数量
     * @param keyword 搜索关键词
     * @return 共享文件列表
     */
    PageResult<FileVO> getSharedFiles(Integer page, Integer size, String keyword);

    /**
     * 复制文件到指定用户
     * @param fileId 文件ID
     * @param userId 目标用户ID
     * @return 是否复制成功
     */
    boolean copyFileTo(Long fileId, Long userId);
}
