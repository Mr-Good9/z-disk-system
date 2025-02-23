package com.good.zdisksystem.service;

import com.good.zdisksystem.model.dto.SystemSettingsDTO;
import com.good.zdisksystem.model.vo.SystemSettingsVO;
import com.good.zdisksystem.model.vo.SystemStatisticsVO;

public interface SystemService {
    /**
     * 获取系统设置
     */
    SystemSettingsVO getSettings();
    
    /**
     * 更新系统设置
     */
    void updateSettings(SystemSettingsDTO settings);
    
    /**
     * 获取系统统计信息
     */
    SystemStatisticsVO getStatistics();
    
    /**
     * 清理系统缓存
     */
    void clearCache();
    
    /**
     * 备份系统数据
     */
    String backupData();
    
    /**
     * 恢复系统数据
     */
    void restoreData(String backupFile);
} 