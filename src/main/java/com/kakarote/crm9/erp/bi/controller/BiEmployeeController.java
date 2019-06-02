package com.kakarote.crm9.erp.bi.controller;

import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.erp.bi.service.BiEmployeeService;

/**
 * @author wyq
 */
public class BiEmployeeController extends Controller {
    @Inject
    BiEmployeeService biEmployeeService;

    /**
     * 合同数量分析
     * @author wyq
     */
    @NotNullValidate(value = "year",message = "年份不能为空")
    @NotNullValidate(value = "deptId",message = "部门id不能为空")
    public void contractNumStats(@Para("deptId")Integer deptId, @Para("userId")Long userId,@Para("year") String year){
        renderJson(biEmployeeService.contractNumStats(deptId,userId,"contractNum",year));
    }

    /**
     * 合同金额分析
     * @author wyq
     */
    public void contractMoneyStats(@Para("deptId")Integer deptId, @Para("userId")Long userId,@Para("year") String year){
        renderJson(biEmployeeService.contractNumStats(deptId,userId,"contractMoney",year));
    }

    /**
     * 回款金额分析
     * @author wyq
     */
    public void receivablesMoneyStats(@Para("deptId")Integer deptId, @Para("userId")Long userId ,@Para("year") String year){
        renderJson(biEmployeeService.contractNumStats(deptId,userId,"receivables",year));
    }

    /**
     * 合同汇总表
     * @author wyq
     */
    public void totalContract(@Para("deptId")Integer deptId,@Para("userId")Long userId,@Para("type")String type,@Para("startTime") String startTime,@Para("endTime")String endTime){
        renderJson(biEmployeeService.totalContract(deptId,userId,type,startTime,endTime));
    }
}
