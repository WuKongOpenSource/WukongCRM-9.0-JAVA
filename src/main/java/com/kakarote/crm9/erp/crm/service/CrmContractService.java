package com.kakarote.crm9.erp.crm.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.entity.AdminRecord;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminExamineRecordService;
import com.kakarote.crm9.erp.admin.service.AdminFieldService;
import com.kakarote.crm9.erp.admin.service.AdminFileService;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.entity.*;
import com.kakarote.crm9.erp.oa.common.OaEnum;
import com.kakarote.crm9.erp.oa.entity.OaEvent;
import com.kakarote.crm9.erp.oa.entity.OaEventRelation;
import com.kakarote.crm9.erp.oa.service.OaActionRecordService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.FieldUtil;
import com.kakarote.crm9.utils.R;

import java.util.*;

public class CrmContractService {
    @Inject
    private AdminFieldService adminFieldService;

    @Inject
    private FieldUtil fieldUtil;

    @Inject
    private CrmRecordService crmRecordService;

    @Inject
    private AdminFileService adminFileService;

    @Inject
    private AdminExamineRecordService examineRecordService;

    @Inject
    private OaActionRecordService oaActionRecordService;

    /**
     * 分页条件查询合同
     */
    public Page<Record> queryPage(BasePageRequest<CrmContract> basePageRequest) {
        return Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("crm.contract.getProductPageList"));
    }

    /**
     * 根据id查询合同
     */
    public R queryById(Integer id) {
        Record record = Db.findFirst(Db.getSql("crm.contract.queryByContractId"), id);
        return R.ok().put("data", record);
    }

    /**
     * 根据id查询合同基本信息
     */
    public List<Record> information(Integer id) {
        Record record = Db.findFirst(Db.getSql("crm.contract.queryByContractId"), id);
        if (record == null) {
            return null;
        }
        List<Record> fieldList = new ArrayList<>();
        FieldUtil field = new FieldUtil(fieldList);
        field.set("合同编号", record.getStr("num"))
                .set("合同名称", record.getStr("name"))
                .set("客户名称", record.getStr("customer_name"))
                .set("商机名称", record.getStr("business_name"))
                .set("下单时间", DateUtil.formatDate(record.getDate("order_date")))
                .set("合同金额", record.getStr("money"))
                .set("合同开始时间", DateUtil.formatDate(record.getDate("start_time")))
                .set("合同结束时间", DateUtil.formatDate(record.getDate("end_time")))
                .set("客户签约人", record.getStr("contacts_name"))
                .set("公司签约人", record.getStr("company_user_name")).
                set("备注", record.getStr("remark"));
        List<Record> fields = adminFieldService.list("6");
        for (Record r:fields){
            field.set(r.getStr("name"),record.getStr(r.getStr("name")));
        }
        return fieldList;
    }

    /**
     * 根据id删除合同
     */
    public R deleteByIds(String contractIds) {

        String[] idsArr = contractIds.split(",");
        List<CrmReceivables> list = CrmReceivables.dao.find(Db.getSqlPara("crm.receivables.queryReceivablesByContractIds", Kv.by("contractIds", idsArr)));
        if (list.size() > 0) {
            return R.error("该数据已被其他模块引用，不能被删除！");
        }
        List<Record> idsList = new ArrayList<>();
        for (String id : idsArr) {
            Record record = new Record();
            idsList.add(record.set("contract_id", Integer.valueOf(id)));
        }
        return Db.tx(() -> {
            Db.batch(Db.getSql("crm.contract.deleteByIds"), "contract_id", idsList, 100);
            return true;
        }) ? R.ok() : R.error();
    }

    /**
     * 添加或修改
     */
    @Before(Tx.class)
    public R saveAndUpdate(JSONObject jsonObject) {
        CrmContract crmContract = jsonObject.getObject("entity", CrmContract.class);
        String batchId = StrUtil.isNotEmpty(crmContract.getBatchId()) ? crmContract.getBatchId() : IdUtil.simpleUUID();

        adminFieldService.save(jsonObject.getJSONArray("field"), batchId);
        boolean flag;
        if (crmContract.getContractId() == null) {
            Integer contract =Db.queryInt(Db.getSql("crm.contract.queryByNum"),crmContract.getNum());
            if (contract != 0){
                return R.error("合同编号已存在，请校对后再添加！");
            }
            crmContract.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
            crmContract.setBatchId(batchId);
            crmContract.setCreateTime(DateUtil.date());
            crmContract.setUpdateTime(DateUtil.date());
            crmContract.setRoUserId(",");
            crmContract.setRwUserId(",");
            crmContract.setCheckStatus(0);
            crmContract.setOwnerUserId(BaseUtil.getUser().getUserId().intValue());


            Map<String, Integer> map = examineRecordService.saveExamineRecord(1, jsonObject.getLong("checkUserId"), crmContract.getOwnerUserId(), null);
            if (map.get("status") == 0) {
                return R.error("没有启动的审核步骤，不能添加！");
            } else {
                crmContract.setExamineRecordId(map.get("id"));
            }
            flag = crmContract.save();
        } else {
            CrmContract contract = CrmContract.dao.findById(crmContract.getContractId());
            if (contract.getCheckStatus() != 4 && contract.getCheckStatus() != 3) {
                return R.error("不能编辑，请先撤回再编辑！");
            }
            Map<String, Integer> map = examineRecordService.saveExamineRecord(1, jsonObject.getLong("checkUserId"), contract.getOwnerUserId(), contract.getExamineRecordId());
            if (map.get("status") == 0) {
                return R.error("没有启动的审核步骤，不能添加！");
            } else {
                crmContract.setExamineRecordId(map.get("id"));
            }
            crmContract.setCheckStatus(0);
            crmContract.setUpdateTime(DateUtil.date());
            flag = crmContract.update();
        }
        JSONArray jsonArray = jsonObject.getJSONArray("product");
        if (jsonArray != null) {
            List<CrmContractProduct> contractProductList = jsonArray.toJavaList(CrmContractProduct.class);
            //删除之前的合同产品关联表
            Db.delete(Db.getSql("crm.contract.deleteByContractId"), crmContract.getContractId());
            if (crmContract.getBusinessId() != null){
                Db.delete("delete from 72crm_crm_business_product where business_id = ?",crmContract.getBusinessId());
            }
            if (contractProductList != null) {
                for (CrmContractProduct crmContractProduct : contractProductList) {
                    crmContractProduct.setContractId(crmContract.getContractId());
                    crmContractProduct.save();
                    if (crmContract.getBusinessId() != null){
                        CrmBusinessProduct crmBusinessProduct = new CrmBusinessProduct()._setOrPut(crmContractProduct.toRecord().getColumns());
                        crmBusinessProduct.setRId(null);
                        crmBusinessProduct.setBusinessId(crmContract.getBusinessId());
                        crmBusinessProduct.save();
                    }
                }
            }

        }

        return R.isSuccess(flag);

    }

    /**
     * 根据条件查询合同
     */
    public List<CrmContract> queryList(CrmContract crmContract) {
        StringBuffer sql = new StringBuffer("select * from 72crm_crm_contract where 1 = 1 ");
        if (crmContract.getCustomerId() != null) {
            sql.append(" and  customer_id = ").append(crmContract.getCustomerId());
        }
        if (crmContract.getBusinessId() != null) {
            sql.append(" and  business_id = ").append(crmContract.getBusinessId());
        }
        return CrmContract.dao.find(sql.toString());
    }

    /**
     * 根据条件查询合同
     */
    public List<Record> queryListByType(String type, Integer id) {
        StringBuffer sql = new StringBuffer("select * from contractview where ");
        if (type.equals(CrmEnum.CUSTOMER_TYPE_KEY.getTypes())) {
            sql.append("  customer_id = ? ");
        }
        if (type.equals(CrmEnum.BUSINESS_TYPE_KEY.getTypes())) {
            sql.append("  business_id = ? ");
        }

        return Db.find(sql.toString(), id);
    }

    /**
     * 根据合同批次查询产品
     *
     * @param batchId 合同批次
     * @return
     */
    public List<Record> queryProductById(String batchId) {
        return Db.find(Db.getSql("crm.contract.queryProductById"), batchId);
    }

    /**
     * 根据合同id查询回款
     *
     * @param id
     * @author HJP
     */
    public List<CrmReceivables> queryReceivablesById(Integer id) {
        return CrmReceivables.dao.find(Db.getSql("crm.receivables.queryReceivablesByContractId"), id);
    }

    /**
     * 根据合同id查询回款计划
     *
     * @param id
     * @author HJP
     */
    public List<CrmReceivablesPlan> queryReceivablesPlanById(Integer id) {
        return CrmReceivablesPlan.dao.find(Db.getSql("crm.receivablesplan.queryReceivablesPlanById"), id);
    }

    /**
     * 根据客户id变更负责人
     *
     * @author wyq
     */
    public R updateOwnerUserId(CrmCustomer crmCustomer) {
        CrmContract crmContract = new CrmContract();
        crmContract.setNewOwnerUserId(crmCustomer.getNewOwnerUserId());
        crmContract.setTransferType(crmCustomer.getTransferType());
        crmContract.setPower(crmCustomer.getPower());
        String[] customerIdsArr = crmCustomer.getCustomerIds().split(",");
        StringBuffer stringBuffer = new StringBuffer();
        for (String customerId : customerIdsArr) {
            stringBuffer.append(",").append(CrmContract.dao.findFirst("select contract_id from 72crm_crm_contract where customer_id = ?", Integer.valueOf(customerId)).getContractId());
        }
        crmContract.setContractIds(stringBuffer.deleteCharAt(0).toString());
        return transfer(crmContract);
    }

    /**
     * @author wyq
     * 根据合同id变更负责人
     */
    public R transfer(CrmContract crmContract) {
        String[] contractIdsArr = crmContract.getContractIds().split(",");
        return Db.tx(() -> {
            for (String contractId : contractIdsArr) {
                String memberId = "," + crmContract.getNewOwnerUserId() + ",";
                Db.update(Db.getSql("crm.contract.deleteMember"), memberId, memberId, Integer.valueOf(contractId));
                CrmContract oldContract = CrmContract.dao.findById(Integer.valueOf(contractId));
                if (2 == crmContract.getTransferType()) {
                    if (1 == crmContract.getPower()) {
                        crmContract.setRoUserId(oldContract.getRoUserId() + oldContract.getOwnerUserId() + ",");
                    }
                    if (2 == crmContract.getPower()) {
                        crmContract.setRwUserId(oldContract.getRwUserId() + oldContract.getOwnerUserId() + ",");
                    }
                }
                crmContract.setContractId(Integer.valueOf(contractId));
                crmContract.setOwnerUserId(crmContract.getNewOwnerUserId());
                crmContract.update();
                crmRecordService.addConversionRecord(Integer.valueOf(contractId), CrmEnum.CONTRACT_TYPE_KEY.getTypes(), crmContract.getNewOwnerUserId());
            }
            return true;
        }) ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 查询团队成员
     */
    public List<Record> getMembers(Integer contractId) {
        CrmContract crmContract = CrmContract.dao.findById(contractId);
        List<Record> recordList = new ArrayList<>();
        if (null != crmContract.getOwnerUserId()) {
            Record ownerUser = Db.findFirst(Db.getSql("crm.customer.getMembers"), crmContract.getOwnerUserId());
            recordList.add(ownerUser.set("power", "负责人权限").set("groupRole", "负责人"));
        }
        String roUserId = crmContract.getRoUserId();
        String rwUserId = crmContract.getRwUserId();
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
    public R addMember(CrmContract crmContract) {
        String[] contractIdsArr = crmContract.getIds().split(",");
        String[] memberArr = crmContract.getMemberIds().split(",");
        StringBuilder stringBuilder = new StringBuilder();
        for (String id : contractIdsArr) {
            Integer ownerUserId = CrmContract.dao.findById(Integer.valueOf(id)).getOwnerUserId();
            for (String memberId : memberArr) {
                if (ownerUserId.equals(Integer.valueOf(memberId))) {
                    return R.error("负责人不能重复选为团队成员");
                }
                Db.update(Db.getSql("crm.contract.deleteMember"), "," + memberId + ",", "," + memberId + ",", Integer.valueOf(id));
            }
            if (1 == crmContract.getPower()) {
                stringBuilder.setLength(0);
                String roUserId = stringBuilder.append(CrmContract.dao.findById(Integer.valueOf(id)).getRoUserId()).append(crmContract.getMemberIds()).append(",").toString();
                Db.update("update 72crm_crm_contract set ro_user_id = ? where contract_id = ?", roUserId, Integer.valueOf(id));
            }
            if (2 == crmContract.getPower()) {
                stringBuilder.setLength(0);
                String rwUserId = stringBuilder.append(CrmContract.dao.findById(Integer.valueOf(id)).getRwUserId()).append(crmContract.getMemberIds()).append(",").toString();
                Db.update("update 72crm_crm_contract set rw_user_id = ? where contract_id = ?", rwUserId, Integer.valueOf(id));
            }
        }
        return R.ok();
    }

    /**
     * @author wyq
     * 删除团队成员
     */
    public R deleteMembers(CrmContract crmContract) {
        String[] contractIdsArr = crmContract.getIds().split(",");
        String[] memberArr = crmContract.getMemberIds().split(",");
        return Db.tx(() -> {
            for (String id : contractIdsArr) {
                for (String memberId : memberArr) {
                    Db.update(Db.getSql("crm.contract.deleteMember"), "," + memberId + ",", "," + memberId + ",", Integer.valueOf(id));
                }
            }
            return true;
        }) ? R.ok() : R.error();
    }


    /**
     * @author zxy
     * 查询合同自定义字段（添加）
     */
    public List<Record> queryField() {
        List<Record> fieldList = new ArrayList<>();
        String[] settingArr = new String[]{};
        fieldUtil.getFixedField(fieldList, "num", "合同编号", "", "number", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "name", "合同名称", "", "text", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "customerId", "客户名称", settingArr, "customer", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "businessId", "商机名称", settingArr, "business", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "orderDate", "下单时间", "", "date", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "money", "合同金额", "", "floatnumber", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "startTime", "合同开始时间", "", "date", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "endTime", "合同结束时间", "", "date", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "contactsId", "客户签约人", settingArr, "contacts", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "companyUserId", "公司签约人", settingArr, "user", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "remark", "备注", "", "textarea", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "product", "产品", Kv.by("discount_rate", "").set("product", new ArrayList<>()).set("total_price", ""), "product", settingArr, 0);
        fieldList.addAll(adminFieldService.list("6"));
        return fieldList;
    }

    /**
     * @author zxy
     * 查询合同自定义字段（编辑）
     */
    public List<Record> queryField(Integer contractId) {
        List<Record> fieldList = new ArrayList<>();
        Record record = Db.findFirst("select * from contractview where contract_id = ?", contractId);
        String[] settingArr = new String[]{};
        fieldUtil.getFixedField(fieldList, "num", "合同编号", record.getStr("num"), "number", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "name", "合同名称", record.getStr("name"), "text", settingArr, 1);
        List<Record> customerList = new ArrayList<>();
        Record customer = new Record();
        customerList.add(customer.set("customerId", record.getInt("customer_id")).set("customerName", record.getStr("customer_name")));
        fieldUtil.getFixedField(fieldList, "customerId", "客户名称", customerList, "customer", settingArr, 1);
        customerList = new ArrayList<>();
        if (record.getStr("business_id") != null && record.getInt("business_id") != 0) {
            customer = new Record();
            customerList.add(customer.set("businessId", record.getInt("business_id")).set("businessName", record.getStr("business_name")));
        }

        fieldUtil.getFixedField(fieldList, "businessId", "商机名称", customerList, "business", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "orderDate", "下单时间", DateUtil.formatDateTime(record.get("order_date")), "date", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "money", "合同金额", record.getStr("money"), "floatnumber", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "startTime", "合同开始时间", DateUtil.formatDateTime(record.get("start_time")), "date", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "endTime", "合同结束时间", DateUtil.formatDateTime(record.get("end_time")), "date", settingArr, 0);
        customerList = new ArrayList<>();
        if (record.getStr("contacts_id") != null && record.getInt("contacts_id") != 0) {
            customer = new Record();
            customerList.add(customer.set("contactsId", record.getStr("contacts_id")).set("name", record.getStr("contacts_name")));
        }
        fieldUtil.getFixedField(fieldList, "contactsId", "客户签约人", customerList, "contacts", settingArr, 0);
        customerList = new ArrayList<>();
        if (record.getStr("company_user_id") != null && record.getInt("company_user_id") != 0) {
            customer = new Record();
            customerList.add(customer.set("companyUserId", record.getStr("company_user_id")).set("realname", record.getStr("company_user_name")));
        }
        fieldUtil.getFixedField(fieldList, "companyUserId", "公司签约人", customerList, "user", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "remark", "备注", record.getStr("remark"), "textarea", settingArr, 0);
        fieldList.addAll(adminFieldService.queryByBatchId(record.getStr("batch_id")));
        Record r = Db.findFirst("select IFNULL(SUM(subtotal),0) as total_price \n" +
                "    from 72crm_crm_contract_product    \n" +
                "    where contract_id = ? ", contractId);

        Kv kv = Kv.by("discount_rate", record.getBigDecimal("discount_rate"))
                .set("product", Db.find(Db.getSql("crm.contract.queryBusinessProduct"), contractId))
                .set("total_price", r.getStr("total_price"));

        fieldUtil.getFixedField(fieldList, "product", "产品", kv, "product", settingArr, 0);
        return fieldList;
    }

    /**
     * @author wyq
     * 添加跟进记录
     */
    @Before(Tx.class)
    public R addRecord(AdminRecord adminRecord) {
        adminRecord.setTypes("crm_contract");
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
            oaEventRelation.setContractIds(","+adminRecord.getTypesId().toString()+",");
            oaEventRelation.setCreateTime(DateUtil.date());
            oaEventRelation.save();
        }
        return adminRecord.save() ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 查看跟进记录
     */
    public List<Record> getRecord(BasePageRequest<CrmContract> basePageRequest) {
        CrmContract crmContract = basePageRequest.getData();
        List<Record> recordList = Db.find(Db.getSql("crm.contract.getRecord"), crmContract.getContractId());
        recordList.forEach(record -> {
            adminFileService.queryByBatchId(record.getStr("batch_id"), record);
        });
        return recordList;
    }

    /**
     * 根据合同ID查询产品
     */
    public R qureyProductListByContractId(BasePageRequest<CrmContractProduct> basePageRequest) {

        Integer pageType = basePageRequest.getPageType();
        Record record = Db.findFirst(Db.getSql("crm.product.querySubtotalByContractId"), basePageRequest.getData().getContractId());
        if (record.getStr("money") == null){
            record.set("money",0);
        }
        if (0 == pageType) {
            record.set("list", Db.find(Db.getSql("crm.product.queryProductPageList"), basePageRequest.getData().getContractId()));
            return R.ok().put("data", record);
        } else {
            Page<Record> page = Db.paginateByFullSql(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSql("crm.product.queryProductPagecount"), Db.getSql("crm.product.queryProductPageList"), basePageRequest.getData().getContractId());
            record.set("pageNumber", page.getPageNumber());
            record.set("pageSize", page.getPageSize());
            record.set("totalPage", page.getTotalPage());
            record.set("totalRow", page.getTotalRow());
            record.set("list", page.getList());
            return R.ok().put("data", record);
        }
    }

}

