package com.kakarote.crm9.erp.crm.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.plugin.activerecord.Page;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.entity.AdminRecord;
import com.kakarote.crm9.erp.admin.service.AdminSceneService;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.entity.CrmContract;
import com.kakarote.crm9.erp.crm.entity.CrmContractProduct;
import com.kakarote.crm9.erp.crm.entity.CrmReceivables;
import com.kakarote.crm9.erp.crm.service.CrmContractService;
import com.kakarote.crm9.erp.crm.service.CrmReceivablesPlanService;
import com.kakarote.crm9.erp.crm.service.CrmReceivablesService;
import com.kakarote.crm9.utils.AuthUtil;
import com.kakarote.crm9.utils.R;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;


public class CrmContractController extends Controller {
    @Inject
    private CrmContractService crmContractService;
    @Inject
    private CrmReceivablesService receivablesService;
    @Inject
    private CrmReceivablesPlanService receivablesPlanService;

    @Inject
    private AdminSceneService adminSceneService;

    /**
     * @author wyq
     * 查看列表页
     */
    @Permissions({"crm:contract:index"})
    public void queryPageList(BasePageRequest basePageRequest){
        JSONObject jsonObject = basePageRequest.getJsonObject().fluentPut("type",6);
        basePageRequest.setJsonObject(jsonObject);
        renderJson(adminSceneService.filterConditionAndGetPageList(basePageRequest));
    }

    /**
     * 分页条件查询合同
     * @author zxy
     */
    public void queryPage(BasePageRequest<CrmContract> basePageRequest){
        renderJson(R.ok().put("data",crmContractService.queryPage(basePageRequest)));
    }
    /**
     * 根据id查询合同
     * @author zxy
     */
    @Permissions("crm:contract:read")
    @NotNullValidate(value = "contractId",message = "合同id不能为空")
    public void queryById(@Para("contractId") Integer id){
        renderJson(crmContractService.queryById(id));
    }
    /**
     * 根据id删除合同
     * @author zxy
     */
    @Permissions("crm:contract:delete")
    @NotNullValidate(value = "contractIds",message = "合同id不能为空")
    public void deleteByIds(@Para("contractIds") String contractIds){
        renderJson(crmContractService.deleteByIds(contractIds));
    }
    /**
     * @author wyq
     * 合同转移
     */
    @Permissions("crm:contract:transfer")
    @NotNullValidate(value = "contractIds",message = "合同id不能为空")
    @NotNullValidate(value = "newOwnerUserId",message = "负责人id不能为空")
    @NotNullValidate(value = "transferType",message = "移除方式不能为空")
    public void transfer(@Para("")CrmContract crmContract){
        renderJson(crmContractService.transfer(crmContract));
    }
    /**
     * 添加或修改
     * @author zxy
     */
    @Permissions({"crm:contract:save","crm:contract:update"})
    public void saveAndUpdate(){
        String data = getRawData();
        JSONObject jsonObject = JSON.parseObject(data);
        renderJson(crmContractService.saveAndUpdate(jsonObject));
    }
    /**
     * 根据条件查询合同
     * @author zxy
     */
    public void queryList(@Para("")CrmContract crmContract){
        renderJson(R.ok().put("data",crmContractService.queryList(crmContract)));
    }
    /**
     * 根据条件查询合同
     * @author zxy
     */
    @NotNullValidate(value = "id",message = "id不能为空")
    @NotNullValidate(value = "type",message = "类型不能为空")
    public void queryListByType(@Para("type") String type,@Para("id")Integer id ){
        renderJson(R.ok().put("data",crmContractService.queryListByType(type,id)));
    }
    /**
     根据合同批次查询产品
     * @param batchId
     * @author zxy
     */
    public void queryProductById(@Para("batchId") String batchId){
        renderJson(R.ok().put("data",crmContractService.queryProductById(batchId)));
    }
    /**
     * 根据合同id查询回款
     * @author zxy
     */
    public void queryReceivablesById(@Para("id") Integer id){
        renderJson(R.ok().put("data",crmContractService.queryReceivablesById(id)));
    }
    /**
     * 根据合同id查询回款计划
     * @author zxy
     */
    public void queryReceivablesPlanById(@Para("id") Integer id){
        renderJson(R.ok().put("data",crmContractService.queryReceivablesPlanById(id)));
    }

