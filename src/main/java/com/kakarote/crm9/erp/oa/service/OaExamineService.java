package com.kakarote.crm9.erp.oa.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.erp.admin.entity.AdminExamineLog;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminFieldService;
import com.kakarote.crm9.erp.admin.service.AdminFileService;
import com.kakarote.crm9.erp.crm.entity.CrmBusiness;
import com.kakarote.crm9.erp.crm.entity.CrmContacts;
import com.kakarote.crm9.erp.crm.entity.CrmContract;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import com.kakarote.crm9.erp.oa.common.OaEnum;
import com.kakarote.crm9.erp.oa.entity.*;
import com.kakarote.crm9.utils.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;


public class OaExamineService{

    //添加日志
    @Inject
    private OaActionRecordService oaActionRecordService;
    @Inject
    private AdminFileService adminFileService;

    @Inject
    private AdminFieldService adminFieldService;


    public R myInitiate(BasePageRequest<Void> request){
        JSONObject jsonObject = request.getJsonObject();
        Long userId = BaseUtil.getUser().getUserId();
        if(request.getPageType() == 0){
            List<Record> recordList = Db.find(Db.getSqlPara("oa.examine.myInitiate", Kv.by("userId", userId).set("categoryId", jsonObject.getInteger("categoryId")).set("status", jsonObject.getInteger("checkStatus")).set("startTime", jsonObject.getDate("startTime")).set("endTime", jsonObject.getDate("endTime"))));
            transfer(recordList);
            return R.ok().put("data", recordList);
        }else{
            Page<Record> recordList = Db.paginate(request.getPage(), request.getLimit(), Db.getSqlPara("oa.examine.myInitiate", Kv.by("userId", userId).set("categoryId", jsonObject.getInteger("categoryId")).set("status", jsonObject.getInteger("checkStatus")).set("startTime", jsonObject.getDate("startTime")).set("endTime", jsonObject.getDate("endTime"))));
            transfer(recordList.getList());
            return R.ok().put("data", recordList);
        }
    }

    public R myOaExamine(BasePageRequest<OaExamine> request){
        Long userId = BaseUtil.getUser().getUserId();
        JSONObject jsonObject = request.getJsonObject();
        if(request.getPageType() == 0){
            List<Record> recordList = Db.find(Db.getSqlPara("oa.examine.myOaExamine", Kv.by("userId", userId).set("categoryId", jsonObject.getInteger("categoryId")).set("status", jsonObject.getInteger("status")).set("startTime", jsonObject.getDate("startTime")).set("endTime", jsonObject.getDate("endTime"))));
            transfer(recordList);
            return R.ok().put("data", recordList);
        }else{
            Page<Record> recordList = Db.paginate(request.getPage(), request.getLimit(), Db.getSqlPara("oa.examine.myOaExamine", Kv.by("userId", userId).set("categoryId", jsonObject.getInteger("categoryId")).set("status", jsonObject.getInteger("status")).set("startTime", jsonObject.getDate("startTime")).set("endTime", jsonObject.getDate("endTime"))));
            transfer(recordList.getList());
            return R.ok().put("data", recordList);
        }
    }

    public void transfer(List<Record> recordList){
        recordList.forEach(record -> {
            setRelation(record);
            record.set("createUser", Db.findFirst("select user_id,realname,img from 72crm_admin_user where user_id = ?", record.getLong("create_user_id")));
            String batchId = record.getStr("batch_id");
            adminFileService.queryByBatchId(batchId, record);
            setCountRecord(record);
            List<Integer> roles = BaseUtil.getUser().getRoles();
            Map<String,Integer> permission = new HashMap<>();
            Long createUserId = record.getLong("create_user_id");
            Integer examineStatus = record.getInt("examine_status");
            Long userId = BaseUtil.getUser().getUserId();
            if((userId.equals(BaseConstant.SUPER_ADMIN_USER_ID) && (examineStatus == 4 || examineStatus == 2)) || (createUserId.equals(BaseUtil.getUser().getUserId()) && (examineStatus == 4 || examineStatus == 2))){
                permission.put("isUpdate", 1);
            }else{
                permission.put("isUpdate", 0);
            }
            if(roles.contains(BaseConstant.SUPER_ADMIN_ROLE_ID) && examineStatus != 1){
                permission.put("isDelete", 1);
            }else{
                permission.put("isDelete", 0);
            }
            if(((userId.equals(BaseConstant.SUPER_ADMIN_USER_ID) || userId.equals(record.getLong("create_user_id"))) && (examineStatus == 0 || examineStatus == 3)) && examineStatus != 4){
                permission.put("isChecked", 1);
            }else{
                permission.put("isChecked", 0);
            }
            record.set("permission", permission);
        });
    }

    private void setCountRecord(Record record){
        Integer examineId = record.getInt("examine_id");
        String categoryTitle = record.getStr("categoryTitle");
        Integer count = Db.queryInt("select count(*) as count from 72crm_oa_examine_travel where examine_id = ?", examineId);
        StringBuilder causeTitle = new StringBuilder();
        if(count > 0){
            causeTitle.append(count);
            switch(categoryTitle){
                case "出差审批":
                    causeTitle.append("个行程，共");
                    if(record.get("duration") != null){
                        causeTitle.append(record.getInt("duration"));
                    }else{
                        causeTitle.append(0);
                    }
                    causeTitle.append("天。");
                    break;
                case "差旅报销":
                    causeTitle.append("个报销事项，共");
                    if(record.get("money") != null){
                        causeTitle.append(record.getInt("money"));
                    }else{
                        causeTitle.append(0);
                    }
                    causeTitle.append("元。");
                    break;
                default:
                    break;
            }
        }
        record.set("causeTitle", causeTitle.toString());
    }

