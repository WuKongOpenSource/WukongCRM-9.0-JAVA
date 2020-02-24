package com.kakarote.crm9.erp.admin.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import com.jfinal.aop.Before;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kakarote.crm9.common.config.cache.CaffeineCache;
import com.kakarote.crm9.erp.admin.entity.AdminField;
import com.kakarote.crm9.erp.admin.entity.AdminFieldSort;
import com.kakarote.crm9.erp.admin.entity.AdminFieldStyle;
import com.kakarote.crm9.erp.admin.entity.AdminFieldv;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.common.FormTypeEnum;
import com.kakarote.crm9.utils.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


public class AdminFieldService {

    /**
     * @author wyq
     * 查询新增字段列表
     */
    public List<Record> queryAddField(CrmEnum crmEnum){
        List<Record> fieldList = Db.find(Db.getSql("admin.field.queryAddField"),crmEnum.getType());
        recordToFormType(fieldList);
        if (crmEnum == CrmEnum.CRM_CUSTOMER){
            Record map = new Record();
            fieldList.add(map.set("field_name", "map_address").set("name", "地区定位").set("form_type", "map_address").set("is_null", 0).set("field_type",FormTypeEnum.TEXT.getType()));
        }else if (crmEnum == CrmEnum.CRM_BUSINESS){
            fieldList.add(new Record().set("field_name","type_id").set("name","商机状态组").set("value","").set("form_type","business_type").set("setting",new String[0]).set("is_null",1).set("field_type",FormTypeEnum.TEXT.getType()));
            fieldList.add(new Record().set("field_name","status_id").set("name","商机阶段").set("value","").set("form_type","business_status").set("setting",new String[0]).set("is_null",1).set("field_type",FormTypeEnum.TEXT.getType()));
        }else if (crmEnum == CrmEnum.CRM_PRODUCT){
            fieldList.forEach(record -> {
                if ("category".equals(record.getStr("field_name"))){
                    record.set("value",new String[0]);
                }
            });
        }
        if (crmEnum == CrmEnum.CRM_BUSINESS || crmEnum == CrmEnum.CRM_CONTRACT){
            Record record = new Record();
            fieldList.add(record.set("field_name","product").set("name","产品").set("value",Kv.by("discount_rate", "").set("product", new ArrayList<>()).set("total_price", "")).set("formType","product").set("setting",new String[]{}).set("is_null",0).set("field_type",FormTypeEnum.TEXT.getType()));
        }
        return fieldList;
    }

    /**
     * @author wyq
     * 查询编辑字段列表
     */
    public List<Record> queryUpdateField(Integer label,Record record){
        List<Record> recordList = Db.find(Db.getSql("admin.field.queryAddField"),label);
        recordList.forEach(field ->{
            if (field.getInt("type") == FormTypeEnum.FILE.getType()){
                field.set("value",Db.find("select file_id, name, size, create_user_id, create_time, file_path, file_type from 72crm_admin_file where batch_id = ?",StrUtil.isNotEmpty(record.get(field.getStr("field_name")))?record.get(field.getStr("field_name")):""));
            }else if (field.getInt("type") == FormTypeEnum.USER.getType()){
                field.set("value",Db.find("select user_id,realname from 72crm_admin_user where find_in_set(user_id,ifnull(?,0))",record.getStr(field.getStr("field_name"))));
            }else if (field.getInt("type") == FormTypeEnum.STRUCTURE.getType()){
                field.set("value",Db.find("select dept_id,name from 72crm_admin_dept where find_in_set(dept_id,ifnull(?,0))",record.getStr(field.getStr("field_name"))));
            }else {
                field.set("value",record.get(field.getStr("field_name"))!=null ? record.get(field.getStr("field_name")):"");
            }
        });
        recordToFormType(recordList);
        return recordList;
    }

