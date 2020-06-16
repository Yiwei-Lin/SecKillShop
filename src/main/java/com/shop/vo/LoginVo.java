package com.shop.vo;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

public class LoginVo {
	
	@NotNull
	@Length(min=11, max=11)
	private String mobile;
	
	@NotNull
	@Length(min=32, max=32)
	private String password; // 密码经过前台md5固定salt加密后必为32位

	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	@Override
	public String toString() {
		return "LoginVo [mobile=" + mobile + ", password=" + password + "]";
	}
}
