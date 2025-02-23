package com.good.zdisksystem.model.vo;

import lombok.Data;
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
    private Date createTime;
    private Date updateTime;
} 