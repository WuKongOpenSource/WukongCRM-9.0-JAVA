package com.kakarote.crm9.erp.admin.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import com.kakarote.crm9.common.config.cache.CaffeineCache;
import com.kakarote.crm9.common.config.cache.RedisCache;
import com.kakarote.crm9.erp.admin.entity.AdminField;
import com.kakarote.crm9.erp.admin.entity.AdminFieldSort;
import com.kakarote.crm9.erp.admin.entity.AdminFieldStyle;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.FieldUtil;
import com.kakarote.crm9.utils.R;
import com.jfinal.aop.Before;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;

import java.util.*;
import java.util.stream.Collectors;


public class AdminFieldService {

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
        Map<String,List<AdminField>> collect = adminFields.stream().map(adminField -> TypeUtils.castToJavaBean(adminField, AdminField.class)).collect(Collectors.groupingBy(AdminField::getName));
        for (Map.Entry<String,List<AdminField>> entry : collect.entrySet()) {
            if(entry.getValue().size() > 1){
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
            field.setBatchId("");
            if (field.getFieldId() != null) {
                arr.add(field.getFieldId());
            }
        });
        List<AdminField> fieldSorts = AdminField.dao.find("select name from 72crm_admin_field where label = ? and parent_id = 0", label);
        List<String> nameList = fieldSorts.stream().map(AdminField::getName).collect(Collectors.toList());
        if (arr.size() > 0) {
            SqlPara sql = Db.getSqlPara("admin.field.deleteByChooseId", Kv.by("ids", arr).set("label", label).set("categoryId", categoryId));
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
            if (label == 10) {
                entity.setExamineCategoryId(jsonObject.getInteger("categoryId"));
            }
            entity.setSorting(i);
            entity.set("label", label);
            if (entity.getFieldId() != null) {
                entity.update();
                Db.update(Db.getSqlPara("admin.field.updateFieldByParentId", entity));
                Db.update(Db.getSqlPara("admin.field.updateFieldSortName", entity));
            } else {
                entity.save();
            }
            fieldList.add(entity.getName());
        }
        createView(label);
        nameList.removeAll(fieldList);
        if (nameList.size() != 0) {
            Db.update(Db.getSqlPara("admin.field.deleteFieldSort", Kv.by("label", label).set("names", nameList)));
        }
        CaffeineCache.ME.removeAll("field");
        return R.ok();
    }

    public R verify(Kv kv) {
        SqlPara sqlPara = Db.getSqlPara("admin.field.queryFieldIsExist", kv);
        return Db.queryInt(sqlPara.getSql(), sqlPara.getPara()) > 0 ? R.error("参数校验错误").put("error", kv.getStr("name") + "：参数唯一") : R.ok();
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
        Db.deleteById("72crm_admin_field", "batch_id", batchId);
        array.forEach(obj -> {
            AdminField field = TypeUtils.castToJavaBean(obj, AdminField.class);
            field.setParentId(field.getFieldId());
            field.setFieldId(null);
            field.setBatchId(batchId);
            field.save();
        });
        return true;
    }

    /**
     * 保存自定义字段
     *
     * @param array   参数对象
     * @param batchId 批次ID
     * @return 操作结果
     */
    public boolean save(List<AdminField> array, String batchId) {
        if (array == null || StrUtil.isEmpty(batchId)) {
            return false;
        }
        Db.deleteById("72crm_admin_field", "batch_id", batchId);
        array.forEach(field -> {
            field.setParentId(field.getFieldId());
            field.setFieldId(null);
            field.setBatchId(batchId);
            field.save();
        });
        return true;
    }

    private synchronized void createView(Integer label) {
        List<Record> fieldNameList = Db.find("select  name,type from 72crm_admin_field WHERE batch_id is null and label=? ORDER BY sorting asc", label);
        StringBuilder sql = new StringBuilder();
        fieldNameList.forEach(record -> {
            String name = record.getStr("name");
            Integer type = record.getInt("type");
            if (type == 10) {
                sql.append(",(select GROUP_CONCAT(realname) from 72crm_admin_user where FIND_IN_SET(user_id,IFNULL((SELECT value FROM 72crm_admin_field WHERE name='").append(name).append("' and batch_id=a.batch_id limit 1),0))) as '").append(name).append("'");
            } else if (type == 12) {
                sql.append(",(select GROUP_CONCAT(name) from 72crm_admin_dept where FIND_IN_SET(dept_id,IFNULL((SELECT value FROM 72crm_admin_field WHERE name='").append(name).append("' and batch_id=a.batch_id limit 1),0))) as '").append(name).append("'");
            } else {
                sql.append(",(SELECT value FROM 72crm_admin_field WHERE name='").append(name).append("' and batch_id=a.batch_id limit 1) as '").append(name).append("'");
            }
        });
        String create = "";
        switch (label) {
            case 1:
                create = Db.getSql("admin.field.leadsview").replace("?", sql);
                break;
            case 2:
                create = Db.getSql("admin.field.customerview").replace("?", sql);
                break;
            case 3:
                create = Db.getSql("admin.field.contactsview").replace("?", sql);
                break;
            case 4:
                create = Db.getSql("admin.field.productview").replace("?", sql);
                break;
            case 5:
                create = Db.getSql("admin.field.businessview").replace("?", sql);
                break;
            case 6:
                create = Db.getSql("admin.field.contractview").replace("?", sql);
                break;
            case 7:
                create = Db.getSql("admin.field.receivablesview").replace("?", sql);
                break;
            default:
                break;
        }
        if (StrUtil.isNotBlank(create)) {
            Db.update(create);
        }
    }

    public List<AdminField> queryFieldsByBatchId(String batchId, String... name) {
        if (StrUtil.isEmpty(batchId)) {
            return new ArrayList<>();
        }
        return AdminField.dao.find(AdminField.dao.getSqlPara("admin.field.queryFieldsByBatchId", Kv.by("batchId", batchId).set("names", name)));
    }
    public List<Record> queryByBatchId(String batchId,Integer label) {
        if (StrUtil.isEmpty(batchId)) {
            return new ArrayList<>();
        }
        List<Record> recordList = Db.find(AdminField.dao.getSqlPara("admin.field.queryFieldsByBatchId", Kv.by("batchId", batchId).set("label",label)));
        recordList.forEach(record -> {
            if (record.getInt("type") == 10){
                if(StrUtil.isNotEmpty(record.getStr("value"))){
                    List<Record> userList = Db.find("select user_id,realname from 72crm_admin_user where user_id in ("+record.getStr("value")+")");
                    record.set("value",userList);
                }else {
                    record.set("value",new ArrayList<>());
                }
                record.set("default_value", new ArrayList<>(0));
            } else if (record.getInt("type") == 12){
                if(StrUtil.isNotEmpty(record.getStr("value"))){
                    List<Record> deptList = Db.find("select dept_id,name from 72crm_admin_dept where dept_id in ("+record.getStr("value")+")");
                    record.set("value",deptList);
                }else {
                    record.set("value",new ArrayList<>());
                }
                record.set("default_value", new ArrayList<>(0));
            }
        });
        recordToFormType(recordList);
        return recordList;
    }
    public List<Record> queryByBatchId(String batchId) {
        return queryByBatchId(batchId,null);
    }

    public R queryFields() {
        List<Record> records = Db.find(Db.getSqlPara("admin.field.queryFields"));
        return R.ok().put("data", records);
    }

    private void recordToFormType(List<Record> recordList) {
        for (Record record : recordList) {
            Integer dataType = record.getInt("type");
            if (1 == dataType) {
                record.set("formType", "text");
            } else if (2 == dataType) {
                record.set("formType", "textarea");
            } else if (3 == dataType) {
                record.set("formType", "select");
            } else if (4 == dataType) {
                record.set("formType", "date");
            } else if (5 == dataType) {
                record.set("formType", "number");
            } else if (6 == dataType) {
                record.set("formType", "floatnumber");
            } else if (7 == dataType) {
                record.set("formType", "mobile");
            } else if (8 == dataType) {
                record.set("formType", "file");
            } else if (9 == dataType) {
                record.set("formType", "checkbox");
                recordValueToArray(record);
            } else if (10 == dataType) {
                record.set("formType", "user");
                record.set("default_value", new ArrayList<>(0));
                //recordValueToArray(record);
            } else if (12 == dataType) {
                record.set("formType", "structure");
                record.set("default_value", new ArrayList<>(0));
                //recordValueToArray(record);
            } else if (13 == dataType) {
                record.set("formType", "datetime");
            } else if (14 == dataType) {
                record.set("formType", "email");
            }
            if (3 == dataType || 9 == dataType) {
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

    public List<Record> list(String label) {
        return list(label, null);
    }

    public List<Record> list(String label, String categoryId) {
        List<Record> recordList = Db.find(Db.getSqlPara("admin.field.list", Kv.by("label", label).set("categoryId", categoryId)));
        recordToFormType(recordList);
        if (categoryId == null) {
            return recordList;
        }
        FieldUtil fieldUtil = new FieldUtil(recordList);
        String[] arr = new String[0];
        switch (categoryId) {
            case "1":
                fieldUtil.oaFieldAdd("content", "审批内容", "text", arr, 1, 0, "", "", 3)
                        .oaFieldAdd("remark", "备注", "textarea", arr, 0, 0, "", "", 3);
                break;
            case "2":
                fieldUtil.oaFieldAdd("type_id", "请假类型", "select", new String[]{"年假", "事假", "病假", "产假", "调休", "婚假", "丧假", "其他"}, 1, 0, "", "", 3)
                        .oaFieldAdd("content", "审批内容", "text", arr, 1, 0, "", "", 3)
                        .oaFieldAdd("start_time", "开始时间", "datetime", arr, 1, 0, "", "", 3)
                        .oaFieldAdd("end_time", "结束时间", "datetime", arr, 1, 0, "", "", 3)
                        .oaFieldAdd("duration", "时长(天)", "floatnumber", arr, 1, 0, "", "", 3)
                        .oaFieldAdd("remark", "备注", "textarea", arr, 0, 0, "", "", 3);
                break;
            case "3":
                fieldUtil.oaFieldAdd("content", "出差事由", "text", arr, 1, 0, "", "", 3)
                        .oaFieldAdd("remark", "备注", "textarea", arr, 0, 0, "", "", 3)
                        .oaFieldAdd("cause", "行程明细", "business_cause", arr, 1, 0, "", "", 3)
                        .oaFieldAdd("duration", "时长(天)", "floatnumber", arr, 1, 0, "", "", 3);
                break;
            case "4":
                fieldUtil.oaFieldAdd("content", "加班原因", "text", arr, 1, 0, "", "", 3)
                        .oaFieldAdd("start_time", "开始时间", "datetime", arr, 1, 0, "", "", 3)
                        .oaFieldAdd("end_time", "结束时间", "datetime", arr, 1, 0, "", "", 3)
                        .oaFieldAdd("duration", "加班总天数", "floatnumber", arr, 1, 0, "", "", 3)
                        .oaFieldAdd("remark", "备注", "textarea", arr, 0, 0, "", "", 3);
                break;
            case "5":
                fieldUtil.oaFieldAdd("content", "差旅事由", "text", arr, 1, 0, "", "", 3)
                        .oaFieldAdd("cause", "费用明细", "examine_cause", arr, 1, 0, "", "", 3)
                        .oaFieldAdd("money", "报销总金额", "floatnumber", arr, 1, 0, "", "", 3)
                        .oaFieldAdd("remark", "备注", "textarea", arr, 0, 0, "", "", 3);
                break;
            case "6":
                fieldUtil.oaFieldAdd("content", "借款事由", "text", arr, 1, 0, "", "", 3)
                        .oaFieldAdd("money", "借款金额（元）", "floatnumber", arr, 1, 0, "", "", 3)
                        .oaFieldAdd("remark", "备注", "textarea", arr, 0, 0, "", "", 3);
                break;
            default:
                List<Record> recordList1 = Db.find(Db.getSqlPara("admin.field.list1", Kv.by("label", label).set("categoryId", categoryId)));
                recordToFormType(recordList1);
                return recordList1;
        }
        return fieldUtil.getRecordList();
    }

    public R setFelidStyle(Kv kv) {
        int type;
        String types = kv.getStr("types");
        switch (types) {
            case "crm_leads":
                type = 1;
                break;
            case "crm_customer":
                type = 2;
                break;
            case "crm_contacts":
                type = 3;
                break;
            case "crm_product":
                type = 4;
                break;
            case "crm_business":
                type = 5;
                break;
            case "crm_contract":
                type = 6;
                break;
            case "crm_receivables":
                type = 7;
                break;
            case "crm_customer_pool":
                type = 8;
                break;
            default:
                type = 0;
                break;
        }
        AdminFieldStyle adminFleldStyle = AdminFieldStyle.dao.findFirst(AdminFieldStyle.dao.getSql("admin.field.queryFieldStyle"), type, kv.getStr("field"), BaseUtil.getUser().getUserId());
        if (adminFleldStyle != null) {
            adminFleldStyle.setStyle(Integer.valueOf(kv.getStr("width")));
            adminFleldStyle.update();
        } else {
            adminFleldStyle = new AdminFieldStyle();
            adminFleldStyle.setType(type);
            adminFleldStyle.setCreateTime(new Date());
            adminFleldStyle.setStyle(Integer.valueOf(kv.getStr("width")));
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
            List<Record> fieldList = list(adminFieldSort.getLabel().toString());
            List<AdminFieldSort> sortList = new LinkedList<>();
            FieldUtil fieldUtil = new FieldUtil(sortList, userId, adminFieldSort.getLabel());
            if (1 == adminFieldSort.getLabel()) {
                fieldUtil.add("leadsName", "线索名称").add("telephone", "电话").add("mobile", "手机")
                        .add("updateTime", "更新时间").add("createTime", "创建时间").add("ownerUserName", "负责人")
                        .add("address", "地址").add("nextTime", "下次联系时间").add("remark", "备注")
                        .add("createUserName", "创建人");
            } else if (2 == adminFieldSort.getLabel() || 8 == adminFieldSort.getLabel()) {
                fieldUtil.add("customerName", "客户名称").add("dealStatus", "成交状态").add("telephone", "电话")
                        .add("website", "网址").add("nextTime", "下次联系时间").add("createUserName", "创建人")
                        .add("updateTime", "更新时间").add("createTime", "创建时间").add("ownerUserName", "负责人")
                        .add("remark", "备注");
            } else if (3 == adminFieldSort.getLabel()) {
                fieldUtil.add("name", "姓名").add("customerName", "客户名称").add("mobile", "手机")
                        .add("telephone", "电话").add("email", "电子邮箱").add("post", "职务")
                        .add("address", "地址").add("nextTime", "下次联系时间").add("remark", "备注")
                        .add("createUserName", "创建人").add("updateTime", "更新时间").add("createTime", "创建时间")
                        .add("ownerUserName", "负责人");
            } else if (4 == adminFieldSort.getLabel()) {
                fieldUtil.add("name", "产品名称").add("num", "产品编码").add("categoryName", "产品类别")
                        .add("price", "标准价格").add("description", "产品描述").add("createUserName", "创建人")
                        .add("updateTime", "更新时间").add("createTime", "创建时间").add("ownerUserName", "负责人");
            } else if (5 == adminFieldSort.getLabel()) {
                fieldUtil.add("businessName", "商机名称").add("customerName", "客户名称").add("typeName", "商机状态组")
                        .add("statusName", "商机阶段").add("money", "商机金额").add("dealDate", "预计成交日期")
                        .add("remark", "备注").add("createUserName", "创建人").add("updateTime", "更新时间")
                        .add("createTime", "创建时间").add("createUserName", "创建人");
            } else if (6 == adminFieldSort.getLabel()) {
                fieldUtil.add("num", "合同编号").add("name", "合同名称").add("customerName", "客户名称")
                        .add("businessName", "商机名称").add("orderDate", "下单时间").add("money", "合同金额")
                        .add("startTime", "合同开始时间").add("endTime", "合同结束时间").add("contactsName", "客户签约人")
                        .add("companyUserName", "公司签约人").add("remark", "备注").add("createUserName", "创建人")
                        .add("updateTime", "更新时间").add("createTime", "创建时间").add("ownerUserName", "负责人");
            } else if (7 == adminFieldSort.getLabel()) {
                fieldUtil.add("number", "回款编号").add("customerName", "客户名称").add("contractNum", "合同编号")
                        .add("returnTime", "回款日期").add("money", "回款金额").add("planNum", "期数")
                        .add("remark", "备注").add("createUserName", "创建人").add("updateTime", "更新时间")
                        .add("createTime", "创建时间").add("ownerUserName", "负责人");
            } else {
                return null;
            }
            if (null != fieldList) {
                for (Record record : fieldList) {
                    fieldUtil.add(record.getStr("name"), record.getStr("name"),record.getInt("field_id"));
                }
            }
            sortList = fieldUtil.getAdminFieldSortList();
            for (int i = 0; i < sortList.size(); i++) {
                AdminFieldSort newUserFieldSort = sortList.get(i);
                newUserFieldSort.setSort(i).save();
            }
        }
        return Db.findByCache("field", "listHead:" + adminFieldSort.getLabel() + userId, Db.getSql("admin.field.queryListHead"), adminFieldSort.getLabel(), userId);
    }

    /**
     * @author wyq
     * 查询字段排序隐藏设置
     */
    @Before(Tx.class)
    public R queryFieldConfig(AdminFieldSort adminFieldSort) {
        Long userId = BaseUtil.getUser().getUserId();
        //查出自定义字段，查看顺序表是否存在该字段，没有则插入，设为隐藏
        List<Record> fieldList = list(adminFieldSort.getLabel().toString());
        for (Record record : fieldList) {
            String fieldName = record.getStr("name");
            Integer number = Db.queryInt("select count(*) as number from 72crm_admin_field_sort where user_id = ? and label = ? and field_name = ?", userId, adminFieldSort.getLabel(), fieldName);
            if (number.equals(0)) {
                AdminFieldSort newField = new AdminFieldSort();
                newField.setFieldName(fieldName).setName(fieldName).setLabel(adminFieldSort.getLabel()).setIsHide(1).setUserId(userId).setSort(1);
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
}
