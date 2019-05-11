#namespace("oa.announcement")
   #sql("queryById")
    select an.* , us.user_id,us.username,us.img,us.realname,us.parent_id ,LEFT(an.start_time,10) as startTime,LEFT(an.end_time,10) as endTime
    from 72crm_oa_announcement as an
    LEFT JOIN 72crm_admin_user as us on us.user_id = create_user_id
    where
    an.announcement_id = ?
   #end
   #sql("queryList")
    select an.* , us.user_id,us.username,us.img,us.realname,us.parent_id
    from 72crm_oa_announcement as an
    LEFT JOIN 72crm_admin_user as us on us.user_id = create_user_id
    order by an.announcement_id desc
   #end
   #sql("queryTotal")
    select count(*)
    from 72crm_oa_announcement as an
    LEFT JOIN 72crm_admin_user as us on us.user_id = create_user_id
   #end
#end
