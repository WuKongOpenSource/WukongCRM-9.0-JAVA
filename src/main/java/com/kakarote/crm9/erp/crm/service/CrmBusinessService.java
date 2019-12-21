package com.kakarote.crm9.erp.crm.service;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Aop;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.erp.admin.entity.AdminRecord;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminFieldService;
import com.kakarote.crm9.erp.admin.service.AdminFileService;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.entity.*;
import com.kakarote.crm9.erp.oa.common.OaEnum;
import com.kakarote.crm9.erp.oa.entity.OaEvent;
import com.kakarote.crm9.erp.oa.entity.OaEventRelation;
import com.kakarote.crm9.erp.oa.service.OaActionRecordService;
import com.kakarote.crm9.utils.AuthUtil;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.FieldUtil;
import com.kakarote.crm9.utils.R;

import java.util.*;
import java.util.stream.Collectors;

public class CrmBusinessService {
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
     * 新增或更新商机
     */
    @Before(Tx.class)
    public R addOrUpdate(JSONObject jsonObject) {
        CrmBusiness crmBusiness = jsonObject.getObject("entity", CrmBusiness.class);
        JSONArray jsonArray = jsonObject.getJSONArray("product");
        List<CrmBusinessProduct> businessProductList = jsonArray.toJavaList(CrmBusinessProduct.class);
        Db.delete(Db.getSql("crm.business.clearBusinessProduct"), crmBusiness.getBusinessId());
        String batchId = StrUtil.isNotEmpty(crmBusiness.getBatchId()) ? crmBusiness.getBatchId() : IdUtil.simpleUUID();
        crmRecordService.updateRecord(jsonObject.getJSONArray("field"), batchId);
        adminFieldService.save(jsonObject.getJSONArray("field"), batchId);
        boolean saveOrUpdate;
        if (crmBusiness.getBusinessId() != null) {
            if (!BaseUtil.getUserId().equals(BaseConstant.SUPER_ADMIN_USER_ID) && !BaseUtil.getUser().getRoles().contains(BaseConstant.SUPER_ADMIN_ROLE_ID) && Db.queryInt(Db.getSql("crm.business.queryIsRoUser"), BaseUtil.getUserId(), crmBusiness.getBusinessId()) > 0) {
                return R.error("没有权限");
            }
            crmBusiness.setUpdateTime(DateUtil.date());
            crmRecordService.updateRecord(new CrmBusiness().dao().findById(crmBusiness.getBusinessId()), crmBusiness, CrmEnum.CRM_BUSINESS);
            CrmBusiness oldBusiness = CrmBusiness.dao.findById(crmBusiness.getBusinessId());
            if (!oldBusiness.getStatusId().equals(crmBusiness.getStatusId())) {
                CrmBusinessChange change = new CrmBusinessChange();
                change.setBusinessId(crmBusiness.getBusinessId());
                change.setStatusId(crmBusiness.getStatusId());
                change.setCreateTime(DateUtil.date());
                change.setCreateUserId(BaseUtil.getUserId());
                change.save();
            }
            saveOrUpdate = crmBusiness.update();
        } else {
            crmBusiness.setCreateTime(DateUtil.date());
            crmBusiness.setUpdateTime(DateUtil.date());
            crmBusiness.setCreateUserId(BaseUtil.getUser().getUserId());
            crmBusiness.setOwnerUserId(BaseUtil.getUser().getUserId());
            crmBusiness.setBatchId(batchId);
            crmBusiness.setRwUserId(",");
            crmBusiness.setRoUserId(",");
            saveOrUpdate = crmBusiness.save();
            if (jsonObject.getInteger("contactsId") != null) {
                CrmContactsBusiness crmContactsBusiness = new CrmContactsBusiness();
                crmContactsBusiness.setBusinessId(crmBusiness.getBusinessId());
                crmContactsBusiness.setContactsId(jsonObject.getInteger("contactsId"));
                crmContactsBusiness.save();
            }
            crmRecordService.addRecord(crmBusiness.getBusinessId(), CrmEnum.CRM_BUSINESS);
        }
        if (businessProductList != null) {
            for (CrmBusinessProduct crmBusinessProduct : businessProductList) {
                crmBusinessProduct.setBusinessId(crmBusiness.getBusinessId());
                crmBusinessProduct.save();
            }
        }
        return saveOrUpdate ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 根据商机id查询
     */
    public Record queryById(Integer businessId) {
        Record crmBusiness = Db.findFirst(Db.getSql("crm.business.queryById"), businessId);
        List<Record> recordList = Db.find("select name,value from `72crm_admin_fieldv` where batch_id = ?", crmBusiness.getStr("batch_id"));
        recordList.forEach(field -> crmBusiness.set(field.getStr("name"), field.getStr("value")));
        return crmBusiness;
    }

    /**
     * @author wyq
     * 基本信息
     */
    public List<Record> information(Integer busienssId) {
        Record record = queryById(busienssId);
        List<String> keyList = Arrays.asList("business_name", "deal_date", "money", "remark");
        List<Record> recordList = adminFieldService.queryInformation(CrmEnum.CRM_BUSINESS, record, keyList);
        recordList.add(new Record().set("name", "商机阶段").set("value", new Record().set("statusId", record.getInt("status_id")).set("statusName", record.getStr("statusName"))).set("formType", "statusName").set("field_type", 1));
        recordList.add(new Record().set("name", "商机状态组").set("value", new Record().set("typeId", record.getInt("type_id")).set("typeName", record.getStr("typeName"))).set("formType", "typeName").set("field_type", 1));
        recordList.add(new Record().set("name", "客户名称").set("value", new Record().set("customerId", record.getInt("customer_id")).set("customerName", record.getStr("customer_name"))).set("formType", "customer").set("field_type", 1));
        return recordList.stream().sorted(Comparator.comparingInt(r -> -r.getInt("field_type"))).map(r -> r.remove("field_type", "field_name", "setting", "type")).collect(Collectors.toList());
    }

    /**
     * @author wyq
     * 根据商机名称查询
     */
    public Record queryByName(String name) {
        return Db.findFirst(Db.getSql("crm.business.queryByName"), name);
    }

    /**
     * @author wyq
     * 根据商机id查询产品
     */
    public R queryProduct(BasePageRequest<CrmBusiness> basePageRequest) {
        Integer businessId = basePageRequest.getData().getBusinessId();
        Integer pageType = basePageRequest.getPageType();
        Record record = Db.findFirst(Db.getSql("crm.product.querySubtotalByBusinessId"), businessId);
        if (record.getStr("money") == null) {
            record.set("money", 0);
        }
        if (0 == pageType) {
            record.set("list", Db.find(Db.getSql("crm.business.queryProduct"), businessId));
        } else {
            record.set("list", Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), new SqlPara().setSql(Db.getSql("crm.business.queryProduct")).addPara(businessId)));
        }
        return R.ok().put("data", record);
    }

    /**
     * @author wyq
     * 根据商机id查询合同
     */
    public R queryContract(BasePageRequest<CrmBusiness> basePageRequest) {
        Integer businessId = basePageRequest.getData().getBusinessId();
        Integer pageType = basePageRequest.getPageType();
        if (0 == pageType) {
            return R.ok().put("data", Db.find(Db.getSql("crm.business.queryContract"), businessId));
        } else {
            return R.ok().put("data", Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), new SqlPara().setSql(Db.getSql("crm.business.queryContract")).addPara(businessId)));
        }
    }

    /**
     * @author wyq
     * 根据商机id查询联系人
     */
    public R queryContacts(BasePageRequest<CrmBusiness> basePageRequest) {
        Integer businessId = basePageRequest.getData().getBusinessId();
        Integer pageType = basePageRequest.getPageType();
        if (0 == pageType) {
            return R.ok().put("data", Db.find(Db.getSql("crm.business.queryContacts"), businessId));
        } else {
            return R.ok().put("data", Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), new SqlPara().setSql(Db.getSql("crm.business.queryContacts")).addPara(businessId)));
        }
    }

    /**
     * @author wyq
     * 商机关联联系人
     */
    public R relateContacts(Integer businessId, String contactsIds) {
        String[] contactsIdsArr = contactsIds.split(",");
        Db.delete("delete from 72crm_crm_contacts_business where business_id = ?", businessId);
        List<CrmContactsBusiness> crmContactsBusinessList = new ArrayList<>();
        for (String id : contactsIdsArr) {
            CrmContactsBusiness crmContactsBusiness = new CrmContactsBusiness();
            crmContactsBusiness.setContactsId(Integer.valueOf(id));
            crmContactsBusiness.setBusinessId(businessId);
            crmContactsBusinessList.add(crmContactsBusiness);
        }
        Db.batchSave(crmContactsBusinessList, 100);
        return R.ok();
    }

    /**
     * @author wyq
     * 商机解除关联联系人
     */
    public R unrelateContacts(Integer businessId, String contactsIds) {
        String[] idsArr = contactsIds.split(",");
        SqlPara sqlPara = Db.getSqlPara("crm.business.unrelateContacts", Kv.by("businessId", businessId).set("ids", idsArr));
        Db.delete(sqlPara.getSql(), sqlPara.getPara());
        return R.ok();
    }

    /**
     * @author wyq
     * 根据id删除商机
     */
    public R deleteByIds(String businessIds) {
        String[] idsArr = businessIds.split(",");
        Integer number = Db.queryInt(Db.getSql("crm.business.queryContractNumber"), businessIds);
        if (number > 0) {
            return R.error("该条数据与其他数据有必要关联，请勿删除");
        }
        List<Record> idsList = new ArrayList<>();
        for (String id : idsArr) {
            Record record = new Record();
            idsList.add(record.set("business_id", Integer.valueOf(id)));
        }
        List<Record> batchIdList = Db.find(Db.getSqlPara("crm.business.queryBatchIdByIds", Kv.by("ids", idsArr)));
        return Db.tx(() -> {
            Db.batch(Db.getSql("crm.business.deleteByIds"), "business_id", idsList, 100);
            Db.batch("delete from 72crm_admin_fieldv where batch_id = ?", "batch_id", batchIdList, 100);
            return true;
        }) ? R.ok() : R.error();
    }

    /**
     * 根据客户id变更负责人
     *
     * @author wyq
     */
    public R updateOwnerUserId(CrmCustomer crmCustomer) {
        CrmBusiness crmBusiness = new CrmBusiness();
        crmBusiness.setNewOwnerUserId(crmCustomer.getNewOwnerUserId());
        crmBusiness.setTransferType(crmCustomer.getTransferType());
        crmBusiness.setPower(crmCustomer.getPower());
        String businessIds = Db.queryStr("select GROUP_CONCAT(business_id) from 72crm_crm_business where customer_id in (" + crmCustomer.getCustomerIds() + ")");
        if (StrUtil.isEmpty(businessIds)) {
            return R.ok();
        }
        crmBusiness.setBusinessIds(businessIds);
        return transfer(crmBusiness);
    }

    /**
     * @author wyq
     * 根据商机id变更负责人
     */
    public R transfer(CrmBusiness crmBusiness) {
        String[] businessIdsArr = crmBusiness.getBusinessIds().split(",");
        for (String businessId : businessIdsArr) {
            if (!BaseUtil.getUserId().equals(BaseConstant.SUPER_ADMIN_USER_ID) && !AuthUtil.isRwAuth(Integer.parseInt(businessId), "business")) {
                return R.error("无权限转移");
            }
            String memberId = "," + crmBusiness.getNewOwnerUserId() + ",";
            Db.update(Db.getSql("crm.business.deleteMember"), memberId, memberId, Integer.valueOf(businessId));
            CrmBusiness oldBusiness = CrmBusiness.dao.findById(Integer.valueOf(businessId));
            if (2 == crmBusiness.getTransferType()) {
                if (1 == crmBusiness.getPower()) {
                    crmBusiness.setRoUserId(oldBusiness.getRoUserId() + oldBusiness.getOwnerUserId() + ",");
                }
                if (2 == crmBusiness.getPower()) {
                    crmBusiness.setRwUserId(oldBusiness.getRwUserId() + oldBusiness.getOwnerUserId() + ",");
                }
            }
            crmBusiness.setBusinessId(Integer.valueOf(businessId));
            crmBusiness.setOwnerUserId(crmBusiness.getNewOwnerUserId());
            crmBusiness.update();
            crmRecordService.addConversionRecord(Integer.valueOf(businessId), CrmEnum.CRM_BUSINESS, crmBusiness.getNewOwnerUserId());
        }
        return R.ok();
    }

    /**
     * @author wyq
     * 查询团队成员
     */
    public List<Record> getMembers(Integer businessId) {
        CrmBusiness crmBusiness = CrmBusiness.dao.findById(businessId);
        List<Record> recordList = new ArrayList<>();
        if (crmBusiness.getOwnerUserId() != null) {
            Record ownerUser = Db.findFirst(Db.getSql("crm.customer.getMembers"), crmBusiness.getOwnerUserId());
            recordList.add(ownerUser.set("power", "负责人权限").set("groupRole", "负责人"));
        }
        String roUserId = crmBusiness.getRoUserId();
        String rwUserId = crmBusiness.getRwUserId();
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
    @Before(Tx.class)
    public R addMember(CrmBusiness crmBusiness) {
        String[] businessIdsArr = crmBusiness.getIds().split(",");
        String[] memberArr = crmBusiness.getMemberIds().split(",");
        StringBuffer stringBuffer = new StringBuffer();
        for (String id : businessIdsArr) {
            if (StrUtil.isNotEmpty(id)) {
                Long userId = BaseUtil.getUserId();
                if (!userId.equals(BaseConstant.SUPER_ADMIN_USER_ID) && !BaseUtil.getUser().getRoles().contains(BaseConstant.SUPER_ADMIN_ROLE_ID) && Db.template("crm.business.queryIsAuth", Kv.by("userIds", Aop.get(AdminUserService.class).queryUserByAuth(userId, "business")).set("businessId", id)).queryInt() == 0) {
                    return R.error("没有权限");
                }
                Long ownerUserId = CrmBusiness.dao.findById(Integer.valueOf(id)).getOwnerUserId();
                for (String memberId : memberArr) {
                    if (ownerUserId.equals(Long.valueOf(memberId))) {
                        return R.error("负责人不能重复选为团队成员");
                    }
                    Db.update(Db.getSql("crm.business.deleteMember"), "," + memberId + ",", "," + memberId + ",", Integer.valueOf(id));
                }
                if (1 == crmBusiness.getPower()) {
                    stringBuffer.setLength(0);
                    String roUserId = stringBuffer.append(CrmBusiness.dao.findById(Integer.valueOf(id)).getRoUserId()).append(crmBusiness.getMemberIds()).append(",").toString();
                    Db.update("update 72crm_crm_business set ro_user_id = ? where business_id = ?", roUserId, Integer.valueOf(id));
                }
                if (2 == crmBusiness.getPower()) {
                    stringBuffer.setLength(0);
                    String rwUserId = stringBuffer.append(CrmBusiness.dao.findById(Integer.valueOf(id)).getRwUserId()).append(crmBusiness.getMemberIds()).append(",").toString();
                    Db.update("update 72crm_crm_business set rw_user_id = ? where business_id = ?", rwUserId, Integer.valueOf(id));
                }
            }

        }
        return R.ok();
    }

    /**
     * @author wyq
     * 删除团队成员
     */
    public R deleteMembers(CrmBusiness crmBusiness) {
        String[] businessIdsArr = crmBusiness.getIds().split(",");
        String[] memberArr = crmBusiness.getMemberIds().split(",");
        for (String id : businessIdsArr) {
            Long userId = BaseUtil.getUserId();
            if (!userId.equals(BaseConstant.SUPER_ADMIN_USER_ID) && !BaseUtil.getUser().getRoles().contains(BaseConstant.SUPER_ADMIN_ROLE_ID) && Db.template("crm.business.queryIsAuth", Kv.by("userIds", Aop.get(AdminUserService.class).queryUserByAuth(userId, "business")).set("businessId", id)).queryInt() == 0) {
                return R.error("没有权限");
            }
        }
        return Db.tx(() -> {
            for (String id : businessIdsArr) {
                for (String memberId : memberArr) {
                    Db.update(Db.getSql("crm.business.deleteMember"), "," + memberId + ",", "," + memberId + ",", Integer.valueOf(id));
                }
            }
            return true;
        }) ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 商机状态组展示
     */
    public Record queryBusinessStatus(Integer businessId) {
        List<Record> statusList = Db.find(Db.getSql("crm.business.queryBusinessStatusList"), businessId);
        Record business = Db.findFirst(Db.getSql("crm.business.queryBusinessStatus"), businessId);
        return business.set("statusList", statusList);
    }

    /**
     * @author wyq
     * 商机状态组推进
     */
    @Before(Tx.class)
    public R boostBusinessStatus(CrmBusiness crmBusiness) {
        if (crmBusiness.getStatusId() != null) {
            CrmBusinessChange change = new CrmBusinessChange();
            change.setBusinessId(crmBusiness.getBusinessId());
            change.setStatusId(crmBusiness.getStatusId());
            change.setCreateTime(DateUtil.date());
            change.setCreateUserId(BaseUtil.getUserId());
            change.save();
        }
        return crmBusiness.update() ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 查询编辑字段
     */
    public List<Record> queryField(Integer businessId) {
        Record business = queryById(businessId);
        List<Record> customerList = new ArrayList<>();
        Record customer = new Record();
        customerList.add(customer.set("customer_id", business.getInt("customer_id")).set("customer_name", business.getStr("customer_name")));
        business.set("customer_id", customerList);
        List<Record> fieldList = adminFieldService.queryUpdateField(CrmEnum.CRM_BUSINESS.getType(), business);
        fieldList.add(new Record().set("field_name", "type_id").set("name", "商机状态组").set("value", business.getInt("type_id")).set("form_type", "business_type").set("setting", new String[0]).set("is_null", 1).set("field_type", 1));
        fieldList.add(new Record().set("field_name", "status_id").set("name", "商机阶段").set("value", business.getInt("status_id")).set("form_type", "business_status").set("setting", new String[0]).set("is_null", 1).set("field_type", 1));
        List<Record> productList = Db.find(Db.getSql("crm.business.queryBusinessProduct"), businessId);
        Kv kv = Kv.by("discount_rate", business.getBigDecimal("discount_rate")).set("product", productList).set("total_price", business.getStr("total_price"));
        fieldList.add(new Record().set("field_name", "product").set("name", "产品").set("value", kv).set("form_type", "product").set("setting", new String[]{}).set("is_null", 0).set("field_type", 1));
        return fieldList;
    }

    /**
     * @author wyq
     * 查询商机状态组及商机状态
     */
    public List<Record> queryBusinessStatusOptions(String type) {
        List<Record> businessTypeList = Db.find("select * from 72crm_crm_business_type where status = 1");
        for (Record record : businessTypeList) {
            Integer typeId = record.getInt("type_id");
            List<Record> businessStatusList = Db.find("select * from 72crm_crm_business_status where type_id = ?", typeId);
            if ("condition".equals(type)) {
                Record win = new Record();
                win.set("name", "赢单").set("typeId", typeId).set("statusId", "win");
                businessStatusList.add(win);
                Record lose = new Record();
                lose.set("name", "输单").set("typeId", typeId).set("statusId", "lose");
                businessStatusList.add(lose);
                Record invalid = new Record();
                invalid.set("name", "无效").set("typeId", typeId).set("statusId", "invalid");
                businessStatusList.add(invalid);
            }
            record.set("statusList", businessStatusList);
        }
        return businessTypeList;
    }

    /**
     * @author wyq
     * 添加跟进记录
     */
    @Before(Tx.class)
    public R addRecord(AdminRecord adminRecord) {
        adminRecord.setTypes("crm_business");
        adminRecord.setCreateTime(DateUtil.date());
        adminRecord.setCreateUserId(BaseUtil.getUser().getUserId());
        if (StrUtil.isEmpty(adminRecord.getCategory())) {
            return R.error("跟进类型不能为空");
        }
        if (1 == adminRecord.getIsEvent()) {
            OaEvent oaEvent = new OaEvent();
            oaEvent.setTitle(adminRecord.getContent());
            oaEvent.setStartTime(adminRecord.getNextTime());
            oaEvent.setEndTime(DateUtil.offsetDay(adminRecord.getNextTime(), 1));
            oaEvent.setCreateTime(DateUtil.date());
            oaEvent.setCreateUserId(BaseUtil.getUser().getUserId());
            oaEvent.save();
            AdminUser user = BaseUtil.getUser();
            oaActionRecordService.addRecord(oaEvent.getEventId(), OaEnum.EVENT_TYPE_KEY.getTypes(), 1, oaActionRecordService.getJoinIds(user.getUserId(), oaEvent.getOwnerUserIds()), oaActionRecordService.getJoinIds(Long.valueOf(user.getDeptId()), ""));
            OaEventRelation oaEventRelation = new OaEventRelation();
            oaEventRelation.setEventId(oaEvent.getEventId());
            oaEventRelation.setBusinessIds("," + adminRecord.getTypesId().toString() + ",");
            oaEventRelation.setCreateTime(DateUtil.date());
            oaEventRelation.save();
        }
        return adminRecord.save() ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 查看跟进记录
     */
    public List<Record> getRecord(BasePageRequest<CrmBusiness> basePageRequest) {
        CrmBusiness crmBusiness = basePageRequest.getData();
        List<Record> recordList = Db.find(Db.getSql("crm.business.getRecord"), crmBusiness.getBusinessId(), crmBusiness.getBusinessId());
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
}
