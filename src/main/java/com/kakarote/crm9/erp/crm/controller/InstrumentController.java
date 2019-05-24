package com.kakarote.crm9.erp.crm.controller;

import cn.hutool.core.util.StrUtil;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.erp.crm.service.InstrumentService;
import com.kakarote.crm9.utils.BaseUtil;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;

import java.util.Date;

public class InstrumentController extends Controller {

    @Inject
    private InstrumentService instrumentService;
    @Inject
    private AdminUserService adminUserService;

    /**
     * 销售简报
     *
     * @author zxy
     * type 1.今天 2.昨天 3.本周 4.上周 5.本月6.上月7.本季度8.上季度9.本年10上年
     * deptIds 部门id
     * userIds 员工或者部门id拼写id之间用‘,’隔开
     */
    @NotNullValidate(value = "type", message = "类型不能为空")
    public void queryBulletin() {
        Integer type = getInt("type");
        String userIds = getPara("userIds");
        String deptIds = getPara("deptIds");
        if (userIds == null) {
            userIds = BaseUtil.getUser().getUserId().intValue() + "";
        } else if (deptIds != null && StrUtil.isNotEmpty(deptIds)) {
            userIds = adminUserService.queryUserIdsByDept(deptIds) + "," + userIds;
        }

        renderJson(instrumentService.queryBulletin(type, userIds));
    }

    /**
     * 销售趋势
     * type 1.月份 2.季度
     * userIds 员工id拼写id之间用‘,’隔开
     * deptIds 部门id
     */
    @NotNullValidate(value = "type", message = "类型不能为空")
    public void sellMonth() {
        Integer type = getInt("type");
        String userIds = getPara("userIds");
        String year = getPara("year");
        String deptIds = getPara("deptIds");
        if (userIds == null) {
            userIds = BaseUtil.getUser().getUserId().intValue() + "";
        } else if (deptIds != null && StrUtil.isNotEmpty(deptIds)) {
            userIds = adminUserService.queryUserIdsByDept(deptIds) + "," + userIds;
        }
        if (type == 1) {
            renderJson(instrumentService.sellMonth(year, userIds));
        } else {
            renderJson(instrumentService.sellYears(year, userIds));
        }
    }

    /**
     * 业绩指标
     * type 1 回款 2.合同
     * deptIds 门id拼写id之间用‘,’隔开
     * userIds 员工id拼写id之间用‘,’隔开
     */
    @NotNullValidate(value = "type", message = "类型不能为空")
    @NotNullValidate(value = "startTime", message = "开始年月不能为空")
    @NotNullValidate(value = "endTime", message = "结束年月不能为空")
    public void queryPerformance() {
        Integer type = getInt("type");
        String userIds = getPara("userIds");
        String startTime = getPara("startTime");
        String endTime = getPara("endTime");
        String deptIds = getPara("deptIds");
        if (StrUtil.isNotEmpty(deptIds)) {
            userIds = adminUserService.queryUserIdsByDept(deptIds) + "," + userIds;
            renderJson(instrumentService.queryPerformance(startTime, endTime, userIds, type, 2));
        }else {
            if (userIds == null) {
                userIds = BaseUtil.getUser().getUserId().intValue() + "";
            }
            renderJson(instrumentService.queryPerformance(startTime, endTime, userIds, type, 3));
        }
    }
    /**
     * 销售漏斗
     */
    public void queryBusiness() {
        Date startTime = getDate("startTime");
        Date endTime = getDate("endTime");
        String userIds = getPara("userIds");
        Integer typeId = getInt("typeId");
        String deptIds = getPara("deptIds");
        if (userIds == null) {
            userIds = BaseUtil.getUser().getUserId().intValue() + "";
        }
        renderJson(instrumentService.queryBusiness(userIds,deptIds,typeId,startTime, endTime));
    }
}
