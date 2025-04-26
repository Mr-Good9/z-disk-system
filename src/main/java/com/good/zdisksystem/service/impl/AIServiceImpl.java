package com.good.zdisksystem.service.impl;

import com.good.zdisksystem.entity.model.File;
import com.good.zdisksystem.mapper.FileMapper;
import com.good.zdisksystem.service.AIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private final FileMapper fileMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<Long> getRecommendedFiles(Map<String, Object> userData, int maxResults) {
        try {
            // 获取用户ID
            Long userId = (Long) userData.get("userId");

            // 获取所有共享文件
            List<File> allSharedFiles = fileMapper.findSharedFiles();
            if (allSharedFiles.isEmpty()) {
                return new ArrayList<>();
            }

            // 过滤掉用户自己的文件
            allSharedFiles = allSharedFiles.stream()
                    .filter(file -> !file.getUserId().equals(userId))
                    .collect(Collectors.toList());

            // 计算每个文件的"AI分数"
            Map<Long, Double> fileScores = calculateFileScores(allSharedFiles, userData);

            // 选择得分最高的文件，并添加一些随机因素
            return selectTopFilesWithDiversity(fileScores, maxResults);
        } catch (Exception e) {
            log.error("AI推荐处理失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean saveUserFeedback(Long userId, Long fileId, boolean isPositive) {
        try {
            // 记录反馈到Redis
            String key = "ai:feedback:" + userId + ":" + fileId;
            Map<String, Object> feedbackData = new HashMap<>();
            feedbackData.put("userId", userId);
            feedbackData.put("fileId", fileId);
            feedbackData.put("isPositive", isPositive);
            feedbackData.put("timestamp", System.currentTimeMillis());

            // 存储到Redis，保留30天
            redisTemplate.opsForValue().set(key, feedbackData, 30, TimeUnit.DAYS);

            // 如果是正向反馈，调整用户偏好
            if (isPositive) {
                adjustUserPreferences(userId, fileId);
            }

            return true;
        } catch (Exception e) {
            log.error("保存推荐反馈失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 计算文件得分
     */
    private Map<Long, Double> calculateFileScores(List<File> files, Map<String, Object> userData) {
        Map<Long, Double> fileScores = new HashMap<>();

        @SuppressWarnings("unchecked")
        Map<String, Integer> typePreferences =
                (Map<String, Integer>) userData.getOrDefault("fileTypePreferences", new HashMap<>());

        @SuppressWarnings("unchecked")
        List<String> keywords =
                (List<String>) userData.getOrDefault("interestKeywords", new ArrayList<>());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> recentActions =
                (List<Map<String, Object>>) userData.getOrDefault("recentActions", new ArrayList<>());

        // 提取最近操作的文件类型
        Set<String> recentFileTypes = new HashSet<>();
        for (Map<String, Object> action : recentActions) {
            if (action.containsKey("fileType")) {
                recentFileTypes.add((String) action.get("fileType"));
            }
        }

        for (File file : files) {
            double score = 0;

            // 基于文件类型的匹配度 (0-5分)
            if (typePreferences.containsKey(file.getType())) {
                score += Math.min(typePreferences.get(file.getType()), 5) * 0.5;
            }

            // 关键词匹配 (每个匹配0.5分)
            String fileName = file.getName().toLowerCase();
            for (String keyword : keywords) {
                if (fileName.contains(keyword.toLowerCase())) {
                    score += 0.5;
                }
            }

            // 最近操作类型匹配 (匹配加2分)
            if (recentFileTypes.contains(file.getType())) {
                score += 2.0;
            }

            // 文件新鲜度加分 (最近30天内，最高1分)
            if (file.getCreateTime() != null) {
                long daysAgo = java.time.Duration.between(
                    file.getCreateTime(),
                    LocalDateTime.now()
                ).toDays();

                if (daysAgo < 30) {
                    score += (1 - (daysAgo / 30.0));
                }
            }

            // 流行度加分 (从Redis获取下载次数)
            String downloadCountKey = "file:downloads:" + file.getId();
            Object downloadCountObj = redisTemplate.opsForValue().get(downloadCountKey);
            if (downloadCountObj != null) {
                int downloadCount = Integer.parseInt(downloadCountObj.toString());
                // 最高加2分
                score += Math.min(downloadCount / 10.0, 2.0);
            }

            // 保存最终分数
            fileScores.put(file.getId(), score);
        }

        return fileScores;
    }

    /**
     * 从候选文件中选择最终推荐结果，确保结果多样性
     */
    private List<Long> selectTopFilesWithDiversity(Map<Long, Double> fileScores, int maxResults) {
        // 获取候选集 - 得分前30个
        List<Map.Entry<Long, Double>> candidateFiles = fileScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(Math.min(30, fileScores.size()))
                .collect(Collectors.toList());

        // 最终结果列表
        List<Long> result = new ArrayList<>();

        // 确保Top 3高分文件被选中
        int topCount = Math.min(3, candidateFiles.size());
        for (int i = 0; i < topCount; i++) {
            result.add(candidateFiles.get(i).getKey());
        }

        // 剩余文件随机选择，增加多样性
        if (candidateFiles.size() > topCount) {
            List<Map.Entry<Long, Double>> remainingCandidates =
                    new ArrayList<>(candidateFiles.subList(topCount, candidateFiles.size()));

            // 根据分数作为权重进行加权随机选择
            int remaining = maxResults - result.size();
            for (int i = 0; i < Math.min(remaining, remainingCandidates.size()); i++) {
                // 加权随机选择
                double totalWeight = 0;
                for (Map.Entry<Long, Double> entry : remainingCandidates) {
                    totalWeight += entry.getValue();
                }

                double randomValue = Math.random() * totalWeight;
                double weightSum = 0;
                int selectedIndex = 0;

                for (int j = 0; j < remainingCandidates.size(); j++) {
                    weightSum += remainingCandidates.get(j).getValue();
                    if (weightSum >= randomValue) {
                        selectedIndex = j;
                        break;
                    }
                }

                // 添加选中的文件
                result.add(remainingCandidates.get(selectedIndex).getKey());
                // 移除已选择的文件
                remainingCandidates.remove(selectedIndex);

                // 如果没有更多候选项，退出循环
                if (remainingCandidates.isEmpty()) {
                    break;
                }
            }
        }

        // 打乱结果顺序，避免用户察觉推荐顺序
        Collections.shuffle(result);
        return result;
    }

    /**
     * 根据正向反馈调整用户偏好
     */
    private void adjustUserPreferences(Long userId, Long fileId) {
        try {
            // 获取文件信息
            File file = fileMapper.selectById(fileId);
            if (file == null) {
                return;
            }

            // 保存文件类型偏好
            String typeKey = "ai:user:preferences:type:" + userId;
            String fileType = file.getType();
            if (fileType != null && !fileType.isEmpty()) {
                redisTemplate.opsForHash().increment(typeKey, fileType, 1);
                // 设置过期时间（90天）
                redisTemplate.expire(typeKey, 90, TimeUnit.DAYS);
            }

            // 保存文件关键词偏好
            String keywordKey = "ai:user:preferences:keywords:" + userId;
            String fileName = file.getName();
            if (fileName != null) {
                // 提取关键词
                String[] parts = fileName.split("[\\s_\\-.]");
                for (String part : parts) {
                    if (part.length() > 2) {
                        redisTemplate.opsForZSet().incrementScore(
                                keywordKey, part.toLowerCase(), 1);
                    }
                }
                // 设置过期时间（90天）
                redisTemplate.expire(keywordKey, 90, TimeUnit.DAYS);
            }
        } catch (Exception e) {
            log.error("调整用户偏好失败: {}", e.getMessage(), e);
        }
    }
}
