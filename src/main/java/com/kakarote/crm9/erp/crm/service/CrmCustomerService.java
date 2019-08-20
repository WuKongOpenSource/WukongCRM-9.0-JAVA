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
import com.kakarote.crm9.erp.admin.entity.AdminConfig;
import com.kakarote.crm9.erp.admin.service.AdminSceneService;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.admin.entity.AdminRecord;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminFieldService;
import com.kakarote.crm9.erp.admin.service.AdminFileService;
import com.kakarote.crm9.erp.crm.common.CrmParamValid;
import com.kakarote.crm9.erp.crm.entity.*;
import com.kakarote.crm9.erp.oa.common.OaEnum;
import com.kakarote.crm9.erp.oa.entity.OaEvent;
import com.kakarote.crm9.erp.oa.entity.OaEventRelation;
import com.kakarote.crm9.erp.oa.service.OaActionRecordService;
import com.kakarote.crm9.utils.*;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;

import java.util.*;
import java.util.stream.Collectors;

public class CrmCustomerService {
    @Inject
    private AdminFieldService adminFieldService;

    @Inject
    private FieldUtil fieldUtil;

    @Inject
    private CrmRecordService crmRecordService;

    @Inject
    private AdminFileService adminFileService;

    @Inject
    private AdminSceneService adminSceneService;

    @Inject
    private OaActionRecordService oaActionRecordService;

    @Inject
    private CrmParamValid crmParamValid;

    @Inject
    private AuthUtil authUtil;