    @Before(Tx.class)
    public R setOaExamine(JSONObject jsonObject){
        AdminUser user = BaseUtil.getUser();
        OaExamine oaExamine = jsonObject.getObject("oaExamine", OaExamine.class);
        boolean oaAuth = AuthUtil.isOaAuth(OaEnum.EXAMINE_TYPE_KEY.getTypes(), oaExamine.getExamineId());
        if(oaAuth){
            return R.noAuth();
        }
        if(oaExamine.getStartTime() != null && oaExamine.getEndTime() != null){
            if((oaExamine.getStartTime().compareTo(oaExamine.getEndTime())) == 1){
                return R.error("审批结束时间早于开始时间");
            }
        }
        boolean bol;
        String batchId = StrUtil.isNotEmpty(oaExamine.getBatchId()) ? oaExamine.getBatchId() : IdUtil.simpleUUID();
        adminFieldService.save(jsonObject.getJSONArray("field"), batchId);
        oaExamine.setBatchId(batchId);
        String checkUserIds = jsonObject.getString("checkUserId");
        Integer categoryId = oaExamine.getCategoryId();
        OaExamineCategory oaExamineCategory = OaExamineCategory.dao.findById(categoryId);
        OaExamineStep oaExamineStep = new OaExamineStep();
        Integer examineType = oaExamineCategory.getExamineType();
        if(oaExamineCategory.getExamineType() == 1){
            oaExamineStep = OaExamineStep.dao.findFirst("SELECT * FROM 72crm_oa_examine_step WHERE category_id = ? ORDER BY step_num LIMIT 0,1", categoryId);
        }
        Integer recordId = null;
        //创建审批记录
        if(oaExamine.getExamineId() == null){
            oaExamine.setCreateUserId(user.getUserId());
            oaExamine.setCreateTime(new Date());
            bol = oaExamine.save();
        }else{
            oaExamine.setUpdateTime(new Date());
            bol = oaExamine.update();
            Db.delete("delete from 72crm_oa_examine_travel where examine_id = ?", oaExamine.getExamineId());
            Db.delete("delete from 72crm_oa_examine_relation where examine_id = ?", oaExamine.getExamineId());
            recordId = Db.queryInt("select  record_id from 72crm_oa_examine_record where examine_id = ? limit 1", oaExamine.getExamineId());
        }
        oaExamine = new OaExamine().findById(oaExamine.getExamineId());
        OaExamineRecord oaExamineRecord = new OaExamineRecord();
        oaExamineRecord.setExamineId(oaExamine.getExamineId());
        oaExamineRecord.setExamineStepId(oaExamineStep.getStepId());
        oaExamineRecord.setExamineStatus(0);
        //生成审批记录
        if(recordId == null){
            oaExamineRecord.setCreateUser(user.getUserId());
            oaExamineRecord.setCreateTime(new Date());
            oaExamineRecord.save();
        }else{
            oaExamineRecord.setExamineStatus(0);
            oaExamineRecord.setRecordId(recordId);
            oaExamineRecord.update();
            //更新审核日志状态
            Db.update("update 72crm_oa_examine_log set is_recheck = 1 where record_id = ?", recordId);
        }

        //生成审批日志
        if(examineType == 1){
            Integer stepType = oaExamineStep.getStepType();
            if(stepType == 1){
                checkUserIds = Db.queryInt("select parent_id from 72crm_admin_user where user_id = ?", BaseUtil.getUser().getUserId()) + "";
            }else if(stepType == 4){
                checkUserIds = Db.queryInt("select parent_id from 72crm_admin_user where user_id = (select parent_id from 72crm_admin_user where user_id = ?)", BaseUtil.getUser().getUserId()) + "";
            }else{
                checkUserIds = oaExamineStep.getCheckUserId();
            }
        }
        if("0".equals(checkUserIds) && ! oaExamine.getCreateUserId().equals(BaseConstant.SUPER_ADMIN_USER_ID)){
            checkUserIds = BaseConstant.SUPER_ADMIN_USER_ID + "";
        }
        if(StrUtil.isEmpty(checkUserIds)){
            //没有审核人，审批结束
            oaExamineRecord.setExamineStatus(1);
            oaExamineRecord.update();
        }else{
            //添加审核日志
            for(Integer userId : TagUtil.toSet(checkUserIds)){
                OaExamineLog oaExamineLog = new OaExamineLog();
                oaExamineLog.setRecordId(oaExamineRecord.getRecordId());
                oaExamineLog.setOrderId(1);
                if(oaExamineStep.getStepId() != null){
                    oaExamineLog.setExamineStepId(oaExamineStep.getStepId());
                }
                oaExamineLog.setExamineStatus(0);
                oaExamineLog.setCreateUser(user.getUserId());
                oaExamineLog.setCreateTime(new Date());
                oaExamineLog.setExamineUser(Long.valueOf(userId));
                oaExamineLog.save();
            }
        }
        oaActionRecordService.addRecord(oaExamine.getExamineId(), OaEnum.EXAMINE_TYPE_KEY.getTypes(), oaExamine.getUpdateTime() == null ? 1 : 2, oaActionRecordService.getJoinIds(user.getUserId(), TagUtil.fromString(checkUserIds)), "");
        if(jsonObject.get("oaExamineRelation") != null){
            OaExamineRelation oaExamineRelation = jsonObject.getObject("oaExamineRelation", OaExamineRelation.class);
            oaExamineRelation.setRId(null);
            oaExamineRelation.setBusinessIds(TagUtil.fromString(oaExamineRelation.getBusinessIds()));
            oaExamineRelation.setContactsIds(TagUtil.fromString(oaExamineRelation.getContactsIds()));
            oaExamineRelation.setContractIds(TagUtil.fromString(oaExamineRelation.getContractIds()));
            oaExamineRelation.setCustomerIds(TagUtil.fromString(oaExamineRelation.getCustomerIds()));
            oaExamineRelation.setExamineId(oaExamine.getExamineId());
            oaExamineRelation.setCreateTime(new Date());
            oaExamineRelation.save();
        }
        if(jsonObject.get("oaExamineTravelList") != null){
            JSONArray oaExamineRelation = jsonObject.getJSONArray("oaExamineTravelList");
            for(Object json : oaExamineRelation){
                OaExamineTravel oaExamineTravel = TypeUtils.castToJavaBean(json, OaExamineTravel.class);
                if(oaExamineTravel.getStartTime() != null && oaExamineTravel.getEndTime() != null){
                    if((oaExamineTravel.getStartTime().compareTo(oaExamineTravel.getEndTime())) == 1){
                        return R.error("差旅结束时间早于开始时间");
                    }
                }
                oaExamineTravel.setTravelId(null);
                oaExamineTravel.setExamineId(oaExamine.getExamineId());
                oaExamineTravel.save();
            }
        }
        return bol ? R.ok() : R.error();
    }


