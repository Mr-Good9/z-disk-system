package com.good.zdisksystem.model.dto;

import lombok.Data;

@Data
public class FileQueryParam {
    private String keyword;     // 搜索关键词
    private String owner;       // 所有者
    private String type;        // 文件类型
    private Integer status;     // 文件状态
    private Integer pageNum = 1;
    private Integer pageSize = 10;
} 