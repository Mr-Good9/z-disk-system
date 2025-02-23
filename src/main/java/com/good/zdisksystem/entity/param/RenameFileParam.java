package com.good.zdisksystem.entity.param;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class RenameFileParam {
    @NotBlank(message = "新文件名不能为空")
    private String newName;
} 