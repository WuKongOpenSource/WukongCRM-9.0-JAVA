package com.kakarote.crm9.erp.work.service;

import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.erp.work.entity.WorkTaskLabel;
import com.kakarote.crm9.utils.AuthUtil;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LabelService{
    @Inject
    private WorkbenchService workbenchService;

    @Inject
    private WorkService workService;

    public R setLabel(WorkTaskLabel taskLable) {
        boolean bol;
        if (taskLable.getLabelId() == null) {
            taskLable.setCreateTime(new Date());
            taskLable.setCreateUserId(BaseUtil.getUser().getUserId());
            bol = taskLable.save();
        } else {

            bol = taskLable.update();
        }
        return bol ? R.ok() : R.error();
    }

    public R deleteLabel(String labelId){
        Integer count = Db.queryInt("select count(*) from 72crm_task where label_id like concat('%,',?,',%');", labelId);
        if(count>0){
            return R.error("使用中的标签不能删除");
        }
        Db.delete("delete from 72crm_work_task_label where label_id = ?",labelId);
        return R.ok();
    }

    public R getLabelList(){
        List<WorkTaskLabel> all = new WorkTaskLabel().dao().findAll();
        return R.ok().put("data",all);
    }

    public R queryById(Integer labelId){
        return R.ok().put("data",WorkTaskLabel.dao.findById(labelId));
    }

    /**
     * 标签任务列表
     */
    public R getTaskList(Integer labelId){
        List<Record> taskList = Db.find(Db.getSqlPara("work.label.queryTaskList", Kv.by("labelId",labelId).set("userId",BaseUtil.getUserId())));
        workbenchService.taskListTransfer(taskList);
        Map<Integer,List<Record>> map = taskList.stream().collect(Collectors.groupingBy(record -> record.getInt("work_id")));
        List<Record> workList = Db.find(Db.getSqlPara("work.label.queryWorkList",Kv.by("labelId",labelId).set("userId",BaseUtil.getUserId())));
        workList.forEach(work -> work.set("list",map.get(work.getInt("work_id"))));
        return R.ok().put("data",workList);
    }

    public R getLabelListByOwn(){
        Long userId = BaseUtil.getUserId();
        List<String> labelIdList = Db.query(Db.getSql("work.label.queryLabelIdList"), userId, userId, userId);
        List<Integer> list = workService.toList(labelIdList);
        List<String> collect = list.stream().map(Object::toString).collect(Collectors.toList());
        List<WorkTaskLabel> resultList;
        if(AuthUtil.isWorkAdmin()){
            resultList = new WorkTaskLabel().dao().findAll();
        }else {
            resultList = new WorkTaskLabel().find("select * from `72crm_work_task_label` where label_id in (?)", String.join(",", collect));
        }
        return R.ok().put("data",resultList);
    }
}
