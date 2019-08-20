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
import com.kakarote.crm9.erp.admin.entity.AdminConfig;
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
import com.kakarote.crm9.utils.AuthUtil;
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

    @Inject
    private AuthUtil authUtil;

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
                .set("公司签约人", record.getStr("company_user_name"))
                .set("备注", record.getStr("remark"));
        List<Record> recordList = Db.find(Db.getSql("admin.field.queryCustomField"),record.getStr("batch_id"));
        fieldUtil.handleType(recordList);
        fieldList.addAll(recordList);
        return fieldList;
    }

    /**
     * 根据id删除合同
     */
    @Before(Tx.class)
    public R deleteByIds(String contractIds) {

        String[] idsArr = contractIds.split(",");
        List<CrmReceivables> list = CrmReceivables.dao.find(Db.getSqlPara("crm.receivables.queryReceivablesByContractIds", Kv.by("contractIds", idsArr)));
        if (list.size() > 0) {
            return R.error("该数据已被其他模块引用，不能被删除！");
        }
        for (String id : idsArr) {
            CrmContract contract = CrmContract.dao.findById(id);
            if (contract != null) {
                Db.delete("delete FROM 72crm_admin_fieldv where batch_id = ?", contract.getBatchId());
            }
            if (!CrmContract.dao.deleteById(id)){
                return R.error();
            }
        }
        return R.ok();
    }

    /**
     * 添加或修改
     */
    @Before(Tx.class)
    public R saveAndUpdate(JSONObject jsonObject) {
        CrmContract crmContract = jsonObject.getObject("entity", CrmContract.class);
        String batchId = StrUtil.isNotEmpty(crmContract.getBatchId()) ? crmContract.getBatchId() : IdUtil.simpleUUID();
        crmRecordService.updateRecord(jsonObject.getJSONArray("field"), batchId);
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
            crmRecordService.addRecord(crmContract.getContractId(),CrmEnum.CONTRACT_TYPE_KEY.getTypes());
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
            crmRecordService.updateRecord(new CrmContract().dao().findById(crmContract.getContractId()), crmContract, CrmEnum.CONTRACT_TYPE_KEY.getTypes());
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
        StringBuilder sql = new StringBuilder("select * from 72crm_crm_contract where 1 = 1 ");
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
        StringBuilder sql = new StringBuilder("select * from contractview where ");
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
        String contractIds = Db.queryStr("select GROUP_CONCAT(contract_id) from 72crm_crm_contract where customer_id in ("+crmCustomer.getCustomerIds()+")");
        if (StrUtil.isEmpty(contractIds)){
            return R.ok();
        }
        crmContract.setContractIds(contractIds);
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
     * @author wyq
     * 查询编辑字段
     */
    public List<Record> queryField(Integer contractId) {
        Record contract = Db.findFirst("select * from contractview where contract_id = ?",contractId);
        List<Record> list = new ArrayList<>();
        list.add(new Record().set("customer_id",contract.getInt("customer_id")).set("customer_name",contract.getStr("customer_name")));
        contract.set("customer_id",list);
        list = new ArrayList<>();
        if (contract.getStr("business_id") != null && contract.getInt("business_id") != 0) {
            list.add(new Record().set("business_id", contract.getInt("business_id")).set("business_name", contract.getStr("business_name")));
        }
        contract.set("business_id",list);
        list = new ArrayList<>();
        if (contract.getStr("contacts_id") != null && contract.getInt("contacts_id") != 0) {
            list.add(new Record().set("contacts_id", contract.getStr("contacts_id")).set("name", contract.getStr("contacts_name")));
        }
        contract.set("contacts_id",list);
        list = new ArrayList<>();
        if (contract.getStr("company_user_id") != null && contract.getInt("company_user_id") != 0) {
            list.add(new Record().set("company_user_id", contract.getStr("company_user_id")).set("realname", contract.getStr("company_user_name")));
        }
        contract.set("company_user_id",list);
        List<Record> fieldList = adminFieldService.queryUpdateField(6,contract);
        Kv kv = Kv.by("discount_rate", contract.getBigDecimal("discount_rate"))
                .set("product", Db.find(Db.getSql("crm.contract.queryBusinessProduct"), contractId))
                .set("total_price", contract.getStr("total_price"));
        fieldList.add(new Record().set("field_name","product").set("name","产品").set("value",kv).set("form_type","product").set("setting",new String[]{}).set("is_null",0).set("field_type",1));
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

    /**
     * 查询合同到期提醒设置
     */
    public R queryContractConfig(){
        AdminConfig config = AdminConfig.dao.findFirst("select status,value as contractDay from 72crm_admin_config where name = 'expiringContractDays' limit 1");
        if (config == null){
            config = new AdminConfig();
            config.setStatus(0);
            config.setName("expiringContractDays");
            config.setValue("3");
            config.setDescription("合同到期提醒");
            config.save();
        }
        return R.ok().put("data",config);
    }

    /**
     * 修改合同到期提醒设置
     */
    @Before(Tx.class)
    public R setContractConfig(Integer status,Integer contractDay){
        if (status == 1 && contractDay == null){
            return R.error("contractDay不能为空");
        }
        Integer number = Db.update(Db.getSqlPara("crm.contract.setContractConfig",Kv.by("status",status).set("contractDay",contractDay)));
        if (0 == number){
            AdminConfig adminConfig = new AdminConfig();
            adminConfig.setStatus(0);
            adminConfig.setName("expiringContractDays");
            adminConfig.setValue("3");
            adminConfig.setDescription("合同到期提醒");
            adminConfig.save();
        }
        return R.ok();
    }
}

