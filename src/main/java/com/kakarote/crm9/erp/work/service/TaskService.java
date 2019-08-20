package com.kakarote.crm9.erp.work.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminFileService;
import com.kakarote.crm9.erp.oa.common.OaEnum;
import com.kakarote.crm9.erp.oa.service.OaActionRecordService;
import com.kakarote.crm9.erp.work.entity.*;
import com.kakarote.crm9.utils.AuthUtil;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.kakarote.crm9.utils.TagUtil;

import java.util.*;

public class TaskService{
    //添加日志
    @Inject
    private OaActionRecordService oaActionRecordService;

    @Inject
    private AdminFileService adminFileService;

    public R setTaskClass(WorkTaskClass taskClass){
        boolean bol;
        if(taskClass.getClassId() == null){
            Work work = new Work().findById(taskClass.getWorkId());
            Integer isOpen = work.getIsOpen();
            if(isOpen == 0 && ! AuthUtil.isWorkAuth(taskClass.getWorkId().toString(), "taskClass:save")){
                return R.noAuth();
            }
            Integer orderNum = Db.queryInt("select max(order_num) from `72crm_work_task_class` where work_id = ?", taskClass.getWorkId());
            taskClass.setOrderNum(orderNum+1);
            taskClass.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
            taskClass.setCreateTime(new Date());
            bol = taskClass.save();
        }else{
            Integer workId = Db.queryInt("select work_id from `72crm_work_task_class` where class_id = ?", taskClass.getClassId());
            if(! AuthUtil.isWorkAuth(workId.toString(), "taskClass:update")){
                return R.noAuth();
            }
            bol = taskClass.update();
        }
        return R.isSuccess(bol);
    }


    public void changeOrderTaskClass(String originalClassId, String targetClassId){
        WorkTaskClass originalClass = WorkTaskClass.dao.findById(originalClassId);
        WorkTaskClass targetClass = WorkTaskClass.dao.findById(targetClassId);
        Integer originalClassOrderId = originalClass.getOrderNum();
        Integer targetClassOrderId = targetClass.getOrderNum();
        Db.update("update 72crm_work_task_class setUser order_id = ? where class_id = ?", originalClassOrderId, targetClassId);
        Db.update("update 72crm_work_task_class setUser order_id = ? where class_id = ?", targetClassOrderId, originalClassId);
    }

    @Before(Tx.class)
    public R setTask(Task task, TaskRelation taskRelation){
        AdminUser user = BaseUtil.getUser();
        boolean bol;
        if(task.getLabelId() != null){
            task.setLabelId(TagUtil.fromString(task.getLabelId()));
        }
        if(task.getTaskId() == null){
            if(task.getMainUserId() == null){
                task.setMainUserId(user.getUserId().intValue());
            }
            if(task.getOwnerUserId() != null){
                Set<Integer> ownerUserId = TagUtil.toSet(task.getOwnerUserId());
                ownerUserId.add(user.getUserId().intValue());
                task.setOwnerUserId(TagUtil.fromSet(ownerUserId));
            }else{
                task.setOwnerUserId("," + user.getUserId() + ",");
            }
            task.setCreateTime(new Date());
            task.setUpdateTime(new Date());
            task.setCreateUserId(user.getUserId().intValue());
            task.setBatchId(IdUtil.simpleUUID());
            bol = task.save();
            WorkTaskLog workTaskLog = new WorkTaskLog();
            workTaskLog.setUserId(user.getUserId().intValue());
            workTaskLog.setTaskId(task.getTaskId());
            workTaskLog.setContent("添加了新任务 " + task.getName());
            saveWorkTaskLog(workTaskLog);

        }else{
            task.setUpdateTime(new Date());
            bol = getWorkTaskLog(task, user.getUserId().intValue());
        }
        if(taskRelation.getBusinessIds() != null || taskRelation.getContactsIds() != null || taskRelation.getContractIds() != null || taskRelation.getCustomerIds() != null){
            Db.deleteById("72crm_task_relation", "task_id", task.getTaskId());
            taskRelation.setCreateTime(DateUtil.date());
            taskRelation.setTaskId(task.getTaskId());
            taskRelation.save();
        }
        task.getMainUserId();
        oaActionRecordService.addRecord(task.getTaskId(), OaEnum.TASK_TYPE_KEY.getTypes(), task.getUpdateTime() == null ? 1 : 2, oaActionRecordService.getJoinIds(user.getUserId().intValue(), getJoinUserIds(task)), oaActionRecordService.getJoinIds(user.getDeptId(), ""));
        return bol ? R.ok().put("data", Kv.by("task_id", task.getTaskId())) : R.error();
    }

