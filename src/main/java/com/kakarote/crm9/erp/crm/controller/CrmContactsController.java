package com.kakarote.crm9.erp.crm.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.entity.AdminRecord;
import com.kakarote.crm9.erp.crm.entity.CrmContacts;
import com.kakarote.crm9.erp.crm.service.CrmContactsService;
import com.kakarote.crm9.utils.R;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;

public class CrmContactsController extends Controller {
    @Inject
    private CrmContactsService crmContactsService;

    /**
     * @author wyq
     * 分页条件查询联系人
     */
    public void queryList(BasePageRequest basePageRequest){
        renderJson(R.ok().put("data",crmContactsService.queryList(basePageRequest)));
    }

    /**
     * @author wyq
     * 根据id查询联系人
     */
    @Permissions("crm:contacts:read")
    public void queryById(@Para("contactsId")Integer contactsId){
         renderJson(R.ok().put("data",crmContactsService.queryById(contactsId)));
    }

    /**
     * @author wyq
     * 根据联系人名称查询
     */
    public void queryByName(@Para("name")String name){
        renderJson(R.ok().put("data",crmContactsService.queryByName(name)));
    }

    /**
     * @author wyq
     * 根据联系人id查询商机
     */
    public void queryBusiness(BasePageRequest<CrmContacts> basePageRequest){
        renderJson(crmContactsService.queryBusiness(basePageRequest));
    }

    /**
     * @author wyq
     * 新建或更新联系人
     */
    @Permissions({"crm:contacts:save","crm:contacts:update"})
    public void addOrUpdate(){
        JSONObject jsonObject = JSON.parseObject(getRawData());
        renderJson(crmContactsService.addOrUpdate(jsonObject));
    }

    /**
     * @author wyq
     * 根据id删除联系人
     */
    @Permissions("crm:contacts:delete")
    public void deleteByIds(@Para("contactsIds")String contactsIds){
        renderJson(crmContactsService.deleteByIds(contactsIds));
    }

    /**
     * @author wyq
     * 联系人转移
     */
    @Permissions("crm:contacts:transfer")
    @NotNullValidate(value = "contactsIds",message = "联系人id不能为空")
    @NotNullValidate(value = "newOwnerUserId",message = "新负责人不能为空")
    public void transfer(@Para("")CrmContacts crmContacts){
        renderJson(crmContactsService.transfer(crmContacts));
    }

    /**
     * @author wyq
     * 查询自定义字段
     */
    public void queryField(){
        renderJson(R.ok().put("data",crmContactsService.queryField()));
    }

    /**
     * @author wyq
     * 添加跟进记录
     */
    @NotNullValidate(value = "typesId",message = "联系人id不能为空")
    @NotNullValidate(value = "content",message = "内容不能为空")
    @NotNullValidate(value = "category",message = "跟进类型不能为空")
    public void addRecord(@Para("")AdminRecord adminRecord){
        renderJson(crmContactsService.addRecord(adminRecord));
    }

    /**
     * @author wyq
     * 查看跟进记录
     */
    public void getRecord(BasePageRequest<CrmContacts> basePageRequest){
        renderJson(R.ok().put("data",crmContactsService.getRecord(basePageRequest)));
    }
}
