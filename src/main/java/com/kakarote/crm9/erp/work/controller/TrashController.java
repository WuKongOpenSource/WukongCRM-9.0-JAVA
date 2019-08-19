package com.kakarote.crm9.erp.work.controller;

import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.kakarote.crm9.erp.work.service.TrashService;

/**
 * 回收站
 */
public class TrashController extends Controller{
    @Inject
    private TrashService trashService;

    public void queryList(){
        renderJson(trashService.queryList());
    }

    /**
     * 彻底删除任务
     * @author wyq
     * @param taskId
     */
    public void deleteTask(@Para("taskId")Integer taskId){
        renderJson(trashService.deleteTask(taskId));
    }

    /**
     * 还原任务
     * @author wyq
     * @param taskId
     */
    public void restore(@Para("taskId")Integer taskId){
        renderJson(trashService.restore(taskId));
    }
}
