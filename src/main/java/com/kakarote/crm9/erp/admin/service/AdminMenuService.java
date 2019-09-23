package com.kakarote.crm9.erp.admin.service;

import com.jfinal.aop.Before;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.erp.admin.entity.AdminMenu;
import com.kakarote.crm9.erp.admin.entity.AdminRole;
import com.kakarote.crm9.erp.admin.entity.AdminRoleMenu;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.utils.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminMenuService {

    /**
     * 通过用户ID查询用户所拥有菜单
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    public List<Record> queryMenuByUserId(Long userId) {
        return Db.find(Db.getSql("admin.menu.queryMenuByUserId"), userId);
    }

    public List<Record> queryAllMenu() {
        return Db.find(Db.getSql("admin.menu.queryAllMenu"));
    }

    /**
     * @author wyq
     * 展示全部菜单
     */
    public List<AdminMenu> getExceptWorkMenuList(Integer parentId, Integer deepness) {
        List<String> realmList = new ArrayList<>();
        realmList.add("work");
        return getMenuList(parentId, deepness, realmList);
    }

    private List<AdminMenu> getMenuList(Integer parentId, Integer deepness, List<String> realmList) {
        List<AdminMenu> adminMenus = AdminMenu.dao.find(Db.getSql("admin.menu.queryMenuByParentId"), parentId);
        adminMenus.removeIf(adminMenu -> realmList.contains(adminMenu.getRealm()));
        if (deepness != 0) {
            adminMenus.forEach(adminMenu -> {
                if (!adminMenu.getMenuType().equals(3)) {
                    adminMenu.put("childMenu", getExceptWorkMenuList(adminMenu.getMenuId(), deepness - 1));
                }
            });
        }
        return adminMenus;
    }

    public R getMenuListByType(Integer type) {
        Kv kv = new Kv();
        if (type == 1) {
            AdminMenu system = AdminMenu.dao.findFirst("select * from 72crm_admin_menu where parent_id = 0 and realm = 'manage'");
            List<AdminMenu> adminMenuList = getMenuList(system.getMenuId(), BaseConstant.AUTH_DATA_RECURSION_NUM,new ArrayList<>());
            system.put("childMenu", adminMenuList);
            kv.set("data", system);
        } else if (type == 2) {
            AdminMenu crm = AdminMenu.dao.findFirst("select * from 72crm_admin_menu where parent_id = 0 and realm = 'crm'");
            List<AdminMenu> adminMenuList = getExceptWorkMenuList(crm.getMenuId(),BaseConstant.AUTH_DATA_RECURSION_NUM);
            crm.put("childMenu", adminMenuList);
            kv.set("data", crm);
            AdminMenu bi = AdminMenu.dao.findFirst("select * from 72crm_admin_menu where parent_id = 0 and realm = 'bi'");
            List<String> realmList = new ArrayList<>();
            realmList.add("oa");
            List<AdminMenu> biList = getMenuList(bi.getMenuId(),BaseConstant.AUTH_DATA_RECURSION_NUM, realmList);
            bi.put("childMenu", biList);
            kv.set("bi", bi);
        } else if (type == 7) {
            AdminMenu oa = AdminMenu.dao.findFirst("select * from 72crm_admin_menu where parent_id = 0 and realm = 'oa'");
            List<AdminMenu> adminMenuList = getExceptWorkMenuList(oa.getMenuId(),BaseConstant.AUTH_DATA_RECURSION_NUM);
            oa.put("childMenu", adminMenuList);
            kv.set("data", oa);
            AdminMenu bi = AdminMenu.dao.findFirst("select * from 72crm_admin_menu where parent_id = 0 and realm = 'bi'");
            String[] realmArr = new String[]{"achievement", "business", "customer", "contract", "product", "portrait", "ranking"};
            List<String> realmList = new ArrayList<>(Arrays.asList(realmArr));
            List<AdminMenu> biList = getMenuList(bi.getMenuId(),BaseConstant.AUTH_DATA_RECURSION_NUM, realmList);
            bi.put("childMenu", biList);
            kv.set("bi", bi);
        } else if (type == 8) {
            AdminMenu project = AdminMenu.dao.findFirst("select * from 72crm_admin_menu where parent_id = 0 and realm = 'project'");
            List<AdminMenu> adminMenuList = getExceptWorkMenuList(project.getMenuId(),BaseConstant.AUTH_DATA_RECURSION_NUM);
            project.put("childMenu", adminMenuList);
            kv.set("data", project);
        }
        return R.ok().put("data", kv);
    }

    public List<AdminMenu> getAllMenuList(Integer parentId, Integer deepness) {
        return getMenuList(parentId, deepness, new ArrayList<>());
    }

    /**
     * @author zhangzhiwei
     * 根据parentId查询菜单
     */
    public List<AdminMenu> queryMenuByParentId(Integer parentId) {
        return AdminMenu.dao.find(Db.getSql("admin.menu.queryMenuByParentId"), parentId);
    }


    /**
     * @author wyq
     * 保存角色权限
     */
    @Before(Tx.class)
    public boolean saveRoleMenu(Integer roleId, Integer dateType, List<Integer> menuIdList) {
        AdminRole adminRole = new AdminRole();
        adminRole.setRoleId(roleId);
        if (dateType != null) {
            adminRole.setDataType(dateType);
        }
        adminRole.update();
        Db.delete(Db.getSql("admin.role.deleteRoleMenu"), roleId);
        if (null == menuIdList || menuIdList.size() == 0) {
            return true;
        }
        for (Integer menuId : menuIdList) {
            AdminRoleMenu adminRoleMenu = new AdminRoleMenu();
            adminRoleMenu.setRoleId(roleId);
            adminRoleMenu.setMenuId(menuId);
            adminRoleMenu.save();
        }
        return true;
    }
}
