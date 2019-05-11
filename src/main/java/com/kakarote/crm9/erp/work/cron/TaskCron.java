package com.kakarote.crm9.erp.work.cron;


import com.jfinal.plugin.activerecord.Db;

/**
 * 定时扫描延期任务
 */
public class TaskCron implements Runnable {

    @Override
    public void run() {
        Db.update("update 72crm_task set status = 2 where stop_time < now()");
    }
}