    public R oaExamine(OaExamineLog nowadayExamineLog, Long nextUserId){
        //当前审批人
        Long auditUserId = BaseUtil.getUser().getUserId();
        String checkUserIds = "";
        if(nextUserId != null){
            checkUserIds = nextUserId + "";
        }
        Integer recordId = nowadayExamineLog.getRecordId();
        Integer status = nowadayExamineLog.getExamineStatus();
        //根据审核记录id查询审核记录
        OaExamineRecord examineRecord = OaExamineRecord.dao.findById(recordId);
        if(status == 4){
            if(! examineRecord.getCreateUser().equals(auditUserId) && ! auditUserId.equals(BaseConstant.SUPER_ADMIN_USER_ID)){
                return R.error("当前用户没有审批权限！");
            }
        }else{
            SqlPara sqlPara = Db.getSqlPara("oa.examine.queryExamineLog", Kv.by("recordId", recordId).set("examineUser", auditUserId).set("stepId", examineRecord.get("examine_step_id")));
            Record oaExamineLog = Db.findFirst(sqlPara);
            //【判断当前审批人是否有审批权限
            if(oaExamineLog == null){
                return R.error("当前用户没有审批权限！");
            }
        }
        examineRecord.setExamineStatus(status);
        Integer examineId = examineRecord.getExamineId();
        //查询审批流程
        OaExamine examine = OaExamine.dao.findById(examineId);
        //查询审批类型
        OaExamineCategory examineCategory = OaExamineCategory.dao.findById(examine.getCategoryId());
        //查询当前审批步骤
        OaExamineStep examineStep = OaExamineStep.dao.findById(examineRecord.getExamineStepId());
        //查询当前审核日志
        Long createUserId = examine.getCreateUserId();
        Record log;
        if(examineCategory.getExamineType() == 1){
            log = Db.findFirst("select log_id,order_id from 72crm_oa_examine_log where record_id = ? and examine_step_id = ? and examine_user = ? and is_recheck = 0", examineRecord.getRecordId(), examineRecord.getExamineStepId(), auditUserId);
        }else{
            log = Db.findFirst("select log_id,order_id from 72crm_oa_examine_log where record_id = ? and examine_status = 0 and examine_user = ? and is_recheck = 0", examineRecord.getRecordId(), auditUserId);
        }
        nowadayExamineLog.setExamineUser(auditUserId);
        if(log != null){
            nowadayExamineLog.setLogId(log.getLong("log_id"));
            nowadayExamineLog.setOrderId(log.getInt("order_id"));
        }

        //审核日志 添加审核人
        nowadayExamineLog.setExamineTime(DateUtil.date());
        if(status != 4){
            nowadayExamineLog.update();
        }
        if(status == 2){
            //判断审核拒绝
            if(examineStep != null && examineStep.getStepType() == 2){
                examineRecord.setExamineStatus(3);
                Record record = Db.findFirst(Db.getSqlPara("oa.examine.queryCountByStepId", Kv.by("recordId", recordId).set("stepId", examineStep.getStepId())));
                if(record.getInt("toCount") == 0){
                    examineRecord.setExamineStatus(status);
                }
            }
        }else if(status == 4){
            examineRecord.setExamineStatus(4);
            //先查询该审批流程的审批步骤的第一步
            OaExamineStep oneExamineStep = OaExamineStep.dao.findFirst("SELECT * FROM 72crm_oa_examine_step WHERE category_id = ? ORDER BY step_num LIMIT 0,1", examine.getCategoryId());
            //判断审核撤回
            OaExamineLog examineLog = new OaExamineLog();
            examineLog.setExamineUser(auditUserId);
            examineLog.setCreateTime(DateUtil.date());
            examineLog.setCreateUser(auditUserId);
            examineLog.setExamineStatus(status);
            examineLog.setIsRecheck(1);
            examineLog.setExamineTime(new Date());
            if(examineCategory.getExamineType() == 1){
                examineRecord.setExamineStepId(oneExamineStep.getStepId());
                examineLog.setExamineStepId(examineStep.getStepId());
                examineLog.setOrderId(examineStep.getStepNum());
            }else{
                Integer orderId = Db.queryInt("select order_id from 72crm_oa_examine_log where record_id = ? and is_recheck = 0 and examine_status !=0 order by order_id desc limit 1 ", recordId);
                if(orderId == null){
                    orderId = 1;
                }
                examineLog.setOrderId(orderId);
            }
            examineLog.setRecordId(examineRecord.getRecordId());
            examineLog.setRemarks(nowadayExamineLog.getRemarks());
            examineLog.save();
            //更新审核日志状态
            Db.update("update 72crm_oa_examine_log set is_recheck = 1 where record_id = ?", recordId);
        }else{
            //审核通过
            //判断该审批流程类型
            OaExamineStep nextExamineStep = null;
            boolean flag = true;
            if(examineCategory.getExamineType() == 1){
                //固定审批
                //查询下一个审批步骤
                nextExamineStep =
                        OaExamineStep.dao.findFirst(Db.getSql("oa.examine.queryExamineStepByNextExamineIdOrderByStepId"), examineCategory.getCategoryId(), examineRecord.getExamineStepId());


                //判断是否是并签
                if(examineStep.getStepType() == 3){
                    //查询当前并签是否都完成
                    //根据审核记录ID，审核步骤ID，查询审核日志
                    // List<AdminExamineLog> examineLogs = AdminExamineLog.dao.find(Db.getSql("admin.examineLog.queryNowadayExamineLogByRecordIdAndStepId"),examineRecord.getRecordId(),examineRecord.getExamineStepId());
                    //当前并签人员
                    for(Integer userId : TagUtil.toSet(examineStep.getCheckUserId())){
                        AdminExamineLog examineLog = AdminExamineLog.dao.findFirst(Db.getSql("oa.examine.queryNowadayExamineLogByRecordIdAndStepId"), examineRecord.getRecordId(), examineRecord.getExamineStepId(), userId);
                        if(examineLog.getExamineStatus() == 0){
                            //并签未走完
                            flag = false;
                            break;
                        }
                    }
                    //并签未完成
                    if(! flag){
                        examineRecord.setExamineStatus(3);
                    }
                }
                if(flag){
                    //判断是否有下一步流程
                    if(nextExamineStep != null){
                        //有下一步流程
                        examineRecord.setExamineStatus(3);
                        examineRecord.setExamineStepId(nextExamineStep.getStepId());
                        Integer stepType = nextExamineStep.getStepType();
                        //生成审批日志
                        if(stepType == 1){
                            checkUserIds = Db.queryInt("select parent_id from 72crm_admin_user where user_id = ?", createUserId) + "";
                        }else if(stepType == 4){
                            AdminUser adminUser = AdminUser.dao.findById(nowadayExamineLog.getExamineUser());
                            if (adminUser != null && adminUser.getParentId() != null) {
                                checkUserIds = adminUser.getParentId()+"";
                            }else {
                                checkUserIds = BaseConstant.SUPER_ADMIN_USER_ID+"";
                            }
                        }else{
                            checkUserIds = nextExamineStep.getCheckUserId();
                        }
                    }
                    if("0".equals(checkUserIds) && ! createUserId.equals(BaseConstant.SUPER_ADMIN_USER_ID)){
                        checkUserIds = BaseConstant.SUPER_ADMIN_USER_ID + "";
                    }
                }
            }
            if(((examineCategory.getExamineType() == 2) || (examineCategory.getExamineType() == 1 && flag)) && StrUtil.isEmpty(checkUserIds)){
                //没有上级，审核通过
                examineRecord.setExamineStatus(1);
            }else{
                examineRecord.setExamineStatus(3);
                //把下一步审批人放入操作记录中
                OaActionRecord oaActionRecord = new OaActionRecord().findFirst("select * from `72crm_oa_action_record` where type = 5 and action_id = ? and content = '添加了审批' limit 1", examineId);
                String joinUserIds = oaActionRecord.getJoinUserIds();
                joinUserIds += checkUserIds;
                oaActionRecord.setJoinUserIds(TagUtil.fromString(joinUserIds));
                oaActionRecord.update();
                //添加审核日志
                for(Integer userId : TagUtil.toSet(checkUserIds)){
                    OaExamineLog oaExamineLog = new OaExamineLog();
                    oaExamineLog.setRecordId(examineRecord.getRecordId());
                    oaExamineLog.setOrderId(nowadayExamineLog.getOrderId() + 1);
                    if(nextExamineStep != null){
                        oaExamineLog.setOrderId(nextExamineStep.getStepNum());
                        oaExamineLog.setExamineStepId(nextExamineStep.getStepId());
                    }
                    oaExamineLog.setExamineStatus(0);
                    oaExamineLog.setCreateUser(BaseUtil.getUser().getUserId());
                    oaExamineLog.setCreateTime(new Date());
                    oaExamineLog.setExamineUser(Long.valueOf(userId));
                    oaExamineLog.save();
                }
            }

        }

        return examineRecord.update() ? R.ok() : R.error();
    }

