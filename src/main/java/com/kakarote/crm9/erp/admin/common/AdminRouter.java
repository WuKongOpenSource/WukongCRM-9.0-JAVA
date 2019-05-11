package com.kakarote.crm9.erp.admin.common;

import com.jfinal.config.Routes;
import com.kakarote.crm9.erp.admin.controller.*;

public class AdminRouter extends Routes {
    @Override
    public void config() {
        addInterceptor(new AdminInterceptor());
        add("/", AdminLoginController.class);
        add("/manager/user", AdminUserController.class);
        add("/manager/dept", AdminDeptController.class);
        add("/manager/menu", AdminMenuController.class);
        add("/manager/role", AdminRoleController.class);
        add("/file", AdminFileController.class);
        add("/field",AdminFieldController.class);
        add("/scene",AdminSceneController.class);
        add("/businessType", AdminBusinessTypeController.class);
        add("/achievement",AdminAchievementController.class);
        add("/sysConfig",AdminSysConfigController.class);
        add("/examine", AdminExamineController.class);
        add("/examineRecord", AdminExamineRecordController.class);
    }
}
