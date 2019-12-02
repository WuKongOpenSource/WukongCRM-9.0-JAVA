package com.kakarote.crm9.erp.bi.service;

import com.jfinal.plugin.activerecord.SqlPara;
import com.kakarote.crm9.erp.bi.common.MonthEnum;
import com.kakarote.crm9.utils.R;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import java.util.*;
import java.util.stream.Collectors;

public class BiService{

    /**
     * @author hjp
     * 根据商机id查询合同
     */
    public R queryCrmBusinessStatistics(Long userId, Integer deptId, Integer productId, Date startTime, Date endTime){
        List<Record> records = Db.find(Db.getSqlPara("bi.base.queryCrmBusinessStatistics",
                Kv.by("ownerUserId", userId).set("productId", productId)
                        .set("startTime", startTime).set("endTime", endTime).set("deptId", deptId)));
        return R.ok().put("data", records);
    }

    /**
     * 产品销售情况统计
     * startTime 开始时间 endTime 结束时间 userId用户ID deptId部门ID
     */
    public R queryProductSell(Date startTime, Date endTime, Long userId, Integer deptId){
        List<Record> recordList = Db.find(Db.getSqlPara("bi.base.queryProductSell", Kv.by("startTime", startTime).set("endTime", endTime).set("userId", userId).set("deptId", deptId)));
        List<Record> productList = recordList.stream().sorted(Comparator.comparing(record -> record.getStr("productName"))).collect(Collectors.toList());
        return R.ok().put("data", productList);
    }

    /**
     * 获取商业智能业绩目标完成情况
     *
     * @author wyq
     */
    public R taskCompleteStatistics(String year, Integer status, Integer deptId, Integer userId){
        List<Record> recordList;
        Kv kv = Kv.by("map", MonthEnum.values()).set("year", year);
        SqlPara sqlPara;
        if(userId == null){
            kv.set("deptId", deptId).set("status", status).set("type", 2).set("objId", deptId);
            sqlPara = Db.getSqlPara("bi.base.taskCompleteStatistics", kv);
        }else{
            kv.set("userId", userId).set("status", status).set("type", 3).set("objId", userId);
            sqlPara = Db.getSqlPara("bi.base.taskCompleteStatistics", kv);
        }
        recordList = Db.find(sqlPara);
        if(recordList.size() == 0){
            return R.error("请先设置业绩目标");
        }
        return R.ok().put("data", recordList);
    }
}
