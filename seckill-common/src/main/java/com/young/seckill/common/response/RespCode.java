package com.young.seckill.common.response;

import lombok.Getter;

@Getter
public enum RespCode {

    // 系统级别CODE
    SUCCESS(2000, "操作成功"),
    UN_AUTHORIZATION(4001, "未授权，请登录重试"),
    UN_FORBIDDEN(4003, "无权限访问"),
    ERROR(4004, "网站数据存在问题，请联系管理员处理"),
    REQUEST_METHOD_ERROR(4005, "请求方式错误"),
    INTERNAL_ERROR(5000, "服务器内部出错"),
    RETRY_LATER(5004, "请稍后再试"),
    PARAMS_INVALID(2001, "参数错误"),
    SERVER_EXCEPTION(2002, "服务器异常"),
    DATA_PARSE_FAILED(2003, "数据解析失败"),


    // Jackson序列化发序列化CODE
    SERIALIZATION_FAILED(4014, "Jackson 序列化对象失败"),
    SERIALIZATION_WITH_CAUSE_FAILED(4015, "Jackson 序列化对象失败"),
    DESERIALIZATION_FAILED(4024, "Jackson deserialize for class [%s] failed."),
    DESERIALIZATION_WITH_CAUSE_FAILED(4025, "Jackson deserialize for class [%s] failed, cause error[%s]."),


    // USER服务级别CODE
    USERNAME_IS_NULL(2103, "用户名不能为空"),
    PASSWORD_IS_NULL(2104, "密码不能为空"),
    USERNAME_IS_ERROR(2105, "用户名错误"),
    PASSWORD_IS_ERROR(2106, "密码错误"),
    USER_NOT_LOGIN(2107, "用户未登录"),
    TOKEN_EXPIRE(2108, "Token失效"),
    USER_ENV_RISK(2109, "用户当前使用环境有风险"),


    // ACTIVITY服务级别CODE
    ACTIVITY_EXISTS(2201, "当前活动已存在"),
    ACTIVITY_NOT_EXISTS(2202, "当前活动不存在"),


    // GOODS服务级别CODE
    GOODS_EXISTS(2301, "当前商品已存在"),
    GOODS_NOT_EXISTS(2302, "当前商品不存在"),
    GOODS_UN_PUBLISH(2303, "商品未上线"),
    GOODS_OFFLINE(2304, "商品已下架"),
    STOCK_LT_ZERO(2305, "库存不足"),
    STOCK_IS_NULL(2305, "库存为空"),
    GOODS_FINISH(2306, "商品已售罄"),


    // ORDER服务级别CODE
    BEYOND_LIMIT_NUM(2401, "下单数量不能超过限购数量"),
    SAVE_ORDER_FAILED(2402, "下单失败"),
    REDUNDANT_SUBMIT(2403, "请勿重复下单"),
    ORDER_TOKEN_UNAVAILABLE(2404, "暂无可用库存"),
    ORDER_TASK_ID_INVALID(2405, "下单编号错误/无效"),


    // 分库分表级别CODE
    BUCKET_INIT_STOCK_ERROR(2601, "分桶总库存错误"),
    BUCKET_AVAILABLE_STOCK_ERROR(2602, "分桶可用库存错误"),
    BUCKET_STOCK_ERROR(2603, "分桶库存错误"),
    BUCKET_GOODS_ID_ERROR(2604, "秒杀商品id错误"),
    BUCKET_CREATE_FAILED(2605, "库存分桶失败"),
    BUCKET_CLOSED_FAILED(2606, "关闭分桶失败"),
    BUCKET_SOLD_BEYOND_TOTAL(2607, "已售商品数量大于要设置的总库存"),
    FREQUENTLY_ERROR(2608, "操作频繁，稍后再试");


    private final Integer code;
    private final String  message;

    RespCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }


    @Override
    public String toString() {
        return "{" + "code: " + code + ", message: \"" + message + "\"}";
    }
}
