package com.kakarote.crm9.erp.admin.controller;

import com.kakarote.crm9.erp.admin.service.AdminMenuService;
import com.kakarote.crm9.utils.R;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;

public class AdminMenuController extends Controller {
    @Inject
    private AdminMenuService adminMenuService;

    /**
     * @author wyq
     * @param roleId 角色id
     * 根据角色id查询菜单id
     */
    public void getRoleMenu(@Para("roleId") Integer roleId){
        renderJson(R.ok().put("data",adminMenuService.getMenuIdByRoleId(roleId)));
    }

    /**
     * @author wyq
     * 展示全部菜单
     */
    public void getAllMenuList(){
        renderJson(R.ok().put("data",adminMenuService.getAllMenuList(0,20)));
    }
}
