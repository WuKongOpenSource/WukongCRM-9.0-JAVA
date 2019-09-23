package com.kakarote.crm9.erp.admin.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.entity.*;
import com.kakarote.crm9.erp.crm.entity.CrmContract;
import com.kakarote.crm9.erp.crm.entity.CrmReceivables;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.jfinal.aop.Before;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import java.util.ArrayList;
import java.util.List;

public class AdminExamineService {
    /**
     * 添加审批流程
     */
    @Before(Tx.class)
    public R saveExamine(JSONObject jsonObject) {
        AdminExamine adminExamine = jsonObject.toJavaObject(AdminExamine.class);
        List<Integer> deptIds = jsonObject.getJSONArray("deptIds").toJavaList(Integer.class);
        adminExamine.setDeptIds(getIds(deptIds));
        List<Integer> userIds = jsonObject.getJSONArray("userIds").toJavaList(Integer.class);
        adminExamine.setUserIds(getIds(userIds));
        Boolean flag;

        if (adminExamine.getExamineId() == null) {
            //添加
            AdminExamine examine = AdminExamine.dao.findFirst(Db.getSql("admin.examine.getExamineByCategoryType"), adminExamine.getCategoryType());
            if (examine != null) {
                //判断有未删除的审批流程，不能添加
                examine.setStatus(0);
                examine.setUpdateUserId(BaseUtil.getUser().getUserId());
                examine.setUpdateTime(DateUtil.date());
                examine.update();
            }
            adminExamine.setCreateUserId(BaseUtil.getUser().getUserId());
            adminExamine.setCreateTime(DateUtil.date());
            adminExamine.setUpdateUserId(BaseUtil.getUser().getUserId());
            adminExamine.setUpdateTime(DateUtil.date());
            adminExamine.setStatus(1);
            flag = adminExamine.save();

        } else {
            //更新 把旧的更新成删除状态 并添加一条新的
            AdminExamine examine = AdminExamine.dao.findById(adminExamine.getExamineId());
            examine.setStatus(2);
            examine.update();
            adminExamine.setCreateUserId(examine.getCreateUserId());
            adminExamine.setCreateTime(examine.getCreateTime());
            adminExamine.setUpdateUserId(BaseUtil.getUser().getUserId());
            adminExamine.setUpdateTime(DateUtil.date());
            adminExamine.setExamineId(null);
            adminExamine.setStatus(1);
            flag = adminExamine.save();


        }
        if (adminExamine.getExamineType() == 1) {
            //如果是固定审批，添加审批步骤
            int i = 1;
            List<JSONObject> jsonArray = jsonObject.getJSONArray("step").toJavaList(JSONObject.class);
            for (JSONObject e : jsonArray) {
                AdminExamineStep examineStep = e.toJavaObject(AdminExamineStep.class);
                examineStep.setExamineId(adminExamine.getExamineId());
                examineStep.setCreateTime(DateUtil.date());
                examineStep.setStepNum(i++);
                List<Integer> list = e.getJSONArray("checkUserId").toJavaList(Integer.class);
                examineStep.setCheckUserId(getIds(list));
                examineStep.save();
            }
        }
        return flag ? R.ok().put("status", 1) : R.error();

    }

