package com.my.multiroundconversationchatbackend.common;

/**
 * 自定义错误码
 *

 */
public enum ErrorCode {

    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTH_ERROR(40101, "无权限"),
    USER_STATUS_IS_NOT_ACTIVE(40102, "用户状态不可用"),
    VERIFICATION_CODE_WRONG(40103, "验证码错误"),
    USER_QUERY_FAILED(40104, "用户信息查询失败"),
    USER_NOT_LOGIN(40105, "用户未登录"),
    USER_NOT_EXIST(40106, "用户不存在"),
    DUPLICATE_TELEPHONE_NUMBER(40107,"手机号重复"),
    USER_STATUS_CANT_OPERATE(40108,"当前用户状态不能修改"),
    NICK_NAME_EXIST(404109,"该昵称已存在"),

    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    TOO_MANY_REQUEST(40900,"请求过于频繁"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败"),
    API_CALL_ERROR(50002,"api服务异常,请稍后重试");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
