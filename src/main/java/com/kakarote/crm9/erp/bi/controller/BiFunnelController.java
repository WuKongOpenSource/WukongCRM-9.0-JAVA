package com.kakarote.crm9.erp.bi.controller;

import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.erp.bi.service.BiFunnelService;

public class BiFunnelController extends Controller {
    @Inject
    private BiFunnelService service;
    /**
     * 销售漏斗
     * @author zxy
     */
    @Permissions("bi:business:read")
    public void sellFunnel(@Para("deptId")Integer deptId, @Para("userId")Long userId, @Para("type")String type,
                           @Para("startTime") String startTime, @Para("endTime")String endTime,@Para("typeId")Integer typeId){
        renderJson(service.sellFunnel(deptId,userId,type,startTime,endTime,typeId));
    }
    /**
     * 新增商机分析图
     * @author zxy
     */
    @Permissions("bi:business:read")
    public void addBusinessAnalyze(@Para("deptId")Integer deptId, @Para("userId")Long userId, @Para("type")String type,
                           @Para("startTime") String startTime, @Para("endTime")String endTime){
        renderJson(service.addBusinessAnalyze(deptId,userId,type,startTime,endTime));
    }
    /**
     * 新增商机分析表
     * @author zxy
     */
    @Permissions("bi:business:read")
    public void sellFunnelList(@Para("deptId")Integer deptId, @Para("userId")Long userId, @Para("type")String type,
                                   @Para("startTime") String startTime, @Para("endTime")String endTime){
        renderJson(service.sellFunnelList(deptId,userId,type,startTime,endTime));
    }
    /**
     * 商机转化率分析
     * @author zxy
     */
    @Permissions("bi:business:read")
    public void win(@Para("deptId")Integer deptId, @Para("userId")Long userId, @Para("type")String type,
                               @Para("startTime") String startTime, @Para("endTime")String endTime){
        renderJson(service.win(deptId,userId,type,startTime,endTime));
    }
}
