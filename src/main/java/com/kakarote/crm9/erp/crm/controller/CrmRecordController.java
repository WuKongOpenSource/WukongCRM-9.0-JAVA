package com.kakarote.crm9.erp.crm.controller;

import com.kakarote.crm9.erp.crm.service.CrmRecordService;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;


public class CrmRecordController extends Controller {

    @Inject
    private CrmRecordService crmRecordService;

    /**
     * @author hmb
     * 查询操作记录列表
     */
    public void queryRecordList(){
        String actionId = getPara("actionId");
        String types = getPara("types");
        renderJson(crmRecordService.queryRecordList(actionId,types));
    }

    /**
     * @author wyq
     * 删除跟进记录
     */
    public void deleteFollowRecord(@Para("recordId")Integer recordId){
        renderJson(crmRecordService.deleteFollowRecord(recordId));
    }
}
