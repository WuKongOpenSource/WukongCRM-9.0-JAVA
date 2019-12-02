#namespace("bi.base")
  #sql ("queryCrmBusinessStatistics")
    SELECT DISTINCT (SELECT COUNT(*) from 72crm_crm_business WHERE scb.status_id = status_id) as  businessNum,
    scb.status_id,IFNULL((SELECT sum(money)  from 72crm_crm_business WHERE scb.status_id = status_id),0)as  total_price,
    scbs.`name`
    from 72crm_crm_business as scb
    LEFT JOIN 72crm_crm_business_product as scbp on scbp.business_id = scb.business_id
    LEFT JOIN 72crm_crm_business_status as scbs on scbs.status_id = scb.status_id
    LEFT JOIN 72crm_admin_user as sau on sau.user_id = scb.owner_user_id
    where  1 = 1
    #if(ownerUserId)
      and scb.owner_user_id = #para(ownerUserId)
    #end
    #if(productId)
      and scbp.product_id = #para(productId)
    #end
    #if(deptId)
      and sau.dept_id = #para(deptId)
    #end
    #if(startTime)
      and unix_timestamp(#para(startTime)) - unix_timestamp(scb.create_time) < 0
    #end
    #if(endTime)
      and unix_timestamp(#para(endTime)) - unix_timestamp(scb.create_time) > 0
    #end
      group by status_id
  #end
  #sql ("queryByUserIdOrYear")
      select co.* from contractview as co LEFT JOIN 72crm_admin_user as sau on co.owner_user_id = sau.user_id
      where 1 = 1
      #if(year)
        and YEAR(co.create_time) = #para(year)
      #end
      #if(month)
        and month(co.create_time) = #para(month)
      #end
      #if(userId)
        and sau.owner_user_id = #para(userId)
      #end
      #if(deptId)
        and sau.dept_id = #para(deptId)
      #end
  #end
  #sql ("queryProductSell")
      SELECT scp.product_id,scpc.name as categoryName , scpc.category_id,
      scp.name as productName, scc.num as contracNum ,
      sau.realname as ownerUserName,sccu.customer_name as customerName ,
      sccp.sales_price as productPrice ,sccp.num as productNum,
      sccp.subtotal as productSubtotal,
      scc.contract_id,
      sccu.customer_id
      FROM 72crm_crm_product as scp
      LEFT JOIN 72crm_crm_contract_product as sccp on sccp.product_id = scp.product_id
      LEFT JOIN 72crm_crm_contract as scc on scc.contract_id = sccp.contract_id
      LEFT JOIN 72crm_admin_user as sau on sau.user_id = scc.owner_user_id
      LEFT JOIN 72crm_crm_customer as sccu on sccu.customer_id = scc.customer_id
		  LEFT JOIN 72crm_crm_product_category as scpc on scpc.category_id = scp.category_id
      where 1 = 1 and scc.check_status != 6
      #if(startTime)
        and unix_timestamp(#para(startTime)) - unix_timestamp(scc.order_date) < 0
      #end
      #if(endTime)
        and unix_timestamp(#para(endTime)) - unix_timestamp(scc.order_date) > 0
      #end
      #if(userId)
        and sau.user_id = #para(userId)
      #end
      #if(deptId && userId==null )
        and sau.dept_id = #para(deptId)
      #end
   #end
  #sql("salesTrend")
    select #para(beginTime) as type,IFNULL(SUM(money),0) as contractMoneys, (SELECT IFNULL(SUM(money),0) FROM 72crm_crm_receivables WHERE DATE_FORMAT( return_time,#para(sqlDateFormat) ) = #para(beginTime)  and check_status = 1 AND owner_user_id in (#for(x:userIds) #(for.index == 0 ? "" : ",") #para(x) #end)) as receivablesMoneys FROM 72crm_crm_contract as ccco where DATE_FORMAT(ccco.order_date,#para(sqlDateFormat))=#para(beginTime) and ccco.check_status = 1 AND owner_user_id in (#for(x:userIds) #(for.index == 0 ? "" : ",") #para(x) #end)
  #end
  #sql ("taskCompleteStatistics")
    #for(x : map)
    #(for.index == 0 ? "" : "union all")
      select '#(x.remark)'                                                              as month,
             IFNULL(a.#(x.name), 0)                                            as achievement,
             b.receivables,
             IFNULL(ROUND(b.receivables / IFNULL(a.#(x.name), 0) * 100, 2), 0) as rate
      from 72crm_crm_achievement as a,
       #if(type == 3)
          #if(status == 1)
          (select IFNULL(SUM(money), 0) as receivables
           from 72crm_crm_contract
           where owner_user_id = #para(userId)
           and DATE_FORMAT(order_date, '%Y%m') = CONCAT('#(year)', '#(x.value)')
           and check_status = 1) as b
          #else if(status == 2)
          (select IFNULL(SUM(money), 0) as receivables
          from 72crm_crm_receivables
          where owner_user_id = #para(userId)
            and DATE_FORMAT(return_time, '%Y%m') = CONCAT('#(year)', '#(x.value)')
          and check_status = 1)       as b
          #end
      #else if(type == 2)
          #if(status == 1)
          (select IFNULL(SUM(a.money), 0) as receivables
             from 72crm_crm_contract as a
             inner join 72crm_admin_user as b
             where a.owner_user_id = b.user_id
             and b.dept_id = #para(deptId)
             and DATE_FORMAT(order_date, '%Y%m') = CONCAT('#(year)', '#(x.value)')
             and check_status = 1) as b
          #else if(status == 2)
            (select IFNULL(SUM(a.money), 0)  as receivables
            from 72crm_crm_receivables as a
                 inner join 72crm_admin_user as b
            where a.owner_user_id = b.user_id
              and b.dept_id = #para(deptId)
              and DATE_FORMAT(return_time, '%Y%m') = CONCAT('#(year)', '#(x.value)')
            and check_status = 1) as b
          #end
      #end
      where a.obj_id = #para(objId)
      and a.type = #para(type)
      and a.year = #(year)
      and a.status = #para(status)
    #end
  #end
#end
