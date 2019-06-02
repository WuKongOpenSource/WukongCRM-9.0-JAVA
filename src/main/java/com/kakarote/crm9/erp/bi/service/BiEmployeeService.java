package com.kakarote.crm9.erp.bi.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.erp.bi.common.BiTimeUtil;
import com.kakarote.crm9.utils.R;

import java.util.List;

/**
 * @author wyq
 */
public class BiEmployeeService {
    @Inject
    BiTimeUtil biTimeUtil;

    /**
     * 业绩分析
     * @author wyq
     */
    public R contractNumStats(Integer deptId,Long userId,String type,String year){
        Record record = new Record();
        record.set("deptId",deptId).set("userId",userId).set("startTime", year+"0101").set("endTime",year+"1231");
        biTimeUtil.analyzeType(record);
        Integer cycleNum = 12;
        String userIds = record.getStr("userIds");
        if (StrUtil.isEmpty(userIds)){
            userIds = "0";
        }
        Integer beginTime = record.getInt("beginTime");
        StringBuffer sqlStringBuffer = new StringBuffer();
        if ("contractNum".equals(type)){
            for (int i=1; i <= cycleNum;i++){
                sqlStringBuffer.append("select '").append(beginTime).append("' as month,count(contract_id) as thisMonth," +
                        "(select count(contract_id) from 72crm_crm_contract where DATE_FORMAT(order_date,'%Y%m ') = '")
                        .append(beginTime-1).append("' and check_status = 2 and owner_user_id in (").append(userIds)
                        .append(")) as lastMonth,(select count(contract_id) from 72crm_crm_contract where DATE_FORMAt(order_date,'%Y%m') = '")
                        .append(beginTime-100).append("' and check_status = 2  and owner_user_id in (").append(userIds)
                        .append(")) as lastYear from 72crm_crm_contract where DATE_FORMAT(order_date,'%Y%m') = '")
                        .append(beginTime).append("' and check_status = 2 and owner_user_id in (").append(userIds).append(")");
                if (i != cycleNum){
                    sqlStringBuffer.append(" union all ");
                }
                beginTime = biTimeUtil.estimateTime(beginTime);
            }
        }else if ("contractMoney".equals(type)){
            for (int i=1; i <= cycleNum;i++){
                sqlStringBuffer.append("select '").append(beginTime).append("' as month,IFNULL(SUM(money),0) as thisMonth," +
                        "(select IFNULL(SUM(money),0) from 72crm_crm_contract where DATE_FORMAT(order_date,'%Y%m ') = '")
                        .append(beginTime-1).append("' and check_status = 2 and owner_user_id in (").append(userIds)
                        .append(")) as lastMonth,(select IFNULL(SUM(money),0) from 72crm_crm_contract where DATE_FORMAt(order_date,'%Y%m') = '")
                        .append(beginTime-100).append("' and check_status = 2  and owner_user_id in (").append(userIds)
                        .append(")) as lastYear from 72crm_crm_contract where DATE_FORMAT(order_date,'%Y%m') = '")
                        .append(beginTime).append("' and check_status = 2 and owner_user_id in (").append(userIds).append(")");
                if (i != cycleNum){
                    sqlStringBuffer.append(" union all ");
                }
                beginTime = biTimeUtil.estimateTime(beginTime);
            }
        }else if ("receivables".equals(type)){
            for (int i=1; i <= cycleNum;i++){
                sqlStringBuffer.append("select '").append(beginTime).append("' as month,IFNULL(SUM(money),0) as thisMonth," +
                        "(select IFNULL(SUM(money),0) from 72crm_crm_receivables where DATE_FORMAT(return_time,'%Y%m ') = '")
                        .append(beginTime-1).append("' and check_status = 2 and owner_user_id in (").append(userIds)
                        .append(")) as lastMonth,(select IFNULL(SUM(money),0) from 72crm_crm_receivables where DATE_FORMAt(return_time,'%Y%m') = '")
                        .append(beginTime-100).append("' and check_status = 2  and owner_user_id in (").append(userIds)
                        .append(")) as lastYear from 72crm_crm_receivables where DATE_FORMAT(return_time,'%Y%m') = '")
                        .append(beginTime).append("' and check_status = 2 and owner_user_id in (").append(userIds).append(")");
                if (i != cycleNum){
                    sqlStringBuffer.append(" union all ");
                }
                beginTime = biTimeUtil.estimateTime(beginTime);
            }
        }
        List<Record> recordList = Db.find(sqlStringBuffer.toString());
        recordList.forEach(r -> {
            Integer thisMonth = r.getInt("thisMonth");
            Integer lastMonth = r.getInt("lastMonth");
            Integer lastYear = r.getInt("lastYear");
            r.set("lastMonthGrowth",thisMonth != 0 && lastMonth != 0 ? thisMonth*100/lastMonth : 0);
            r.set("lastYearGrowth",thisMonth != 0 && lastYear != 0 ? thisMonth*100/lastYear : 0);
        });
        return R.ok().put("data",recordList);
    }

    /**
     * 合同汇总表
     * @author wyq
     */
    public R totalContract(Integer deptId,Long userId,String type,String startTime,String endTime){
        Record record = new Record();
        record.set("deptId",deptId).set("userId",userId).set("type",type).set("startTime",startTime).set("endTime",endTime);
        biTimeUtil.analyzeType(record);
        String sqlDateFormat = record.getStr("sqlDateFormat");
        String userIds = record.getStr("userIds");
        if (StrUtil.isEmpty(userIds)){
            userIds = "0";
        }
        Integer beginTime = record.getInt("beginTime");
        Integer finalTime = record.getInt("finalTime");
        Integer cycleNum = record.getInt("cycleNum");
        Record total = Db.findFirst(Db.getSqlPara("bi.employee.totalContract", Kv.by("sqlDateFormat",sqlDateFormat).set("beginTime",beginTime).set("finalTime",finalTime).set("userIds",userIds)));
        StringBuffer sqlStringBuffer = new StringBuffer();
        for (int i=1; i <= cycleNum;i++){
            sqlStringBuffer.append("select '").append(beginTime).append("'as type,count(contract_id) as contractNum,IFNULL(SUM(money),0) " +
                    "as contractMoney,IFNULL((select SUM(money) from 72crm_crm_receivables as b where b.contract_id = a.contract_id),0)" +
                    " as receivablesMoney from 72crm_crm_contract as a where DATE_FORMAT(order_date,'").append(sqlDateFormat)
                    .append("') = '").append(beginTime).append("' and check_status = 2 and owner_user_id in (").append(userIds).append(")");
            if (i != cycleNum){
                sqlStringBuffer.append(" union all ");
            }
            beginTime = biTimeUtil.estimateTime(beginTime);
        }
        List<Record> recordList = Db.find(sqlStringBuffer.toString());
        return R.ok().put("data",total.set("list",recordList));
    }
}
