#namespace("oa.announcement")
   #sql("queryById")
    select an.* , us.user_id,us.username,us.img,us.realname,us.parent_id ,LEFT(an.start_time,10) as startTime,LEFT(an.end_time,10) as endTime
    from 72crm_oa_announcement as an
    LEFT JOIN 72crm_admin_user as us on us.user_id = create_user_id
    where
    an.announcement_id = ?
   #end
   #sql("queryList")
    SELECT
      a.*,d.user_id,d.username,d.img,d.realname,d.parent_id ,LEFT(a.start_time,10) as startTime,LEFT(a.end_time,10) as endTime,(CASE WHEN FIND_IN_SET(b.user_id,read_user_ids)>0 THEN 1 ELSE 0 END) as is_read
    FROM
      72crm_oa_announcement AS a
    LEFT JOIN 72crm_admin_user as d on a.create_user_id=d.user_id
    LEFT JOIN 72crm_admin_user as b on FIND_IN_SET(b.user_id,a.owner_user_ids)
    LEFT JOIN 72crm_admin_dept as c on FIND_IN_SET(c.dept_id,a.dept_ids)
    WHERE 1=1
      and (c.dept_id=#para(deptId) or b.user_id=#para(userId) or (a.owner_user_ids ='' and a.dept_ids ='') or a.create_user_id=#para(userId))
    #if(type==1)
      and a.start_time < NOW() and a.end_time>NOW()
    #elseif(type==2)
      and (a.start_time > NOW() or a.end_time<NOW())
    #end
    order by a.announcement_id desc
   #end
#end
