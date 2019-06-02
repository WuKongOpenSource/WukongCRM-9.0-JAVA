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
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.kakarote.crm9.utils.TagUtil;

import java.util.*;
import java.util.stream.Collectors;

public class TaskService {
    //添加日志
    @Inject
    private OaActionRecordService oaActionRecordService;

    @Inject
    private AdminFileService adminFileService;

    public R setTaskClass(WorkTaskClass taskClass) {
        boolean bol;
        if (taskClass.getClassId() == null) {
            taskClass.setClassId(BaseUtil.getUser().getUserId().intValue());
            taskClass.setCreateTime(new Date());
            bol = taskClass.save();
        } else {
            bol = taskClass.update();
        }
        return R.isSuccess(bol);
    }


    public void changeOrderTaskClass(String originalClassId, String targetClassId) {
        WorkTaskClass originalClass = WorkTaskClass.dao.findById(originalClassId);
        WorkTaskClass targetClass = WorkTaskClass.dao.findById(targetClassId);
        Integer originalClassOrderId = originalClass.getOrderNum();
        Integer targetClassOrderId = targetClass.getOrderNum();
        Db.update("update 72crm_work_task_class setUser order_id = ? where class_id = ?", originalClassOrderId, targetClassId);
        Db.update("update 72crm_work_task_class setUser order_id = ? where class_id = ?", targetClassOrderId, originalClassId);
    }
    @Before(Tx.class)
    public R setTask(Task task , TaskRelation taskRelation) {
        AdminUser user = BaseUtil.getUser();
        boolean bol;
        if(task.getOwnerUserId() != null){
            task.setOwnerUserId(TagUtil.fromString(task.getOwnerUserId()));
        }
        if(task.getLableId() != null){
            task.setLableId(TagUtil.fromString(task.getLableId()));
        }
        if (task.getTaskId() == null) {
            task.setCreateTime(new Date());
            task.setCreateUserId(user.getUserId().intValue());
            task.setBatchId(IdUtil.simpleUUID());
            bol = task.save();
            WorkTaskLog workTaskLog = new WorkTaskLog();
            workTaskLog.setUserId(user.getUserId().intValue());
            workTaskLog.setTaskId(task.getTaskId());
            workTaskLog.setContent("添加了新任务 " + task.getName());
            saveWorkTaskLog(workTaskLog);

        } else {
            task.setUpdateTime(new Date());
            bol =  getWorkTaskLog(task,user.getUserId().intValue());
        }
        if (taskRelation.getBusinessIds()!= null||taskRelation.getContactsIds()!= null|| taskRelation.getContractIds() != null || taskRelation.getCustomerIds() != null){
            Db.deleteById("72crm_task_relation","task_id",task.getTaskId());
            taskRelation.setCreateTime(DateUtil.date());
            taskRelation.setTaskId(task.getTaskId());
            taskRelation.save();
        }
        task.getMainUserId();
        oaActionRecordService.addRecord(task.getTaskId(), OaEnum.TASK_TYPE_KEY.getTypes(),task.getUpdateTime()==null?1:2,oaActionRecordService.getJoinIds(user.getUserId().intValue(),getJoinUserIds(task)),oaActionRecordService.getJoinIds(user.getDeptId(),""));
        return bol ? R.ok().put("data",Kv.by("task_id",task.getTaskId())) : R.error();
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

    public R myTask(Integer userId) {
        List<Record> recordList = Db.find(Db.getSqlPara("work.task.myTask", Kv.by("userId", userId)));
        Map<Integer, List<Record>> isTopGroup = recordList.stream().collect(Collectors.groupingBy(record -> record.getInt("is_top")));
        return R.ok().put("data", isTopGroup);
    }

    public R setLable(WorkTaskLable taskLable) {
        boolean bol;
        if (taskLable.getLableId() == null) {
            taskLable.setCreateTime(new Date());
            taskLable.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
            bol = taskLable.save();
        } else {

            bol = taskLable.update();
        }
        return bol ? R.ok() : R.error();
    }

    public R queryTaskInfo(String taskId) {
        Record mainTask = transfer(taskId);
        adminFileService.queryByBatchId(mainTask.get("batch_id"),mainTask);
        List<Record> recordList = Db.find("select task_id from 72crm_task where pid = ?", taskId);
        List<Record> childTaskList = new ArrayList<>();
        if (recordList != null && recordList.size() > 0) {
            recordList.forEach(childTaskRecord -> {
                String childTaskId = childTaskRecord.getStr("task_id");
                Record childTask = transfer(childTaskId);
                adminFileService.queryByBatchId(childTask.getStr("batch_id"),childTask);
                childTaskList.add(childTask);
            });
        }
        mainTask.set("childTask", childTaskList);
        return R.ok().put("data", mainTask);
    }

    private Record transfer(String taskId) {
        Record task = Db.findFirst("select * from 72crm_task where task_id = ?", taskId);
        task.set("stop_time",DateUtil.formatDate(task.getDate("stop_time")));
        task.set("mainUser",Db.findFirst("select user_id,realname,img from 72crm_admin_user where user_id = ?",task.getInt("main_user_id")));
        task.set("createUser",Db.findFirst("select user_id,realname,img from 72crm_admin_user where user_id = ?",task.getInt("create_user_id")));
        ArrayList<Record> labelList = new ArrayList<>();
        ArrayList<Record> ownerUserList = new ArrayList<>();
        if (StrUtil.isNotBlank(task.getStr("lable_id"))) {
            String[] lableIds = task.getStr("lable_id").split(",");
            for (String lableId : lableIds) {
                if (StrUtil.isNotBlank(lableId)) {
                    Record lable = Db.findFirst("select lable_id,name as lableName,color from 72crm_work_task_lable where lable_id = ?", lableId);
                    labelList.add(lable);
                }
            }
        }
        if (StrUtil.isNotBlank(task.getStr("owner_user_id"))) {
            String[] ownerUserIds = task.getStr("owner_user_id").split(",");
            for (String ownerUserId : ownerUserIds) {
                if (StrUtil.isNotBlank(ownerUserId)) {
                    Record ownerUser = Db.findFirst("select user_id,realname,img from 72crm_admin_user where user_id = ?", ownerUserId);
                    ownerUserList.add(ownerUser);
                }
            }
        }
        List<Record> relations = Db.find("select * FROM 72crm_task_relation where task_id = ?",taskId);
        if (relations != null){
            for (Record relation: relations) {
                List<Record> customerList = new ArrayList<>();
                List<Record> contactsList = new ArrayList<>();
                List<Record> businessList = new ArrayList<>();
                List<Record> contractList = new ArrayList<>();
                if (StrUtil.isNotBlank(relation.getStr("customer_ids"))){
                    String[] customerIds = relation.getStr("customer_ids").split(",");
                    for (String customerId: customerIds) {
                        if (StrUtil.isNotBlank(customerId)){
                            Record customer = Db.findFirst("select customer_id,customer_name  from 72crm_crm_customer where customer_id = ?", customerId);
                            if(customer != null){
                                customerList.add(customer);
                            }
                        }
                    }
                }

                if (StrUtil.isNotBlank(relation.getStr("contacts_ids"))){
                    String[] contactsIds = relation.getStr("contacts_ids").split(",");

                    for (String contactsId: contactsIds) {
                        if (StrUtil.isNotBlank(contactsId)) {
                            Record contacts = Db.findFirst("select contacts_id,name from 72crm_crm_contacts  where contacts_id = ?", contactsId);
                            if(contacts != null){
                                contactsList.add(contacts);
                            }
                        }
                    }
                }
                if (StrUtil.isNotBlank(relation.getStr("business_ids"))){
                    String[] businessIds = relation.getStr("business_ids").split(",");

                    for (String businessId: businessIds) {
                        if (StrUtil.isNotBlank(businessId)) {
                            Record business = Db.findFirst("select business_id,business_name  from 72crm_crm_business  where business_id = ?", businessId);
                            if(business != null){
                                businessList.add(business);
                            }
                        }
                    }
                }
                if (StrUtil.isNotBlank(relation.getStr("contract_ids"))){
                    String[] contractIds = relation.getStr("contract_ids").split(",");

                    for (String contractId: contractIds) {
                        if (StrUtil.isNotBlank(contractId)) {
                            Record contract = Db.findFirst("select contract_id,name from 72crm_crm_contract  where contract_id = ?", contractId);
                            if(contract != null){
                                contractList.add(contract);
                            }
                        }
                    }
                    task.set("contractList",contractList);
                }
                task.set("customerList",customerList);
                task.set("contactsList",contactsList);
                task.set("businessList",businessList);
                task.set("contractList",contractList);
            }
        }
        return task.set("labelList", labelList).set("ownerUserList", ownerUserList);
    }


    /**
     * 查询任务列表
     */
    public R getTaskList(Integer type , Integer status, Integer priority, Integer date, List<Integer> userIds, BasePageRequest<Task> basePageRequest, String name){
        String total = "select count(*) from 72crm_task a where a.pid = 0 and a.ishidden = 0 ";
        StringBuffer sql = new StringBuffer();
        Page<Record> page = new Page<>();
        if (userIds.size() > 0){
        if ( type == null || type == 0){

            sql.append(" and ( a.main_user_id in ( ");
            for (int i = 0;i < userIds.size();i++){
                if (i == 0){
                    sql.append("'").append(userIds.get(i)).append("'");
                }else {
                    sql.append(",'").append(userIds.get(i)).append("'");
                }
            }
            sql.append(")");
            sql.append(" or a.create_user_id in ( ");
            for (int i = 0;i < userIds.size();i++){
                if (i == 0){
                    sql.append("'").append(userIds.get(i)).append("'");
                }else {
                    sql.append(",'").append(userIds.get(i)).append("'");
                }
            }
            sql.append(")");
            sql.append(" or ( ");
            for (int i = 0;i < userIds.size();i++){
                if (i == 0){
                    sql.append(" a.owner_user_id like concat('%,',").append(userIds.get(i)).append(",',%')");
                }else {
                    sql.append(" or a.owner_user_id like concat('%,',").append(userIds.get(i)).append(",',%')");
                }
            }
            sql.append("))");
        } else
         if ( type == 1){
            sql.append(" and a.main_user_id in ( ");
            for (int i = 0;i < userIds.size();i++){
                if (i == 0){
                    sql.append("'").append(userIds.get(i)).append("'");
                }else {
                    sql.append(",'").append(userIds.get(i)).append("'");
                }
            }
            sql.append(")");
        }else if (type == 2){
            sql.append(" and a.create_user_id in ( ");
            for (int i = 0;i < userIds.size();i++){
                if (i == 0){
                    sql.append("'").append(userIds.get(i)).append("'");
                }else {
                    sql.append(",'").append(userIds.get(i)).append("'");
                }
            }
            sql.append(")");
        }else if (type == 3){
            sql.append(" and ( ");
            for (int i = 0;i < userIds.size();i++){
                if (i == 0){
                    sql.append(" a.owner_user_id like concat('%,',").append(userIds.get(i)).append(",',%')");
                }else {
                    sql.append(" or a.owner_user_id like concat('%,',").append(userIds.get(i)).append(",',%')");
                }
            }
            sql.append(")");
        }
        }else {
            page.setList(new ArrayList<>());
        }
        if (status != null ){
            sql.append(" and a.status = ").append(status);
        }
        if (priority != null){
            sql.append(" and a.priority = ").append(priority);
        }
        if (date != null){
            if (date == 1)
            { sql.append(" and TO_DAYS(a.stop_time) = TO_DAYS(now()) ");}
            if (date == 2)
            { sql.append(" and to_days(NOW()) - TO_DAYS(a.stop_time) = -1 ");}
            if (date == 3)
            { sql.append(" and to_days(NOW()) - TO_DAYS(a.stop_time) >= -7 and to_days(NOW()) - TO_DAYS(a.stop_time) <= 0 ");}
            if (date == 4)
            { sql.append(" and to_days(NOW()) - TO_DAYS(a.stop_time) >= -30 and to_days(NOW()) - TO_DAYS(a.stop_time) <= 0 ");}
        }
        if (StrUtil.isNotEmpty(name)){
            sql.append(" and a.name like '%").append(name).append("%' ");
        }
        sql.append(" order by a.create_time desc ");
        page = Db.paginateByFullSql(basePageRequest.getPage(),basePageRequest.getLimit(),total + sql,Db.getSql("work.task.getTaskList")+sql);
        page.setList(queryUser(page.getList()));
        return R.ok().put("data", page);
    }
    private List<Record> queryUser(List<Record> tasks){
        ArrayList<Record> labelList ;
        ArrayList<Record> ownerUserList;
        for (Record task:tasks) {
            labelList = new ArrayList<>();
            ownerUserList = new ArrayList<>();
            if (StrUtil.isNotBlank(task.getStr("lable_id"))) {
                String[] lableIds = task.getStr("lable_id").split(",");
                for (String lableId : lableIds) {
                    if (StrUtil.isNotBlank(lableId)) {
                        Record lable = Db.findFirst("select lable_id,name as lableName , color from 72crm_work_task_lable where lable_id = ?", lableId);
                        labelList.add(lable);
                    }
                }
            }
            if (StrUtil.isNotBlank(task.getStr("owner_user_id"))) {
                String[] ownerUserIds = task.getStr("owner_user_id").split(",");
                for (String ownerUserId : ownerUserIds) {
                    if (StrUtil.isNotBlank(ownerUserId)) {
                        Record ownerUser = Db.findFirst("select user_id,realname,img from 72crm_admin_user where user_id = ?", ownerUserId);
                        ownerUserList.add(ownerUser);
                    }
                }
            }
            TaskRelation taskRelation = TaskRelation.dao.findFirst(" select * from 72crm_task_relation where task_id = ?",task.getInt("task_id"));
            Integer start = 0;
            if (taskRelation != null){
                start = queryCount(start,taskRelation.getBusinessIds());
                start = queryCount(start,taskRelation.getContactsIds());
                start = queryCount(start,taskRelation.getContractIds());
                start = queryCount(start,taskRelation.getCustomerIds());
            }
            task.set("relationCount",start);
            if (task.getDate("stop_time") != null){
                Calendar date = Calendar.getInstance();
                date.setTime(DateUtil.date());
                //设置开始时间
                Calendar begin = Calendar.getInstance();
                begin.setTime(task.getDate("stop_time"));
                if (date.after(begin) && task.getInt("status") != 5 && task.getInt("status") != 2){
                    task.set("is_end",1);
                }else {
                    task.set("is_end",0);
                }
            }else {
                task.set("is_end",0);
            }
            task.set("labelList", labelList).set("ownerUserList", ownerUserList);
        }
        return tasks;
    }
    private Integer queryCount(Integer start , String str){
        // start 开始个数
        if (str != null){
            String[] ownerUserIds = str.split(",");
            for (String ownerUserId : ownerUserIds) {
                if (StrUtil.isNotBlank(ownerUserId)) {
                    ++start;
                }
            }
        }

         return start;
    }
    public R queryWorkTaskLog(Integer taskId){
        List<Record> recordList = Db.find(Db.getSqlPara("work.task.myWorkLog", Kv.by("taskId", taskId)));
       return R.ok().put("data",recordList);
    }

    private void saveWorkTaskLog(WorkTaskLog workTaskLog){
        workTaskLog.setCreateTime(DateUtil.date());
        workTaskLog.setLogId(null);
        workTaskLog.save();
    }
    @Before(Tx.class)
    private boolean getWorkTaskLog(Task task,Integer userId){
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
        Set<Map.Entry<String, Object>> newEntries =task._getAttrsEntrySet();
        Set<Map.Entry<String, Object>> oldEntries =auldTask._getAttrsEntrySet();
        newEntries.forEach(x->{
            oldEntries.forEach(y -> {
                Object oldValue = y.getValue();
                Object newValue = x.getValue();
                if(oldValue instanceof Date){
                    oldValue = DateUtil.formatDateTime((Date) oldValue);
                }
                if(newValue instanceof Date){
                    newValue = DateUtil.formatDateTime((Date) newValue);
                }
                if (oldValue == null || "".equals(oldValue)) {
                    oldValue = "空";
                }
                if (newValue == null || "".equals(newValue)) {
                    newValue = "空";
                }
                if (x.getKey().equals(y.getKey()) && !oldValue.equals(newValue)) {
                    if(!"update_time".equals(y.getKey()) && !"lable_id".equals(y.getKey()) && !"owner_user_id".equals(y.getKey())){
                        if ("priority".equals(y.getKey())){
                            String value = "";
                            if(Integer.valueOf(newValue.toString()) == 1) {
                                value = "普通";
                            }else if(Integer.valueOf(newValue.toString()) == 2) {
                                value = "紧急";
                            }else if(Integer.valueOf(newValue.toString()) == 3) {
                                value = "非常紧急";
                            }else  {
                                value = "无";
                            }
                            workTaskLog.setContent("修改 优先级 为："+ value + "");
                        }else {
                            workTaskLog.setContent("修改" +getTaileName(y.getKey()) + "为："+ newValue + "");
                        }
                        saveWorkTaskLog(workTaskLog);
                    }
                }
            });
        });
        //判断是否修改了标签
        if (task.getLableId() != null){
            WorkTaskLable workTaskLable ;

            if (StrUtil.isEmpty(auldTask.getLableId())){
                //旧数据没有标签 直接添加
                List<String> lableName = Arrays.asList(task.getLableId().split(","));
                for (String id: lableName) {
                    if(StrUtil.isNotBlank(id)){
                        workTaskLable = WorkTaskLable.dao.findById(id);
                        workTaskLog.setContent("增加了标签 " + workTaskLable.getName());
                        saveWorkTaskLog(workTaskLog);
                    }
                }
            }else {
                //旧数据有标签 自动添加或修改
                   List<String> lableName = Arrays.asList(task.getLableId().split(","));
                   for (String id: lableName) {
                       if(StrUtil.isNotBlank(id)){
                           if(! auldTask.getLableId().contains("," + id + ",")){
                               workTaskLable = WorkTaskLable.dao.findById(id);
                               workTaskLog.setContent("增加了标签 " + workTaskLable.getName());
                               saveWorkTaskLog(workTaskLog);
                           }
                       }
                   }

                   List<String> auldLableName = Arrays.asList(auldTask.getLableId().split(","));
                   for (String id: auldLableName) {
                       if(StrUtil.isNotBlank(id)){
                           if(! task.getLableId().contains("," + id + ",")){
                               workTaskLable = WorkTaskLable.dao.findById(id);
                               workTaskLog.setContent("删除了标签 " + workTaskLable.getName());
                               saveWorkTaskLog(workTaskLog);
                           }
                       }

               }
            }
        }
        //判断是参与人
        if (task.getOwnerUserId() != null){
            AdminUser adminUser ;
            if (StrUtil.isEmpty(auldTask.getOwnerUserId())){
                //判断旧数据没有参与人
                List<String> userIds = Arrays.asList(task.getOwnerUserId().split(","));
                for (String id: userIds) {
                    if(StrUtil.isNotBlank(id)){
                        adminUser = AdminUser.dao.findById(id);
                        workTaskLog.setContent("添加 " + adminUser.getUsername()+"参与任务");
                        saveWorkTaskLog(workTaskLog);
                    }
                }
            }else {
                //判断旧数据有参与人
                List<String> userIds = Arrays.asList(task.getOwnerUserId().split(","));
                for (String id: userIds) {
                    if(StrUtil.isNotBlank(id)){
                        if(! auldTask.getOwnerUserId().contains("," + id + ",")){
                            adminUser = AdminUser.dao.findById(id);
                            workTaskLog.setContent("添加 " + adminUser.getUsername() + "参与任务");
                            saveWorkTaskLog(workTaskLog);
                        }
                    }
                }
                List<String> ids = Arrays.asList(auldTask.getOwnerUserId().split(","));
                for (String id: ids) {
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
        if ("name".equals(key)){
            return "任务名称";
        }else  if ("start_time".equals(key)){
            return "开始时间";
        }else  if ("stop_time".equals(key)){
            return "结束时间";
        }else  if ("description".equals(key)){
            return "任务描述";
        }
        return "";
    }
    /**
     * @author zxy
     * 添加任务与业务关联
     */
    public R svaeTaskRelation(TaskRelation taskRelation,Integer userId){
        taskRelation.setCreateTime(DateUtil.date());
        WorkTaskLog workTaskLog = new WorkTaskLog();
        workTaskLog.setUserId(userId);
        workTaskLog.setTaskId(taskRelation.getTaskId());

        saveWorkTaskLog(workTaskLog);
        return taskRelation.save()?R.ok():R.error();
    }

    public R getLableList(){
        List<WorkTaskLable> all = new WorkTaskLable().dao().findAll();
        return R.ok().put("data",all);
    }

    @Before(Tx.class)
    public R deleteTask(Integer taskId){
        Task task = new Task().dao().findById(taskId);
        if(task==null){
            return R.error("任务不存在！");
        }
        int userId = BaseUtil.getUser().getUserId().intValue();
        if(task.getCreateUserId() != userId && (task.getMainUserId()!=null && task.getMainUserId() != userId)){
            return R.error("您无权删除任务！");
        }
        boolean bol = Db.update("update 72crm_task set ishidden = 1 where task_id = ?", taskId)>0;
        return bol?R.ok():R.error();
    }

    public R deleteLable(String lableId){
        Integer count = Db.queryInt("select count(*) from 72crm_task where lable_id like concat('%,',?,',%');", lableId);
        if(count>0){
            return R.error("使用中的标签不能删除");
        }
        Db.delete("delete from 72crm_work_task_lable where lable_id = ?",lableId);
        return R.ok();
    }

    /**
     * @author zxy
     * crm查询任务
     */
    public R queryTaskRelation(BasePageRequest<TaskRelation> basePageRequest){
        TaskRelation relation = basePageRequest.getData();
        return R.ok().put("data",Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("work.task.queryTaskRelation", Kv.by("businessIds", relation.getBusinessIds()).set("contactsIds", relation.getContactsIds()).set("contractIds", relation.getContractIds()).set("customerIds", relation.getCustomerIds()))));
    }

    public R dateList(String startTime, String endTime){
        List<Task> taskList = Task.dao.find(Db.getSqlPara("work.task.dateList", Kv.by("userId", BaseUtil.getUser().getUserId()).set("startTime", startTime).set("endTime", endTime)));
        Map<String,List<Task>> dateListMap = taskList.stream().collect(Collectors.groupingBy(Task::getStopTime));
        return R.ok().put("data",dateListMap);
    }

    public R getTaskList(BasePageRequest basePageRequest, String labelId, String ishidden){
        Page<Record> recordList = Db.paginate(basePageRequest.getPage(),basePageRequest.getLimit(),Db.getSqlPara("work.task.myTask", Kv.by("userId", BaseUtil.getUser().getUserId()).set("labelId",labelId).set("ishidden",ishidden)));
        return R.ok().put("data",recordList);
    }
}
