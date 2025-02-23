package com.good.zdisksystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.good.zdisksystem.common.exception.CustomException;
import com.good.zdisksystem.common.exception.enums.GlobalErrorCodeConstants;
import com.good.zdisksystem.common.result.CommonResult;
import com.good.zdisksystem.common.result.PageResult;
import com.good.zdisksystem.common.utils.RequestUser;
import com.good.zdisksystem.entity.model.FileShare;
import com.good.zdisksystem.entity.model.ShareReceive;
import com.good.zdisksystem.entity.model.ShareStats;
import com.good.zdisksystem.entity.model.User;
import com.good.zdisksystem.entity.model.File;
import com.good.zdisksystem.entity.param.UpdateShareSettingsParam;
import com.good.zdisksystem.entity.param.ShareListParam;
import com.good.zdisksystem.entity.vo.ShareFileVO;
import com.good.zdisksystem.mapper.FileShareMapper;
import com.good.zdisksystem.mapper.FileMapper;
import com.good.zdisksystem.mapper.ShareReceiveMapper;
import com.good.zdisksystem.mapper.UserMapper;
import com.good.zdisksystem.service.FileShareService;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileShareServiceImpl implements FileShareService {

    private final FileShareMapper fileShareMapper;
    private final FileMapper fileMapper;
    private final ShareReceiveMapper shareReceiveMapper;
    private final UserMapper userMapper;
    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileShare createShare(Long fileId, Integer expireDays, Integer shareType) {
        // 获取当前用户
        User user = RequestUser.getUser();

        // 检查文件是否存在
        File file = fileMapper.selectById(fileId);
        if (file == null || file.getIsDeleted() == 1) {
            throw new CustomException(GlobalErrorCodeConstants.FILE_NOT_FOUND);
        }

        // 检查是否是文件所有者
        if (!file.getUserId().equals(user.getId())) {
            throw new CustomException(GlobalErrorCodeConstants.FORBIDDEN);
        }

        // 创建分享记录
        FileShare share = new FileShare();
        share.setFileId(fileId);
        share.setUserId(user.getId());
        share.setShareCode(generateShareCode());
        share.setShareType(shareType);
        share.setExpireDays(expireDays);
        share.setCreateTime(LocalDateTime.now());

        if (expireDays != null && expireDays > 0) {
            share.setExpireTime(LocalDateTime.now().plusDays(expireDays));
        }

        share.setStatus(0);  // 0:正常
        share.setViewCount(0);
        share.setDownloadCount(0);
        share.setIsDeleted(0);

        fileShareMapper.insert(share);
        return share;
    }

    @Override
    public FileShare getShareByCode(String shareCode) {
        LambdaQueryWrapper<FileShare> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileShare::getShareCode, shareCode)
               .eq(FileShare::getIsDeleted, 0);

        FileShare share = fileShareMapper.selectOne(wrapper);

        if (share != null) {
            // 检查是否过期
            if (share.getExpireTime() != null && share.getExpireTime().isBefore(LocalDateTime.now())) {
                share.setStatus(1); // 设置为已过期
                fileShareMapper.updateById(share);
                return null;
            }
        }

        return share;
    }

    @Override
    public List<FileShare> getSharesByFileId(Long fileId) {
        LambdaQueryWrapper<FileShare> wrapper = new LambdaQueryWrapper<FileShare>()
                .eq(FileShare::getFileId, fileId)
                .eq(FileShare::getIsDeleted, 0);
        return fileShareMapper.selectList(wrapper);
    }

    @Override
    public List<FileShare> getSharesByUserId(Long userId) {
        LambdaQueryWrapper<FileShare> wrapper = new LambdaQueryWrapper<FileShare>()
                .eq(FileShare::getUserId, userId)
                .eq(FileShare::getIsDeleted, 0);
        return fileShareMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelShare(Long shareId) {
        FileShare share = fileShareMapper.selectById(shareId);
        if (share == null || share.getIsDeleted() == 1) {
            throw new CustomException("分享不存在");
        }

        // 设置分享状态为已取消
        share.setStatus(2);
        share.setIsDeleted(1);
        fileShareMapper.updateById(share);

        // 删除相关的接收记录
        LambdaQueryWrapper<ShareReceive> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShareReceive::getShareId, shareId);

        ShareReceive update = new ShareReceive();
        update.setIsDeleted(1);
        shareReceiveMapper.update(update, wrapper);
    }

    @Override
    public File getSharedFile(String shareCode) {
        FileShare share = getShareByCode(shareCode);
        File file = fileMapper.selectById(share.getFileId());
        if (file == null || file.getIsDeleted() == 1) {
            throw new CustomException(GlobalErrorCodeConstants.FILE_NOT_FOUND);
        }
        return file;
    }

    @Override
    @Transactional
    public List<FileShare> batchShare(List<Long> fileIds, Integer expireDays) {
        if (fileIds == null || fileIds.isEmpty()) {
            throw new CustomException("请选择要分享的文件");
        }

        // 检查文件数量限制
        if (fileIds.size() > 100) {
            throw new CustomException("单次最多分享100个文件");
        }

        List<FileShare> shares = new ArrayList<>();
        for (Long fileId : fileIds) {
            // 默认为私密分享
            shares.add(createShare(fileId, expireDays, 0));
        }
        return shares;
    }

    @Override
    public ShareStats getShareStats(Long shareId) {
        FileShare share = fileShareMapper.selectById(shareId);
        if (share == null || share.getIsDeleted() == 1) {
            throw new CustomException("分享不存在");
        }

        // 获取保存次数
        LambdaQueryWrapper<ShareReceive> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShareReceive::getShareId, shareId)
               .eq(ShareReceive::getStatus, 1)  // 已保存
               .eq(ShareReceive::getIsDeleted, 0);
        int saveCount = shareReceiveMapper.selectCount(wrapper).intValue();

        ShareStats stats = new ShareStats();
        stats.setViewCount(share.getViewCount());
        stats.setDownloadCount(share.getDownloadCount());
        stats.setSaveCount(saveCount);
        stats.setCreateTime(share.getCreateTime());
        stats.setExpireTime(share.getExpireTime());

        return stats;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<?> saveSharedFile(Long shareId, Long userId) {
        // 1. 验证分享是否存在和有效
        FileShare share = fileShareMapper.selectById(shareId);
        if (share == null || share.getIsDeleted() == 1 || share.getStatus() != 0) {
            throw new CustomException("分享不存在或已失效");
        }

        // 2. 获取源文件信息
        File sourceFile = fileMapper.selectById(share.getFileId());
        if (sourceFile == null) {
            throw new CustomException("源文件不存在");
        }

        // 3. 检查用户存储空间是否足够
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new CustomException("用户不存在");
        }
        long availableSpace = user.getStorageMax() - user.getStorageUsed();
        if (availableSpace < sourceFile.getSize()) {
            throw new CustomException("存储空间不足");
        }

        try {
            // 4. 在 Minio 中复制文件
            String newObjectName = generateNewObjectName(userId, sourceFile.getName());
            copyMinioObject(sourceFile.getPath(), newObjectName);

            // 5. 创建新的文件记录
            File newFile = new File();
            newFile.setUserId(userId);
            newFile.setParentId(0L);
            newFile.setName(sourceFile.getName());
            newFile.setType(sourceFile.getType());
            newFile.setSize(sourceFile.getSize());
            newFile.setPath(newObjectName);
            newFile.setIsFolder(sourceFile.getIsFolder());
            newFile.setIsDeleted(0);
            newFile.setCreateTime(LocalDateTime.now());
            newFile.setUpdateTime(LocalDateTime.now());

            fileMapper.insert(newFile);

            // 6. 更新用户存储空间使用量
            user.setStorageUsed(user.getStorageUsed() + sourceFile.getSize());
            userMapper.updateById(user);

            // 7. 更新分享接收记录
            ShareReceive shareReceive = new ShareReceive();
            shareReceive.setShareId(shareId);
            shareReceive.setReceiverId(userId);
            shareReceive.setCreateTime(LocalDateTime.now());
            shareReceive.setStatus(1);
            shareReceive.setIsDeleted(0);
            shareReceiveMapper.insert(shareReceive);

            // 8. 更新分享的下载次数
            fileShareMapper.incrementDownloadCount(shareId);

            return CommonResult.success("文件保存成功");
        } catch (Exception e) {
            log.error("保存分享文件失败", e);
            throw new CustomException("保存文件失败: " + e.getMessage());
        }
    }

    private String generateNewObjectName(Long userId, String fileName) {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%d/%s_%s_%s", userId, timestamp, uuid, fileName);
    }

    private void copyMinioObject(String sourceObject, String targetObject) throws Exception {
        minioClient.copyObject(
            CopyObjectArgs.builder()
                .source(CopySource.builder()
                    .bucket(bucketName)
                    .object(sourceObject)
                    .build())
                .bucket(bucketName)
                .object(targetObject)
                .build()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileShare updateShareSettings(Long shareId, UpdateShareSettingsParam param) {
        FileShare share = fileShareMapper.selectById(shareId);
        if (share == null || share.getIsDeleted() == 1) {
            throw new CustomException(GlobalErrorCodeConstants.NOT_FOUND);
        }

        // 检查权限
        User user = RequestUser.getUser();
        if (!share.getUserId().equals(user.getId())) {
            throw new CustomException(GlobalErrorCodeConstants.FORBIDDEN);
        }

        // 更新设置
        if (param.getExpireDays() != null) {
            share.setExpireDays(param.getExpireDays());
            if (param.getExpireDays() > 0) {
                share.setExpireTime(LocalDateTime.now().plusDays(param.getExpireDays()));
            } else {
                share.setExpireTime(null);  // 永久有效
            }
        }

        // 修复密码设置
        if (param.getAccessCode() != null) {
            share.setAccessCode(param.getAccessCode());  // 应该使用专门的访问密码字段
        }

        if (param.getShareType() != null) {
            share.setShareType(param.getShareType());
        }

        share.setUpdateTime(LocalDateTime.now());
        fileShareMapper.updateById(share);
        return share;
    }

    public List<FileShare> getReceivedShares(Long userId) {
        // TODO: 实现接收到的分享列表查询
        // 这需要添加一个新的表来记录分享接收关系
        return new ArrayList<>();
    }

    @Override
    public boolean verifySharePassword(String shareCode, String password) {
        FileShare share = getShareByCode(shareCode);
        if (share == null || share.getIsDeleted() == 1) {
            throw new CustomException("分享不存在或已失效");
        }

        if (share.getShareType() == 1) { // 公开分享
            return true;
        }

        return share.getAccessCode() != null && share.getAccessCode().equals(password);
    }

    @Override
    @Transactional
    public void incrementViewCount(String shareCode) {
        FileShare share = getShareByCode(shareCode);
        if (share == null || share.getIsDeleted() == 1) {
            throw new CustomException("分享不存在或已失效");
        }
        fileShareMapper.incrementViewCount(share.getId());
    }

    @Override
    @Transactional
    public void incrementDownloadCount(String shareCode) {
        FileShare share = getShareByCode(shareCode);
        if (share == null || share.getIsDeleted() == 1) {
            throw new CustomException("分享不存在或已失效");
        }
        fileShareMapper.incrementDownloadCount(share.getId());
    }

    @Override
    public List<ShareFileVO> getMyShares() {
        return null;
    }

    @Override
    public List<ShareFileVO> getReceivedShares() {
        return null;
    }

    @Override
    public PageResult<ShareFileVO> getShareList(ShareListParam param) {
        Long userId = RequestUser.getUser().getId();
        int offset = (param.getPage() - 1) * param.getSize();

        List<ShareFileVO> records;
        Long total;

        // 构建搜索条件
        String searchPattern = null;
        if (StringUtils.hasText(param.getKeyword())) {
            searchPattern = "%" + param.getKeyword() + "%";
        }

        if ("received".equals(param.getType())) {
            // 收到的分享
            if (searchPattern != null) {
                records = fileShareMapper.searchReceivedShares(userId, searchPattern, offset, param.getSize());
                total = fileShareMapper.getReceivedSharesSearchCount(userId, searchPattern);
            } else {
                records = fileShareMapper.getReceivedShares(userId, offset, param.getSize());
                total = fileShareMapper.getReceivedSharesCount(userId);
            }
        } else {
            // 我的分享
            if (searchPattern != null) {
                records = fileShareMapper.searchMyShares(userId, searchPattern, offset, param.getSize());
                total = fileShareMapper.getMySharesSearchCount(userId, searchPattern);
            } else {
                records = fileShareMapper.getMyShares(userId, offset, param.getSize());
                total = fileShareMapper.getMySharesCount(userId);
            }
        }

        // 处理文件信息
        if (records != null && !records.isEmpty()) {
            for (ShareFileVO vo : records) {
                // 处理过期状态
                if (vo.getExpireTime() != null && vo.getExpireTime().isBefore(LocalDateTime.now())) {
                    vo.setStatus(1); // 已过期
                }

                // 格式化时间
                if (vo.getCreateTime() != null) {
                    vo.setCreateTimeStr(formatDateTime(vo.getCreateTime()));
                }
                if (vo.getExpireTime() != null) {
                    vo.setExpireTimeStr(formatDateTime(vo.getExpireTime()));
                }
            }
        }

        PageResult<ShareFileVO> result = PageResult
                .build(records, total, param.getPage(), param.getSize());
        return result;
    }

    // 格式化日期时间
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    @Transactional
    public void receiveShare(String shareCode) {
        // 获取分享信息
        FileShare share = getShareByCode(shareCode);

        // 检查分享状态
        if (share.getStatus() != 0) {
            throw new CustomException("分享已失效");
        }

        // 检查是否已经接收过
        Long userId = RequestUser.getUser().getId();
        LambdaQueryWrapper<ShareReceive> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShareReceive::getShareId, share.getId())
               .eq(ShareReceive::getReceiverId, userId)
               .eq(ShareReceive::getIsDeleted, 0);

        if (shareReceiveMapper.selectOne(wrapper) != null) {
            throw new CustomException("已经接收过此分享");
        }

        // 创建接收记录
        ShareReceive receive = new ShareReceive();
        receive.setShareId(share.getId());
        receive.setReceiverId(userId);
        receive.setCreateTime(LocalDateTime.now());
        receive.setStatus(0);  // 未保存
        receive.setIsDeleted(0);

        shareReceiveMapper.insert(receive);

        // 更新浏览次数
        fileShareMapper.incrementViewCount(share.getId());
    }

    private String generateShareCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
