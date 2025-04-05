package com.good.zdisksystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.good.zdisksystem.common.exception.CustomException;
import com.good.zdisksystem.common.exception.enums.GlobalErrorCodeConstants;
import com.good.zdisksystem.common.result.PageResult;
import com.good.zdisksystem.common.utils.RequestUser;
import com.good.zdisksystem.entity.model.User;
import com.good.zdisksystem.entity.model.File;
import com.good.zdisksystem.entity.model.RecycleBin;
import com.good.zdisksystem.entity.param.RecycleBinParam;
import com.good.zdisksystem.entity.vo.FileTreeVo;
import com.good.zdisksystem.entity.vo.UserFileVO;
import com.good.zdisksystem.mapper.FileMapper;
import com.good.zdisksystem.mapper.UserFileMapper;
import com.good.zdisksystem.mapper.UserMapper;
import com.good.zdisksystem.mapper.RecycleBinMapper;
import com.good.zdisksystem.service.UserFileService;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.RemoveObjectArgs;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;

import javax.annotation.PostConstruct;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserFileServiceImpl implements UserFileService {

    private final MinioClient minioClient;
    private final FileMapper fileMapper;
    private final UserFileMapper userFileMapper;
    private final UserMapper userMapper;
    private final RecycleBinMapper recycleBinMapper;

    @Value("${minio.bucket}")
    private String bucket;

    @PostConstruct
    public void init() {
        try {
            log.info("正在初始化MinIO...");
            // 检查存储桶是否存在，不存在则创建
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!bucketExists) {
                log.info("存储桶 {} 不存在，正在创建...", bucket);
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("存储桶 {} 创建成功", bucket);
            } else {
                log.info("存储桶 {} 已存在", bucket);
            }
        } catch (Exception e) {
            log.error("MinIO初始化失败: {}", e.getMessage(), e);
            throw new CustomException(GlobalErrorCodeConstants.SYSTEM_ERROR.getCode(),
                    "MinIO初始化失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public File uploadFile(MultipartFile file, Long parentId) {
        // 获取当前用户
        User user = RequestUser.getUser();

        // 检查父文件夹是否存在
        if (parentId != null && parentId != 0) {
            File parentFolder = checkAndGetFolder(parentId);
            if (!parentFolder.getUserId().equals(user.getId())) {
                throw new CustomException(GlobalErrorCodeConstants.FORBIDDEN);
            }
        }

        // 获取文件扩展名和MIME类型
        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.hasText(originalFilename) ?
                FilenameUtils.getExtension(originalFilename) : "";
        String contentType = file.getContentType();

        // 检查是否为视频文件
        boolean isVideo = isVideoFile(extension, contentType);

        // 生成文件存储路径
        String storagePath = generateStoragePath(user.getId(), extension);

        try {
            // 上传文件到MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(storagePath)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // 创建文件记录
            File userFile = new File();
            userFile.setUserId(user.getId());
            userFile.setName(originalFilename);
            userFile.setPath(storagePath);
            userFile.setType(extension.toLowerCase());
            userFile.setSize(file.getSize());
            userFile.setParentId(parentId != null ? parentId : 0L);
            userFile.setIsFolder(0);

            // 设置是否为视频文件
            userFile.setIsVideo(isVideo ? 1 : 0);

            userFile.setCreateTime(LocalDateTime.now());
            userFile.setUpdateTime(LocalDateTime.now());
            userFile.setIsDeleted(0);

            fileMapper.insert(userFile);
            return userFile;

        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new CustomException(GlobalErrorCodeConstants.FILE_UPLOAD_ERROR);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public File createFolder(String name, Long parentId) {
        // 获取当前用户
        User user = RequestUser.getUser();

        // 检查父文件夹是否存在
        if (parentId != null && parentId != 0) {
            File parentFolder = checkAndGetFolder(parentId);
            // 检查是否有权限在该文件夹下创建
            if (!parentFolder.getUserId().equals(user.getId())) {
                throw new CustomException(GlobalErrorCodeConstants.FORBIDDEN);
            }
        }

        // 检查文件夹名是否已存在
        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<File>()
                .eq(File::getUserId, user.getId())
                .eq(File::getParentId, parentId != null ? parentId : 0L)
                .eq(File::getName, name)
                .eq(File::getIsDeleted, 0);

        if (fileMapper.selectCount(wrapper) > 0) {
            throw new CustomException(GlobalErrorCodeConstants.FILE_NAME_DUPLICATE);
        }

        // 创建文件夹记录
        File folder = new File();
        folder.setUserId(user.getId());
        folder.setName(name);
        folder.setIsFolder(1);  // 1表示是文件夹
        folder.setParentId(parentId != null ? parentId : 0L);
        folder.setSize(0L);  // 文件夹初始大小为0
        folder.setCreateTime(LocalDateTime.now());
        folder.setUpdateTime(LocalDateTime.now());
        folder.setIsDeleted(0);

        // 保存到数据库
        fileMapper.insert(folder);

        return folder;
    }

    @Override
    public List<File> getFileList(Long parentId, Integer isShared) {
        // 获取当前用户
        User user = RequestUser.getUser();

        // 构建查询条件
        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<File>()
                .eq(File::getUserId, user.getId())
                .eq(File::getParentId, parentId != null ? parentId : 0L)
                .eq(File::getIsDeleted, 0)
                .eq(isShared != null, File::getIsShared, isShared)
                .orderByAsc(File::getIsFolder)
                .orderByDesc(File::getUpdateTime);

        List<File> files = fileMapper.selectList(wrapper);

        // 更新文件夹大小
        files.stream()
                .filter(file -> file.getIsFolder() == 1)
                .forEach(folder -> folder.setSize(calculateFolderSize(folder.getId())));

        return files;
    }

    @Override
    public List<FileTreeVo> getFolderTree() {
        // 获取当前用户
        User user = RequestUser.getUser();

        // 获取所有文件夹
        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<File>()
                .eq(File::getUserId, user.getId())
                .eq(File::getIsFolder, 1)
                .eq(File::getIsDeleted, 0)
                .orderByAsc(File::getParentId);

        List<File> folders = fileMapper.selectList(wrapper);

        // 构建文件夹树
        return buildFolderTree(folders, 0L);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void renameFile(Long fileId, String newName) {
        // 获取文件信息并检查权限
        File file = checkAndGetFile(fileId);

        // 检查新文件名是否重复
        checkNameDuplicate(newName, file.getParentId(), file.getUserId());

        // 更新文件名
        file.setName(newName);
        file.setUpdateTime(LocalDateTime.now());
        fileMapper.updateById(file);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveFile(Long fileId, Long targetFolderId) {
        // 获取文件信息并检查权限
        File file = checkAndGetFile(fileId);

        // 检查目标文件夹
        if (targetFolderId != 0) {
            File targetFolder = checkAndGetFolder(targetFolderId);
            if (!targetFolder.getUserId().equals(file.getUserId())) {
                throw new CustomException(GlobalErrorCodeConstants.FORBIDDEN);
            }
        }

        // 检查是否移动到自己的子文件夹
        if (file.getIsFolder() == 1 && isSubFolder(fileId, targetFolderId)) {
            throw new CustomException(GlobalErrorCodeConstants.INVALID_MOVE_OPERATION);
        }

        // 检查文件名是否重复
        checkNameDuplicate(file.getName(), targetFolderId, file.getUserId());

        // 更新父文件夹ID
        file.setParentId(targetFolderId);
        file.setUpdateTime(LocalDateTime.now());
        fileMapper.updateById(file);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(Long fileId) {
        // 获取文件信息并检查权限
        File file = checkAndGetFile(fileId);
        Long userId = RequestUser.getUser().getId();

        if (!file.getUserId().equals(userId)) {
            throw new CustomException(GlobalErrorCodeConstants.FORBIDDEN);
        }

        try {
            log.info("开始删除文件: fileId={}, fileName={}", fileId, file.getName());

            // 1. 更新文件表状态
            file.setIsDeleted(1);
            file.setDeleteTime(LocalDateTime.now());
            file.setUpdateTime(LocalDateTime.now());
            fileMapper.updateById(file);

            // 2. 创建回收站记录
            RecycleBin recycleBin = new RecycleBin();
            recycleBin.setFileId(fileId);
            recycleBin.setUserId(userId);
            recycleBin.setOriginalPath(file.getPath());
            recycleBin.setDeleteTime(LocalDateTime.now());
            recycleBin.setExpireTime(LocalDateTime.now().plusDays(30));
            recycleBinMapper.insert(recycleBin);

            log.info("文件已移入回收站: fileId={}", fileId);

            // 如果是文件夹，递归处理子文件
            if (file.getIsFolder() == 1) {
                List<File> children = getAllChildren(fileId);
                log.info("找到子文件数量: {}", children.size());

                for (File child : children) {
                    // 更新子文件状态
                    child.setIsDeleted(1);
                    child.setDeleteTime(LocalDateTime.now());
                    child.setUpdateTime(LocalDateTime.now());
                    fileMapper.updateById(child);

                    // 为子文件创建回收站记录
                    RecycleBin childRecycleBin = new RecycleBin();
                    childRecycleBin.setFileId(child.getId());
                    childRecycleBin.setUserId(userId);
                    childRecycleBin.setOriginalPath(child.getPath());
                    childRecycleBin.setDeleteTime(LocalDateTime.now());
                    childRecycleBin.setExpireTime(LocalDateTime.now().plusDays(30));
                    recycleBinMapper.insert(childRecycleBin);

                    log.info("子文件已移入回收站: fileId={}, fileName={}",
                            child.getId(), child.getName());
                }
            }

            log.info("文件删除完成: fileId={}", fileId);
        } catch (Exception e) {
            log.error("文件删除失败: fileId={}, error={}", fileId, e.getMessage(), e);
            throw new CustomException(GlobalErrorCodeConstants.FILE_DELETE_ERROR);
        }
    }

    // 获取所有子文件和文件夹
    private List<File> getAllChildren(Long folderId) {
        List<File> result = new ArrayList<>();
        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(File::getParentId, folderId)
                .eq(File::getIsDeleted, 0);  // 只获取未删除的文件

        List<File> children = fileMapper.selectList(wrapper);
        for (File child : children) {
            result.add(child);
            if (child.getIsFolder() == 1) {
                result.addAll(getAllChildren(child.getId()));
            }
        }
        return result;
    }

    @Override
    public byte[] downloadFile(Long fileId) {
        File file = checkAndGetFile(fileId);
        // 检查文件是否存在
        if (file.getIsDeleted() == 1) {
            throw new CustomException(GlobalErrorCodeConstants.FILE_NOT_FOUND);
        }
        
        // 检查是否为文件夹
        if (file.getIsFolder() == 1) {
            throw new CustomException(GlobalErrorCodeConstants.FILE_DOWNLOAD_ERROR.getCode(), 
                    "文件夹不支持直接下载");
        }

        try {
            // 从MinIO下载文件
            GetObjectResponse response = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(file.getPath())
                            .build()
            );

            // 使用 IOUtils 读取文件内容
            try (InputStream inputStream = response) {
                return IOUtils.toByteArray(inputStream);
            }
        } catch (Exception e) {
            log.error("文件下载失败", e);
            throw new CustomException(GlobalErrorCodeConstants.FILE_DOWNLOAD_ERROR.getCode(),
                    "文件下载失败: " + e.getMessage());
        }
    }

    @Override
    public List<File> searchFiles(String name) {
        // 获取当前用户
        User user = RequestUser.getUser();

        // 构建查询条件
        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<File>()
                .eq(File::getUserId, user.getId())
                .eq(File::getIsDeleted, 0)
                .like(StringUtils.hasText(name), File::getName, name)
                .orderByDesc(File::getUpdateTime);

        return fileMapper.selectList(wrapper);
    }

    // 检查文件是否支持预览
    private boolean isPreviewable(File file) {
        String type = file.getType();
        return type != null && (
                type.equalsIgnoreCase("jpg") ||
                        type.equalsIgnoreCase("png") ||
                        type.equalsIgnoreCase("gif") ||
                        type.equalsIgnoreCase("pdf")
        );
    }

    // 添加视频文件识别方法
    private boolean isVideoFile(String extension, String contentType) {
        // 通过扩展名判断
        if (extension != null) {
            extension = extension.toLowerCase();
            return extension.equals("mp4") || extension.equals("avi") ||
                   extension.equals("mov") || extension.equals("wmv") ||
                   extension.equals("flv") || extension.equals("mkv") ||
                   extension.equals("webm") || extension.equals("3gp") ||
                   extension.equals("m4v") || extension.equals("mpg") ||
                   extension.equals("mpeg");
        }

        // 通过MIME类型判断
        if (contentType != null) {
            return contentType.startsWith("video/");
        }

        return false;
    }

    // 私有辅助方法

    /**
     * 检查并获取文件信息
     */
    @Override
    public File checkAndGetFile(Long fileId) {
        File file = fileMapper.selectById(fileId);
        if (file == null || file.getIsDeleted() == 1) {
            throw new CustomException(GlobalErrorCodeConstants.FILE_NOT_FOUND);
        }

        // 检查文件所有权
        User user = RequestUser.getUser();
        if (!file.getUserId().equals(user.getId())) {
            throw new CustomException(GlobalErrorCodeConstants.FORBIDDEN);
        }

        return file;
    }

    /**
     * 检查并获取文件夹信息
     */
    private File checkAndGetFolder(Long folderId) {
        File folder = fileMapper.selectById(folderId);
        if (folder == null || folder.getIsDeleted() == 1) {
            throw new CustomException(GlobalErrorCodeConstants.FILE_NOT_FOUND);
        }
        if (folder.getIsFolder() != 1) {
            throw new CustomException(GlobalErrorCodeConstants.NOT_A_FOLDER);
        }
        return folder;
    }

    /**
     * 检查文件名是否重复
     */
    private void checkNameDuplicate(String name, Long parentId, Long userId) {
        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<File>()
                .eq(File::getUserId, userId)
                .eq(File::getParentId, parentId)
                .eq(File::getName, name)
                .eq(File::getIsDeleted, 0);

        if (fileMapper.selectCount(wrapper) > 0) {
            throw new CustomException(GlobalErrorCodeConstants.FILE_NAME_DUPLICATE);
        }
    }

    /**
     * 生成文件存储路径
     */
    private String generateStoragePath(Long userId, String extension) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return String.format("%d/%s/%s.%s",
                userId,
                LocalDateTime.now().toLocalDate(),
                uuid,
                extension);
    }

    /**
     * 检查是否是子文件夹
     */
    private boolean isSubFolder(Long parentId, Long childId) {
        if (parentId.equals(childId)) {
            return true;
        }

        File child = fileMapper.selectById(childId);
        if (child == null || child.getParentId() == 0) {
            return false;
        }

        return isSubFolder(parentId, child.getParentId());
    }

    /**
     * 构建文件夹树
     */
    private List<FileTreeVo> buildFolderTree(List<File> folders, Long parentId) {
        List<FileTreeVo> tree = new ArrayList<>();

        for (File folder : folders) {
            if (folder.getParentId().equals(parentId)) {
                FileTreeVo node = new FileTreeVo();
                node.setId(folder.getId());
                node.setName(folder.getName());
                node.setChildren(buildFolderTree(folders, folder.getId()));

                // 计算文件夹大小
                node.setTotalSize(calculateFolderSize(folder.getId()));

                tree.add(node);
            }
        }

        return tree;
    }

    /**
     * 计算文件夹大小
     */
    private Long calculateFolderSize(Long folderId) {
        // 获取文件夹下所有未删除的文件和子文件夹
        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<File>()
                .eq(File::getParentId, folderId)
                .eq(File::getIsDeleted, 0);

        List<File> children = fileMapper.selectList(wrapper);
        long totalSize = 0;

        for (File child : children) {
            if (child.getIsFolder() == 1) {
                // 递归计算子文件夹大小
                totalSize += calculateFolderSize(child.getId());
            } else {
                totalSize += child.getSize();
            }
        }

        return totalSize;
    }

    @Override
    public PageResult<UserFileVO> getRecycleBinFiles(RecycleBinParam param) {
        Long userId = RequestUser.getUser().getId();
        int offset = (param.getPage() - 1) * param.getSize();

        String searchPattern = null;
        if (StringUtils.hasText(param.getKeyword())) {
            searchPattern = param.getKeyword();
        }

        log.info("开始查询回收站文件: userId={}, keyword={}, offset={}, size={}",
                userId, searchPattern, offset, param.getSize());

        // 先查询回收站表确认数据
        LambdaQueryWrapper<RecycleBin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecycleBin::getUserId, userId);
        List<RecycleBin> recycleBins = recycleBinMapper.selectList(wrapper);
        log.info("回收站表中的记录数: {}", recycleBins.size());
        if (!recycleBins.isEmpty()) {
            log.info("回收站第一条记录: fileId={}, userId={}, path={}",
                    recycleBins.get(0).getFileId(),
                    recycleBins.get(0).getUserId(),
                    recycleBins.get(0).getOriginalPath());
        }

        // 查询文件列表
        List<UserFileVO> records = recycleBinMapper.getRecycleBinFiles(
                userId, searchPattern, offset, param.getSize());
        Long total = recycleBinMapper.getRecycleBinCount(userId, searchPattern);

        log.info("查询回收站文件结果: records={}, total={}",
                records != null ? records.size() : 0, total);

        // 处理文件信息
        if (records != null && !records.isEmpty()) {
            for (UserFileVO vo : records) {
                if (vo.getDeleteTime() != null) {
                    vo.setDeleteTimeStr(formatDateTime(vo.getDeleteTime()));
                }
            }
        }

        PageResult<UserFileVO> result = PageResult.
                build(records, total, param.getPage(), param.getSize());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreFile(Long fileId) {
        Long userId = RequestUser.getUser().getId();

        // 检查文件是否存在
        File file = userFileMapper.selectById(fileId);
        if (file == null || file.getIsDeleted() == 0) {
            throw new CustomException("文件不存在或已恢复");
        }

        // 检查权限
        if (!file.getUserId().equals(userId)) {
            throw new CustomException(GlobalErrorCodeConstants.FORBIDDEN);
        }

        try {
            log.info("开始恢复文件: fileId={}, fileName={}", fileId, file.getName());

            // 1. 恢复文件状态
            file.setIsDeleted(0);
            file.setDeleteTime(null);
            file.setUpdateTime(LocalDateTime.now());
            fileMapper.updateById(file);

            // 2. 删除回收站记录
            LambdaQueryWrapper<RecycleBin> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RecycleBin::getFileId, fileId)
                    .eq(RecycleBin::getUserId, userId);
            recycleBinMapper.delete(wrapper);

            // 如果是文件夹，递归恢复子文件
            if (file.getIsFolder() == 1) {
                // 获取所有被删除的子文件
                LambdaQueryWrapper<File> childWrapper = new LambdaQueryWrapper<>();
                childWrapper.eq(File::getParentId, fileId)
                        .eq(File::getIsDeleted, 1);
                List<File> children = fileMapper.selectList(childWrapper);

                log.info("找到被删除的子文件数量: {}", children.size());

                for (File child : children) {
                    // 恢复子文件
                    restoreFile(child.getId());
                }
            }

            log.info("文件恢复完成: fileId={}", fileId);
        } catch (Exception e) {
            log.error("文件恢复失败: fileId={}, error={}", fileId, e.getMessage(), e);
            throw new CustomException("恢复文件失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFileCompletely(Long fileId) {
        Long userId = RequestUser.getUser().getId();

        // 检查文件是否存在
        File file = userFileMapper.selectById(fileId);
        if (file == null) {
            throw new CustomException("文件不存在");
        }

        // 检查权限
        if (!file.getUserId().equals(userId)) {
            throw new CustomException(GlobalErrorCodeConstants.FORBIDDEN);
        }

        try {
            log.info("开始彻底删除文件: fileId={}, fileName={}", fileId, file.getName());

            // 1. 标记回收站记录为彻底删除状态
            LambdaQueryWrapper<RecycleBin> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RecycleBin::getFileId, fileId)
                    .eq(RecycleBin::getUserId, userId);

            RecycleBin recycleBin = recycleBinMapper.selectOne(wrapper);
            if (recycleBin != null) {
                recycleBin.setStatus(0);  // 设置为彻底删除状态
                recycleBin.setUpdateTime(LocalDateTime.now());
                recycleBinMapper.updateById(recycleBin);
                log.info("回收站记录已标记为彻底删除: fileId={}", fileId);
            }

            // 2. 从MinIO中删除文件
            if (file.getIsFolder() != 1) {
                try {
                    minioClient.removeObject(
                            RemoveObjectArgs.builder()
                                    .bucket(bucket)
                                    .object(file.getPath())
                                    .build()
                    );
                    log.info("MinIO文件已删除: path={}", file.getPath());
                } catch (Exception e) {
                    log.error("MinIO文件删除失败: {}", e.getMessage(), e);
                }
            }

            // 3. 如果是文件夹，递归处理子文件
            if (file.getIsFolder() == 1) {
                LambdaQueryWrapper<File> childWrapper = new LambdaQueryWrapper<>();
                childWrapper.eq(File::getParentId, fileId)
                        .eq(File::getIsDeleted, 1);
                List<File> children = fileMapper.selectList(childWrapper);

                log.info("找到子文件数量: {}", children.size());
                for (File child : children) {
                    deleteFileCompletely(child.getId());
                }
            }

            // 4. 更新用户存储空间使用量
            if (file.getIsFolder() != 1) {
                User user = userMapper.selectById(userId);
                user.setStorageUsed(user.getStorageUsed() - file.getSize());
                userMapper.updateById(user);
                log.info("用户存储空间已更新: userId={}, size={}", userId, file.getSize());
            }

            log.info("文件彻底删除完成: fileId={}", fileId);
        } catch (Exception e) {
            log.error("彻底删除文件失败: fileId={}, error={}", fileId, e.getMessage(), e);
            throw new CustomException("删除文件失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearRecycleBin() {
        Long userId = RequestUser.getUser().getId();

        // 获取回收站中的所有文件
        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(File::getUserId, userId)
                .eq(File::getIsDeleted, 1);

        List<File> files = userFileMapper.selectList(wrapper);

        for (File file : files) {
            deleteFileCompletely(file.getId());
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public String getPreviewUrl(Long fileId) {
        File file = checkAndGetFile(fileId);
        log.info("获取文件预览URL: fileId={}, fileName={}, type={}, isVideo={}",
                fileId, file.getName(), file.getType(), file.getIsVideo());

        // 检查是否为视频文件并使用专门的视频预览方法
        if (file.getIsVideo() != null && file.getIsVideo() == 1) {
            log.info("通过isVideo字段识别到视频文件");
            return getVideoPreviewUrl(fileId);
        }

        // 兼容旧数据，根据文件扩展名判断
        if (file.getType() != null && isVideoFile(file.getType(), null)) {
            log.info("通过文件扩展名识别到视频文件");
            return getVideoPreviewUrl(fileId);
        }

        // 确保文件类型支持预览
        if (!isPreviewable(file)) {
            log.warn("不支持预览的文件类型: {}", file.getType());
            throw new CustomException(GlobalErrorCodeConstants.FILE_PREVIEW_UNSUPPORTED.getCode(),
                    "不支持预览该类型的文件: " + file.getType());
        }

        // 生成预览URL
        try {
            String previewUrl = generatePreviewUrl(file);
            log.info("成功生成预览URL");
            return previewUrl;
        } catch (Exception e) {
            log.error("生成预览URL失败: {}", e.getMessage(), e);
            throw new CustomException(GlobalErrorCodeConstants.FILE_PREVIEW_ERROR.getCode(),
                    "生成预览URL失败: " + e.getMessage());
        }
    }

    // 改进预览URL生成方法，添加详细的异常处理
    private String generatePreviewUrl(File file) {
        try {
            log.info("开始生成预览URL: filePath={}", file.getPath());
            // 生成一个有效期为7天的预签名URL
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucket)
                            .object(file.getPath())
                            .method(Method.GET)
                            .expiry(7, TimeUnit.DAYS)
                            .build()
            );
            log.info("预览URL生成成功");
            return url;
        } catch (Exception e) {
            log.error("生成预览URL时发生异常: {}", e.getMessage(), e);
            throw new CustomException(GlobalErrorCodeConstants.FILE_PREVIEW_ERROR.getCode(),
                    "生成预览URL失败: " + e.getMessage());
        }
    }

    // 改进视频预览URL方法，添加更多日志
    @Override
    public String getVideoPreviewUrl(Long fileId) {
        // 获取文件信息
        File file = checkAndGetFile(fileId);
        log.info("尝试获取视频预览URL: fileId={}, fileName={}, type={}",
                file.getId(), file.getName(), file.getType());

        // 检查是否为视频文件
        boolean isVideo = false;

        // 先检查 isVideo 字段
        if (file.getIsVideo() != null && file.getIsVideo() == 1) {
            isVideo = true;
            log.info("通过isVideo字段确认为视频文件");
        } else {
            // 根据扩展名判断
            String extension = file.getType() != null ? file.getType().toLowerCase() : "";
            isVideo = isVideoFile(extension, null);
            log.info("通过文件扩展名判断是否为视频文件: extension={}, isVideo={}", extension, isVideo);
        }

        if (!isVideo) {
            log.warn("尝试获取非视频文件的视频预览URL: fileId={}, type={}", fileId, file.getType());
            throw new CustomException(GlobalErrorCodeConstants.FILE_PREVIEW_UNSUPPORTED.getCode(),
                    "该文件不是视频文件，无法预览");
        }

        try {
            log.info("正在生成视频预览URL: fileId={}, fileName={}, path={}",
                    file.getId(), file.getName(), file.getPath());

            // 生成预览URL，设置有效期为2小时
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucket)
                            .object(file.getPath())
                            .method(Method.GET)
                            .expiry(2, TimeUnit.HOURS)
                            .build()
            );

            log.info("成功生成视频预览URL: {}", url);
            return url;
        } catch (Exception e) {
            log.error("生成视频预览URL失败: {}", e.getMessage(), e);
            throw new CustomException(GlobalErrorCodeConstants.FILE_PREVIEW_ERROR.getCode(),
                    "生成视频预览URL失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream getFileInputStream(Long fileId) {
        File file = checkAndGetFile(fileId);
        // 检查文件是否存在
        if (file.getIsDeleted() == 1) {
            throw new CustomException(GlobalErrorCodeConstants.FILE_NOT_FOUND);
        }
        
        // 检查是否为文件夹
        if (file.getIsFolder() == 1) {
            throw new CustomException(GlobalErrorCodeConstants.FILE_DOWNLOAD_ERROR.getCode(), 
                    "文件夹不支持直接下载");
        }

        try {
            log.info("正在获取文件流: fileId={}, path={}", fileId, file.getPath());
            // 从MinIO获取文件流
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(file.getPath())
                            .build()
            );
        } catch (Exception e) {
            log.error("获取文件流失败: {}", e.getMessage(), e);
            throw new CustomException(GlobalErrorCodeConstants.FILE_DOWNLOAD_ERROR.getCode(),
                    "获取文件流失败: " + e.getMessage());
        }
    }
}
