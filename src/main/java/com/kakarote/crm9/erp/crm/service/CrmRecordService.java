package com.kakarote.crm9.erp.crm.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.util.TypeUtils;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.erp.admin.entity.AdminConfig;
import com.kakarote.crm9.erp.admin.entity.AdminField;
import com.kakarote.crm9.erp.admin.entity.AdminFieldv;
import com.kakarote.crm9.erp.admin.entity.AdminRecord;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.entity.*;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.kakarote.crm9.utils.TagUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * crm模块操作记录
 *
 * @author hmb
 */
public class CrmRecordService<T> {

    /**
     * 属性kv
     */
    private static Map<String, Map<String, String>> propertiesMap = new HashMap<>();
    private static final String CRM_PROPERTIES_KEY = "crm:properties_map";
    @SuppressWarnings("unchecked")
    private void init() {
        List<Record> recordList = Db.findByCache(CRM_PROPERTIES_KEY, CRM_PROPERTIES_KEY, Db.getSql("crm.record.getProperties"));
        Map<String,List<Record>> pMap = recordList.stream().collect(Collectors.groupingBy(record -> record.get("type")));
        setProperties(pMap);
    }

    private void setProperties(Map<String,List<Record>> pMap) {
        pMap.forEach((k,v)->{
            HashMap<String,String> resultMap = new HashMap<>();
            v.forEach(record-> resultMap.put(record.getStr("COLUMN_NAME"), record.getStr("COLUMN_COMMENT")));
            propertiesMap.put(k,resultMap);
        });
    }

    private static List<String> textList = new ArrayList<>();

    /**
     * 更新记录
     *
     * @param oldObj   之前对象
     * @param newObj   新对象
     * @param crmTypes 类型
     */
    void updateRecord(T oldObj, T newObj, String crmTypes) {
        init();
        CrmActionRecord crmActionRecord = new CrmActionRecord();
        crmActionRecord.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
        crmActionRecord.setCreateTime(new Date());

        if (crmTypes.equals(CrmEnum.PRODUCT_TYPE_KEY.getTypes())) {
            CrmProduct oldObj1 = (CrmProduct) oldObj;
            CrmProduct newObj1 = (CrmProduct) newObj;
            searchChange(textList, oldObj1._getAttrsEntrySet(), newObj1._getAttrsEntrySet(), CrmEnum.PRODUCT_TYPE_KEY.getTypes());
            crmActionRecord.setTypes(CrmEnum.PRODUCT_TYPE_KEY.getTypes());
            crmActionRecord.setActionId(oldObj1.getProductId());
        } else if (crmTypes.equals(CrmEnum.CONTACTS_TYPE_KEY.getTypes())) {
            CrmContacts oldObj1 = (CrmContacts) oldObj;
            CrmContacts newObj1 = (CrmContacts) newObj;
            searchChange(textList, oldObj1._getAttrsEntrySet(), newObj1._getAttrsEntrySet(), CrmEnum.CONTACTS_TYPE_KEY.getTypes());
            crmActionRecord.setTypes(CrmEnum.CONTACTS_TYPE_KEY.getTypes());
            crmActionRecord.setActionId(oldObj1.getContactsId());
        } else if (crmTypes.equals(CrmEnum.CUSTOMER_TYPE_KEY.getTypes())) {
            CrmCustomer oldObj1 = (CrmCustomer) oldObj;
            CrmCustomer newObj1 = (CrmCustomer) newObj;
            searchChange(textList, oldObj1._getAttrsEntrySet(), newObj1._getAttrsEntrySet(), CrmEnum.CUSTOMER_TYPE_KEY.getTypes());
            crmActionRecord.setTypes(CrmEnum.CUSTOMER_TYPE_KEY.getTypes());
            crmActionRecord.setActionId(oldObj1.getCustomerId());
        } else if (crmTypes.equals(CrmEnum.LEADS_TYPE_KEY.getTypes())) {
            CrmLeads oldObj1 = (CrmLeads) oldObj;
            CrmLeads newObj1 = (CrmLeads) newObj;
            searchChange(textList, oldObj1._getAttrsEntrySet(), newObj1._getAttrsEntrySet(), CrmEnum.LEADS_TYPE_KEY.getTypes());
            crmActionRecord.setTypes(CrmEnum.LEADS_TYPE_KEY.getTypes());
            crmActionRecord.setActionId(oldObj1.getLeadsId());
        } else if (crmTypes.equals(CrmEnum.CONTRACT_TYPE_KEY.getTypes())) {
            CrmContract oldObj1 = (CrmContract) oldObj;
            CrmContract newObj1 = (CrmContract) newObj;
            searchChange(textList, oldObj1._getAttrsEntrySet(), newObj1._getAttrsEntrySet(), CrmEnum.CONTRACT_TYPE_KEY.getTypes());
            crmActionRecord.setTypes(CrmEnum.CONTRACT_TYPE_KEY.getTypes());
            crmActionRecord.setActionId(oldObj1.getContractId());
        } else if (crmTypes.equals(CrmEnum.RECEIVABLES_TYPE_KEY.getTypes())) {
            CrmReceivables oldObj1 = (CrmReceivables) oldObj;
            CrmReceivables newObj1 = (CrmReceivables) newObj;
            searchChange(textList, oldObj1._getAttrsEntrySet(), newObj1._getAttrsEntrySet(), CrmEnum.RECEIVABLES_TYPE_KEY.getTypes());
            crmActionRecord.setTypes(CrmEnum.RECEIVABLES_TYPE_KEY.getTypes());
            crmActionRecord.setActionId(oldObj1.getReceivablesId());
        } else if (crmTypes.equals(CrmEnum.BUSINESS_TYPE_KEY.getTypes())) {
            CrmBusiness oldObj1 = (CrmBusiness) oldObj;
            CrmBusiness newObj1 = (CrmBusiness) newObj;
            searchChange(textList, oldObj1._getAttrsEntrySet(), newObj1._getAttrsEntrySet(), CrmEnum.BUSINESS_TYPE_KEY.getTypes());
            crmActionRecord.setTypes(CrmEnum.BUSINESS_TYPE_KEY.getTypes());
            crmActionRecord.setActionId(oldObj1.getBusinessId());
        }
        crmActionRecord.setContent(JSON.toJSONString(textList));
        if (textList.size() > 0) {
            crmActionRecord.save();
        }
        textList.clear();
    }