    /**
     * 查询详情页基本信息
     * @param crmEnum
     * @param record
     * @param fieldKey
     */
    public List<Record> queryInformation(CrmEnum crmEnum,Record record,List<String> fieldKey){
        List<Record> recordList = Db.find(Db.getSql("admin.field.queryInformationField"),crmEnum.getType());
        Map<Integer, List<Record>> fieldTypeMap = recordList.stream().collect(Collectors.groupingBy(filed -> filed.getInt("field_type")));
        List<Record> resultList = new ArrayList<>();
        fieldTypeMap.forEach((type,list)->{
            if (type == 1){
                list.forEach(field->{
                    String fieldName=field.getStr("field_name");
                    if (fieldKey.contains(fieldName)){
                        field.set("value",record.get(fieldName)!=null ? record.get(fieldName):"");
                        resultList.add(field);
                    }
                });
            }else {
                list.forEach(field->{
                    if (field.getInt("type") == FormTypeEnum.FILE.getType()){
                        field.set("value",Db.find("select file_id, name, size, create_time, file_path, file_type, batch_id from 72crm_admin_file where batch_id = ?",StrUtil.isNotEmpty(record.get(field.getStr("field_name")))?record.get(field.getStr("field_name")):""));
                    }else if (field.getInt("type") == FormTypeEnum.USER.getType()){
                        field.set("value",Db.find("select user_id,realname from 72crm_admin_user where find_in_set(user_id,ifnull(?,0))",record.getStr(field.getStr("field_name"))));
                    }else if (field.getInt("type") == FormTypeEnum.STRUCTURE.getType()){
                        field.set("value",Db.find("select dept_id,name from 72crm_admin_dept where find_in_set(dept_id,ifnull(?,0))",record.getStr(field.getStr("field_name"))));
                    }else {
                        field.set("value",record.get(field.getStr("field_name"))!=null ? record.get(field.getStr("field_name")):"");
                    }
                    resultList.add(field);
                });
            }
        });
        recordToFormType(resultList);
        return resultList;
    }

    /**
     * author zhangzhiwei
     * 保存自定义字段信息
     *
     * @param jsonObject 详见接口文档
     * @return R
     */
    @Before(Tx.class)
    public R save(JSONObject jsonObject) {
        JSONArray adminFields = jsonObject.getJSONArray("data");
        Map<String, List<AdminField>> collect = adminFields.stream().map(adminField -> TypeUtils.castToJavaBean(adminField, AdminField.class)).collect(Collectors.groupingBy(AdminField::getName));
        for (Map.Entry<String, List<AdminField>> entry : collect.entrySet()) {
            if (entry.getValue().size() > 1) {
                return R.error("自定义表单名称不能重复！");
            }
        }
        Integer label = jsonObject.getInteger("label");
        Integer categoryId = jsonObject.getInteger("categoryId");
        if (categoryId != null && Db.queryInt("select ifnull(is_sys,0) from 72crm_oa_examine_category where category_id = ?", categoryId) == 1) {
            return R.error("系统审批类型暂不支持编辑");
        }
        List<Integer> arr = new ArrayList<>();
        adminFields.forEach(object -> {
            AdminField field = TypeUtils.castToJavaBean(object, AdminField.class);
            if (field.getFieldId() != null) {
                arr.add(field.getFieldId());
            }
        });
        List<AdminField> fieldSorts = AdminField.dao.find("select name from 72crm_admin_field where label = ?", label);
        List<String> nameList = fieldSorts.stream().map(AdminField::getName).collect(Collectors.toList());
        if (arr.size() > 0) {
            SqlPara sql = Db.getSqlPara("admin.field.deleteByChooseId", Kv.by("ids", arr).set("label", label).set("categoryId", categoryId));
            SqlPara sqlPara = Db.getSqlPara("admin.field.deleteByFieldValue", Kv.by("ids", arr).set("label", label).set("categoryId", categoryId));
            Db.delete(sqlPara.getSql(),sqlPara.getPara());
            Db.delete(sql.getSql(), sql.getPara());
        }
        List<String> fieldList = new ArrayList<>();
        for (int i = 0; i < adminFields.size(); i++) {
            adminFields.getJSONObject(i).remove("value");
            Object defaultValue = adminFields.getJSONObject(i).get("defaultValue");
            if (defaultValue instanceof JSONArray && ((JSONArray) defaultValue).size() == 0) {
                adminFields.getJSONObject(i).remove("defaultValue");
            }
            AdminField entity = TypeUtils.castToJavaBean(adminFields.get(i), AdminField.class);
            entity.setUpdateTime(DateUtil.date());
            if (entity.getFieldType() == null || entity.getFieldType() == 0){
                entity.setFieldName(entity.getName());
            }
            if (label == 10) {
                entity.setExamineCategoryId(jsonObject.getInteger("categoryId"));
            }
            entity.setSorting(i);
            entity.set("label", label);
            if (entity.getFieldId() != null) {
                entity.update();
                if (entity.getFieldType() == 0){
                    Db.update(Db.getSqlPara("admin.field.updateFieldSortName", entity));
                }else if (entity.getFieldType() == 1){
                    Db.update("update 72crm_admin_field_sort set name = ? where field_id = ?",entity.getName(),entity.getFieldId());
                }
            } else {
                entity.save();
            }
            fieldList.add(entity.getName());
        }
        nameList.removeAll(fieldList);
        if (nameList.size() != 0) {
            Db.update(Db.getSqlPara("admin.field.deleteFieldSort", Kv.by("label", label).set("names", nameList)));
        }
        CaffeineCache.ME.removeAll("field");
        return R.ok();
    }