    public R queryOaExamineInfo(String id){
        Record oaExamineInfo = Db.findFirst(Db.getSql("oa.examine.queryExamineById"), id);
        oaExamineInfo.set("createUser", Db.findFirst("select user_id,realname,img from 72crm_admin_user where user_id = ?", oaExamineInfo.getLong("create_user_id")));
        String batchId = oaExamineInfo.getStr("batch_id");
        setRelation(oaExamineInfo);
        Record first = Db.findFirst("select * from 72crm_oa_examine_record where examine_id = ?", id);
        oaExamineInfo.set("record", first);
        oaExamineInfo.set("examine_record_id", first.get("record_id"));
        adminFileService.queryByBatchId(batchId, oaExamineInfo);
        List<Record> examineTravelList = Db.find("select * from 72crm_oa_examine_travel where examine_id = ?", id);
        examineTravelList.forEach(record -> adminFileService.queryByBatchId(record.getStr("batch_id"), record));
        oaExamineInfo.set("examineTravelList", examineTravelList);
        return R.ok().put("data", oaExamineInfo);
    }

    public R getField(String id, Integer isDetail){
        Record oaExamineInfo = Db.findFirst("select * from 72crm_oa_examine where examine_id = ?", id);
        String categoryId = oaExamineInfo.getStr("category_id");
        OaExamineCategory oaExamineCategory = new OaExamineCategory().findById(categoryId);
        List<Record> examineTravelList = Db.find(Db.getSql("oa.examine.queryTravel"), oaExamineInfo.getInt("examine_id"));
        examineTravelList.forEach(record -> adminFileService.queryByBatchId(record.getStr("batchId"), record));
        List<Record> recordList = new ArrayList<>();
        FieldUtil fieldUtil = new FieldUtil(recordList);
        String[] arr = new String[0];
        switch(oaExamineCategory.getType()){
            case 1:
                fieldUtil.oaFieldAdd("content", "审批内容", "text", arr, 1, 0, oaExamineInfo.get("content"), "", 3, 1)
                        .oaFieldAdd("remark", "备注", "textarea", arr, 0, 0, oaExamineInfo.get("remark"), "", 3, 1);
                break;
            case 2:
                fieldUtil.oaFieldAdd("type_id", "请假类型", "select", new String[]{"年假", "事假", "病假", "产假", "调休", "婚假", "丧假", "其他"}, 1, 0, oaExamineInfo.get("type_id"), "", 3, 1)
                        .oaFieldAdd("content", "审批内容", "text", arr, 1, 0, oaExamineInfo.get("content"), "", 3, 1)
                        .oaFieldAdd("start_time", "开始时间", "datetime", arr, 1, 0, oaExamineInfo.get("start_time"), "", 3, 1)
                        .oaFieldAdd("end_time", "结束时间", "datetime", arr, 1, 0, oaExamineInfo.get("end_time"), "", 3, 1)
                        .oaFieldAdd("duration", "时长(天)", "floatnumber", arr, 1, 0, String.valueOf(oaExamineInfo.getBigDecimal("duration")), "", 3, 1)
                        .oaFieldAdd("remark", "备注", "textarea", arr, 0, 0, oaExamineInfo.get("remark"), "", 3, 1);
                break;
            case 3:
                fieldUtil.oaFieldAdd("content", "出差事由", "text", arr, 1, 0, oaExamineInfo.get("content"), "", 3, 1)
                        .oaFieldAdd("remark", "备注", "textarea", arr, 0, 0, oaExamineInfo.get("remark"), "", 3, 1)
                        .oaFieldAdd("cause", "行程明细", "business_cause", arr, 1, 0, examineTravelList, "", 3, 1)
                        .oaFieldAdd("duration", "时长(天)", "floatnumber", arr, 1, 0, String.valueOf(oaExamineInfo.getBigDecimal("duration")), "", 3, 1);
                break;
            case 4:
                fieldUtil.oaFieldAdd("content", "加班原因", "text", arr, 1, 0, oaExamineInfo.get("content"), "", 3, 1)
                        .oaFieldAdd("start_time", "开始时间", "datetime", arr, 1, 0, oaExamineInfo.get("start_time"), "", 3, 1)
                        .oaFieldAdd("end_time", "结束时间", "datetime", arr, 1, 0, oaExamineInfo.get("end_time"), "", 3, 1)
                        .oaFieldAdd("duration", "加班总天数", "floatnumber", arr, 1, 0, oaExamineInfo.get("duration"), "", 3, 1)
                        .oaFieldAdd("remark", "备注", "textarea", arr, 0, 0, oaExamineInfo.get("remark"), "", 3, 1);
                break;
            case 5:
                fieldUtil.oaFieldAdd("content", "差旅事由", "text", arr, 1, 0, oaExamineInfo.get("content"), "", 3, 1)
                        .oaFieldAdd("cause", "费用明细", "examine_cause", arr, 1, 0, examineTravelList, "", 3, 1)
                        .oaFieldAdd("money", "报销总金额", "floatnumber", arr, 1, 0, String.valueOf(oaExamineInfo.getInt("money")), "", 3, 1)
                        .oaFieldAdd("remark", "备注", "textarea", arr, 0, 0, oaExamineInfo.get("remark"), "", 3, 1);
                break;
            case 6:
                fieldUtil.oaFieldAdd("content", "借款事由", "text", arr, 1, 0, oaExamineInfo.get("content"), "", 3, 1)
                        .oaFieldAdd("money", "借款金额（元）", "floatnumber", arr, 1, 0, String.valueOf(oaExamineInfo.getInt("money")), "", 3, 1)
                        .oaFieldAdd("remark", "备注", "textarea", arr, 0, 0, oaExamineInfo.get("remark"), "", 3, 1);
                break;
            default:
                fieldUtil.oaFieldAdd("content", "审批事由", "textarea", arr, 1, 0, oaExamineInfo.get("content"), "", 1, 1)
                        .oaFieldAdd("remark", "备注", "textarea", arr, 1, 0, oaExamineInfo.get("remark"), "", 1, 1);
                List<Record> recordList1 = Db.find(Db.getSql("oa.examine.queryFieldByBatchId"), categoryId, oaExamineInfo.getStr("batch_id"));
                adminFieldService.recordToFormType(recordList1);
                adminFieldService.transferFieldList(recordList1, isDetail);
                recordList.addAll(recordList1);
                break;
        }
        return R.ok().put("data", fieldUtil.getRecordList());
    }


