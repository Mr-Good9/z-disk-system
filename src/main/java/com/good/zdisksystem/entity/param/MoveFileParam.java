package com.good.zdisksystem.entity.param;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class MoveFileParam {
    @NotNull(message = "目标文件夹ID不能为空")
    private Long targetFolderId;
} 