    /**
     * 查询唯一字段
     * @author zhangzhiwei
     * @param crmEnum 字段类型
     * @param record 参数
     * @return 数据库存在的数量
     */
    public Integer verify(CrmEnum crmEnum,Record record) {
        if (record.get("field_type").equals(0)){
            return Db.template("admin.field.queryFieldIsExist",record.getColumns()).queryInt();
        }else {
            return Db.template("admin.field.queryFixedIsExist",record.set("tableName",crmEnum.getName()).getColumns()).queryInt();
        }
    }

    /**
     * 保存自定义字段
     *
     * @param array   参数对象
     * @param batchId 批次ID
     * @return 操作结果
     */
    public boolean save(JSONArray array, String batchId) {
        if (array == null || StrUtil.isEmpty(batchId)) {
            return false;
        }
        Db.deleteById("72crm_admin_fieldv", "batch_id", batchId);
        List<AdminFieldv> adminFieldvs=new ArrayList<>();
        array.forEach(obj -> {
            AdminFieldv fieldv = TypeUtils.castToJavaBean(obj, AdminFieldv.class);
            fieldv.setId(null);
            fieldv.setCreateTime(DateUtil.date());
            fieldv.setBatchId(batchId);
            adminFieldvs.add(fieldv);
        });
        Db.batchSave(adminFieldvs,100);
        return true;
    }

    /**
     * 保存自定义字段
     *
     * @param array   参数对象
     * @param batchId 批次ID
     * @return 操作结果
     */
    public boolean save(List<AdminFieldv> array, String batchId) {
        if (array == null || StrUtil.isEmpty(batchId)) {
            return false;
        }
        Db.deleteById("72crm_admin_fieldv", "batch_id", batchId);
        array.forEach(fieldv -> {
            fieldv.setId(null);
            fieldv.setCreateTime(DateUtil.date());
            fieldv.setBatchId(batchId);
            fieldv.save();
        });
        return true;
    }

    public List<Record> queryFieldsByBatchId(String batchId, String... name) {
        if (StrUtil.isEmpty(batchId)) {
            return new ArrayList<>();
        }
        return Db.find(AdminField.dao.getSqlPara("admin.field.queryFieldsByBatchId", Kv.by("batchId", batchId).set("names", name)));
    }

    public List<Record> queryByBatchId(String batchId, Integer label) {
        if (StrUtil.isEmpty(batchId)) {
            return new ArrayList<>();
        }
        List<Record> recordList = Db.find(AdminField.dao.getSqlPara("admin.field.queryFieldsByBatchId", Kv.by("batchId", batchId).set("label", label)));
        recordList.forEach(record -> {
            if (record.getInt("type") == 10) {
                if (StrUtil.isNotEmpty(record.getStr("value"))) {
                    List<Record> userList = Db.find("select user_id,realname from 72crm_admin_user where user_id in (" + record.getStr("value") + ")");
                    record.set("value", userList);
                } else {
                    record.set("value", new ArrayList<>());
                }
                record.set("default_value", new ArrayList<>(0));
            } else if (record.getInt("type") == 12) {
                if (StrUtil.isNotEmpty(record.getStr("value"))) {
                    List<Record> deptList = Db.find("select dept_id,name from 72crm_admin_dept where dept_id in (" + record.getStr("value") + ")");
                    record.set("value", deptList);
                } else {
                    record.set("value", new ArrayList<>());
                }
                record.set("default_value", new ArrayList<>(0));
            }
        });
        recordToFormType(recordList);
        return recordList;
    }

