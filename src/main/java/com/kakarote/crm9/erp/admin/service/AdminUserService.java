package com.kakarote.crm9.erp.admin.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.config.redis.RedisManager;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.entity.AdminUserRole;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.kakarote.crm9.utils.Sort;
import com.kakarote.crm9.utils.TagUtil;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jfinal.plugin.activerecord.tx.Tx;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

public class AdminUserService {
    @Inject
    private AdminRoleService adminRoleService;
    @Inject
    private AdminDeptService adminDeptService;

    @Before(Tx.class)
    public R setUser(AdminUser adminUser, String roleIds) {
        boolean bol;
        updateScene(adminUser);
        if (adminUser.getUserId() == null) {
            Integer count = Db.queryInt("select count(*) from 72crm_admin_user where username = ?", adminUser.getUsername());
            if (count > 0) {
                return R.error("手机号重复！");
            }
            String salt = IdUtil.fastSimpleUUID();
            adminUser.setNum(RandomUtil.randomNumbers(15));
            adminUser.setSalt(salt);
            adminUser.setPassword(BaseUtil.sign((adminUser.getUsername().trim() + adminUser.getPassword().trim()), salt));
            adminUser.setCreateTime(new Date());
            adminUser.setMobile(adminUser.getUsername());
            bol = adminUser.save();
        } else {
            if (adminUser.getParentId() != null && adminUser.getParentId() != 0) {
                List<Record> topUserList = queryTopUserList(adminUser.getUserId());
                boolean isContain = false;
                for (Record record : topUserList) {
                    if (record.getLong("user_id").equals(adminUser.getParentId())) {
                        isContain = true;
                        break;
                    }
                }
                if (!isContain) {
                    return R.error("该员工的下级员工不能设置为直属上级");
                }
            }
            String username = Db.queryStr("select username from 72crm_admin_user where user_id = ?", adminUser.getUserId());
            if (!username.equals(adminUser.getUsername())) {
                return R.error("用户名不能修改！");
            }
            bol = adminUser.update();
            Db.delete("delete from 72crm_admin_user_role where user_id = ?", adminUser.getUserId());
            Db.delete("delete from 72crm_admin_scene where user_id = ? and is_system = 1", adminUser.getUserId());
        }
        if (StrUtil.isNotBlank(roleIds)) {
            Long userId = adminUser.getUserId();
            for (Integer roleId : TagUtil.toSet(roleIds)) {
                AdminUserRole adminUserRole = new AdminUserRole();
                adminUserRole.setUserId(userId);
                adminUserRole.setRoleId(roleId);
                adminUserRole.save();
            }
        }
        return R.isSuccess(bol);
    }

    private void updateScene(AdminUser adminUser){
        List<Long> ids = new ArrayList<>();
        if (adminUser.getUserId() == null && adminUser.getParentId() != null){
            ids.add(adminUser.getParentId());
        }else if (adminUser.getUserId() != null){
            AdminUser oldAdminUser = AdminUser.dao.findById(adminUser.getUserId());
            if (oldAdminUser.getParentId() == null && adminUser.getParentId() != null){
                ids.add(adminUser.getParentId());
            }else if (oldAdminUser.getParentId() != null && !oldAdminUser.getParentId().equals(adminUser.getParentId())){
                ids.add(oldAdminUser.getParentId());
                ids.add(adminUser.getParentId());
            }
        }
        if (ids.size() > 0){
            HashSet<Long> idsSet = new HashSet<>();
            ids.forEach(id -> {
                List<Long> longs = queryTopUserId(id, BaseConstant.AUTH_DATA_RECURSION_NUM);
                if (CollUtil.isNotEmpty(longs)){
                    idsSet.addAll(longs);
                }
            });
            SqlPara sqlPara=Db.getSqlPara("admin.user.updateScene",Kv.by("ids",idsSet));
            Db.delete(sqlPara.getSql(),sqlPara.getPara());
        }
    }

    /**
     * @author wyq
     * 查询上级id
     * @param userId 用户ID
     */
    private List<Long> queryTopUserId(Long userId,Integer deepness){
        List<Long> arrUsers=new ArrayList<>();
        if (deepness-- > 0){
            AdminUser adminUser=AdminUser.dao.findById(userId);
            if(adminUser!=null){
                if(adminUser.getParentId() != null && !adminUser.getParentId().equals(0L)){
                    arrUsers.addAll(queryTopUserId(adminUser.getParentId(),deepness));
                }
                arrUsers.add(adminUser.getUserId());
            }
        }
        return arrUsers;
    }

