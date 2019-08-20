package com.kakarote.crm9.erp.crm.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.entity.AdminRecord;
import com.kakarote.crm9.erp.admin.service.AdminSceneService;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.entity.CrmBusiness;
import com.kakarote.crm9.erp.crm.service.CrmBusinessService;
import com.kakarote.crm9.utils.AuthUtil;
import com.kakarote.crm9.utils.R;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;

public class CrmBusinessController extends Controller {
    @Inject
    private CrmBusinessService crmBusinessService;

    @Inject
    private AdminSceneService adminSceneService;

    /**
     * @author wyq
     * 查看列表页
     */
    @Permissions({"crm:business:index"})
    public void queryPageList(BasePageRequest basePageRequest){
        JSONObject jsonObject = basePageRequest.getJsonObject().fluentPut("type",5);
        basePageRequest.setJsonObject(jsonObject);
        renderJson(adminSceneService.filterConditionAndGetPageList(basePageRequest));
    }

    /**
     * @author wyq
     * 全局搜索查询商机
     */
    public void queryList(BasePageRequest basePageRequest){
        renderJson(R.ok().put("data",crmBusinessService.getBusinessPageList(basePageRequest)));
    }

    /**
     * @author wyq
     * 新增或更新商机
     */
    @Permissions({"crm:business:save","crm:business:update"})
    public void addOrUpdate(){
        JSONObject jsonObject = JSON.parseObject(getRawData());
        renderJson(crmBusinessService.addOrUpdate(jsonObject));
    }

    /**
     * @author wyq
     * 根据商机id查询
     */
    @Permissions("crm:business:read")
    @NotNullValidate(value = "businessId",message = "商机id不能为空")
    public void queryById(@Para("businessId")Integer businessId){
        renderJson(crmBusinessService.queryById(businessId));
    }

    /**
     * @author wyq
     * 根据商机名称查询
     */
    @NotNullValidate(value = "name",message = "名称不能为空")
    public void queryByName(@Para("name")String name){
        renderJson(R.ok().put("data",crmBusinessService.queryByName(name)));
    }

