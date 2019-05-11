package com.kakarote.crm9.erp.bi.common;

import com.kakarote.crm9.erp.bi.controller.BiController;
import com.jfinal.config.Routes;

public class BiRouter extends Routes {
    @Override
    public void config() {
        addInterceptor(new BiInterceptor());
        add("/bi", BiController.class);
    }
}
