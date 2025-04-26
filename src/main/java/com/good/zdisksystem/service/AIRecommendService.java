package com.good.zdisksystem.service;

import com.good.zdisksystem.entity.model.File;
import java.util.List;
import java.util.Map;

public interface AIRecommendService {
    
    /**
     * 使用大模型为用户推荐文件
     * 
     * @param allFiles 所有可供推荐的文件
     * @param userData 用户数据信息
     * @return 推荐结果列表，包含文件ID和推荐理由
     */
    List<Map<String, Object>> getRecommendations(List<File> allFiles, Map<String, Object> userData);
} 