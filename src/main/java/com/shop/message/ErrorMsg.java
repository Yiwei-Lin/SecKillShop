package com.shop.message;

/**
 * 错误消息：
 *    包括错误码和消息提示信息
 */
public class ErrorMsg {
    private int errorCode;   // 错误码
    private String message;  // 错误消息
    // 通用异常
    public static final ErrorMsg GENERAL_ERROR = new ErrorMsg(50010, "服务器异常");
    public static final ErrorMsg VERIFY_CODE_ERROR = new ErrorMsg(50011, "验证码错误");
    // 登录异常
    public static final ErrorMsg LOGIN_ERROR = new ErrorMsg(50020, "登录异常");
    public static final ErrorMsg MOBILE_NOT_EXIST = new ErrorMsg(50021, "手机号未注册");
    public static final ErrorMsg PASSWORD_ERROR = new ErrorMsg(50022, "密码错误");
    public static final ErrorMsg SESSION_ERROR = new ErrorMsg(50023, "Session已失效");
    //订单异常
    public static final ErrorMsg ORDER_ERROR = new ErrorMsg(50030, "订单异常");
    public static final ErrorMsg ORDER_NOT_EXIST = new ErrorMsg(50031, "订单不存在");
    //秒杀异常
    public static final ErrorMsg SECKILL_FAIL = new ErrorMsg(50050, "秒杀失败");
    public static final ErrorMsg SECKILL_OVER = new ErrorMsg(50051, "商品已售空");
    public static final ErrorMsg REPEATED_SECKILL = new ErrorMsg(50052, "请勿重复秒杀");
    public static final ErrorMsg SECKILL_LIMIT= new ErrorMsg(50053, "请求太频繁，请稍候再试！");

    private ErrorMsg(int errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public int getErrorCode() {
        return errorCode;
    }
    public String getMessage() { return message; }

    @Override
    public String toString() {
        return "ErrorMsg [code=" + errorCode + ", msg=" + message + "]";
    }
}
