package com.kakarote.crm9.erp.oa.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminFileService;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.erp.oa.common.OaEnum;
import com.kakarote.crm9.erp.oa.entity.OaLog;
import com.kakarote.crm9.erp.oa.entity.OaLogRelation;
import com.kakarote.crm9.utils.AuthUtil;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.kakarote.crm9.utils.TagUtil;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

public class OaLogService {
    //添加日志
    @Inject
    private OaActionRecordService oaActionRecordService;
    @Inject
    private AdminFileService adminFileService;
    @Inject
    private OaCommentService commentService;

    /**
     * 查询日志
     *
     * @param basePageRequest 分页参数
     * @author zhangzhiwei
     */
    public Page<Record> queryList(BasePageRequest<OaLog> basePageRequest) {
        JSONObject object = basePageRequest.getJsonObject();
        AdminUser user = BaseUtil.getUser();
        Integer by = TypeUtils.castToInt(object.getOrDefault("by", 4));
        Kv kv = Kv.by("by", by);
        List<Long> userIds;
        if(user.getRoles().contains(BaseConstant.SUPER_ADMIN_ROLE_ID)){
            userIds = Db.query("SELECT user_id FROM `72crm_admin_user` where user_id != ? ",user.getUserId());
        }else {
            userIds = new AdminUserService().queryUserByParentUser(user.getUserId(), BaseConstant.AUTH_DATA_RECURSION_NUM);
            if (object.containsKey("createUserId")) {
                if(!userIds.contains(object.getLong("createUserId"))){
                    return new Page<>();
                }
            }
        }
        if (by == 1) {
            kv.set("create_user_id", user.getUserId());
        } else if (by == 2) {
            kv.set("send_user_ids", user.getUserId()).set("send_dept_ids", user.getDeptId()).set("userIds", userIds);
        } else if (by == 3) {
            kv.set("send_user_ids", user.getUserId()).set("send_dept_ids", user.getDeptId()).set("userIds", userIds).set("userId", user.getUserId());
        } else {
            userIds.add(user.getUserId());
            kv.set("send_user_ids", user.getUserId()).set("send_dept_ids", user.getDeptId()).set("userIds", userIds);
        }
        if (object.containsKey("createUserId")) {
            kv.set("create_user_id", object.get("createUserId"));
        }
        if (object.containsKey("createTime")) {
            kv.set("create_time", object.get("createTime"));
        }
        if (object.containsKey("categoryId") && !"0".equals(object.get("categoryId"))) {
            kv.set("category_id", object.get("categoryId"));
        }
        Page<Record> recordList = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("oa.log.queryList", kv));
        recordList.getList().forEach((record -> {
            queryLogDetail(record, user.getUserId());
        }));
        return recordList;
    }

    public void queryLogDetail(Record record, Long userId) {
        adminFileService.queryByBatchId(record.get("batch_id"), record);
        record.set("sendUserList", Db.find(Db.getSqlPara("admin.user.queryByIds", Kv.by("ids", record.getStr("send_user_ids")))));
        record.set("sendDeptList", Db.find(Db.getSqlPara("admin.dept.queryByIds", Kv.by("ids", record.getStr("send_dept_ids")))));
        record.set("customerList", Db.find(Db.getSqlPara("crm.customer.queryByIds", Kv.by("ids", record.getStr("customer_ids")))));
        record.set("businessList", Db.find(Db.getSqlPara("crm.business.queryByIds", Kv.by("ids", record.getStr("business_ids")))));
        record.set("contactsList", Db.find(Db.getSqlPara("crm.contact.queryByIds", Kv.by("ids", record.getStr("contacts_ids")))));
        record.set("contractList", Db.find(Db.getSqlPara("crm.contract.queryByIds", Kv.by("ids", record.getStr("contract_ids")))));
        record.set("createUser", Db.findFirst(Db.getSql("admin.user.queryUserByUserId"), record.getStr("create_user_id")));
        Integer isRead = record.getStr("read_user_ids").contains("," + userId + ",") ? 1 : 0;
        int isEdit = userId.equals(record.getLong("create_user_id")) ? 1 : 0;
        int isDel = 0;
        if((System.currentTimeMillis()-1000*3600*72)>record.getDate("create_time").getTime()){
            if(BaseUtil.getUser().getRoles().contains(BaseConstant.SUPER_ADMIN_ROLE_ID)){
                isDel=1;
            }
        }else {
            if(isEdit==1||new AdminUserService().queryUserByParentUser(userId,BaseConstant.AUTH_DATA_RECURSION_NUM).contains(record.getLong("create_user_id"))){
                isDel=1;
            }
        }
        record.set("permission", new JSONObject().fluentPut("is_update", isEdit).fluentPut("is_delete", isDel));
        record.set("is_read", isRead);
        record.set("replyList", commentService.queryCommentList(record.get("log_id").toString(), "2"));

    }

    /**
     * @param object object
     * @return 响应结果
     * @author zhangzhiwei
     */
    @Before(Tx.class)
    public R saveAndUpdate(JSONObject object) {
        AdminUser user = BaseUtil.getUser();
        OaLog oaLog = object.toJavaObject(OaLog.class);
        OaLogRelation oaLogRelation = object.toJavaObject(OaLogRelation.class);
        oaLog.setCreateUserId(user.getUserId());
        oaLog.setCreateTime(new Date());
        oaLog.setReadUserIds(",,");
        oaLog.setSendUserIds(TagUtil.fromString(oaLog.getSendUserIds()));
        oaLog.setSendDeptIds(TagUtil.fromString(oaLog.getSendDeptIds()));
        if (oaLog.getLogId() != null) {
            boolean oaAuth = AuthUtil.isOaAuth(OaEnum.LOG_TYPE_KEY.getTypes(), oaLog.getLogId());
            if(oaAuth){return R.noAuth();}
            oaLog.update();
            oaActionRecordService.addRecord(oaLog.getLogId(), OaEnum.LOG_TYPE_KEY.getTypes(), 2, oaActionRecordService.getJoinIds(user.getUserId(), oaLog.getSendUserIds()),  oaLog.getSendDeptIds());
        } else {
            oaLog.save();
            oaActionRecordService.addRecord(oaLog.getLogId(), OaEnum.LOG_TYPE_KEY.getTypes(), 1, oaActionRecordService.getJoinIds(user.getUserId(), oaLog.getSendUserIds()),  oaLog.getSendDeptIds());
        }
        if (oaLogRelation != null) {
            Db.deleteById("72crm_oa_log_relation", "log_id", oaLog.getLogId());
            oaLogRelation.setLogId(oaLog.getLogId());
            oaLogRelation.setBusinessIds(TagUtil.fromString(oaLogRelation.getBusinessIds()));
            oaLogRelation.setContactsIds(TagUtil.fromString(oaLogRelation.getContactsIds()));
            oaLogRelation.setContractIds(TagUtil.fromString(oaLogRelation.getContractIds()));
            oaLogRelation.setCustomerIds(TagUtil.fromString(oaLogRelation.getCustomerIds()));
            oaLogRelation.setCreateTime(DateUtil.date());
            oaLogRelation.save();
        }
        return R.ok();
    }


    /**
     * 根据id获取日志
     *
     * @param id 日志ID
     * @author zhangzhiwei
     */
    public Record queryById(Integer id) {
        Record record = Db.findFirst(Db.getSqlPara("oa.log.queryList", Kv.by("logId",id)));
        queryLogDetail(record, BaseUtil.getUser().getUserId());
        return record;
    }

    /**
     * 根据id删除日志
     *
     * @param logId 日志ID
     * @author zhangzhiwei
     */
    @Before(Tx.class)
    public boolean deleteById(Integer logId) {
        OaLog oaLog = OaLog.dao.findById(logId);
        if (oaLog != null) {
            oaActionRecordService.deleteRecord(OaEnum.LOG_TYPE_KEY.getTypes(), logId);
            Db.deleteById("72crm_oa_log_relation", "log_id", logId);
            adminFileService.removeByBatchId(oaLog.getBatchId());
            Db.deleteById("72crm_oa_log","log_id",logId);
            commentService.deleteComment(2,logId);
            return true;
        }
        return false;
    }

    /**
     * TODO 目前可能会产生脏读，
     *
     * @param logId 日志ID
     * @author zhangzhiwei
     */
    public void readLog(Integer logId) {
        OaLog oaLog = OaLog.dao.findById(logId);
        HashSet<String> hashSet = new HashSet<>(StrUtil.splitTrim(oaLog.getReadUserIds(), ","));
        hashSet.add(BaseUtil.getUser().getUserId().toString());
        oaLog.setReadUserIds("," + String.join(",", hashSet) + ",");
        oaLog.update();
    }

    /**
     * 查询crm关联日志
     */
    public R queryLogRelation(BasePageRequest<OaLogRelation> basePageRequest) {
        OaLogRelation relation = basePageRequest.getData();
        if(AuthUtil.oaAuth(relation.toRecord())){
            return R.noAuth();
        }
        Page<Record> recordPage = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("oa.log.queryLogRelation", Kv.by("businessIds", relation.getBusinessIds()).set("contactsIds", relation.getContactsIds()).set("contractIds", relation.getContractIds()).set("customerIds", relation.getCustomerIds())));
        AdminUser user = BaseUtil.getUser();
        recordPage.getList().forEach((record -> {
            queryLogDetail(record, user.getUserId());
        }));
        return R.ok().put("data", recordPage);
    }
}
