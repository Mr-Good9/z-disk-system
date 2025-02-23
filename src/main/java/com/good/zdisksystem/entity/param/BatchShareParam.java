package com.good.zdisksystem.entity.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@ApiModel("批量分享参数")
public class BatchShareParam {
    
    @NotEmpty(message = "文件ID列表不能为空")
    @ApiModelProperty(value = "文件ID列表", required = true)
    private List<Long> fileIds;
    
    @ApiModelProperty("有效期(天)，null表示永久有效")
    private Integer expireDays;
    
    @ApiModelProperty("分享类型(0:私密分享 1:公开分享)")
    private Integer shareType;
    
    @ApiModelProperty("访问密码")
    private String accessCode;
}
