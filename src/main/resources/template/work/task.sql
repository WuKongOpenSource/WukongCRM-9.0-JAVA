#namespace("work.task")
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
  #sql("getTaskList")
    select a.*,
      (select count(*) from 72crm_task_comment where type_id = a.task_id and type = 1) as commentCount,
      (select count(*) from 72crm_task where pid = a.task_id and status = 5) as childWCCount,
      (select count(*) from 72crm_task where pid = a.task_id) as childAllCount,
      (select count(*) from 72crm_admin_file where batch_id = a.batch_id) as fileCount
      from 72crm_task a
      where a.pid = 0 and a.ishidden = 0
      #if(type == null  || type == 0)
        and ( a.main_user_id in (
           #for(i : userIds)
            #(for.index > 0 ? "," : "")#para(i)
           #end
        )
          or a.create_user_id in (
           #for(i : userIds)
            #(for.index > 0 ? "," : "")#para(i)
           #end
          )
          or (
             #for(i : userIds)
              #(for.index > 0 ? "or" : "")
              a.owner_user_id like concat('%,', #para(i),',%')
            #end
          )
        )
      #end
      #if(type == 1)
       and  a.main_user_id in (
           #for(i : userIds)
            #(for.index > 0 ? "," : "")#para(i)
           #end
        )
      #end
      #if(type == 2)
       and  a.create_user_id in (
           #for(i : userIds)
            #(for.index > 0 ? "," : "")#para(i)
           #end
        )
      #end
       #if(type == 3)
       and   (
             #for(i : userIds)
              #(for.index > 0 ? "or" : "")
              a.owner_user_id like concat('%,', #para(i),',%')
            #end
          )
      #end
      #if(status)
       and a.status = #para(status)
      #end
      #if(priority)
       and a.priority = #para(priority)
      #end
       #if(date == 1)
      and TO_DAYS(a.stop_time) = TO_DAYS(now())
      #end
       #if(date == 2)
       and to_days(NOW()) - TO_DAYS(a.stop_time) = -1
      #end
       #if(date == 3)
       and to_days(NOW()) - TO_DAYS(a.stop_time) >= -7 and to_days(NOW()) - TO_DAYS(a.stop_time) <= 0
      #end
       #if(date == 4)
       and to_days(NOW()) - TO_DAYS(a.stop_time) >= -30 and to_days(NOW()) - TO_DAYS(a.stop_time) <= 0
      #end
      #if(taskName)
       and a.name like concat('%', #para(taskName),'%')
      #end
      order by a.create_time desc
  #end
#end