    /**
     * @author wyq
     * 分页条件查询客户
     */
    public Page<Record> getCustomerPageList(BasePageRequest<CrmCustomer> basePageRequest) {
        String customerName = basePageRequest.getData().getCustomerName();
        if (!crmParamValid.isValid(customerName)){
            return new Page<>();
        }
        String mobile = basePageRequest.getData().getMobile();
        String telephone = basePageRequest.getData().getTelephone();
        if (StrUtil.isEmpty(customerName)&&StrUtil.isEmpty(telephone)&&StrUtil.isEmpty(mobile)){
            return new Page<>();
        }
        return Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("crm.customer.getCustomerPageList",Kv.by("customerName",customerName).set("mobile",mobile).set("telephone",telephone)));
    }

    /**
     * @author wyq
     * 新增或更新客户
     */
    public R addOrUpdate(JSONObject jsonObject,String type) {
        CrmCustomer crmCustomer = jsonObject.getObject("entity", CrmCustomer.class);
        String batchId = StrUtil.isNotEmpty(crmCustomer.getBatchId()) ? crmCustomer.getBatchId() : IdUtil.simpleUUID();
        crmRecordService.updateRecord(jsonObject.getJSONArray("field"), batchId);
        adminFieldService.save(jsonObject.getJSONArray("field"), batchId);
        if (crmCustomer.getCustomerId() != null) {
            CrmCustomer oldCrmCustomer = new CrmCustomer().dao().findById(crmCustomer.getCustomerId());
            crmRecordService.updateRecord(oldCrmCustomer, crmCustomer, CrmEnum.CUSTOMER_TYPE_KEY.getTypes());
            crmCustomer.setUpdateTime(DateUtil.date());
            return crmCustomer.update() ? R.ok() : R.error();
        } else {
            crmCustomer.setCreateTime(DateUtil.date());
            crmCustomer.setUpdateTime(DateUtil.date());
            crmCustomer.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
            if ("noImport".equals(type)){
                crmCustomer.setOwnerUserId(BaseUtil.getUser().getUserId().intValue());
            }
            crmCustomer.setBatchId(batchId);
            crmCustomer.setRwUserId(",");
            crmCustomer.setRoUserId(",");
            boolean save = crmCustomer.save();
            crmRecordService.addRecord(crmCustomer.getCustomerId(), CrmEnum.CUSTOMER_TYPE_KEY.getTypes());
            return save ? R.ok().put("data", Kv.by("customer_id", crmCustomer.getCustomerId()).set("customer_name", crmCustomer.getCustomerName())) : R.error();
        }
    }

    /**
     * @author wyq
     * 根据客户id查询
     */
    public Record queryById(Integer customerId) {
        return Db.findFirst(Db.getSql("crm.customer.queryById"), customerId);
    }

    /**
     * @author wyq
     * 基本信息
     */
    public List<Record> information(Integer customerId) {
        CrmCustomer crmCustomer = CrmCustomer.dao.findById(customerId);
        List<Record> fieldList = new ArrayList<>();
        FieldUtil field = new FieldUtil(fieldList);
        field.set("客户名称", crmCustomer.getCustomerName())
                .set("成交状态", crmCustomer.getDealStatus())
                .set("下次联系时间", DateUtil.formatDateTime(crmCustomer.getNextTime()))
                .set("网址", crmCustomer.getWebsite())
                .set("备注", crmCustomer.getRemark())
                .set("电话", crmCustomer.getTelephone())
                .set("手机", crmCustomer.getMobile())
                .set("定位", crmCustomer.getLocation())
                .set("区域", crmCustomer.getAddress())
                .set("详细地址", crmCustomer.getDetailAddress());
        List<Record> recordList = Db.find(Db.getSql("admin.field.queryCustomField"),crmCustomer.getBatchId());
        fieldUtil.handleType(recordList);
        fieldList.addAll(recordList);
        return fieldList;
    }

    /**
     * @author wyq
     * 根据客户名称查询
     */
    public Record queryByName(String name) {
        return Db.findFirst(Db.getSql("crm.customer.queryByName"), name);
    }

    /**
     * @author wyq
     * 根据客户id查找商机
     */
    public R queryBusiness(BasePageRequest<CrmCustomer> basePageRequest) {
        JSONObject jsonObject = basePageRequest.getJsonObject();
        Integer customerId = jsonObject.getInteger("customerId");
        String search = jsonObject.getString("search");
        Integer pageType = basePageRequest.getPageType();
        if (0 == pageType) {
            List<Record> recordList = Db.find(Db.getSqlPara("crm.customer.queryBusiness", Kv.by("customerId", customerId).set("businessName", search)));
            adminSceneService.setBusinessStatus(recordList);
            return R.ok().put("data", recordList);
        } else {
            Page<Record> paginate = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("crm.customer.queryBusiness", Kv.by("customerId", customerId).set("businessName", search)));
            adminSceneService.setBusinessStatus(paginate.getList());
            return R.ok().put("data",paginate );
        }
    }


    /**
     * @author wyq
     * 根据客户id查询联系人
     */
    public R queryContacts(BasePageRequest<CrmCustomer> basePageRequest) {
        Integer customerId = basePageRequest.getData().getCustomerId();
        Integer pageType = basePageRequest.getPageType();
        String search = basePageRequest.getJsonObject().getString("search");
        if (0 == pageType) {
            return R.ok().put("data", Db.find(Db.getSqlPara("crm.customer.queryContacts",Kv.by("customerId", customerId).set("contactsName", search))));
        } else {
            return R.ok().put("data", Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("crm.customer.queryContacts",Kv.by("customerId", customerId).set("contactsName", search))));
        }
    }

    /**
     * @auyhor wyq
     * 根据客户id查询合同
     */
    public R queryContract(BasePageRequest<CrmCustomer> basePageRequest) {
        Integer customerId = basePageRequest.getData().getCustomerId();
        Integer pageType = basePageRequest.getPageType();
        String search = basePageRequest.getJsonObject().getString("search");
        if (basePageRequest.getData().getCheckstatus() != null){
            if (0 == pageType) {
                return R.ok().put("data", Db.find(Db.getSqlPara("crm.customer.queryPassContract",Kv.by("customerId",customerId).set("checkStatus",basePageRequest.getData().getCheckstatus()).set("contractName",search))));
            } else {
                return R.ok().put("data", Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("crm.customer.queryPassContract",Kv.by("customerId",customerId).set("checkStatus",basePageRequest.getData().getCheckstatus()).set("contractName",search))));
            }
        }
        if (0 == pageType) {
            return R.ok().put("data", Db.find(Db.getSqlPara("crm.customer.queryContract",Kv.by("customerId",customerId).set("contractName",search))));
        } else {
            return R.ok().put("data", Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("crm.customer.queryContract",Kv.by("customerId", customerId).set("contractName", search))));
        }
    }

    /**
     * @author wyq
     * 根据客户id查询回款计划
     */
    public R queryReceivablesPlan(BasePageRequest<CrmCustomer> basePageRequest) {
        Integer customerId = basePageRequest.getData().getCustomerId();
        Integer pageType = basePageRequest.getPageType();
        if (0 == pageType) {
            return R.ok().put("data", Db.find(Db.getSql("crm.customer.queryReceivablesPlan"), customerId));
        } else {
            return R.ok().put("data", Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), new SqlPara().setSql(Db.getSql("crm.customer.queryReceivablesPlan")).addPara(customerId)));
        }
    }

    /**
     * @author wyq
     * 根据客户id查询回款
     */
    public R queryReceivables(BasePageRequest<CrmCustomer> basePageRequest) {
        Integer customerId = basePageRequest.getData().getCustomerId();
        if (0 == basePageRequest.getPageType()) {
            return R.ok().put("data", Db.find(Db.getSql("crm.customer.queryReceivables"), customerId));
        } else {
            return R.ok().put("data", Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), new SqlPara().setSql(Db.getSql("crm.customer.queryReceivables")).addPara(customerId)));
        }
    }

    /**
     * @author wyq
     * 根据id删除客户
     */
    public R deleteByIds(String customerIds) {
        Integer contactsNum = Db.queryInt(Db.getSql("crm.customer.queryContactsNumber"), customerIds);
        Integer businessNum = Db.queryInt(Db.getSql("crm.customer.queryBusinessNumber"), customerIds);
        if (contactsNum > 0 || businessNum > 0) {
            return R.error("该条数据与其他数据有必要关联，请勿删除");
        }
        String[] idsArr = customerIds.split(",");
        List<Record> idsList = new ArrayList<>();
        for (String id : idsArr) {
            Record record = new Record();
            idsList.add(record.set("customer_id", Integer.valueOf(id)));
        }
        List<Record> batchIdList = Db.find(Db.getSqlPara("crm.customer.queryBatchIdByIds",Kv.by("ids",idsArr)));
        return Db.tx(() -> {
            Db.batch(Db.getSql("crm.customer.deleteByIds"), "customer_id", idsList, 100);
            Db.batch("delete from 72crm_admin_fieldv where batch_id = ?","batch_id",batchIdList,100);
            return true;
        }) ? R.ok() : R.error();
    }

    /**
     * @author zxy
     * 条件查询客户公海
     */
    public Page<Record> queryPageGH(BasePageRequest basePageRequest) {
        return Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), new SqlPara().setSql("select *  from customerview where owner_user_id = 0"));
    }

    /**
     * @author wyq
     * 客户锁定
     */
    public R lock(CrmCustomer crmCustomer) {
        String[] ids = crmCustomer.getIds().split(",");
        crmRecordService.addIsLockRecord(ids,CrmEnum.CUSTOMER_TYPE_KEY.getTypes(),crmCustomer.getIsLock());
        return Db.update(Db.getSqlPara("crm.customer.lock", Kv.by("isLock", crmCustomer.getIsLock()).set("ids", ids))) > 0 ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 变更负责人
     */
    public R updateOwnerUserId(CrmCustomer crmCustomer) {
        String[] customerIdsArr = crmCustomer.getCustomerIds().split(",");
        return Db.tx(() -> {
            for (String customerId : customerIdsArr) {
                String memberId = "," + crmCustomer.getNewOwnerUserId() + ",";
                Db.update(Db.getSql("crm.customer.deleteMember"), memberId, memberId, Integer.valueOf(customerId));
                CrmCustomer oldCustomer = CrmCustomer.dao.findById(Integer.valueOf(customerId));
                if (2 == crmCustomer.getTransferType()) {
                    if (1 == crmCustomer.getPower()) {
                        crmCustomer.setRoUserId(oldCustomer.getRoUserId() + oldCustomer.getOwnerUserId() + ",");
                    }
                    if (2 == crmCustomer.getPower()) {
                        crmCustomer.setRwUserId(oldCustomer.getRwUserId() + oldCustomer.getOwnerUserId() + ",");
                    }
                }
                crmCustomer.setCustomerId(Integer.valueOf(customerId));
                crmCustomer.setOwnerUserId(crmCustomer.getNewOwnerUserId());
                crmCustomer.setFollowup(0);
                crmCustomer.update();
                crmRecordService.addConversionRecord(Integer.valueOf(customerId), CrmEnum.CUSTOMER_TYPE_KEY.getTypes(), crmCustomer.getNewOwnerUserId());
            }
            return true;
        }) ? R.ok() : R.error();
    }


    /**
     * @author wyq
     * 查询团队成员
     */
    public List<Record> getMembers(Integer customerId) {
        CrmCustomer crmCustomer = CrmCustomer.dao.findById(customerId);
        if (null == crmCustomer) {
            return null;
        }
        List<Record> recordList = new ArrayList<>();
        if (crmCustomer.getOwnerUserId() != null) {
            Record ownerUser = Db.findFirst(Db.getSql("crm.customer.getMembers"), crmCustomer.getOwnerUserId());
            if (ownerUser != null) {
                recordList.add(ownerUser.set("power", "负责人权限").set("groupRole", "负责人"));
            }
        }
        String roUserId = crmCustomer.getRoUserId();
        String rwUserId = crmCustomer.getRwUserId();
        String memberIds = roUserId + rwUserId.substring(1);
        if (",".equals(memberIds)) {
            return recordList;
        }
        String[] memberIdsArr = memberIds.substring(1, memberIds.length() - 1).split(",");
        Set<String> memberIdsSet = new HashSet<>(Arrays.asList(memberIdsArr));
        for (String memberId : memberIdsSet) {
            Record record = Db.findFirst(Db.getSql("crm.customer.getMembers"), memberId);
            if (roUserId.contains(memberId)) {
                record.set("power", "只读").set("groupRole", "普通成员");
            }
            if (rwUserId.contains(memberId)) {
                record.set("power", "读写").set("groupRole", "普通成员");
            }
            recordList.add(record);
        }
        return recordList;
    }

    /**
     * @author wyq
     * 添加团队成员
     */
    public R addMember(CrmCustomer crmCustomer) {
        String[] customerIdsArr = crmCustomer.getIds().split(",");
        String[] memberArr = crmCustomer.getMemberIds().split(",");
        StringBuffer stringBuffer = new StringBuffer();
        for (String id : customerIdsArr) {
            Integer ownerUserId = CrmCustomer.dao.findById(Integer.valueOf(id)).getOwnerUserId();
            for (String memberId : memberArr) {
                if (ownerUserId.equals(Integer.valueOf(memberId))) {
                    return R.error("负责人不能重复选为团队成员!");
                }
                Db.update(Db.getSql("crm.customer.deleteMember"), "," + memberId + ",", "," + memberId + ",", Integer.valueOf(id));
            }
            if (1 == crmCustomer.getPower()) {
                stringBuffer.setLength(0);
                String roUserId = stringBuffer.append(CrmCustomer.dao.findById(Integer.valueOf(id)).getRoUserId()).append(crmCustomer.getMemberIds()).append(",").toString();
                Db.update("update 72crm_crm_customer set ro_user_id = ? where customer_id = ?", roUserId, Integer.valueOf(id));
            }
            if (2 == crmCustomer.getPower()) {
                stringBuffer.setLength(0);
                String rwUserId = stringBuffer.append(CrmCustomer.dao.findById(Integer.valueOf(id)).getRwUserId()).append(crmCustomer.getMemberIds()).append(",").toString();
                Db.update("update 72crm_crm_customer set rw_user_id = ? where customer_id = ?", rwUserId, Integer.valueOf(id));
            }
        }
        return R.ok();
    }

    /**
     * @author wyq
     * 删除团队成员
     */
    public R deleteMembers(CrmCustomer crmCustomer) {
        String[] customerIdsArr = crmCustomer.getIds().split(",");
        String[] memberArr = crmCustomer.getMemberIds().split(",");
        return Db.tx(() -> {
            for (String id : customerIdsArr) {
                for (String memberId : memberArr) {
                    Db.update(Db.getSql("crm.customer.deleteMember"), "," + memberId + ",", "," + memberId + ",", Integer.valueOf(id));
                }
            }
            return true;
        }) ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 根据客户ids获取合同ids
     */
    public String getContractIdsByCustomerIds(String customerIds) {
        String[] customerIdsArr = customerIds.split(",");
        StringBuffer stringBuffer = new StringBuffer();
        for (String id : customerIdsArr) {
            List<Record> recordList = Db.find("select contract_id from 72crm_crm_contract where customer_id = ?", id);
            if (recordList != null) {
                for (Record record : recordList) {
                    stringBuffer.append(",").append(record.getStr("contract_id"));
                }
            }
        }
        if (stringBuffer.length() > 0) {
            stringBuffer.deleteCharAt(0);
        }
        return stringBuffer.toString();
    }

    /**
     * @author wyq
     * 根据客户ids获取商机ids
     */
    public String getBusinessIdsByCustomerIds(String customerIds) {
        String[] customerIdsArr = customerIds.split(",");
        StringBuffer stringBuffer = new StringBuffer();
        for (String id : customerIdsArr) {
            List<Record> recordList = Db.find("select business_id from 72crm_crm_business where customer_id = ?", id);
            if (recordList != null) {
                for (Record record : recordList) {
                    stringBuffer.append(",").append(record.getStr("business_id"));
                }
            }
        }
        if (stringBuffer.length() > 0) {
            stringBuffer.deleteCharAt(0);
        }
        return stringBuffer.toString();
    }

    /**
     * @author zxy
     * 定时将客户放入公海
     */
    public void putInInternational(Record record) {
        List<Integer> ids = Db.query(Db.getSql("crm.customer.selectOwnerUserId"), Integer.valueOf(record.getStr("followupDay")) * 60 * 60 * 24, Integer.valueOf(record.getStr("dealDay")) * 60 * 60 * 24);
        if(ids != null && ids.size() > 0){
            crmRecordService.addPutIntoTheOpenSeaRecord(ids,CrmEnum.CUSTOMER_TYPE_KEY.getTypes());
            Db.update(Db.getSqlPara("crm.customer.updateOwnerUserId", Kv.by("ids",ids)));
        }
    }

    /**
     * @author wyq
     * 查询编辑字段
     */
    public List<Record> queryField(Integer customerId) {
        Record customer = Db.findFirst("select * from customerview where customer_id = ?",customerId);
        List<Record> fieldList = adminFieldService.queryUpdateField(2,customer);
        fieldList.add(new Record().set("fieldName", "map_address")
                .set("name", "地区定位")
                .set("value", Kv.by("location", customer.getStr("location"))
                        .set("address", customer.getStr("address"))
                        .set("detailAddress", customer.getStr("detail_address"))
                        .set("lng", customer.getStr("lng"))
                        .set("lat", customer.getStr("lat")))
                .set("formType", "map_address")
                .set("isNull", 0));
        return fieldList;
    }


    /**
     * @author wyq
     * 添加跟进记录
     */
    @Before(Tx.class)
    public R addRecord(AdminRecord adminRecord) {
        adminRecord.setTypes("crm_customer");
        adminRecord.setCreateTime(DateUtil.date());
        adminRecord.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
        if (1 == adminRecord.getIsEvent()) {
            OaEvent oaEvent = new OaEvent();
            oaEvent.setTitle(adminRecord.getContent());
            oaEvent.setStartTime(adminRecord.getNextTime());
            oaEvent.setEndTime(DateUtil.offsetDay(adminRecord.getNextTime(), 1));
            oaEvent.setCreateTime(DateUtil.date());
            oaEvent.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
            oaEvent.save();
            AdminUser user = BaseUtil.getUser();
            oaActionRecordService.addRecord(oaEvent.getEventId(), OaEnum.EVENT_TYPE_KEY.getTypes(),1,oaActionRecordService.getJoinIds(user.getUserId().intValue(),oaEvent.getOwnerUserIds()),oaActionRecordService.getJoinIds(user.getDeptId(),""));
            OaEventRelation oaEventRelation = new OaEventRelation();
            oaEventRelation.setEventId(oaEvent.getEventId());
            oaEventRelation.setCustomerIds(","+adminRecord.getTypesId().toString()+",");
            oaEventRelation.setCreateTime(DateUtil.date());
            oaEventRelation.save();
        }
        if (adminRecord.getNextTime() != null) {
            Date nextTime = adminRecord.getNextTime();
            CrmCustomer crmCustomer = new CrmCustomer();
            crmCustomer.setCustomerId(adminRecord.getTypesId());
            crmCustomer.setNextTime(nextTime);
            crmCustomer.update();
            if (adminRecord.getContactsIds() != null) {
                String[] idsArr = adminRecord.getContactsIds().split(",");
                for (String id : idsArr) {
                    CrmContacts crmContacts = new CrmContacts();
                    crmContacts.setContactsId(Integer.valueOf(id));
                    crmContacts.setNextTime(nextTime);
                    crmContacts.update();
                }
            }
            if (adminRecord.getBusinessIds() != null) {
                String[] idsArr = adminRecord.getBusinessIds().split(",");
                for (String id : idsArr) {
                    CrmBusiness crmBusiness = new CrmBusiness();
                    crmBusiness.setBusinessId(Integer.valueOf(id));
                    crmBusiness.setNextTime(nextTime);
                    crmBusiness.update();
                }
            }
        }
        Db.update("update 72crm_crm_customer set followup = 1 where customer_id = ?",adminRecord.getTypesId());
        return adminRecord.save() ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 查看跟进记录
     */
    public List<Record> getRecord(BasePageRequest<CrmCustomer> basePageRequest) {
        CrmCustomer crmCustomer = basePageRequest.getData();
        List<Record> recordList = Db.find(Db.getSql("crm.customer.getRecord"), crmCustomer.getCustomerId());
        recordList.forEach(record -> {
            adminFileService.queryByBatchId(record.getStr("batch_id"), record);
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
     * 导出客户
     */
    public List<Record> exportCustomer(String customerIds) {
        String[] customerIdsArr = customerIds.split(",");
        return Db.find(Db.getSqlPara("crm.customer.excelExport", Kv.by("ids", customerIdsArr)));
    }

    /**
     * @author zxy
     * 客户保护规则设置
     */
    @Before(Tx.class)
    public R updateRulesSetting(Integer dealDay,Integer followupDay,Integer type) {
        Db.update("update 72crm_admin_config set value = ? where name = 'customerPoolSettingDealDays'",dealDay);
        Db.update("update 72crm_admin_config set value = ? where name = 'customerPoolSettingFollowupDays'",followupDay);
        Db.update("update 72crm_admin_config set status = ? where name = 'customerPoolSetting'",type);
        return R.ok();
    }

    /**
     * @author zxy
     * 获取客户保护规则设置
     */
    @Before(Tx.class)
    public R getRulesSetting() {
        String dealDay = Db.queryStr("select value from 72crm_admin_config where name = 'customerPoolSettingDealDays'");
        String followupDay = Db.queryStr("select value from 72crm_admin_config where name = 'customerPoolSettingFollowupDays'");
        Integer type = Db.queryInt("select status from 72crm_admin_config where name = 'customerPoolSetting'");
        if (dealDay == null || followupDay == null || type == null){
            if (dealDay == null){
                AdminConfig adminConfig = new AdminConfig();
                adminConfig.setName("customerPoolSettingDealDays");
                adminConfig.setValue("3");
                adminConfig.save();
                dealDay = "3";
            }
            if (followupDay == null){
                AdminConfig adminConfig = new AdminConfig();
                adminConfig.setName("customerPoolSettingFollowupDays");
                adminConfig.setValue("7");
                adminConfig.save();
                followupDay = "7";
            }
            if (type == null){
                AdminConfig adminConfig = new AdminConfig();
                adminConfig.setName("customerPoolSetting");
                adminConfig.setStatus(0);
                adminConfig.save();
                type = 0;
            }
        }
        AdminConfig config = AdminConfig.dao.findFirst("select status,value  from 72crm_admin_config where name = 'expiringContractDays' limit 1");
        if (config == null){
            config = new AdminConfig();
            config.setStatus(0);
            config.setName("expiringContractDays");
            config.setValue("3");
            config.setDescription("合同到期提醒");
            config.save();
        }
        return R.ok().put("data",Kv.by("dealDay",dealDay).set("followupDay",followupDay).set("customerConfig",type).set("contractConfig",config.getStatus()).set("contractDay",config.getValue()));
    }

    /**
     * 客户放入公海
     *
     * @author zxy
     */
    @Before(Tx.class)
    public R updateCustomerByIds(String ids) {
        crmRecordService.addPutIntoTheOpenSeaRecord(TagUtil.toSet(ids),CrmEnum.CUSTOMER_TYPE_KEY.getTypes());
        StringBuffer sq = new StringBuffer("select count(*) from 72crm_crm_customer where customer_id in ( ");
        sq.append(ids).append(") and is_lock = 1");
        Integer count = Db.queryInt(sq.toString());
        if (count > 0) {
            return R.error("选中的客户有被锁定的，不能放入公海！");
        }
        StringBuffer sql = new StringBuffer("UPDATE 72crm_crm_customer SET owner_user_id = null where customer_id in (");
        sql.append(ids).append(") and is_lock = 0");
        String[] idsArr = ids.split(",");
        for (String id:idsArr){
            CrmCustomer crmCustomer = CrmCustomer.dao.findById(Integer.valueOf(id));
            CrmOwnerRecord crmOwnerRecord = new CrmOwnerRecord();
            crmOwnerRecord.setTypeId(Integer.valueOf(id));
            crmOwnerRecord.setType(8);
            crmOwnerRecord.setPreOwnerUserId(crmCustomer.getOwnerUserId());
            crmOwnerRecord.setCreateTime(DateUtil.date());
            crmOwnerRecord.save();
        }
        return Db.update(sql.toString()) > 0 ? R.ok() : R.error();
    }

    /**
     * 领取或分配客户
     *
     * @author zxy
     */
    @Before(Tx.class)
    public R getCustomersByIds(String ids, Long userId) {
        crmRecordService.addDistributionRecord(ids, CrmEnum.CUSTOMER_TYPE_KEY.getTypes(), userId);
        if (userId == null) {
            userId = BaseUtil.getUser().getUserId();
        }
        String[] idsArr = ids.split(",");
        for (String id:idsArr){
            CrmOwnerRecord crmOwnerRecord = new CrmOwnerRecord();
            crmOwnerRecord.setTypeId(Integer.valueOf(id));
            crmOwnerRecord.setType(8);
            crmOwnerRecord.setPostOwnerUserId(userId.intValue());
            crmOwnerRecord.setCreateTime(DateUtil.date());
            crmOwnerRecord.save();
        }
        SqlPara sqlPara = Db.getSqlPara("crm.customer.getCustomersByIds",Kv.by("userId",userId).set("createTime",DateUtil.date()).set("ids",idsArr));
        return Db.update(sqlPara) > 0 ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 获取客户导入查重字段
     */
    public R getCheckingField(){
        return R.ok().put("data","客户名称");
    }

    /**
     * 导入客户
     * @author wyq
     */
    public R uploadExcel(UploadFile file, Integer repeatHandling, Integer ownerUserId) {
        ExcelReader reader = ExcelUtil.getReader(FileUtil.file(file.getUploadPath() + "\\" + file.getFileName()));
        AdminFieldService adminFieldService = new AdminFieldService();
        Kv kv = new Kv();
        int errNum = 0;
        try {
            List<List<Object>> read = reader.read();
            List<Object> list = read.get(1);
            List<Record> recordList = adminFieldService.customFieldList("2");
            recordList.removeIf(record -> "file".equals(record.getStr("formType")) || "checkbox".equals(record.getStr("formType"))|| "user".equals(record.getStr("formType"))|| "structure".equals(record.getStr("formType")));
            List<Record> fieldList = adminFieldService.queryAddField(2);
            fieldList.removeIf(record -> "file".equals(record.getStr("formType")) || "checkbox".equals(record.getStr("formType"))|| "user".equals(record.getStr("formType"))|| "structure".equals(record.getStr("formType")));
            fieldList.forEach(record -> {
                if (record.getInt("is_null") == 1){
                    record.set("name",record.getStr("name")+"(*)");
                }
                if ("map_address".equals(record.getStr("field_name"))){
                    record.set("name","详细地址");
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
                    List<Object> customerList = read.get(i);
                    if (customerList.size() < list.size()) {
                        for (int j = customerList.size() - 1; j < list.size(); j++) {
                            customerList.add(null);
                        }
                    }
                    String customerName = customerList.get(kv.getInt("customer_name")).toString();
                    Integer number = Db.queryInt("select count(*) from 72crm_crm_customer where customer_name = ?", customerName);
                    if (0 == number) {
                        object.fluentPut("entity", new JSONObject().fluentPut("customer_name", customerName)
                                .fluentPut("mobile",customerList.get(kv.getInt("mobile")))
                                .fluentPut("telephone", customerList.get(kv.getInt("telephone")))
                                .fluentPut("website", customerList.get(kv.getInt("website")))
                                .fluentPut("next_time", customerList.get(kv.getInt("next_time")))
                                .fluentPut("remark", customerList.get(kv.getInt("remark")))
                                .fluentPut("detail_address", customerList.get(kv.getInt("map_address")))
                                .fluentPut("deal_status",customerList.get(kv.getInt("deal_status")))
                                .fluentPut("owner_user_id", ownerUserId));
                    } else if (number > 0 && repeatHandling == 1) {
                        Record leads = Db.findFirst("select customer_id,batch_id from 72crm_crm_customer where customer_name = ?", customerName);
                        object.fluentPut("entity", new JSONObject().fluentPut("customer_id", leads.getInt("customer_id"))
                                .fluentPut("customer_name", customerName)
                                .fluentPut("mobile",customerList.get(kv.getInt("mobile")))
                                .fluentPut("telephone", customerList.get(kv.getInt("telephone")))
                                .fluentPut("website", customerList.get(kv.getInt("website")))
                                .fluentPut("next_time", customerList.get(kv.getInt("next_time")))
                                .fluentPut("remark", customerList.get(kv.getInt("remark")))
                                .fluentPut("detail_address", customerList.get(kv.getInt("map_address")))
                                .fluentPut("deal_status",customerList.get(kv.getInt("deal_status")))
                                .fluentPut("owner_user_id", ownerUserId)
                                .fluentPut("batch_id", leads.getStr("batch_id")));
                    } else if (number > 0 && repeatHandling == 2) {
                        continue;
                    }
                    JSONArray jsonArray = new JSONArray();
                    for (Record record : recordList) {
                        Integer columnsNum = kv.getInt(record.getStr("name"))!=null?kv.getInt(record.getStr("name")):kv.getInt(record.getStr("name")+"(*)");
                        record.set("value", customerList.get(columnsNum));
                        jsonArray.add(JSONObject.parseObject(record.toJson()));
                    }
                    object.fluentPut("field", jsonArray);
                    addOrUpdate(object,null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
