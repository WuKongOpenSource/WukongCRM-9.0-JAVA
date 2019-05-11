#namespace("work")
  #sql("queryWorkNameList")
    select distinct a.work_id,a.name
      from 72crm_work a left join 72crm_task b on a.work_id = b.work_id
      where a.is_open = 1
      or (a.is_open = 2 and (b.create_user_id = #para(userId) or b.main_user_id = #para(userId) or b.owner_user_id like concat('%,', #para(userId), ',%')))
      or (a.owner_user_id like concat('%,',1,',%'))
  #end
  #sql("queryTaskByWorkId")
    select a.*,ifnull(b.name,'未分组') as className,b.order_num as classOrder,
      (select count(*) from 72crm_task_comment where type_id = a.task_id and type = 1) as commentCount,
      (select count(*) from 72crm_task where pid = a.task_id and status = 5) as childWCCount,
      (select count(*) from 72crm_task where pid = a.task_id) as childAllCount,
      (select count(*) from 72crm_admin_file where batch_id = a.batch_id) as fileCount
      from 72crm_task a left join 72crm_work_task_class b on a.class_id = b.class_id where a.work_id = #para(workId)
      #if(status)
        and a.status = #para(status)
      #end
      order by b.order_num
  #end
  #sql("workStatistics")
    select count(*) as allCount,
      count(status=1 or null) as pending,
      count(status=2 or null) as extension,
      count(status=5 or null) as complete,
      ifnull(ROUND((count(status=5 or null)/count(*))*100,2),0) as completionRate,
      ifnull(ROUND((count(status=2 or null)/count(*))*100,2),0) as extensionRate
      from 72crm_task where work_id = #para(workId)
      #if(userId)
        and (main_user_id = #para(userId) or owner_user_id like concat('%,',#para(userId),',%'))
      #end
  #end
#end