    private String getJoinUserIds(Task task){
        StringBuilder joinUserIds = new StringBuilder(",");
        if(task.getMainUserId() != null){
            joinUserIds.append(task.getMainUserId()).append(",");
        }
        if(StrUtil.isNotEmpty(task.getOwnerUserId())){
            joinUserIds.append(task.getOwnerUserId());
        }
        return joinUserIds.toString();
    }


    public R queryTaskInfo(String taskId){
        Record mainTask = transfer(taskId);
        adminFileService.queryByBatchId(mainTask.get("batch_id"), mainTask);
        List<Record> recordList = Db.find("select task_id from 72crm_task where pid = ?", taskId);
        List<Record> childTaskList = new ArrayList<>();
        if(recordList != null && recordList.size() > 0){
            recordList.forEach(childTaskRecord -> {
                String childTaskId = childTaskRecord.getStr("task_id");
                Record childTask = transfer(childTaskId);
                adminFileService.queryByBatchId(childTask.getStr("batch_id"), childTask);
                childTaskList.add(childTask);
            });
        }
        mainTask.set("childTask", childTaskList);
        return R.ok().put("data", mainTask);
    }

    private Record transfer(String taskId){
        Record task = Db.findFirst("select a.*,b.name as workName from 72crm_task a left join `72crm_work` b on a.work_id = b.work_id where task_id = ?", taskId);
        task.set("stop_time", DateUtil.formatDate(task.getDate("stop_time")));
        task.set("mainUser", Db.findFirst("select user_id,realname,img from 72crm_admin_user where user_id = ?", task.getInt("main_user_id")));
        task.set("createUser", Db.findFirst("select user_id,realname,img from 72crm_admin_user where user_id = ?", task.getInt("create_user_id")));
        ArrayList<Record> labelList = new ArrayList<>();
        ArrayList<Record> ownerUserList = new ArrayList<>();
        if(StrUtil.isNotBlank(task.getStr("label_id"))){
            String[] labelIds = task.getStr("label_id").split(",");
            for(String labelId : labelIds){
                if(StrUtil.isNotBlank(labelId)){
                    Record label = Db.findFirst("select label_id,name as labelName,color from 72crm_work_task_label where label_id = ?", labelId);
                    labelList.add(label);
                }
            }
        }
        if(StrUtil.isNotBlank(task.getStr("owner_user_id"))){
            String[] ownerUserIds = task.getStr("owner_user_id").split(",");
            for(String ownerUserId : ownerUserIds){
                if(StrUtil.isNotBlank(ownerUserId)){
                    Record ownerUser = Db.findFirst("select user_id,realname,img from 72crm_admin_user where user_id = ?", ownerUserId);
                    ownerUserList.add(ownerUser);
                }
            }
        }
        Record relation = Db.findFirst("select * FROM 72crm_task_relation where task_id = ?", taskId);
        List<Record> customerList = new ArrayList<>();
        List<Record> contactsList = new ArrayList<>();
        List<Record> businessList = new ArrayList<>();
        List<Record> contractList = new ArrayList<>();
        if(relation != null){
            if(StrUtil.isNotBlank(relation.getStr("customer_ids"))){
                String[] customerIds = relation.getStr("customer_ids").split(",");
                for(String customerId : customerIds){
                    if(StrUtil.isNotBlank(customerId)){
                        Record customer = Db.findFirst("select customer_id,customer_name  from 72crm_crm_customer where customer_id = ?", customerId);
                        if(customer != null){
                            customerList.add(customer);
                        }
                    }
                }
            }

            if(StrUtil.isNotBlank(relation.getStr("contacts_ids"))){
                String[] contactsIds = relation.getStr("contacts_ids").split(",");

                for(String contactsId : contactsIds){
                    if(StrUtil.isNotBlank(contactsId)){
                        Record contacts = Db.findFirst("select contacts_id,name from 72crm_crm_contacts  where contacts_id = ?", contactsId);
                        if(contacts != null){
                            contactsList.add(contacts);
                        }
                    }
                }
            }
            if(StrUtil.isNotBlank(relation.getStr("business_ids"))){
                String[] businessIds = relation.getStr("business_ids").split(",");

                for(String businessId : businessIds){
                    if(StrUtil.isNotBlank(businessId)){
                        Record business = Db.findFirst("select business_id,business_name  from 72crm_crm_business  where business_id = ?", businessId);
                        if(business != null){
                            businessList.add(business);
                        }
                    }
                }
            }
            if(StrUtil.isNotBlank(relation.getStr("contract_ids"))){
                String[] contractIds = relation.getStr("contract_ids").split(",");
                for(String contractId : contractIds){
                    if(StrUtil.isNotBlank(contractId)){
                        Record contract = Db.findFirst("select contract_id,name from 72crm_crm_contract  where contract_id = ?", contractId);
                        if(contract != null){
                            contractList.add(contract);
                        }
                    }
                }
                task.set("contractList", contractList);
            }
        }
        task.set("customerList", customerList);
        task.set("contactsList", contactsList);
        task.set("businessList", businessList);
        task.set("contractList", contractList);

        return task.set("labelList", labelList).set("ownerUserList", ownerUserList);
    }