    /**
     * 重置用户信息
     *
     * @return 用户信息
     */
    public AdminUser resetUser(HttpServletRequest request) {
        AdminUser adminUser = AdminUser.dao.findFirst(Db.getSql("admin.user.queryUserByUserId"), BaseUtil.getUserId());
        adminUser.setRoles(adminRoleService.queryRoleIdsByUserId(adminUser.getUserId()));
        RedisManager.getRedis().setex(BaseUtil.getToken(request), 360000, adminUser);
        adminUser.remove("password", "salt");
        return adminUser;
    }

    public R queryUserList(BasePageRequest<AdminUser> request, String roleId) {
        List<Integer> deptIdList = new ArrayList<>();
        if (request.getData().getDeptId() != null) {
            deptIdList.add(request.getData().getDeptId());
            deptIdList.addAll(queryChileDeptIds(request.getData().getDeptId(),BaseConstant.AUTH_DATA_RECURSION_NUM));
        }
        if (request.getPageType() == 0) {
            List<Record> recordList = Db.find(Db.getSqlPara("admin.user.queryUserList", Kv.by("name", request.getData().getRealname()).set("deptId", deptIdList).set("status", request.getData().getStatus()).set("roleId", roleId)));
            return R.ok().put("data", recordList);
        } else {
            Page<Record> paginate = Db.paginate(request.getPage(), request.getLimit(), Db.getSqlPara("admin.user.queryUserList", Kv.by("name", request.getData().getRealname()).set("deptId", deptIdList).set("status", request.getData().getStatus()).set("roleId", roleId)));
            return R.ok().put("data", paginate);
        }
    }

    /**
     * 查询可设置为上级的user
     */
    public List<Record> queryTopUserList(Long userId) {
        List<Record> recordList = Db.find("select user_id,realname,parent_id from 72crm_admin_user");
        List<Long> subUserList = queryChileUserIds(userId,BaseConstant.AUTH_DATA_RECURSION_NUM);
        recordList.removeIf(record -> subUserList.contains(record.getLong("user_id")));
        recordList.removeIf(record -> record.getLong("user_id").equals(userId));
        return recordList;
    }

    /**
     * 查询本部门下的所有部门id
     *
     * @param deptId 当前部门id
     */
    public List<Integer> queryChileDeptIds(Integer deptId,Integer deepness) {
        List<Integer> list = Db.query("select dept_id from 72crm_admin_dept where pid = ?", deptId);
        if (list.size() != 0 && deepness > 0) {
            int size = list.size();
            for (int i = 0; i < size; i++) {
                list.addAll(queryChileDeptIds(list.get(i),deepness-1));
            }
        }
        return list;
    }

    /**
     * 查询本用户下的所有下级id
     *
     * @param userId 当前用户id
     */
    public List<Long> queryChileUserIds(Long userId,Integer deepness) {
        List<Long> query = Db.query("select user_id from 72crm_admin_user where parent_id = ?", userId);
        if (deepness > 0) {
            for (int i = 0,size=query.size(); i < size; i++) {
                query.addAll(queryChileUserIds(query.get(i),deepness-1));
            }
        }
        HashSet<Long> set=new HashSet<>(query);
        query.clear();
        query.addAll(set);
        return query;
    }


    public R resetPassword(String ids, String pwd) {
        for (String id : ids.split(",")) {
            AdminUser adminUser = new AdminUser().dao().findById(id);
            String password = BaseUtil.sign(adminUser.getUsername() + pwd, adminUser.getSalt());
            Db.update("update 72crm_admin_user set password = ? where user_id = ?", password, id);
            BaseUtil.userExit(adminUser.getUserId(),null);
        }
        return R.ok();
    }

    public R querySuperior(String realName) {
        return R.ok().put("data", Db.find(Db.getSqlPara("admin.user.querySuperior", Kv.by("name", realName))));
    }

