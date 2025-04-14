// src/main/java/com/good/zdisksystem/model/vo/RecommendationVO.java
package com.good.zdisksystem.entity.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecommendationVO {
    private String id;
    private String type; // user / resource
    private String title;
    private String description;
    private String avatar; // 用户头像或资源图标
    private String userId; // 仅user类型有值
    private String resourceType; // 仅resource类型有值
    private String resourceId; // 仅resource类型有值
}
