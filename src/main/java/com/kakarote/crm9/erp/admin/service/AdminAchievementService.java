package com.kakarote.crm9.erp.admin.service;

import cn.hutool.core.util.StrUtil;
import com.kakarote.crm9.erp.admin.entity.CrmAchievement;
import com.kakarote.crm9.erp.bi.common.MonthEnum;
import com.kakarote.crm9.utils.R;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminAchievementService {
    @Before(Tx.class)
    public R setAchievement(List<CrmAchievement> achievementList) {
        achievementList.forEach(achievement -> {
            Db.delete(Db.getSql("admin.achievement.deleteAchievement"),achievement.getObjId(),achievement.getType(),achievement.getYear(),achievement.getStatus());
            achievement.save();
        });
        return R.ok();
    }

    public R queryAchievementList(CrmAchievement achievement, String userId, Integer deptId) {
        if (achievement.getType() == null) {
            achievement.setType(2);
        }
        if (achievement.getType() == 2) {
            List<Record> deptList = new ArrayList<>();
            //查询部门
            Record record = Db.findFirst(Db.getSql("admin.achievement.queryDeptById"), deptId);
            Record recordInfo = Db.findFirst(Db.getSql("admin.achievement.queryDeptInfo"),achievement.getYear(),achievement.getType(),record.get("dept_id"),achievement.getStatus());
            if (recordInfo == null) {
                recordInfo = new Record();
                for(MonthEnum monthEnum : MonthEnum.values()){
                    recordInfo.set(monthEnum.getName(),0);
                }
                recordInfo.set("yeartarget",0).set("obj_id",deptId).set("year",achievement.getYear()).set("type",achievement.getType()).set("status",achievement.getStatus());
            }
            record.setColumns(recordInfo);
            deptList.add(record);
            List<Record> deptIdList = Db.find(Db.getSql("admin.achievement.queryDeptByPid"), deptId);
            if (deptIdList != null && deptIdList.size() > 0) {
                deptIdList.forEach(record1 -> {
                    Record info = Db.findFirst(Db.getSql("admin.achievement.queryDeptInfo"),achievement.getYear(), achievement.getType(), record1.get("dept_id"), achievement.getStatus());
                    if (info == null) {
                        info = new Record();
                        for(MonthEnum monthEnum : MonthEnum.values()){
                            info.set(monthEnum.getName(),0);
                        }
                        info.set("yeartarget",0).set("obj_id",record1.get("dept_id")).set("year",achievement.getYear()).set("type",achievement.getType()).set("status",achievement.getStatus());
                    }
                    record1.setColumns(info);
                });
                deptList.addAll(deptIdList);
            }
            return R.ok().put("data", deptList);
        } else if (achievement.getType() == 3) {
            List<Record> userIdList = null;
            if (StrUtil.isEmpty(userId)) {
                userIdList = Db.find(Db.getSql("admin.achievement.queryUserByDeptId"), deptId);
            } else {
                userIdList = Db.find(Db.getSql("admin.achievement.queryUserById"), userId);
            }
            if (userIdList != null && userIdList.size() > 0) {
                userIdList.forEach(record -> {
                    Record info = Db.findFirst(Db.getSql("admin.achievement.queryUserInfo"),achievement.getYear(), achievement.getType(), record.get("user_id"), achievement.getStatus());
                    if (info == null) {
                        info = new Record();
                        for(MonthEnum monthEnum : MonthEnum.values()){
                            info.set(monthEnum.getName(),0);
                        }
                        info.set("yeartarget",0).set("obj_id",record.get("user_id")).set("year",achievement.getYear()).set("type",achievement.getType()).set("status",achievement.getStatus());
                    }
                    record.setColumns(info);
                });
            }
            return R.ok().put("data", userIdList);
        }
        return null;
    }

}
