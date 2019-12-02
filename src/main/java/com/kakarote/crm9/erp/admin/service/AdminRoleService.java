package com.kakarote.crm9.erp.admin.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ReUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.kit.Kv;
import com.jfinal.log.Log;
import com.jfinal.template.stat.ast.Else;
import com.kakarote.crm9.common.config.cache.CaffeineCache;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.erp.admin.entity.*;
import com.kakarote.crm9.utils.R;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class AdminRoleService {

    @Inject
    private AdminMenuService adminMenuService;

    /**
     * @author wyq
     * 获取全部角色列表
     */
    public List<Record> getAllRoleList() {
        List<Record> records = new ArrayList<>();
        for (Integer roleType : BaseConstant.ROLE_TYPES) {
            Record record = new Record();
            record.set("name", roleTypeCaseName(roleType));
            record.set("pid", roleType);
            List<Record> recordList = Db.find(Db.getSql("admin.role.getRoleListByRoleType"), roleType);
            recordList.forEach(role -> {
                List<Integer> crm = Db.query(Db.getSql("admin.role.getRoleMenu"), role.getInt("id"), 1, 1);
                List<Integer> bi = Db.query(Db.getSql("admin.role.getRoleMenu"), role.getInt("id"), 2, 2);
                role.set("rules",new JSONObject().fluentPut("crm",crm).fluentPut("bi",bi));
            });
            record.set("list", recordList);
            records.add(record);
        }
        return records;
    }

    /**
     * @author wyq
     * 根据角色类型查询角色
     */
    public List<Record> getRoleByType(Integer roleType){
        List<Record> recordList = Db.find("select * from 72crm_admin_role where role_type = ?",roleType);
        String realm;
        switch (roleType){
            case 1:
                realm = "manage";
                break;
            case 2:
                realm = "crm";
                break;
            case 7:
                realm = "oa";
                break;
            case 8:
                realm = "project";
                break;
            default:
                return new ArrayList<>();
        }
        Integer pid = Db.queryInt("select menu_id from 72crm_admin_menu where parent_id = 0 and realm = ?",realm);
        recordList.forEach(record -> {
            List<Integer> data = Db.query(Db.getSql("admin.role.getRoleMenu"), record.getInt("role_id"), pid, pid);
            List<Integer> bi = Db.query(Db.getSql("admin.role.getRoleMenu"), record.getInt("role_id"), 2, 2);
            record.set("rules",new JSONObject().fluentPut("data",data).fluentPut("bi",bi));
        });
        return recordList;
    }

    /**
     * @author wyq
     * 根据角色类型查询关联员工
     */
    public List<Record> getRoleUser(Integer roleType) {
        return Db.find(Db.getSql("admin.role.getRoleUser"), roleType);
    }

    /**
     * @author wyq
     * 新建
     */
    public R save(AdminRole adminRole) {
        Integer number = Db.queryInt("select count(*) from 72crm_admin_role where role_name = ? and role_type = ?", adminRole.getRoleName(), adminRole.getRoleType());
        if (number > 0) {
            return R.error("角色名已存在");
        }
        return adminRole.save() ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 编辑角色
     */
    @Before(Tx.class)
    public Integer update(AdminRole adminRole) {
        adminRole.update();
        List<Integer> menuList;
        if (adminRole.getMenuIds() != null) {
            try {
                menuList = JSON.parseArray(URLDecoder.decode(adminRole.getMenuIds(), "utf-8"), Integer.class);
            } catch (UnsupportedEncodingException e) {
                Log.getLog(getClass()).error("",e);
                throw new RuntimeException("数据错误");
            }
            adminMenuService.saveRoleMenu(adminRole.getRoleId(), adminRole.getDataType(), menuList);
            return 1;
        }
        return 0;
    }

    @Before(Tx.class)
    public void updateRoleMenu(JSONObject jsonObject) {
        adminMenuService.saveRoleMenu(jsonObject.getInteger("id"), jsonObject.getInteger("type"), jsonObject.getJSONArray("rules").toJavaList(Integer.class));
    }

    /**
     * 查看权限
     */
    public JSONObject auth(Long userId) {
        JSONObject jsonObject=CaffeineCache.ME.get("role:permissions",userId.toString());
        if(jsonObject!=null){
            return jsonObject;
        }
        jsonObject = new JSONObject();
        List<Record> menuRecords;
        List<Integer> roleIds = queryRoleIdsByUserId(userId);
        if (roleIds.contains(BaseConstant.SUPER_ADMIN_ROLE_ID)) {
            menuRecords = adminMenuService.queryAllMenu();
        } else{
            menuRecords = adminMenuService.queryMenuByUserId(userId);
            //如果是项目管理员查询创建项目角色
            if(roleIds.contains(BaseConstant.WORK_ADMIN_ROLE_ID)){
                menuRecords.add(Db.findFirst("select realm,menu_id,parent_id from `72crm_admin_menu` where remarks = 'projectSave'"));
            }
        }
        List<AdminMenu> adminMenus = adminMenuService.queryMenuByParentId(0);
        for (AdminMenu adminMenu : adminMenus) {
            JSONObject object = new JSONObject();
            List<AdminMenu> adminMenuList = adminMenuService.queryMenuByParentId(adminMenu.getMenuId());
            for (AdminMenu menu : adminMenuList) {
                JSONObject authObject = new JSONObject();
                for (Record record : menuRecords) {
                    if (menu.getMenuId().equals(record.getInt("parent_id"))) {
                        authObject.put(record.getStr("realm"), true);
                    }
                }
                if (!authObject.isEmpty()) {
                    object.put(menu.getRealm(), authObject);
                }
            }
            if (adminMenu.getMenuId().equals(3)) {
                if (roleIds.contains(BaseConstant.SUPER_ADMIN_ROLE_ID)){
                    object.put("system",new JSONObject().fluentPut("read",true).fluentPut("update",true));
                    object.put("configSet",new JSONObject().fluentPut("read",true).fluentPut("update",true));
                    object.put("users",new JSONObject().fluentPut("read",true).fluentPut("userSave",true).fluentPut("userEnables",true).fluentPut("userUpdate",true).fluentPut("deptSave",true).fluentPut("deptUpdate",true).fluentPut("deptDelete",true));
                    object.put("permission",new JSONObject().fluentPut("update",true));
                    object.put("oa",new JSONObject().fluentPut("examine",true));
                    object.put("examineFlow",new JSONObject().fluentPut("update",true));
                    object.put("crm",new JSONObject().fluentPut("field",true).fluentPut("pool",true).fluentPut("setting",true).fluentPut("achievement",true));
                    object.put("work",new JSONObject().fluentPut("update",true));
                }
            }
            if (!object.isEmpty()) {
                jsonObject.put(adminMenu.getRealm(), object);
            }
        }
        List<String> moduleName = Db.query("select name from 72crm_admin_config where name in ('oa','crm','project') and status = 0");
        if(!jsonObject.containsKey("project")){
            jsonObject.put("project",new Object());
        }
        if(!jsonObject.containsKey("oa")){
            jsonObject.put("oa",new Object());
        }
        for (String s : moduleName) {
            jsonObject.remove(s);
            JSONObject bi = jsonObject.getJSONObject("bi");
            if (bi != null) {
                if (s.equals("oa")) {
                    bi.remove("oa");
                } else if (s.equals("crm")) {
                    String[] oabiMenuArr = new String[]{"achievement","business","customer","contract","product","portrait","ranking"};
                    List<String> oabiMenuList = CollectionUtil.newArrayList(oabiMenuArr);
                    oabiMenuList.forEach(bi::remove);
                }
            }
        }
        CaffeineCache.ME.put("role:permissions:",userId.toString(),jsonObject);
        return jsonObject;
    }

    /**
     * @author wyq
     * 删除
     */
    public boolean delete(Integer roleId) {
        Record record = Db.findFirst("select count(*) as menuNum from 72crm_admin_role_menu where role_id = ?", roleId);
        if (record.getInt("menuNum") == 0) {
            return Db.delete(Db.getSql("admin.role.deleteRole"), roleId) > 0;
        }
        return Db.tx(() -> {
            Db.delete(Db.getSql("admin.role.deleteRole"), roleId);
            Db.delete(Db.getSql("admin.role.deleteRoleMenu"), roleId);
            return true;
        });
    }

    /**
     * @author wyq
     * 删除
     */
    @Before(Tx.class)
    public boolean deleteWorkRole(Integer roleId) {
        Db.delete(Db.getSql("admin.role.deleteRole"), roleId);
        Db.delete(Db.getSql("admin.role.deleteRoleMenu"), roleId);
        Db.update("update `72crm_work_user` set role_id = ? where role_id = ?",BaseConstant.SMALL_WORK_EDIT_ROLE_ID,roleId);
        return true;
    }


    /**
     * @author wyq
     * 复制
     */
    @Before(Tx.class)
    public void copy(Integer roleId) {
        AdminRole adminRole = AdminRole.dao.findById(roleId);
        List<Record> recordList = Db.find(Db.getSql("admin.role.getMenuIdsList"), roleId);
        List<Integer> menuIdsList = new ArrayList<>(recordList.size());
        for (Record record : recordList) {
            menuIdsList.add(record.getInt("menu_id"));
        }
        String roleName = adminRole.getRoleName().trim();
        String pre = ReUtil.delFirst("[(]\\d+[)]$", roleName);
        List<AdminRole> adminRoleList;
        if (!ReUtil.contains("^[(]\\d+[)]$", roleName)) {
            adminRoleList = AdminRole.dao.find("select * from 72crm_admin_role where role_name like '" + pre + "%'");
        } else {
            adminRoleList = AdminRole.dao.find("select * from 72crm_admin_role where role_name regexp '^[(]\\d+[)]$'");
        }
        StringBuilder numberSb = new StringBuilder();
        for (AdminRole dbAdminRole : adminRoleList) {
            String endCode = ReUtil.get("[(]\\d+[)]$", dbAdminRole.getRoleName(), 0);
            if (endCode != null) {
                numberSb.append(endCode);
            }
        }
        int i = 1;
        if (numberSb.length() == 0) {
            while (numberSb.toString().contains("(" + i + ")")) {
                i++;
            }
        }
        adminRole.setRoleName(pre + "(" + i + ")");
        adminRole.setRoleId(null);
        adminRole.save();
        Integer copyRoleId = adminRole.getInt("role_id");
        adminMenuService.saveRoleMenu(copyRoleId, adminRole.getDataType(), menuIdsList);
    }

    /**
     * @author wyq
     * 角色关联员工
     */
    @Before(Tx.class)
    public R relatedUser(AdminUserRole adminUserRole) {
        if (adminUserRole != null && adminUserRole.getUserIds() != null) {
            String[] userIdsArr = adminUserRole.getUserIds().split(",");
            String[] roleIdsArr = adminUserRole.getRoleIds().split(",");
            for (String userId : userIdsArr) {
                for (String roleId : roleIdsArr) {
                    Db.delete("delete from 72crm_admin_user_role where user_id = ? and role_id = ?", Integer.valueOf(userId), Integer.valueOf(roleId));
                    AdminUserRole userRole = new AdminUserRole();
                    userRole.setUserId(Long.valueOf(userId));
                    userRole.setRoleId(Integer.valueOf(roleId));
                    userRole.save();
                }
            }
            return R.ok();
        } else {
            return R.error("请选择角色和员工");
        }
    }

    /**
     * @author wyq
     * 解除角色关联员工
     */
    public R unbindingUser(AdminUserRole adminUserRole) {
        if (adminUserRole.getUserId().equals(BaseConstant.SUPER_ADMIN_USER_ID)) {
            return R.error("超级管理员不可被更改");
        }
        if(Db.queryInt("select count(1) from `72crm_admin_user_role` where user_id = ?",adminUserRole.getUserId())==1){
            return R.error("用户至少需要一个角色");
        }
        return Db.delete("delete from 72crm_admin_user_role where user_id = ? and role_id = ?", adminUserRole.getUserId(), adminUserRole.getRoleId()) > 0 ? R.ok() : R.error();
    }

    public List<Integer> queryRoleIdsByUserId(Long userId) {
        return Db.query(Db.getSql("admin.role.queryRoleIdsByUserId"), userId);
    }

    /**
     * 角色类型转换名称
     *
     * @param type 类型
     * @return 角色名称
     */
    private String roleTypeCaseName(Integer type) {
        String name;
        switch (type) {
            case 1:
                name = "系统管理角色";
                break;
            case 2:
                name = "客户管理角色";
                break;
            case 7:
                name = "办公管理角色";
                break;
            case 8:
                name = "项目管理角色";
                break;
            default:
                name = "自定义角色";
        }
        return name;
    }

    /**
     * 项目管理角色列表
     * @author wyq
     */
    public R queryProjectRoleList(){
        List<Record> roleList = Db.find("select * from 72crm_admin_role where role_type in (5,6) and is_hidden = 1");
        roleList.forEach(record -> {
            List<Integer> rules = Db.query("select menu_id from 72crm_admin_role_menu where role_id = ?",record.getInt("role_id"));
            record.set("rules",rules);
        });
        return R.ok().put("data",roleList);
    }

    public R setWorkRole(JSONObject jsonObject){
        boolean bol;
        Integer roleId = jsonObject.getInteger("roleId");
        String roleName = jsonObject.getString("roleName");
        String remark = jsonObject.getString("remark");
        JSONArray rules = jsonObject.getJSONArray("rules");
        AdminRole adminRole = new AdminRole();
        adminRole.setRoleName(roleName);
        adminRole.setRoleType(6);
        adminRole.setRemark(remark);
        if(roleId == null){
            bol = adminRole.save();
        }else {
            adminRole.setRoleId(roleId);
            Db.delete("delete from `72crm_admin_role_menu` where role_id = ?",roleId);
            bol = adminRole.update();
        }
        rules.forEach(menuId->{
            AdminRoleMenu adminRoleMenu = new AdminRoleMenu();
            adminRoleMenu.setRoleId(adminRole.getRoleId());
            adminRoleMenu.setMenuId((Integer) menuId);
            adminRoleMenu.save();
        });
        return bol?R.ok():R.error();
    }

    public R getRoleTypeList(){
        List<Kv> data = new ArrayList<>();
        data.add(Kv.by("name","系统管理角色").set("roleType",1));
        data.add(Kv.by("name","办公管理角色").set("roleType",7));
        data.add(Kv.by("name","客户管理角色").set("roleType",2));
        data.add(Kv.by("name","项目管理角色").set("roleType",8));
        return R.ok().put("data",data);
    }
}
