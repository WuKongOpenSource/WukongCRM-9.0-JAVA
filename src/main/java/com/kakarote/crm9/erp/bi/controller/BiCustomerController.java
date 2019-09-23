package com.kakarote.crm9.erp.bi.controller;

import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.erp.bi.service.BiCustomerService;

/**
 * @author wyq
 */
public class BiCustomerController extends Controller {
    @Inject
    BiCustomerService biCustomerService;

    /**
     * 客户总量分析柱状图
     * @param deptId
     * @param userId
     * @param type
     */
    @Permissions("bi:customer:read")
    @NotNullValidate(value = "deptId",message = "部门id不能为空")
    public void totalCustomerStats(@Para("deptId")Integer deptId,@Para("userId")Long userId,@Para("type")String type,@Para("startTime") String startTime,@Para("endTime")String endTime){
        renderJson(biCustomerService.totalCustomerStats(deptId,userId,type,startTime,endTime));
    }

    /**
     * 客户总量分析表
     * @author wyq
     */
    @Permissions("bi:customer:read")
    @NotNullValidate(value = "deptId",message = "部门id不能为空")
    public void totalCustomerTable(@Para("deptId")Integer deptId,@Para("userId")Long userId,@Para("type")String type,@Para("startTime") String startTime,@Para("endTime")String endTime){
        renderJson(biCustomerService.totalCustomerTable(deptId,userId,type,startTime,endTime));
    }

    /**
     * 客户跟进次数分析
     * @author wyq
     */
    @Permissions("bi:customer:read")
    public void customerRecordStats(@Para("deptId")Integer deptId,@Para("userId")Long userId,@Para("type")String type,@Para("startTime") String startTime,@Para("endTime")String endTime){
        renderJson(biCustomerService.customerRecordStats(deptId,userId,type,startTime,endTime));
    }

    /**
     * 客户跟进次数分析表
     * @author wyq
     */
    @Permissions("bi:customer:read")
    public void customerRecordInfo(@Para("deptId")Integer deptId,@Para("userId")Long userId,@Para("type")String type,@Para("startTime") String startTime,@Para("endTime")String endTime){
        renderJson(biCustomerService.customerRecordInfo(deptId,userId,type,startTime,endTime));
    }

    /**
     * 客户跟进方式分析
     * @author wyq
     */
    @Permissions("bi:customer:read")
    public void customerRecodCategoryStats(@Para("deptId")Integer deptId,@Para("userId")Long userId,@Para("type")String type,@Para("startTime") String startTime,@Para("endTime")String endTime){
        renderJson(biCustomerService.customerRecodCategoryStats(deptId,userId,type,startTime,endTime));
    }

    /**
     * 客户转化率分析图
     * @author wyq
     */
    @Permissions("bi:customer:read")
    public void customerConversionStats(@Para("deptId")Integer deptId,@Para("userId")Long userId,@Para("type")String type,@Para("startTime") String startTime,@Para("endTime")String endTime){
        renderJson(biCustomerService.customerConversionStats(deptId,userId,type,startTime,endTime));
    }

    /**
     * 客户转化率分析表
     * @author wyq
     */
    @Permissions("bi:customer:read")
    public void customerConversionInfo(@Para("deptId")Integer deptId,@Para("userId")Long userId,@Para("type")String type,@Para("startTime") String startTime,@Para("endTime")String endTime){
        renderJson(biCustomerService.customerConversionInfo(deptId,userId,type,startTime,endTime));
    }

    /**
     *公海客户分析图
     * @author wyq
     */
    @Permissions("bi:customer:read")
    public void poolStats(@Para("deptId")Integer deptId,@Para("userId")Long userId,@Para("type")String type,@Para("startTime") String startTime,@Para("endTime")String endTime){
        renderJson(biCustomerService.poolStats(deptId,userId,type,startTime,endTime));
    }

    /**
     *公海客户分析表
     * @author wyq
     */
    @Permissions("bi:customer:read")
    public void poolTable(@Para("deptId")Integer deptId,@Para("userId")Long userId,@Para("type")String type,@Para("startTime") String startTime,@Para("endTime")String endTime){
        renderJson(biCustomerService.poolTable(deptId,userId,type,startTime,endTime));
    }

    /**
     * 员工客户成交周期图
     * @author wyq
     */
    @Permissions("bi:customer:read")
    public void employeeCycle(@Para("deptId")Integer deptId,@Para("userId")Long userId,@Para("type")String type,@Para("startTime") String startTime,@Para("endTime")String endTime){
        renderJson(biCustomerService.employeeCycle(deptId,userId,type,startTime,endTime));
    }

    /**
     * 员工客户成交周期表
     * @author wyq
     */
    @Permissions("bi:customer:read")
    public void employeeCycleInfo(@Para("deptId")Integer deptId,@Para("userId")Long userId,@Para("type")String type,@Para("startTime") String startTime,@Para("endTime")String endTime){
        renderJson(biCustomerService.employeeCycleInfo(deptId,userId,type,startTime,endTime));
    }

    /**
     * 地区成交周期图
     * @author wyq
     */
    @Permissions("bi:customer:read")
    public void districtCycle(@Para("deptId")Integer deptId,@Para("userId")Long userId,@Para("type")String type,@Para("startTime") String startTime,@Para("endTime")String endTime){
        renderJson(biCustomerService.districtCycle(deptId,userId,type,startTime,endTime));
    }

    /**
     * 产品成交周期
     * @author wyq
     */
    @Permissions("bi:customer:read")
    public void productCycle(@Para("deptId")Integer deptId,@Para("userId")Long userId,@Para("type")String type,@Para("startTime") String startTime,@Para("endTime")String endTime){
        renderJson(biCustomerService.productCycle(deptId,userId,type,startTime,endTime));
    }
}
