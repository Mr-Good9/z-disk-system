// src/main/java/com/good/zdisksystem/service/impl/MessageAnalysisServiceImpl.java
package com.good.zdisksystem.service.impl;

import com.good.zdisksystem.common.result.PageResult;
import com.good.zdisksystem.dto.MessageBoardDTO;
import com.good.zdisksystem.dto.MessageBoardRequestDTO;
import com.good.zdisksystem.entity.model.File;
import com.good.zdisksystem.entity.model.User;
import com.good.zdisksystem.entity.vo.RecommendationVO;
import com.good.zdisksystem.mapper.FileMapper;
import com.good.zdisksystem.mapper.UserMapper;
import com.good.zdisksystem.service.MessageAnalysisService;
import com.good.zdisksystem.service.MessageBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessageAnalysisServiceImpl implements MessageAnalysisService {

    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<RecommendationVO> analyzeAndRecommend(String content, Long userId) {
        // 1. 提取关键词
        List<String> keywords = extractKeywords(content);

        if (keywords.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 基于关键词查找相关用户
        List<User> relatedUsers = findRelatedUsers(keywords, userId);

        // 3. 基于关键词查找相关资源
        List<File> relatedFiles = findRelatedFiles(keywords);

        // 4. 构建推荐结果
        return buildRecommendations(relatedUsers, relatedFiles);
    }

    // 提取关键词（简单实现）
    private List<String> extractKeywords(String content) {
        // 关键词列表
        Map<String, List<String>> keywordMap = new HashMap<>();
        keywordMap.put("考研", Arrays.asList("考研", "研究生", "考试", "专业课", "英语"));
        keywordMap.put("编程", Arrays.asList("编程", "代码", "开发", "程序", "软件"));
        keywordMap.put("前端", Arrays.asList("前端", "vue", "react", "javascript", "html", "css"));
        keywordMap.put("后端", Arrays.asList("后端", "java", "python", "spring", "数据库"));
        keywordMap.put("设计", Arrays.asList("设计", "UI", "UX", "界面", "交互"));

        Set<String> result = new HashSet<>();

        // 简单匹配
        for (Map.Entry<String, List<String>> entry : keywordMap.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (content.toLowerCase().contains(keyword.toLowerCase())) {
                    result.add(entry.getKey());
                    break;
                }
            }
        }

        return new ArrayList<>(result);
    }

    // 查找相关用户
    private List<User> findRelatedUsers(List<String> keywords, Long currentUserId) {
        // 根据关键词匹配用户上传过的文件名或分享过相关内容的用户
        // 这里简化实现，实际应结合文件标签、内容分析等

        // 模拟查询，实际项目中应该查询数据库
        // 示例：查询分享过与关键词相关文件的用户
        List<User> users = new ArrayList<>();

        // TODO: 实现基于关键词的用户查询
        // 根据用户上传的文件名包含关键词的用户

        // 过滤掉当前用户
        return users.stream()
                .filter(user -> !user.getId().equals(currentUserId))
                .limit(2) // 最多返回2个用户
                .collect(Collectors.toList());
    }

    // 查找相关文件
    private List<File> findRelatedFiles(List<String> keywords) {
        // 根据关键词匹配文件名或内容
        // 这里简化实现，实际应结合文件标签、内容分析等

        // 模拟查询，实际项目中应该查询数据库
        List<File> files = new ArrayList<>();

        // TODO: 实现基于关键词的文件查询
        // 查询文件名包含关键词的共享文件

        return files.stream()
                .limit(3) // 最多返回3个文件
                .collect(Collectors.toList());
    }

    // 构建推荐结果
    private List<RecommendationVO> buildRecommendations(List<User> users, List<File> files) {
        List<RecommendationVO> result = new ArrayList<>();

        // 添加用户推荐
        for (User user : users) {
            RecommendationVO recommendation = RecommendationVO.builder()
                    .id("user-" + user.getId())
                    .type("user")
                    .title(user.getNickname() != null ? user.getNickname() : user.getUsername())
                    .description("分享过相关内容的用户")
                    .avatar(user.getAvatar())
                    .userId(user.getId().toString())
                    .build();

            result.add(recommendation);
        }

        // 添加文件推荐
        for (File file : files) {
            RecommendationVO recommendation = RecommendationVO.builder()
                    .id("file-" + file.getId())
                    .type("resource")
                    .title(file.getName())
                    .description("与你的留言相关的共享文件")
                    .resourceType(file.getType())
                    .resourceId(file.getId().toString())
                    .build();

            result.add(recommendation);
        }

        return result;
    }
}