    public R queryListName(String name) {
        List<Record> users = Db.find(Db.getSqlPara("admin.user.queryUserByRealName", Kv.by("name", name)));
        Sort sort = new Sort();
        Map<String, List<Record>> map = sort.sort(users);
        return R.ok().put("data", map);
    }

    /**
     * @author zxy
     * 查询系统下属用户列表
     */
    public List<Long> queryUserIdsByParentId(Long userId) {
        String sql = "select user_id from 72crm_admin_user where parent_id = ? ";
        List<Record> records = Db.find(sql, userId);
        List<Long> userIds = new ArrayList<>();
        for (Record record : records) {
            userIds.add(record.getLong("user_id"));
        }
        return userIds;
    }

    /**
     * @author zxy
     * 查询部门属用户列表
     */
    public R queryListNameByDept(String name) {
        List<Record> records = Db.find(Db.getSql("admin.dept.deptSql"));
        for (Record record : records) {
            List<Record> users = Db.find(Db.getSqlPara("admin.user.queryUserByRealName", Kv.by("deptId", record.getInt("dept_id")).set("name", name)));
            record.set("userList", users);
            record.set("userNumber", users.size());
        }
        return R.ok().put("data", records);
    }

    /**
     * @author zxy
     * 根据部门查询用户id
     */
    public String queryUserIdsByDept(String deptIds) {
        if (StrUtil.isEmpty(deptIds)) {
            return null;
        }
        SqlPara sqlPara=Db.getSqlPara("admin.user.queryUserIdByDeptId",Kv.by("deptIds",deptIds));
        List<Long> users=Db.query(sqlPara.getSql(),sqlPara.getPara());
        return StrUtil.join(",",users);
    }

    public R queryAllUserList() {
        List<Record> recordList = Db.find(Db.getSqlPara("admin.user.queryUserList"));
        return R.ok().put("data", recordList);
    }

    public R setUserStatus(String ids, String status) {
        for (Integer id : TagUtil.toSet(ids)) {
            Db.update("update 72crm_admin_user set status = ? where user_id = ?", status, id);
            if("0".equals(status)){
                BaseUtil.userExit(Long.valueOf(id),null);
            }
        }
        return R.ok();
    }

    public boolean updateImg(String url, Long userId) {
        AdminUser adminUser = AdminUser.dao.findById(userId);
        adminUser.setImg(url);
        return adminUser.update();
    }

    public boolean updateUser(AdminUser adminUser) {
        if (!BaseUtil.getUser().getUsername().equals(adminUser.getUsername())) {
            return false;
        }
        adminUser.setUserId(BaseUtil.getUserId());
        if (StrUtil.isNotEmpty(adminUser.getPassword())) {
            adminUser.setSalt(IdUtil.simpleUUID());
            adminUser.setPassword(BaseUtil.sign((adminUser.getUsername().trim() + adminUser.getPassword().trim()), adminUser.getSalt()));
        }
        return adminUser.update();
    }

