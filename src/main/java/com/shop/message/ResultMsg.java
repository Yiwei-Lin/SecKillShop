package com.shop.message;

/**
 * 结果信息：
 *   包括返回码、消息和数据
 */
public class ResultMsg<T> {

    private int code;        // 返回码
    private String message;  // 消息
    private T data;          // 数据

    // 成功
    private ResultMsg(T data) {
        this.code = 200;
        this.message = "success";
        this.data = data;
    }
    // 失败
    private ResultMsg(ErrorMsg errorMsg) {
        if(errorMsg == null) {
            return;
        }
        this.code = errorMsg.getErrorCode();
        this.message = errorMsg.getMessage();
    }

    public static<T> ResultMsg<T> success(T data){
        return new ResultMsg<T>(data);
    }

    public static <T> ResultMsg<T> error(ErrorMsg errorMsg){
        return new ResultMsg<T>(errorMsg);
    }


    public int getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }
    public T getData() {
        return data;
    }
}
