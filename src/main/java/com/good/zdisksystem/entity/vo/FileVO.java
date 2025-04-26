package com.good.zdisksystem.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class FileVO {
    private Long id;
    private String name;
    private String type;
    private Long size;
    private String path;
    private String owner;
    private Integer status;     // 0-正常 1-已删除
    private Boolean isShared;   // 是否已分享
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    private String ownerName;
    private String createTimeStr;
    private String updateTimeStr;
    private String reason;
    private String recommendSource; // 推荐来源
    private Double recommendScore; // 推荐分数
    private String recommendCategory;
}
