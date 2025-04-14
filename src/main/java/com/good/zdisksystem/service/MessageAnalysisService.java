// src/main/java/com/good/zdisksystem/service/MessageAnalysisService.java
package com.good.zdisksystem.service;


import com.good.zdisksystem.entity.vo.RecommendationVO;

import java.util.List;

public interface MessageAnalysisService {

    /**
     * 分析消息内容并生成推荐
     * @param content 消息内容
     * @param userId 用户ID
     * @return 推荐列表
     */
    List<RecommendationVO> analyzeAndRecommend(String content, Long userId);
}
