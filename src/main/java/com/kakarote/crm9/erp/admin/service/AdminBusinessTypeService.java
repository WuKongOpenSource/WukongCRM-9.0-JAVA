package com.kakarote.crm9.erp.admin.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.util.TypeUtils;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.entity.CrmBusinessStatus;
import com.kakarote.crm9.erp.admin.entity.CrmBusinessType;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.jfinal.aop.Before;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdminBusinessTypeService {

    @Before(Tx.class)
    public void addBusinessType(CrmBusinessType crmBusinessType, JSONArray crmBusinessStatusList){
        if (crmBusinessType.getTypeId() == null) {
            crmBusinessType.setCreateTime(new Date());
            crmBusinessType.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
            crmBusinessType.save();
        } else {
            crmBusinessType.setUpdateTime(new Date());
            crmBusinessType.update();
            Db.delete(Db.getSql("admin.businessType.deleteBusinessStatus"),crmBusinessType.getTypeId());
        }
        Integer typeId = crmBusinessType.getTypeId();
        for(int i = 0; i < crmBusinessStatusList.size(); i++){
            CrmBusinessStatus crmBusinessStatus = TypeUtils.castToJavaBean(crmBusinessStatusList.getJSONObject(i), CrmBusinessStatus.class);
            crmBusinessStatus.setStatusId(null);
            crmBusinessStatus.setTypeId(typeId);
            crmBusinessStatus.setOrderNum(i+1);
            crmBusinessStatus.save();
        }
    }

    public Page<Record> queryBusinessTypeList(BasePageRequest request) {
        Page<Record> paginate = Db.paginate(request.getPage(), request.getLimit(), Db.getSqlPara("admin.businessType.queryBusinessTypeList"));
        paginate.getList().forEach(record -> {
            System.out.println(record.getStr("dept_ids"));
            if(record.getStr("dept_ids") != null && record.getStr("dept_ids").split(",").length > 0){
                List<Record> deptList = Db.find(Db.getSqlPara("admin.dept.queryByIds", Kv.by("ids", record.getStr("dept_ids").split(","))));
                record.set("deptIds", deptList);
            }else{
                record.set("deptIds", new ArrayList<>());
            }
        });
        return paginate;
    }

    public R getBusinessType(String typeId) {
        Record record = Db.findFirst(Db.getSql("admin.businessType.getBusinessType"), typeId);
        if(record.getStr("dept_ids") != null && record.getStr("dept_ids").split(",").length > 0){
            List<Record> deptList = Db.find(Db.getSqlPara("admin.dept.queryByIds", Kv.by("ids", record.getStr("dept_ids").split(","))));
            record.set("deptIds", deptList);
        }else{
            record.set("deptIds", new ArrayList<>());
        }
        List<Record> statusList = Db.find(Db.getSql("admin.businessType.queryBusinessStatus"), typeId);
        record.set("statusList", statusList);
        return R.ok().put("data", record);
    }

    @Before(Tx.class)
    public R deleteById(String typeId) {
        Integer count = Db.queryInt("select count(*) from 72crm_crm_business where type_id = ?", typeId);
        if (count > 0) {
            return R.error("使用中的商机组不可以删除");
        }
        Db.deleteById("72crm_crm_business_type", "type_id", typeId);
        Db.deleteById("72crm_crm_business_status", "type_id", typeId);
        return R.ok();
    }

}
