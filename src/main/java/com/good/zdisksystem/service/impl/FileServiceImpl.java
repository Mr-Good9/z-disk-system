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
import com.good.zdisksystem.entity.vo.ShareFileVO;
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
import java.util.*;
import java.util.stream.Collectors;
import java.text.SimpleDateFormat;
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

    // 添加缓存，但移除缓存以确保每次推荐都是新的
    // @Cacheable(value = "fileRecommendations", key = "#userId", unless = "#result.isEmpty()")
    @Override
    public List<FileVO> getRecommendedShares(Long userId) {
        // 1. 获取用户的文件类型偏好
        Map<String, Integer> userFileTypePreferences = analyzeUserFileTypePreferences(userId);

        // 2. 获取用户最近访问文件的关键词
        List<String> userInterestKeywords = extractUserInterestKeywords(userId);

        // 3. 基于用户偏好查找共享文件并添加随机因素
        List<File> recommendedFiles = findRecommendedSharesWithRandomness(userId, userFileTypePreferences, userInterestKeywords);

        // 4. 转换为VO对象并添加随机化的推荐理由
        return convertToVOWithRandomReason(recommendedFiles, userFileTypePreferences, userInterestKeywords);
    }

    private Map<String, Integer> analyzeUserFileTypePreferences(Long userId) {
        // 分析用户的文件类型偏好
        Map<String, Integer> preferences = new HashMap<>();

        List<File> userFiles = fileMapper.findByUserId(userId);
        for (File file : userFiles) {
            String type = file.getType();
            if (type != null && !type.isEmpty()) {
                preferences.put(type, preferences.getOrDefault(type, 0) + 1);
            }
        }

        return preferences;
    }

    private List<String> extractUserInterestKeywords(Long userId) {
        // 提取用户兴趣关键词
        List<String> keywords = new ArrayList<>();

        // 获取用户文件名中的关键词
        List<File> userFiles = fileMapper.findByUserId(userId);
        for (File file : userFiles) {
            String[] words = file.getName().split("[\\s_\\-.]");
            for (String word : words) {
                if (word.length() > 2) {  // 忽略太短的词
                    keywords.add(word.toLowerCase());
                }
            }
        }

        return keywords;
    }

    private List<File> findRecommendedSharesWithRandomness(Long userId, Map<String, Integer> preferences, List<String> keywords) {
        // 查找所有共享文件 (is_shared = 1)
        List<File> allSharedFiles = fileMapper.findSharedFiles();
        // 如果没有共享文件，返回空列表
        if (allSharedFiles.isEmpty()) {
            return new ArrayList<>();
        }
        // 简单推荐算法：根据文件类型和关键词匹配度排序，并添加随机因素
        Map<File, Double> fileScores = new HashMap<>();
        for (File file : allSharedFiles) {
            // 跳过用户自己的文件
            if (file.getUserId().equals(userId)) {
                continue;
            }
            // 计算文件类型偏好匹配分
            double typeScore = preferences.getOrDefault(file.getType(), 0) * 0.5;
            // 计算关键词匹配分
            double keywordScore = 0;
            String fileName = file.getName().toLowerCase();
            for (String keyword : keywords) {
                if (fileName.contains(keyword)) {
                    keywordScore += 0.3;
                }
            }
            // 添加文件新鲜度因素
            double freshnessScore = 0;
            if (file.getCreateTime() != null) {
                // 如果文件是最近30天内创建的，给予额外加分
                long daysAgo = java.time.Duration.between(
                    file.getCreateTime(),
                    LocalDateTime.now()
                ).toDays();

                if (daysAgo < 30) {
                    freshnessScore = 0.1 * (1 - (daysAgo / 30.0));
                }
            }
            // 添加随机因素 (0-30%的随机波动)
            double randomFactor = 0.7 + (Math.random() * 0.6); // 0.7到1.3之间的随机值
            // 计算总分 - 加入随机因子
            double totalScore = (typeScore + keywordScore + freshnessScore) * randomFactor;
            fileScores.put(file, totalScore);
        }
        // 获取候选集 - 获取多一些的结果用于随机选择
        List<File> candidateFiles = fileScores.entrySet().stream()
                .sorted(Map.Entry.<File, Double>comparingByValue().reversed())
                .limit(Math.min(20, allSharedFiles.size())) // 获取前20个或全部
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        // 最终随机选择10个结果
        List<File> finalSelection = new ArrayList<>();
        int resultSize = Math.min(10, candidateFiles.size());
        // 确保结果集有一定的随机性
        // 先选30%的高分结果以保证质量
        int topPicks = (int) Math.ceil(resultSize * 0.3);
        for (int i = 0; i < Math.min(topPicks, candidateFiles.size()); i++) {
            finalSelection.add(candidateFiles.get(i));
        }
        // 剩余的从候选集中随机选择
        if (candidateFiles.size() > topPicks) {
            List<File> remainingCandidates = new ArrayList<>(candidateFiles.subList(topPicks, candidateFiles.size()));
            Collections.shuffle(remainingCandidates);
            int remaining = resultSize - finalSelection.size();
            for (int i = 0; i < Math.min(remaining, remainingCandidates.size()); i++) {
                finalSelection.add(remainingCandidates.get(i));
            }
        }
        // 最后打乱结果顺序
        Collections.shuffle(finalSelection);
        return finalSelection;
    }

    private List<FileVO> convertToVOWithRandomReason(List<File> files,
                                                    Map<String, Integer> preferences,
                                                    List<String> keywords) {
        List<FileVO> vos = new ArrayList<>();

        // 多样化的推荐理由模板
        List<String> typeReasonTemplates = Arrays.asList(
            "基于您对{type}类型文件的偏好推荐",
            "您经常使用{type}文件，您可能对这个感兴趣",
            "与您收藏的{type}文件类似",
            "为{type}爱好者推荐的精选内容"
        );

        List<String> generalReasonTemplates = Arrays.asList(
            "热门共享文件",
            "最近很多用户都在关注",
            "本周热门推荐",
            "高质量的精选内容",
            "值得一看的共享资源"
        );

        for (File file : files) {
            FileVO vo = convertToVO(file);  // 基本转换

            // 随机选择推荐理由类型
            double reasonType = Math.random();
            String reason = "";
            String fileType = file.getType();

            if (reasonType < 0.4 && preferences.containsKey(fileType) && preferences.get(fileType) > 2) {
                // 类型偏好理由 (40%概率)
                int templateIndex = (int)(Math.random() * typeReasonTemplates.size());
                reason = typeReasonTemplates.get(templateIndex).replace("{type}", fileType);
            }
            else if (reasonType < 0.8) {
                // 关键词匹配理由 (40%概率)
                String fileName = file.getName().toLowerCase();
                List<String> matchedKeywords = keywords.stream()
                        .filter(kw -> fileName.contains(kw.toLowerCase()))
                        .collect(Collectors.toList());

                if (!matchedKeywords.isEmpty()) {
                    // 随机选择一个匹配的关键词
                    String keyword = matchedKeywords.get((int)(Math.random() * matchedKeywords.size()));
                } else {
                    // 如果没有匹配的关键词，使用通用理由
                    int templateIndex = (int)(Math.random() * generalReasonTemplates.size());
                    reason = generalReasonTemplates.get(templateIndex);
                }
            }
            else {
                // 通用理由 (20%概率)
                int templateIndex = (int)(Math.random() * generalReasonTemplates.size());
                reason = generalReasonTemplates.get(templateIndex);
            }

            vo.setReason(reason);
            vos.add(vo);
        }

        return vos;
    }
}
