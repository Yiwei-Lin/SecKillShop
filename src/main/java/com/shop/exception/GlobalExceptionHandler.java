package com.shop.exception;

import com.shop.message.ErrorMsg;
import com.shop.message.ResultMsg;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * 异常处理类：
 * 	  如果是定义的全局异常，则返回对应错误提示信息；如果是其他异常，则返回GENERAL_ERROR
 */
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {
	@ExceptionHandler(value=Exception.class)
	public ResultMsg<String> exceptionHandler(HttpServletRequest request, Exception e){
		e.printStackTrace();
		if(e instanceof GlobalException) {    // 是系统定义的全局异常则返回对应错误信息
			GlobalException globalException = (GlobalException)e;
			return ResultMsg.error(globalException.getErrorMsg());
		} else {   // 否则返回GENERAL_ERROR
			return ResultMsg.error(ErrorMsg.GENERAL_ERROR);
		}
	}
}
