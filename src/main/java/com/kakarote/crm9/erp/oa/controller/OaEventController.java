package com.kakarote.crm9.erp.oa.controller;

import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.oa.common.OaEnum;
import com.kakarote.crm9.erp.oa.entity.OaEvent;
import com.kakarote.crm9.erp.oa.entity.OaEventRelation;
import com.kakarote.crm9.erp.oa.service.OaEventService;
import com.kakarote.crm9.utils.AuthUtil;
import com.kakarote.crm9.utils.R;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;

public class OaEventController extends Controller {
    @Inject
    private OaEventService oaEventService;

    /**
     * @author wyq
     * 查询日程列表
     */
    public void queryList(@Para("")OaEvent oaEvent){
        renderJson(R.ok().put("data",oaEventService.queryList(oaEvent)));
    }

    /**
     * @author wyq
     * 查询日程列表
     */
    public void queryById(@Para("eventId")Integer eventId){
        renderJson(R.ok().put("data",oaEventService.queryById(eventId)));
    }

    /**
     * @author wyq
     * 新增日程
     */
    public void add(@Para("")OaEvent oaEvent){
        renderJson(oaEventService.add(oaEvent));
    }

    /**
     * @author wyq
     * 更新日程
     */
    public void update(@Para("")OaEvent oaEvent){
        boolean oaAuth = AuthUtil.isOaAuth(OaEnum.EVENT_TYPE_KEY.getTypes(), oaEvent.getEventId());
        if(oaAuth){
            renderJson(R.noAuth());
            return;
        }
        renderJson(oaEventService.update(oaEvent));
    }

    /**
     * @author wyq
     * 删除日程
     */
    public void delete(@Para("eventId")Integer eventId){
        boolean oaAuth = AuthUtil.isOaAuth(OaEnum.EVENT_TYPE_KEY.getTypes(), eventId);
        if(oaAuth){
            renderJson(R.noAuth());
            return;
        }
        renderJson(oaEventService.delete(eventId));
    }

    /**
     * @author wyq
     * crm查询日程
     */
    public void queryEventRelation(BasePageRequest<OaEventRelation> basePageRequest){
        renderJson(oaEventService.queryEventRelation(basePageRequest));
    }
}
