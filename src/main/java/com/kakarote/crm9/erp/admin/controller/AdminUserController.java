package com.kakarote.crm9.erp.admin.controller;

import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.config.redis.RedisManager;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminFileService;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.jfinal.upload.UploadFile;

/**
 * @author hmb
 */
public class AdminUserController extends Controller {

    @Inject
    private AdminUserService adminUserService;

    @Inject
    private AdminFileService adminFileService;

    /**
     * @author hmb
     * 设置系统用户
     * @param adminUser
     */
    @Permissions("manage:users:userSave")
    public void addUser(@Para("") AdminUser adminUser){
        renderJson(adminUserService.setUser(adminUser,getPara("roleIds")));
    }

    @Permissions("manage:users:userUpdate")
    public void setUser(@Para("") AdminUser adminUser){
        renderJson(adminUserService.setUser(adminUser,getPara("roleIds")));
    }

    /**
     * @author hmb
     * 更新状态
     */
    @Permissions("manage:users:userEnables")
    public void setUserStatus(){
        String ids = getPara("userIds");
        String status = getPara("status");
        renderJson(adminUserService.setUserStatus(ids,status));
    }

    /**
     * @author hmb
     * 查询系统用户列表
     * @param basePageRequest 分页对象
     */
    public void queryUserList(BasePageRequest<AdminUser> basePageRequest){
        String roleId = getPara("roleId");
        renderJson(adminUserService.queryUserList(basePageRequest,roleId));
    }

    /**
     * @author hmb
     * 重置密码
     */
    @Permissions("manage:users:userEnables")
    public void resetPassword(){
        String ids = getPara("userIds");
        String pwd = getPara("password");
        renderJson(adminUserService.resetPassword(ids,pwd));
    }

    /**
     * @author hmb
     * 查询上级列表
     */
    @Permissions("manage:users:read")
    public void querySuperior(){
        String realName = getPara("realName");
        renderJson(adminUserService.querySuperior(realName));
    }

    /**
     * 查询所用用户列表
     * @author hmb
     */
    @Permissions("manage:users:read")
    public void queryAllUserList(){
        renderJson(adminUserService.queryAllUserList());
    }
    /**
     * @author zxy
     * 查询系统所有用户名称
     */
    public void queryListName(@Para("search") String search){
        renderJson(adminUserService.queryListName(search));
    }
    /**
     * @author zxy
     * 查询部门属用户列表
     */
    public void queryListNameByDept(@Para("name") String name){
        renderJson(adminUserService.queryListNameByDept(name));
    }

    /**
     * 查询当前登录的用户
     * @author zhangzhiwei
     */
    public void queryLoginUser(){
        renderJson(R.ok().put("data",adminUserService.resetUser(getRequest())));
    }

    public void updateImg(){
        String prefix= BaseUtil.getDate();
        UploadFile uploadFile=getFile("file",prefix);
        R r=adminFileService.upload(uploadFile,null,"file","/"+prefix,getRequest());
        if(r.isSuccess()){
            String url= (String) r.get("url");
            if(adminUserService.updateImg(url,getParaToLong("userId"))){
                renderJson(R.ok());
                return;
            }
        }
        renderJson(R.error("修改头像失败"));
    }
    public void updatePassword(){
        String oldPass=getPara("oldPwd");
        String newPass=getPara("newPwd");
        AdminUser adminUser=BaseUtil.getUser();
        if(!BaseUtil.verify(adminUser.getUsername()+oldPass,adminUser.getSalt(),adminUser.getPassword())){
            renderJson(R.error("密码输入错误"));
            return;
        }
        adminUser.setPassword(newPass);
        boolean b=adminUserService.updateUser(adminUser);
        if(b){
            BaseUtil.userExit(adminUser.getUserId(),null);
        }
        renderJson(R.isSuccess(b));
    }

    @NotNullValidate(value = "realname",message = "姓名不能为空")
    @NotNullValidate(value = "username",message = "用户名不能为空")
    public void updateUser(@Para("")AdminUser adminUser){
        boolean b=adminUserService.updateUser(adminUser);
        renderJson(R.isSuccess(b,"修改信息失败"));
    }

    /**
     *
     * @author zhangzhiwei
     * @param id 用户ID
     * @param username 用户新账号
     * @param password 用户新密码
     */
    @Permissions("manage:users:userUpdate")
    @NotNullValidate(value = "username",message = "账号不能为空")
    @NotNullValidate(value = "password",message = "密码不能为空")
    @NotNullValidate("id")
    public void usernameEdit(@Para("id")Integer id,@Para("username")String username,@Para("password")String password){
        renderJson(adminUserService.usernameEdit(id,username,password));

    }
    public void queryUserByDeptId(@Para("deptId")Integer deptId){
        renderJson(R.ok().put("data",adminUserService.queryAllUserByDeptId(deptId)));
    }
}
