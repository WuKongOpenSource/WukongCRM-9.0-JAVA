package com.kakarote.crm9.erp.admin.common;

import com.jfinal.config.Routes;
import com.kakarote.crm9.erp.admin.controller.*;

public class AdminRouter extends Routes {
    @Override
    public void config() {
        addInterceptor(new AdminInterceptor());
        add("/", AdminLoginController.class);
        add("/system/user", AdminUserController.class);
        add("/system/dept", AdminDeptController.class);
        add("/system/menu", AdminMenuController.class);
        add("/system/role", AdminRoleController.class);
        add("/system/message", AdminMessageController.class);
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
