package com.kakarote.crm9.erp.oa.controller;

import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.kakarote.crm9.erp.oa.service.OaBackLogService;

/**
 * @author wyq
 */
public class OaBackLogController extends Controller {
    @Inject
    OaBackLogService oaBackLogService;

    /**
     * oa代办事项提醒
     */
    public void num(){
        renderJson(oaBackLogService.backLogNum());
    }
}
