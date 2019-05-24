package com.kakarote.crm9.erp.work.controller;

import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.erp.work.entity.Task;
import com.kakarote.crm9.erp.work.entity.TaskRelation;
import com.kakarote.crm9.erp.work.entity.WorkTaskClass;
import com.kakarote.crm9.erp.work.entity.WorkTaskLable;
import com.kakarote.crm9.erp.work.service.TaskService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hmb
 */
public class TaskController extends Controller {

    @Inject
    private TaskService taskService;
    @Inject
    private AdminUserService userService;

    /**
     * @author hmb
     * 设置任务类别
     *
     * @param taskClass 任务类别对象
     */
    public void setTaskClass(@Para("") WorkTaskClass taskClass){
        renderJson(taskService.setTaskClass(taskClass));
    }

    /**
     * @author hmb
     * 交换任务列表排序
     */
    public void changeOrderTaskClass(){
        String originalClassId = getPara("originalClassId");
        String targetClassId = getPara("targetClassId");
        taskService.changeOrderTaskClass(originalClassId,targetClassId);
        renderJson(R.ok());
    }

    /**
     * @author hmb
     * 设置任务
     * @param task 任务对象
     */
    public void setTask(@Para("") Task task){
        String customerIds = getPara("customerIds");
        String contactsIds = getPara("contactsIds");
        String businessIds = getPara("businessIds");
        String contractIds = getPara("contractIds");
        TaskRelation taskRelation = new TaskRelation();
        if (customerIds != null || contactsIds != null || businessIds != null || contractIds != null ){

            taskRelation.setBusinessIds(businessIds);
            taskRelation.setContactsIds(contactsIds);
            taskRelation.setContractIds(contractIds);
            taskRelation.setCustomerIds(customerIds);
        }
        renderJson(taskService.setTask(task,taskRelation));
    }

    /**
     * @author hmb
     * 我的任务
     */
    public void myTask(){
        renderJson(taskService.myTask(BaseUtil.getUser().getUserId().intValue()));
    }

    /**
     * @author hmb
     * 查询任务列表
     */
    public void getTaskList(BasePageRequest basePageRequest){
        String labelId = getPara("labelId");
        String ishidden = getPara("ishidden");
        renderJson(taskService.getTaskList(basePageRequest,labelId,ishidden));
    }

    /**
     * @author hmb
     * 任务日历
     */
    public void dateList(){
        String startTime = getPara("startTime");
        String endTime = getPara("endTime");
        renderJson(taskService.dateList(startTime,endTime));
    }

    /**
     * @author hmb
     * 设置任务标签
     * @param taskLable 任务标签对象
     */
    public void setLable(@Para("") WorkTaskLable taskLable){
        renderJson(taskService.setLable(taskLable));
    }
    /**
     * @author hmb
     * 删除任务标签
     */
    public void deleteLable(){
        String lableId = getPara("lableId");
        renderJson(taskService.deleteLable(lableId));
    }
    /**
     * @author hmb
     * 任务标签列表
     */
    public void getLableList(){
        renderJson(taskService.getLableList());
    }

    /**
     * @author hmb
     * 查询任务信息
     */
    public void queryTaskInfo(){
        String taskId = getPara("taskId");
        renderJson(taskService.queryTaskInfo(taskId));
    }

    /**
     * 查询任务列表
     */
    public void queryTaskList(BasePageRequest<Task> basePageRequest){
        Integer type = getParaToInt("type");
        Integer status = getParaToInt("status");
        Integer priority = getParaToInt("priority");
        Integer date = getParaToInt("date");
        Integer mold = getParaToInt("mold");
        Integer userId = getParaToInt("userId");
        String name = get("search");
        List<Integer> userIds = new ArrayList<>();
        if (mold == null){
            userIds.add(BaseUtil.getUser().getUserId().intValue());
            //userIds.add(1);
        }else if (mold == 1 && userId == null){
            userIds = userService.queryUserIdsByParentId(BaseUtil.getUser().getUserId().intValue());
        }else {
            userIds.add(userId);
        }
        renderJson(taskService.getTaskList(type,status,priority,date,userIds,basePageRequest,name));
    }
    /**
     * @author zxy
     * 根据任务id查询活动日志
     * taskId 任务id
     */
    public void queryWorkTaskLog(){
        Integer taskId = getParaToInt("taskId");
        renderJson(taskService.queryWorkTaskLog(taskId));
    }
    /**
     * @author zxy
     * 添加任务与业务关联
     */
    public void svaeTaskRelation(@Para("") TaskRelation taskRelation){
        renderJson(taskService.svaeTaskRelation(taskRelation,BaseUtil.getUser().getUserId().intValue()));
    }

    /**
     * 删除任务
     * taskId 任务id
     */
    public void deleteTask(){
        Integer taskId = getParaToInt("taskId");
        renderJson(taskService.deleteTask(taskId));
    }
    /**
     * @author wyq
     * crm查询关联任务
     */
    public void queryTaskRelation(@Para("") BasePageRequest<TaskRelation> basePageRequest){
        renderJson(taskService.queryTaskRelation(basePageRequest));
    }
}
