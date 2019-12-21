package com.kakarote.crm9.erp.crm.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.util.TypeUtils;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.TableMapping;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.erp.admin.entity.AdminConfig;
import com.kakarote.crm9.erp.admin.entity.AdminField;
import com.kakarote.crm9.erp.admin.entity.AdminFieldv;
import com.kakarote.crm9.erp.admin.entity.AdminRecord;
import com.kakarote.crm9.erp.admin.service.AdminFileService;
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
public class CrmRecordService<T extends Model>{

    @Inject
    private AdminFileService adminFileService;

    /**
     * 属性kv
     */
    private static Map<String,Map<String,String>> propertiesMap = new HashMap<>();
    private static final String CRM_PROPERTIES_KEY = "crm:properties_map";

    private void init(){
        List<Record> recordList = Db.findByCache(CRM_PROPERTIES_KEY, CRM_PROPERTIES_KEY, Db.getSql("crm.record.getProperties"));
        Map<String,List<Record>> pMap = recordList.stream().collect(Collectors.groupingBy(record -> record.get("type")));
        pMap.forEach((k, v) -> {
            HashMap<String,String> resultMap = new HashMap<>();
            v.forEach(record -> resultMap.put(record.getStr("COLUMN_NAME"), record.getStr("COLUMN_COMMENT")));
            propertiesMap.put(k, resultMap);
        });
    }


    private static List<String> textList = new ArrayList<>();

    /**
     * 更新记录
     *
     * @param oldObj  之前对象
     * @param newObj  新对象
     * @param crmEnum 类型
     */
    @SuppressWarnings("unchecked")
    void updateRecord(T oldObj, T newObj, CrmEnum crmEnum){
        init();
        CrmActionRecord crmActionRecord = new CrmActionRecord();
        crmActionRecord.setCreateUserId(BaseUtil.getUser().getUserId());
        crmActionRecord.setCreateTime(new Date());
        searchChange(textList, oldObj._getAttrsEntrySet(), (newObj)._getAttrsEntrySet(), crmEnum.getType() + "");
        crmActionRecord.setTypes(crmEnum.getType() + "");
        crmActionRecord.setActionId(oldObj.getInt(TableMapping.me().getTable(oldObj.getClass()).getPrimaryKey()[0]));
        crmActionRecord.setContent(JSON.toJSONString(textList));
        if(textList.size() > 0){
            crmActionRecord.save();
        }
        textList.clear();
    }

    public void addRecord(Integer actionId, CrmEnum crmEnum){
        CrmActionRecord crmActionRecord = new CrmActionRecord();
        crmActionRecord.setCreateUserId(BaseUtil.getUser().getUserId());
        crmActionRecord.setCreateTime(new Date());
        crmActionRecord.setTypes(crmEnum.getType() + "");
        crmActionRecord.setActionId(actionId);
        ArrayList<String> strings = new ArrayList<>();
        strings.add("新建了" + crmEnum.getRemarks());
        crmActionRecord.setContent(JSON.toJSONString(strings));
        crmActionRecord.save();
    }

    public void updateRecord(JSONArray jsonArray, String batchId){
        if(jsonArray == null){
            return;
        }
        List<AdminFieldv> oldFieldList = new AdminFieldv().dao().find("select * from 72crm_admin_fieldv where batch_id = ?", batchId);
        oldFieldList.forEach(oldField -> {
            jsonArray.forEach(json -> {
                AdminFieldv newField = TypeUtils.castToJavaBean(json, AdminFieldv.class);
                String oldFieldValue;
                String newFieldValue;
                if(oldField.getValue() == null){
                    oldFieldValue = "空";
                }else{
                    oldFieldValue = oldField.getValue();
                }
                if(newField.getValue() == null){
                    newFieldValue = "空";
                }else{
                    newFieldValue = newField.getValue();
                }
                if(oldField.getName().equals(newField.getName()) && ! oldFieldValue.equals(newFieldValue)){
                    textList.add("将" + oldField.getName() + " 由" + oldFieldValue + "修改为" + newFieldValue + "。");
                }
            });
        });
    }

    private void searchChange(List<String> textList, Set<Map.Entry<String,Object>> oldEntries, Set<Map.Entry<String,Object>> newEntries, String crmTypes){
        oldEntries.forEach(x -> {
            newEntries.forEach(y -> {
                Object oldValue = x.getValue();
                Object newValue = y.getValue();
                if(oldValue instanceof Date){
                    oldValue = DateUtil.formatDateTime((Date) oldValue);
                }
                if(newValue instanceof Date){
                    newValue = DateUtil.formatDateTime((Date) newValue);
                }
                if(oldValue == null || "".equals(oldValue)){
                    oldValue = "空";
                }
                if(newValue == null || "".equals(newValue)){
                    newValue = "空";
                }
                if(x.getKey().equals(y.getKey()) && ! oldValue.equals(newValue)){
                    if("owner_user_id".equals(x.getKey())){
                        newValue = Db.queryStr("select realname from `72crm_admin_user` where user_id = ?", newValue);
                        oldValue = Db.queryStr("select realname from `72crm_admin_user` where user_id = ?", oldValue);
                    }else if("customer_id".equals(x.getKey())){
                        newValue = Db.queryStr("select customer_name from `72crm_crm_customer` where customer_id = ?", newValue);
                        oldValue = Db.queryStr("select customer_name from `72crm_crm_customer` where customer_id = ?", oldValue);
                    }else if("business_id".equals(x.getKey())){
                        newValue = Db.queryStr("select business_name from `72crm_crm_business` where business_name = ?", newValue);
                        oldValue = Db.queryStr("select business_name from `72crm_crm_business` where business_name = ?", oldValue);
                    }else if("contract_id".equals(x.getKey())){
                        newValue = Db.queryStr("select name from `72crm_crm_contract` where contract_id = ?", newValue);
                        oldValue = Db.queryStr("select name from `72crm_crm_contract` where contract_id = ?", oldValue);
                    }else if("category_id".equals(x.getKey())){
                        newValue = Db.queryStr("select name from `72crm_crm_product_category` where category_id = ?", newValue);
                        oldValue = Db.queryStr("select name from `72crm_crm_product_category` where category_id = ?", oldValue);
                    }
                    if(! "update_time".equals(x.getKey()) && ! "create_user_id".equals(x.getKey())){
                        textList.add("将" + propertiesMap.get(crmTypes).get(x.getKey()) + " 由" + oldValue + "修改为" + newValue + "。");
                    }
                }
            });
        });
    }

