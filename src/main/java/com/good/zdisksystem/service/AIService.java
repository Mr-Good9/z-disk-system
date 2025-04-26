package com.good.zdisksystem.service;

import java.util.List;
import java.util.Map;

/**
 * AI推荐服务接口
 */
public interface AIService {

    /**
     * 获取AI推荐结果
     *
     * @param userData 用户数据信息
     * @param maxResults 最大推荐数量
     * @return 推荐文件ID列表
     */
    List<Long> getRecommendedFiles(Map<String, Object> userData, int maxResults);

    /**
     * 记录用户对推荐的反馈
     *
     * @param userId 用户ID
     * @param fileId 文件ID
     * @param isPositive 是否为正向反馈
     * @return 操作结果
     */
    boolean saveUserFeedback(Long userId, Long fileId, boolean isPositive);
}
