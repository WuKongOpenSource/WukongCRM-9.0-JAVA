package com.kakarote.crm9.erp.crm.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.erp.admin.entity.AdminRecord;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminFieldService;
import com.kakarote.crm9.erp.admin.service.AdminFileService;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.common.CrmParamValid;
import com.kakarote.crm9.erp.crm.entity.CrmBusiness;
import com.kakarote.crm9.erp.crm.entity.CrmContacts;
import com.kakarote.crm9.erp.crm.entity.CrmContactsBusiness;
import com.kakarote.crm9.erp.oa.common.OaEnum;
import com.kakarote.crm9.erp.oa.entity.OaEvent;
import com.kakarote.crm9.erp.oa.entity.OaEventRelation;
import com.kakarote.crm9.erp.oa.service.OaActionRecordService;
import com.kakarote.crm9.utils.AuthUtil;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.FieldUtil;
import com.kakarote.crm9.utils.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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

    @Inject
    private CrmParamValid crmParamValid;

    /**
     * @author wyq
     * 分页条件查询联系人
     */
    public Page<Record> queryList(BasePageRequest<CrmContacts> basePageRequest){
        String contactsName = basePageRequest.getData().getName();
        String telephone = basePageRequest.getData().getTelephone();
        String mobile = basePageRequest.getData().getMobile();
        String customerName = basePageRequest.getData().getCustomerName();
        if (!crmParamValid.isValid(customerName)){
            return new Page<>();
        }
        if (StrUtil.isEmpty(contactsName) && StrUtil.isEmpty(telephone) && StrUtil.isEmpty(mobile) && StrUtil.isEmpty(customerName)){
            return new Page<>();
        }
        return Db.paginate(basePageRequest.getPage(),basePageRequest.getLimit(), Db.getSqlPara("crm.contact.getContactsPageList", Kv.by("contactsName",contactsName).set("customerName",customerName).set("telephone",telephone).set("mobile",mobile)));
    }

    /**
     * @author wyq
     * 根据id查询联系人
     */
    public Record queryById(Integer contactsId){
        Record crmContacts = Db.findFirst(Db.getSql("crm.contact.queryById"),contactsId);
        List<Record> recordList = Db.find("select name,value from `72crm_admin_fieldv` where batch_id = ?", crmContacts.getStr("batch_id"));
        recordList.forEach(field->crmContacts.set(field.getStr("name"),field.getStr("value")));
        return crmContacts;
    }

    /**
     * @author wyq
     * 基本信息
     */
    public List<Record> information(Integer contactsId){
        Record record = queryById(contactsId);
        List<String> keyList = Arrays.asList("name", "telephone", "mobile","email","post","address","next_time","remark");
        List<Record> recordList = adminFieldService.queryInformation(CrmEnum.CRM_CONTACTS,record, keyList);
        recordList.add(new Record().set("name","客户名称").set("value",new Record().set("customerId",record.getInt("customer_id")).set("customerName",record.getStr("customer_name"))).set("formType","customer").set("field_type",1));
        return recordList.stream().sorted(Comparator.comparingInt(r->-r.getInt("field_type"))).map(r-> r.remove("field_type","field_name","setting","type")).collect(Collectors.toList());
    }

    /**
     * @author wyq
     * 根据联系人名称查询
     */
    public Record queryByName(String name) {
        return Db.findFirst(Db.getSql("crm.contact.queryByName"), name);
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
    @Before(Tx.class)
    public R relateBusiness(Integer contactsId, String businessIds){
        String[] businessIdsArr = businessIds.split(",");
        Db.delete("delete from 72crm_crm_contacts_business where contacts_id = ?",contactsId);
        List<CrmContactsBusiness> crmContactsBusinessList = new ArrayList<>();
        for (String id:businessIdsArr){
            CrmContactsBusiness crmContactsBusiness = new CrmContactsBusiness();
            crmContactsBusiness.setContactsId(contactsId);
            crmContactsBusiness.setBusinessId(Integer.valueOf(id));
            crmContactsBusinessList.add(crmContactsBusiness);
        }
        Db.batchSave(crmContactsBusinessList,100);
        return R.ok();
    }

    /**
     * @author wyq
     * 联系人解除关联商机
     */
    public R unrelateBusiness(Integer contactsId, String businessIds){
        String[] idsArr = businessIds.split(",");
        SqlPara sqlPara = Db.getSqlPara("crm.contact.unrelateBusiness",Kv.by("contactsId",contactsId).set("ids",idsArr));
        Db.delete(sqlPara.getSql(),sqlPara.getPara());
        return R.ok();
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
            crmRecordService.updateRecord(new CrmContacts().dao().findById(crmContacts.getContactsId()),crmContacts, CrmEnum.CRM_CONTACTS);
            return crmContacts.update() ? R.ok() : R.error();
        }else {
            crmContacts.setCreateTime(DateUtil.date());
            crmContacts.setUpdateTime(DateUtil.date());
            crmContacts.setCreateUserId(BaseUtil.getUserId());
            if (crmContacts.getOwnerUserId() == null){
                crmContacts.setOwnerUserId(BaseUtil.getUserId());
            }
            crmContacts.setBatchId(batchId);
            boolean save = crmContacts.save();
            if (jsonObject.getInteger("businessId") != null){
                CrmContactsBusiness crmContactsBusiness = new CrmContactsBusiness();
                crmContactsBusiness.setBusinessId(jsonObject.getInteger("businessId"));
                crmContactsBusiness.setContactsId(crmContacts.getContactsId());
                crmContactsBusiness.save();
            }
            crmRecordService.addRecord(crmContacts.getContactsId(),CrmEnum.CRM_CONTACTS);
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
        List<Record> batchIdList = Db.find(Db.getSqlPara("crm.contact.queryBatchIdByIds",Kv.by("ids",idsArr)));
        return Db.tx(() ->{
            Db.batch(Db.getSql("crm.contact.deleteByIds"),"contacts_id",idsList,100);
            Db.batch("delete from 72crm_admin_fieldv where batch_id = ?","batch_id",batchIdList,100);
            return true;
        }) ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 联系人转移
     */
    @Before(Tx.class)
    public R transfer(CrmContacts crmContacts){
        String[] contactsIdsArr = crmContacts.getContactsIds().split(",");
        int update = Db.update(Db.getSqlPara("crm.contact.transfer", Kv.by("ownerUserId", crmContacts.getNewOwnerUserId()).set("ids", contactsIdsArr)));
        for(String contactsId : contactsIdsArr){
            if (!BaseUtil.getUserId().equals(BaseConstant.SUPER_ADMIN_USER_ID) && !AuthUtil.isRwAuth(Integer.parseInt(contactsId),"customer")){
                return R.error("无权限转移");
            }
            crmRecordService.addConversionRecord(Integer.valueOf(contactsId),CrmEnum.CRM_CONTACTS,crmContacts.getNewOwnerUserId());
        }
        return update > 0 ? R.ok() : R.error();
    }

    /**
     * 根据客户id变更负责人
     * @param customerId 客户ID
     * @param ownerUserId 负责人ID
     */
    public R updateOwnerUserId(Integer customerId,Long ownerUserId){
        List<Integer> contactsIdList = Db.query("select contacts_id from 72crm_crm_contacts where customer_id = ?",customerId);
        for (Integer contactsId : contactsIdList) {
            if (!BaseUtil.getUserId().equals(BaseConstant.SUPER_ADMIN_USER_ID) && !AuthUtil.isRwAuth(contactsId, "contacts")) {
                return R.error("无权限转移");
            }
        }
        Db.update("update 72crm_crm_contacts set owner_user_id = " + ownerUserId + " where customer_id = "+customerId);
        crmRecordService.addConversionRecord(customerId,CrmEnum.CRM_CUSTOMER,ownerUserId);
        return R.ok();
    }

    /**
     * @author wyq
     * 查询编辑字段
     */
    public List<Record> queryField(Integer contactsId) {
        Record contacts = queryById(contactsId);
        List<Record> customerList = new ArrayList<>();
        Record customer = new Record();
        customerList.add(customer.set("customer_id",contacts.getInt("customer_id")).set("customer_name",contacts.getStr("customer_name")));
        contacts.set("customer_id",customerList);
        return adminFieldService.queryUpdateField(CrmEnum.CRM_CONTACTS.getType(),contacts);
    }

    /**
     * @author wyq
     * 添加跟进记录
     */
    @Before(Tx.class)
    public R addRecord(AdminRecord adminRecord){
        adminRecord.setTypes("crm_contacts");
        adminRecord.setCreateTime(DateUtil.date());
        adminRecord.setCreateUserId(BaseUtil.getUser().getUserId());
        if (StrUtil.isEmpty(adminRecord.getCategory())){
            return R.error("跟进类型不能为空");
        }
        if (1 == adminRecord.getIsEvent()){
            OaEvent oaEvent = new OaEvent();
            oaEvent.setTitle(adminRecord.getContent());
            oaEvent.setStartTime(adminRecord.getNextTime());
            oaEvent.setEndTime(DateUtil.offsetDay(adminRecord.getNextTime(),1));
            oaEvent.setCreateTime(DateUtil.date());
            oaEvent.setCreateUserId(BaseUtil.getUser().getUserId());
            oaEvent.save();

            AdminUser user = BaseUtil.getUser();
            oaActionRecordService.addRecord(oaEvent.getEventId(), OaEnum.EVENT_TYPE_KEY.getTypes(),1,oaActionRecordService.getJoinIds(user.getUserId(),oaEvent.getOwnerUserIds()),oaActionRecordService.getJoinIds(Long.valueOf(user.getDeptId()),""));
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
     * 获取联系人导入查重字段
     */
    public R getCheckingField(){
        return R.ok().put("data","联系人姓名,电话,手机");
    }
}
