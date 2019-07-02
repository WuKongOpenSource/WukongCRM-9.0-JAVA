package com.kakarote.crm9.erp.oa.controller;

import com.alibaba.fastjson.JSON;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.oa.common.OaEnum;
import com.kakarote.crm9.erp.oa.entity.OaLog;
import com.kakarote.crm9.erp.oa.entity.OaLogRelation;
import com.kakarote.crm9.erp.oa.service.OaLogService;
import com.kakarote.crm9.erp.work.service.TaskService;
import com.kakarote.crm9.utils.AuthUtil;
import com.kakarote.crm9.utils.R;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

/**
 * OA日志模块
 */
public class OaLogController extends Controller {

    @Inject
    private OaLogService oaLogService;
    @Inject
    private TaskService taskService;

    /**
     * 分页条件查询日志
     * @author zhangzhiwei
     */
    public void queryList(BasePageRequest<OaLog> basePageRequest){
        Page<Record> recordList=oaLogService.queryList(basePageRequest);
        renderJson(R.ok().put("data",recordList));
    }

    /**
     * 根据日志id获取日志
     * @param logId 日志ID
     * @author zhangzhiwei
     */
    public void queryById(@Para("logId") Integer logId){
        Record record=oaLogService.queryById(logId);
        renderJson(R.ok().put("data",record));
    }
    /**
     * 添加日志
     * @author zhangzhiwei
     */
    public void addOrUpdate(){
        renderJson(oaLogService.saveAndUpdate(JSON.parseObject(getRawData())));
    }

    /**
     * 根据日志id删除日志
     * @param logId 日志ID
     * @author zhangzhiwei
     */
    public void deleteById(@Para("logId") Integer logId){
        boolean oaAuth = AuthUtil.isOaAuth(OaEnum.LOG_TYPE_KEY.getTypes(), logId);
        if(oaAuth){renderJson(R.noAuth());return;}
        renderJson(oaLogService.deleteById(logId) ? R.ok() : R.error("删除失败"));
    }

    /**
     * 日志设为已读
     * @param logId 日志ID
     * @author zhangzhiwei
     */
    public void readLog(@Para("logId")Integer logId){
        oaLogService.readLog(logId);
        renderJson(R.ok());
    }

    /**
     * 查询crm关联日志
     */
    public void queryLogRelation(BasePageRequest<OaLogRelation> basePageRequest){
        renderJson(oaLogService.queryLogRelation(basePageRequest));
    }
}