    /**
     * @author wyq
     * 根据商机id查询产品
     */
    public void queryProduct(BasePageRequest<CrmBusiness> basePageRequest){
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.BUSINESS_TYPE_KEY.getSign()), basePageRequest.getData().getBusinessId());
        if(auth){renderJson(R.noAuth()); return; }
        renderJson(crmBusinessService.queryProduct(basePageRequest));
    }

    /**
     * @author wyq
     * 根据商机id查询合同
     */
    public void queryContract(BasePageRequest<CrmBusiness> basePageRequest){
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.BUSINESS_TYPE_KEY.getSign()), basePageRequest.getData().getBusinessId());
        if(auth){renderJson(R.noAuth()); return; }
        renderJson(crmBusinessService.queryContract(basePageRequest));
    }

    /**
     * @author wyq
     * 根据商机id查询联系人
     */
    public void queryContacts(BasePageRequest<CrmBusiness> basePageRequest){
        renderJson(crmBusinessService.queryContacts(basePageRequest));
    }

    /**
     * @author wyq
     * 商机关联联系人
     */
    public void relateContacts(@Para("businessId")Integer businessId,@Para("contactsIds")String contactsIds){
        renderJson(crmBusinessService.relateContacts(businessId,contactsIds));
    }

    /**
     * @author wyq
     * 商机解除关联联系人
     */
    public void unrelateContacts(@Para("businessId")Integer businessId,@Para("contactsIds")String contactsIds){
        renderJson(crmBusinessService.unrelateContacts(businessId,contactsIds));
    }

    /**
     * @author wyq
     * 根据id删除商机
     */
    @Permissions("crm:business:delete")
    @NotNullValidate(value = "businessIds",message = "商机id不能为空")
    public void deleteByIds(@Para("businessIds")String businessIds){
        renderJson(crmBusinessService.deleteByIds(businessIds));
    }

    /**
     * @author wyq
     * 根据商机id变更负责人
     */
    @Permissions("crm:business:transfer")
    @NotNullValidate(value = "businessIds",message = "商机id不能为空")
    @NotNullValidate(value = "newOwnerUserId",message = "负责人id不能为空")
    @NotNullValidate(value = "transferType",message = "移除方式不能为空")
    public void transfer(@Para("")CrmBusiness crmBusiness){
        renderJson(crmBusinessService.transfer(crmBusiness));
    }

    /**
     * @author wyq
     * 查询团队成员
     */
    @NotNullValidate(value = "businessId",message = "商机id不能为空")
    public void getMembers(@Para("businessId")Integer businessId){
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.BUSINESS_TYPE_KEY.getSign()), businessId);
        if(auth){renderJson(R.noAuth()); return; }
        renderJson(R.ok().put("data",crmBusinessService.getMembers(businessId)));
    }

    /**
     * @author wyq
     * 添加团队成员
     */
    @Permissions("crm:business:teamsave")
    @NotNullValidate(value = "ids",message = "商机id不能为空")
    @NotNullValidate(value = "memberIds",message = "成员id不能为空")
    @NotNullValidate(value = "power",message = "读写权限不能为空")
    public void addMembers(@Para("")CrmBusiness crmBusiness){
        renderJson(crmBusinessService.addMember(crmBusiness));
    }

    /**
     * @author wyq
     * 编辑团队成员
     */
    @NotNullValidate(value = "ids",message = "商机id不能为空")
    @NotNullValidate(value = "memberIds",message = "成员id不能为空")
    @NotNullValidate(value = "power",message = "读写权限不能为空")
    public void updateMembers(@Para("")CrmBusiness crmBusiness){
        renderJson(crmBusinessService.addMember(crmBusiness));
    }

    /**
     * @author wyq
     * 删除团队成员
     */
    @NotNullValidate(value = "ids",message = "商机id不能为空")
    @NotNullValidate(value = "memberIds",message = "成员id不能为空")
    public void deleteMembers(@Para("")CrmBusiness crmBusiness){
        renderJson(crmBusinessService.deleteMembers(crmBusiness));
    }

    /**
     * @author
     * 商机状态组展示
     */
    @NotNullValidate(value = "businessId",message = "商机id不能为空")
    public void queryBusinessStatus(@Para("businessId")Integer businessId){
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.BUSINESS_TYPE_KEY.getSign()), businessId);
        if(auth){renderJson(R.noAuth()); return; }
        renderJson(R.ok().put("data",crmBusinessService.queryBusinessStatus(businessId)));
    }

    /**
     * @author wyq
     * 商机状态组推进
     */
    @NotNullValidate(value = "businessId",message = "商机id不能为空")
    public void boostBusinessStatus(@Para("")CrmBusiness crmBusiness){
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.BUSINESS_TYPE_KEY.getSign()), crmBusiness.getBusinessId());
        if(auth){renderJson(R.noAuth()); return; }
        renderJson(crmBusinessService.boostBusinessStatus(crmBusiness));
    }
    /**
     * @author wyq
     * 查询商机状态组及商机状态
     */
    public void queryBusinessStatusOptions(){
        renderJson(R.ok().put("data",crmBusinessService.queryBusinessStatusOptions(null)));
    }

    /**
     * @author wyq
     * 添加跟进记录
     */
    @NotNullValidate(value = "typesId",message = "商机id不能为空")
    @NotNullValidate(value = "content",message = "内容不能为空")
    @NotNullValidate(value = "category",message = "跟进类型不能为空")
    public void addRecord(@Para("")AdminRecord adminRecord){
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.BUSINESS_TYPE_KEY.getSign()), adminRecord.getTypesId());
        if(auth){renderJson(R.noAuth()); return; }
        renderJson(crmBusinessService.addRecord(adminRecord));
    }

    /**
     * @author wyq
     * 查看跟进记录
     */
    public void getRecord(BasePageRequest<CrmBusiness> basePageRequest){
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.BUSINESS_TYPE_KEY.getSign()), basePageRequest.getData().getBusinessId());
        if(auth){renderJson(R.noAuth()); return; }
        renderJson(R.ok().put("data",crmBusinessService.getRecord(basePageRequest)));
    }

}
