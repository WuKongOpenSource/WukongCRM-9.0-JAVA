package com.kakarote.crm9.erp.bi.service;

import cn.hutool.core.date.DateUtil;
import com.kakarote.crm9.utils.R;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BiService {

    /**
     * @author hjp
     * 根据商机id查询合同
     */
    public R queryCrmBusinessStatistics(Long userId, Integer deptId, Integer productId, Date startTime, Date endTime) {
        List<Record> records = Db.find(Db.getSqlPara("bi.base.queryCrmBusinessStatistics",
                Kv.by("ownerUserId",userId).set("productId",productId)
                        .set("startTime",startTime).set("endTime",endTime).set("deptId",deptId)));
        return R.ok().put("data",records);
    }
    /**
     * 产品销售情况统计
     * startTime 开始时间 endTime 结束时间 userId用户ID deptId部门ID
     */
    public R queryProductSell(Date startTime,Date endTime,Integer userId,Integer deptId){
        List<Record> categorys = Db.find(Db.getSqlPara("bi.base.queryProductSell",Kv.by("startTime",startTime).set("endTime",endTime).set("userId",userId).set("deptId",deptId)));
        return  R.ok().put("data",categorys);
    }
    /**
     * 回款统计，根据月份获取合同信息
     *  userId用户ID deptId部门ID
     */
    public R queryByUserIdOrYear(Integer userId,Integer deptId,String year , String month){
        List<Record> categorys = Db.find(Db.getSqlPara("bi.base.queryByUserIdOrYear",
                Kv.by("userId",userId).set("deptId",deptId)
                        .set("year",year).set("month",month)));
        return  R.ok().put("data",categorys);
    }

    /**
     * 获取商业智能业绩目标完成情况
     * @author wyq
     */
    public R taskCompleteStatistics(String year, Integer type, Integer deptId, Integer userId){
        if (type == 1){
            if (userId == null){
                return R.ok().put("data",Db.find(Db.getSqlPara("bi.base.queryContractByDeptId",Kv.by("year",year).set("deptId",deptId))));
            }else {
                return R.ok().put("data",Db.find(Db.getSqlPara("bi.base.queryContractByUserId",Kv.by("year",year).set("userId",userId))));
            }
        }else if (type == 2){
            if (userId == null){
                return R.ok().put("data",Db.find(Db.getSqlPara("bi.base.queryReceivablesByDeptId",Kv.by("year",year).set("deptId",deptId))));
            }else {
                return R.ok().put("data",Db.find(Db.getSqlPara("bi.base.queryReceivablesByUserId",Kv.by("year",year).set("userId",userId))));
            }
        }else {
            return R.error("type不符合要求");
        }

    }
}
