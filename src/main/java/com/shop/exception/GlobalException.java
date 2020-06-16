package com.shop.exception;

import com.shop.message.ErrorMsg;

/**
 * 全局异常
 */
public class GlobalException extends RuntimeException{

	private ErrorMsg errorMsg;
	
	public GlobalException(ErrorMsg errorMsg) {
		super(errorMsg.toString());
		this.errorMsg = errorMsg;
	}

	public ErrorMsg getErrorMsg() {
		return errorMsg;
	}

}
