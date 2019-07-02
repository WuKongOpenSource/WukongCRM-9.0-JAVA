package com.kakarote.crm9.erp.crm.cron;

import com.jfinal.aop.Aop;
import com.kakarote.crm9.erp.admin.entity.AdminConfig;
import com.kakarote.crm9.erp.crm.service.CrmCustomerService;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class CrmCustomerCron implements Runnable {
    @Override
    public void run() {
        String dealDay = Db.queryStr("select value from 72crm_admin_config where name = 'customerPoolSettingDealDays'");
        String followupDay = Db.queryStr("select value from 72crm_admin_config where name = 'customerPoolSettingFollowupDays'");
        Integer type = Db.queryInt("select status from 72crm_admin_config where name = 'customerPoolSetting'");
        //获取是否启动客户保护规则设置
        if (dealDay == null || followupDay == null || type == null){
            if (dealDay == null){
                AdminConfig adminConfig = new AdminConfig();
                adminConfig.setName("customerPoolSettingDealDays");
                adminConfig.setValue("3");
                adminConfig.setDescription("客户公海规则设置未成交天数");
                adminConfig.save();
            }
            if (followupDay == null){
                AdminConfig adminConfig = new AdminConfig();
                adminConfig.setName("customerPoolSettingFollowupDays");
                adminConfig.setValue("7");
                adminConfig.setDescription("客户公海规则设置未跟进天数");
                adminConfig.save();
            }
            if (type == null){
                AdminConfig adminConfig = new AdminConfig();
                adminConfig.setName("customerPoolSetting");
                adminConfig.setStatus(0);
                adminConfig.setDescription("客户公海规则设置");
                adminConfig.save();
            }
        } else {
            if (type == 1){
                Record record = new Record();
                record.set("dealDay",dealDay).set("followupDay",followupDay);
                Aop.get(CrmCustomerService.class).putInInternational(record);
            }
        }
    }
}