    public List<Record> queryByBatchId(String batchId) {
        return queryByBatchId(batchId, null);
    }

    public R queryFields() {
        List<Record> records = Db.find(Db.getSqlPara("admin.field.queryFields"));
        return R.ok().put("data", records);
    }

    public void recordToFormType(List<Record> recordList) {
        for (Record record : recordList) {
            Integer dataType = record.getInt("type");
            FormTypeEnum typeEnum = FormTypeEnum.parse(dataType);
            record.set("formType", typeEnum.getFormType());
            if(dataType == FormTypeEnum.CHECKBOX.getType()){
                recordValueToArray(record);
            }else if(dataType == FormTypeEnum.USER.getType()){
                record.set("default_value", new ArrayList<>(0));
            }else if(dataType == FormTypeEnum.STRUCTURE.getType()){
                record.set("default_value", new ArrayList<>(0));
            }
            if (FormTypeEnum.SELECT.getType() == dataType || FormTypeEnum.CHECKBOX.getType() == dataType) {
                if (record.getStr("options") != null) {
                    record.set("setting", record.getStr("options").split(","));
                }
            } else {
                record.set("setting", new String[]{});
            }
        }
    }

    private void recordValueToArray(Record record) {
        record.set("default_value", StrUtil.isNotEmpty(record.get("default_value")) ? record.getStr("default_value").split(",") : new String[]{});
        record.set("value", StrUtil.isNotEmpty(record.getStr("value")) ? record.getStr("value").split(",") : new String[]{});
    }

    /**
     * @author wyq
     * 查询fieldType为0的字段
     */
    public List<Record> customFieldList(Integer label){
        List <Record> recordList = Db.find(Db.getSql("admin.field.customerFieldList"),label);
        recordToFormType(recordList);
        return recordList;
    }

    public List<Record> list(Integer label) {
        return list(label, null);
    }

    public List<Record> list(Integer label, String categoryId) {
        List<Record> recordList = Db.find(Db.getSqlPara("admin.field.list", Kv.by("label", label).set("categoryId", categoryId)));
        recordToFormType(recordList);
        if (categoryId == null) {
            return recordList;
        }
        FieldUtil fieldUtil = new FieldUtil(recordList);
        return fieldUtil.getRecordList();
    }

    public R setFelidStyle(Kv kv) {
        String types = kv.getStr("types");
        int type=CrmEnum.parse(types).getType();
        AdminFieldStyle adminFleldStyle = AdminFieldStyle.dao.findFirst(AdminFieldStyle.dao.getSql("admin.field.queryFieldStyle"), type, kv.getStr("field"), BaseUtil.getUser().getUserId());
        if (adminFleldStyle != null) {
            adminFleldStyle.setStyle(new BigDecimal(kv.getStr("width")).intValue());
            adminFleldStyle.update();
        } else {
            adminFleldStyle = new AdminFieldStyle();
            adminFleldStyle.setType(type);
            adminFleldStyle.setCreateTime(new Date());
            adminFleldStyle.setStyle(new BigDecimal(kv.getStr("width")).intValue());
            adminFleldStyle.setFieldName(kv.getStr("field"));
            adminFleldStyle.setUserId(BaseUtil.getUser().getUserId());
            adminFleldStyle.save();
        }
        return R.ok().put("data", "编辑成功");
    }

    public List<AdminFieldStyle> queryFieldStyle(String type) {
        Long userId = BaseUtil.getUser().getUserId();
        return AdminFieldStyle.dao.find("select * from 72crm_admin_field_style where type = ? and user_id = ?", type, userId);
    }

