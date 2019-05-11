package com.kakarote.crm9.erp.crm.entity;
/**
 * 系统消息实体类（非数据库表）
 * @author Administrator
 *
 */
public class CrmSystemMessage {
	//时间
	private Integer time;
	//标题
	private String title;
	//内容
	private String content;
	public Integer getTime() {
		return time;
	}
	public void setTime(Integer time) {
		this.time = time;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}

}