    private void setRelation(Record relationRecord){
        List<Record> recordList = Db.find("select * from 72crm_oa_examine_relation where examine_id = ?", relationRecord.getInt("examine_id"));
        for(Record record : recordList){
            List<CrmCustomer> customerList = new ArrayList<>();
            if(record.getStr("customer_ids") != null && ! record.getStr("customer_ids").isEmpty()){
                for(Integer customerId : TagUtil.toSet(record.getStr("customer_ids"))){
                    CrmCustomer crmCustomer = CrmCustomer.dao.findById(customerId);
                    if(crmCustomer != null){
                        customerList.add(crmCustomer);
                    }
                }
            }
            relationRecord.set("customerList", customerList);
            List<CrmContacts> contactsList = new ArrayList<>();
            if(record.getStr("contacts_ids") != null && ! record.getStr("contacts_ids").isEmpty()){
                for(Integer contactsId : TagUtil.toSet(record.getStr("contacts_ids"))){
                    CrmContacts crmContacts = CrmContacts.dao.findById(contactsId);
                    if(crmContacts != null){
                        contactsList.add(crmContacts);
                    }
                }
            }
            relationRecord.set("contactsList", contactsList);
            List<CrmBusiness> businessList = new ArrayList<>();
            if(record.getStr("business_ids") != null && ! record.getStr("business_ids").isEmpty()){
                for(Integer businessId : TagUtil.toSet(record.getStr("business_ids"))){
                    CrmBusiness crmBusiness = CrmBusiness.dao.findById(Integer.valueOf(businessId));
                    if(crmBusiness != null){
                        businessList.add(crmBusiness);
                    }
                }
            }
            relationRecord.set("businessList", businessList);
            List<CrmContract> contractList = new ArrayList<>();
            if(record.getStr("contract_ids") != null && ! record.getStr("contract_ids").isEmpty()){
                for(Integer contractId : TagUtil.toSet(record.getStr("contract_ids"))){
                    CrmContract crmContract = CrmContract.dao.findById(Integer.valueOf(contractId));
                    if(crmContract != null){
                        contractList.add(crmContract);
                    }
                }
            }
            relationRecord.set("contractList", contractList);
        }

    }

