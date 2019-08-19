#namespace("bi.customer")
  #sql ("biCustomerCron")
  select a.user_id,count(b.customer_id) as customer_num
  from 72crm_admin_user as a left join 72crm_crm_customer as b on a.user_id = b.owner_user_id
  group by a.user_id
  #end

  #sql ("poolTable")
  select a.realname,b.name as deptName,
  (select count(type_id) from 72crm_crm_owner_record where DATE_FORMAT(create_time,#para(sqlDateFormat)) between #para(beginTime) and #para(finalTime) and type = 8 and pre_owner_user_id = a.user_id) as putInNum,
  (select count(type_id) from 72crm_crm_owner_record where DATE_FORMAT(create_time,#para(sqlDateFormat)) between #para(beginTime) and #para(finalTime) and type = 8 and post_owner_user_id = a.user_id) as receiveNum,
  IFNULL((select c.customer_num from 72crm_crm_customer_stats as c where DATE_FORMAT(create_time,#para(sqlDateFormat)) = #para(beginTime) and c.user_id = a.user_id limit 1),0) as customerNum
  from 72crm_admin_user as a left join 72crm_admin_dept as b on a.dept_id = b.dept_id
  where b.dept_id = #para(deptId)
    #if(userId)
    and a.user_id = #para(userId)
    #end
  #end
#end
