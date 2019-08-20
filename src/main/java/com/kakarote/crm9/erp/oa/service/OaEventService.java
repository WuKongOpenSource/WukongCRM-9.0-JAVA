package com.kakarote.crm9.erp.oa.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.crm.entity.CrmBusiness;
import com.kakarote.crm9.erp.crm.entity.CrmContacts;
import com.kakarote.crm9.erp.crm.entity.CrmContract;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import com.kakarote.crm9.erp.oa.common.OaEnum;
import com.kakarote.crm9.erp.oa.entity.OaEvent;
import com.kakarote.crm9.erp.oa.entity.OaEventRelation;
import com.kakarote.crm9.utils.AuthUtil;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.kakarote.crm9.utils.TagUtil;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OaEventService {
    @Inject
    private OaActionRecordService oaActionRecordService;

    /**
     * @author wyq
     * 查询日程列表
     */
    public List<Record> queryList(OaEvent oaEvent){
        Date startTime = oaEvent.getStartTime();
        Date endTime = oaEvent.getEndTime();
        Integer userId = BaseUtil.getUserId().intValue();
        List<Record> recordList = Db.find(Db.getSql("oa.event.queryList"),endTime,startTime,userId,userId);
        if (recordList != null){
            for (Record record : recordList){
                record.set("createUser", Db.findFirst("select user_id,realname,img from 72crm_admin_user where user_id = ?", record.getInt("create_user_id")));
                queryRelateList(record);
            }
        }
        return recordList;
    }

    /**
     * @author wyq
     * 新增日程
     */
    public R add(OaEvent oaEvent){
        if (oaEvent.getStartTime() != null && oaEvent.getEndTime() != null){
            if((oaEvent.getStartTime().compareTo(oaEvent.getEndTime())) == 1){
                return R.error("结束时间早于开始时间");
            }
        }
        OaEventRelation oaEventRelation = new OaEventRelation();
        oaEventRelation.setCustomerIds(TagUtil.fromString(oaEvent.getCustomerIds()));
        oaEventRelation.setContactsIds(TagUtil.fromString(oaEvent.getContactsIds()));
        oaEventRelation.setBusinessIds(TagUtil.fromString(oaEvent.getBusinessIds()));
        oaEventRelation.setContractIds(TagUtil.fromString(oaEvent.getContractIds()));
        oaEvent.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
        oaEvent.setCreateTime(DateUtil.date());
        oaEvent.setOwnerUserIds(TagUtil.fromString(oaEvent.getOwnerUserIds()));
        oaEventRelation.setCreateTime(DateUtil.date());
        AdminUser user = BaseUtil.getUser();
        return Db.tx(() -> {
            oaEvent.save();
            oaActionRecordService.addRecord(oaEvent.getEventId(), OaEnum.EVENT_TYPE_KEY.getTypes(),1,oaActionRecordService.getJoinIds(user.getUserId().intValue(),oaEvent.getOwnerUserIds()),"");
            oaEventRelation.setEventId(oaEvent.getEventId());
            oaEventRelation.save();
            return true;
        }) ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 更新日程
     */
    public R update(OaEvent oaEvent){
        if (oaEvent.getStartTime() != null && oaEvent.getEndTime() != null){
            if((oaEvent.getStartTime().compareTo(oaEvent.getEndTime())) == 1){
                return R.error("结束时间早于开始时间");
            }
        }
        OaEventRelation oaEventRelation = new OaEventRelation();
        oaEventRelation.setEventId(oaEvent.getEventId());
        oaEventRelation.setCustomerIds(TagUtil.fromString(oaEvent.getCustomerIds()));
        oaEventRelation.setContactsIds(TagUtil.fromString(oaEvent.getContactsIds()));
        oaEventRelation.setBusinessIds(TagUtil.fromString(oaEvent.getBusinessIds()));
        oaEventRelation.setContractIds(TagUtil.fromString(oaEvent.getContractIds()));
        oaEvent.setUpdateTime(DateUtil.date());
        oaEvent.setOwnerUserIds(TagUtil.fromString(oaEvent.getOwnerUserIds()));
        AdminUser user = BaseUtil.getUser();
        return Db.tx(() -> {
            oaEvent.update();
            oaActionRecordService.addRecord(oaEvent.getEventId(), OaEnum.EVENT_TYPE_KEY.getTypes(),2,oaActionRecordService.getJoinIds(user.getUserId().intValue(),oaEvent.getOwnerUserIds()),"");
            oaEventRelation.setEventId(oaEvent.getEventId());
            Record eventRelation= Db.findFirst("select eventrelation_id from 72crm_oa_event_relation where event_id = ?",oaEvent.getEventId());
            oaEventRelation.setEventrelationId(eventRelation.getInt("eventrelation_id"));
            oaEventRelation.update();
            return true;
        }) ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 删除日程
     */
    public R delete(Integer eventId){
        oaActionRecordService.deleteRecord(OaEnum.EVENT_TYPE_KEY.getTypes(),eventId);
        return Db.delete(Db.getSql("oa.event.delete"),eventId) > 0 ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * crm查询日程
     */
    public R queryEventRelation(BasePageRequest<OaEventRelation> basePageRequest){
        OaEventRelation relation = basePageRequest.getData();
        if(AuthUtil.oaAnth(relation.toRecord())){
            return R.noAuth();
        }
        Page<Record> recordPage = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("oa.event.queryEventRelation", Kv.by("businessIds", relation.getBusinessIds()).set("contactsIds", relation.getContactsIds()).set("contractIds", relation.getContractIds()).set("customerIds", relation.getCustomerIds())));
        for (Record record : recordPage.getList()){
            record.set("createUser",Kv.by("user_id",record.get("create_user_id")).set("realname",record.get("realname")).set("img",record.get("img")));
            queryRelateList(record);
        }
        return R.ok().put("data",recordPage);
    }

    public void queryRelateList(Record record){
        if (record.getInt("create_user_id") == BaseUtil.getUser().getUserId().intValue()){
            record.set("permission",Kv.by("is_update",1).set("is_delete",1));
        }else {
            record.set("permission",Kv.by("is_update",0).set("is_delete",0));
        }
        List<AdminUser> adminUserList = new ArrayList<>();
        if (StrUtil.isNotEmpty(record.getStr("owner_user_ids"))){
            String[] ownerUserIdsArr = record.getStr("owner_user_ids").split(",");
            adminUserList = AdminUser.dao.find(Db.getSqlPara("oa.event.queryOwnerList",Kv.by("ids",ownerUserIdsArr)));
        }
        record.set("ownerList",adminUserList);
        List<CrmCustomer> customerList = new ArrayList<>();
        if (StrUtil.isNotEmpty(record.getStr("customer_ids"))){
            String[] customerIdsArr = record.getStr("customer_ids").split(",");
            customerList = CrmCustomer.dao.find(Db.getSqlPara("oa.event.queryCustomerList",Kv.by("ids",customerIdsArr)));
        }
        record.set("customerList",customerList);
        List<CrmContacts> contactsList = new ArrayList<>();
        if (StrUtil.isNotEmpty(record.getStr("contacts_ids"))){
            String[] contactsIdsArr = record.getStr("contacts_ids").split(",");
            contactsList = CrmContacts.dao.find(Db.getSqlPara("oa.event.queryContactsList",Kv.by("ids",contactsIdsArr)));
        }
        record.set("contactsList",contactsList);
        List<CrmBusiness> businessList = new ArrayList<>();
        if (StrUtil.isNotEmpty(record.getStr("business_ids"))){
            String[] businessIdsArr = record.getStr("business_ids").split(",");
            businessList = CrmBusiness.dao.find(Db.getSqlPara("oa.event.queryBusinessList",Kv.by("ids",businessIdsArr)));
        }
        record.set("businessList",businessList);
        List<CrmContract> contractList = new ArrayList<>();
        if (StrUtil.isNotEmpty(record.getStr("contract_ids"))){
            String[] contractIdsArr = record.getStr("contract_ids").split(",");
            contractList = CrmContract.dao.find(Db.getSqlPara("oa.event.queryContractList",Kv.by("ids",contractIdsArr)));
        }
        record.set("contractList",contractList);
    }

    public Record queryById(Integer eventId){
        Record record = Db.findFirst(Db.getSql("oa.event.queryById"),eventId);
        record.set("createUser", Db.findFirst("select user_id,realname,img from 72crm_admin_user where user_id = ?", record.getInt("create_user_id")));
        queryRelateList(record);
        return record;
    }

}
