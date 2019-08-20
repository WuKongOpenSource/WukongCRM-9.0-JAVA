package com.kakarote.crm9.erp.bi.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Aop;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.bi.common.BiTimeUtil;
import com.kakarote.crm9.erp.oa.service.OaExamineService;

import java.util.*;

public class BiWorkService {
    @Inject
    private BiTimeUtil biTimeUtil;

    /**
     * 查询日志统计信息
     * @author zhang
     */
    public List<Record> logStatistics(Integer deptId, Long userId, String type) {
        Record record = new Record().set("deptId", deptId).set("userId", userId).set("type", type);
        biTimeUtil.analyzeType(record);
        List<Record> records = new ArrayList<>();
        for (String uid : StrUtil.splitTrim(record.getStr("userIds"),",")) {
            List<Record> recordList = Db.find(Db.getSqlPara("bi.work.queryLogByUser", record.set("userId", uid)));
            if (recordList.size() > 0) {
                Record userRecord = new Record().setColumns(recordList.get(0)).remove("sum", "send_user_ids", "read_user_ids");
                int commentCount = 0, unCommentCount = 0, unReadCont = 0, count = recordList.size();
                for (Record task : recordList) {
                    if (task.getInt("sum") > 0) {
                        commentCount++;
                    } else {
                        unCommentCount++;
                    }
                    String sendUser = task.getStr("send_user_ids");
                    if (StrUtil.isNotEmpty(sendUser) && sendUser.split(",").length > 0) {
                        if(!isIntersection(StrUtil.splitTrim(sendUser,","),StrUtil.splitTrim(record.getStr("read_user_ids"),","))){
                            unReadCont++;
                        }
                    }
                }
                userRecord.set("commentCount",commentCount)
                        .set("unCommentCount",unCommentCount)
                        .set("unReadCont",unReadCont)
                        .set("count",count);
                records.add(userRecord);
            }
        }
        return records;
    }

    /**
     * 查询审批统计信息
     * @author zhang
     */
    public JSONObject examineStatistics(Integer deptId, Long userId, String type){
        JSONObject object=new JSONObject();
        Record record = new Record().set("deptId", deptId).set("userId", userId).set("type", type);
        biTimeUtil.analyzeType(record);
        List<Record> categoryList= Db.find(Db.getSql("bi.work.queryExamineCategory"));
        object.put("categoryList",categoryList);
        List<String> users=StrUtil.splitTrim(record.getStr("userIds"),",");
        if(users.size()==0){
            object.put("userList",users);
        }else {
            SqlPara sql=Db.getSqlPara("bi.work.examineStatistics",record.set("categorys",categoryList).set("userList",users));
            List<Record> userList=Db.find(sql);
            object.put("userList",userList);
        }
        return object;
    }

    /**
     * 审批详情
     * @author zhangz
     */
    public Record examineInfo(BasePageRequest request){
        JSONObject jsonObject = request.getJsonObject();
        Record record = new Record().set("userId", jsonObject.getInteger("userId")).set("type", jsonObject.get("type"));
        biTimeUtil.analyzeType(record);
        Kv kv= Kv.by("userId", jsonObject.get("userId")).set("categoryId", jsonObject.get("categoryId")).set("startTime", record.get("beginDate")).set("endTime", record.get("endDate"));
        Page<Record> recordList = Db.paginate(request.getPage(), request.getLimit(), Db.getSqlPara("oa.examine.myInitiate", kv));
        Aop.get(OaExamineService.class).transfer(recordList.getList());
        SqlPara sqlPara=Db.getSqlPara("bi.work.queryExamineCount",kv);
        Record info=Db.findFirst(sqlPara);
        info.set("list",recordList.getList()).set("totalRow",recordList.getTotalRow());
        return info;
    }


    /**
     * 判断两个数组是否有交集
     * @author zhang
     * @return true为存在交集
     */
    private static boolean isIntersection(List<String> m, List<String> n) {
        // 将较长的数组转换为set
        Set<String> set = new HashSet<>(m.size() > n.size() ? m : n);

        for (String i : m.size() > n.size() ? n : m) {
            if (set.contains(i)) {
                return true;
            }
        }
        return false;
    }
}