    /**
     * @author wyq
     * 查询客户管理列表页字段
     */
    @Before(Tx.class)
    public List<Record> queryListHead(AdminFieldSort adminFieldSort) {
        //查看userid是否存在于顺序表，没有则插入
        Long userId = BaseUtil.getUser().getUserId();
        Integer number = Db.queryInt("select count(*) from 72crm_admin_field_sort where user_id = ? and label = ?", userId, adminFieldSort.getLabel());
        if (0 == number) {
            List<Record> fieldList;
            if (adminFieldSort.getLabel() == CrmEnum.CRM_CUSTOMER_POOL.getType()){
                fieldList = list(CrmEnum.CRM_CUSTOMER.getType());
            }else {
                fieldList = list(adminFieldSort.getLabel());
            }
            List<AdminFieldSort> sortList = new LinkedList<>();
            FieldUtil fieldUtil = new FieldUtil(sortList, userId, adminFieldSort.getLabel());
            if (null != fieldList) {
                for (Record record : fieldList) {
                    fieldUtil.add(record.getStr("field_name"), record.getStr("name"), record.getInt("type"), record.getInt("field_id"));
                }
            }
            if (CrmEnum.CRM_CUSTOMER.getType() == adminFieldSort.getLabel()){
                fieldUtil.add("dealStatus", "成交状态", 3)
                        .add("poolDay", "距进入公海客户天数", 5)
                        .add("lastTime", "最后跟进时间", 4)
                        .add("lastContent","最后跟进记录",1);
            }else if (CrmEnum.CRM_BUSINESS.getType() == adminFieldSort.getLabel()){
                fieldUtil.add("typeName", "商机状态组", 3).add("statusName", "商机阶段", 3);
            }else if (CrmEnum.CRM_CONTRACT.getType() == adminFieldSort.getLabel()){
                fieldUtil.add("receivedMoney","已收款金额",6).add("unreceivedMoney","未收款金额",6);
            }else if (CrmEnum.CRM_RECEIVABLES.getType() == adminFieldSort.getLabel()){
                fieldUtil.add("contractMoney","合同金额",6);
            }else if (CrmEnum.CRM_CUSTOMER_POOL.getType() == adminFieldSort.getLabel()){
                fieldUtil.add("dealStatus","成交状态",3)
                        .add("lastContent","最后跟进记录",1);
            }else if(CrmEnum.CRM_LEADS.getType() == adminFieldSort.getLabel()){
                fieldUtil.add("lastContent","最后跟进记录",1);
            }
            fieldUtil.add("updateTime", "更新时间",4).add("createTime", "创建时间",4)
                    .add("ownerUserName", "负责人",1).add("createUserName", "创建人",1);
            fieldUtil.getAdminFieldSortList().forEach(fieldSort -> {
                String fieldName = StrUtil.toCamelCase(fieldSort.getFieldName());
                fieldSort.setFieldName(fieldName);
                if ("customerId".equals(fieldSort.getFieldName())){
                    fieldSort.setFieldName("customerName");
                } else if ("categoryId".equals(fieldSort.getFieldName())){
                    fieldSort.setFieldName("categoryName");
                } else if ("contactsId".equals(fieldSort.getFieldName())){
                    fieldSort.setFieldName("contactsName");
                } else if ("companyUserId".equals(fieldSort.getFieldName())){
                    fieldSort.setFieldName("companyUserName");
                } else if ("businessId".equals(fieldSort.getFieldName())){
                    fieldSort.setFieldName("businessName");
                } else if ("contractId".equals(fieldSort.getFieldName())){
                    fieldSort.setFieldName("contractNum");
                } else if ("planId".equals(fieldSort.getFieldName())){
                    fieldSort.setFieldName("planNum");
                }
            });
            sortList = fieldUtil.getAdminFieldSortList();
            for (int i = 0; i < sortList.size(); i++) {
                AdminFieldSort newUserFieldSort = sortList.get(i);
                newUserFieldSort.setSort(i).save();
            }
        }
        List<Record> recordList = Db.findByCache("field", "listHead:" + adminFieldSort.getLabel() + userId, Db.getSql("admin.field.queryListHead"), adminFieldSort.getLabel(), userId);
        recordToFormType(recordList);
        return recordList;
    }

