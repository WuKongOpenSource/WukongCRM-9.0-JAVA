package com.kakarote.crm9.erp.bi.controller;

import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.erp.bi.service.BiRankingService;

public class BiRankingController extends Controller {
    @Inject
    private BiRankingService service;
    /**
     * 合同金额排行榜
     * @author zxy
     */
    @Permissions("bi:ranking:read")
    public void contractRanKing(@Para("deptId")Integer deptId, @Para("type")String type, @Para("startTime") String startTime, @Para("endTime")String endTime){
        renderJson(service.contractRanKing(deptId,type,startTime,endTime));
    }
    /**
     * 回款金额排行榜
     * @author zxy
     */
    @Permissions("bi:ranking:read")
    public void receivablesRanKing(@Para("deptId")Integer deptId, @Para("type")String type, @Para("startTime") String startTime, @Para("endTime")String endTime){
        renderJson(service.receivablesRanKing(deptId,type,startTime,endTime));
    }
    /**
     * 签约合同排行榜
     * @author zxy
     */
    @Permissions("bi:ranking:read")
    public void contractCountRanKing(@Para("deptId")Integer deptId, @Para("type")String type, @Para("startTime") String startTime, @Para("endTime")String endTime){
        renderJson(service.contractCountRanKing(deptId,type,startTime,endTime));
    }
    /**
     * 产品销量排行榜
     * @author zxy
     */
    @Permissions("bi:ranking:read")
    public void productCountRanKing(@Para("deptId")Integer deptId, @Para("type")String type, @Para("startTime") String startTime, @Para("endTime")String endTime){
        renderJson(service.productCountRanKing(deptId,type,startTime,endTime));
    }
    /**
     * 新增客户数排行榜
     * @author zxy
     */
    @Permissions("bi:ranking:read")
    public void customerCountRanKing(@Para("deptId")Integer deptId, @Para("type")String type, @Para("startTime") String startTime, @Para("endTime")String endTime){
        renderJson(service.customerCountRanKing(deptId,type,startTime,endTime));
    }
    /**
     * 新增联系人排行榜
     * @author zxy
     */
    @Permissions("bi:ranking:read")
    public void contactsCountRanKing(@Para("deptId")Integer deptId, @Para("type")String type, @Para("startTime") String startTime, @Para("endTime")String endTime){
        renderJson(service.contactsCountRanKing(deptId,type,startTime,endTime));
    }
    /**
     * 跟进客户数排行榜
     * @author zxy
     */
    @Permissions("bi:ranking:read")
    public void customerGenjinCountRanKing(@Para("deptId")Integer deptId, @Para("type")String type, @Para("startTime") String startTime, @Para("endTime")String endTime){
        renderJson(service.customerGenjinCountRanKing(deptId,type,startTime,endTime));
    }
    /**
     * 跟进次数排行榜
     * @author zxy
     */
    @Permissions("bi:ranking:read")
    public void recordCountRanKing(@Para("deptId")Integer deptId, @Para("type")String type, @Para("startTime") String startTime, @Para("endTime")String endTime){
        renderJson(service.recordCountRanKing(deptId,type,startTime,endTime));
    }
    /**
     * 产品分类销量分析
     * @author zxy
     */
    @Permissions("bi:ranking:read")
    public void contractProductRanKing(@Para("deptId")Integer deptId,@Para("userId")Long userId, @Para("type")String type, @Para("startTime") String startTime, @Para("endTime")String endTime){
        renderJson(service.contractProductRanKing(deptId,userId,type,startTime,endTime));
    }
    /**
     * 出差次数排行
     * @author zxy
     */
    @Permissions("bi:ranking:read")
    public void travelCountRanKing(@Para("deptId")Integer deptId, @Para("type")String type, @Para("startTime") String startTime, @Para("endTime")String endTime){
        renderJson(service.travelCountRanKing(deptId,type,startTime,endTime));
    }
    /**
     * 产品销售情况统计
     * @author zxy
     */
    @Permissions("bi:product:read")
    public void productSellRanKing(@Para("deptId")Integer deptId,@Para("userId")Long userId, @Para("type")String type, @Para("startTime") String startTime, @Para("endTime")String endTime){
        renderJson(service.productSellRanKing(deptId,userId,type,startTime,endTime));
    }
    /**
     * 城市分布分析
     * @author zxy
     */
    @Permissions("bi:portrait:read")
    public void addressAnalyse(){
        renderJson(service.addressAnalyse());
    }
    /**
     * 客户行业分析
     * @author zxy
     */
    @Permissions("bi:portrait:read")
    public void portrait(@Para("deptId")Integer deptId,@Para("userId")Long userId, @Para("type")String type, @Para("startTime") String startTime, @Para("endTime")String endTime){
        renderJson(service.portrait(deptId,userId,type,startTime,endTime));
    }
    /**
     * 客户级别分析
     * @author zxy
     */
    @Permissions("bi:portrait:read")
    public void portraitLevel(@Para("deptId")Integer deptId,@Para("userId")Long userId, @Para("type")String type, @Para("startTime") String startTime, @Para("endTime")String endTime){
        renderJson(service.portraitLevel(deptId,userId,type,startTime,endTime));
    }
    /**
     * 客户级别分析
     * @author zxy
     */
    @Permissions("bi:portrait:read")
    public void portraitSource(@Para("deptId")Integer deptId,@Para("userId")Long userId, @Para("type")String type, @Para("startTime") String startTime, @Para("endTime")String endTime){
        renderJson(service.portraitSource(deptId,userId,type,startTime,endTime));
    }
}
