package com.good.zdisksystem.entity.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel("分享文件参数")
public class ShareFileParam {
    
    @NotNull(message = "文件ID不能为空")
    @ApiModelProperty(value = "文件ID", required = true)
    private Long fileId;
    
    @ApiModelProperty("有效期(天)，null表示永久有效")
    private Integer expireDays;
    
    @ApiModelProperty("分享类型(0:私密分享 1:公开分享)")
    private Integer shareType;
    
    @ApiModelProperty("访问密码")
    private String accessCode;
} 