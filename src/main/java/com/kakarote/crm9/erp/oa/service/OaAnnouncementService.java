package com.kakarote.crm9.erp.oa.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.jfinal.aop.Aop;
import com.jfinal.kit.Kv;
import com.kakarote.crm9.erp.admin.common.AdminMessageEnum;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.oa.common.OaEnum;
import com.kakarote.crm9.erp.oa.entity.OaAnnouncement;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kakarote.crm9.utils.TagUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * 公告
 */
public class OaAnnouncementService {

    //添加日志
    @Inject
    private OaActionRecordService oaActionRecordService;

    /**
     * 添加或修改
     */
    public R saveAndUpdate(OaAnnouncement oaAnnouncement) {
        boolean flag;
        if (oaAnnouncement.getStartTime() != null && oaAnnouncement.getEndTime() != null) {
            if ((oaAnnouncement.getStartTime().compareTo(oaAnnouncement.getEndTime())) > 0) {
                return R.error("结束时间早于开始时间");
            }
        } else {
            return R.error("开始时间或结束时间为空！");
        }
        oaAnnouncement.setDeptIds(TagUtil.fromString(oaAnnouncement.getDeptIds()));
        oaAnnouncement.setOwnerUserIds(TagUtil.fromString(oaAnnouncement.getOwnerUserIds()));
        if (oaAnnouncement.getAnnouncementId() == null) {
            oaAnnouncement.setCreateTime(DateUtil.date());
            flag = oaAnnouncement.save();
        } else {
            oaAnnouncement.setUpdateTime(DateUtil.date());
            flag = oaAnnouncement.update();
        }
        AdminUser user = BaseUtil.getUser();
        String userIds;
        String deptIds;
        if(StrUtil.isEmpty(oaAnnouncement.getOwnerUserIds()) && StrUtil.isEmpty(oaAnnouncement.getDeptIds())){
            userIds = Db.queryStr("select group_concat(user_id) from `72crm_admin_user`");
            deptIds = Db.queryStr("select group_concat(dept_id) from `72crm_admin_dept`");
        }else {
            userIds = oaActionRecordService.getJoinIds(user.getUserId(), oaAnnouncement.getOwnerUserIds());
            deptIds = oaActionRecordService.getJoinIds(Long.valueOf(user.getDeptId()), oaAnnouncement.getDeptIds());
        }

        oaActionRecordService.addRecord(oaAnnouncement.getAnnouncementId(), OaEnum.ANNOUNCEMENT_TYPE_KEY.getTypes(), oaAnnouncement.getUpdateTime() == null ? 1 : 2, userIds, deptIds);
        return R.isSuccess(flag);
    }

    /**
     * 删除
     */
    @Before(Tx.class)
    public R delete(Integer id) {
        oaActionRecordService.deleteRecord(OaEnum.ANNOUNCEMENT_TYPE_KEY.getTypes(), id);
        return R.ok().put("status", OaAnnouncement.dao.deleteById(id) ? 1 : 0);
    }

    /**
     * 根据ID查询详情
     */
    public R queryById(Integer id) {
        return R.ok().put("data", Db.findFirst(Db.getSql("oa.announcement.queryById"), id));
    }

    /**
     * 倒叙查询公告集合
     * @author zhangzhiwei
     */
    public R queryList(BasePageRequest<OaAnnouncement> page, Integer type) {
        AdminUser adminUser=BaseUtil.getUser();
        Kv kv=Kv.by("type",type).set("deptId",adminUser.getDeptId()).set("userId",adminUser.getUserId());
        Page<Record> recordPage=Db.paginate(page.getPage(),page.getLimit(),Db.getSqlPara("oa.announcement.queryList",kv));
        return R.ok().put("data", recordPage);
    }

    public void readAnnouncement(Integer announcementId) {
        OaAnnouncement oaAnnouncement = OaAnnouncement.dao.findById(announcementId);
        HashSet<String> hashSet = new HashSet<>(StrUtil.splitTrim(oaAnnouncement.getReadUserIds(), ","));
        hashSet.add(BaseUtil.getUser().getUserId().toString());
        oaAnnouncement.setReadUserIds("," + String.join(",", hashSet) + ",");
        oaAnnouncement.update();
    }
}
