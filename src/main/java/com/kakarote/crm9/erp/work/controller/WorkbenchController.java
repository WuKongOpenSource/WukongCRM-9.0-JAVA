package com.kakarote.crm9.erp.work.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.kakarote.crm9.erp.work.service.WorkbenchService;
import com.kakarote.crm9.utils.BaseUtil;

/**
 * 工作台
 */
public class WorkbenchController extends Controller{

    @Inject
    private WorkbenchService workbenchService;

    /**
     * @author hmb
     * 我的任务
     */
    public void myTask(){
        renderJson(workbenchService.myTask(BaseUtil.getUser().getUserId()));
    }

    /**
     * @author hmb
     * 任务日历
     */
    public void dateList(){
        String startTime = getPara("startTime");
        String endTime = getPara("endTime");
        renderJson(workbenchService.dateList(startTime,endTime));
    }

    /**
     * @author hmb
     * 修改工作台任务排序
     */
    public void updateTop(){
        JSONObject jsonObject = JSON.parseObject(getRawData());
        renderJson(workbenchService.updateTop(jsonObject));
    }
}
