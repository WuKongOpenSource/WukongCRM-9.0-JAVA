package com.kakarote.crm9.erp.admin.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Aop;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.erp.admin.common.AdminMessageEnum;
import com.kakarote.crm9.erp.admin.entity.*;
import com.kakarote.crm9.erp.crm.entity.CrmContract;
import com.kakarote.crm9.erp.crm.entity.CrmReceivables;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.jfinal.aop.Before;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;


import java.util.*;

public class AdminExamineRecordService {
    /**
     * 第一次添加审核记录和审核日志 type 1 合同 2 回款 userId:授权审批人
     */
    @Before(Tx.class)
    public Map<String, Integer> saveExamineRecord(Integer type, Long userId, Long ownerUserId, Integer recordId,Integer status) {
        Map<String, Integer> map = new HashMap<>();
        //创建审核记录
        AdminExamineRecord examineRecord = new AdminExamineRecord();
        if (recordId != null) {
            examineRecord = AdminExamineRecord.dao.findById(recordId);
            Db.update(Db.getSql("admin.examineLog.updateExamineLogIsRecheckByRecordId"), recordId);
        } else {
            examineRecord.setCreateTime(DateUtil.date());
            examineRecord.setCreateUser(BaseUtil.getUser().getUserId());
        }
        //创建审核日志
        AdminExamineLog examineLog = new AdminExamineLog();
        examineLog.setCreateTime(DateUtil.date());
        examineLog.setCreateUser(BaseUtil.getUser().getUserId());
        if (status != null) {
            examineLog.setExamineStatus(5);
            examineRecord.setExamineStatus(5);
        }else {
            examineLog.setExamineStatus(0);
            examineRecord.setExamineStatus(0);
        }
        examineLog.setOrderId(1);
        //根据type查询当前启用审批流程
        AdminExamine examine = AdminExamine.dao.findFirst(Db.getSql("admin.examine.getExamineByCategoryType"), type);
        if (examine == null) {
            map.put("status", 0);
        } else {
            examineRecord.setExamineId(examine.getExamineId());
            //先判断该审批流程是否为固定审批
            if (examine.getExamineType() == 1 ) {
                //固定审批
                //先查询该审批流程的审批步骤的第一步
                AdminExamineStep examineStep = AdminExamineStep.dao.findFirst(Db.getSql("admin.examineStep.queryExamineStepByExamineIdOrderByStepNum"), examine.getExamineId());
                examineRecord.setExamineStepId(examineStep.getStepId());
                examineLog.setExamineStepId(examineStep.getStepId());
                if (recordId == null) {
                    examineRecord.save();
                } else {
                    examineRecord.update();
                }
                if (status != null){
                    map.put("status", 1);
                    map.put("id", examineRecord.getRecordId());
                    return map;
                }
                if (examineStep.getStepType() == 2 || examineStep.getStepType() == 3) {
                    String[] userIds = examineStep.getCheckUserId().split(",");
                    for (String id : userIds) {
                        if (StrUtil.isNotEmpty(id)) {
                            examineLog.setLogId(null);
                            examineLog.setExamineUser(Long.valueOf(id));
                            examineLog.setRecordId(examineRecord.getRecordId());
                            examineLog.setIsRecheck(0);
                            examineLog.save();
                        }
                    }
                } else if (examineStep.getStepType() == 1) {
                    //如果是负责人主管审批 获取主管ID
                    Record r = Db.findFirst(Db.getSql("admin.examineLog.queryUserByUserId"), ownerUserId);
                    if (r == null || r.getLong("user_id") == null){
                        examineLog.setExamineUser(BaseConstant.SUPER_ADMIN_USER_ID);
                    }else {
                    examineLog.setExamineUser(r.getLong("user_id"));}
                    examineLog.setRecordId(examineRecord.getRecordId());
                    examineLog.setIsRecheck(0);
                    examineLog.save();
                } else {
                    //如果是负责人主管审批 获取主管的主管ID
                    Record r = Db.findFirst(Db.getSql("admin.examineLog.queryUserByUserId"), Db.findFirst(Db.getSql("admin.examineLog.queryUserByUserId"), ownerUserId).getLong("user_id"));
                    if (r == null || r.getLong("user_id") == null){
                        examineLog.setExamineUser(BaseConstant.SUPER_ADMIN_USER_ID);
                    }else {
                        examineLog.setExamineUser(r.getLong("user_id"));}
                    examineLog.setRecordId(examineRecord.getRecordId());
                    examineLog.setIsRecheck(0);
                    examineLog.save();
                }

            } else {
                //授权审批
                examineLog.setExamineUser(userId);
                if (recordId == null) {
                    examineRecord.save();
                } else {
                    examineRecord.update();
                }
                examineLog.setRecordId(examineRecord.getRecordId());
                if (userId != null) {
                    examineLog.save();
                }
            }

            map.put("status", 1);
            map.put("id", examineRecord.getRecordId());
        }
        return map;
    }

