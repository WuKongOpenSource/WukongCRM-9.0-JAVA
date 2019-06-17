package com.kakarote.crm9.erp.crm.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.upload.UploadFile;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.admin.entity.AdminRecord;
import com.kakarote.crm9.erp.admin.service.AdminFieldService;
import com.kakarote.crm9.erp.admin.service.AdminFileService;
import com.kakarote.crm9.erp.crm.entity.CrmBusiness;
import com.kakarote.crm9.erp.crm.entity.CrmContacts;
import com.kakarote.crm9.erp.crm.entity.CrmContactsBusiness;
import com.kakarote.crm9.erp.oa.common.OaEnum;
import com.kakarote.crm9.erp.oa.entity.OaEvent;
import com.kakarote.crm9.erp.oa.entity.OaEventRelation;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.oa.service.OaActionRecordService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.FieldUtil;
import com.kakarote.crm9.utils.R;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jfinal.plugin.activerecord.tx.Tx;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class CrmContactsService {
    @Inject
    private AdminFieldService adminFieldService;

    @Inject
    private FieldUtil fieldUtil;

    @Inject
    private CrmRecordService crmRecordService;

    @Inject
    private AdminFileService adminFileService;

    @Inject
    private OaActionRecordService oaActionRecordService;

    /**
     * @author wyq
     * 分页条件查询联系人
     */
    public Page<Record> queryList(BasePageRequest<CrmContacts> basePageRequest){
        String contactsName = basePageRequest.getData().getName();
        String telephone = basePageRequest.getData().getTelephone();
        String mobile = basePageRequest.getData().getMobile();
        if (StrUtil.isEmpty(contactsName) && StrUtil.isEmpty(telephone) && StrUtil.isEmpty(mobile)){
            return new Page<>();
        }
        return Db.paginate(basePageRequest.getPage(),basePageRequest.getLimit(),Db.getSqlPara("crm.contact.getContactsPageList",Kv.by("contactsName",contactsName).set("telephone",telephone).set("mobile",mobile)));
    }

    /**
     * @author wyq
     * 根据id查询联系人
     */
    public Record queryById(Integer contactsId){
        return Db.findFirst(Db.getSql("crm.contact.queryById"),contactsId);
    }

    /**
     * @author wyq
     * 基本信息
     */
    public List<Record> information(Integer contactsId){
        Record record = Db.findFirst("select * from contactsview where contacts_id = ?",contactsId);
        if (null == record){
            return null;
        }
        List<Record> fieldList = new ArrayList<>();
        FieldUtil field = new FieldUtil(fieldList);
        field.set("姓名",record.getStr("name")).set("客户名称",record.getStr("customer_name"))
                .set("下次联系时间",DateUtil.formatDateTime(record.get("next_time"))).set("职务",record.getStr("post"))
                .set("手机",record.getStr("mobile")).set("电话",record.getStr("telephone")).set("邮箱",record.getStr("email"))
                .set("地址",record.getStr("address")).set("备注",record.getStr("remark"));
        List<Record> fields = adminFieldService.list("3");
        for (Record r:fields){
            field.set(r.getStr("name"),record.getStr(r.getStr("name")));
        }
        return fieldList;
    }

    /**
     * @author wyq
     * 根据联系人名称查询
     */
    public Record queryByName(String name){
        return Db.findFirst(Db.getSql("crm.contact.queryByName"),name);
    }

    /**
     * @author wyq
     * 根据联系人id查询商机
     */
    public R queryBusiness(BasePageRequest<CrmContacts> basePageRequest){
        Integer contactsId = basePageRequest.getData().getContactsId();
        Integer pageType = basePageRequest.getPageType();
        if (0 == pageType){
            return R.ok().put("data",Db.find(Db.getSql("crm.contact.queryBusiness"),contactsId));
        }else {
            return R.ok().put("data",Db.paginate(basePageRequest.getPage(),basePageRequest.getLimit(),new SqlPara().setSql(Db.getSql("crm.contact.queryBusiness")).addPara(contactsId)));
        }
    }

    /**
     * @author wyq
     * 联系人关联商机
     */
    public R relateBusiness(CrmContactsBusiness crmContactsBusiness){
        return crmContactsBusiness.save() ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 联系人解除关联商机
     */
    public R unrelateBusiness(Integer id){
        return CrmContactsBusiness.dao.deleteById(id) ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 新建或更新联系人
     */
    @Before(Tx.class)
    public R addOrUpdate(JSONObject jsonObject){
        CrmContacts crmContacts = jsonObject.getObject("entity",CrmContacts.class);
        String batchId = StrUtil.isNotEmpty(crmContacts.getBatchId()) ? crmContacts.getBatchId() : IdUtil.simpleUUID();
        crmRecordService.updateRecord(jsonObject.getJSONArray("field"),batchId);
        adminFieldService.save(jsonObject.getJSONArray("field"),batchId);
        if (crmContacts.getContactsId() != null) {
            crmContacts.setUpdateTime(DateUtil.date());
            crmRecordService.updateRecord(new CrmContacts().dao().findById(crmContacts.getContactsId()),crmContacts, CrmEnum.CONTACTS_TYPE_KEY.getTypes());
            return crmContacts.update() ? R.ok() : R.error();
        }else {
            crmContacts.setCreateTime(DateUtil.date());
            crmContacts.setUpdateTime(DateUtil.date());
            crmContacts.setCreateUserId(BaseUtil.getUserId().intValue());
            if (crmContacts.getOwnerUserId() == null){
                crmContacts.setOwnerUserId(BaseUtil.getUserId().intValue());
            }
            crmContacts.setBatchId(batchId);
            boolean save = crmContacts.save();
            crmRecordService.addRecord(crmContacts.getContactsId(),CrmEnum.CONTACTS_TYPE_KEY.getTypes());
            return  save? R.ok() : R.error();
        }
    }

    /**
     * @author wyq
     * 根据id删除联系人
     */
    public R deleteByIds(String contactsIds){
        String[] idsArr = contactsIds.split(",");
        List<Record> idsList = new ArrayList<>();
        for (String id : idsArr){
            Record record =new Record();
            idsList.add(record.set("contacts_id",Integer.valueOf(id)));
        }
        return Db.tx(() ->{
            Db.batch(Db.getSql("crm.contact.deleteByIds"),"contacts_id",idsList,100);
            return true;
        }) ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 联系人转移
     */
    public R transfer(CrmContacts crmContacts){
        String[] contactsIdsArr = crmContacts.getContactsIds().split(",");
        int update = Db.update(Db.getSqlPara("crm.contact.transfer", Kv.by("ownerUserId", crmContacts.getNewOwnerUserId()).set("ids", contactsIdsArr)));
        for(String contactsId : contactsIdsArr){
            crmRecordService.addConversionRecord(Integer.valueOf(contactsId),CrmEnum.CONTACTS_TYPE_KEY.getTypes(),crmContacts.getNewOwnerUserId());
        }
        return update > 0 ? R.ok() : R.error();
    }

    /**
     * 根据客户id变更负责人
     * @param customerId 客户ID
     * @param ownerUserId 负责人ID
     * @author zzw
     */
    public boolean updateOwnerUserId(Integer customerId,Integer ownerUserId){
        String sql = "update 72crm_crm_contacts set owner_user_id = " + ownerUserId + " where customer_id = "+customerId;
        int update = Db.update(sql);
        crmRecordService.addConversionRecord(customerId,CrmEnum.CUSTOMER_TYPE_KEY.getTypes(),ownerUserId);
        return  update> 0;
    }

    /**
     * @author wyq
     * 查询新增字段
     */
    public List<Record> queryField(){
        List<Record> fieldList = new LinkedList<>();
        String[] settingArr = new String[]{};
        fieldUtil.getFixedField(fieldList,"name","姓名","","text",settingArr,1);
        fieldUtil.getFixedField(fieldList,"customerId","客户名称","","customer",settingArr,1);
        fieldUtil.getFixedField(fieldList,"mobile","手机","","mobile",settingArr,0);
        fieldUtil.getFixedField(fieldList,"telephone","电话","","text",settingArr,0);
        fieldUtil.getFixedField(fieldList,"email","电子邮箱","","email",settingArr,0);
        fieldUtil.getFixedField(fieldList,"post","职务","","text",settingArr,0);
        fieldUtil.getFixedField(fieldList,"address","地址","","text",settingArr,0);
        fieldUtil.getFixedField(fieldList,"nextTime","下次联系时间","","datetime",settingArr,0);
        fieldUtil.getFixedField(fieldList,"remark","备注","","text",settingArr,0);
        fieldList.addAll(adminFieldService.list("3"));
        return fieldList;
    }

    /**
     * @author wyq
     * 查询编辑字段
     */
    public List<Record> queryField(Integer contactsId){
        List<Record> fieldList = new LinkedList<>();
        Record record = Db.findFirst("select * from contactsview where contacts_id = ?",contactsId);
        String[] settingArr = new String[]{};
        fieldUtil.getFixedField(fieldList,"name","姓名",record.getStr("name"),"text",settingArr,1);
        List<Record> customerList = new ArrayList<>();
        Record customer = new Record();
        customerList.add(customer.set("customer_id",record.getInt("customer_id")).set("customer_name",record.getStr("customer_name")));
        fieldUtil.getFixedField(fieldList,"customerId","客户名称",customerList,"customer",settingArr,1);
        fieldUtil.getFixedField(fieldList,"mobile","手机",record.getStr("mobile"),"mobile",settingArr,0);
        fieldUtil.getFixedField(fieldList,"telephone","电话",record.getStr("telephone"),"text",settingArr,0);
        fieldUtil.getFixedField(fieldList,"email","电子邮箱",record.getStr("email"),"email",settingArr,0);
        fieldUtil.getFixedField(fieldList,"post","职务",record.getStr("post"),"text",settingArr,0);
        fieldUtil.getFixedField(fieldList,"address","地址",record.getStr("address"),"text",settingArr,0);
        fieldUtil.getFixedField(fieldList,"nextTime","下次联系时间",DateUtil.formatDateTime(record.get("next_time")),"datetime",settingArr,0);
        fieldUtil.getFixedField(fieldList,"remark","备注",record.getStr("remark"),"text",settingArr,0);
        fieldList.addAll(adminFieldService.queryByBatchId(record.getStr("batch_id")));
        return fieldList;
    }

    /**
     * @author wyq
     * 添加跟进记录
     */
    @Before(Tx.class)
    public R addRecord(AdminRecord adminRecord){
        adminRecord.setTypes("crm_contacts");
        adminRecord.setCreateTime(DateUtil.date());
        adminRecord.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
        if (1 == adminRecord.getIsEvent()){
            OaEvent oaEvent = new OaEvent();
            oaEvent.setTitle(adminRecord.getContent());
            oaEvent.setStartTime(adminRecord.getNextTime());
            oaEvent.setEndTime(DateUtil.offsetDay(adminRecord.getNextTime(),1));
            oaEvent.setCreateTime(DateUtil.date());
            oaEvent.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
            oaEvent.save();

            AdminUser user = BaseUtil.getUser();
            oaActionRecordService.addRecord(oaEvent.getEventId(), OaEnum.EVENT_TYPE_KEY.getTypes(),1,oaActionRecordService.getJoinIds(user.getUserId().intValue(),oaEvent.getOwnerUserIds()),oaActionRecordService.getJoinIds(user.getDeptId(),""));
            OaEventRelation oaEventRelation = new OaEventRelation();
            oaEventRelation.setEventId(oaEvent.getEventId());
            oaEventRelation.setContactsIds(","+adminRecord.getTypesId().toString()+",");
            oaEventRelation.setCreateTime(DateUtil.date());
            oaEventRelation.save();
        }
        return R.isSuccess(adminRecord.save());
    }

    /**
     * @author wyq
     * 查看跟进记录
     */

    public List<Record> getRecord(BasePageRequest<CrmContacts> basePageRequest){
        CrmContacts crmContacts = basePageRequest.getData();
        List<Record> recordList = Db.find(Db.getSql("crm.contact.getRecord"),crmContacts.getContactsId(),crmContacts.getContactsId());
        recordList.forEach(record -> {
            adminFileService.queryByBatchId(record.getStr("batch_id"),record);
            String businessIds = record.getStr("business_ids");
            List<CrmBusiness> businessList = new ArrayList<>();
            if (businessIds != null) {
                String[] businessIdsArr = businessIds.split(",");
                for (String businessId : businessIdsArr) {
                    businessList.add(CrmBusiness.dao.findById(Integer.valueOf(businessId)));
                }
            }
            String contactsIds = record.getStr("contacts_ids");
            List<CrmContacts> contactsList = new ArrayList<>();
            if (contactsIds != null) {
                String[] contactsIdsArr = contactsIds.split(",");
                for (String contactsId : contactsIdsArr) {
                    contactsList.add(CrmContacts.dao.findById(Integer.valueOf(contactsId)));
                }
            }
            record.set("business_list", businessList).set("contacts_list", contactsList);
        });
        return recordList;
    }

    /**
     * @author wyq
     * 联系人导出
     */
    public List<Record> exportContacts(String contactsIds) {
        String[] contactsIdsArr = contactsIds.split(",");
        return Db.find(Db.getSqlPara("crm.contact.excelExport", Kv.by("ids", contactsIdsArr)));
    }

    /**
     * @author wyq
     * 获取联系人导入查重字段
     */
    public R getCheckingField(){
        return R.ok().put("data","联系人姓名,电话,手机");
//        return R.ok().put("data",Db.getSql("crm.contacts.getCheckingField"));
    }

    /**
     * @author wyq
     * 导入联系人
     */
    @Before(Tx.class)
    public R uploadExcel(UploadFile file, Integer repeatHandling, Integer ownerUserId) {
        ExcelReader reader = ExcelUtil.getReader(FileUtil.file(file.getUploadPath() + "\\" + file.getFileName()));
        try {
            List<List<Object>> read = reader.read();
            List<Object> list = read.get(0);
            AdminFieldService adminFieldService = new AdminFieldService();
            Kv kv = new Kv();
            for (int i = 0; i < list.size(); i++) {
                kv.set(list.get(i), i);
            }
            List<Record> recordList = adminFieldService.list("3");
            List<Record> fieldList = queryField();
            fieldList.forEach(record -> {
                if (record.getInt("is_null") == 1){
                    record.set("name",record.getStr("name")+"(*)");
                }
            });
            List<String> nameList = fieldList.stream().map(record -> record.getStr("name")).collect(Collectors.toList());
            if (nameList.size() != list.size() || !nameList.containsAll(list)){
                return R.error("请使用最新导入模板");
            }
            if (read.size() > 1) {
                R status;
                JSONObject object = new JSONObject();
                for (int i = 1; i < read.size(); i++) {
                    List<Object> contactsList = read.get(i);
                    if (contactsList.size() < list.size()) {
                        for (int j = contactsList.size() - 1; j < list.size(); j++) {
                            contactsList.add(null);
                        }
                    }
                    String contactsName = contactsList.get(kv.getInt("姓名(*)")).toString();
                    String telephone = contactsList.get(kv.getInt("电话"))!=null?contactsList.get(kv.getInt("电话")).toString():null;
                    String mobile = contactsList.get(kv.getInt("手机"))!=null?contactsList.get(kv.getInt("手机")).toString():null;
                    Record  repeatField= Db.findFirst(Db.getSqlPara("crm.contact.queryRepeatFieldNumber",Kv.by("contactsName",contactsName).set("telephone",telephone).set("mobile",mobile)));
                    Integer number = repeatField.getInt("number");
                    Integer customerId = Db.queryInt("select customer_id from 72crm_crm_customer where customer_name = ?",contactsList.get(kv.getInt("客户名称(*)")));
                    if (0 == number) {
                        object.fluentPut("entity", new JSONObject().fluentPut("name", contactsName)
                                .fluentPut("customer_id",customerId)
                                .fluentPut("telephone", contactsList.get(kv.getInt("电话")))
                                .fluentPut("mobile", contactsList.get(kv.getInt("手机")))
                                .fluentPut("email",contactsList.get(kv.getInt("电子邮箱")))
                                .fluentPut("post",contactsList.get(kv.getInt("职务")))
                                .fluentPut("address", contactsList.get(kv.getInt("地址")))
                                .fluentPut("next_time", contactsList.get(kv.getInt("下次联系时间")))
                                .fluentPut("remark", contactsList.get(kv.getInt("备注")))
                                .fluentPut("owner_user_id", ownerUserId));
                    } else if (number == 1 ) {
                        if (repeatHandling == 1){
                            Record contacts = Db.findFirst(Db.getSqlPara("crm.contact.queryRepeatField",Kv.by("contactsName",contactsName).set("telephone",telephone).set("mobile",mobile)));
                            object.fluentPut("entity", new JSONObject().fluentPut("contacts_id", contacts.getInt("contacts_id"))
                                    .fluentPut("name", contactsName)
                                    .fluentPut("customer_id",customerId)
                                    .fluentPut("telephone", telephone)
                                    .fluentPut("mobile", mobile)
                                    .fluentPut("email",contactsList.get(kv.getInt("电子邮箱")))
                                    .fluentPut("post",contactsList.get(kv.getInt("职务")))
                                    .fluentPut("address", contactsList.get(kv.getInt("地址")))
                                    .fluentPut("next_time", contactsList.get(kv.getInt("下次联系时间")))
                                    .fluentPut("remark", contactsList.get(kv.getInt("备注")))
                                    .fluentPut("owner_user_id", ownerUserId)
                                    .fluentPut("batch_id", contacts.getStr("batch_id")));
                        }else if (repeatHandling == 2){
                            continue;
                        }
                    } else if (number > 1){
                        return R.error("数据多条重复");
                    }
                    JSONArray jsonArray = new JSONArray();
                    for (Record record : recordList) {
                        Integer columnsNum = kv.getInt(record.getStr("name"))!=null?kv.getInt(record.getStr("name")):kv.getInt(record.getStr("name")+"(*)");
                        record.set("value", contactsList.get(columnsNum));
                        jsonArray.add(JSONObject.parseObject(record.toJson()));
                    }
                    object.fluentPut("field", jsonArray);
                    status = addOrUpdate(object);
                    if ("500".equals(status.get("code"))) {
                        return R.error("第" + i + "行错误!");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return R.error();
        } finally {
            reader.close();
        }
        return R.ok();
    }
}
