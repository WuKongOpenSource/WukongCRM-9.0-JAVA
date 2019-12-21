package com.kakarote.crm9.erp.crm.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Aop;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.entity.AdminField;
import com.kakarote.crm9.erp.admin.entity.AdminFieldv;
import com.kakarote.crm9.erp.admin.entity.AdminFile;
import com.kakarote.crm9.erp.admin.entity.AdminRecord;
import com.kakarote.crm9.erp.admin.service.AdminFieldService;
import com.kakarote.crm9.erp.admin.service.AdminFileService;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.common.CrmParamValid;
import com.kakarote.crm9.erp.crm.entity.CrmActionRecord;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import com.kakarote.crm9.erp.crm.entity.CrmLeads;
import com.kakarote.crm9.erp.oa.entity.OaEvent;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.FieldUtil;
import com.kakarote.crm9.utils.R;

import java.util.*;
import java.util.stream.Collectors;

public class CrmLeadsService {
    @Inject
    private AdminFieldService adminFieldService;

    @Inject
    private FieldUtil fieldUtil;

    @Inject
    private CrmRecordService crmRecordService;

    @Inject
    private AdminFileService adminFileService;

    @Inject
    private CrmParamValid crmParamValid;

    /**
     * @author wyq
     * 分页条件查询线索
     */
    public Page<Record> getLeadsPageList(BasePageRequest<CrmLeads> basePageRequest) {
        String leadsName = basePageRequest.getData().getLeadsName();
        if (!crmParamValid.isValid(leadsName)){
            return new Page<>();
        }
        String telephone = basePageRequest.getData().getTelephone();
        String mobile = basePageRequest.getData().getMobile();
        if (StrUtil.isEmpty(leadsName) && StrUtil.isEmpty(telephone) && StrUtil.isEmpty(mobile)){
            return new Page<>();
        }
        return Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("crm.leads.getLeadsPageList",Kv.by("leadsName",leadsName).set("telephone",telephone).set("mobile",mobile)));
    }

    /**
     * @author wyq
     * 新增或更新线索
     */
    @Before(Tx.class)
    public R addOrUpdate(JSONObject object) {
        CrmLeads crmLeads = object.getObject("entity", CrmLeads.class);
        String batchId = StrUtil.isNotEmpty(crmLeads.getBatchId()) ? crmLeads.getBatchId() : IdUtil.simpleUUID();
        crmRecordService.updateRecord(object.getJSONArray("field"), batchId);
        adminFieldService.save(object.getJSONArray("field"), batchId);
        if (crmLeads.getLeadsId() != null) {
            crmLeads.setCustomerId(0);
            crmLeads.setUpdateTime(DateUtil.date());
            crmRecordService.updateRecord(new CrmLeads().dao().findById(crmLeads.getLeadsId()), crmLeads, CrmEnum.CRM_LEADS);
            return crmLeads.update() ? R.ok() : R.error();
        } else {
            crmLeads.setCreateTime(DateUtil.date());
            crmLeads.setUpdateTime(DateUtil.date());
            crmLeads.setCreateUserId(BaseUtil.getUser().getUserId());
            if (crmLeads.getOwnerUserId() == null) {
                crmLeads.setOwnerUserId(BaseUtil.getUser().getUserId());
            }
            crmLeads.setBatchId(batchId);
            boolean save = crmLeads.save();
            crmRecordService.addRecord(crmLeads.getLeadsId(), CrmEnum.CRM_LEADS);
            return save ? R.ok() : R.error();
        }
    }

    /**
     * @author wyq
     * 基本信息
     */
    public List<Record> information(Integer leadsId) {
        Record record = queryById(leadsId);
        List<String> keyList = Arrays.asList("leads_name", "mobile", "address","telephone","next_time","remark");
        List<Record> recordList = adminFieldService.queryInformation(CrmEnum.CRM_LEADS,record, keyList);
        return recordList.stream().sorted(Comparator.comparingInt(r->-r.getInt("field_type"))).map(r-> r.remove("field_type","field_name","setting","type")).collect(Collectors.toList());
    }

    /**
     * @author wyq
     * 根据线索id查询
     */
    public Record queryById(Integer leadsId) {
        Record record = Db.findFirst(Db.getSql("crm.leads.queryById"), leadsId);
        List<Record> recordList = Db.find("select name,value from `72crm_admin_fieldv` where batch_id = ?", record.getStr("batch_id"));
        recordList.forEach(field->record.set(field.getStr("name"),field.getStr("value")));
        return record;
    }

    /**
     * @author wyq
     * 根据线索名称查询
     */
    public Record queryByName(String name) {
        return Db.findFirst(Db.getSql("crm.leads.queryByName"), name);
    }

    /**
     * @author wyq
     * 根据id 删除线索
     */
    public R deleteByIds(String leadsIds) {
        String[] idsArr = leadsIds.split(",");
        List<Record> idsList = new ArrayList<>();
        for (String id : idsArr) {
            Record record = new Record();
            idsList.add(record.set("leads_id", Integer.valueOf(id)));
        }
        List<Record> batchIdList = Db.find(Db.getSqlPara("crm.leads.queryBatchIdByIds",Kv.by("ids",idsArr)));
        return Db.tx(() -> {
            Db.batch(Db.getSql("crm.leads.deleteByIds"), "leads_id", idsList, 100);
            Db.batch("delete from 72crm_admin_fieldv where batch_id = ?","batch_id",batchIdList,100);
            return true;
        }) ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 变更负责人
     */
    public R updateOwnerUserId(String leadsIds, Long ownerUserId) {
        String[] ids = leadsIds.split(",");
        int update = Db.update(Db.getSqlPara("crm.leads.updateOwnerUserId", Kv.by("ownerUserId", ownerUserId).set("ids", ids)));
        for (String id : ids) {
            crmRecordService.addConversionRecord(Integer.valueOf(id), CrmEnum.CRM_LEADS, ownerUserId);
        }
        return update > 0 ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 线索转客户
     */
    @Before(Tx.class)
    public R translate(String leadsIds) {
        String[] leadsIdsArr = leadsIds.split(",");
        for (String leadsId : leadsIdsArr) {
            Record crmLeads = queryById(Integer.valueOf(leadsId));
            if (1 == crmLeads.getInt("is_transform")) {
                return R.error("已转化线索不能再次转化");
            }
            List<Record> leadsFields = adminFieldService.list(CrmEnum.CRM_LEADS.getType());
            CrmCustomer crmCustomer = new CrmCustomer();
            crmCustomer.setCustomerName(crmLeads.getStr("leads_name"));
            crmCustomer.setIsLock(0);
            crmCustomer.setNextTime(crmLeads.getDate("next_time"));
            crmCustomer.setMobile(crmLeads.getStr("mobile"));
            crmCustomer.setTelephone(crmLeads.getStr("telephone"));
            crmCustomer.setDealStatus(0);
            crmCustomer.setCreateUserId(BaseUtil.getUser().getUserId());
            crmCustomer.setOwnerUserId(crmLeads.getLong("owner_user_id"));
            crmCustomer.setCreateTime(DateUtil.date());
            crmCustomer.setUpdateTime(DateUtil.date());
            crmCustomer.setRoUserId(",");
            crmCustomer.setRwUserId(",");
            crmCustomer.setDetailAddress(crmLeads.getStr("address"));
            crmCustomer.setLocation("");
            crmCustomer.setAddress("");
            crmCustomer.setLng("");
            crmCustomer.setLat("");
            crmCustomer.setRemark("");
            String customerBatchId = IdUtil.simpleUUID();
            crmCustomer.setBatchId(customerBatchId);
            List<AdminField> customerFields = AdminField.dao.find("select field_id,name,field_name,field_type,is_null,is_unique from 72crm_admin_field where label = '2'");
            List<AdminFieldv> adminFieldvList = new ArrayList<>();
            for (Record leadsField : leadsFields) {
                for (AdminField customerField : customerFields) {
                    Integer isNull = customerField.getIsNull();
                    Integer isUnique = customerField.getIsUnique();
                    if (leadsField.get("relevant") != null && customerField.getFieldId().equals(leadsField.get("relevant"))) {
                        if (customerField.getFieldType().equals(1)) {
                            crmCustomer.set(customerField.getFieldName(), crmLeads.get(leadsField.get("field_name")));
                        } else {
                            AdminFieldv adminFieldv = new AdminFieldv();
                            adminFieldv.setValue(crmLeads.get(leadsField.get("name")));
                            adminFieldv.setFieldId(customerField.getFieldId());
                            adminFieldv.setName(customerField.getName());
                            adminFieldvList.add(adminFieldv);
                        }
                        continue;
                    }
                    if(!customerField.getFieldType().equals(0)){
                        continue;
                    }
                    if ("客户来源".equals(customerField.getName()) && "线索来源".equals(leadsField.getStr("name"))) {
                        Integer integer = Db.template("crm.customer.queryFieldDuplicate1", Kv.by("key", customerField.getName()).set("value", crmLeads.get(leadsField.get("name")))).queryInt();
                        if (isUnique == 1 && Db.template("crm.customer.queryFieldDuplicate1",Kv.by("key",customerField.getName()).set("value",crmLeads.get(leadsField.get("name")))).queryInt()>0){
                            return R.error(customerField.getName()+"已存在");
                        }
                        AdminFieldv adminFieldv = new AdminFieldv();
                        adminFieldv.setValue(crmLeads.get(leadsField.get("name")));
                        adminFieldv.setFieldId(customerField.getFieldId());
                        adminFieldv.setName(customerField.getName());
                        adminFieldvList.add(adminFieldv);
                    }
                    if ("客户行业".equals(customerField.getName()) && "客户行业".equals(leadsField.getStr("name"))) {
                        if (isUnique == 1 && Db.template("crm.customer.queryFieldDuplicate1",Kv.by("key",customerField.getName()).set("value",crmLeads.get(leadsField.get("name")))).queryInt()>0){
                            return R.error(customerField.getName()+"已存在");
                        }
                        AdminFieldv adminFieldv = new AdminFieldv();
                        adminFieldv.setValue(crmLeads.get(leadsField.get("name")));
                        adminFieldv.setFieldId(customerField.getFieldId());
                        adminFieldv.setName(customerField.getName());
                        adminFieldvList.add(adminFieldv);
                    }
                    if ("客户级别".equals(customerField.getName()) && "客户级别".equals(leadsField.getStr("name"))) {
//                        if (isNull==1 && ObjectUtil.isEmpty(crmLeads.get(leadsField.get("name")))){
//                            return R.error(customerField.getName()+"不能为空");
//                        }
                        if (isUnique == 1 && Db.template("crm.customer.queryFieldDuplicate1",Kv.by("key",customerField.getName()).set("value",crmLeads.get(leadsField.get("name")))).queryInt()>0){
                            return R.error(customerField.getName()+"已存在");
                        }
                        AdminFieldv adminFieldv = new AdminFieldv();
                        adminFieldv.setValue(crmLeads.get(leadsField.get("name")));
                        adminFieldv.setFieldId(customerField.getFieldId());
                        adminFieldv.setName(customerField.getName());
                        adminFieldvList.add(adminFieldv);
                    }
                }
            }
            for (AdminField customerField : customerFields) {
                Integer isNull = customerField.getIsNull();
                Integer isUnique = customerField.getIsUnique();
                String name = customerField.getName();
                Map<String, Object> customerMap = crmCustomer.toRecord().getColumns();
                for (String key : customerMap.keySet()) {
                    if (key.equals(customerField.getFieldName())){
                        if (isUnique == 1 && Db.template("crm.customer.queryFieldDuplicate",Kv.by("key",key).set("value",customerMap.get(key))).queryInt()>0){
                            return R.error(name+"已存在");
                        }
                    }
                }
            }
            crmCustomer.save();
            boolean isMaxOwner = Aop.get(CrmCustomerService.class).isMaxOwner(crmLeads.getLong("owner_user_id"),new String[]{crmCustomer.getCustomerId().toString()});
            if (!isMaxOwner){
                return R.error("该员工拥有客户数已达上限");
            }
            crmRecordService.addConversionCustomerRecord(crmCustomer.getCustomerId(), CrmEnum.CRM_CUSTOMER.getType()+"", crmCustomer.getCustomerName());
            adminFieldService.save(adminFieldvList, customerBatchId);
            Db.update("update 72crm_crm_leads set is_transform = 1,update_time = ?,customer_id = ? where leads_id = ?",
                    DateUtil.date(), crmCustomer.getCustomerId(), Integer.valueOf(leadsId));
            List<AdminRecord> adminRecordList = AdminRecord.dao.find("select * from 72crm_admin_record where types = 'crm_leads' and types_id = ?",Integer.valueOf(leadsId));
            List<CrmActionRecord> crmActionRecordList = CrmActionRecord.dao.find("select * from `72crm_crm_action_record` where action_id = ? and types = 1", Integer.valueOf(leadsId));
            List<AdminFile> adminFileList = new ArrayList<>();
            crmActionRecordList.forEach(crmActionRecord -> {
                crmActionRecord.setId(null);
                crmActionRecord.setTypes(String.valueOf(CrmEnum.CRM_CUSTOMER.getType()));
                crmActionRecord.setActionId(crmCustomer.getCustomerId());
            });
            Db.batchSave(crmActionRecordList,500);
            if (adminRecordList.size() != 0){
                adminRecordList.forEach(adminRecord -> {
                    List<AdminFile> leadsRecordFiles = AdminFile.dao.find("select file_id, name, size, create_user_id, create_time, file_path, file_type from 72crm_admin_file where batch_id = ?",adminRecord.getBatchId());
                    String customerRecordBatchId = IdUtil.simpleUUID();
                    leadsRecordFiles.forEach(adminFile -> {
                        adminFile.setBatchId(customerRecordBatchId);
                        adminFile.setFileId(null);
                    });
                    adminFileList.addAll(leadsRecordFiles);
                    adminRecord.setBatchId(customerRecordBatchId);
                    adminRecord.setRecordId(null);
                    adminRecord.setTypes("crm_customer");
                    adminRecord.setTypesId(crmCustomer.getCustomerId());
                    adminRecord.setUpdateTime(DateUtil.date());
                });
                Db.batchSave(adminRecordList,100);
            }
            List<AdminFile> fileList = AdminFile.dao.find("select file_id, name, size, create_user_id, create_time, file_path, file_type from 72crm_admin_file where batch_id = ?",crmLeads.getStr("batch_id"));
            if (fileList.size() != 0){
                fileList.forEach(adminFile -> {
                    adminFile.setBatchId(customerBatchId);
                    adminFile.setFileId(null);
                });
            }
            adminFileList.addAll(fileList);
            Db.batchSave(adminFileList,100);
        }
        return R.ok();
    }

    /**
     * @author wyq
     * 查询编辑字段
     */
    public List<Record> queryField(Integer leadsId) {
        Record leads = queryById(leadsId);
        return adminFieldService.queryUpdateField(CrmEnum.CRM_LEADS.getType(),leads);
    }

    /**
     * @author wyq
     * 添加跟进记录
     */
    @Before(Tx.class)
    public R addRecord(AdminRecord adminRecord) {
        adminRecord.setCreateUserId(BaseUtil.getUser().getUserId());
        adminRecord.setCreateTime(DateUtil.date());
        adminRecord.setTypes("crm_leads");
        if (StrUtil.isEmpty(adminRecord.getCategory())){
            return R.error("跟进类型不能为空");
        }
        if (1 == adminRecord.getIsEvent()) {
            OaEvent oaEvent = new OaEvent();
            oaEvent.setTitle(adminRecord.getContent());
            oaEvent.setCreateUserId(adminRecord.getCreateUserId());
            oaEvent.setStartTime(adminRecord.getNextTime());
            oaEvent.setEndTime(DateUtil.offsetDay(adminRecord.getNextTime(), 1));
            oaEvent.setCreateTime(DateUtil.date());
            oaEvent.save();
        }
        CrmLeads crmLeads = new CrmLeads();
        crmLeads.setLastContent(adminRecord.getContent());
        crmLeads.setLeadsId(adminRecord.getTypesId());
        crmLeads.setFollowup(1);
        crmLeads.setNextTime(adminRecord.getNextTime());
        crmLeads.update();
        return adminRecord.save() ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 查看跟进记录
     */
    public List<Record> getRecord(BasePageRequest<CrmLeads> basePageRequest) {
        CrmLeads crmLeads = basePageRequest.getData();
        List<Record> recordList = Db.find(Db.getSql("crm.leads.getRecord"), crmLeads.getLeadsId());
        recordList.forEach(record -> {
            adminFileService.queryByBatchId(record.getStr("batch_id"), record);
        });
        return recordList;
    }

    /**
     * @author wyq
     * 获取线索导入查重字段
     */
    public R getCheckingField(){
        return R.ok().put("data","线索名称");
    }
}
