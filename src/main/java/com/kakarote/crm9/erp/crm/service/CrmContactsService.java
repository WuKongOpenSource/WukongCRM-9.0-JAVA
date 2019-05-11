package com.kakarote.crm9.erp.crm.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.admin.entity.AdminRecord;
import com.kakarote.crm9.erp.admin.service.AdminFieldService;
import com.kakarote.crm9.erp.admin.service.AdminFileService;
import com.kakarote.crm9.erp.crm.entity.CrmContacts;
import com.kakarote.crm9.erp.oa.entity.OaEvent;
import com.kakarote.crm9.erp.oa.entity.OaEventRelation;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
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

public class CrmContactsService {
    @Inject
    private AdminFieldService adminFieldService;

    @Inject
    private FieldUtil fieldUtil;

    @Inject
    private CrmRecordService crmRecordService;

    @Inject
    private AdminFileService adminFileService;

    /**
     * @author wyq
     * 分页条件查询联系人
     */
    public Page<Record> queryList(BasePageRequest basePageRequest){
        return Db.paginate(basePageRequest.getPage(),basePageRequest.getLimit(),new SqlPara().setSql("select * from contactsView"));
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
            return R.ok().put("data",Db.findFirst(Db.getSqlPara("crm.contact.queryBusiness",Kv.by("id",contactsId))));
        }else {
            return R.ok().put("data",Db.paginate(basePageRequest.getPage(),basePageRequest.getLimit(),new SqlPara().setSql(Db.getSql("crm.contact.queryBusiness")).addPara(contactsId)));
        }
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
            crmContacts.setOwnerUserId(BaseUtil.getUserId().intValue());
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
        fieldUtil.getFixedField(fieldList,"name","姓名","","text",settingArr,0);
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
        fieldUtil.getFixedField(fieldList,"name","姓名",record.getStr("name"),"text",settingArr,0);
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
            OaEventRelation oaEventRelation = new OaEventRelation();
            oaEventRelation.setEventId(oaEvent.getEventId());
            oaEventRelation.setContactsIds(adminRecord.getTypesId().toString());
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
        List<Record> recordList = Db.find(Db.getSql("crm.contact.getRecord"),crmContacts.getContactsId());
        recordList.forEach(record -> {
            adminFileService.queryByBatchId(record.getStr("batch_id"),record);
        });
        return recordList;
    }
}
