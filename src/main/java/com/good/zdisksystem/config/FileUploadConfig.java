package com.good.zdisksystem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
@Configuration
@ConfigurationProperties(prefix = "file")
public class FileUploadConfig {

    private String uploadPath;
    private String maxSize;
    private List<String> allowedTypes;

    private static final Logger log = LoggerFactory.getLogger(FileUploadConfig.class);

    @PostConstruct
    public void init() {
        try {
            // 获取完整的上传路径
            Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();

            // 创建目录（如果不存在）
            Files.createDirectories(uploadDir);

            log.info("文件上传目录: {}", uploadDir);
        } catch (IOException e) {
            log.error("创建文件上传目录失败", e);
            throw new RuntimeException("无法创建文件上传目录", e);
        }
    }

    /**
     * 检查文件类型是否允许
     */
    public boolean isAllowedType(String contentType) {
        if (contentType == null || allowedTypes == null) {
            return false;
        }

        return allowedTypes.stream().anyMatch(allowed -> {
            if (allowed.endsWith("/*")) {
                String prefix = allowed.substring(0, allowed.length() - 2);
                return contentType.startsWith(prefix);
            }
            return allowed.equals(contentType);
        });
    }
}