    /**
     * 查询所有启用审批流程
     */
    public R queryAllExamine(BasePageRequest<AdminExamine> basePageRequest) {
        Page<Record> page = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("admin.examine.queryExaminePage"));
        page.getList().forEach(record -> {
            List<Record> stepList = Db.find(Db.getSql("admin.examineStep.queryExamineStepByExamineId"), record.getInt("examine_id"));
            if (stepList != null) {
                stepList.forEach(step -> {
                    if (step.getStr("check_user_id") != null && step.getStr("check_user_id").split(",").length > 0) {
                        List<Record> userList = Db.find(Db.getSqlPara("admin.user.queryByIds", Kv.by("ids", step.getStr("check_user_id").split(","))));
                        step.set("userList", userList);
                    } else {
                        step.set("userList", new ArrayList<>());
                    }
                });
                record.set("stepList", stepList);
            }
            if (record.getStr("user_ids") != null && record.getStr("user_ids").split(",").length > 0) {
                List<Record> userList = Db.find(Db.getSqlPara("admin.user.queryByIds", Kv.by("ids", record.getStr("user_ids").split(","))));
                record.set("userIds", userList);
            } else {
                record.set("userIds", new ArrayList<>());
            }
            if (record.getStr("dept_ids") != null && record.getStr("dept_ids").split(",").length > 0) {
                List<Record> deptList = Db.find(Db.getSqlPara("admin.dept.queryByIds", Kv.by("ids", record.getStr("dept_ids").split(","))));
                record.set("deptIds", deptList);
            } else {
                record.set("deptIds", new ArrayList<>());
            }
        });
        return R.ok().put("data", page);
    }

    /**
     * 根据id查询审批流程
     */
    public R queryExamineById(Integer examineId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("examine", AdminExamine.dao.findById(examineId));
        List<AdminExamineStep> examineSteps = AdminExamineStep.dao.find(Db.getSql("admin.examineStep.queryExamineStepByExamineId"), examineId);
        jsonObject.put("step", examineSteps);
        return R.ok().put("data", jsonObject);
    }

    /**
     * 停用或删除审批流程
     */
    public R updateStatus(AdminExamine adminExamine) {
        adminExamine.setUpdateUserId(BaseUtil.getUser().getUserId());
        adminExamine.setUpdateTime(DateUtil.date());
        if(adminExamine.getStatus() == null){
            adminExamine.setStatus(2);
        }
        return adminExamine.update() ? R.ok() : R.error();
    }

    private String getIds(List<Integer> ids) {
        if (ids == null || ids.size() == 0) {
            return null;
        } else {
            StringBuffer idss = new StringBuffer();
            for (Integer id : ids) {
                if (idss.length() == 0) {
                    idss.append(",").append(id).append(",");
                } else {
                    idss.append(id).append(",");
                }
            }
            return idss.toString();
        }
    }

    /**
     * 查询当前启用审核流程步骤
     */
    public R queryExaminStep(Integer categoryType,Integer id) {
        Record record = Db.findFirst(Db.getSql("admin.examine.getExamineByCategoryType"), categoryType);
        if (record != null) {
            if (record.getInt("examine_type") == 1) {
                List<Record> list = Db.find(Db.getSql("admin.examineStep.queryExamineStepByExamineId"), record.getInt("examine_id"));
                list.forEach(r -> {
                    //根据审核人id查询审核问信息
                    List<Record> userList = new ArrayList<>();
                    if (r.getStr("check_user_id") != null) {
                        String[] userIds = r.getStr("check_user_id").split(",");
                        for (String userId : userIds) {
                            if (StrUtil.isNotEmpty(userId)) {
                                Record r1 = Db.findFirst(Db.getSql("admin.user.queryUserByUserId"), userId);
                                if (r1 != null) {
                                    userList.add(r1);
                                }
                            }
                        }
                    }
                    r.set("userList", userList);
                });
                record.set("examineSteps", list);
            }else {
                if (id != null) {
                    Integer recordId = null;
                    if (categoryType == 1) {
                        CrmContract contract = CrmContract.dao.findById(id);
                        if (contract.getExamineRecordId() != null) {
                            recordId = contract.getExamineRecordId();
                        }
                    } else {
                        CrmReceivables receivables = CrmReceivables.dao.findById(id);
                        if (receivables.getExamineRecordId() != null) {
                            recordId = receivables.getExamineRecordId();
                        }
                    }
                    if (recordId != null) {
                        AdminExamineLog examineLog = AdminExamineLog.dao.findFirst("select * from 72crm_admin_examine_log where record_id = ? and is_recheck = 0", recordId);
                        if(examineLog != null){
                            record.set("examineUser", examineLog.getExamineUser());
                            AdminUser user = AdminUser.dao.findById(examineLog.getExamineUser());
                            if(user != null){
                                record.set("examineUserName", user.getRealname());
                            }
                        }
                    }
                }
            }
            return R.ok().put("data", record);
        }
        return null;
    }
}
