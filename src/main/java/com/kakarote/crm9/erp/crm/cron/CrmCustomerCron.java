package com.kakarote.crm9.erp.crm.cron;

import com.kakarote.crm9.erp.crm.entity.AdminCustomerSetting;
import com.kakarote.crm9.erp.crm.service.CrmCustomerService;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class CrmCustomerCron implements Runnable {

    private static CrmCustomerService crmCustomerService = new CrmCustomerService();

    @Override
    public void run() {
        Record record = Db.findFirst("select * from 72crm_admin_customer_setting");
        //获取是否启动客户保护规则设置
        if (record == null){
            AdminCustomerSetting adminCustomerSetting = new AdminCustomerSetting();
            adminCustomerSetting.setDealDay(3);
            adminCustomerSetting.setFollowupDay(7);
            adminCustomerSetting.setType(0);
            adminCustomerSetting.save();
        } else {
            if (record.getInt("type") == 1){
                crmCustomerService.putInInternational(record);
            }
        }
    }
}