    @Before(Tx.class)
    public R deleteOaExamine(Integer oaExamineId){
        Integer recordId = Db.queryInt("select record_id from 72crm_oa_examine_record where examine_id  = ? limit 1", oaExamineId);
        Db.delete("delete from `72crm_admin_fieldv` where batch_id = (select `72crm_oa_examine`.batch_id from `72crm_oa_examine` where examine_id = ?)", oaExamineId);
        Db.delete("delete from 72crm_oa_examine where examine_id = ?", oaExamineId);
        Db.delete("delete from 72crm_oa_examine_relation where examine_id = ?", oaExamineId);
        Db.delete("delete from 72crm_oa_examine_travel where examine_id = ?", oaExamineId);
        Db.delete("delete from 72crm_oa_examine_record where record_id = ?", recordId);
        Db.delete("delete from 72crm_oa_examine_log where record_id = ?", recordId);
        oaActionRecordService.deleteRecord(OaEnum.EXAMINE_TYPE_KEY.getTypes(), oaExamineId);
        return R.ok();
    }

    public R queryExaminStep(String categoryId){
        Record record = Db.findFirst("select examine_type from 72crm_oa_examine_category where category_id = ?", categoryId);
        Integer examineType = record.getInt("examine_type");
        if(examineType == 1){
            List<Record> recordList = Db.find("select * from 72crm_oa_examine_step where category_id = ?", categoryId);
            recordList.forEach(step -> {
                List<Record> userList = Db.find(Db.getSqlPara("admin.user.queryByIds", Kv.by("ids", step.getStr("check_user_id").split(","))));
                step.set("userList", userList);
            });
            record.set("examineSteps", recordList);
        }
        return R.ok().put("data", record);
    }

