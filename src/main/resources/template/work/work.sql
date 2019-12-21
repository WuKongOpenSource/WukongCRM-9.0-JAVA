#namespace("work")
  #sql("queryWorkNameList")
    select  a.work_id,a.name,owner_user_id
    from 72crm_work a
    where 1 = 1 and status = 1
      #if(userId)
      and (owner_user_id like concat('%,',#para(userId),',%') and is_open = 0) or is_open = 1
      #end
  #end
  #sql("queryOwnerWorkIdList")
      select  a.work_id
      from 72crm_work a
      where 1 = 1
        #if(userId)
      and (owner_user_id like concat('%,',#para(userId),',%') and is_open = 0) or is_open = 1
        #end
  #end
  #sql("queryTaskByWorkId")
    select a.*,ifnull(b.name,'未分组') as className,ifnull(b.class_id,0) as classId,b.order_num as classOrder,
      (select count(*) from 72crm_task_comment where type_id = a.task_id and type = 1) as commentCount,
      (select count(*) from 72crm_task where pid = a.task_id and status = 5) as childWCCount,
      (select count(*) from 72crm_task where pid = a.task_id) as childAllCount,
      (select count(*) from 72crm_admin_file where batch_id = a.batch_id) as fileCount
      from 72crm_task a left join 72crm_work_task_class b on a.class_id = b.class_id where a.work_id = #para(workId)
      and a.status != 3  and a.ishidden = 0 and is_archive = 0
      #if(classId == -1)
         and a.class_id is null
      #else
        and a.class_id = #para(classId)
      #end
      #if(userIds && userIds.size()>0)
        and (
        #for(i : userIds)
        #(for.index > 0 ? " or " : "")
        a.owner_user_id like concat('%,',#para(i),',%') or  a.main_user_id = #para(i)
        #end
          )
      #end
      #if(labelIds && labelIds.size()>0)
        and (
        #for(i : labelIds)
          #(for.index > 0 ? " or " : "")
            a.label_id like  concat('%,',#para(i),',%')
        #end
        )
      #end
      #if(stopTimeType)
        and
        case #para(stopTimeType)  when 1 then  date_format(a.stop_time,'%Y-%m-%d') = date_format(now(),'%Y-%m-%d')
                                  when 2 then  date_format(a.stop_time,'%Y-%m-%d') = date_format(DATE_ADD(NOW(),INTERVAL 1 DAY),'%Y-%m-%d')
                                  when 3 then  YEARWEEK(date_format(a.stop_time,'%Y-%m-%d')) = YEARWEEK(now())
                                  when 4 then  date_format(a.stop_time,'%Y%m') = DATE_FORMAT( CURDATE( ) , '%Y%m' )
                                  when 5 then  a.stop_time is null
                                  when 6 then  a.stop_time < now()
                                  when 7 then  date_format(a.update_time,'%Y-%m-%d') = date_format(now(),'%Y-%m-%d') end
      #end

      order by b.order_num
  #end
  #sql("workStatistics")
    select count(*) as allCount,
      count(status=1 or null) as unfinished,
      count(status=2 or null) as overdue,
      count(status=5 or null) as complete,
      count(is_archive=1 or null) as archive,
      ifnull(ROUND((count(status=5 or null)/count(*))*100,2),0) as completionRate,
      ifnull(ROUND((count(status=2 or null)/count(*))*100,2),0) as overdueRate
      from 72crm_task where 1 = 1 and ishidden = 0
      #if(workId)
        and work_id = #para(workId)
      #end
      #if(workIds)
        and work_id in (#para(workIds))
      #end
      #if(userId)
        and (main_user_id = #para(userId) or owner_user_id like concat('%,',#para(userId),',%'))  and (is_archive = 0 or (is_archive = 1 and status = 5))
      #end
      #if(mainUserId)
        and main_user_id = #para(mainUserId)
      #end
  #end

  #sql("queryRoleList")
  select role_id,role_name,remark from 72crm_admin_role where role_type in (5,6) and status = 1
  #end

  #sql ("queryOwnerRoleList")
  select a.user_id,b.realname,a.role_id,c.role_name,b.img
  from 72crm_work_user as a left join 72crm_admin_user as b on a.user_id = b.user_id
  left join 72crm_admin_role as c on a.role_id = c.role_id
  where a.work_id = ?
  #end
  #sql ("queryRealmByRoleId")
    select concat((select realm from `72crm_admin_menu` where menu_id = b.parent_id),':',b.realm) from `72crm_admin_role_menu` a left join `72crm_admin_menu` b on a.menu_id = b.menu_id where a.role_id = ? and b.menu_type = 3
  #end
  #sql("archList")
  select a.*,b.name as workName,(select count(*) from 72crm_task_comment where type_id = a.task_id and type = 1) as commentCount,
         (select count(*) from 72crm_task where pid = a.task_id and status = 5) as childWCCount,
         (select count(*) from 72crm_task where pid = a.task_id) as childAllCount,
         (select count(*) from 72crm_admin_file where batch_id = a.batch_id) as fileCount
  from 72crm_task a left join 72crm_work b on a.work_id = b.work_id where a.work_id = ? and a.is_archive = 1 and a.ishidden = 0
  #end
  #sql("queryTaskFileByWorkId")
    SELECT a.file_id,a.name, CONCAT(FLOOR(a.size/1000),'KB') as size,a.create_user_id,b.realname as create_user_name,a.create_time,a.file_path,a.file_type,a.batch_id
    FROM `72crm_admin_file` as a inner join `72crm_admin_user` as b on a.create_user_id = b.user_id
    where a.batch_id in (select batch_id from `72crm_task` where work_id = #para(workId) and ishidden = 0)
  #end
  #sql("leave")
    update `72crm_task` set main_user_id = null where work_id =  #para(workId) and main_user_id = #para(userId);
  #end
  #sql("leave1")
    update `72crm_task` set owner_user_id = replace(owner_user_id,concat(',',#para(userId),','),',') where work_id = #para(workId) and  owner_user_id like concat(',',#para(userId),',');
  #end
  #sql("getTaskOwnerOnWork")
    select user_id,img,realname from `72crm_admin_user` where user_id in (select main_user_id from `72crm_task` where ishidden = 0 and work_id in (?))
  #end

  #sql ("queryProjectUser")
  select user_id,realname from 72crm_admin_user as a left join 72crm_admin_user_role as b on a.user_id = b.user_id
  left join 72crm_admin_role_menu as c on b.role_id = c.role_id
  where d.menu_id = ?
  #end
#end
