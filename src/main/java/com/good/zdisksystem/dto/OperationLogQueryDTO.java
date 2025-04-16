// z-disk-system/z-disk-system/src/main/java/com/good/zdisksystem/dto/OperationLogQueryDTO.java
package com.good.zdisksystem.dto;

import lombok.Data;

@Data
public class OperationLogQueryDTO {

    private String userId;

    private String module;

    private String action;

    private String startTime;

    private String endTime;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
