#namespace("bi.work")
  #sql ("queryLogByUser")
    SELECT b.realname,b.username,b.img,b.user_id,(SELECT COUNT(1) FROM 72crm_task_comment WHERE type='2' and type_id=a.log_id) as sum,a.send_user_ids,a.read_user_ids
    FROM 72crm_oa_log as a LEFT JOIN 72crm_admin_user as b on a.create_user_id=b.user_id
    WHERE DATE_FORMAT(a.create_time,#para(sqlDateFormat)) between #para(beginTime) and #para(finalTime) and a.create_user_id =#para(userId)
  #end
  #sql ("queryExamineCategory")
    SELECT category_id,title FROM 72crm_oa_examine_category WHERE 1=1
  #end
  #sql ("queryExamineCount")
    SELECT IFNULL(SUM(money), 0) as money,IFNULL(SUM(duration), 0) as duration
    FROM 72crm_oa_examine as a LEFT JOIN 72crm_oa_examine_record as b on a.examine_id=b.examine_id and b.examine_status='1'
    WHERE a.category_id = #para(categoryId)
    and (a.create_time between #para(startTime) and  #para(endTime))
    and a.create_user_id = #para(userId)
  #end
  #sql ("examineStatistics")
    SELECT
      a.user_id,a.username,a.realname,a.img
      #for(x:categorys) ,COUNT(case when b.category_id=#para(x.category_id) then 1 end) as count_#(x.category_id) #end
    FROM
      72crm_admin_user AS a
    LEFT JOIN 72crm_oa_examine AS b ON a.user_id = b.create_user_id and (DATE_FORMAT(b.create_time,#para(sqlDateFormat)) between #para(beginTime) and #para(finalTime))
    where a.user_id in (
     #for(x:userList)
        #(for.index == 0 ? "" : ",")
        #para(x)
     #end
    )
    GROUP BY a.user_id
  #end
#end
