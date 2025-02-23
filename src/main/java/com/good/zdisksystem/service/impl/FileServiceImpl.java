package com.good.zdisksystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.good.zdisksystem.common.exception.CustomException;
import com.good.zdisksystem.common.result.PageResult;
import com.good.zdisksystem.entity.model.File;
import com.good.zdisksystem.entity.param.FileQueryParam;
import com.good.zdisksystem.entity.vo.FileStatisticsVO;
import com.good.zdisksystem.entity.vo.StorageUsageVO;
import com.good.zdisksystem.mapper.FileMapper;
import com.good.zdisksystem.model.dto.FileQueryParam;
import com.good.zdisksystem.model.vo.FileVO;
import com.good.zdisksystem.service.FileService;
import com.good.zdisksystem.service.MinioService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private MinioService minioService;

    @Override
    public FileStatisticsVO getFileStatistics() {
        FileStatisticsVO vo = new FileStatisticsVO();
        
        // 获取总文件数和总大小
        vo.setTotalFiles(fileMapper.countTotalFiles());
        vo.setTotalSize(fileMapper.sumFileSize());
        
        // 获取共享文件数和回收站文件数
        vo.setSharedFiles(fileMapper.countSharedFiles());
        vo.setDeletedFiles(fileMapper.countDeletedFiles());
        
        // 计算占比
        if (vo.getTotalFiles() > 0) {
            vo.setSharedRatio((double) vo.getSharedFiles() / vo.getTotalFiles() * 100);
            vo.setDeletedRatio((double) vo.getDeletedFiles() / vo.getTotalFiles() * 100);
        }
        
        // 计算存储使用率
        Long totalStorage = 10L * 1024 * 1024 * 1024; // 10GB
        vo.setStorageUsage((double) vo.getTotalSize() / totalStorage * 100);
        
        return vo;
    }

    @Override
    public PageResult<FileVO> getFileList(FileQueryParam param) {
        Page<File> page = new Page<>(param.getPageNum(), param.getPageSize());
        
        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<>();
        // 添加搜索条件
        if (param.getKeyword() != null) {
            wrapper.like(File::getName, param.getKeyword());
        }
        if (param.getOwner() != null) {
            wrapper.eq(File::getUserId, param.getOwner());
        }
        if (param.getType() != null) {
            wrapper.eq(File::getType, param.getType());
        }
        if (param.getStatus() != null) {
            wrapper.eq(File::getIsDeleted, param.getStatus());
        }
        
        // 执行查询
        Page<File> result = fileMapper.selectPage(page, wrapper);
        
        // 转换为VO
        List<FileVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return PageResult.build(voList, result.getTotal(), param.getPageNum(), param.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteFiles(List<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return;
        }
        // 逻辑删除文件
        fileMapper.update(
            null,
            new LambdaUpdateWrapper<File>()
                .set(File::getStatus, 1)
                .in(File::getId, fileIds)
        );
    }

    @Override
    public String getPreviewUrl(Long fileId) {
        File file = fileMapper.selectById(fileId);
        if (file == null) {
            throw new CustomException("文件不存在");
        }
        // 生成预览链接
        return minioService.getPreviewUrl(file.getPath());
    }

    private FileVO convertToVO(File file) {
        FileVO vo = new FileVO();
        BeanUtils.copyProperties(file, vo);
        // 设置额外属性
        return vo;
    }

    @Override
    public StorageUsageVO getStorageUsage() {
        return null;
    }

    @Override
    public void restoreFiles(Long[] fileIds) {

    }

    @Override
    public void permanentlyDeleteFiles(Long[] fileIds) {

    }

    @Override
    public void updateFileStatus(Long fileId, Integer status) {

    }

    @Override
    public Long countTotalFiles() {
        return fileMapper.countTotalFiles();
    }

    @Override
    public Long getUsedStorage() {
        return fileMapper.sumFileSize();
    }
}
