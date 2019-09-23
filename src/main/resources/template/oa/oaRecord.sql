#namespace("oa.record")
  #sql("queryList")
    select a.log_id,a.action_id,a.content as action_content,a.create_time,a.type,a.user_id from 72crm_oa_action_record a  where 1 = 1 and
      case when type = 2 or type = 3 or type = 4 or type = 5 then  (a.join_user_ids like concat('%,',#para(userId),',%') or a.dept_ids like concat('%,',#para(deptId),',%') or a.user_id = #para(userId))
      else (
          #for(i : userIds)
              #(for.index > 0 ? "or" : "")
                a.join_user_ids like concat('%,',#para(i),',%')
          #end
        or a.user_id = #para(userId)
      )

      end
      #if(type)
      and a.type = #para(type)
      #end
    order by a.create_time desc
  #end
  #sql("queryTask")
select task_id,name,create_time,stop_time,priority from 72crm_task where ishidden = 0 and (status = 1 or status = 2)and ( main_user_id = #para(userId) or owner_user_id like concat('%',#para(userId),'%')) and pid = 0 order by create_time desc
  #end
#end
