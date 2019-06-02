package com.kakarote.crm9.erp.bi.cron;

import cn.hutool.core.date.DateUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.erp.crm.entity.CrmCustomerStats;

import java.util.List;

/**
 * @author wyq
 */
public class BiCustomerCron implements Runnable{
    @Override
    public void run() {
        List<Record> recordList = Db.find(Db.getSql("bi.customer.biCustomerCron"));
        recordList.forEach(record -> {
            record.set("create_time", DateUtil.date());
            CrmCustomerStats crmCustomerStats = new CrmCustomerStats()._setAttrs(record.getColumns());
            crmCustomerStats.save();
        });
    }
}
