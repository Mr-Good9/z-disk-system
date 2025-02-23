package com.good.zdisksystem.common.exception;


import com.good.zdisksystem.common.exception.enums.GlobalErrorCodeConstants;
import lombok.Data;

/**
 * 自定义异常类
 */
@Data
public class CustomException extends RuntimeException {

    private Integer code;

    private String msg;

    private GlobalErrorCodeConstants errorEnums;

    public CustomException(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public CustomException(GlobalErrorCodeConstants errorEnums) {
        this(errorEnums.getCode(), errorEnums.getMsg());
    }

    public CustomException(String msg) {
        this(GlobalErrorCodeConstants.FAILURE.getCode(), msg);
    }
}
