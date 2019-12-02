package com.kakarote.crm9.utils;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 返回数据
 */
public class R extends LinkedHashMap<String, Object> implements Serializable {
	private static final long serialVersionUID = 1L;

	public R() {
		put("code", 0);
	}

	public static R error() {
		return error(500, "未知异常，请联系管理员");
	}

	public static R error(String msg) {
		return error(500, msg);
	}

	public static R noAuth() {
		return error("没有权限");
	}

	public static R error(int code, String msg) {
		R r = new R();
		r.put("code", code);
		r.put("msg", msg);
		return r;
	}

	public static R ok(String msg) {
		R r = new R();
		r.put("msg", msg);
		return r;
	}

	public static R ok(Map<String, Object> map) {
		R r = new R();
		r.putAll(map);
		return r;
	}

	public static R ok() {
		return new R();
	}

	@Override
	public R put(String key, Object value) {
		super.put(key, value);
		return this;
	}
	public boolean isSuccess(){
		return super.containsKey("code")&&super.get("code").equals(0);
	}

	public static R isSuccess(boolean b,String msg){
		return b?R.ok():R.error(msg);
	}
	public static R isSuccess(boolean b){
		return isSuccess(b,null);
	}

}
