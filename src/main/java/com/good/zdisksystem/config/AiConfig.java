package com.good.zdisksystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import com.good.zdisksystem.service.AIRecommendService;
import com.good.zdisksystem.service.impl.ZhipuAIRecommendServiceImpl;
import com.good.zdisksystem.service.impl.BaiduAIRecommendServiceImpl;
import com.good.zdisksystem.service.impl.MockAIRecommendServiceImpl;

@Configuration
public class AiConfig {

    @Bean
    @Primary
    public AIRecommendService primaryAIRecommendService(ZhipuAIRecommendServiceImpl zhipuService) {
        // 默认使用智普AI作为主要服务
        return zhipuService;
    }
} 