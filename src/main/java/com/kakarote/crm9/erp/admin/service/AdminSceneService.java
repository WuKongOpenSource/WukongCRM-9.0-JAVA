package com.kakarote.crm9.erp.admin.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Aop;
import com.jfinal.json.Json;
import com.jfinal.plugin.activerecord.Page;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.erp.admin.entity.AdminScene;
import com.kakarote.crm9.erp.admin.entity.AdminSceneDefault;
import com.kakarote.crm9.erp.crm.service.CrmBusinessService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.FieldUtil;
import com.kakarote.crm9.utils.ParamsUtil;
import com.kakarote.crm9.utils.R;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class AdminSceneService {
    @Inject
    private AdminFieldService adminFieldService;

    @Inject
    private CrmBusinessService crmBusinessService;
    /**
     * @author wyq
     * 查询场景字段
     */
    public R queryField(Integer label) {
        List<Record> recordList = new LinkedList<>();
        FieldUtil fieldUtil = new FieldUtil(recordList);
        String[] settingArr = new String[]{};
        if (1 == label) {
            fieldUtil.add("leads_name", "线索名称", "text", settingArr)
                    .add("telephone", "电话", "text", settingArr)
                    .add("mobile", "手机", "mobile", settingArr)
                    .add("address", "地址", "text", settingArr)
                    .add("next_time", "下次联系时间", "datetime", settingArr)
                    .add("remark", "备注", "text", settingArr)
                    .add("owner_user_id", "负责人", "user", settingArr)
                    .add("create_user_id", "创建人", "user", settingArr)
                    .add("update_time", "更新时间", "datetime", settingArr)
                    .add("create_time", "创建时间", "datetime", settingArr);
        } else if (2 == label) {
            String[] dealStatusArr = new String[]{"未成交", "已成交"};
            fieldUtil.add("customer_name", "客户名称", "text", settingArr)
                    .add("mobile", "手机", "text", settingArr)
                    .add("telephone", "电话", "text", settingArr)
                    .add("website", "网址", "text", settingArr)
                    .add("next_time", "下次联系时间", "datetime", settingArr)
                    .add("remark", "备注", "text", settingArr)
                    .add("deal_status", "成交状态", "select", dealStatusArr)
                    .add("owner_user_id", "负责人", "user", settingArr)
                    .add("create_user_id", "创建人", "user", settingArr)
                    .add("update_time", "更新时间", "datetime", settingArr)
                    .add("create_time", "创建时间", "datetime", settingArr)
                    .add("address", "地区定位", "map_address", settingArr);
        } else if (3 == label) {
            fieldUtil.add("name", "姓名", "text", settingArr)
                    .add("customer_name", "客户名称", "customer", settingArr)
                    .add("mobile", "手机", "mobile", settingArr)
                    .add("telephone", "电话", "text", settingArr)
                    .add("email", "电子邮箱", "email", settingArr)
                    .add("post", "职务", "text", settingArr)
                    .add("address", "地址", "text", settingArr)
                    .add("next_time", "下次联系时间", "datetime", settingArr)
                    .add("remark", "备注", "text", settingArr)
                    .add("owner_user_id", "负责人", "user", settingArr)
                    .add("create_user_id", "创建人", "user", settingArr)
                    .add("update_time", "更新时间", "datetime", settingArr)
                    .add("create_time", "创建时间", "datetime", settingArr);
        } else if (4 == label) {
            fieldUtil.add("name", "产品名称", "text", settingArr)
                    .add("category_id", "产品类别", "category", settingArr)
                    .add("num", "产品编码", "number", settingArr)
                    .add("price", "价格", "floatnumber", settingArr)
                    .add("description", "产品描述", "text", settingArr)
                    .add("owner_user_id", "负责人", "user", settingArr)
                    .add("create_user_id", "创建人", "user", settingArr)
                    .add("update_time", "更新时间", "datetime", settingArr)
                    .add("create_time", "创建时间", "datetime", settingArr);
        } else if (5 == label) {
            fieldUtil.add("business_name", "商机名称", "text", settingArr)
                    .add("customer_name", "客户名称", "customer", settingArr)
                    .add("type_id", "商机状态组", "business_type", crmBusinessService.queryBusinessStatusOptions("condition"))
                    .add("money", "商机金额", "floatnumber", settingArr)
                    .add("deal_date", "预计成交日期", "datetime", settingArr)
                    .add("remark", "备注", "text", settingArr)
                    .add("product", "产品", "product", settingArr)
                    .add("owner_user_id", "负责人", "user", settingArr)
                    .add("create_user_id", "创建人", "user", settingArr)
                    .add("update_time", "更新时间", "datetime", settingArr)
                    .add("create_time", "创建时间", "datetime", settingArr);
        } else if (6 == label) {
            List<Map<String,Object>> checkList=new ArrayList<>();
            checkList.add(new JSONObject().fluentPut("name","待审核").fluentPut("value",0));
            checkList.add(new JSONObject().fluentPut("name","审核中").fluentPut("value",1));
            checkList.add(new JSONObject().fluentPut("name","审核通过").fluentPut("value",2));
            checkList.add(new JSONObject().fluentPut("name","审核未通过").fluentPut("value",3));
            checkList.add(new JSONObject().fluentPut("name","已撤回").fluentPut("value",4));
            fieldUtil.add("num", "合同编号", "number", settingArr)
                    .add("name", "合同名称", "text", settingArr)
                    .add("check_status", "审核状态", "checkStatus", checkList)
                    .add("customer_name", "客户名称", "customer", settingArr)
                    .add("business_name", "商机名称", "business", settingArr)
                    .add("order_date", "下单时间", "date", settingArr)
                    .add("money", "合同金额", "floatnumber", settingArr)
                    .add("start_time", "合同开始时间", "datetime", settingArr)
                    .add("end_time", "合同结束时间", "datetime", settingArr)
                    .add("contacts_name", "客户签约人", "contacts", settingArr)
                    .add("company_user_id", "公司签约人", "user", settingArr)
                    .add("remark", "备注", "number", settingArr)
                    .add("product", "产品", "product", settingArr)
                    .add("owner_user_id", "负责人", "user", settingArr)
                    .add("create_user_id", "创建人", "user", settingArr)
                    .add("update_time", "更新时间", "datetime", settingArr)
                    .add("create_time", "创建时间", "datetime", settingArr);
        } else if (7 == label) {
            List<Map<String,Object>> checkList=new ArrayList<>();
            checkList.add(new JSONObject().fluentPut("name","待审核").fluentPut("value",0));
            checkList.add(new JSONObject().fluentPut("name","审核中").fluentPut("value",1));
            checkList.add(new JSONObject().fluentPut("name","审核通过").fluentPut("value",2));
            checkList.add(new JSONObject().fluentPut("name","审核未通过").fluentPut("value",3));
            checkList.add(new JSONObject().fluentPut("name","已撤回").fluentPut("value",4));
            fieldUtil.add("number", "回款编号", "number", settingArr)
                    .add("check_status", "审核状态", "checkStatus", checkList)
                    .add("customer_name", "客户名称", "customer", settingArr)
                    .add("contract_num", "合同编号", "contract", settingArr)
                    .add("return_time", "回款日期", "date", settingArr)
                    .add("money", "回款金额", "floatnumber", settingArr)
                    .add("remark", "备注", "textarea", settingArr)
                    .add("owner_user_id", "负责人", "user", settingArr)
                    .add("create_user_id", "创建人", "user", settingArr)
                    .add("update_time", "更新时间", "datetime", settingArr)
                    .add("create_time", "创建时间", "datetime", settingArr);
        } else {
            return R.error("场景label不符合要求！");
        }
        recordList = fieldUtil.getRecordList();
        List<Record> records = adminFieldService.customFieldList(label.toString());
        if (recordList != null && records != null) {
            for (Record r : records){
                r.set("field_name",r.getStr("name"));
            }
            recordList.addAll(records);
        }
        return R.ok().put("data", recordList);
    }

    /**
     * @author wyq
     * 增加场景
     */
    @Before(Tx.class)
    public R addScene(AdminScene adminScene) {
        Long userId = BaseUtil.getUser().getUserId();
        adminScene.setIsHide(0).setSort(99999).setIsSystem(0).setCreateTime(DateUtil.date()).setUserId(userId);
        adminScene.save();
        if (1 == adminScene.getIsDefault()) {
            AdminSceneDefault adminSceneDefault = new AdminSceneDefault();
            adminSceneDefault.setSceneId(adminScene.getSceneId()).setType(adminScene.getType()).setUserId(userId).save();
        }
        return R.ok();
    }

    /**
     * @author wyq
     * 更新场景
     */
    @Before(Tx.class)
    public R updateScene(AdminScene adminScene) {
        Long userId = BaseUtil.getUser().getUserId();
        AdminScene oldAdminScene = AdminScene.dao.findById(adminScene.getSceneId());
        if (1 == adminScene.getIsDefault()) {
            Db.update("update 72crm_admin_scene_default set scene_id = ? where user_id = ? and type = ?", adminScene.getSceneId(), userId, oldAdminScene.getType());
        }
        adminScene.setUserId(userId).setType(oldAdminScene.getType()).setSort(oldAdminScene.getSort()).setIsSystem(oldAdminScene.getIsSystem()).setUpdateTime(DateUtil.date());
        return R.isSuccess(adminScene.update());
    }

    /**
     * @author wyq
     * 设置默认场景
     */
    @Before(Tx.class)
    public R setDefaultScene(Integer sceneId) {
        Long userId = BaseUtil.getUser().getUserId();
        AdminScene oldAdminScene = AdminScene.dao.findById(sceneId);
        Db.delete("delete from 72crm_admin_scene_default where user_id = ? and type = ?", userId, oldAdminScene.getType());
        AdminSceneDefault adminSceneDefault = new AdminSceneDefault();
        return adminSceneDefault.setSceneId(sceneId).setType(oldAdminScene.getType()).setUserId(userId).save() ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 删除场景
     */
    @Before(Tx.class)
    public R deleteScene(AdminScene adminScene) {
        if (1 == AdminScene.dao.findById(adminScene.getSceneId()).getIsSystem()) {
            return R.error("系统场景不能删除");
        }
        return AdminScene.dao.deleteById(adminScene.getSceneId()) ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 查询场景
     */
    @Before(Tx.class)
    public R queryScene(Integer type) {
        Long userId = BaseUtil.getUser().getUserId();
        //查询userId下是否有系统场景，没有则插入
        Integer number = Db.queryInt(Db.getSql("admin.scene.querySystemNumber"), type, userId);
        if (number.equals(0)) {
            AdminScene systemScene = new AdminScene();
            systemScene.setUserId(userId).setSort(0).setData("").setIsHide(0).setIsSystem(1).setCreateTime(DateUtil.date()).setType(type);
            JSONObject ownerObject = new JSONObject();
            ownerObject.fluentPut("owner_user_id", new JSONObject().fluentPut("name", "owner_user_id").fluentPut("condition", "is").fluentPut("value", userId));
            JSONObject subOwnerObject = new JSONObject();
            subOwnerObject.fluentPut("owner_user_id", new JSONObject().fluentPut("name", "owner_user_id").fluentPut("condition", "in").fluentPut("value", getSubUserId(userId.intValue(),BaseConstant.AUTH_DATA_RECURSION_NUM).substring(1)));
            if (1 == type) {
                systemScene.setName("全部线索").setData(new JSONObject().fluentPut("is_transform", new JSONObject().fluentPut("name", "is_transform").fluentPut("condition", "is").fluentPut("value", 0)).toString()).save();
                ownerObject.fluentPut("owner_user_id", new JSONObject().fluentPut("name", "owner_user_id").fluentPut("condition", "is").fluentPut("value", userId)).fluentPut("is_transform", new JSONObject().fluentPut("name", "is_transform").fluentPut("condition", "is").fluentPut("value", 0));
                systemScene.setSceneId(null).setName("我负责的线索").setData(ownerObject.toString()).save();
                subOwnerObject.fluentPut("owner_user_id", new JSONObject().fluentPut("name", "owner_user_id").fluentPut("condition", "in").fluentPut("value", getSubUserId(userId.intValue(),BaseConstant.AUTH_DATA_RECURSION_NUM).substring(1))).fluentPut("is_transform", new JSONObject().fluentPut("name", "is_transform").fluentPut("condition", "is").fluentPut("value", 0));
                systemScene.setSceneId(null).setName("下属负责的线索").setData(subOwnerObject.toString()).save();
                JSONObject jsonObject = new JSONObject();
                jsonObject.fluentPut("is_transform", new JSONObject().fluentPut("name", "is_transform").fluentPut("condition", "is").fluentPut("value", "1"));
                systemScene.setSceneId(null).setName("已转化的线索").setData(jsonObject.toString()).setBydata("transform").save();
            } else if (2 == type) {
                systemScene.setName("全部客户").save();
                systemScene.setSceneId(null).setName("我负责的客户").setData(ownerObject.toString()).save();
                systemScene.setSceneId(null).setName("下属负责的客户").setData(subOwnerObject.toString()).save();
                JSONObject jsonObject = new JSONObject();
                jsonObject.fluentPut("ro_user_id", new JSONObject().fluentPut("name", "ro_user_id").fluentPut("condition", "takePart").fluentPut("value", userId));
                systemScene.setSceneId(null).setName("我参与的客户").setData(jsonObject.toString()).save();
            } else if (3 == type) {
                systemScene.setName("全部联系人").save();
                systemScene.setSceneId(null).setName("我负责的联系人").setData(ownerObject.toString()).save();
                systemScene.setSceneId(null).setName("下属负责的联系人").setData(subOwnerObject.toString()).save();
            } else if (4 == type) {
                systemScene.setName("上架的产品").setData(new JSONObject().fluentPut("是否上下架",new JSONObject().fluentPut("name","是否上下架").fluentPut("condition","is").fluentPut("value","上架")).toString()).save();
                JSONObject jsonObject = new JSONObject();
                jsonObject.fluentPut("是否上下架",new JSONObject().fluentPut("name","是否上下架").fluentPut("condition","is").fluentPut("value","下架"));
                systemScene.setSceneId(null).setName("下架的产品").setData(jsonObject.toString()).save();
            } else if (5 == type) {
                systemScene.setName("全部商机").save();
                systemScene.setSceneId(null).setName("我负责的商机").setData(ownerObject.toString()).save();
                systemScene.setSceneId(null).setName("下属负责的商机").setData(subOwnerObject.toString()).save();
                JSONObject jsonObject = new JSONObject();
                jsonObject.fluentPut("ro_user_id", new JSONObject().fluentPut("name", "ro_user_id").fluentPut("condition", "takePart").fluentPut("value", userId));
                systemScene.setSceneId(null).setName("我参与的商机").setData(jsonObject.toString()).save();
            } else if (6 == type) {
                systemScene.setName("全部合同").save();
                systemScene.setSceneId(null).setName("我负责的合同").setData(ownerObject.toString()).save();
                systemScene.setSceneId(null).setName("下属负责的合同").setData(subOwnerObject.toString()).save();
                JSONObject jsonObject = new JSONObject();
                jsonObject.fluentPut("ro_user_id", new JSONObject().fluentPut("name", "ro_user_id").fluentPut("condition", "takePart").fluentPut("value", userId));
                systemScene.setSceneId(null).setName("我参与的合同").setData(jsonObject.toString()).save();
            } else if (7 == type) {
                systemScene.setName("全部回款").save();
                systemScene.setSceneId(null).setName("我负责的回款").setData(ownerObject.toString()).save();
                systemScene.setSceneId(null).setName("下属负责的回款").setData(subOwnerObject.toString()).save();
            }
        }
        return R.ok().put("data", Db.find(Db.getSql("admin.scene.queryScene"), type, userId));
    }

    /**
     * 递归查询下属id
     */
    public String getSubUserId(Integer userId,Integer deepness) {
        StringBuilder ids = new StringBuilder();
        if (deepness > 0){
            List<Long> list = Db.query("select user_id from 72crm_admin_user where parent_id = ?", userId);
            if (list != null && list.size() > 0) {
                for (Long l : list) {
                    ids.append(",").append(l).append(getSubUserId(l.intValue(),deepness -1));
                }
            }
        }
        return StrUtil.isNotEmpty(ids.toString()) ? ids.toString() : " ";
    }

    /**
     * @author wyq
     * 查询场景设置
     */
    public R querySceneConfig(AdminScene adminScene) {
        Long userId = BaseUtil.getUser().getUserId();
        List<Record> valueList = Db.find(Db.getSql("admin.scene.queryScene"), adminScene.getType(), userId);
        for (Record scene : valueList) {
            if (StrUtil.isNotEmpty(scene.getStr("data"))) {
                JSONObject jsonObject = JSON.parseObject(scene.getStr("data"));
                scene.set("data", jsonObject);
            }
        }
        List<Record> hideValueList = Db.find(Db.getSql("admin.scene.queryHideScene"), adminScene.getType(), userId);
        for (Record hideScene : hideValueList) {
            if (StrUtil.isNotEmpty(hideScene.getStr("data"))) {
                JSONObject jsonObject = JSON.parseObject(hideScene.getStr("data"));
                hideScene.set("data", jsonObject);
            }
        }
        return R.ok().put("data", Kv.by("value", valueList).set("hide_value", hideValueList));
    }

    /**
     * @author wyq
     * 设置场景
     */
    @Before(Tx.class)
    public R sceneConfig(AdminScene adminScene) {
        Long userId = BaseUtil.getUser().getUserId();
        String[] sortArr = adminScene.getNoHideIds().split(",");
        for (int i = 0; i < sortArr.length; i++) {
            Db.update(Db.getSql("admin.scene.sort"), i + 1, adminScene.getType(), userId, sortArr[i]);
        }
        if (null != adminScene.getHideIds()) {
            String[] hideIdsArr = adminScene.getHideIds().split(",");
            Record number = Db.findFirst(Db.getSqlPara("admin.scene.queryIsHideSystem",Kv.by("ids",hideIdsArr)));
            if (number.getInt("number") > 0){
                return R.error("系统场景不能隐藏");
            }
            Db.update(Db.getSqlPara("admin.scene.isHide", Kv.by("ids", hideIdsArr).set("type", adminScene.getType()).set("userId", userId)));
        }
        return R.ok();
    }

    public R filterConditionAndGetPageList(BasePageRequest basePageRequest){
        JSONObject jsonObject = basePageRequest.getJsonObject();
        Integer sceneId = jsonObject.getInteger("sceneId");
        JSONObject data = new JSONObject();
        if (sceneId != null && sceneId != 0){
            data = JSON.parseObject(AdminScene.dao.findById(sceneId).getData());
        }
        if (sceneId == null && jsonObject.getInteger("type") == 1){
            data = new JSONObject().fluentPut("is_transform",new JSONObject().fluentPut("name","is_transform").fluentPut("condition","is").fluentPut("value","0"));
        }
        if (sceneId == null && jsonObject.getInteger("type") == 4){
            data = new JSONObject().fluentPut("是否上下架",new JSONObject().fluentPut("name","是否上下架").fluentPut("condition","is").fluentPut("value","上架"));
        }
        if (jsonObject.getJSONObject("data") != null){
            if (data != null){
                jsonObject.getJSONObject("data").putAll(data);
            }
        }else {
            jsonObject.put("data",data);
        }
        basePageRequest.setJsonObject(jsonObject);
        return getCrmPageList(basePageRequest);
    }

    /**
     * @author wyq
     * Crm列表页查询
     */
    public R getCrmPageList(BasePageRequest basePageRequest) {
        Integer type = basePageRequest.getJsonObject().getInteger("type");
        String viewName;
        //操作地址
        String realm;
        switch (type) {
            case 1:
                viewName = "leadsview";
                realm="leads";
                break;
            case 2:
                viewName = "customerview";
                realm="customer";
                break;
            case 3:
                viewName = "contactsview";
                realm="contacts";
                break;
            case 4:
                viewName = "productview";
                realm="product";
                break;
            case 5:
                viewName = "businessview";
                realm="business";
                break;
            case 6:
                viewName = "contractview";
                realm="contract";
                break;
            case 7:
                viewName = "receivablesview";
                realm="receivables";
                break;
            case 8:
                viewName = "customerview";
                realm="customer";
                break;
            default:
                return R.error("type不符合要求");
        }
        JSONObject data = basePageRequest.getJsonObject().getJSONObject("data");
        List<JSONObject> jsonObjectList = new ArrayList<>();
        if (data != null) {
            data.forEach((k, v) -> jsonObjectList.add(JSON.parseObject(v.toString())));
        }
        StringBuilder conditions = new StringBuilder(" where 1=1");
        for (JSONObject jsonObject : jsonObjectList) {
            String condition = jsonObject.getString("condition");
            String value = jsonObject.getString("value");
            String name  = jsonObject.getString("name");
            if (!ParamsUtil.isValid(name)){
                return R.error("参数包含非法字段");
            }
            if (StrUtil.isNotEmpty(value)&&!ParamsUtil.isValid(value)){
                return R.error("参数包含非法字段");
            }
            if (StrUtil.isNotEmpty(jsonObject.getString("start"))&&!ParamsUtil.isValid(jsonObject.getString("start"))){
                return R.error("参数包含非法字段");
            }
            if (StrUtil.isNotEmpty(jsonObject.getString("end"))&&!ParamsUtil.isValid(jsonObject.getString("end"))){
                return R.error("参数包含非法字段");
            }
            String formType = jsonObject.getString("formType");
            if ("business_type".equals(formType)) {
                conditions.append(" and ").append(name).append(" = ").append(jsonObject.getInteger("typeId"));
                if (StrUtil.isNotEmpty(jsonObject.getString("statusId"))) {
                    if ("win".equals(jsonObject.getString("statusId"))){
                        conditions.append(" and is_end = 1");
                    }else if ("lose".equals(jsonObject.getString("statusId"))){
                        conditions.append(" and is_end = 2");
                    }else if ("invalid".equals(jsonObject.getString("statusId"))){
                        conditions.append(" and is_end = 3");
                    }else {
                        conditions.append(" and status_id = ").append(jsonObject.getString("statusId"));
                    }
                }
                continue;
            }else if ("map_address".equals(formType)){
                String address = value.substring(0,value.length()-1);
                conditions.append(" and ").append(name).append(" like '%").append(address).append("%'");
                continue;
            }
            if (StrUtil.isNotEmpty(value) || StrUtil.isNotEmpty(jsonObject.getString("start")) || StrUtil.isNotEmpty(jsonObject.getString("end"))) {
                if ("takePart".equals(condition)) {
                    conditions.append(" and (ro_user_id like '%,").append(value).append(",%' or rw_user_id like '%,").append(value).append(",%')");
                } else {
                    conditions.append(" and ").append(jsonObject.getString("name"));
                    if ("is".equals(condition)) {
                        conditions.append(" = '").append(value).append("'");
                    } else if ("isNot".equals(condition)) {
                        conditions.append(" != '").append(value).append("'");
                    } else if ("contains".equals(condition)) {
                        conditions.append(" like '%").append(value).append("%'");
                    } else if ("notContains".equals(condition)) {
                        conditions.append(" not like '%").append(value).append("%'");
                    } else if ("isNull".equals(condition)) {
                        conditions.append(" is null");
                    } else if ("isNotNull".equals(condition)) {
                        conditions.append(" is not null");
                    } else if ("gt".equals(condition)) {
                        conditions.append(" > ").append(value);
                    } else if ("egt".equals(condition)) {
                        conditions.append(" >= ").append(value);
                    } else if ("lt".equals(condition)) {
                        conditions.append(" < ").append(value);
                    } else if ("elt".equals(condition)) {
                        conditions.append(" <= ").append(value);
                    } else if ("in".equals(condition)) {
                        conditions.append(" in (").append(value).append(")");
                    }
                    if ("datetime".equals(formType)) {
                        conditions.append(" between '").append(jsonObject.getString("start")).append("' and '").append(jsonObject.getString("end")).append("'");
                    }
                    if ("date".equals(formType)) {
                        conditions.append(" between '").append(jsonObject.getString("startDate")).append("' and '").append(jsonObject.getString("endDate")).append("'");
                    }
                }
            }
        }
        String search = basePageRequest.getJsonObject().getString("search");
        if (StrUtil.isNotEmpty(search)) {
            if (!ParamsUtil.isValid(search)){
                return R.error("参数包含非法字段");
            }
            if (type == 1){
                conditions.append(" and (leads_name like '%").append(search).append("%' or telephone like '%")
                        .append(search).append("%' or mobile like '%").append(search).append("%')");
            }else if (type == 2 || type == 8){
                conditions.append(" and (customer_name like '%").append(search).append("%' or telephone like '%")
                        .append(search).append("%')");
            }else if (type == 3){
                conditions.append(" and (name like '%").append(search).append("%' or telephone like '%")
                        .append(search).append("%' or mobile like '%").append(search).append("%')");
            }else if (type == 4 || type == 6){
                conditions.append(" and (name like '%").append(search).append("%')");
            }else if (type == 5){
                conditions.append(" and (business_name like '%").append(search).append("%')");
            }else{
                conditions.append(" and (number like '%").append(search).append("%')");
            }
        }
        String camelField = basePageRequest.getJsonObject().getString("sortField");
        String sortField = StrUtil.toUnderlineCase(camelField);
        String orderNum = basePageRequest.getJsonObject().getString("order");
        if (StrUtil.isEmpty(sortField) || StrUtil.isEmpty(orderNum)){
            sortField = "update_time";
            orderNum = "desc";
        }else {
            if (!ParamsUtil.isValid(sortField)){
                return R.error("参数包含非法字段");
            }
            orderNum="2".equals(orderNum)?"asc":"desc";
        }
        if (2 == type) {
            conditions.append(" and owner_user_id is not null");
        } else if (8 == type) {
            conditions.append(" and owner_user_id is null");
        }
        Long userId=BaseUtil.getUserId();
        if(!type.equals(8)&&!type.equals(4)&& !BaseConstant.SUPER_ADMIN_USER_ID.equals(userId)){
            List<Long> longs= Aop.get(AdminUserService.class).queryUserByAuth(userId,realm);
            if(longs!=null&&longs.size()>0){
                conditions.append(" and owner_user_id in (").append(StrUtil.join(",", longs)).append(")");
                if(type.equals(2)||type.equals(6)||type.equals(5)){
                    conditions.append(" or ro_user_id like CONCAT('%,','").append(userId).append("',',%')").append(" or rw_user_id like CONCAT('%,','").append(userId).append("',',%')");
                }
            }
        }
        conditions.insert(0," from "+viewName);
        conditions.append(" order by ").append(viewName).append(".").append(sortField).append(" ").append(orderNum);
        if (StrUtil.isNotEmpty(basePageRequest.getJsonObject().getString("excel"))) {
            return R.ok().put("data", Db.find("select * " + conditions.toString()));
        }
        if (2 == type || 8 == type) {
            Integer configType = Db.queryInt("select status from 72crm_admin_config where name = 'customerPoolSetting'");
            if (1 == configType && 2 == type) {
                return R.ok().put("data", Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(),Db.getSql("admin.scene.getCustomerPageList"),conditions.toString()));
            } else {
                return R.ok().put("data", Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(),"select *,(select count(*) from 72crm_crm_business as a where a.customer_id = " + viewName + ".customer_id) as business_count",conditions.toString()));
            }

        }else if (6 == type){
            Record totalMoney = Db.findFirst("select SUM(money) as contractMoney,GROUP_CONCAT(contract_id) as contractIds "+conditions.toString());
            Page<Record> page = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(),"select *,IFNULL((select SUM(a.money) from 72crm_crm_receivables as a where a.contract_id = contractview.contract_id),0) as receivedMoney",conditions.toString());

            String receivedMoney = Db.queryStr("select SUM(money) from 72crm_crm_receivables where receivables_id in ("+totalMoney.getStr("contractIds")+")");
            JSONObject jsonObject = JSONObject.parseObject(Json.getJson().toJson(page),JSONObject.class);
            return R.ok().put("data",jsonObject.fluentPut("money",new JSONObject().fluentPut("contractMoney",totalMoney.getStr("contractMoney")!=null?totalMoney.getStr("contractMoney"):"0").fluentPut("receivedMoney",receivedMoney!=null?receivedMoney:"0")));
        }
        Page<Record> recordPage = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(),"select *",conditions.toString());
        if (type == 5){
            recordPage.getList().forEach(record -> {
                if (record.getInt("is_end") == 1){
                    record.set("status_name","赢单");
                }else if (record.getInt("is_end") == 2){
                    record.set("status_name","输单");
                }else if (record.getInt("is_end") == 3){
                    record.set("status_name","无效");
                }
            });
            setBusinessStatus(recordPage.getList());
        }
        return R.ok().put("data",recordPage);
    }

    public void setBusinessStatus(List<Record> list){
        list.forEach(record -> {
                    if (record.getInt("is_end") == 0){
                        Integer sortNum = Db.queryInt("select order_num from 72crm_crm_business_status where status_id = ?",record.getInt("status_id"));
                        int totalStatsNum = Db.queryInt("select count(*) from 72crm_crm_business_status where type_id = ?",record.getInt("type_id")) + 1;
                        if(sortNum == null){
                            sortNum = 0;
                        }
                        record.set("progressBar",sortNum+"/"+totalStatsNum);
                    }else if (record.getInt("is_end") == 1){
                        int totalStatsNum = Db.queryInt("select count(*) from 72crm_crm_business_status where type_id = ?",record.getInt("type_id")) + 1;
                        record.set("progressBar",totalStatsNum+"/"+totalStatsNum);
                    }else if (record.getInt("is_end") == 2){
                        int totalStatsNum = Db.queryInt("select count(*) from 72crm_crm_business_status where type_id = ?",record.getInt("type_id")) + 1;
                        record.set("progressBar","0/"+totalStatsNum);
                    }else if (record.getInt("is_end") == 3){
                        record.set("progressBar","0/0");
                    }
                });
    }
}
