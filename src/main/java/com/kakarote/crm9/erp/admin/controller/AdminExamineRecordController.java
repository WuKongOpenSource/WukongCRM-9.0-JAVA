package com.kakarote.crm9.erp.admin.controller;

import com.kakarote.crm9.erp.admin.service.AdminExamineRecordService;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;

/**
 * 审核合同或回款
 * @author zxy
 */
public class AdminExamineRecordController extends Controller {
    @Inject
    private AdminExamineRecordService examineRecordService;

    /**
     * 审核合同或者回款 recordId:审核记录id status:审批状态：审核状态  1 审核通过 2 审核拒绝 4 已撤回
     * remarks:审核备注 id:审核对象的id（合同或者回款的id）
     */
    public void auditExamine(){
        Integer recordId = getInt("recordId");
        Integer status = getInt("status");
        Integer id = getInt("id");
        String remarks = get("remarks");
        Long nextUserId = getLong("nextUserId");
        Long ownerUserId = getLong("ownerUserId");
        renderJson(examineRecordService.auditExamine(recordId,status,remarks,id,nextUserId,ownerUserId));
    }
    /**
     * 根据审核记录id，查询审核日志
     * recordId 审核记录id
     */
    public void queryExamineLogList(){
        Integer recordId = getInt("recordId");
        renderJson(examineRecordService.queryExamineLogList(recordId));
    }
    /**
     * 根据审核记录id，查询审核日志
     * recordId 审核记录id ownerUserId 负责人ID
     */
    public void queryExamineRecordList(){
        Integer recordId = getInt("recordId");
        Integer ownerUserId = getInt("ownerUserId");
        renderJson(examineRecordService.queryExamineRecordList(recordId,ownerUserId));
    }
}
