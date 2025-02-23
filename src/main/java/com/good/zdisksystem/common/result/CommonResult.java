package com.good.zdisksystem.common.result;

import com.good.zdisksystem.common.exception.enums.GlobalErrorCodeConstants;
import lombok.Data;
import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * 全局统一结果返回类（通用结果返回）
 */
@Data
public class CommonResult<T> implements Serializable {

    /**
     * 响应状态码
     */
    private Integer code;

    /**
     * 错误提示
     */
    private String msg;

    /**
     * 返回数据
     */
    private T data;

    private CommonResult() {
    }

    private CommonResult(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> CommonResult<T> success(T data) {
        CommonResult<T> result = new CommonResult<>();
        result.data = data;
        result.msg = GlobalErrorCodeConstants.SUCCESS.getMsg();
        result.code = GlobalErrorCodeConstants.SUCCESS.getCode();
        return result;
    }

    /**
     * 成功 —— 自定义返回消息
     */
    public static <T> CommonResult<T> success(String msg) {
        CommonResult<T> result = new CommonResult<>();
        result.msg = msg;
        result.code = GlobalErrorCodeConstants.SUCCESS.getCode();
        return result;
    }

    public static <T> CommonResult<T> error(Integer code, String msg) {
        // 断言-判断是否是错误的
        Assert.isTrue(!GlobalErrorCodeConstants.SUCCESS.getCode().equals(code), "code 必须是错误的！");
        CommonResult<T> result = new CommonResult<>();
        result.msg = msg;
        result.code = code;
        return result;
    }

    public static <T> CommonResult<T> error(String msg) {
        return error(GlobalErrorCodeConstants.FAILURE.getCode(), msg);
    }

    public static <T> CommonResult<T> error(GlobalErrorCodeConstants enums) {
        return error(enums.getCode(), enums.getMsg());
    }

}
