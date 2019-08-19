package com.kakarote.crm9.erp.work.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kakarote.crm9.erp.work.entity.Task;
import com.kakarote.crm9.erp.work.entity.TaskRelation;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.kakarote.crm9.utils.TagUtil;

import java.util.*;
import java.util.stream.Collectors;

public class WorkbenchService{

    @Inject
    private TaskService taskService;

    public R myTask(Integer userId){
        List<Record> result = new ArrayList<>();
        result.add(new Record().set("title", "收件箱").set("is_top", 0).set("count", 0).set("list", new ArrayList<>()));
        result.add(new Record().set("title", "今天要做").set("is_top", 1).set("count", 0).set("list", new ArrayList<>()));
        result.add(new Record().set("title", "下一步要做").set("is_top", 2).set("count", 0).set("list", new ArrayList<>()));
        result.add(new Record().set("title", "以后要做").set("is_top", 3).set("count", 0).set("list", new ArrayList<>()));
        result.forEach(record -> {
            Integer isTop = record.getInt("is_top");
            List<Record> resultist = Db.find(Db.getSqlPara("work.workbench.myTask", Kv.by("userId", userId).set("isTop", isTop)));
            record.set("count", resultist.size());
            if(resultist.size() != 0){
                resultist.sort(Comparator.comparingInt(o -> o.getInt("top_order_num")));
                taskListTransfer(resultist);
                record.set("list", resultist);
            }

        });
        return R.ok().put("data", result);
    }

    public void taskListTransfer(List<Record> taskList){
        taskList.forEach(task -> {
            Date stopTime = task.getDate("stop_time");
            if(stopTime != null){
                if(stopTime.getTime() < System.currentTimeMillis()){
                    task.set("isEnd", 1);
                }else{
                    task.set("isEnd", 0);
                }
            }else{
                task.set("isEnd", 0);
            }
            Integer taskId = task.getInt("task_id");
            task.set("mainUser", Db.findFirst("select user_id,realname,img from 72crm_admin_user where user_id = ?", task.getInt("main_user_id")));
            task.set("createUser", Db.findFirst("select user_id,realname,img from 72crm_admin_user where user_id = ?", task.getInt("create_user_id")));
            ArrayList<Record> labelList = new ArrayList<>();
            if(StrUtil.isNotBlank(task.getStr("label_id"))){
                String[] lableIds = task.getStr("label_id").split(",");
                for(String lableId : lableIds){
                    if(StrUtil.isNotBlank(lableId)){
                        Record lable = Db.findFirst("select label_id,name as labelName,color from 72crm_work_task_label where label_id = ?", lableId);
                        labelList.add(lable);
                    }
                }
            }
            task.set("labelList", labelList);
            TaskRelation taskRelation = new TaskRelation().findFirst("select * from `72crm_task_relation` where task_id = ?", taskId);
            int relationCount = 0;
            if(taskRelation != null){
                relationCount += TagUtil.toSet(taskRelation.getBusinessIds()).size();
                relationCount += TagUtil.toSet(taskRelation.getContactsIds()).size();
                relationCount += TagUtil.toSet(taskRelation.getCustomerIds()).size();
                relationCount += TagUtil.toSet(taskRelation.getContractIds()).size();
            }
            task.set("relationCount", relationCount);
        });
    }

    public R dateList(String startTime, String endTime){
        List<Task> taskList = Task.dao.find(Db.getSqlPara("work.workbench.dateList", Kv.by("userId", BaseUtil.getUser().getUserId()).set("startTime", startTime).set("endTime", endTime)));
        return R.ok().put("data", taskList);
    }

    @Before(Tx.class)
    public R updateTop(JSONObject jsonObject){
        String updateSql = "update `72crm_task` set is_top = ?,top_order_num = ? where task_id = ?";
        if(jsonObject.containsKey("fromList")){
            JSONArray fromlist = jsonObject.getJSONArray("fromList");
            Integer fromTopId = jsonObject.getInteger("fromTopId");
            for(int i = 1; i <= fromlist.size(); i++){
                Db.update(updateSql, fromTopId, i, fromlist.get(i - 1));
            }
        }
        if(jsonObject.containsKey("toList")){
            JSONArray tolist = jsonObject.getJSONArray("toList");
            Integer toTopId = jsonObject.getInteger("toTopId");
            for(int i = 1; i <= tolist.size(); i++){
                Db.update(updateSql, toTopId, i, tolist.get(i - 1));
            }
        }
        return R.ok();
    }
}