    public R queryExamineLogList(Integer recordId){
        Integer examineType = Db.queryInt(Db.getSql("oa.examine.queryExamineTypeByRecordId"), recordId);
        List<Record> logs = null;
        if(examineType == 1){
            logs = Db.find(Db.getSql("oa.examine.queryExamineLogByRecordIdByStep"), recordId);
        }else{
            logs = Db.find(Db.getSql("oa.examine.queryExamineLogByRecordIdByStep1"), recordId);
        }
        return R.ok().put("data", logs);
    }


    public R queryExamineRecordList(Integer recordId){
        JSONObject jsonObject = new JSONObject();
        Record examineRecord = Db.findFirst(Db.getSql("oa.examine.queryExamineRecordById"), recordId);
        Integer examineStatus = examineRecord.getInt("examine_status");
        //如果当前审批已撤回
        if(examineRecord.getInt("examine_status") == 4){
            jsonObject.put("examineType", 1);
            Record user = Db.findFirst(Db.getSql("oa.examine.queryUserByRecordId"), recordId);
            examineRecord.set("userList", user);
            List<Record> records = new ArrayList<>();
            records.add(examineRecord);
            jsonObject.put("steps", records);
            return R.ok().put("data", jsonObject);
        }
        OaExamine oaExamine = OaExamine.dao.findById(examineRecord.getInt("examine_id"));
        OaExamineCategory oaExamineCategory = OaExamineCategory.dao.findById(oaExamine.getCategoryId());
        List<Record> list = new ArrayList<>();
        Record rec = Db.findFirst(Db.getSql("oa.examine.queryRecordAndId"), recordId);
        Long auditUserId = BaseUtil.getUser().getUserId();
        //jsonObject.put("isRecheck",0);
        //判断是否有撤回权限
        if((auditUserId.equals(examineRecord.getLong("create_user")) || auditUserId.equals(BaseConstant.SUPER_ADMIN_USER_ID)) && (examineStatus == 0 || examineStatus == 3)){
            jsonObject.put("isRecheck", 1);
        }else{
            jsonObject.put("isRecheck", 0);
        }
        if(oaExamineCategory.getExamineType() == 2){
            Record log = Db.findFirst(Db.getSqlPara("oa.examine.queryRecordByUserIdAndStatus", Kv.by("create_user", rec.getInt("create_user")).set("examineTime", rec.getDate("examineTime"))));
            rec.set("examinUser", log);
            list.add(rec);
            //授权审批
            List<Record> logs = Db.find(Db.getSql("oa.examine.queryExamineLogAndUserByRecordId"), recordId);
            logs.forEach(r -> {
                r.set("examinUser", Db.findFirst(Db.getSql("oa.examine.queryExamineLogAndUserByLogId"), r.getInt("log_id")));
            });
            list.addAll(logs);
            SqlPara sqlPara = Db.getSqlPara("oa.examine.queryExamineLog", Kv.by("recordId", recordId).set("examineUser", auditUserId).set("stepId", examineRecord.get("examine_step_id")));
            Record oaExamineLog = Db.findFirst(sqlPara);
            if(oaExamineLog != null){
                jsonObject.put("isCheck", 1);
            }else{
                jsonObject.put("isCheck", 0);
            }
            jsonObject.put("examineType", 2);
            jsonObject.put("steps", list);
        }else{
            jsonObject.put("examineType", 1);
            //固定审批
            List<Record> steps = Db.find("select * from 72crm_oa_examine_step where  category_id = ? ORDER BY step_num", oaExamineCategory.getCategoryId());
            //上一审核步骤
            AtomicReference<Long> lsatuUserId = null;

            steps.forEach(step -> {
                List<Record> logs = new ArrayList<>();
                if(step.getInt("step_type") == 1){
                    //负责人主管
                    logs = Db.find(Db.getSql("oa.examine.queryUserByRecordIdAndStepIdAndStatus"), recordId, step.getInt("step_id"));
                    //已经创建审核日志
                    if(logs != null && logs.size() > 0){
                        for(Record record : logs){
                            step.set("examine_status", record.getInt("examine_status"));
                        }
                        step.set("userList", logs);
                    }else{
                        step.set("examine_status", 0);
                        //还未创建审核日志
                        //查询负责人主管
                        List<Record> r = Db.find(Db.getSql("oa.examine.queryUserByUserId"), oaExamine.getCreateUserId());
                        if(r == null || r.size() == 0){
                            r = Db.find(Db.getSql("oa.examine.queryUserByUserIdAnd"), BaseConstant.SUPER_ADMIN_USER_ID);
                        }
                        step.set("userList", r);
                    }
                }else if(step.getInt("step_type") == 2 || step.getInt("step_type") == 3){
                    //先判断是否已经审核过
                    logs = Db.find(Db.getSql("oa.examine.queryUserByRecordIdAndStepIdAndStatus"), recordId, step.getInt("step_id"));
                    if(logs != null && logs.size() != 0){
                        //已经创建审核日志
                        int status = 0;
                        if(step.getInt("step_type") == 2){
                            int i = 0;
                            for(Record record : logs){
                                if(record.getInt("examine_status") == 1){
                                    status = 1;
                                }
                                if(record.getInt("examine_status") == 2){
                                    i++;
                                }
                            }
                            if(i == logs.size()){
                                status = 2;
                            }
                        }
                        if(step.getInt("step_type") == 3){
                            int i = 0;
                            for(Record record : logs){
                                if(record.getInt("examine_status") == 2){
                                    status = 2;
                                }
                                if(record.getInt("examine_status") == 1){
                                    i++;
                                }
                            }
                            if(i == logs.size()){
                                status = 1;
                            }
                        }
                        step.set("examine_status", status);
                        step.set("userList", logs);
                    }else{
                        //该步骤还未审核
                        logs = new ArrayList<>();
                        for(Integer userId : TagUtil.toSet(step.getStr("check_user_id"))){
                            logs.add(Db.findFirst(Db.getSql("oa.examine.queryUserByUserIdAndStatus"), userId));
                        }
                        step.set("examine_status", 0);
                        step.set("userList", logs);
                    }
                }else{
                    //主管的主管
                    logs = Db.find(Db.getSql("oa.examine.queryUserByRecordIdAndStepIdAndStatus"), recordId, step.getInt("step_id"));
                    //已经创建审核日志
                    if(logs != null && logs.size() != 0){
                        for(Record record : logs){
                            step.set("examine_status", record.getInt("examine_status"));
                        }
                        step.set("userList", logs);
                    }else{
                        step.set("examine_status", 0);
                        //还未创建审核日志
                        //查询负责人主管的主管
                        AdminUser adminUser = AdminUser.dao.findById(lsatuUserId);
                        if (adminUser != null && adminUser.getParentId() != null) {
                            logs = Db.find(Db.getSql("admin.examineLog.queryUserByUserIdAnd"),adminUser.getParentId());
                        }else {
                            logs = Db.find(Db.getSql("admin.examineLog.queryUserByUserIdAnd"), lsatuUserId.get());
                        }
                        //查询负责人主管的主管
                        step.set("userList", logs );
                    }
                }
                if (logs.size()>0){
                    lsatuUserId.set(logs.get(0).getLong("user_id"));
                }
            });
            SqlPara sqlPara = Db.getSqlPara("oa.examine.queryExamineLog", Kv.by("recordId", recordId).set("examineUser", auditUserId).set("stepId", examineRecord.get("examine_step_id")));
            Record oaExamineLog = Db.findFirst(sqlPara);
            if(oaExamineLog != null){
                jsonObject.put("isCheck", 1);
            }else{
                jsonObject.put("isCheck", 0);
            }
            List<Record> logs = Db.find(Db.getSqlPara("oa.examine.queryRecordByUserIdAndStatus", Kv.by("create_user", rec.getInt("create_user")).set("examineTime", rec.getDate("examineTime"))));
            rec.set("userList", logs);
            list.add(rec);
            list.addAll(steps);
            jsonObject.put("steps", list);
        }
        return R.ok().put("data", jsonObject);

    }


