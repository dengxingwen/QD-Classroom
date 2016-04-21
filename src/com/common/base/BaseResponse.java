package com.common.base;

/**
 * @author 
 * 客户端消息返回基类
 */
public class BaseResponse {
	
	private int code; //返回状态：200成功，400失败
	private Object result; //返回的内容
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public Object getResult() {
		return result;
	}
	public void setResult(Object result) {
		this.result = result;
	}

}
