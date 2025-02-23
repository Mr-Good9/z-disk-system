package com.good.zdisksystem.entity.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 分享列表查询参数
 */
@Data
@ApiModel(description = "分享列表查询参数")
public class ShareListParam {
    
    @ApiModelProperty("分享类型：created-我的分享，received-收到的分享")
    private String type = "created";
    
    @ApiModelProperty("页码")
    private Integer page = 1;
    
    @ApiModelProperty("每页大小")
    private Integer size = 20;
    
    @ApiModelProperty("搜索关键词")
    private String keyword;

    @ApiModelProperty("排序字段")
    private String orderBy = "createTime";

    @ApiModelProperty("排序方式：asc-升序，desc-降序")
    private String orderDirection = "desc";
} 