    public R queryExamineRelation(BasePageRequest<OaExamineRelation> pageRequest){
        OaExamineRelation relation = pageRequest.getData();
        if(AuthUtil.oaAuth(relation.toRecord())){
            return R.noAuth();
        }
        Page<Record> paginate = Db.paginate(pageRequest.getPage(), pageRequest.getLimit(), Db.getSqlPara("oa.examine.queryExamineRelation", Kv.by("businessIds", relation.getBusinessIds()).set("contactsIds", relation.getContactsIds()).set("contractIds", relation.getContractIds()).set("customerIds", relation.getCustomerIds())));
        transfer(paginate.getList());
        return R.ok().put("data", paginate);
    }

    /**
     * 递归查询上一审核人主管第一级审批步骤
     */
    private Long queryStep(OaExamineStep step, Integer recordId, Long ownerUserId){
        Record r = Db.findFirst(Db.getSqlPara("oa.examine.queryExamineUserByExamineStepId",
                Kv.by("recordId",recordId).set("examineStepId",step.getStepId()).set("examineStatus",1)));
        if (r == null) {
            //获取上一个审核步骤
            OaExamineStep lastStep = OaExamineStep.dao.findFirst(Db.getSql("oa.examine.queryExamineStepByLastExamineIdOrderByStepId"), step.getCategoryId(), step.getStepId());
            if (lastStep == null){
                AdminUser adminUser = AdminUser.dao.findById(ownerUserId);
                if (adminUser.getParentId() == null){
                    return ownerUserId;
                }else {
                    return adminUser.getParentId();
                }
            }
            Long userId = queryStep(lastStep, recordId,ownerUserId);
            if (userId != null) {
                AdminUser adminUser = AdminUser.dao.findById(userId);
                if (adminUser == null || adminUser.getParentId() == null){
                    return userId;
                }else {
                    return adminUser.getParentId();
                }
            }else{
                AdminUser adminUser = AdminUser.dao.findById(ownerUserId);
                if (adminUser.getParentId() == null){
                    return ownerUserId;
                }else {
                    return adminUser.getParentId();
                }
            }
        }else {
            //已经生成审核
            AdminUser adminUser = AdminUser.dao.findById(r.getLong("examine_user"));
            if (adminUser.getParentId() == null){
                return r.getLong("examine_user");
            }else {
                return adminUser.getParentId();
            }
        }
    }


}