    /**
     * 查询任务列表
     */
    public R getTaskList(Integer type, Integer status, Integer priority, Integer date, List<Integer> userIds, BasePageRequest<Task> basePageRequest, String name){
        Page<Record> page = new Page<>();
        if(userIds.size() == 0){
            page.setList(new ArrayList<>());
            return R.ok().put("data", page);
        }
        if(basePageRequest.getPageType() == 0){
            List<Record> recordList = Db.find(Db.getSqlPara("work.task.getTaskList",
                    Kv.by("type",type).set("userIds",userIds).set("status",status).
                            set("priority",priority).set("date",date).set("taskName",name)));
            return R.ok().put("data", queryUser(recordList));
        }else {
            page = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(),
                    Db.getSqlPara("work.task.getTaskList",
                            Kv.by("type",type).set("userIds",userIds).set("status",status).
                                    set("priority",priority).set("date",date).set("taskName",name)));
            page.setList(queryUser(page.getList()));
            return R.ok().put("data", page);
        }

    }

    private List<Record> queryUser(List<Record> tasks){
        ArrayList<Record> labelList;
        ArrayList<Record> ownerUserList;
        for(Record task : tasks){
            labelList = new ArrayList<>();
            ownerUserList = new ArrayList<>();
            if(StrUtil.isNotBlank(task.getStr("label_id"))){
                String[] labelIds = task.getStr("label_id").split(",");
                for(String labelId : labelIds){
                    if(StrUtil.isNotBlank(labelId)){
                        Record label = Db.findFirst("select label_id,name as labelName , color from 72crm_work_task_label where label_id = ?", labelId);
                        labelList.add(label);
                    }
                }
            }
            if(StrUtil.isNotBlank(task.getStr("owner_user_id"))){
                String[] ownerUserIds = task.getStr("owner_user_id").split(",");
                for(String ownerUserId : ownerUserIds){
                    if(StrUtil.isNotBlank(ownerUserId)){
                        Record ownerUser = Db.findFirst("select user_id,realname,img from 72crm_admin_user where user_id = ?", ownerUserId);
                        ownerUserList.add(ownerUser);
                    }
                }
            }
            TaskRelation taskRelation = TaskRelation.dao.findFirst(" select * from 72crm_task_relation where task_id = ?", task.getInt("task_id"));
            Integer start = 0;
            if(taskRelation != null){
                start = queryCount(start, taskRelation.getBusinessIds());
                start = queryCount(start, taskRelation.getContactsIds());
                start = queryCount(start, taskRelation.getContractIds());
                start = queryCount(start, taskRelation.getCustomerIds());
            }
            task.set("relationCount", start);
            if(task.getDate("stop_time") != null){
                Calendar date = Calendar.getInstance();
                date.setTime(DateUtil.date());
                //设置开始时间
                Calendar begin = Calendar.getInstance();
                begin.setTime(task.getDate("stop_time"));
                if(date.after(begin) && task.getInt("status") != 5 && task.getInt("status") != 2){
                    task.set("is_end", 1);
                }else{
                    task.set("is_end", 0);
                }
            }else{
                task.set("is_end", 0);
            }
            task.set("labelList", labelList).set("ownerUserList", ownerUserList);
        }
        return tasks;
    }

    private Integer queryCount(Integer start, String str){
        // start 开始个数
        if(str != null){
            String[] ownerUserIds = str.split(",");
            for(String ownerUserId : ownerUserIds){
                if(StrUtil.isNotBlank(ownerUserId)){
                    ++ start;
                }
            }
        }

        return start;
    }

    public R queryWorkTaskLog(Integer taskId){
        List<Record> recordList = Db.find(Db.getSqlPara("work.task.myWorkLog", Kv.by("taskId", taskId)));
        return R.ok().put("data", recordList);
    }

    private void saveWorkTaskLog(WorkTaskLog workTaskLog){
        workTaskLog.setCreateTime(DateUtil.date());
        workTaskLog.setLogId(null);
        workTaskLog.save();
    }

    @Before(Tx.class)
    private boolean getWorkTaskLog(Task task, Integer userId){
        WorkTaskLog workTaskLog = new WorkTaskLog();
        workTaskLog.setUserId(userId);
        workTaskLog.setTaskId(task.getTaskId());

        Task auldTask = Task.dao.findById(task.getTaskId());
        task.update();

        //判断描述是否修改
       /* if (task.getDescription() != null){
            if (auldTask.getDescription() == null || auldTask.getDescription().equals("")){
                workTaskLog.setContent("增加加了描述 " + task.getDescription());
            } else if (!auldTask.getDescription().equals(task.getDescription())){
               workTaskLog.setContent("修改了描述 " + task.getDescription());
            }
           saveWorkTaskLog(workTaskLog);
        }*/
        Set<Map.Entry<String,Object>> newEntries = task._getAttrsEntrySet();
        Set<Map.Entry<String,Object>> oldEntries = auldTask._getAttrsEntrySet();
        newEntries.forEach(x -> {
            oldEntries.forEach(y -> {
                Object oldValue = y.getValue();
                Object newValue = x.getValue();
                if(oldValue instanceof Date){
                    oldValue = DateUtil.formatDateTime((Date) oldValue);
                }
                if(newValue instanceof Date){
                    newValue = DateUtil.formatDateTime((Date) newValue);
                }
                if(oldValue == null || "".equals(oldValue)){
                    oldValue = "空";
                }
                if(newValue == null || "".equals(newValue)){
                    newValue = "空";
                }
                if(x.getKey().equals(y.getKey()) && ! oldValue.equals(newValue)){
                    if(! "update_time".equals(y.getKey()) && ! "label_id".equals(y.getKey()) && ! "owner_user_id".equals(y.getKey())){
                        if("priority".equals(y.getKey())){
                            String value = "";
                            if(Integer.valueOf(newValue.toString()) == 1){
                                value = "普通";
                            }else if(Integer.valueOf(newValue.toString()) == 2){
                                value = "紧急";
                            }else if(Integer.valueOf(newValue.toString()) == 3){
                                value = "非常紧急";
                            }else{
                                value = "无";
                            }
                            workTaskLog.setContent("修改 优先级 为：" + value + "");
                        }else{
                            workTaskLog.setContent("修改" + getTaileName(y.getKey()) + "为：" + newValue + "");
                        }
                        saveWorkTaskLog(workTaskLog);
                    }
                }
            });
        });
        //判断是否修改了标签
        if(task.getLabelId() != null){
            WorkTaskLabel workTaskLabel;

            if(StrUtil.isEmpty(auldTask.getLabelId())){
                //旧数据没有标签 直接添加
                List<String> labelName = Arrays.asList(task.getLabelId().split(","));
                for(String id : labelName){
                    if(StrUtil.isNotBlank(id)){
                        workTaskLabel = WorkTaskLabel.dao.findById(id);
                        workTaskLog.setContent("增加了标签 " + workTaskLabel.getName());
                        saveWorkTaskLog(workTaskLog);
                    }
                }
            }else{
                //旧数据有标签 自动添加或修改
                List<String> labelName = Arrays.asList(task.getLabelId().split(","));
                for(String id : labelName){
                    if(StrUtil.isNotBlank(id)){
                        if(! auldTask.getLabelId().contains("," + id + ",")){
                            workTaskLabel = WorkTaskLabel.dao.findById(id);
                            workTaskLog.setContent("增加了标签 " + workTaskLabel.getName());
                            saveWorkTaskLog(workTaskLog);
                        }
                    }
                }

                List<String> auldLabelName = Arrays.asList(auldTask.getLabelId().split(","));
                for(String id : auldLabelName){
                    if(StrUtil.isNotBlank(id)){
                        if(! task.getLabelId().contains("," + id + ",")){
                            workTaskLabel = WorkTaskLabel.dao.findById(id);
                            workTaskLog.setContent("删除了标签 " + workTaskLabel.getName());
                            saveWorkTaskLog(workTaskLog);
                        }
                    }

                }
            }
        }
        //判断是参与人
        if(task.getOwnerUserId() != null){
            AdminUser adminUser;
            if(StrUtil.isEmpty(auldTask.getOwnerUserId())){
                //判断旧数据没有参与人
                List<String> userIds = Arrays.asList(task.getOwnerUserId().split(","));
                for(String id : userIds){
                    if(StrUtil.isNotBlank(id)){
                        adminUser = AdminUser.dao.findById(id);
                        workTaskLog.setContent("添加 " + adminUser.getUsername() + "参与任务");
                        saveWorkTaskLog(workTaskLog);
                    }
                }
            }else{
                //判断旧数据有参与人
                List<String> userIds = Arrays.asList(task.getOwnerUserId().split(","));
                for(String id : userIds){
                    if(StrUtil.isNotBlank(id)){
                        if(! auldTask.getOwnerUserId().contains("," + id + ",")){
                            adminUser = AdminUser.dao.findById(id);
                            workTaskLog.setContent("添加 " + adminUser.getUsername() + "参与任务");
                            saveWorkTaskLog(workTaskLog);
                        }
                    }
                }
                List<String> ids = Arrays.asList(auldTask.getOwnerUserId().split(","));
                for(String id : ids){
                    if(StrUtil.isNotBlank(id)){
                        if(! task.getOwnerUserId().contains("," + id + ",")){
                            adminUser = AdminUser.dao.findById(id);
                            workTaskLog.setContent("将 " + adminUser.getUsername() + "从任务中移除");
                            saveWorkTaskLog(workTaskLog);
                        }
                    }
                }
            }
        }
        return true;
    }

    private String getTaileName(String key){
        if("name".equals(key)){
            return "任务名称";
        }else if("start_time".equals(key)){
            return "开始时间";
        }else if("stop_time".equals(key)){
            return "结束时间";
        }else if("description".equals(key)){
            return "任务描述";
        }
        return "";
    }

    /**
     * @author zxy
     * 添加任务与业务关联
     */
    public R svaeTaskRelation(TaskRelation taskRelation, Integer userId){
        Db.delete("delete from `72crm_task_relation` where task_id = ?", taskRelation.getTaskId());
        taskRelation.setCreateTime(DateUtil.date());
        return taskRelation.save() ? R.ok() : R.error();
    }


    @Before(Tx.class)
    public R deleteTask(Integer taskId){
        Task task = new Task().dao().findById(taskId);
        if(task == null){
            return R.error("任务不存在！");
        }
        boolean bol;
        if(task.getPid() != 0){
            bol = task.delete();
        }else {
            bol = Db.update("update 72crm_task set ishidden = 1,hidden_time = now() where task_id = ?", taskId) > 0;
        }
        return bol ? R.ok() : R.error();
    }


    /**
     * @author zxy
     * crm查询任务
     */
    public R queryTaskRelation(BasePageRequest<TaskRelation> basePageRequest){
        TaskRelation relation = basePageRequest.getData();
        if(AuthUtil.oaAnth(relation.toRecord())){
            return R.noAuth();
        }
        Page<Record> paginate = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("work.task.queryTaskRelation", Kv.by("businessIds", relation.getBusinessIds()).set("contactsIds", relation.getContactsIds()).set("contractIds", relation.getContractIds()).set("customerIds", relation.getCustomerIds())));
        paginate.getList().forEach(this::composeUser);
        return R.ok().put("data", paginate);
    }

    private void composeUser(Record record){
        Integer createUserId = record.getInt("create_user_id");
        record.set("createUser",Db.findFirst("select user_id,realname,img from 72crm_admin_user where user_id = ?", createUserId));
        Integer mainUserId = record.getInt("main_user_id");
        record.set("mainUser",Db.findFirst("select user_id,realname,img from 72crm_admin_user where user_id = ?", mainUserId));
        String ownerUserId = record.getStr("owner_user_id");
        List<Record> ownerUserList = new ArrayList<>();
        TagUtil.toSet(ownerUserId).forEach(userId-> ownerUserList.add(Db.findFirst("select user_id,realname,img from 72crm_admin_user where user_id = ?", userId)));
        record.set("ownerUserList",ownerUserList);
    }


    public R getTaskList(BasePageRequest basePageRequest, String labelId, String ishidden){
        Page<Record> recordList = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("work.task.myTask", Kv.by("userId", BaseUtil.getUser().getUserId()).set("labelId", labelId).set("ishidden", ishidden)));
        return R.ok().put("data", recordList);
    }

    public R archiveByTaskId(Integer taskId){
        int update = Db.update("update  `72crm_task` set is_archive = 1,archive_time = now() where task_id = ?", taskId);
        return update > 0 ? R.ok() : R.error();
    }
}
