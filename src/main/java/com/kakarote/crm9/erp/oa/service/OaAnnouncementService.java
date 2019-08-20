package com.kakarote.crm9.erp.oa.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.erp.admin.entity.AdminRole;
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
    public R saveAndUpdate(OaAnnouncement oaAnnouncement){
        AdminRole adminRole = AdminRole.dao.findFirst(Db.getSql("admin.role.queryAnnouncementByUserId"),oaAnnouncement.getCreateUserId());
        if (adminRole == null && !BaseUtil.getUser().getUserId().equals(BaseConstant.SUPER_ADMIN_USER_ID)){
            return R.error("没有发布公告权限，不能发布公告！");
        }
        boolean flag;
        if (oaAnnouncement.getStartTime() != null && oaAnnouncement.getEndTime() != null){
            if((oaAnnouncement.getStartTime().compareTo(oaAnnouncement.getEndTime()))==1){
                return R.error("结束时间早于开始时间");
            }
        }
        if (oaAnnouncement.getStartTime() == null ){
          return  R.error("开始时间不能为空！");
        }
        if (oaAnnouncement.getEndTime() == null ){
            return  R.error("结束时间不能为空！");
        }
        oaAnnouncement.setDeptIds(TagUtil.fromString(oaAnnouncement.getDeptIds()));
        oaAnnouncement.setOwnerUserIds(TagUtil.fromString(oaAnnouncement.getOwnerUserIds()));
        if (oaAnnouncement.getAnnouncementId() == null){
            oaAnnouncement.setCreateTime(DateUtil.date());
            flag = oaAnnouncement.save();
        }else {
            oaAnnouncement.setUpdateTime(DateUtil.date());
            flag = oaAnnouncement.update();
        }
        AdminUser user = BaseUtil.getUser();
        oaActionRecordService.addRecord(oaAnnouncement.getAnnouncementId(), OaEnum.ANNOUNCEMENT_TYPE_KEY.getTypes(),oaAnnouncement.getUpdateTime()==null?1:2,oaActionRecordService.getJoinIds(user.getUserId().intValue(),oaAnnouncement.getOwnerUserIds()),oaActionRecordService.getJoinIds(user.getDeptId(),oaAnnouncement.getDeptIds()));
        return R.isSuccess(flag);
    }
    /**
     * 删除
     */
    @Before(Tx.class)
    public R delete(Integer id){
        AdminRole adminRole = AdminRole.dao.findFirst(Db.getSql("admin.role.queryAnnouncementByUserId"), BaseUtil.getUser().getUserId().intValue());
        if (adminRole == null && !BaseUtil.getUser().getUserId().equals(BaseConstant.SUPER_ADMIN_USER_ID)){
            return R.error("没有删除公告权限，不能删除公告！");
        }
        oaActionRecordService.deleteRecord(OaEnum.ANNOUNCEMENT_TYPE_KEY.getTypes(),id);
      return   R.ok().put("status",OaAnnouncement.dao.deleteById(id)?1:0);
    }
    /**
     * 根据ID查询详情
     */
    public R queryById(Integer id){
        return R.ok().put("data", Db.findFirst(Db.getSql("oa.announcement.queryById"),id));
    }
    /**
     * 倒叙查询公告集合
     */
    public R queryList(BasePageRequest<OaAnnouncement> basePageRequest,Integer type){
        AdminRole adminRole = AdminRole.dao.findFirst(Db.getSql("admin.role.queryAnnouncementByUserId"),BaseUtil.getUser().getUserId().intValue());
        int status = 1;
        if (adminRole == null && !BaseUtil.getUser().getUserId().equals(BaseConstant.SUPER_ADMIN_USER_ID)){
            status = 0;
        }
        String tatal = "select count(*)";
        String queryList = "select an.* , us.user_id,us.username,us.img,us.realname,us.parent_id ,LEFT(an.start_time,10) as startTime,LEFT(an.end_time,10) as endTime ";

        StringBuffer sql = new StringBuffer(" from 72crm_oa_announcement as an " +
                " LEFT JOIN 72crm_admin_user as us on us.user_id = create_user_id");
        if (type != null && type == 2){
            sql.append(" WHERE (unix_timestamp(now()) - unix_timestamp(an.end_time)) > 0 ");
        }else {
            sql.append(" WHERE (unix_timestamp(now()) - unix_timestamp(an.end_time)) <= 0 ");
        }

        String cc = " order by an.announcement_id desc";
        Page<Record> page = Db.paginateByFullSql(basePageRequest.getPage(),basePageRequest.getLimit(),tatal + sql,queryList + sql + cc );
        for (Record r: page.getList()) {
            r.set("is_update",status);
            r.set("is_delete",status);
            if (r.getStr("read_user_ids")!=null){
                r.set("is_read",r.getStr("read_user_ids").contains("," + BaseUtil.getUserId().intValue() + ",") ? 1 : 0);
            }else {
                r.set("is_read",0);
            }
        }
        Map<String,Object> map = new HashMap<>();
        map.put("totalRow",page.getTotalRow());
        map.put("totalPage",page.getTotalPage());
        map.put("pageSize",page.getPageSize());
        map.put("pageNumber",page.getPageNumber());
        map.put("list",page.getList());
        map.put("is_save",status);
        return R.ok().put("data",map);
    }

    public void readAnnouncement(Integer announcementId) {
        OaAnnouncement oaAnnouncement = OaAnnouncement.dao.findById(announcementId);
        HashSet<String> hashSet = new HashSet<>(StrUtil.splitTrim(oaAnnouncement.getReadUserIds(), ","));
        hashSet.add(BaseUtil.getUser().getUserId().toString());
        oaAnnouncement.setReadUserIds("," + String.join(",", hashSet) + ",");
        oaAnnouncement.update();
    }
}
