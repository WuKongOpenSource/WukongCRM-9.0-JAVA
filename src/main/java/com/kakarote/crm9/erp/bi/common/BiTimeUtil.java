package com.kakarote.crm9.erp.bi.common;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.jfinal.aop.Aop;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.erp.admin.service.AdminDeptService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author wyq
 */
public class BiTimeUtil {
    public Record analyzeType(Record record){
        Date beginDate = DateUtil.date();
        Date endDate = DateUtil.date();
        Integer cycleNum = 12;
        String sqlDateFormat = "%Y%m";
        String dateFormat = "yyyyMM";
        String userIds;
        Integer deptId = record.getInt("deptId");
        Long userId = record.getLong("userId");
        String type = record.getStr("type");
        String startTime = record.getStr("startTime");
        String endTime = record.getStr("endTime");
        if (StrUtil.isNotEmpty(type)){
            if ("year".equals(type)){
                beginDate = DateUtil.beginOfYear(DateUtil.date());
                endDate = DateUtil.endOfYear(DateUtil.date());
            }else if ("lastYear".equals(type)){
                beginDate = DateUtil.beginOfYear(DateUtil.offsetMonth(DateUtil.date(),-12));
                endDate = DateUtil.endOfYear(DateUtil.offsetMonth(DateUtil.date(),-12));
            }else if ("quarter".equals(type)){
                beginDate = DateUtil.beginOfQuarter(DateUtil.date());
                endDate = DateUtil.endOfQuarter(DateUtil.date());
                cycleNum = 3;
            }else if ("lastQuarter".equals(type)){
                beginDate = DateUtil.beginOfQuarter(DateUtil.offsetMonth(DateUtil.date(),-3));
                endDate = DateUtil.endOfQuarter(DateUtil.offsetMonth(DateUtil.date(),-3));
                cycleNum = 3;
            }else if ("month".equals(type)){
                beginDate = DateUtil.beginOfMonth(DateUtil.date());
                endDate = DateUtil.endOfMonth(DateUtil.date());
                sqlDateFormat = "%Y%m%d";
                dateFormat = "yyyyMMdd";
                cycleNum = (int)DateUtil.between(beginDate,endDate,DateUnit.DAY)+1;
            }else if ("lastMonth".equals(type)){
                beginDate = DateUtil.beginOfMonth(DateUtil.offsetMonth(DateUtil.date(),-1));
                endDate = DateUtil.endOfMonth(DateUtil.offsetMonth(DateUtil.date(),-1));
                sqlDateFormat = "%Y%m%d";
                dateFormat = "yyyyMMdd";
                cycleNum = (int)DateUtil.between(beginDate,endDate,DateUnit.DAY)+1;
            }else if ("week".equals(type)){
                beginDate = DateUtil.beginOfWeek(DateUtil.date());
                endDate = DateUtil.endOfWeek(DateUtil.date());
                sqlDateFormat = "%Y%m%d";
                dateFormat = "yyyyMMdd";
                cycleNum = 7;
            }else if ("lastWeek".equals(type)){
                beginDate = DateUtil.beginOfWeek(DateUtil.offsetWeek(DateUtil.date(),-1));
                endDate = DateUtil.endOfWeek(DateUtil.offsetWeek(DateUtil.date(),-1));
                sqlDateFormat = "%Y%m%d";
                dateFormat = "yyyyMMdd";
                cycleNum = 7;
            }else if ("today".equals(type)){
                beginDate = DateUtil.beginOfDay(DateUtil.date());
                endDate = DateUtil.endOfDay(DateUtil.date());
                sqlDateFormat = "%Y%m%d";
                dateFormat = "yyyyMMdd";
                cycleNum = 1;
            }else if ("yesterday".equals(type)){
                beginDate = DateUtil.offsetDay(DateUtil.date(),-1);
                endDate = DateUtil.offsetDay(DateUtil.date(),-1);
                sqlDateFormat = "%Y%m%d";
                dateFormat = "yyyyMMdd";
                cycleNum = 1;
            }
        }else if (StrUtil.isNotEmpty(startTime) && StrUtil.isNotEmpty(endTime)){
            Date start = DateUtil.parse(startTime);
            Date end = DateUtil.parse(endTime);
            Integer startMonth = Integer.valueOf(DateUtil.format(start,"yyyyMM"));
            Integer endMonth = Integer.valueOf(DateUtil.format(end,"yyyyMM"));
            if (startMonth.equals(endMonth)){
                sqlDateFormat = "%Y%m%d";
                dateFormat = "yyyyMMdd";
                Long diffDay = DateUtil.between(start,end,DateUnit.DAY);
                cycleNum = diffDay.intValue() + 1;
            }else {
                sqlDateFormat = "%Y%m";
                dateFormat = "yyyyMM";
                Integer diffYear = Integer.valueOf(endMonth.toString().substring(0,4)) - Integer.valueOf(startMonth.toString().substring(0,4));
                Integer diffMonth = endMonth%100 - startMonth%100 + 1;
                cycleNum = diffYear*12 + diffMonth;
            }
            beginDate = start;
            endDate = end;
        }
        if (userId != null){
            userIds = userId.toString();
        }else if(deptId!=null){
            List<Record> records = Aop.get(AdminDeptService.class).queryDeptByParentDept(deptId, BaseConstant.AUTH_DATA_RECURSION_NUM);
            List<Integer> deptIds = new ArrayList<>();
            deptIds.add(deptId);
            records.forEach(dept ->deptIds.add(dept.getInt("id")));
            SqlPara sqlPara = Db.getSqlPara("admin.user.queryUserIdByDeptId", Kv.by("deptIds", deptIds));
            List<Long> userIdList =  Db.query(sqlPara.getSql(), sqlPara.getPara());
            userIds = CollectionUtil.join(userIdList,",");
        }else {
            userIds="";
        }
        Integer beginTime = Integer.valueOf(DateUtil.format(beginDate,dateFormat));
        Integer finalTime = Integer.valueOf(DateUtil.format(endDate,dateFormat));
        record.set("sqlDateFormat",sqlDateFormat).set("beginDate",beginDate).set("endDate",endDate).set("cycleNum",cycleNum).set("userIds",userIds).set("beginTime",beginTime).set("finalTime",finalTime);
        return record;
    }

