#namespace("work.workbench")
  #sql("myTask")
    select a.*,b.name as workName,(select count(*) from 72crm_task_comment where type_id = a.task_id and type = 1) as commentCount,
           (select count(*) from 72crm_task where pid = a.task_id and status = 5) as childWCCount,
           (select count(*) from 72crm_task where pid = a.task_id) as childAllCount,
           (select count(*) from 72crm_admin_file where batch_id = a.batch_id) as fileCount
    from 72crm_task a left join 72crm_work b on a.work_id = b.work_id where (a.owner_user_id like concat('%,',#para(userId),',%') or a.main_user_id = #para(userId)) and a.pid = 0  and a.ishidden = 0 and a.is_top = #para(isTop) and (a.status = 1 or a.status = 2) and a.is_archive = 0
  #end
  #sql("dateList")
    select task_id,name,date_format(start_time,'%Y-%m-%d') as start_time,date_format(stop_time,'%Y-%m-%d') as stop_time,priority,create_time,update_time
    from 72crm_task a
    where (a.main_user_id = #para(userId) or a.owner_user_id like concat('%,',#para(userId),',%')) and ( a.stop_time between #para(startTime) and #para(endTime) or a.stop_time is null ) and a.pid = 0 and (a.status = 1 or a.status = 2) and a.is_archive = 0  and a.ishidden = 0
      #end
#end
