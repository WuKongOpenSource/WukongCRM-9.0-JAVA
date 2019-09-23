#namespace("work.label")
  #sql ("queryTaskList")
  select a.task_id,a.name,a.stop_time,a.priority,a.work_id,a.status,
  (select count(*) from 72crm_admin_file where batch_id = a.batch_id) as file_count,
  (select count(*) from 72crm_task_comment where type_id = a.task_id and type = 1) as comment_count,
  (select count(*) from 72crm_task where pid = a.task_id) as child_all_count,
  (select count(*) from 72crm_task where pid = a.task_id and status = 5) as child_WC_count
  from 72crm_task as a where pid = 0 and find_in_set(#para(labelId),a.label_id) and a.ishidden = 0
    #if(userId)
    and (a.main_user_id = #para(userId) or a.owner_user_id like concat('%,',#para(userId),',%'))
    #end
  #end

  #sql ("queryWorkList")
  select distinct a.work_id,a.name,a.color
  from 72crm_work as a left join 72crm_task as b on a.work_id = b.work_id
  where find_in_set(#para(labelId),b.label_id) and b.ishidden = 0
    #if(userId)
    and (b.main_user_id = #para(userId) or b.owner_user_id like concat('%,',#para(userId),',%'))
    #end
  #end

  #sql ("queryLabelIdList")
  select label_id from `72crm_task` where create_user_id = ? or main_user_id = ? or owner_user_id like concat('%,',?,',%') and ishidden = 0
  #end
#end
