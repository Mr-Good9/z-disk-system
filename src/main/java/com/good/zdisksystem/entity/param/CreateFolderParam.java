package com.good.zdisksystem.entity.param;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CreateFolderParam {
    @NotBlank(message = "文件夹名称不能为空")
    private String name;
    
    private Long parentId;
} 