    /**
     * 审核合同或者回款 recordId:审核记录id status:审批状态：审核状态  1 审核通过 2 审核拒绝 4 已撤回
     * remarks:审核备注 id:审核对象的id（合同或者回款的id）nextUserId:下一个审批人 ownerUserId:负责人
     */
    @Before(Tx.class)
    public R auditExamine(Integer recordId, Integer status, String remarks, Integer id, Long nextUserId, Long ownerUserId) {

        //当前审批人
        Long auditUserId = BaseUtil.getUser().getUserId();

        //根据审核记录id查询审核记录
        AdminExamineRecord examineRecord = AdminExamineRecord.dao.findById(recordId);
        if (status == 4) {
            if (!examineRecord.getCreateUser().equals(auditUserId) && !auditUserId.equals(BaseConstant.SUPER_ADMIN_USER_ID)) {
                return R.error("当前用户没有审批权限！");
            }
        } else {
            //【判断当前审批人是否有审批权限
            Record reco = Db.findFirst(Db.getSqlPara("admin.examineLog.queryExamineLog",
                    Kv.by("recordId", recordId).set("auditUserId", auditUserId).set("stepId", examineRecord.getExamineStepId())));
            if (reco == null) {
                return R.error("当前用户没有审批权限！");
            }
        }
        examineRecord.setExamineStatus(status);
        //查询审批流程
        AdminExamine examine = AdminExamine.dao.findById(examineRecord.getExamineId());
        if (examine.getCategoryType() == 1) {
            ownerUserId = Long.valueOf(CrmContract.dao.findById(id).getOwnerUserId());
        } else {
            ownerUserId = Long.valueOf(CrmReceivables.dao.findById(id).getOwnerUserId());
        }
        //查询当前审批步骤
        AdminExamineStep examineStep = AdminExamineStep.dao.findById(examineRecord.getExamineStepId());
        //查询当前审核日志
        AdminExamineLog nowadayExamineLog = null;
        if (examine.getExamineType() == 1) {
            nowadayExamineLog = AdminExamineLog.dao.findFirst(Db.getSql("admin.examineLog.queryNowadayExamineLogByRecordIdAndStepId"), examineRecord.getRecordId(), examineRecord.getExamineStepId(), auditUserId);
        } else {
            nowadayExamineLog = AdminExamineLog.dao.findFirst(Db.getSql("admin.examineLog.queryNowadayExamineLogByRecordIdAndStatus"), examineRecord.getRecordId(), auditUserId);
        }

        //审核日志 添加审核人
        if (nowadayExamineLog != null) {
            nowadayExamineLog.setExamineTime(DateUtil.date());
            nowadayExamineLog.setRemarks(remarks);
        }

        if (status == 2) {
            //判断审核拒绝
            nowadayExamineLog.setExamineStatus(status);
            if (examineStep != null && examineStep.getStepType() == 2) {
                examineRecord.setExamineStatus(3);
                Record record = Db.findFirst(Db.getSqlPara("admin.examineLog.queryCountByStepId", Kv.by("recordId", recordId).set("stepId", examineStep.getStepId())));
                if (record.getInt("toCount") == 0) {
                    examineRecord.setExamineStatus(status);
                }
            }

            if (examine.getCategoryType() == 1) {
                //合同
                Db.update(Db.getSql("crm.contract.updateCheckStatusById"),status,id);
            } else {
                //回款
                Db.update(Db.getSql("crm.receivables.updateCheckStatusById"),status,id);
            }
        } else if (status == 4) {
            //先查询该审批流程的审批步骤的第一步
            AdminExamineStep oneExamineStep = AdminExamineStep.dao.findFirst(Db.getSql("admin.examineStep.queryExamineStepByExamineIdOrderByStepNum"), examine.getExamineId());
            //判断审核撤回
            AdminExamineLog examineLog = new AdminExamineLog();
            examineLog.setLogId(null);
            examineLog.setExamineUser(auditUserId);
            examineLog.setCreateTime(DateUtil.date());
            examineLog.setCreateUser(auditUserId);
            examineLog.setExamineStatus(status);
            examineLog.setExamineTime(new Date());
            examineLog.setIsRecheck(0);
            if (examine.getExamineType() == 1) {
                examineRecord.setExamineStepId(oneExamineStep.getStepId());
                examineLog.setExamineStepId(examineStep.getStepId());
                examineLog.setOrderId(examineStep.getStepNum());
            } else {
                Integer orderId = Db.queryInt(Db.getSql("admin.examineStep.queryExamineStepOrderId"), recordId);
                if (orderId == null) {
                    orderId = 1;
                }
                examineLog.setOrderId(orderId);
            }
            examineLog.setRecordId(examineRecord.getRecordId());
            examineLog.setRemarks(remarks);
            examineLog.save();
            if (examine.getCategoryType() == 1) {
                //合同
                CrmContract contract = CrmContract.dao.findById(id);
                if (contract.getCheckStatus() == 1) {
                    return R.error("该合同已审核通过，不能撤回！");
                }
                Db.update(Db.getSql("crm.contract.updateCheckStatusById"),4,id);
            } else {
                //回款
                CrmReceivables receivables = CrmReceivables.dao.findById(id);
                if (receivables.getCheckStatus() == 1) {
                    return R.error("该回款已审核通过，不能撤回！");
                }
                Db.update(Db.getSql("crm.receivables.updateCheckStatusById"),4,id);
            }
        } else {
            //审核通过
            nowadayExamineLog.setExamineStatus(status);
            //判断该审批流程类型
            if (examine.getExamineType() == 1) {
                //固定审批

                //查询下一个审批步骤
                AdminExamineStep nextExamineStep =
                        AdminExamineStep.dao.findFirst(Db.getSql("admin.examineStep.queryExamineStepByNextExamineIdOrderByStepId"), examine.getExamineId(), examineRecord.getExamineStepId());

                boolean flag = true;
                //判断是否是并签
                if (examineStep.getStepType() == 3) {
                    //查询当前并签是否都完成
                    //根据审核记录ID，审核步骤ID，查询审核日志
                    // List<AdminExamineLog> examineLogs = AdminExamineLog.dao.find(Db.getSql("admin.examineLog.queryNowadayExamineLogByRecordIdAndStepId"),examineRecord.getRecordId(),examineRecord.getExamineStepId());
                    //当前并签人员
                    nowadayExamineLog.update();
                    String[] userIds = examineStep.getCheckUserId().split(",");
                    for (String userId : userIds) {
                        if (StrUtil.isNotEmpty(userId)) {
                            AdminExamineLog examineLog = AdminExamineLog.dao.findFirst(Db.getSql("admin.examineLog.queryNowadayExamineLogByRecordIdAndStepId"), examineRecord.getRecordId(), examineRecord.getExamineStepId(), userId);
                            if (examineLog.getExamineStatus() == 0) {
                                //并签未走完
                                flag = false;
                                break;
                            }
                        }
                    }
                    //并签未完成
                    if (!flag) {
                        examineRecord.setExamineStatus(3);
                        if (examine.getCategoryType() == 1) {
                            //合同
                            Db.update(Db.getSql("crm.contract.updateCheckStatusById"),3,id);

                        } else {
                            //回款
                            Db.update(Db.getSql("crm.receivables.updateCheckStatusById"),3,id);

                        }
                    }
                }
                if (flag) {
                    //判断是否有下一步流程
                    if (nextExamineStep != null) {
                        //有下一步流程
                        examineRecord.setExamineStatus(3);
                        examineRecord.setExamineStepId(nextExamineStep.getStepId());

                        AdminExamineLog examineLog = new AdminExamineLog();
                        examineLog.setOrderId(nextExamineStep.getStepNum());
                        if (nextExamineStep.getStepType() == 2 || nextExamineStep.getStepType() == 3) {
                            //并签或者或签
                            String[] userIds = nextExamineStep.getCheckUserId().split(",");
                            for (String uid : userIds) {
                                if (StrUtil.isNotEmpty(uid)) {
                                    examineLog.setLogId(null);
                                    examineLog.setExamineUser(Long.valueOf(uid));
                                    examineLog.setCreateTime(DateUtil.date());
                                    examineLog.setCreateUser(BaseUtil.getUser().getUserId());
                                    examineLog.setExamineStatus(0);
                                    examineLog.setIsRecheck(0);
                                    examineLog.setExamineStepId(nextExamineStep.getStepId());
                                    examineLog.setRecordId(examineRecord.getRecordId());

                                    examineLog.save();
                                }
                            }
                        } else if (nextExamineStep.getStepType() == 1) {
                            Record r = Db.findFirst(Db.getSql("admin.examineLog.queryUserByUserId"), ownerUserId);
                            examineLog.setLogId(null);
                            if (r == null|| r.getLong("user_id") == null) {
                                examineLog.setExamineUser(BaseConstant.SUPER_ADMIN_USER_ID);
                            } else {
                                examineLog.setExamineUser(r.getLong("user_id"));

                            }
                            examineLog.setExamineStatus(0);
                            examineLog.setCreateTime(DateUtil.date());
                            examineLog.setCreateUser(BaseUtil.getUser().getUserId());
                            examineLog.setIsRecheck(0);
                            examineLog.setExamineStepId(nextExamineStep.getStepId());
                            examineLog.setRecordId(examineRecord.getRecordId());
                            examineLog.save();
                        } else {
                            Record r = Db.findFirst(Db.getSql("admin.examineLog.queryUserByUserId"), Db.findFirst(Db.getSql("admin.examineLog.queryUserByUserId"), ownerUserId).getLong("user_id"));
                            examineLog.setLogId(null);
                            AdminUser adminUser = AdminUser.dao.findById(nowadayExamineLog.getExamineUser());
                            if (adminUser != null && adminUser.getParentId() != null) {
                                examineLog.setExamineUser(adminUser.getParentId());
                            }else {
                                examineLog.setExamineUser(ownerUserId);
                            }

                            examineLog.setExamineStatus(0);
                            examineLog.setCreateTime(DateUtil.date());
                            examineLog.setCreateUser(BaseUtil.getUser().getUserId());
                            examineLog.setExamineStepId(nextExamineStep.getStepId());
                            examineLog.setRecordId(examineRecord.getRecordId());
                            examineLog.setIsRecheck(0);
                            examineLog.save();
                        }

                        // AdminExamineLog examineLog = new AdminExamineLog();
                        if (examine.getCategoryType() == 1) {
                            //合同
                            Db.update(Db.getSql("crm.contract.updateCheckStatusById"),3,id);
                        } else {
                            //回款
                            Db.update(Db.getSql("crm.receivables.updateCheckStatusById"),3,id);
                        }
                    } else {
                        //没有下一审批流程步骤
                        if (examine.getCategoryType() == 1) {
                            //合同
                            Db.update(Db.getSql("crm.contract.updateCheckStatusById"),1,id);
                            CrmContract contract = CrmContract.dao.findById(id);
                            Db.update(Db.getSql("crm.customer.updateDealStatusById"),"1",contract.getCustomerId());
                        } else {
                            //回款
                            Db.update(Db.getSql("crm.receivables.updateCheckStatusById"),1,id);
                        }

                    }
                }
            } else {
                //授权审批
                if (nextUserId != null) {
                    //有下一审批人
                    examineRecord.setExamineStatus(3);
                    AdminExamineLog examineLog = new AdminExamineLog();
                    examineLog.setCreateTime(DateUtil.date());
                    examineLog.setCreateUser(BaseUtil.getUser().getUserId());
                    examineLog.setExamineUser(nextUserId);
                    examineLog.setExamineStatus(0);
                    examineLog.setIsRecheck(0);
                    examineLog.setRecordId(examineRecord.getRecordId());
                    examineLog.setOrderId(nowadayExamineLog.getOrderId() + 1);
                    examineLog.save();
                    if (examine.getCategoryType() == 1) {
                        //合同
                        Db.update(Db.getSql("crm.contract.updateCheckStatusById"),3,id);
                    } else {
                        //回款
                        Db.update(Db.getSql("crm.receivables.updateCheckStatusById"),3,id);
                    }
                } else {
                    //没有下一审批人
                    if (examine.getCategoryType() == 1) {
                        //合同
                        Db.update(Db.getSql("crm.contract.updateCheckStatusById"),1,id);
                        CrmContract contract = CrmContract.dao.findById(id);
                        Db.update(Db.getSql("crm.customer.updateDealStatusById"),1,contract.getCustomerId());
                    } else {
                        //回款
                        Db.update(Db.getSql("crm.receivables.updateCheckStatusById"),1,id);
                    }
                }

            }
        }
        if (status != 4) {
            nowadayExamineLog.update();
        }
        return examineRecord.update() ? R.ok() : R.error();
    }

