package com.good.zdisksystem.utils;

import org.springframework.stereotype.Component;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import com.sun.management.UnixOperatingSystemMXBean;

@Component
public class SystemInfoUtil {
    
    public String getLastUpdateTime() {
        // 获取系统最后更新时间
        return "2024-03-20 12:00:00";
    }
    
    public Long getTotalStorage() {
        // 获取系统总存储空间
        return 1024L * 1024 * 1024 * 1000; // 1000GB
    }
    
    public Integer getOnlineUsers() {
        // 获取在线用户数
        return 10;
    }
    
    public Double getCpuUsage() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof UnixOperatingSystemMXBean) {
            return ((UnixOperatingSystemMXBean) osBean).getSystemCpuLoad() * 100;
        }
        return 0.0;
    }
    
    public Double getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        return ((double) (totalMemory - freeMemory) / totalMemory) * 100;
    }
    
    public String getSystemUptime() {
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        long uptime = runtimeBean.getUptime();
        long days = uptime / (24 * 60 * 60 * 1000);
        long hours = (uptime % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
        return days + "天" + hours + "小时";
    }
} 