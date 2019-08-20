package com.kakarote.crm9.erp.crm.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.log.Log;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.entity.AdminField;
import com.kakarote.crm9.erp.admin.entity.AdminFieldv;
import com.kakarote.crm9.erp.admin.entity.AdminFile;
import com.kakarote.crm9.erp.admin.entity.AdminRecord;
import com.kakarote.crm9.erp.admin.service.AdminFieldService;
import com.kakarote.crm9.erp.admin.service.AdminFileService;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.common.CrmParamValid;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import com.kakarote.crm9.erp.crm.entity.CrmLeads;
import com.kakarote.crm9.erp.oa.entity.OaEvent;
import com.kakarote.crm9.utils.AuthUtil;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.FieldUtil;
import com.kakarote.crm9.utils.R;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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

    @Inject
    private AuthUtil authUtil;

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
            crmRecordService.updateRecord(new CrmLeads().dao().findById(crmLeads.getLeadsId()), crmLeads, CrmEnum.LEADS_TYPE_KEY.getTypes());
            return crmLeads.update() ? R.ok() : R.error();
        } else {
            crmLeads.setCreateTime(DateUtil.date());
            crmLeads.setUpdateTime(DateUtil.date());
            crmLeads.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
            if (crmLeads.getOwnerUserId() == null) {
                crmLeads.setOwnerUserId(BaseUtil.getUser().getUserId().intValue());
            }
            crmLeads.setBatchId(batchId);
            boolean save = crmLeads.save();
            crmRecordService.addRecord(crmLeads.getLeadsId(), CrmEnum.LEADS_TYPE_KEY.getTypes());
            return save ? R.ok() : R.error();
        }
    }

    /**
     * @author wyq
     * 基本信息
     */
    public List<Record> information(Integer leadsId) {
        CrmLeads crmLeads = CrmLeads.dao.findById(leadsId);
        List<Record> fieldList = new ArrayList<>();
        FieldUtil field = new FieldUtil(fieldList);
        field.set("线索名称", crmLeads.getLeadsName()).set("电话", crmLeads.getMobile())
                .set("手机", crmLeads.getTelephone()).set("下次联系时间", DateUtil.formatDateTime(crmLeads.getNextTime()))
                .set("地址", crmLeads.getAddress()).set("备注", crmLeads.getRemark());
        List<Record> recordList = Db.find(Db.getSql("admin.field.queryCustomField"),crmLeads.getBatchId());
        fieldUtil.handleType(recordList);
        fieldList.addAll(recordList);
        return fieldList;
    }

    /**
     * @author wyq
     * 根据线索id查询
     */
    public Record queryById(Integer leadsId) {
        return Db.findFirst(Db.getSql("crm.leads.queryById"), leadsId);
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
    public R updateOwnerUserId(String leadsIds, Integer ownerUserId) {
        String[] ids = leadsIds.split(",");
        int update = Db.update(Db.getSqlPara("crm.leads.updateOwnerUserId", Kv.by("ownerUserId", ownerUserId).set("ids", ids)));
        for (String id : ids) {
            crmRecordService.addConversionRecord(Integer.valueOf(id), CrmEnum.LEADS_TYPE_KEY.getTypes(), ownerUserId);
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
            Record crmLeads = Db.findFirst("select * from leadsview where leads_id = ?", Integer.valueOf(leadsId));
            if (1 == crmLeads.getInt("is_transform")) {
                return R.error("已转化线索不能再次转化");
            }
            List<Record> leadsFields = adminFieldService.list("1");
            CrmCustomer crmCustomer = new CrmCustomer();
            crmCustomer.setCustomerName(crmLeads.getStr("leads_name"));
            crmCustomer.setIsLock(0);
            crmCustomer.setNextTime(crmLeads.getDate("next_time"));
            crmCustomer.setMobile(crmLeads.getStr("mobile"));
            crmCustomer.setTelephone(crmLeads.getStr("telephone"));
            crmCustomer.setDealStatus("未成交");
            crmCustomer.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
            crmCustomer.setOwnerUserId(crmLeads.getInt("owner_user_id"));
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
            List<AdminField> customerFields = AdminField.dao.find("select field_id,name,field_name,field_type from 72crm_admin_field where label = '2'");
            List<AdminFieldv> adminFieldvList = new ArrayList<>();
            for (Record leadsFIeld : leadsFields) {
                for (AdminField customerField : customerFields) {
                    if(leadsFIeld.get("relevant")!=null&&customerField.getFieldId().equals(leadsFIeld.get("relevant"))){
                        if(customerField.getFieldType().equals(1)){
                            crmCustomer.set(customerField.getFieldName(),crmLeads.get(leadsFIeld.get("field_name")));
                        }else {
                            AdminFieldv adminFieldv = new AdminFieldv();
                            adminFieldv.setValue(crmLeads.get(leadsFIeld.get("name")));
                            adminFieldv.setFieldId(customerField.getFieldId());
                            adminFieldv.setName(customerField.getName());
                            adminFieldvList.add(adminFieldv);
                        }
                        continue;
                    }
                    if(!customerField.getFieldType().equals(0)){
                        continue;
                    }
                    if ("客户来源".equals(customerField.getName()) && "线索来源".equals(leadsFIeld.getStr("name"))){
                        AdminFieldv adminFieldv = new AdminFieldv();
                        adminFieldv.setValue(crmLeads.get(leadsFIeld.get("name")));
                        adminFieldv.setFieldId(customerField.getFieldId());
                        adminFieldv.setName(customerField.getName());
                        adminFieldvList.add(adminFieldv);
                    }
                    if ("客户行业".equals(customerField.getName()) && "客户行业".equals(leadsFIeld.getStr("name"))){
                        AdminFieldv adminFieldv = new AdminFieldv();
                        adminFieldv.setValue(crmLeads.get(leadsFIeld.get("name")));
                        adminFieldv.setFieldId(customerField.getFieldId());
                        adminFieldv.setName(customerField.getName());
                        adminFieldvList.add(adminFieldv);
                    }
                    if ("客户级别".equals(customerField.getName()) && "客户级别".equals(leadsFIeld.getStr("name"))){
                        AdminFieldv adminFieldv = new AdminFieldv();
                        adminFieldv.setValue(crmLeads.get(leadsFIeld.get("name")));
                        adminFieldv.setFieldId(customerField.getFieldId());
                        adminFieldv.setName(customerField.getName());
                        adminFieldvList.add(adminFieldv);
                    }
                };
            };
            crmCustomer.save();
            crmRecordService.addConversionCustomerRecord(crmCustomer.getCustomerId(), CrmEnum.CUSTOMER_TYPE_KEY.getTypes(), crmCustomer.getCustomerName());
            adminFieldService.save(adminFieldvList, customerBatchId);
            Db.update("update 72crm_crm_leads set is_transform = 1,update_time = ?,customer_id = ? where leads_id = ?",
                    DateUtil.date(), crmCustomer.getCustomerId(), Integer.valueOf(leadsId));
            List<AdminRecord> adminRecordList = AdminRecord.dao.find("select * from 72crm_admin_record where types = 'crm_leads' and types_id = ?",Integer.valueOf(leadsId));
            List<AdminFile> adminFileList = new ArrayList<>();
            if (adminRecordList.size() != 0){
                adminRecordList.forEach(adminRecord -> {
                    List<AdminFile> leadsRecordFiles = AdminFile.dao.find("select * from 72crm_admin_file where batch_id = ?",adminRecord.getBatchId());
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
            List<AdminFile> fileList = AdminFile.dao.find("select * from 72crm_admin_file where batch_id = ?",crmLeads.getStr("batch_id"));
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
        Record leads = Db.findFirst("select * from leadsview where leads_id = ?",leadsId);
        return adminFieldService.queryUpdateField(1,leads);
    }

    /**
     * @author wyq
     * 添加跟进记录
     */
    @Before(Tx.class)
    public R addRecord(AdminRecord adminRecord) {
        adminRecord.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
        adminRecord.setCreateTime(DateUtil.date());
        adminRecord.setTypes("crm_leads");
        if (1 == adminRecord.getIsEvent()) {
            OaEvent oaEvent = new OaEvent();
            oaEvent.setTitle(adminRecord.getContent());
            oaEvent.setCreateUserId(adminRecord.getCreateUserId());
            oaEvent.setStartTime(adminRecord.getNextTime());
            oaEvent.setEndTime(DateUtil.offsetDay(adminRecord.getNextTime(), 1));
            oaEvent.setCreateTime(DateUtil.date());
            oaEvent.save();
        }
        Db.update("update 72crm_crm_leads set followup = 1 where leads_id = ?",adminRecord.getTypesId());
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
     * 线索导出
     */
    public List<Record> exportLeads(String leadsIds) {
        String[] leadsIdsArr = leadsIds.split(",");
        return Db.find(Db.getSqlPara("crm.leads.excelExport", Kv.by("ids", leadsIdsArr)));
    }

    /**
     * @author wyq
     * 获取线索导入查重字段
     */
    public R getCheckingField(){
        return R.ok().put("data","线索名称");
    }

    /**
     * @author wyq
     * 导入线索
     */
    public R uploadExcel(UploadFile file, Integer repeatHandling, Integer ownerUserId) {
        ExcelReader reader = ExcelUtil.getReader(FileUtil.file(file.getUploadPath() + "\\" + file.getFileName()));
        AdminFieldService adminFieldService = new AdminFieldService();
        Kv kv = new Kv();
        Integer errNum = 0;
        try {
            List<List<Object>> read = reader.read();
            List<Object> list = read.get(1);
            List<Record> recordList = adminFieldService.customFieldList("1");
            recordList.removeIf(record -> "file".equals(record.getStr("formType")) || "checkbox".equals(record.getStr("formType"))|| "user".equals(record.getStr("formType"))|| "structure".equals(record.getStr("formType")));
            List<Record> fieldList = adminFieldService.queryAddField(1);
            fieldList.removeIf(record -> "file".equals(record.getStr("formType")) || "checkbox".equals(record.getStr("formType"))|| "user".equals(record.getStr("formType"))|| "structure".equals(record.getStr("formType")));
            fieldList.forEach(record -> {
                if (record.getInt("is_null") == 1){
                    record.set("name",record.getStr("name")+"(*)");
                }
            });
            List<String> nameList = fieldList.stream().map(record -> record.getStr("name")).collect(Collectors.toList());
            if (nameList.size() != list.size() || !nameList.containsAll(list)){
                return R.error("请使用最新导入模板");
            }
            Kv nameMap = new Kv();
            fieldList.forEach(record -> nameMap.set(record.getStr("name"),record.getStr("field_name")));
            for (int i = 0; i < list.size(); i++) {
                kv.set(nameMap.get(list.get(i)), i);
            }
            if (read.size() > 2) {
                JSONObject object = new JSONObject();
                for (int i = 2; i < read.size(); i++) {
                    errNum = i;
                    List<Object> leadsList = read.get(i);
                    if (leadsList.size() < list.size()) {
                        for (int j = leadsList.size() - 1; j < list.size(); j++) {
                            leadsList.add(null);
                        }
                    }
                    String leadsName = leadsList.get(kv.getInt("leads_name")).toString();
                    Integer number = Db.queryInt("select count(*) from 72crm_crm_leads where leads_name = ?", leadsName);
                    if (0 == number) {
                        object.fluentPut("entity", new JSONObject().fluentPut("leads_name", leadsName)
                                .fluentPut("telephone", leadsList.get(kv.getInt("telephone")))
                                .fluentPut("mobile", leadsList.get(kv.getInt("mobile")))
                                .fluentPut("address", leadsList.get(kv.getInt("address")))
                                .fluentPut("next_time", leadsList.get(kv.getInt("next_time")))
                                .fluentPut("remark", leadsList.get(kv.getInt("remark")))
                                .fluentPut("owner_user_id", ownerUserId));
                    } else if (number > 0 && repeatHandling == 1) {
                        Record leads = Db.findFirst("select leads_id,batch_id from 72crm_crm_leads where leads_name = ?", leadsName);
                        object.fluentPut("entity", new JSONObject().fluentPut("leads_id", leads.getInt("leads_id"))
                                .fluentPut("leads_name", leadsName)
                                .fluentPut("telephone", leadsList.get(kv.getInt("telephone")))
                                .fluentPut("mobile", leadsList.get(kv.getInt("mobile")))
                                .fluentPut("address", leadsList.get(kv.getInt("address")))
                                .fluentPut("next_time", leadsList.get(kv.getInt("next_time")))
                                .fluentPut("remark", leadsList.get(kv.getInt("remark")))
                                .fluentPut("owner_user_id", ownerUserId)
                                .fluentPut("batch_id", leads.getStr("batch_id")));
                    } else if (number > 0 && repeatHandling == 2) {
                        continue;
                    }
                    JSONArray jsonArray = new JSONArray();
                    for (Record record : recordList) {
                        Integer columnsNum = kv.getInt(record.getStr("name"))!=null?kv.getInt(record.getStr("name")):kv.getInt(record.getStr("name")+"(*)");
                        record.set("value", leadsList.get(columnsNum));
                        jsonArray.add(JSONObject.parseObject(record.toJson()));
                    }
                    object.fluentPut("field", jsonArray);
                    addOrUpdate(object);
                }
            }
        } catch (Exception e) {
            Log.getLog(getClass()).error("",e);
            if (errNum != 0){
                return R.error("第" + (errNum+1) + "行错误!");
            }
            return R.error();
        } finally {
            reader.close();
        }
        return R.ok();
    }
}
