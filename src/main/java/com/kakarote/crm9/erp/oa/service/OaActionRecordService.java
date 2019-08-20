package com.kakarote.crm9.erp.oa.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.erp.oa.common.OaEnum;
import com.kakarote.crm9.erp.oa.entity.OaActionRecord;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.kakarote.crm9.utils.TagUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OaActionRecordService {

    @Inject
    private OaLogService oaLogService;

    @Inject
    private AdminUserService userService;

    @Inject
    private OaEventService oaEventService;

    /**
     * 添加日志记录
     *
     * @param actionId 操作对象id
     * @param types    操作类型
     * @param status   1 添加 2 更新
     */
    public void addRecord(Integer actionId, Integer types, Integer status, String joinUserIds, String deptIds) {
        OaActionRecord oaActionRecord = new OaActionRecord();
        oaActionRecord.setUserId(BaseUtil.getUser().getUserId().intValue());
        oaActionRecord.setType(types);
        oaActionRecord.setActionId(actionId);
        oaActionRecord.setCreateTime(new Date());
        oaActionRecord.setJoinUserIds(joinUserIds);
        oaActionRecord.setDeptIds(deptIds);
        if (status == 1) {
            oaActionRecord.setContent("添加了" + OaEnum.getName(types));
        } else if (status == 2) {
            oaActionRecord.setContent("更新了" + OaEnum.getName(types));
        }
        oaActionRecord.save();
    }

    public String getJoinIds(Integer id, String ids) {
        StringBuilder joinIds = new StringBuilder(",").append(id);
        if (StrUtil.isNotEmpty(ids)) {
            joinIds.append(ids);
        }else {
            joinIds.append(",");
        }
        return TagUtil.fromString(joinIds.toString());
    }


    public R getOaRecordPageList(BasePageRequest<OaActionRecord> pageRequest) {
        Integer type = pageRequest.getData().getType();
        AdminUser user = BaseUtil.getUser();
        SqlPara sqlPara;
        List<Long> userIdList;
        if(user.getRoles().contains(BaseConstant.SUPER_ADMIN_ROLE_ID)){
            userIdList = Db.query("SELECT user_id FROM `72crm_admin_user` where user_id != ? ",user.getUserId());
        }else {
            userIdList = new AdminUserService().queryUserByParentUser(user.getUserId(), BaseConstant.AUTH_DATA_RECURSION_NUM);
        }
        userIdList.add(user.getUserId());
        if (type.equals(OaEnum.ALL_TYPE_KEY.getTypes())) {
            sqlPara = Db.getSqlPara("oa.record.queryList", Kv.by("userId", user.getUserId()).set("deptId", user.getDeptId()).set("userIds", userIdList));
        } else {
            sqlPara = Db.getSqlPara("oa.record.queryList", Kv.by("userId", user.getUserId()).set("deptId", user.getDeptId()).set("type", type).set("userIds", userIdList));
        }
        Page<Record> pageData = Db.paginate(pageRequest.getPage(), pageRequest.getLimit(), sqlPara);
        pageData.getList().forEach(record -> {
            record.set("type_name", OaEnum.getName(record.getInt("type")));
            Integer userId = record.getInt("user_id");
            record.set("createUser", Db.findFirst("select user_id,realname,img from 72crm_admin_user where user_id = ?", userId));
            Record info = new Record();
            Integer actionId = record.getInt("action_id");
            Integer recordType = record.getInt("type");
            if (recordType.equals(OaEnum.LOG_TYPE_KEY.getTypes())) {
                info = Db.findFirst(Db.getSqlPara("oa.log.queryList", Kv.by("logId", actionId)));
                if (info!=null){
                    oaLogService.queryLogDetail(info, BaseUtil.getUser().getUserId());
                }
            } else if (recordType.equals(OaEnum.EXAMINE_TYPE_KEY.getTypes())) {
                info = Db.findFirst("select content as title from 72crm_oa_examine where examine_id = ?", actionId);
            } else if (recordType.equals(OaEnum.TASK_TYPE_KEY.getTypes())) {
                info = Db.findFirst("select name as title from 72crm_task where task_id = ?", actionId);
            } else if (recordType.equals(OaEnum.EVENT_TYPE_KEY.getTypes())) {
                info = Db.findFirst("select title  from 72crm_oa_event where event_id = ?", actionId);
                if (info!=null){
                    Record first = Db.findFirst(Db.getSql("oa.event.queryById"), actionId);
                    first.remove("type");
                    oaEventService.queryRelateList(first);
                    info.setColumns(first);
                }
            } else if (recordType.equals(OaEnum.ANNOUNCEMENT_TYPE_KEY.getTypes())) {
                info = Db.findFirst("select title,content as annContent from 72crm_oa_announcement where announcement_id = ?", actionId);
            }
            if (info != null) {
                Date createTime = record.getDate("create_time");
                record.setColumns(info).set("create_time",createTime);
            }
        });
        return R.ok().put("data", pageData);
    }

    public R queryEvent(String month) {
        DateTime dateTime;
        if (month == null) {
            dateTime = DateUtil.beginOfMonth(new Date());
        } else {
            dateTime = DateUtil.parse(month, "yyyy-MM");
        }
        Long userId = BaseUtil.getUser().getUserId();
        int nowMonth = dateTime.month();
        StringBuilder sql = new StringBuilder();
        do {
            sql.append(" select (select '").append(dateTime.toSqlDate()).append("' )as date,if(count(*)>0,1,0) as status from 72crm_oa_event where (create_user_id = ").append(userId).append(" or owner_user_ids like concat('%',").append(userId).append(",'%')) and '").append(dateTime.toSqlDate()).append("' between date_format(start_time,'%Y-%m-%d') and date_format(end_time,'%Y-%m-%d') ").append("union all");
            dateTime = DateUtil.offsetDay(dateTime, 1);
        } while (dateTime.month() == nowMonth);
        sql.delete(sql.length() - 9, sql.length());
        List<Record> recordList = Db.find(sql.toString());
        return R.ok().put("data", recordList);
    }

    public R queryEventByDay(String day) {
        Long userId = BaseUtil.getUser().getUserId();
        List<Record> recordList = Db.find("select event_id,title,date_format(start_time,'%Y-%m-%d') as start_time ,date_format(end_time,'%Y-%m-%d') as end_time ,owner_user_ids from 72crm_oa_event where  (create_user_id = ? or owner_user_ids like concat('%', ?, '%')) and  ? between date_format(start_time,'%Y-%m-%d') and date_format(end_time,'%Y-%m-%d')", userId, userId,day);

        recordList.forEach(record -> {
            StringBuilder realnames = new StringBuilder();
            if (StrUtil.isNotEmpty(record.getStr("owner_user_ids"))) {
                String[] ownerUserIds = record.getStr("owner_user_ids").split(",");
                for (String ownerUserId : ownerUserIds) {
                    if (StrUtil.isNotBlank(ownerUserId)) {
                        String realname = Db.queryStr("select realname from 72crm_admin_user where user_id = ?", ownerUserId);
                        realnames.append(realname).append("、");
                    }
                }
                realnames.delete(realnames.length() - 1, realnames.length());
            }
            record.set("realnames", realnames.toString());

        });
        return R.ok().put("data", recordList);
    }


    public R queryTask() {
        Long userId = BaseUtil.getUser().getUserId();
        List<Record> recordList = Db.find(Db.getSqlPara("oa.record.queryTask", Kv.by("userId", userId)));
        return R.ok().put("data", recordList);
    }

    public void deleteRecord(Integer type, Integer id) {
        Db.delete("delete from 72crm_oa_action_record where type = ? and action_id = ?", type, id);
    }


}
