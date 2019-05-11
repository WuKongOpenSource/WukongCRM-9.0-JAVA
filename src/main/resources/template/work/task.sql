#namespace("work.task")
  #sql("myTask")
      select a.*,b.name as workName,(select count(*) from 72crm_task_comment where type_id = a.task_id and type = 1) as commentCount,
        (select count(*) from 72crm_task where pid = a.task_id and status = 5) as childWCCount,
        (select count(*) from 72crm_task where pid = a.task_id) as childAllCount,
        (select count(*) from 72crm_admin_file where batch_id = a.batch_id) as fileCount
        from 72crm_task a left join 72crm_work b on a.work_id = b.work_id where (a.create_user_id = #para(userId) or a.main_user_id = #para(userId) or  a.owner_user_id like concat('%,',#para(userId),',%'))
        #if(labelId)
          and a.lable_id like concat('%,',#para(labelId),',%')
        #end
        #if(ishidden)
          and a.ishidden = #para(ishidden)
        #else
          and a.ishidden = 0
        #end
  #end
  #sql("myWorkLog")
    SELECT wtl.log_id, wtl.content, wtl.create_time ,au.img,au.realname
      FROM 72crm_work_task_log as wtl
      LEFT JOIN 72crm_admin_user as au on au.user_id = wtl.user_id
      where wtl.task_id = #para(taskId) and wtl.status != 4
      order by wtl.create_time desc
  #end
   #sql("queryTaskRelation")
     SELECT st.*,
      (select count(*) from 72crm_task_comment where type_id = st.task_id and type = 1) as commentCount,
      (select count(*) from 72crm_task where pid = st.task_id and status = 5) as childWCCount,
      (select count(*) from 72crm_task where pid = st.task_id) as childAllCount,
      (select count(*) from 72crm_admin_file where batch_id = st.batch_id) as fileCount
      FROM 72crm_task as st
      LEFT JOIN 72crm_task_relation as str on str.task_id = st.task_id
      where 1 = 2
       #if(businessIds)
        or str.business_ids like concat('%,',#para(businessIds),',%')
      #end
      #if(contactsIds)
        or str.contacts_ids like concat('%,',#para(contactsIds),',%')
      #end
      #if(contractIds)
       or str.contract_ids like concat('%,',#para(contractIds),',%')
      #end
      #if(customerIds)
        or str.customer_ids like concat('%,',#para(customerIds),',%')
      #end
  #end
  #sql("dateList")
    select task_id,name,date_format(stop_time,'%Y-%m-%d') as stop_time,priority
      from 72crm_task a
      where (a.create_user_id = #para(userId) or a.main_user_id = #para(userId) or a.owner_user_id like concat('%,',#para(userId),',%')) and a.stop_time between #para(startTime) and #para(endTime)
  #end
  #sql("getTaskList")
    select a.*,
      (select count(*) from 72crm_task_comment where type_id = a.task_id and type = 1) as commentCount,
      (select count(*) from 72crm_task where pid = a.task_id and status = 5) as childWCCount,
      (select count(*) from 72crm_task where pid = a.task_id) as childAllCount,
      (select count(*) from 72crm_admin_file where batch_id = a.batch_id) as fileCount
      from 72crm_task a where a.pid = 0 and a.ishidden = 0
  #end
#end
