package com.kakarote.crm9.erp.oa.controller;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.oa.entity.OaExamine;
import com.kakarote.crm9.erp.oa.entity.OaExamineLog;
import com.kakarote.crm9.erp.oa.entity.OaExamineRelation;
import com.kakarote.crm9.erp.oa.service.OaExamineService;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;

public class OaExamineController extends Controller {

    @Inject
    private OaExamineService oaExamineService;

    /**
     * @author hmb
     * 我发起的审批
     * @param basePageRequest 分页对象
     */
    public void myInitiate(BasePageRequest<Void> basePageRequest){
        renderJson(oaExamineService.myInitiate(basePageRequest));
    }

    /**
     * @author hmb
     * 我审批的
     * @param basePageRequest 分页对象
     */
    public void myOaExamine(BasePageRequest<OaExamine> basePageRequest){
        renderJson(oaExamineService.myOaExamine(basePageRequest));
    }

    public void getField(){
        String id = getPara("examineId");
        renderJson(oaExamineService.getField(id));
    }

    /**
     * @author hmb
     * 创建审批
     */
    public void setOaExamine(){
        String data = getRawData();
        JSONObject jsonObject = JSON.parseObject(data);
        renderJson(oaExamineService.setOaExamine(jsonObject));
    }

    /**
     * @author hmb
     * 审批
     */
    public void auditExamine(){
        Integer recordId = getInt("recordId");
        Integer status = getInt("status");
        String remarks = get("remarks");
        Long nextUserId = getLong("nextUserId");
        OaExamineLog oaExamineLog = new OaExamineLog();
        oaExamineLog.setRecordId(recordId);
        oaExamineLog.setExamineStatus(status);
        oaExamineLog.setRemarks(remarks);
        renderJson(oaExamineService.oaExamine(oaExamineLog,nextUserId));
    }

    /**
     * @author hmb
     * 查询审批详情
     */
    @NotNullValidate(value = "examineId",message = "审批id不能为空")
    public void queryOaExamineInfo(){
        String id = getPara("examineId");
        renderJson(oaExamineService.queryOaExamineInfo(id));
    }

    /**
     * @author hmb
     * 查询审批步骤
     */
    public void queryExamineRecordList(){
        Integer recordId = getInt("recordId");
        renderJson(oaExamineService.queryExamineRecordList(recordId));
    }

    /**
     * @author hmb
     * 查询审批历史
     */
    @NotNullValidate(value = "recordId",message = "记录id不能为空")
    public void queryExamineLogList(){
        Integer recordId = getInt("recordId");
        renderJson(oaExamineService.queryExamineLogList(recordId));
    }

    /**
     * @author hmb
     * 删除审批
     */
    @NotNullValidate(value = "examineId",message = "审批id不能为空")
    public void deleteOaExamine(){
        Integer oaExamineId = getParaToInt("examineId");
        renderJson(oaExamineService.deleteOaExamine(oaExamineId));
    }

    /**
     * @author hmb
     * 查询审批步骤
     */
    public void queryExaminStep(){
        String categoryId = getPara("categoryId");
        renderJson(oaExamineService.queryExaminStep(categoryId));
    }

    /**
     * @author hmb
     * 查询审批关联业务
     */
    public void queryExamineRelation(@Para("")BasePageRequest<OaExamineRelation> pageRequest){
        renderJson(oaExamineService.queryExamineRelation(pageRequest));
    }

}
