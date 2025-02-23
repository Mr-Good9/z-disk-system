package com.good.zdisksystem.entity.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel("验证分享密码参数")
public class VerifySharePasswordParam {
    
    @NotBlank(message = "访问密码不能为空")
    @ApiModelProperty(value = "访问密码", required = true)
    private String password;
} 