    public R queryExamineLogList(Integer recordId) {
        //根据审核记录id查询审核记录
        AdminExamineRecord examineRecord = AdminExamineRecord.dao.findById(recordId);

        AdminExamine adminExamine = AdminExamine.dao.findById(examineRecord.getExamineId());
        List<Record> logs = null;
        if (adminExamine.getExamineType() == 1) {
            logs = Db.find(Db.getSql("admin.examineLog.queryExamineLogByRecordIdByStep"), recordId);
        } else {
            logs = Db.find(Db.getSql("admin.examineLog.queryExamineLogByRecordIdByStep1"), recordId);
        }

        return R.ok().put("data", logs);
    }

    /**
     * 根据审核记录id，查询审核日志
     * ownerUserId 负责人ID
     */
    public R queryExamineRecordList(Integer recordId, Long ownerUserId) {
        JSONObject jsonObject = new JSONObject();
        Record examineRecord = Db.findFirst(Db.getSql("admin.examineStep.queryExamineRecordById"), recordId);
        //如果当前审批已撤回
        if (examineRecord.getInt("examine_status") == 4 ||  examineRecord.getInt("examine_status") == 5) {
            jsonObject.put("examineType", 1);
            List<Record> user = Db.find(Db.getSql("admin.examineLog.queryUserByRecordId"), recordId,examineRecord.getInt("examine_status"));

            if (examineRecord.getInt("examine_status") == 5) {
                examineRecord.set("examine_status", 6);
                user.forEach(record ->{
                    record.set("examine_status",6);
                });
            }
            examineRecord.set("userList", user);
            List<Record> records = new ArrayList<>();
            records.add(examineRecord);
            jsonObject.put("steps", records);
            return R.ok().put("data", jsonObject);
        }
        AdminExamine adminExamine = AdminExamine.dao.findById(examineRecord.getInt("examine_id"));
        List<Record> list = new ArrayList<>();
        Record rec = Db.findFirst(Db.getSql("admin.examineLog.queryRecordAndId"), recordId);


        //当前审批人
        Long auditUserId = BaseUtil.getUser().getUserId();
        //jsonObject.put("isRecheck",0);
        //判断是否有撤回权限
        if ((auditUserId.equals(examineRecord.getLong("create_user")) || auditUserId.equals(BaseConstant.SUPER_ADMIN_USER_ID))
                && (examineRecord.getInt("examine_status") == 0 ||examineRecord.getInt("examine_status") == 3)) {
            jsonObject.put("isRecheck", 1);
        } else {
            jsonObject.put("isRecheck", 0);
        }
        if (adminExamine.getExamineType() == 2) {
            Record log = Db.findFirst(Db.getSqlPara("admin.examineLog.queryRecordByUserIdAndStatus", Kv.by("create_user", rec.getInt("create_user")).set("examineTime", rec.getDate("examineTime"))));
            rec.set("examinUser", log);
            list.add(rec);
            //授权审批
            List<Record> logs = Db.find(Db.getSql("admin.examineLog.queryExamineLogAndUserByRecordId"), recordId);
            logs.forEach(r -> {
                Record l = Db.findFirst(Db.getSql("admin.examineLog.queryExamineLogAndUserByLogId"), r.getInt("log_id"));
                r.set("examinUser", l);
            });
            list.addAll(logs);
            //判断当前用户有没有权限审核
            Record reco = Db.findFirst(Db.getSqlPara("admin.examineLog.queryExamineLog",
                    Kv.by("recordId", recordId).set("auditUserId", auditUserId)));
            if (reco != null) {
                jsonObject.put("isCheck", 1);
            } else {
                jsonObject.put("isCheck", 0);
            }
            jsonObject.put("examineType", 2);
            jsonObject.put("steps", list);

        } else {
            jsonObject.put("examineType", 1);
            //固定审批
            List<Record> steps = Db.find("select * from 72crm_admin_examine_step where  examine_id = ? ORDER BY step_num", adminExamine.getExamineId());
               //上一审核步骤
                Long lsatuUserId = null;
            for (Record step: steps) {
                List<Record> logs ;
                if (step.getInt("step_type") == 1) {
                    //负责人主管
                     logs = Db.find(Db.getSql("admin.examineLog.queryUserByRecordIdAndStepIdAndStatus"), recordId, step.getInt("step_id"));
                    if (logs.size() == 1){
                        if (logs.get(0).getInt("user_id")==null){
                            logs = null;
                        }

                    }
                    //已经创建审核日志
                    if (logs != null && logs.size() != 0) {
                        for (Record record : logs) {
                            step.set("examine_status", record.getInt("examine_status"));
                        }
                        step.set("userList", logs);
                    } else {
                        step.set("examine_status", 0);
                        //还未创建审核日志
                        //查询负责人主管
                        List<Record> r = Db.find(Db.getSql("admin.examineLog.queryUserByUserId"), ownerUserId);
                        if (r.size() == 1){
                            if (r.get(0).getInt("user_id")==null){
                                r = null;
                            }

                        }
                        if (r == null || r.size() == 0) {
                            r = Db.find(Db.getSql("admin.examineLog.queryUserByUserIdAnd"), BaseConstant.SUPER_ADMIN_USER_ID);
                        }
                        step.set("userList", r);
                    }
                } else if (step.getInt("step_type") == 2 || step.getInt("step_type") == 3) {
                    //先判断是否已经审核过
                   logs = Db.find(Db.getSql("admin.examineLog.queryUserByRecordIdAndStepIdAndStatus"), recordId, step.getInt("step_id"));
                    if (logs != null && logs.size() != 0) {
                        //已经创建审核日志
                        int status = 0;

                        if (step.getInt("step_type") == 2) {
                            int i = 0;
                            for (Record record : logs) {
                                if (record.getInt("examine_status") == 1) {
                                    status = 1;
                                }
                                if (record.getInt("examine_status") == 2) {
                                    i++;
                                }
                            }
                            if (i == logs.size()) {
                                status = 2;
                            }
                        }
                        if (step.getInt("step_type") == 3) {
                            int i = 0;
                            for (Record record : logs) {
                                if (record.getInt("examine_status") == 2) {
                                    status = 2;
                                }
                                if (record.getInt("examine_status") == 1) {
                                    i++;
                                }
                            }
                            if (i == logs.size()) {
                                status = 1;
                            }
                        }
                        step.set("examine_status", status);
                        step.set("userList", logs);
                    } else {
                        //该步骤还未审核
                        logs = new ArrayList<>();
                        String[] userIds = step.getStr("check_user_id").split(",");
                        for (String userId : userIds) {
                            if (StrUtil.isNotEmpty(userId)) {
                                Record user = Db.findFirst(Db.getSql("admin.examineLog.queryUserByUserIdAndStatus"), userId);
                                if (user!=null){
                                    logs.add(user);
                                }
                            }
                        }
                        step.set("examine_status", 0);
                        step.set("userList", logs);
                    }
                } else {
                    //主管的主管
                    logs = Db.find(Db.getSql("admin.examineLog.queryUserByRecordIdAndStepIdAndStatus"), recordId, step.getInt("step_id"));
                    if (logs.size() == 1){
                        if (logs.get(0).getInt("user_id")==null){
                            logs = null;
                        }

                    }
                    //已经创建审核日志
                    if (logs != null && logs.size() != 0) {
                        for (Record record : logs) {
                            step.set("examine_status", record.getInt("examine_status"));
                        }
                        step.set("userList", logs);
                    } else {
                        step.set("examine_status", 0);
                        AdminUser adminUser = AdminUser.dao.findById(lsatuUserId);
                        if (adminUser != null && adminUser.getParentId() != null) {
                            logs = Db.find(Db.getSql("admin.examineLog.queryUserByUserIdAnd"),adminUser.getParentId());
                        }else {
                            logs = Db.find(Db.getSql("admin.examineLog.queryUserByUserIdAnd"),lsatuUserId);
                        }
                        //查询负责人主管的主管
                        step.set("userList", logs );
                    }
                }
                if (logs.size()>0){
                    lsatuUserId = logs.get(0).getLong("user_id");
                }
            }
            //判断当前用户有没有权限审核
            Record reco = Db.findFirst(Db.getSqlPara("admin.examineLog.queryExamineLog",
                    Kv.by("recordId", recordId).set("auditUserId", auditUserId).set("stepId", examineRecord.getInt("examine_step_id"))));
            if (reco != null) {
                jsonObject.put("isCheck", 1);
            } else {
                jsonObject.put("isCheck", 0);
            }
            List<Record> logs = Db.find(Db.getSqlPara("admin.examineLog.queryRecordByUserIdAndStatus", Kv.by("create_user", rec.getInt("create_user")).set("examineTime", rec.getDate("examineTime"))));
            rec.set("userList", logs);
            list.add(rec);
            list.addAll(steps);
            jsonObject.put("steps", list);
        }
        return R.ok().put("data", jsonObject);
    }
}