    public void addRecord(Integer actionId, String crmTypes) {
        CrmActionRecord crmActionRecord = new CrmActionRecord();
        crmActionRecord.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
        crmActionRecord.setCreateTime(new Date());
        crmActionRecord.setTypes(crmTypes);
        crmActionRecord.setActionId(actionId);
        ArrayList<String> strings = new ArrayList<>();
        strings.add("新建了" + CrmEnum.getName(crmTypes));
        crmActionRecord.setContent(JSON.toJSONString(strings));
        crmActionRecord.save();
    }

    public void updateRecord(JSONArray jsonArray, String batchId) {
        if (jsonArray == null) {
            return;
        }
        List<AdminFieldv> oldFieldList = new AdminFieldv().dao().find("select * from 72crm_admin_fieldv where batch_id = ?", batchId);
        oldFieldList.forEach(oldField -> {
            jsonArray.forEach(json -> {
                AdminFieldv newField = TypeUtils.castToJavaBean(json, AdminFieldv.class);
                String oldFieldValue;
                String newFieldValue;
                if (oldField.getValue() == null) {
                    oldFieldValue = "空";
                } else {
                    oldFieldValue = oldField.getValue();
                }
                if (newField.getValue() == null) {
                    newFieldValue = "空";
                } else {
                    newFieldValue = newField.getValue();
                }
                if (oldField.getName().equals(newField.getName()) && !oldFieldValue.equals(newFieldValue)) {
                    textList.add("将" + oldField.getName() + " 由" + oldFieldValue + "修改为" + newFieldValue + "。");
                }
            });
        });
    }

    private void searchChange(List<String> textList, Set<Map.Entry<String, Object>> oldEntries, Set<Map.Entry<String, Object>> newEntries, String crmTypes) {
        oldEntries.forEach(x -> {
            newEntries.forEach(y -> {
                Object oldValue = x.getValue();
                Object newValue = y.getValue();
                if (oldValue instanceof Date) {
                    oldValue = DateUtil.formatDateTime((Date) oldValue);
                }
                if (newValue instanceof Date) {
                    newValue = DateUtil.formatDateTime((Date) newValue);
                }
                if (oldValue == null || "".equals(oldValue)) {
                    oldValue = "空";
                }
                if (newValue == null || "".equals(newValue)) {
                    newValue = "空";
                }
                if (x.getKey().equals(y.getKey()) && !oldValue.equals(newValue)) {
                    if (!"update_time".equals(x.getKey())) {
                        textList.add("将" + propertiesMap.get(crmTypes).get(x.getKey()) + " 由" + oldValue + "修改为" + newValue + "。");
                    }
                }
            });
        });
    }

    public R queryRecordList(String actionId, String crmTypes) {
        List<Record> recordList = Db.find("select a.*,b.realname,b.img from 72crm_crm_action_record a left join 72crm_admin_user b on a.create_user_id = b.user_id where action_id = ? and types = ? order by create_time desc", actionId, crmTypes);
        recordList.forEach(record -> {
            List<String> list = JSON.parseArray(record.getStr("content"), String.class);
            record.set("content", list);
        });
        return R.ok().put("data", recordList);
    }

