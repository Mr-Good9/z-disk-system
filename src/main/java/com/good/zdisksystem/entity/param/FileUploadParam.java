package com.good.zdisksystem.entity.param;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@Data
public class FileUploadParam {
    @NotNull(message = "文件不能为空")
    private MultipartFile file;
    
    private Long parentId;
} 