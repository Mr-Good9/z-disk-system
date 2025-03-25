package com.good.zdisksystem.service.impl;

import com.good.zdisksystem.entity.param.SystemSettingsDTO;
import com.good.zdisksystem.entity.vo.SystemSettingsVO;
import com.good.zdisksystem.entity.vo.SystemStatisticsVO;
import com.good.zdisksystem.service.SystemService;
import com.good.zdisksystem.service.UserService;
import com.good.zdisksystem.service.FileService;
import com.good.zdisksystem.utils.SystemInfoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SystemServiceImpl implements SystemService {

    private final UserService userService;
    private final FileService fileService;
    private final SystemInfoUtil systemInfoUtil;

    @Override
    public SystemSettingsVO getSettings() {
        SystemSettingsVO vo = new SystemSettingsVO();
        // 从配置文件或数据库获取系统设置
        vo.setDefaultStorageSize(1024L * 1024 * 1024 * 10); // 10GB
        vo.setMaxFileSize(1024L * 1024 * 100); // 100MB
        vo.setMaintenanceMode(false);
        vo.setMinPasswordLength(8);
        vo.setMaxLoginAttempts(5);
        vo.setSystemVersion("1.0.0");
        vo.setLastUpdateTime(systemInfoUtil.getLastUpdateTime());
        return vo;
    }

    @Override
    @Transactional
    public void updateSettings(SystemSettingsDTO settings) {
        // 更新系统设置到配置文件或数据库
        // 可以使用配置中心如Nacos或直接存储在数据库中
    }

    @Override
    public SystemStatisticsVO getStatistics() {
        SystemStatisticsVO vo = new SystemStatisticsVO();
        // 获取系统统计信息
        vo.setTotalUsers(userService.countTotalUsers());
        vo.setActiveUsers(userService.countActiveUsers());
        vo.setTotalFiles(fileService.countTotalFiles());
        vo.setTotalStorage(systemInfoUtil.getTotalStorage());
        vo.setUsedStorage(fileService.getUsedStorage());
        vo.setOnlineUsers(systemInfoUtil.getOnlineUsers());
        vo.setCpuUsage(systemInfoUtil.getCpuUsage());
        vo.setMemoryUsage(systemInfoUtil.getMemoryUsage());
        vo.setSystemUptime(systemInfoUtil.getSystemUptime());
        return vo;
    }

    @Override
    public void clearCache() {
        // 清理系统缓存
        // 可以清理Redis缓存、本地缓存等
    }

    @Override
    public String backupData() {
        // 备份系统数据
        // 可以导出数据库、文件等
        return "backup_" + System.currentTimeMillis() + ".zip";
    }

    @Override
    public void restoreData(String backupFile) {
        // 恢复系统数据
        // 从备份文件恢复数据库、文件等
    }
}