    /**
     * @author wyq
     * 查询团队成员
     */
    public void getMembers(@Para("contractId")Integer contractId){
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.CONTRACT_TYPE_KEY.getSign()), contractId);
        if(auth){renderJson(R.noAuth()); return; }
        renderJson(R.ok().put("data",crmContractService.getMembers(contractId)));
    }

    /**
     * @author wyq
     * 编辑团队成员
     */
    public void updateMembers(@Para("")CrmContract crmContract){
        renderJson(crmContractService.addMember(crmContract));
    }

    /**
     * @author wyq
     * 添加团队成员
     */
    @Permissions("crm:contract:teamsave")
    public void addMembers(@Para("")CrmContract crmContract){
        renderJson(crmContractService.addMember(crmContract));
    }

    /**
     * @author wyq
     * 删除团队成员
     */
    public void deleteMembers(@Para("")CrmContract crmContract){
        renderJson(crmContractService.deleteMembers(crmContract));
    }

    /**
     * @author wyq
     * 添加跟进记录
     */
    @NotNullValidate(value = "typesId",message = "合同id不能为空")
    @NotNullValidate(value = "content",message = "内容不能为空")
    @NotNullValidate(value = "category",message = "跟进类型不能为空")
    public void addRecord(@Para("")AdminRecord adminRecord){
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.CONTRACT_TYPE_KEY.getSign()), adminRecord.getTypesId());
        if(auth){renderJson(R.noAuth()); return; }
        renderJson(crmContractService.addRecord(adminRecord));
    }

    /**
     * @author wyq
     * 查看跟进记录
     */
    public void getRecord(BasePageRequest<CrmContract> basePageRequest){
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.CONTRACT_TYPE_KEY.getSign()), basePageRequest.getData().getContractId());
        if(auth){renderJson(R.noAuth()); return; }
        renderJson(R.ok().put("data",crmContractService.getRecord(basePageRequest)));
    }
    /**
     * 根据合同ID查询回款
     * @author zxy
     */
    public void qureyReceivablesListByContractId(BasePageRequest<CrmReceivables> basePageRequest){
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.CONTRACT_TYPE_KEY.getSign()), basePageRequest.getData().getContractId());
        if(auth){renderJson(R.noAuth()); return; }
        renderJson(receivablesService.qureyListByContractId(basePageRequest));
    }
    /**
     * 根据合同ID查询产品
     * @author zxy
     */
    public void qureyProductListByContractId(BasePageRequest<CrmContractProduct> basePageRequest){
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.CONTRACT_TYPE_KEY.getSign()), basePageRequest.getData().getContractId());
        if(auth){renderJson(R.noAuth()); return; }
        renderJson(crmContractService.qureyProductListByContractId(basePageRequest));
    }
    /**
     * 根据合同ID查询回款计划
     * @author zxy
     */
    public void qureyReceivablesPlanListByContractId(BasePageRequest<CrmReceivables> basePageRequest){
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.CONTRACT_TYPE_KEY.getSign()), basePageRequest.getData().getContractId());
        if(auth){renderJson(R.noAuth()); return; }
        renderJson(receivablesPlanService.qureyListByContractId(basePageRequest));
    }

    /**
     * 查询合同到期提醒设置
     */
    public void queryContractConfig(){
        renderJson(crmContractService.queryContractConfig());
    }

    /**
     * 修改合同到期提醒设置
     */
    @NotNullValidate(value = "status",message = "status不能为空")
    public void setContractConfig(@Para("status") Integer status,@Para("contractDay") Integer contractDay){
        renderJson(crmContractService.setContractConfig(status,contractDay));
    }
}
