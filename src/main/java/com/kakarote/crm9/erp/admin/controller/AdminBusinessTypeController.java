package com.kakarote.crm9.erp.admin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.entity.CrmBusinessType;
import com.kakarote.crm9.erp.admin.service.AdminBusinessTypeService;
import com.kakarote.crm9.utils.R;
import com.kakarote.crm9.utils.TagUtil;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;

import java.util.HashSet;
import java.util.List;

/**
 * 商机组设置
 *
 * @author hmb
 */
public class AdminBusinessTypeController extends Controller {

    @Inject
    private AdminBusinessTypeService adminBusinessTypeService;

    /**
     * @author hmb
     * 设置商机组
     */
    @Permissions("manage:crm:setting")
    public void setBusinessType() {
        JSONObject jsonObject = JSON.parseObject(getRawData());
        CrmBusinessType crmBusinessType = jsonObject.getObject("crmBusinessType", CrmBusinessType.class);
        if(jsonObject.getJSONArray("deptIds") != null){
            List<Integer> deptIds = jsonObject.getJSONArray("deptIds").toJavaList(Integer.class);
            crmBusinessType.setDeptIds(TagUtil.fromSet(new HashSet<>(deptIds)));
        }
        JSONArray crmBusinessStatus = jsonObject.getJSONArray("crmBusinessStatus");
        renderJson(adminBusinessTypeService.addBusinessType(crmBusinessType,crmBusinessStatus));
    }

    /**
     * @author hmb
     * @param basePageRequest 分页对象
     * 查询商机组列表
     */
    @Permissions("manage:crm:setting")
    public void queryBusinessTypeList(BasePageRequest<Void> basePageRequest) {
        renderJson(R.ok().put("data", adminBusinessTypeService.queryBusinessTypeList(basePageRequest)));
    }

    /**
     * @author hmb
     * 获取详细信息
     */
    public void getBusinessType() {
        String typeId = getPara("id");
        renderJson(adminBusinessTypeService.getBusinessType(typeId));
    }

    /**
     * @author hmb
     * 删除商机状态组
     */
    @Permissions("manage:crm:setting")
    public void deleteById() {
        String typeId = getPara("id");
        renderJson(adminBusinessTypeService.deleteById(typeId));
    }


}
