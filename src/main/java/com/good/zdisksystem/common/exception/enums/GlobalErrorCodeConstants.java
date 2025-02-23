package com.good.zdisksystem.common.exception.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 全局错误码枚举
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum GlobalErrorCodeConstants {

    SUCCESS(200, "成功"),
    FAILURE(500, "失败"),
    /**
     * 客户端错误
     */
    BAD_REQUEST(400, "请求参数不正确"),
    UNAUTHORIZED(401, "账号未登录"),
    FORBIDDEN(403, "没有访问权限"),
    NOT_FOUND(404, "请求资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不正确"),
    LOCKED(423, "请求失败，请稍后重试"),
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后重试"),
    SYSTEM_ERROR(500, "系统内部错误"),
    /**
     * 自定义错误码
     * 自定义状态码的范围是在标准HTTP状态码范围之外，即大于599的状态码
     */
    REGISTERED_ERROR(900, "账号注册失败"),
    LOGIN_ERROR(901, "账号登录失败, 请检查账号密码是否正确。"),
    CONTENT_TYPE_ERROR(902, "ContentType不是application/json格式"),
    AUTH_CODE_ERROR(903,"验证码错误"),

    EMAIL_REGISTERED(1002, "邮箱已被注册"),
    PHONE_REGISTERED(1003, "手机号已被注册"),
    INVALID_USERNAME_FORMAT(1004, "用户名格式不正确"),
    INVALID_PASSWORD_FORMAT(1005, "密码格式不正确"),
    INVALID_PHONE_FORMAT(1006, "手机号格式不正确"),
    INVALID_EMAIL_FORMAT(1007, "邮箱格式不正确"),
    CAPTCHA_EXPIRED(1008, "验证码已过期"),
    CAPTCHA_ERROR(1009, "验证码错误"),

    FILE_IS_EMPTY(2001, "文件为空"),
    FILE_TYPE_ERROR(2002, "文件类型错误"),
    FILE_SIZE_EXCEED(2003, "文件大小超过限制"),
    OLD_PASSWORD_ERROR(2004, "旧密码错误"),
    FILE_UPLOAD_ERROR(2005, "文件上传失败"),
    INVALID_MOVE_OPERATION(2006, "无效的移动操作"),
    INVALID_RENAME_OPERATION(2007, "无效的重命名操作"),
    INVALID_DOWNLOAD_OPERATION(2008, "无效的下载操作"),
    FILE_DOWNLOAD_ERROR(2009, "文件下载失败"),
    FILE_NOT_FOUND(2010, "文件不存在"),
    NOT_A_FOLDER(2011, "不是文件夹"),
    FILE_NAME_DUPLICATE(2012, "文件名重复"),
    FILE_PREVIEW_UNSUPPORTED(2013, "文件预览不支持"),
    FILE_PREVIEW_ERROR(2014, "文件预览失败"),
    FILE_DELETE_ERROR(2015, "文件删除失败"),
    SHARE_EXPIRED(2016, "分享链接已过期"),
    SHARE_CANCELLED(2017, "分享已被取消"),
;



    private Integer code;
    private String msg;

}
