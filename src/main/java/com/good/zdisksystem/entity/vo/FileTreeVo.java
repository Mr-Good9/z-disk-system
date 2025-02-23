package com.good.zdisksystem.entity.vo;

import lombok.Data;
import java.util.List;

@Data
public class FileTreeVo {
    // 文件夹ID
    private Long id;
    
    // 文件夹名称
    private String name;
    
    // 文件夹大小
    private Long totalSize;
    
    // 子文件夹
    private List<FileTreeVo> children;
} 