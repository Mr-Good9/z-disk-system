package com.good.zdisksystem.entity.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel("分享统计信息")
public class ShareStats {
    
    @ApiModelProperty("浏览次数")
    private Integer viewCount;
    
    @ApiModelProperty("下载次数")
    private Integer downloadCount;
    
    @ApiModelProperty("保存次数")
    private Integer saveCount;
    
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
    
    @ApiModelProperty("过期时间")
    private LocalDateTime expireTime;
}
