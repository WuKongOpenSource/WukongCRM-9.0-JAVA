#namespace("bi.customer")
  #sql ("biCustomerCron")
  select a.user_id,count(b.customer_id) as customer_num
  from 72crm_admin_user as a left join 72crm_crm_customer as b on a.user_id = b.owner_user_id
  group by a.user_id
  #end

  #sql ("poolTable")
  select a.realname,b.name as deptName,
  (select count(type_id) from 72crm_crm_owner_record where DATE_FORMAT(create_time,#para(sqlDateFormat)) between #para(beginTime) and #para(finalTime) and type = 8 and pre_owner_user_id = a.user_id) as putInNum,
  (select count(type_id) from 72crm_crm_owner_record where DATE_FORMAT(create_time,#para(sqlDateFormat)) between #para(beginTime) and #para(finalTime) and type = 8 and post_owner_user_id = a.user_id) as receiveNum
  from 72crm_admin_user as a left join 72crm_admin_dept as b on a.dept_id = b.dept_id
  where user_id in (
    #for(i:ids)
        #(for.index > 0 ? "," : "") #para(i)
    #end
  )
  #end

  #sql ("customerRecodCategoryStats")
  select category,IFNULL(count(record_id),0) as recordNum,
         IFNULL(count(record_id)*100/(select count(*) from 72crm_admin_record
                                      where DATE_FORMAT(create_time,#para(sqlDateFormat)) between #para(beginTime) and #para(finalTime) and types = 'crm_customer'
                                      and create_user_id in (
                                      #for(i:ids)
                                        #(for.index > 0 ? "," : "") #para(i)
                                      #end
                                      )),0) as proportion
  from 72crm_admin_record where types = 'crm_customer' and DATE_FORMAT(create_time,#para(sqlDateFormat)) between #para(beginTime) and #para(finalTime)
  and create_user_id in (
    #for(i:ids)
        #(for.index > 0 ? "," : "") #para(i)
    #end
  ) group by category
  #end

  #sql ("customerConversionInfo")
  select x.*,
  (select value from 72crm_admin_fieldv as a where name = '客户行业' and a.batch_id = x.batch_id) as '客户行业',
  (select value from 72crm_admin_fieldv as b where name = '客户来源' and b.batch_id = x.batch_id) as '客户来源'
  from (select a.batch_id,a.customer_name,b.name as contract_name,b.money as contract_money,
               IFNULL(c.money,0) as receivables_money,d.realname AS owner_user_name,
               e.realname AS create_user_name,a.create_time,a.update_time,b.order_date
        from 72crm_crm_customer as a inner join 72crm_crm_contract as b on a.customer_id = b.customer_id
        left join 72crm_crm_receivables as c on b.contract_id = c.contract_id
        left join 72crm_admin_user as d on a.owner_user_id = d.user_id
        left join 72crm_admin_user as e on a.create_user_id = e.user_id
        where DATE_FORMAT(a.create_time,#para(sqlDateFormat)) between #para(beginTime) and #para(finalTime)
              and b.check_status = 1 and a.owner_user_id in (
        #for(i:ids)
           #(for.index > 0 ? "," : "") #para(i)
        #end
        )
  ) as x
  #end
#end
