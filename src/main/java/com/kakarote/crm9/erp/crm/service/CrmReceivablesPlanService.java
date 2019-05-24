package com.kakarote.crm9.erp.crm.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.service.AdminFieldService;
import com.kakarote.crm9.erp.crm.entity.CrmReceivables;
import com.kakarote.crm9.erp.crm.entity.CrmReceivablesPlan;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.FieldUtil;
import com.kakarote.crm9.utils.R;
import com.jfinal.aop.Inject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import java.util.ArrayList;
import java.util.List;

public class CrmReceivablesPlanService {
    @Inject
    private FieldUtil fieldUtil;
    @Inject
    private AdminFieldService adminFieldService;

    /**
     * 添加或修改回款计划
     */
    public R saveAndUpdate(JSONObject jsonObject) {
        CrmReceivablesPlan crmReceivablesPlan = jsonObject.getObject("entity", CrmReceivablesPlan.class);
        String batchId = StrUtil.isNotEmpty(crmReceivablesPlan.getFileBatch()) ? crmReceivablesPlan.getFileBatch() : IdUtil.simpleUUID();
        adminFieldService.save(jsonObject.getJSONArray("field"), batchId);
        if (null == crmReceivablesPlan.getPlanId()) {
            crmReceivablesPlan.setCreateTime(DateUtil.date());
            crmReceivablesPlan.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
            crmReceivablesPlan.setFileBatch(batchId);
            CrmReceivablesPlan receivablesPlan = CrmReceivablesPlan.dao.findFirst(Db.getSql("crm.receivablesplan.queryByContractId"), crmReceivablesPlan.getContractId());
            if (receivablesPlan == null) {
                crmReceivablesPlan.setNum("1");
            } else {
                crmReceivablesPlan.setNum(Integer.valueOf(receivablesPlan.getNum()) + 1 + "");
            }
            return crmReceivablesPlan.save() ? R.ok() : R.error();
        } else {
            crmReceivablesPlan.setUpdateTime(DateUtil.date());
            return crmReceivablesPlan.update() ? R.ok() : R.error();
        }
    }

    /**
     * @author wyq
     * 删除回款计划
     */
    public R deleteByIds(String planIds){
        String[] idsArr = planIds.split(",");
        List<Record> idsList = new ArrayList<>();
        for (String id : idsArr) {
            Record record = new Record();
            idsList.add(record.set("plan_id", Integer.valueOf(id)));
        }
        return Db.tx(() -> {
            Db.batch(Db.getSql("crm.receivablesplan.deleteByIds"), "plan_id", idsList, 100);
            return true;
        }) ? R.ok() : R.error();
    }

    /**
     * @author zxy
     * 查询回款自定义字段（新增）
     */
    public List<Record> queryField() {
        List<Record> fieldList = new ArrayList<>();
        String[] settingArr = new String[]{};
        fieldUtil.getFixedField(fieldList, "customerId", "客户名称", "", "customer", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "contractId", "合同编号", "", "contract", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "money", "计划回款金额", "", "number", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "returnDate", "计划回款日期", "", "date", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "remind", "提前几天提醒", "", "number", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "remark", "备注", "", "textarea", settingArr, 0);
        fieldList.addAll(adminFieldService.list("8"));
        return fieldList;
    }

    /**
     * 根据合同查询回款计划
     */
    public R qureyListByContractId(BasePageRequest<CrmReceivables> basePageRequest) {

        Integer pageType = basePageRequest.getPageType();
        if (pageType == null || 0 == pageType) {
            return R.ok().put("data", Db.find(Db.getSql("crm.receivablesplan.queryListByContractId"), basePageRequest.getData().getContractId()));
        }
        if (1 == pageType) {
            return R.ok().put("data", Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("crm.receivablesplan.queryListByContractId", basePageRequest.getData().getContractId())));
        }
        return R.error();
    }

    /**
     * 根据合同id和客户id查询未使用的回款计划
     */
    public R queryByContractAndCustomer(CrmReceivablesPlan receivablesPlan) {
        List<CrmReceivablesPlan> plans = CrmReceivablesPlan.dao.find(Db.getSql("crm.receivablesplan.queryByCustomerIdContractId"), receivablesPlan.getContractId(), receivablesPlan.getCustomerId());
        return R.ok().put("data", plans);
    }
}
