package com.kakarote.crm9.erp.work.service;

import cn.hutool.core.util.StrUtil;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.entity.AdminFile;
import com.kakarote.crm9.erp.admin.service.AdminFileService;
import com.kakarote.crm9.erp.work.entity.Work;
import com.kakarote.crm9.erp.work.entity.WorkRelation;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import java.util.*;
import java.util.stream.Collectors;

public class WorkService{

    @Inject
    private AdminFileService adminFileService;

    @Before(Tx.class)
    public R setWork(Work work, String customerIds, String contactsIds, String businessIds, String contractIds){
        boolean bol;
        if(work.getOwnerUserId() != null){
            work.setOwnerUserId("," + work.getOwnerUserId() + ",");
        }
        if(work.getWorkId() == null){
            Integer createUserId = BaseUtil.getUser().getUserId().intValue();
            work.setCreateUserId(createUserId);
            work.setCreateTime(new Date());
            bol = work.save();
        }else{
            Db.delete("delete from 72crm_work_relation where work_id = ?", work.getWorkId());
            bol = work.update();
        }
        Integer workId = work.getWorkId();
        if(StrUtil.isNotEmpty(customerIds)){
            for(String id : customerIds.split(",")){
                WorkRelation relation = new WorkRelation();
                relation.setWorkId(workId);
                relation.setRelationId(Integer.valueOf(id));
                relation.setRelationType(1);
                relation.setCreateTime(new Date());
                relation.save();
            }
        }
        if(StrUtil.isNotEmpty(contactsIds)){
            for(String id : contactsIds.split(",")){
                WorkRelation relation = new WorkRelation();
                relation.setWorkId(workId);
                relation.setRelationId(Integer.valueOf(id));
                relation.setRelationType(2);
                relation.setCreateTime(new Date());
                relation.save();
            }
        }
        if(StrUtil.isNotEmpty(businessIds)){
            for(String id : businessIds.split(",")){
                WorkRelation relation = new WorkRelation();
                relation.setWorkId(workId);
                relation.setRelationId(Integer.valueOf(id));
                relation.setRelationType(3);
                relation.setCreateTime(new Date());
                relation.save();
            }
        }
        if(StrUtil.isNotEmpty(contractIds)){
            for(String id : contractIds.split(",")){
                WorkRelation relation = new WorkRelation();
                relation.setWorkId(workId);
                relation.setRelationId(Integer.valueOf(id));
                relation.setRelationType(4);
                relation.setCreateTime(new Date());
                relation.save();
            }
        }
        return bol ? R.ok() : R.error();
    }

    public R deleteWork(String workId){
        int update = Db.update("update 72crm_work setUser ishidden = 1 where work_id = ?", workId);
        return update > 0 ? R.ok() : R.error();
    }

    public R queryWorkNameList(){
        List<Record> recordList = Db.find(Db.getSqlPara("work.queryWorkNameList", Kv.by("userId", BaseUtil.getUser().getUserId().intValue())));
        return R.ok().put("data", recordList);
    }

    public R queryTaskByWorkId(String workId, String status){
        List<Record> recordList = Db.find(Db.getSqlPara("work.queryTaskByWorkId", Kv.by("workId", workId).set("status", status)));
        Map<String,List<Record>> className = recordList.stream().collect(Collectors.groupingBy(record -> record.getStr("className")));
        return R.ok().put("data", className);
    }


    public R queryTaskFileByWorkId(String workId){
        String batchId = Db.queryStr("select batch_id from 72crm_work where work_id = ?", workId);
        List<AdminFile> adminFiles = adminFileService.queryByBatchId(batchId);
        return R.ok().put("data", adminFiles);
    }

    public R queryArchiveWorkList(BasePageRequest request){
        Page<Record> recordPage = Db.paginate(request.getPage(), request.getLimit(), "select work_id,archive_time,name ", "from 72crm_work where status = 3");
        return R.ok().put("data", recordPage);
    }

    public R workStatistics(String workId){
        Record taskStatistics = Db.findFirst(Db.getSqlPara("work.workStatistics", Kv.by("workId",workId)));
        List<Record> classStatistics = new ArrayList<>();
        List<Record> labelStatistics = new ArrayList<>();
        List<Record> recordList = Db.find("select class_id classId,name className from 72crm_work_task_class a  where a.work_id = ?", workId);
        Map<String,Object> classMap = new HashMap<>();
        recordList.forEach(record -> classMap.put(record.getStr("classId"), record.getStr("className")));
        classMap.forEach((classId, name) -> {
            Record first = Db.findFirst("select count(status = 5 or null) as complete,count(status != 5 or null) as undone from 72crm_task where class_id = ? and work_id = ?", classId, workId);
            first.set("className",classMap.get(classId));
            classStatistics.add(first);
        });
        List<String> labelList = Db.query("select lable_id from 72crm_task where work_id  = ?", workId);
        List<Integer> labelIdList = toList(labelList);
        Set<Integer> labelIdSet = new HashSet<>(labelIdList);
        Map<Integer,Record> labelMap = new HashMap<>();
        labelIdSet.forEach(id ->{
            Record record = Db.findFirst("select lable_id,name,color from 72crm_work_task_lable where lable_id = ?", id);
            labelMap.put(record.getInt("lable_id"), record);
        });
        labelMap.forEach((id, record) -> {
            long count = labelIdList.stream().filter(i -> i.equals(id)).count();
            record.set("count", Long.valueOf(count).doubleValue()/labelIdList.size());
            labelStatistics.add(record);
        });
        List<Record> memberTaskStatistics = memberTaskStatistics(workId);
        Kv result = Kv.by("taskStatistics", taskStatistics).set("classStatistics", classStatistics).set("labelStatistics", labelStatistics).set("memberTaskStatistics",memberTaskStatistics);
        return R.ok().put("data",result);
    }

    private List<Integer> toList(List<String> labelList){
        List<Integer> list = new ArrayList<>();
        if(labelList == null || labelList.size() == 0){
            return list;
        }
        labelList.forEach(ids -> {
            if(StrUtil.isNotEmpty(ids)){
                for(String id : ids.split(",")){
                    if(StrUtil.isNotEmpty(id)){
                        list.add(Integer.valueOf(id));
                    }
                }
            }
        });
        return list;
    }

    /**
     * 项目成员任务统计
     */
    public List<Record> memberTaskStatistics(String workId){
        List<Record> list = new ArrayList<>();
        String ownerUserIds = Db.queryStr("select owner_user_id from 72crm_work where work_id = ?", workId);
        if(StrUtil.isEmpty(ownerUserIds)){
            return list;
        }
        for(String userId : ownerUserIds.split(",")){
            if(StrUtil.isEmpty(userId)){
                continue;
            }
            Record user = Db.findFirst("select user_id,realname,img from 72crm_admin_user where user_id = ?", userId);
            user.setColumns(Db.findFirst(Db.getSqlPara("work.workStatistics", Kv.by("workId", workId).set("userId", userId))));
            list.add(user);
        }
        return list;
    }
}
