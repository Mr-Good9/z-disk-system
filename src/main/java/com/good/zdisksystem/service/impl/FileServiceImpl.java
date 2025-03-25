package com.good.zdisksystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.good.zdisksystem.common.exception.CustomException;
import com.good.zdisksystem.common.result.PageResult;
import com.good.zdisksystem.entity.model.File;
import com.good.zdisksystem.entity.model.FileShare;
import com.good.zdisksystem.entity.model.User;
import com.good.zdisksystem.entity.param.FileQueryParam;
import com.good.zdisksystem.entity.vo.FileStatisticsVO;
import com.good.zdisksystem.entity.vo.StorageUsageVO;
import com.good.zdisksystem.mapper.FileMapper;
import com.good.zdisksystem.entity.vo.FileVO;
import com.good.zdisksystem.mapper.FileShareMapper;
import com.good.zdisksystem.service.FileService;
import com.good.zdisksystem.service.MinioService;
import com.good.zdisksystem.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private MinioService minioService;

    @Autowired
    private UserService userService;

    @Autowired
    private FileShareMapper fileShareMapper;

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
                .set(File::getIsDeleted, 1)
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

        // 设置文件所有者信息
        if (file.getUserId() != null) {
            User owner = userService.getByUserId(file.getUserId());
            if (owner != null) {
                vo.setOwner(owner.getNickname() != null ? owner.getNickname() : owner.getUsername());
            }
        }

        // 设置文件状态
        vo.setStatus(file.getIsDeleted());

        // 设置是否共享 - 直接使用文件的isShared字段
        vo.setIsShared(file.getIsShared() == 1);

        // 设置创建和更新时间
        vo.setCreateTime(file.getCreateTime());
        vo.setUpdateTime(file.getUpdateTime());

        // 设置文件类型，如果是文件夹则设为null
        if (file.getIsFolder() == 1) {
            vo.setType(null);
        }

        return vo;
    }

    private String formatFileSize(Long size) {
        if (size == null) return "0 B";
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.2f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.2f MB", size / 1024.0 / 1024.0);
        return String.format("%.2f GB", size / 1024.0 / 1024.0 / 1024.0);
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

    @Override
    public File getById(Long fileId) {
        return fileMapper.selectById(fileId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateFileShareStatus(Long fileId, Long userId, boolean shared) {
        // 获取文件
        File file = fileMapper.selectById(fileId);
        if (file == null || file.getIsDeleted() == 1) {
            throw new CustomException("文件不存在或已删除");
        }

        // 验证文件所有权
        if (!file.getUserId().equals(userId)) {
            throw new CustomException("您没有权限操作此文件");
        }

        // 文件夹不能共享
        if (file.getIsFolder() == 1) {
            throw new CustomException("文件夹不能共享");
        }

        // 更新共享状态
        file.setIsShared(shared ? 1 : 0);
        file.setUpdateTime(LocalDateTime.now());
        int updated = fileMapper.updateById(file);

        return updated > 0;
    }

    @Override
    public PageResult<FileVO> getSharedFiles(Integer page, Integer size, String keyword) {
        Page<File> pageParam = new Page<>(page, size);

        // 构建查询条件
        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(File::getIsShared, 1); // 已共享
        queryWrapper.eq(File::getIsDeleted, 0); // 未删除

        // 添加关键字搜索
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(File::getName, keyword);
        }

        // 文件夹不能共享，但以防万一也过滤一下
        queryWrapper.eq(File::getIsFolder, 0);

        // 排序
        queryWrapper.orderByDesc(File::getUpdateTime);

        // 执行分页查询
        Page<File> result = fileMapper.selectPage(pageParam, queryWrapper);

        // 转换为VO列表
        List<FileVO> voList = result.getRecords().stream()
                .map(this::convertToFileVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, result.getTotal(), page, size);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean copyFileTo(Long fileId, Long targetUserId) {
        // 获取文件
        File sourceFile = fileMapper.selectById(fileId);
        if (sourceFile == null || sourceFile.getIsDeleted() == 1) {
            throw new CustomException("文件不存在或已删除");
        }

        // 验证文件是否共享
        if (sourceFile.getIsShared() != 1) {
            throw new CustomException("文件未共享");
        }

        // 获取目标用户信息
        User targetUser = userService.getByUserId(targetUserId);
        if (targetUser == null) {
            throw new CustomException("用户不存在");
        }

        // 检查用户存储空间
        long availableSpace = targetUser.getStorageMax() - targetUser.getStorageUsed();
        if (availableSpace < sourceFile.getSize()) {
            throw new CustomException("存储空间不足");
        }

        try {
            // 在MinIO中复制文件
            String newObjectName = generateNewObjectName(targetUserId, sourceFile.getName());
            minioService.copyObject(sourceFile.getPath(), newObjectName);

            // 创建新的文件记录
            File newFile = new File();
            newFile.setUserId(targetUserId);
            newFile.setParentId(0L); // 保存到根目录
            newFile.setName(sourceFile.getName());
            newFile.setType(sourceFile.getType());
            newFile.setSize(sourceFile.getSize());
            newFile.setPath(newObjectName);
            newFile.setIsFolder(0);
            newFile.setIsDeleted(0);
            newFile.setIsShared(0); // 默认不共享
            newFile.setCreateTime(LocalDateTime.now());
            newFile.setUpdateTime(LocalDateTime.now());

            fileMapper.insert(newFile);

            // 更新用户存储空间
            targetUser.setStorageUsed(targetUser.getStorageUsed() + sourceFile.getSize());
            userService.updateById(targetUser);

            return true;
        } catch (Exception e) {
            throw new CustomException("保存文件失败: " + e.getMessage());
        }
    }

    // 辅助方法 - 转换为FileVO
    private FileVO convertToFileVO(File file) {
        FileVO vo = new FileVO();
        BeanUtils.copyProperties(file, vo);
        
        // 设置共享状态
        vo.setIsShared(file.getIsShared() == 1);
        
        // 获取所有者信息
        User owner = userService.getByUserId(file.getUserId());
        if (owner != null) {
            vo.setOwnerName(owner.getNickname() != null ? owner.getNickname() : owner.getUsername());
            vo.setOwner(owner.getUsername());
        }
        
        // 格式化时间
        if (file.getCreateTime() != null) {
            vo.setCreateTimeStr(formatDateTime(file.getCreateTime()));
        }
        
        if (file.getUpdateTime() != null) {
            vo.setUpdateTimeStr(formatDateTime(file.getUpdateTime()));
        }
        
        return vo;
    }

    // 辅助方法 - 生成新的对象名称
    private String generateNewObjectName(Long userId, String fileName) {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%d/%s_%s_%s", userId, timestamp, uuid, fileName);
    }

    // 辅助方法 - 格式化日期时间
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