    /**
     * 添加转移记录
     *
     * @param actionId
     * @param crmTypes
     */
    public void addConversionRecord(Integer actionId, String crmTypes, Integer userId) {
        String name = Db.queryStr("select realname from 72crm_admin_user where user_id = ?", userId);
        CrmActionRecord crmActionRecord = new CrmActionRecord();
        crmActionRecord.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
        crmActionRecord.setCreateTime(new Date());
        crmActionRecord.setTypes(crmTypes);
        crmActionRecord.setActionId(actionId);
        ArrayList<String> strings = new ArrayList<>();
        strings.add("将" + CrmEnum.getName(crmTypes) + "转移给：" + name);
        crmActionRecord.setContent(JSON.toJSONString(strings));
        crmActionRecord.save();
    }

    /**
     * 添加(锁定/解锁)记录
     *
     */
    public void addIsLockRecord(String[] ids, String crmTypes, Integer isLock) {
        CrmActionRecord crmActionRecord = new CrmActionRecord();
        crmActionRecord.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
        crmActionRecord.setCreateTime(new Date());
        crmActionRecord.setTypes(crmTypes);
        ArrayList<String> strings = new ArrayList<>();
        if(isLock == 1){
            strings.add("将客户锁定。");
        }else {
            strings.add("将客户解锁。");
        }
        crmActionRecord.setContent(JSON.toJSONString(strings));
        for(String actionId : ids){
            crmActionRecord.setActionId(Integer.valueOf(actionId));
            crmActionRecord.save();
        }
    }
    /**
     * 线索转化客户
     *
     * @param actionId
     * @param crmTypes
     */
    public void addConversionCustomerRecord(Integer actionId, String crmTypes,String name) {
        CrmActionRecord crmActionRecord = new CrmActionRecord();
        crmActionRecord.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
        crmActionRecord.setCreateTime(new Date());
        crmActionRecord.setTypes(crmTypes);
        crmActionRecord.setActionId(actionId);
        ArrayList<String> strings = new ArrayList<>();
        strings.add("将线索\""+name+"\"转化为客户");
        crmActionRecord.setContent(JSON.toJSONString(strings));
        crmActionRecord.save();
    }
    /**
     * 放入公海
     *
     * @param actionIds
     * @param crmTypes
     */
    public void addPutIntoTheOpenSeaRecord(Collection actionIds, String crmTypes) {
        CrmActionRecord crmActionRecord = new CrmActionRecord();
        if(BaseUtil.getRequest() == null){
            crmActionRecord.setCreateUserId(BaseConstant.SUPER_ADMIN_USER_ID.intValue());
        }else {
            crmActionRecord.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
        }
        crmActionRecord.setCreateTime(new Date());
        crmActionRecord.setTypes(crmTypes);
        ArrayList<String> strings = new ArrayList<>();
        strings.add("将客户放入公海");
        crmActionRecord.setContent(JSON.toJSONString(strings));
        for(Object actionId : actionIds){
            crmActionRecord.remove("id");
            crmActionRecord.setActionId((Integer) actionId);
            crmActionRecord.save();
        }
    }


    /**
     * 添加分配客户记录
     *
     * @param actionId
     * @param crmTypes
     */
    public void addDistributionRecord(String actionId, String crmTypes, Long userId) {
        for(String id : actionId.split(",")){
            if(StrUtil.isEmpty(id)){
                continue;
            }
            ArrayList<String> strings = new ArrayList<>();
            String name = Db.queryStr("select realname from 72crm_admin_user where user_id = ?", userId);
            CrmActionRecord crmActionRecord = new CrmActionRecord();
            crmActionRecord.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
            crmActionRecord.setCreateTime(new Date());
            crmActionRecord.setTypes(crmTypes);
            crmActionRecord.setActionId(Integer.valueOf(id));
            if(userId == null){
                //领取
                strings.add("领取了客户");
            }else {
                //管理员分配
                strings.add("将客户分配给：" + name);
            }
            crmActionRecord.setContent(JSON.toJSONString(strings));
            crmActionRecord.save();
        }
    }


    /**
     * @author wyq
     * 删除跟进记录
     */
    public R deleteFollowRecord(Integer recordId){
        return AdminRecord.dao.deleteById(recordId) ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 查询跟进记录类型
     */
    public R queryRecordOptions(){
        List<String> list = Db.query("select value from 72crm_admin_config where name = 'followRecordOption'");
        return R.ok().put("data",list);
    }

    /**
     * @author wyq
     * 设置跟进记录类型
     */
    @Before(Tx.class)
    public R setRecordOptions(List<String> list){
        Db.delete("delete from 72crm_admin_config where name = 'followRecordOption'");
        List<AdminConfig> adminConfigList = new ArrayList<>();
        for(int i=0;i<list.size();i++){
            AdminConfig adminConfig = new AdminConfig();
            adminConfig.setName("followRecordOption");
            adminConfig.setValue(list.get(i));
            adminConfig.setDescription("跟进记录选项");
            adminConfigList.add(adminConfig);
        }
        Db.batchSave(adminConfigList,100);
        return R.ok();
    }
}
