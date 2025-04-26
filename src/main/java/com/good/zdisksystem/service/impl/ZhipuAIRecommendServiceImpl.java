package com.good.zdisksystem.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.good.zdisksystem.entity.model.File;
import com.good.zdisksystem.service.AIRecommendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Slf4j
@Service("zhipuAIRecommendService")
@RequiredArgsConstructor
public class ZhipuAIRecommendServiceImpl implements AIRecommendService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${zhipu.api.key}")
    private String apiKey;

    @Value("${zhipu.api.secret}")
    private String apiSecret;

    @Value("${zhipu.api.url}")
    private String apiUrl;

    @Value("${zhipu.api.model:glm-4-flash-250414}")
    private String modelName;

    @Override
    public List<Map<String, Object>> getRecommendations(List<File> allFiles, Map<String, Object> userData) {
        try {
            // 准备用户数据和文件列表
            List<Map<String, Object>> fileList = prepareFileListForAI(allFiles);
            String userPreferences = extractUserPreferencesForAI(userData);

            // 构建发送给智普AI的提示
            String prompt = buildAIPrompt(fileList, userPreferences);

            // 生成JWT令牌
            String token = generateJWT();

            // 调用智普AI API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", token);

            // 准备请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);  // 使用GLM-4-Flash模型

            // 创建消息列表
            List<Map<String, String>> messages = new ArrayList<>();

            // 系统消息
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "你是一个专业的文件推荐系统，根据用户偏好为用户推荐最合适的文件。");
            messages.add(systemMessage);

            // 用户消息
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.add(userMessage);

            requestBody.put("messages", messages);

            // 配置其他参数
            requestBody.put("temperature", 0.7);
            requestBody.put("request_id", UUID.randomUUID().toString());
            requestBody.put("do_sample", true);
            requestBody.put("stream", false); // 非流式输出
            requestBody.put("top_p", 0.8);

            log.debug("智普AI请求体: {}", objectMapper.writeValueAsString(requestBody));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            Map<String, Object> response = restTemplate.postForObject(apiUrl, request, Map.class);

            log.debug("智普AI响应: {}", objectMapper.writeValueAsString(response));

            // 解析智普AI的响应
            return parseZhipuAIResponse(response, allFiles);
        } catch (Exception e) {
            log.error("智普AI推荐失败: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 生成JWT令牌
     */
    private String generateJWT() {
        try {
            // 分割API Key获取id和secret
            String[] keyParts = apiKey.split("\\.");
            if (keyParts.length != 2) {
                throw new IllegalArgumentException("无效的API Key格式，应为: id.secret");
            }
            String id = keyParts[0];
            String secret = keyParts[1];
            
            // 获取当前时间戳（毫秒）
            long now = System.currentTimeMillis();
            long expiry = now + 3600 * 1000; // 1小时后过期
            
            // 创建JWT内容
            Map<String, Object> header = new HashMap<>();
            header.put("alg", "HS256");
            header.put("sign_type", "SIGN");
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("api_key", id); // 注意这里使用id而不是完整的apiKey
            payload.put("exp", expiry);
            payload.put("timestamp", now);
            
            String headerStr = Base64.getUrlEncoder().withoutPadding().encodeToString(
                    objectMapper.writeValueAsString(header).getBytes(StandardCharsets.UTF_8));
            String payloadStr = Base64.getUrlEncoder().withoutPadding().encodeToString(
                    objectMapper.writeValueAsString(payload).getBytes(StandardCharsets.UTF_8));
            
            // 组合header和payload，用于签名
            String signContent = headerStr + "." + payloadStr;
            
            // 使用密钥签名
            Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSHA256.init(secretKey);
            byte[] hash = hmacSHA256.doFinal(signContent.getBytes(StandardCharsets.UTF_8));
            String signature = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(hash)
                    .replace("+", "-")
                    .replace("/", "_");
            
            // 返回完整的JWT（格式：Bearer header.payload.signature）
            return "Bearer " + signContent + "." + signature;
        } catch (Exception e) {
            log.error("生成JWT失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成JWT失败", e);
        }
    }

    private List<Map<String, Object>> prepareFileListForAI(List<File> files) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (File file : files) {
            Map<String, Object> fileMap = new HashMap<>();
            fileMap.put("id", file.getId());
            fileMap.put("name", file.getName());
            fileMap.put("type", file.getType());
            fileMap.put("size", file.getSize());
            fileMap.put("createTime", file.getCreateTime());
            result.add(fileMap);
        }
        return result;
    }

    private String extractUserPreferencesForAI(Map<String, Object> userData) {
        StringBuilder sb = new StringBuilder();

        // 用户文件类型偏好
        @SuppressWarnings("unchecked")
        Map<String, Integer> typePrefs = (Map<String, Integer>) userData.getOrDefault("fileTypePreferences", new HashMap<>());
        if (!typePrefs.isEmpty()) {
            sb.append("用户喜欢的文件类型: ");
            typePrefs.forEach((type, count) -> sb.append(type).append("(").append(count).append("次), "));
            sb.append("\n");
        }

        // 用户关键词偏好
        @SuppressWarnings("unchecked")
        List<String> keywords = (List<String>) userData.getOrDefault("interestKeywords", new ArrayList<>());
        if (!keywords.isEmpty()) {
            sb.append("用户感兴趣的关键词: ").append(String.join(", ", keywords)).append("\n");
        }

        // 最近的用户行为
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> actions = (List<Map<String, Object>>) userData.getOrDefault("recentActions", new ArrayList<>());
        if (!actions.isEmpty()) {
            sb.append("用户最近的操作: \n");
            for (int i = 0; i < Math.min(5, actions.size()); i++) {
                Map<String, Object> action = actions.get(i);
                sb.append("- ").append(action.get("action")).append(" 文件: ")
                  .append(action.get("fileName")).append(" 类型: ")
                  .append(action.get("fileType")).append("\n");
            }
        }

        return sb.toString();
    }

    private String buildAIPrompt(List<Map<String, Object>> files, String userPreferences) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("我需要你充分发挥GLM-4-Flash的分析能力，根据用户的偏好和历史行为，从以下文件列表中推荐最适合该用户的文件。\n\n");
        prompt.append("用户偏好信息:\n").append(userPreferences).append("\n\n");
        prompt.append("可供推荐的文件列表 (最多显示前30个):\n");

        int limit = Math.min(30, files.size());
        for (int i = 0; i < limit; i++) {
            Map<String, Object> file = files.get(i);
            prompt.append(i+1).append(". ID: ").append(file.get("id"))
                  .append(", 名称: ").append(file.get("name"))
                  .append(", 类型: ").append(file.get("type"))
                  .append("\n");
        }

        // 增加多样性要求，确保每次推荐结果有所不同
        prompt.append("\n本次推荐请考虑多样性，包括不同类型的文件、新旧程度、流行度等因素。");
        prompt.append("每次推荐都应确保结果有所不同，可以通过引入适当的随机性来实现。");
        prompt.append("请从以下维度考虑推荐多样性：\n");
        prompt.append("1. 文件类型多样性：尝试推荐不同类型的文件，不要只推荐单一类型\n");
        prompt.append("2. 时间多样性：既包含较新的文件，也包含一些经典的旧文件\n");
        prompt.append("3. 探索与利用平衡：70%的推荐基于用户明确偏好，30%可以是探索性推荐\n");
        prompt.append("4. 主题多样性：尝试覆盖用户可能感兴趣的不同主题\n\n");

        prompt.append("请以JSON格式返回推荐结果，包含以下字段：fileId(文件ID), reason(推荐理由，简明扼要的说明推荐原因), score(推荐分数，1-10), category(推荐类别，可以是'兴趣匹配','探索推荐','热门推荐','新上线'等)。格式如下:\n");
        prompt.append("[\n");
        prompt.append("  {\"fileId\": 123, \"reason\": \"这个文件与用户的兴趣匹配\", \"score\": 8.5, \"category\": \"兴趣匹配\"},\n");
        prompt.append("  ...\n");
        prompt.append("]\n");
        prompt.append("\n请确保输出的是有效的JSON格式，便于系统解析。每个推荐理由应该个性化且不超过30个字。");

        return prompt.toString();
    }

    private List<Map<String, Object>> parseZhipuAIResponse(Map<String, Object> response, List<File> allFiles) {
        try {
            // 从智普AI响应中提取内容
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                log.warn("智普AI响应中无choices字段或为空");
                return Collections.emptyList();
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            if (message == null) {
                log.warn("智普AI响应中无message字段");
                return Collections.emptyList();
            }

            String content = (String) message.get("content");
            if (content == null || content.isEmpty()) {
                log.warn("智普AI响应中content为空");
                return Collections.emptyList();
            }

            // 提取JSON部分
            int startIndex = content.indexOf('[');
            int endIndex = content.lastIndexOf(']') + 1;

            if (startIndex >= 0 && endIndex > startIndex) {
                String jsonContent = content.substring(startIndex, endIndex);
                log.debug("解析到的JSON内容: {}", jsonContent);

                // 解析JSON
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> recommendations = objectMapper.readValue(jsonContent, List.class);

                // 填充文件信息
                for (Map<String, Object> rec : recommendations) {
                    Object fileIdObj = rec.get("fileId");
                    if (fileIdObj == null) {
                        continue;
                    }

                    Long fileId;
                    // 处理数字类型转换
                    if (fileIdObj instanceof Integer) {
                        fileId = ((Integer) fileIdObj).longValue();
                    } else {
                        fileId = Long.valueOf(fileIdObj.toString());
                    }

                    for (File file : allFiles) {
                        if (file.getId().equals(fileId)) {
                            rec.put("fileName", file.getName());
                            rec.put("fileType", file.getType());
                            rec.put("fileSize", file.getSize());
                            // 设置来源为智普大模型
                            rec.put("source", "智普大模型");
                            break;
                        }
                    }
                }

                return recommendations;
            } else {
                log.warn("智普AI响应中未找到有效的JSON内容");
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("解析智普AI响应失败: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