    public Integer estimateTime(Integer beginTime){
        if (beginTime < 1000000 && beginTime%100 == 12){
            beginTime = beginTime + 89;
        }else if (beginTime > 1000000){
            String s = beginTime.toString();
            Date date = DateUtil.parse(s,"yyyyMMdd");
            Integer month = DateUtil.month(DateUtil.parse(beginTime.toString(),"yyyyMMdd"))+1;
            if (month == 1||month == 3||month == 5||month == 7||month == 8||month == 10){
                if (beginTime%100 == 31){
                    beginTime = beginTime + 70;
                }else {
                    beginTime++;
                }
            }else if (month == 4||month == 6||month == 8||month == 9||month == 11){
                if (beginTime%100 == 30){
                    beginTime = beginTime + 71;
                }else {
                    beginTime++;
                }
            }else if (month == 2){
                Integer year = DateUtil.year(DateUtil.parse(beginTime.toString(),"yyyyMMdd"));
                if (DateUtil.isLeapYear(year) && beginTime%100 == 29){
                    beginTime = beginTime +72;
                }else if (!DateUtil.isLeapYear(year) && beginTime%100 == 28){
                    beginTime = beginTime+73;
                }else {
                    beginTime++;
                }
            }else if (month == 12){
                if (beginTime%100 == 31){
                    beginTime = beginTime + 8870;
                }else{
                    beginTime++;
                }
            }
        }else {
            beginTime++;
        }
        return beginTime;
    }

    public Integer analyzeType(String type){
        int status;
        if (type == null){
            return 11;
        }
        switch(type){
            case "year":
                status = 9;
                break;
            case "lastYear":
                status = 10;
                break;
            case "quarter":
                status = 7;
                break;
            case "lastQuarter":
                status = 8;
                break;
            case "month":
                status = 5;
                break;
            case "lastMonth":
                status = 6;
                break;
            case "week":
                status = 3;
                break;
            case "lastWeek":
                status = 4;
                break;
            case "today":
                status = 1;
                break;
            case "yesterday":
                status = 2;
                break;
            default:
                status = 11;
                break;
        }
        return status;
    }
    public String[] getAddress(){
        return new String[]{"北京","上海","天津","广东","浙江",
                "海南","福建","湖南","湖北","重庆","辽宁","吉林",
                "黑龙江","河南","河北","陕西","甘肃","宁夏","西藏",
                "新疆","青海","四川","广西","贵州","江西","江苏",
                "云南","内蒙古","山东","山西"};
    }
}