    /**
     * @author wyq
     * 查询字段排序隐藏设置
     */
    @Before(Tx.class)
    public R queryFieldConfig(AdminFieldSort adminFieldSort) {
        Long userId = BaseUtil.getUser().getUserId();
        //查出自定义字段，查看顺序表是否存在该字段，没有则插入，设为隐藏
        List<Record> fieldList = customFieldList(adminFieldSort.getLabel());
        for (Record record : fieldList) {
            String fieldName = record.getStr("name");
            Integer number = Db.queryInt("select count(*) as number from 72crm_admin_field_sort where user_id = ? and label = ? and field_name = ?", userId, adminFieldSort.getLabel(), fieldName);
            if (number.equals(0)) {
                AdminFieldSort newField = new AdminFieldSort();
                newField.setFieldName(fieldName).setName(fieldName).setLabel(adminFieldSort.getLabel()).setIsHide(1).setUserId(userId).setSort(1).setType(record.getInt("type"));
                newField.save();
            }
        }
        List<Record> noHideList = Db.find(Db.getSql("admin.field.queryFieldConfig"), 0, adminFieldSort.getLabel(), userId);
        List<Record> hideList = Db.find(Db.getSql("admin.field.queryFieldConfig"), 1, adminFieldSort.getLabel(), userId);
        return R.ok().put("data", Kv.by("value", noHideList).set("hide_value", hideList));
    }

    /**
     * @author wyq
     * 客户管理列表页字段是否隐藏
     */
    @Before(Tx.class)
    public R fieldConfig(AdminFieldSort adminFieldSort) {
        Long userId = BaseUtil.getUser().getUserId();
        String[] sortArr = adminFieldSort.getNoHideIds().split(",");
        if (sortArr.length < 2) {
            return R.error("至少显示2列");
        }
        for (int i = 0; i < sortArr.length; i++) {
            Db.update(Db.getSql("admin.field.sort"), i + 1, adminFieldSort.getLabel(), userId, sortArr[i]);
        }
        if (null != adminFieldSort.getHideIds()) {
            String[] hideIdsArr = adminFieldSort.getHideIds().split(",");
            Db.update(Db.getSqlPara("admin.field.isHide", Kv.by("ids", hideIdsArr).set("label", adminFieldSort.getLabel()).set("userId", userId)));
        }
        CaffeineCache.ME.remove("field", "listHead:" + adminFieldSort.getLabel() + userId);
        return R.ok();
    }


    /**
     * 自定义字段人员和部门转换
     * @author hmb
     * @param recordList  自定义字段列表
     * @param isDetail   1 value返回name字符串 2 value返回id数组字符串
     */
    public void transferFieldList(List<Record> recordList, Integer isDetail){
        recordList.forEach(record -> {
            Integer dataType = record.getInt("type");
            if(isDetail == 2){
                if(FormTypeEnum.USER.getType() == dataType){
                    if(StrUtil.isNotEmpty(record.getStr("value"))){
                        record.set("value",TagUtil.toSet(record.getStr("value")));
                    }
                }else if (FormTypeEnum.STRUCTURE.getType() == dataType) {
                    if(StrUtil.isNotEmpty(record.getStr("value"))){
                        record.set("value", TagUtil.toSet(record.getStr("value")));
                    }
                }
            }else {
                if(FormTypeEnum.USER.getType() == dataType){
                    if(StrUtil.isNotEmpty(record.getStr("value"))){
                        record.set("value",Db.queryStr("select group_concat(realname) from `72crm_admin_user` where user_id in ("+record.getStr("value")+")"));
                    }
                }else if (FormTypeEnum.STRUCTURE.getType() == dataType) {
                    if(StrUtil.isNotEmpty(record.getStr("value"))){
                        record.set("value",Db.queryStr("select group_concat(name) from `72crm_admin_dept` where dept_id in ("+record.getStr("value")+")"));
                    }
                }
            }
            if(dataType == FormTypeEnum.FILE.getType()){
                if(StrUtil.isNotEmpty(record.getStr("value"))){
                    record.set("value",Db.find("select file_id, name, size, create_user_id, create_time, file_path, file_type from 72crm_admin_file where batch_id = ?",record.getStr("value")));
                }
            }
        });
    }
}
