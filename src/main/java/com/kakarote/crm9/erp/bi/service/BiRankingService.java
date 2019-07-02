package com.kakarote.crm9.erp.bi.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.erp.bi.common.BiTimeUtil;
import com.kakarote.crm9.utils.R;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BiRankingService {

    @Inject
    BiTimeUtil biTimeUtil;

    public R contractRanKing(Integer deptId,String type,String startTime,String endTime){
        Record record = new Record();
        record.set("deptId",deptId).set("type",type).set("startTime",startTime).set("endTime",endTime);
        biTimeUtil.analyzeType(record);
        String userIds = record.getStr("userIds");
        List<Record> list = new ArrayList<>();
        if (StrUtil.isEmpty(userIds)){
            return R.ok().put("data",list);
        }
        String[] userIdsArr = userIds.split(",");
        Integer status = biTimeUtil.analyzeType(type);
         list = Db.find(Db.getSqlPara("bi.ranking.contractRanKing",
                Kv.by("userIds",userIdsArr).set("type",status).set("startTime",startTime).set("endTime",endTime)));
        return R.ok().put("data",list);
    }
    public R receivablesRanKing(Integer deptId,String type,String startTime,String endTime){
        Record record = new Record();
        record.set("deptId",deptId).set("type",type).set("startTime",startTime).set("endTime",endTime);
        biTimeUtil.analyzeType(record);
        String userIds = record.getStr("userIds");
        List<Record> list = new ArrayList<>();
        if (StrUtil.isEmpty(userIds)){
            return R.ok().put("data",list);
        }
        String[] userIdsArr = userIds.split(",");
        Integer status = biTimeUtil.analyzeType(type);
        list = Db.find(Db.getSqlPara("bi.ranking.receivablesRanKing",
                Kv.by("userIds",userIdsArr).set("type",status).set("startTime",startTime).set("endTime",endTime)));
        return R.ok().put("data",list);
    }
    public R contractCountRanKing(Integer deptId,String type,String startTime,String endTime){
        Record record = new Record();
        record.set("deptId",deptId).set("type",type).set("startTime",startTime).set("endTime",endTime);
        biTimeUtil.analyzeType(record);
        String userIds = record.getStr("userIds");
        List<Record> list = new ArrayList<>();
        if (StrUtil.isEmpty(userIds)){
            return R.ok().put("data",list);
        }
        String[] userIdsArr = userIds.split(",");
        Integer status = biTimeUtil.analyzeType(type);
        list = Db.find(Db.getSqlPara("bi.ranking.contractCountRanKing",
                Kv.by("userIds",userIdsArr).set("type",status).set("startTime",startTime).set("endTime",endTime)));
        return R.ok().put("data",list);
    }
    public R productCountRanKing(Integer deptId,String type,String startTime,String endTime){
        Record record = new Record();
        record.set("deptId",deptId).set("type",type).set("startTime",startTime).set("endTime",endTime);
        biTimeUtil.analyzeType(record);
        String userIds = record.getStr("userIds");
        List<Record> list = new ArrayList<>();
        if (StrUtil.isEmpty(userIds)){
            return R.ok().put("data",list);
        }
        String[] userIdsArr = userIds.split(",");
        Integer status = biTimeUtil.analyzeType(type);
       list = Db.find(Db.getSqlPara("bi.ranking.productCountRanKing",
                Kv.by("userIds",userIdsArr).set("type",status).set("startTime",startTime).set("endTime",endTime)));
        return R.ok().put("data",list);
    }
    public R customerCountRanKing(Integer deptId,String type,String startTime,String endTime){
        Record record = new Record();
        record.set("deptId",deptId).set("type",type).set("startTime",startTime).set("endTime",endTime);
        biTimeUtil.analyzeType(record);
        String userIds = record.getStr("userIds");
        List<Record> list = new ArrayList<>();
        if (StrUtil.isEmpty(userIds)){
            return R.ok().put("data",list);
        }
        String[] userIdsArr = userIds.split(",");
        Integer status = biTimeUtil.analyzeType(type);
        list = Db.find(Db.getSqlPara("bi.ranking.customerCountRanKing",
                Kv.by("userIds",userIdsArr).set("type",status).set("startTime",startTime).set("endTime",endTime)));
        return R.ok().put("data",list);
    }
    public R contactsCountRanKing(Integer deptId,String type,String startTime,String endTime){
        Record record = new Record();
        record.set("deptId",deptId).set("type",type).set("startTime",startTime).set("endTime",endTime);
        biTimeUtil.analyzeType(record);
        String userIds = record.getStr("userIds");
        List<Record> list = new ArrayList<>();
        if (StrUtil.isEmpty(userIds)){
            return R.ok().put("data",list);
        }
        String[] userIdsArr = userIds.split(",");
        Integer status = biTimeUtil.analyzeType(type);
        list = Db.find(Db.getSqlPara("bi.ranking.contactsCountRanKing",
                Kv.by("userIds",userIdsArr).set("type",status).set("startTime",startTime).set("endTime",endTime)));
        return R.ok().put("data",list);
    }
    public R customerGenjinCountRanKing(Integer deptId,String type,String startTime,String endTime){
        Record record = new Record();
        record.set("deptId",deptId).set("type",type).set("startTime",startTime).set("endTime",endTime);
        biTimeUtil.analyzeType(record);
        String userIds = record.getStr("userIds");
        List<Record> list = new ArrayList<>();
        if (StrUtil.isEmpty(userIds)){
            return R.ok().put("data",list);
        }
        String[] userIdsArr = userIds.split(",");
        Integer status = biTimeUtil.analyzeType(type);
        list = Db.find(Db.getSqlPara("bi.ranking.customerGenjinCountRanKing",
                Kv.by("userIds",userIdsArr).set("type",status).set("startTime",startTime).set("endTime",endTime)));
        return R.ok().put("data",list);
    }
    public R recordCountRanKing(Integer deptId,String type,String startTime,String endTime){
        Record record = new Record();
        record.set("deptId",deptId).set("type",type).set("startTime",startTime).set("endTime",endTime);
        biTimeUtil.analyzeType(record);
        String userIds = record.getStr("userIds");
        List<Record> list = new ArrayList<>();
        if (StrUtil.isEmpty(userIds)){
            return R.ok().put("data",list);
        }
        String[] userIdsArr = userIds.split(",");
        Integer status = biTimeUtil.analyzeType(type);
        list = Db.find(Db.getSqlPara("bi.ranking.recordCountRanKing",
                Kv.by("userIds",userIdsArr).set("type",status).set("startTime",startTime).set("endTime",endTime)));
        return R.ok().put("data",list);
    }
    public R contractProductRanKing(Integer deptId,Long userId,String type,String startTime,String endTime){
        Record record = new Record();
        record.set("deptId",deptId).set("userId",userId).set("type",type).set("startTime",startTime).set("endTime",endTime);
        biTimeUtil.analyzeType(record);
        String userIds = record.getStr("userIds");
        List<Record> list = new ArrayList<>();
        if (StrUtil.isEmpty(userIds)){
            return R.ok().put("data",list);
        }
        String[] userIdsArr = userIds.split(",");
        Integer status = biTimeUtil.analyzeType(type);
        list = Db.find(Db.getSqlPara("bi.ranking.contractProductRanKing",
                Kv.by("userIds",userIdsArr).set("type",status).set("startTime",startTime).set("endTime",endTime)));
        return R.ok().put("data",list);
    }
    public R travelCountRanKing(Integer deptId,String type,String startTime,String endTime){
        Record record = new Record();
        record.set("deptId",deptId).set("type",type).set("startTime",startTime).set("endTime",endTime);
        biTimeUtil.analyzeType(record);
        String userIds = record.getStr("userIds");
        List<Record> list = new ArrayList<>();
        if (StrUtil.isEmpty(userIds)){
            return R.ok().put("data",list);
        }
        String[] userIdsArr = userIds.split(",");
        Integer status = biTimeUtil.analyzeType(type);
        list = Db.find(Db.getSqlPara("bi.ranking.travelCountRanKing",
                Kv.by("userIds",userIdsArr).set("type",status).set("startTime",startTime).set("endTime",endTime)));
        return R.ok().put("data",list);
    }
    public R productSellRanKing(Integer deptId,Long userId,String type,String startTime,String endTime){
        Record record = new Record();
        record.set("deptId",deptId).set("userId",userId).set("type",type).set("startTime",startTime).set("endTime",endTime);
        biTimeUtil.analyzeType(record);
        String userIds = record.getStr("userIds");
        List<Record> list = new ArrayList<>();
        if (StrUtil.isEmpty(userIds)){
            return R.ok().put("data",list);
        }
        String[] userIdsArr = userIds.split(",");
        Integer status = biTimeUtil.analyzeType(type);
        list = Db.find(Db.getSqlPara("bi.ranking.productSellRanKing",
                Kv.by("userIds",userIdsArr).set("type",status).set("startTime",startTime).set("endTime",endTime)));
        return R.ok().put("data",list);
    }
    public R addressAnalyse(){
        String [] addResss = biTimeUtil.getAddress();
        List<Record> list = new ArrayList<>();
        for (String addRess: addResss) {
            list.add(Db.findFirst(Db.getSqlPara("bi.ranking.addressAnalyse",Kv.by("address",addRess))));
        }
        return R.ok().put("data",list);
    }
    public R portrait(Integer deptId,Long userId,String type,String startTime,String endTime){
        Record record = new Record();
        record.set("deptId",deptId).set("userId",userId).set("type",type).set("startTime",startTime).set("endTime",endTime);
        biTimeUtil.analyzeType(record);
        String userIds = record.getStr("userIds");
        List<Record> list = new ArrayList<>();
        if (StrUtil.isEmpty(userIds)){
            return R.ok().put("data",list);
        }
        String[] userIdsArr = userIds.split(",");
        Integer status = biTimeUtil.analyzeType(type);
        list = Db.find(Db.getSqlPara("bi.ranking.portrait",
                Kv.by("userIds",userIdsArr).set("type",status).set("startTime",startTime).set("endTime",endTime)));
        return R.ok().put("data",list);
    }
    public R portraitLevel(Integer deptId,Long userId,String type,String startTime,String endTime){
        Record record = new Record();
        record.set("deptId",deptId).set("userId",userId).set("type",type).set("startTime",startTime).set("endTime",endTime);
        biTimeUtil.analyzeType(record);
        String userIds = record.getStr("userIds");
        List<Record> list = new ArrayList<>();
        if (StrUtil.isEmpty(userIds)){
            return R.ok().put("data",list);
        }
        String[] userIdsArr = userIds.split(",");
        Integer status = biTimeUtil.analyzeType(type);
        list = Db.find(Db.getSqlPara("bi.ranking.portraitLevel",
                Kv.by("userIds",userIdsArr).set("type",status).set("startTime",startTime).set("endTime",endTime)));
        return R.ok().put("data",list);
    }
    public R portraitSource(Integer deptId,Long userId,String type,String startTime,String endTime){
        Record record = new Record();
        record.set("deptId",deptId).set("userId",userId).set("type",type).set("startTime",startTime).set("endTime",endTime);
        biTimeUtil.analyzeType(record);
        String userIds = record.getStr("userIds");
        List<Record> list = new ArrayList<>();
        if (StrUtil.isEmpty(userIds)){
            return R.ok().put("data",list);
        }
        String[] userIdsArr = userIds.split(",");
        Integer status = biTimeUtil.analyzeType(type);
        list = Db.find(Db.getSqlPara("bi.ranking.portraitSource",
                Kv.by("userIds",userIdsArr).set("type",status).set("startTime",startTime).set("endTime",endTime)));
        return R.ok().put("data",list);
    }
}
