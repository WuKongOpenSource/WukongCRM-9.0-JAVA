package com.kakarote.crm9.erp.oa.controller;

import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.erp.oa.entity.OaAnnouncement;
import com.kakarote.crm9.erp.oa.service.OaAnnouncementService;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.utils.BaseUtil;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.kakarote.crm9.utils.R;

/**
 * 公告
 * @author zxy
 */
public class OaAnnouncementController extends Controller {
    @Inject
    private OaAnnouncementService announcementService;
    /**
     * 添加或修改
     */
    @Permissions({"oa:announcement:save","oa:announcement:update"})
    public void saveAndUpdate(@Para("") OaAnnouncement oaAnnouncement){
        oaAnnouncement.setCreateUserId(BaseUtil.getUser().getUserId());
        renderJson(announcementService.saveAndUpdate(oaAnnouncement));
    }
    /**
     * 删除
     */
    @Permissions("oa:announcement:delete")
    public void delete(@Para("id")Integer id){
        renderJson(announcementService.delete(id));
    }
    /**
     * 根据ID查询详情
     */
    public void queryById(@Para("id")Integer id){
        renderJson(announcementService.queryById(id));
    }

    /**
     * 倒叙查询公告集合
     * @author zhangzhiwei
     */
    public void queryList(BasePageRequest<OaAnnouncement> basePageRequest,@Para("type")Integer type){
        renderJson(announcementService.queryList(basePageRequest,type));
    }

    /**
     * 公告设为已读
     * @param announcementId 公告ID
     * @author wyq
     */
    public void readAnnouncement(@Para("announcementId")Integer announcementId){
        announcementService.readAnnouncement(announcementId);
        renderJson(R.ok());
    }
}