    /**
     * 查询当前用户权限 用于菜单展示 FFF
     * @param userId 用户id
     * @param realm 当前操作的菜单 链接地址
     * @return
     */
    public List<Long> queryUserByAuth(Long userId,String realm) {
        List<Long> adminUsers = new ArrayList<>();
        //查询用户数据权限，从高到低排序
        List<Integer> list = Db.query(Db.getSql("admin.role.queryDataTypeByUserId"), userId);
        if (list.size() == 0) {
            //无权限查询自己的数据
            adminUsers.add(userId);
            return adminUsers;
        }
        //只要上面list的长度不为0 那么这个也不会为0
        List<Record> userRoleList=Db.find(Db.getSqlPara("admin.role.queryUserRoleListByUserId",Kv.by("userId",userId).set("realm",realm)));
        if(list.size()==1&&userRoleList.size()==1){//如果为1的话 验证是否有最高权限，否则及有多个权限
            //拥有最高数据权限
            if (list.contains(5)) {
                return null;
            } else {
                AdminUser adminUser = AdminUser.dao.findById(userId);
                if (list.contains(4)) {
                    List<Record> records = adminDeptService.queryDeptByParentDept(adminUser.getDeptId(), BaseConstant.AUTH_DATA_RECURSION_NUM);
                    List<Integer> deptIds = new ArrayList<>();
                    deptIds.add(adminUser.getDeptId());
                    records.forEach(record -> deptIds.add(record.getInt("id")));
                    SqlPara sqlPara = Db.getSqlPara("admin.user.queryUserIdByDeptId", Kv.by("deptIds", deptIds));
                    adminUsers.addAll(Db.query(sqlPara.getSql(), sqlPara.getPara()));
                } else if(list.contains(3)){
                    queryUserByDeptId(adminUser.getDeptId()).forEach(record -> adminUsers.add(record.getLong("id")));
                }

                if (list.contains(2)) {
                    adminUsers.addAll(queryUserByParentUser(adminUser.getUserId(), BaseConstant.AUTH_DATA_RECURSION_NUM));
                }
                adminUsers.add(adminUser.getUserId());
            }
        }else{//多个权限
            if(realm!=null&&!"".equals(realm)) {
                AdminUser adminUser = AdminUser.dao.findById(userId);
                for (Record r:userRoleList) {//如果有多个权限 验证当前用户是否对当前管理 是否为本人操作
                    if(r.getStr("realm").equals(realm)&&r.getStr("data_type").equals("1")){//当前操作的管理链接地址
                        adminUsers.add(userId);
                        HashSet<Long> hashSet = new HashSet<>(adminUsers);
                        adminUsers.clear();
                        adminUsers.addAll(hashSet);
                        return adminUsers;
                    }else if(r.getStr("realm").equals(realm)&&r.getStr("data_type").equals("2")){//本人及其
                        adminUsers.addAll(queryUserByParentUser(adminUser.getUserId(), BaseConstant.AUTH_DATA_RECURSION_NUM));
                        adminUsers.add(userId);
                        HashSet<Long> hashSet = new HashSet<>(adminUsers);
                        adminUsers.clear();
                        adminUsers.addAll(hashSet);
                        return adminUsers;
                    }else if(r.getStr("realm").equals(realm)&&r.getStr("data_type").equals("3")){//本部门
                        queryUserByDeptId(adminUser.getDeptId()).forEach(record -> adminUsers.add(record.getLong("id")));
                        adminUsers.add(userId);
                        HashSet<Long> hashSet = new HashSet<>(adminUsers);
                        adminUsers.clear();
                        adminUsers.addAll(hashSet);
                        return adminUsers;
                    }else if(r.getStr("realm").equals(realm)&&r.getStr("data_type").equals("4")){//本部门及下属部门
                        List<Record> records = adminDeptService.queryDeptByParentDept(adminUser.getDeptId(), BaseConstant.AUTH_DATA_RECURSION_NUM);
                        List<Integer> deptIds = new ArrayList<>();
                        deptIds.add(adminUser.getDeptId());
                        records.forEach(record -> {
                            deptIds.add(record.getInt("id"));
                        });
                        SqlPara sqlPara = Db.getSqlPara("admin.user.queryUserIdByDeptId", Kv.by("deptIds", deptIds));
                        adminUsers.addAll(Db.query(sqlPara.getSql(), sqlPara.getPara()));
                        adminUsers.add(userId);
                        HashSet<Long> hashSet = new HashSet<>(adminUsers);
                        adminUsers.clear();
                        adminUsers.addAll(hashSet);
                        return adminUsers;
                    }else if(r.getStr("realm").equals(realm)&&r.getStr("data_type").equals("5")){//全部
                        return null;
                    }
                }
            }else{
                if (list.contains(5)) {
                    return null;
                } else {
                    AdminUser adminUser = AdminUser.dao.findById(userId);
                    if (list.contains(4)) {
                        List<Record> records = adminDeptService.queryDeptByParentDept(adminUser.getDeptId(), BaseConstant.AUTH_DATA_RECURSION_NUM);
                        List<Integer> deptIds = new ArrayList<>();
                        deptIds.add(adminUser.getDeptId());
                        records.forEach(record -> deptIds.add(record.getInt("id")));
                        SqlPara sqlPara = Db.getSqlPara("admin.user.queryUserIdByDeptId", Kv.by("deptIds", deptIds));
                        adminUsers.addAll(Db.query(sqlPara.getSql(), sqlPara.getPara()));
                    } else if(list.contains(3)){
                        queryUserByDeptId(adminUser.getDeptId()).forEach(record -> adminUsers.add(record.getLong("id")));
                    }

                    if (list.contains(2)) {
                        adminUsers.addAll(queryUserByParentUser(adminUser.getUserId(), BaseConstant.AUTH_DATA_RECURSION_NUM));
                    }
                    adminUsers.add(adminUser.getUserId());
                }
            }
        }

        adminUsers.add(userId);
        HashSet<Long> hashSet = new HashSet<>(adminUsers);
        adminUsers.clear();
        adminUsers.addAll(hashSet);
        return adminUsers;
    }

