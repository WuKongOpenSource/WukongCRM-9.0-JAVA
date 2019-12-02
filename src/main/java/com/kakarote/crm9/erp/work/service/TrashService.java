package com.kakarote.crm9.erp.work.service;

import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kakarote.crm9.erp.admin.service.AdminFileService;
import com.kakarote.crm9.erp.work.entity.Task;
import com.kakarote.crm9.utils.AuthUtil;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;

import java.util.ArrayList;
import java.util.List;

public class TrashService{
    @Inject
    private WorkbenchService workbenchService;

    @Inject
    private AdminFileService adminFileService;

    /**
     * 回收站列表
     */
    public R queryList(){
        List<Record> recordList;
        if(AuthUtil.isWorkAdmin()){
            recordList = Db.find(Db.getSqlPara("work.trash.queryList"));
        }else {
            recordList = Db.find(Db.getSqlPara("work.trash.queryList", Kv.by("userId", BaseUtil.getUserId())));
        }
        workbenchService.taskListTransfer(recordList);
        return R.ok().put("data", recordList);
    }

    @Before(Tx.class)
    public R deleteTask(Integer taskId){
        Task task = new Task().dao().findById(taskId);
        if(task==null){
            return R.error("任务不存在！");
        }
//        int userId = BaseUtil.getUser().getUserId();
//        if(task.getCreateUserId() != userId && (task.getMainUserId()!=null && task.getMainUserId() != userId)){
//            return R.error("您无权删除任务！");
//        }
        if(task.getIshidden() != 1){
            return R.error("任务不在回收站！");
        }
        adminFileService.removeByBatchId(Db.queryStr("select batch_id from `72crm_task` where task_id = ?",taskId));
        return task.delete()?R.ok():R.error();
    }

    public R restore(Integer taskId){
        Task task = new Task().dao().findById(taskId);
        if(task==null){
            return R.error("任务不存在！");
        }
        if(task.getIshidden() != 1){
            return R.error("任务不在回收站！");
        }
        Integer count = Db.queryInt("select count(*) from `72crm_work_task_class` where class_id = ?", task.getClassId());
        int update;
        if(count>0){
            update = Db.update("update 72crm_task set ishidden = 0,hidden_time = null where task_id = ?", taskId);
        }else {
            update = Db.update("update 72crm_task set ishidden = 0,class_id = null,hidden_time = null where task_id = ?", taskId);
        }
        return update>0?R.ok():R.error();
    }
}
