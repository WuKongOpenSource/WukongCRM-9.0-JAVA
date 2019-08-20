package com.kakarote.crm9.erp.work.controller;

import cn.hutool.core.util.StrUtil;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.erp.oa.common.OaEnum;
import com.kakarote.crm9.erp.work.entity.Task;
import com.kakarote.crm9.erp.work.entity.TaskRelation;
import com.kakarote.crm9.erp.work.entity.Work;
import com.kakarote.crm9.erp.work.entity.WorkTaskClass;
import com.kakarote.crm9.erp.work.service.TaskService;
import com.kakarote.crm9.utils.AuthUtil;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.kakarote.crm9.utils.TagUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hmb
 */
public class TaskController extends Controller{

    @Inject
    private TaskService taskService;
    @Inject
    private AdminUserService userService;

    /**
     * @param taskClass 任务类别对象
     * @author hmb
     * 设置任务类别
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
        taskService.changeOrderTaskClass(originalClassId, targetClassId);
        renderJson(R.ok());
    }

    /**
     * @param task 任务对象
     * @author hmb
     * 设置oa任务
     */
    public void setTask(@Para("") Task task){
        if(task.getPid() != null && task.getPid() != 0){
            boolean oaAuth = AuthUtil.isOaAuth(OaEnum.TASK_TYPE_KEY.getTypes(), task.getPid());
            if(oaAuth){
                renderJson(R.noAuth());
                return;
            }
        }
        if(StrUtil.isNotEmpty(task.getOwnerUserId())){
            task.setOwnerUserId(TagUtil.fromString(task.getOwnerUserId()));
        }
        if(task.getStartTime() != null && task.getStopTime() != null){
            if(task.getStartTime().getTime() > task.getStopTime().getTime()){
                renderJson(R.error("开始时间不能大于结束时间"));
                return;
            }
        }
        String customerIds = getPara("customerIds");
        String contactsIds = getPara("contactsIds");
        String businessIds = getPara("businessIds");
        String contractIds = getPara("contractIds");
        TaskRelation taskRelation = new TaskRelation();
        if(customerIds != null || contactsIds != null || businessIds != null || contractIds != null){

            taskRelation.setBusinessIds(TagUtil.fromString(businessIds));
            taskRelation.setContactsIds(TagUtil.fromString(contactsIds));
            taskRelation.setContractIds(TagUtil.fromString(contractIds));
            taskRelation.setCustomerIds(TagUtil.fromString(customerIds));
        }
        renderJson(taskService.setTask(task, taskRelation));
    }

    public void setWorkTask(@Para("") Task task){
        if(task.getWorkId() != null){
            Integer isOpen = new Work().findById(task.getWorkId()).getIsOpen();
            if(isOpen == 0 && ! AuthUtil.isWorkAuth(task.getWorkId().toString(), "task:save")){
                renderJson(R.noAuth());
                return;
            }
        }
        if(StrUtil.isNotEmpty(task.getOwnerUserId())){
            task.setOwnerUserId(TagUtil.fromString(task.getOwnerUserId()));
        }
        if(task.getStartTime() != null && task.getStopTime() != null){
            if(task.getStartTime().getTime() > task.getStopTime().getTime()){
                renderJson(R.error("开始时间不能大于结束时间"));
                return;
            }
        }
        String customerIds = getPara("customerIds");
        String contactsIds = getPara("contactsIds");
        String businessIds = getPara("businessIds");
        String contractIds = getPara("contractIds");
        TaskRelation taskRelation = new TaskRelation();
        if(customerIds != null || contactsIds != null || businessIds != null || contractIds != null){

            taskRelation.setBusinessIds(TagUtil.fromString(businessIds));
            taskRelation.setContactsIds(TagUtil.fromString(contactsIds));
            taskRelation.setContractIds(TagUtil.fromString(contractIds));
            taskRelation.setCustomerIds(TagUtil.fromString(customerIds));
        }
        renderJson(taskService.setTask(task, taskRelation));
    }


    /**
     * @author hmb
     * 查询任务列表
     */
    public void getTaskList(BasePageRequest basePageRequest){
        String labelId = getPara("labelId");
        String ishidden = getPara("ishidden");
        renderJson(taskService.getTaskList(basePageRequest, labelId, ishidden));
    }


    /**
     * @author hmb
     * 查询oa任务信息
     */
    public void queryTaskInfo(){
        String taskId = getPara("taskId");
        boolean oaAuth = AuthUtil.isOaAuth(OaEnum.TASK_TYPE_KEY.getTypes(), Integer.valueOf(taskId));
        if(oaAuth){
            renderJson(R.noAuth());
            return;
        }
        renderJson(taskService.queryTaskInfo(taskId));
    }

    /**
     * @author hmb
     * 查询项目任务详情
     */
    public void queryTaskById(){
        String taskId = getPara("taskId");
        renderJson(taskService.queryTaskInfo(taskId));
    }

    /**
     * 查询任务列表 oa
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
        if(mold == null){
            userIds.add(BaseUtil.getUser().getUserId().intValue());
        }else if(mold == 1 && userId == null){
            userIds = userService.queryUserIdsByParentId(BaseUtil.getUser().getUserId().intValue());
        }else{
            List<Long> list = userService.queryChileUserIds(BaseUtil.getUser().getUserId(), BaseConstant.AUTH_DATA_RECURSION_NUM);
            for(Long id : list){
                if(id.intValue() == userId){
                    userIds.add(userId);
                }
            }
        }
        renderJson(taskService.getTaskList(type, status, priority, date, userIds, basePageRequest, name));
    }

    /**
     * @author zxy
     * 根据任务id查询活动日志 oa
     * taskId 任务id
     */
    public void queryWorkTaskLog(){
        Integer taskId = getParaToInt("taskId");
        boolean oaAuth = AuthUtil.isOaAuth(OaEnum.TASK_TYPE_KEY.getTypes(), taskId);
        if(oaAuth){
            renderJson(R.noAuth());
            return;
        }
        renderJson(taskService.queryWorkTaskLog(taskId));
    }

    /**
     * 根据任务id查询活动日志 work
     * taskId 任务id
     */
    public void queryTaskLog(){
        Integer taskId = getParaToInt("taskId");
        renderJson(taskService.queryWorkTaskLog(taskId));
    }

    /**
     * @author zxy
     * 添加任务与业务关联
     */
    public void svaeTaskRelation(@Para("") TaskRelation taskRelation){
        renderJson(taskService.svaeTaskRelation(taskRelation, BaseUtil.getUser().getUserId().intValue()));
    }

    /**
     * @author hmb
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

    /**
     * @author hmb
     * 根据任务id归档任务
     *
     * @param taskId
     */
    public void archiveByTaskId(@Para("taskId") Integer taskId){
        renderJson(taskService.archiveByTaskId(taskId));
    }
}
