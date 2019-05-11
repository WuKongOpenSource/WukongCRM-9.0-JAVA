package com.kakarote.crm9.erp.admin.entity;

import com.kakarote.crm9.erp.admin.entity.base.BaseAdminUser;

import java.util.List;

@SuppressWarnings("serial")
public class AdminUser extends BaseAdminUser<AdminUser> {
	public static final AdminUser dao = new AdminUser().dao();
	//查询开始时间
	private String startTime;
	//查询结束时间
	private String endTime;
	//用户角色列表
	private List<Integer> roles;

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public List<Integer> getRoles() {
		return roles;
	}

	public void setRoles(List<Integer> roles) {
		this.roles = roles;
	}
}
