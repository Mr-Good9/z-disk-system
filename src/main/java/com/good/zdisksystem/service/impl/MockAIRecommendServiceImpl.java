package com.good.zdisksystem.service.impl;

import com.good.zdisksystem.entity.model.File;
import com.good.zdisksystem.service.AIRecommendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MockAIRecommendServiceImpl implements AIRecommendService {

    // AI风格的推荐理由模板
    private final List<String> reasonTemplates = Arrays.asList(
        "这个{type}文件与您的兴趣模式高度匹配，推荐您查看",
        "基于您最近对{type}类型文件的偏好，智能推荐此文件",
        "我注意到您经常使用{type}文件，这个文档应该符合您的需求",
        "通过分析您的使用习惯，发现这个{type}文件可能对您当前工作有帮助",
        "根据您的行为数据分析，这个{type}文件与您的工作领域相关性很高",
        "您的历史记录显示对此类{type}内容感兴趣，推荐您查看",
        "基于内容相似度算法，发现这个文件与您最近浏览的{type}内容相似",
        "通过深度学习模型分析，预测您会对这个{type}文件感兴趣",
        "考虑到您的专业背景，这个{type}文件可能包含有价值的信息",
        "您的数据模型显示偏好类似内容，这个{type}文件应该符合您的口味"
    );
    
    @Override
    public List<Map<String, Object>> getRecommendations(List<File> allFiles, Map<String, Object> userData) {
        try {
            // 模拟大模型分析过程
            log.info("模拟大模型分析用户数据和文件列表...");
            
            // 提取用户偏好的文件类型
            @SuppressWarnings("unchecked")
            Map<String, Integer> typePreferences = 
                    (Map<String, Integer>) userData.getOrDefault("fileTypePreferences", new HashMap<>());
            
            // 如果没有足够的用户偏好数据，随机选择一些文件
            if (typePreferences.isEmpty() && allFiles.size() > 10) {
                Collections.shuffle(allFiles);
                allFiles = allFiles.subList(0, 10);
            }
            
            // 基于文件类型偏好为每个文件计算一个分数
            Map<File, Double> scoredFiles = new HashMap<>();
            Random random = new Random();
            
            for (File file : allFiles) {
                double score;
                
                // 如果文件类型在用户偏好中，给予更高分数
                if (file.getType() != null && typePreferences.containsKey(file.getType())) {
                    score = 5.0 + (typePreferences.get(file.getType()) / 5.0) + (random.nextDouble() * 2);
                } else {
                    score = 3.0 + (random.nextDouble() * 4);
                }
                
                // 限制分数范围在1-10之间
                score = Math.max(1, Math.min(10, score));
                scoredFiles.put(file, score);
            }
            
            // 选择得分最高的10个文件
            List<Map.Entry<File, Double>> topFiles = scoredFiles.entrySet().stream()
                    .sorted(Map.Entry.<File, Double>comparingByValue().reversed())
                    .limit(10)
                    .collect(Collectors.toList());
            
            // 转换为结果格式
            List<Map<String, Object>> results = new ArrayList<>();
            for (Map.Entry<File, Double> entry : topFiles) {
                File file = entry.getKey();
                Double score = entry.getValue();
                
                Map<String, Object> recommendation = new HashMap<>();
                recommendation.put("fileId", file.getId());
                recommendation.put("score", score);
                
                // 生成个性化的推荐理由
                String fileType = file.getType() != null ? file.getType() : "文档";
                String reasonTemplate = reasonTemplates.get(random.nextInt(reasonTemplates.size()));
                String reason = reasonTemplate.replace("{type}", fileType);
                recommendation.put("reason", reason);
                
                results.add(recommendation);
            }
            
            // 模拟AI延迟 (1-2秒)
            Thread.sleep(1000 + random.nextInt(1000));
            
            return results;
        } catch (Exception e) {
            log.error("模拟AI推荐失败: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
} 