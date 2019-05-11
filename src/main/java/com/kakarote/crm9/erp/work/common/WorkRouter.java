package com.kakarote.crm9.erp.work.common;

import com.kakarote.crm9.erp.work.controller.TaskController;
import com.kakarote.crm9.erp.work.controller.WorkController;
import com.jfinal.config.Routes;

public class WorkRouter extends Routes {
    @Override
    public void config() {
        addInterceptor(new WorkInterceptor());
        add("/work", WorkController.class);
        add("/task", TaskController.class);
    }
}