    public List<Long> queryUserByParentUser(Long userId, Integer deepness) {
        List<Long> recordList = new ArrayList<>();
        if (deepness > 0) {
            List<Long> records = Db.query("SELECT b.user_id FROM 72crm_admin_user AS b WHERE b.parent_id = ?", userId);
            recordList.addAll(records);
            int size = recordList.size();
            for (int i = 0; i < size; i++) {
                recordList.addAll(queryUserByParentUser(recordList.get(i), deepness - 1));
            }
        }
        return recordList;
    }
    public List<Record> queryUserByDeptId(Integer deptId){
        return Db.find(Db.getSql("admin.user.queryUserByDeptId"),deptId);
    }

    public List<Record> queryAllUserByDeptId(Integer deptId) {
        List<Integer> deptIdList = new ArrayList<>();
        deptIdList.add(deptId);
        deptIdList.addAll(queryChileDeptIds(deptId, BaseConstant.AUTH_DATA_RECURSION_NUM));
        return Db.find(Db.getSqlPara("admin.user.queryAllUserByDeptId",Kv.by("ids",deptIdList)));
    }

    /**
     * @author zxy
     * 根据部门id和用户ID 去重 （仪盘表中业绩指标用）
     */
    public Record queryByDeptIds(String deptIds,String userIds){
        Record record = new Record();
        List<Record> allDepts = Db.find("select * from 72crm_admin_dept where dept_id in ( ? )",deptIds);
        deptIds = getDeptIds(null,allDepts);

        String arrUserIds  = queryUserIdsByDept(deptIds);
        if (StrUtil.isNotEmpty(userIds)) {
            userIds = getUserIds(deptIds, userIds);
        }
        record.set("deptIds",deptIds);
        record.set("userIds",userIds);
        record.set("arrUserIds",arrUserIds);
        return record;
    }

    private String getDeptIds(String deptIds, List<Record> allDepts){
        for ( Record dept: allDepts) {
            Integer pid =   dept.getInt("pid");
            if (pid != 0){
                deptIds = getDeptIds(deptIds,  Db.find("select * from 72crm_admin_dept where dept_id in ( ? )",pid));
            }else {
                if (deptIds == null){
                    deptIds = dept.getStr("dept_id");
                }else {
                    deptIds = deptIds + "," + dept.getStr("dept_id");
                }
            }
        }
        return deptIds;
    }


    /**
     * 修改用户账号功能
     * @param id 用户ID
     * @param username 新的用户名
     * @param password 新的密码
     * @return 操作状态
     */
    @Before(Tx.class)
    public R usernameEdit(Integer id,String username, String password){
        AdminUser adminUser=AdminUser.dao.findById(id);
        if(adminUser==null){
            return R.error("用户不存在！");
        }
        if(adminUser.getUsername().equals(username)){
            return R.error("账号不能和原账号相同");
        }
        Integer count = Db.queryInt("select count(*) from 72crm_admin_user where username = ?", username);
        if (count > 0) {
            return R.error("手机号重复！");
        }
        adminUser.setUsername(username);
        adminUser.setMobile(username);
        adminUser.setPassword(BaseUtil.sign(username+password,adminUser.getSalt()));
        BaseUtil.userExit(adminUser.getUserId(),null);
        return R.isSuccess(adminUser.update());
    }

    private String getUserIds(String deptIds,String userIds){
        List<Record> allUsers = Db.find("select * from 72crm_admin_user where dept_id NOT in (?) and user_id in (?)", deptIds, userIds);
        userIds = null;
        for ( Record user: allUsers) {
            if (userIds == null){
                userIds = user.getStr("user_id");
            }else {
                userIds = deptIds + "," + user.getStr("user_id");
            }
        }
        return userIds;
    }
}