    public R queryRecordList(String actionId, String crmTypes){
        List<Record> recordList = Db.find(Db.getSql("crm.record.queryRecordList"), actionId, crmTypes);
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
     */
    public void addConversionRecord(Integer actionId, CrmEnum crmEnum, Long userId){
        String name = Db.queryStr("select realname from 72crm_admin_user where user_id = ?", userId);
        CrmActionRecord crmActionRecord = new CrmActionRecord();
        crmActionRecord.setCreateUserId(BaseUtil.getUser().getUserId());
        crmActionRecord.setCreateTime(new Date());
        crmActionRecord.setTypes(crmEnum.getType() + "");
        crmActionRecord.setActionId(actionId);
        ArrayList<String> strings = new ArrayList<>();
        strings.add("将" + crmEnum.getName() + "转移给：" + name);
        crmActionRecord.setContent(JSON.toJSONString(strings));
        crmActionRecord.save();
    }

    /**
     * 添加(锁定/解锁)记录
     */
    public void addIsLockRecord(String[] ids, String crmTypes, Integer isLock){
        CrmActionRecord crmActionRecord = new CrmActionRecord();
        crmActionRecord.setCreateUserId(BaseUtil.getUser().getUserId());
        crmActionRecord.setCreateTime(new Date());
        crmActionRecord.setTypes(crmTypes);
        ArrayList<String> strings = new ArrayList<>();
        if(isLock == 1){
            strings.add("将客户锁定。");
        }else{
            strings.add("将客户解锁。");
        }
        crmActionRecord.setContent(JSON.toJSONString(strings));
        for(String actionId : ids){
            crmActionRecord.setId(null);
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
    public void addConversionCustomerRecord(Integer actionId, String crmTypes, String name){
        CrmActionRecord crmActionRecord = new CrmActionRecord();
        crmActionRecord.setCreateUserId(BaseUtil.getUser().getUserId());
        crmActionRecord.setCreateTime(new Date());
        crmActionRecord.setTypes(crmTypes);
        crmActionRecord.setActionId(actionId);
        ArrayList<String> strings = new ArrayList<>();
        strings.add("将线索\"" + name + "\"转化为客户");
        crmActionRecord.setContent(JSON.toJSONString(strings));
        crmActionRecord.save();
    }

    /**
     * 放入公海
     *
     */
    public void addPutIntoTheOpenSeaRecord(Collection actionIds, String crmTypes){
        CrmActionRecord crmActionRecord = new CrmActionRecord();
        if(BaseUtil.getUser() == null){
            crmActionRecord.setCreateUserId(BaseConstant.SUPER_ADMIN_USER_ID);
        }else{
            crmActionRecord.setCreateUserId(BaseUtil.getUser().getUserId());
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
     */
    public void addDistributionRecord(String actionId, String crmTypes, Long userId){
        for(String id : actionId.split(",")){
            if(StrUtil.isEmpty(id)){
                continue;
            }
            ArrayList<String> strings = new ArrayList<>();
            String name = Db.queryStr("select realname from 72crm_admin_user where user_id = ?", userId);
            CrmActionRecord crmActionRecord = new CrmActionRecord();
            crmActionRecord.setCreateUserId(BaseUtil.getUser().getUserId());
            crmActionRecord.setCreateTime(new Date());
            crmActionRecord.setTypes(crmTypes);
            crmActionRecord.setActionId(Integer.valueOf(id));
            if(userId == null){
                //领取
                strings.add("领取了客户");
            }else{
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
        adminFileService.removeByBatchId(Db.queryStr("select batch_id from `72crm_admin_record` where record_id = ?",recordId));
        return AdminRecord.dao.deleteById(recordId) ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 查询跟进记录类型
     */
    public R queryRecordOptions(){
        List<String> list = Db.query("select value from 72crm_admin_config where name = 'followRecordOption'");
        return R.ok().put("data", list);
    }

    /**
     * @author wyq
     * 设置跟进记录类型
     */
    @Before(Tx.class)
    public R setRecordOptions(List<String> list){
        Db.delete("delete from 72crm_admin_config where name = 'followRecordOption'");
        for(int i = 0; i < list.size(); i++){
            AdminConfig adminConfig = new AdminConfig();
            adminConfig.setName("followRecordOption");
            adminConfig.setValue(list.get(i));
            adminConfig.setDescription("跟进记录选项");
            adminConfig.save();
        }
        return R.ok();
    }
}
