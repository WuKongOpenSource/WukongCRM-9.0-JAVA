package com.kakarote.crm9.erp.admin.controller;

import com.alibaba.fastjson.JSON;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.erp.admin.entity.CrmAchievement;
import com.kakarote.crm9.erp.admin.service.AdminAchievementService;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;

import java.util.List;

/**
 * 业绩目标设置
 * @author hmb
 */
public class AdminAchievementController extends Controller {

    @Inject
    private AdminAchievementService adminAchievementService;

    /**
     * 设置业绩目标
     * @author hmb
     */
    @Permissions("manage:crm:achievement")
    public void setAchievement(){
        String data = getRawData();
        List<CrmAchievement> crmAchievements = JSON.parseArray(data, CrmAchievement.class);
        renderJson(adminAchievementService.setAchievement(crmAchievements));
    }

    /**
     * 查询业绩目标列表
     * @param achievement 业绩目标对象
     * @author hmb
     */
    @Permissions("manage:crm:achievement")
    public void queryAchievementList(@Para("")CrmAchievement achievement){
        String userId = getPara("userId");
        Integer deptId = getParaToInt("deptId");
        renderJson(adminAchievementService.queryAchievementList(achievement,userId,deptId));
    }


}
