package com.kakarote.crm9.erp.work.controller;

import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.work.entity.Work;
import com.kakarote.crm9.erp.work.service.WorkService;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;

/**
 * @author hmb
 */
public class WorkController extends Controller {

    @Inject
    private WorkService workService;

    /**
     * @author hmb
     * 设置项目
     * @param work 项目对象
     */
    public void setWork(@Para("") Work work){
        String customerIds = getPara("customerIds");
        String contactsIds = getPara("contactsIds");
        String businessIds = getPara("businessIds");
        String contractIds = getPara("contractIds");
        renderJson(workService.setWork(work,customerIds,contactsIds,businessIds,contractIds));
    }

    /**
     * @author hmb
     * 删除项目
     */
    public void deleteWork(){
        String workId = getPara("workId");
        renderJson(workService.deleteWork(workId));
    }
    /**
     * @author hmb
     * 查询项目名列表
     */
    public void queryWorkNameList(){
        renderJson(workService.queryWorkNameList());
    }
    /**
     * @author hmb
     * 根据项目id查询任务板
     */
    public void queryTaskByWorkId(){
        String workId = getPara("workId");
        String status = getPara("status");
        renderJson(workService.queryTaskByWorkId(workId,status));
    }

    /**
     * @author hmb
     * 根据项目id查询项目附件
     */
    public void queryTaskFileByWorkId(){
        String workId = getPara("workId");
        renderJson(workService.queryTaskFileByWorkId(workId));
    }

    /**
     * @author hmb
     * 查询归档项目列表
     */
    public void queryArchiveWorkList(BasePageRequest pageRequest){
        renderJson(workService.queryArchiveWorkList(pageRequest));
    }

    /**
     * 项目统计
     */
    public void workStatistics(){
        String workId = getPara("workId");
        renderJson(workService.workStatistics(workId));
    }



}
