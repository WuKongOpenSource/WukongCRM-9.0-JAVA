package com.kakarote.crm9.erp.admin.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.kakarote.crm9.common.config.cache.CaffeineCache;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
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
import com.jfinal.plugin.redis.Redis;

import java.util.*;

public class AdminUserService {
    @Inject
    private AdminRoleService adminRoleService;
    @Inject
    private AdminDeptService adminDeptService;

    @Before(Tx.class)
    public R setUser(AdminUser adminUser, String roleIds) {
        boolean bol;
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

    /**
     * 重置用户信息
     *
     * @return 用户信息
     */
    public AdminUser resetUser() {
        AdminUser adminUser = AdminUser.dao.findFirst(Db.getSql("admin.user.queryUserByUserId"), BaseUtil.getUserId());
        adminUser.setRoles(adminRoleService.queryRoleIdsByUserId(adminUser.getUserId()));
        Redis.use().setex(BaseUtil.getToken(), 360000, adminUser);
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
        if (query.size() != 0 && deepness > 0) {
            int size = query.size();
            for (int i = 0; i < size; i++) {
                query.addAll(queryChileUserIds(query.get(i),deepness-1));
            }
        }
        return query;
    }


    public R resetPassword(String ids, String pwd) {
        for (String id : ids.split(",")) {
            AdminUser adminUser = new AdminUser().dao().findById(id);
            String password = BaseUtil.sign(adminUser.getUsername() + pwd, adminUser.getSalt());
            Db.update("update 72crm_admin_user set password = ? where user_id = ?", password, id);
        }
        return R.ok();
    }

    public R querySuperior(String realName) {
        return R.ok().put("data", Db.find(Db.getSqlPara("admin.user.querySuperior", Kv.by("name", realName))));
    }

    public R queryListName(String name) {

        StringBuffer sql = new StringBuffer("select  au.realname,au.mobile,au.post as postName ,ad.name as deptName from 72crm_admin_user as au\n" + "LEFT JOIN 72crm_admin_dept as ad on au.dept_id = ad.dept_id");
        if (name != null && !"".equals(name)) {
            sql.append(" where au.realname like '%").append(name).append("%'");
        }
        List<Record> records = Db.find(sql.toString());
        Sort sort = new Sort();
        Map<String, List<Record>> map = sort.sort(records);
        return R.ok().put("data", map);
    }

    /**
     * @author zxy
     * 查询系统下属用户列表
     */
    public List<Integer> queryUserIdsByParentId(Integer userId) {
        String sql = "select user_id from 72crm_admin_user where parent_id = ? ";
        List<Record> records = Db.find(sql, userId);
        List<Integer> userIds = new ArrayList<>();
        for (Record record : records) {
            userIds.add(record.getInt("user_id"));
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
        if (null == deptIds) {
            return null;
        }
        String sql = "select user_id from 72crm_admin_user where dept_id in (" + deptIds + ")";
        List<Record> records = Db.find(sql);
        StringBuffer userIds = new StringBuffer();
        records.forEach(record -> {
            if (userIds.length() == 0) {
                userIds.append(record.getInt("user_id"));
            } else {
                userIds.append(",").append(record.getInt("user_id"));
            }
        });
        return userIds.toString();
    }

    public R queryAllUserList() {
        List<Record> recordList = Db.find(Db.getSqlPara("admin.user.queryUserList"));
        return R.ok().put("data", recordList);
    }

    public R setUserStatus(String ids, String status) {
        for (Integer id : TagUtil.toSet(ids)) {
            Db.update("update 72crm_admin_user set status = ? where user_id = ?", status, id);
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

    public List<Long> queryUserByAuth(Long userId) {
        List<Long> adminUsers = new ArrayList<>();
        //查询用户数据权限，从高到低排序
        List<Integer> list = Db.query(Db.getSql("admin.role.queryDataTypeByUserId"), userId);
        if (list.size() == 0) {
            //无权限查询自己的数据
            adminUsers.add(userId);
            return adminUsers;
        }
        //拥有最高数据权限
        if (list.contains(5)) {
            return null;
        } else {
            AdminUser adminUser = AdminUser.dao.findById(userId);
            if (list.contains(4)) {
                List<Record> records = adminDeptService.queryDeptByParentDept(adminUser.getDeptId(), BaseConstant.AUTH_DATA_RECURSION_NUM);
                List<Integer> deptIds = new ArrayList<>();
                records.forEach(record -> {
                    deptIds.add(record.getInt("id"));
                });
                SqlPara sqlPara = Db.getSqlPara("admin.user.queryUserIdByDeptId", Kv.by("deptIds", deptIds));
                adminUsers.addAll(Db.query(sqlPara.getSql(), sqlPara.getPara()));
            }
            if (list.contains(2)) {
                adminUsers.addAll(queryUserByParentUser(adminUser.getUserId(), BaseConstant.AUTH_DATA_RECURSION_NUM));
            }
            adminUsers.add(adminUser.getUserId());
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
    private String getUserIds(String deptIds,String userIds){
        List<Record> allUsers = Db.find("select * from 72crm_admin_user where dept_id   NOT in ( ? ) and user_id in (?)", deptIds, userIds);
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
