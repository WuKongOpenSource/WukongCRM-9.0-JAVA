#namespace("bi.ranking")
  #sql ("contractRanKing")
      SELECT IFNULL(SUM(cct.money), 0) as money,cau.realname,cct.owner_user_id,cad.name as structureName
      FROM
        72crm_crm_contract as cct
      LEFT JOIN 72crm_admin_user as cau on cau.user_id = cct.owner_user_id
       left join 72crm_admin_dept as cad on cad.dept_id = cau.dept_id
      WHERE
        check_status = 1
        and  cct.owner_user_id in (
         #for(i : userIds)
            #(for.index > 0 ? "," : "")#para(i)
         #end
         )
        #if(type == 1)
          and to_days(NOW()) = TO_DAYS(order_date)
          #end
           #if(type == 2)
          and to_days(NOW()) - TO_DAYS(order_date) = 1
          #end
           #if(type == 3)
          and YEARWEEK(date_format(order_date,'%Y-%m-%d')) = YEARWEEK(now())
          #end
           #if(type == 4)
          and YEARWEEK(date_format(order_date,'%Y-%m-%d')) = YEARWEEK(now()) -1
          #end
           #if(type == 5)
          and date_format(order_date,'%Y-%m')=date_format(now(),'%Y-%m')
          #end
           #if(type == 6)
          and date_format(order_date,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m')
          #end
           #if(type == 7)
          and QUARTER(order_date)=QUARTER(now()) AND YEAR(order_date)=YEAR(NOW())
          #end
           #if(type == 8)
          and QUARTER(order_date)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(order_date,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER))
          #end
           #if(type == 9)
          and YEAR(order_date)=YEAR(NOW())
          #end
           #if(type == 10)
          and YEAR(order_date)=YEAR(date_sub(now(),interval 1 year))
          #end
           #if(type == 11)
            and  TO_DAYS(order_date) >= TO_DAYS(#para(startTime))
            and  TO_DAYS(order_date) <= TO_DAYS(#para(endTime))
          #end
        GROUP BY cct.owner_user_id
        ORDER BY IFNULL(SUM(cct.money), 0) DESC
  #end
  #sql ("receivablesRanKing")
      SELECT IFNULL(SUM(cct.money), 0) as money,cau.realname,cad.name as structureName
      FROM
        72crm_crm_receivables as cct
      LEFT JOIN 72crm_admin_user as cau on cau.user_id = cct.owner_user_id
      left join 72crm_admin_dept as cad on cad.dept_id = cau.dept_id
      WHERE
        check_status = 1
        and  cct.owner_user_id in (
         #for(i : userIds)
            #(for.index > 0 ? "," : "")#para(i)
         #end
         )
        #if(type == 1)
          and to_days(NOW()) = TO_DAYS(return_time)
          #end
           #if(type == 2)
          and to_days(NOW()) - TO_DAYS(return_time) = 1
          #end
           #if(type == 3)
          and YEARWEEK(date_format(return_time,'%Y-%m-%d')) = YEARWEEK(now())
          #end
           #if(type == 4)
          and YEARWEEK(date_format(return_time,'%Y-%m-%d')) = YEARWEEK(now()) -1
          #end
           #if(type == 5)
          and date_format(return_time,'%Y-%m')=date_format(now(),'%Y-%m')
          #end
           #if(type == 6)
          and date_format(return_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m')
          #end
           #if(type == 7)
          and QUARTER(return_time)=QUARTER(now()) AND YEAR(return_time)=YEAR(NOW())
          #end
           #if(type == 8)
          and QUARTER(return_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(return_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER))
          #end
           #if(type == 9)
          and YEAR(return_time)=YEAR(NOW())
          #end
           #if(type == 10)
          and YEAR(return_time)=YEAR(date_sub(now(),interval 1 year))
          #end
           #if(type == 11)
            and  TO_DAYS(return_time) >= TO_DAYS(#para(startTime))
            and  TO_DAYS(return_time) <= TO_DAYS(#para(endTime))
          #end
        GROUP BY cct.owner_user_id
        ORDER BY IFNULL(SUM(cct.money), 0) DESC
  #end
  #sql ("contractCountRanKing")
      SELECT count(1) as `count`,cau.realname,cct.owner_user_id,cad.name as structureName
      FROM
        72crm_crm_contract as cct
      LEFT JOIN 72crm_admin_user as cau on cau.user_id = cct.owner_user_id
       left join 72crm_admin_dept as cad on cad.dept_id = cau.dept_id
      WHERE
        check_status = 1
        and  cct.owner_user_id in (
         #for(i : userIds)
            #(for.index > 0 ? "," : "")#para(i)
         #end
         )
        #if(type == 1)
          and to_days(NOW()) = TO_DAYS(order_date)
          #end
           #if(type == 2)
          and to_days(NOW()) - TO_DAYS(order_date) = 1
          #end
           #if(type == 3)
          and YEARWEEK(date_format(order_date,'%Y-%m-%d')) = YEARWEEK(now())
          #end
           #if(type == 4)
          and YEARWEEK(date_format(order_date,'%Y-%m-%d')) = YEARWEEK(now()) -1
          #end
           #if(type == 5)
          and date_format(order_date,'%Y-%m')=date_format(now(),'%Y-%m')
          #end
           #if(type == 6)
          and date_format(order_date,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m')
          #end
           #if(type == 7)
          and QUARTER(order_date)=QUARTER(now()) AND YEAR(order_date)=YEAR(NOW())
          #end
           #if(type == 8)
          and QUARTER(order_date)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(order_date,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER))
          #end
           #if(type == 9)
          and YEAR(order_date)=YEAR(NOW())
          #end
           #if(type == 10)
          and YEAR(order_date)=YEAR(date_sub(now(),interval 1 year))
          #end
           #if(type == 11)
            and  TO_DAYS(order_date) >= TO_DAYS(#para(startTime))
            and  TO_DAYS(order_date) <= TO_DAYS(#para(endTime))
          #end
        GROUP BY cct.owner_user_id
        ORDER BY count(1) DESC
  #end
   #sql ("productCountRanKing")
     SELECT count(1) as `count`,cau.realname,cct.owner_user_id,cad.name as structureName
      FROM
        72crm_crm_contract_product as ccp
      LEFT JOIN 72crm_crm_contract as cct on ccp.contract_id = cct.contract_id
      LEFT JOIN 72crm_admin_user as cau on cau.user_id = cct.owner_user_id
       left join 72crm_admin_dept as cad on cad.dept_id = cau.dept_id
      WHERE
        check_status = 1
        and  cct.owner_user_id in (
         #for(i : userIds)
            #(for.index > 0 ? "," : "")#para(i)
         #end
         )
        #if(type == 1)
          and to_days(NOW()) = TO_DAYS(order_date)
          #end
           #if(type == 2)
          and to_days(NOW()) - TO_DAYS(order_date) = 1
          #end
           #if(type == 3)
          and YEARWEEK(date_format(order_date,'%Y-%m-%d')) = YEARWEEK(now())
          #end
           #if(type == 4)
          and YEARWEEK(date_format(order_date,'%Y-%m-%d')) = YEARWEEK(now()) -1
          #end
           #if(type == 5)
          and date_format(order_date,'%Y-%m')=date_format(now(),'%Y-%m')
          #end
           #if(type == 6)
          and date_format(order_date,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m')
          #end
           #if(type == 7)
          and QUARTER(order_date)=QUARTER(now()) AND YEAR(order_date)=YEAR(NOW())
          #end
           #if(type == 8)
          and QUARTER(order_date)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(order_date,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER))
          #end
           #if(type == 9)
          and YEAR(order_date)=YEAR(NOW())
          #end
           #if(type == 10)
          and YEAR(order_date)=YEAR(date_sub(now(),interval 1 year))
          #end
           #if(type == 11)
            and  TO_DAYS(order_date) >= TO_DAYS(#para(startTime))
            and  TO_DAYS(order_date) <= TO_DAYS(#para(endTime))
          #end
        GROUP BY cct.owner_user_id
        ORDER BY count(1) DESC
  #end
  #sql ("customerCountRanKing")
      SELECT count(1) as `count`,cau.realname,cct.create_user_id,cad.name as structureName
      FROM
        72crm_crm_customer as cct
      LEFT JOIN 72crm_admin_user as cau on cau.user_id = cct.create_user_id
       left join 72crm_admin_dept as cad on cad.dept_id = cau.dept_id
      WHERE
         cct.create_user_id in (
         #for(i : userIds)
            #(for.index > 0 ? "," : "")#para(i)
         #end
         )
        #if(type == 1)
          and to_days(NOW()) = TO_DAYS(cct.create_time)
          #end
           #if(type == 2)
          and to_days(NOW()) - TO_DAYS(cct.create_time) = 1
          #end
           #if(type == 3)
          and YEARWEEK(date_format(cct.create_time,'%Y-%m-%d')) = YEARWEEK(now())
          #end
           #if(type == 4)
          and YEARWEEK(date_format(cct.create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1
          #end
           #if(type == 5)
          and date_format(cct.create_time,'%Y-%m')=date_format(now(),'%Y-%m')
          #end
           #if(type == 6)
          and date_format(cct.create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m')
          #end
           #if(type == 7)
          and QUARTER(cct.create_time)=QUARTER(now()) AND YEAR(cct.create_time)=YEAR(NOW())
          #end
           #if(type == 8)
          and QUARTER(cct.create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(cct.create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER))
          #end
           #if(type == 9)
          and YEAR(cct.create_time)=YEAR(NOW())
          #end
           #if(type == 10)
          and YEAR(cct.create_time)=YEAR(date_sub(now(),interval 1 year))
          #end
           #if(type == 11)
            and  TO_DAYS(cct.create_time) >= TO_DAYS(#para(startTime))
            and  TO_DAYS(cct.create_time) <= TO_DAYS(#para(endTime))
          #end
        GROUP BY cct.create_user_id
        ORDER BY count(1) DESC
  #end
  #sql ("contactsCountRanKing")
      SELECT count(1) as `count`,cau.realname,cct.create_user_id,cad.name as structureName
      FROM
        72crm_crm_contacts as cct
      LEFT JOIN 72crm_admin_user as cau on cau.user_id = cct.create_user_id
       left join 72crm_admin_dept as cad on cad.dept_id = cau.dept_id
      WHERE
         cct.create_user_id in (
         #for(i : userIds)
            #(for.index > 0 ? "," : "")#para(i)
         #end
         )
        #if(type == 1)
          and to_days(NOW()) = TO_DAYS(cct.create_time)
          #end
           #if(type == 2)
          and to_days(NOW()) - TO_DAYS(cct.create_time) = 1
          #end
           #if(type == 3)
          and YEARWEEK(date_format(cct.create_time,'%Y-%m-%d')) = YEARWEEK(now())
          #end
           #if(type == 4)
          and YEARWEEK(date_format(cct.create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1
          #end
           #if(type == 5)
          and date_format(cct.create_time,'%Y-%m')=date_format(now(),'%Y-%m')
          #end
           #if(type == 6)
          and date_format(cct.create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m')
          #end
           #if(type == 7)
          and QUARTER(cct.create_time)=QUARTER(now()) AND YEAR(cct.create_time)=YEAR(NOW())
          #end
           #if(type == 8)
          and QUARTER(cct.create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(cct.create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER))
          #end
           #if(type == 9)
          and YEAR(cct.create_time)=YEAR(NOW())
          #end
           #if(type == 10)
          and YEAR(cct.create_time)=YEAR(date_sub(now(),interval 1 year))
          #end
           #if(type == 11)
            and  TO_DAYS(cct.create_time) >= TO_DAYS(#para(startTime))
            and  TO_DAYS(cct.create_time) <= TO_DAYS(#para(endTime))
          #end
        GROUP BY cct.create_user_id
        ORDER BY count(1) DESC
  #end
  #sql ("customerGenjinCountRanKing")
      SELECT count(a.record_id) as `count`,cau.realname,cct.owner_user_id,cad.name as structureName
      FROM 72crm_admin_user as cau
      LEFT JOIN(select owner_user_id,customer_id from 72crm_crm_customer where
        #if(type == 1)
          to_days(NOW()) = TO_DAYS(update_time)
          #end
           #if(type == 2)
          to_days(NOW()) - TO_DAYS(update_time) = 1
          #end
           #if(type == 3)
          YEARWEEK(date_format(update_time,'%Y-%m-%d')) = YEARWEEK(now())
          #end
           #if(type == 4)
          YEARWEEK(date_format(update_time,'%Y-%m-%d')) = YEARWEEK(now()) -1
          #end
           #if(type == 5)
          date_format(update_time,'%Y-%m')=date_format(now(),'%Y-%m')
          #end
           #if(type == 6)
          date_format(update_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m')
          #end
           #if(type == 7)
          QUARTER(update_time)=QUARTER(now()) AND YEAR(cct.update_time)=YEAR(NOW())
          #end
           #if(type == 8)
          QUARTER(update_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(update_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER))
          #end
           #if(type == 9)
          YEAR(update_time)=YEAR(NOW())
          #end
           #if(type == 10)
          YEAR(update_time)=YEAR(date_sub(now(),interval 1 year))
          #end
           #if(type == 11)
            TO_DAYS(update_time) >= TO_DAYS(#para(startTime))
            TO_DAYS(update_time) <= TO_DAYS(#para(endTime))
          #end
       ) as cct on cau.user_id = cct.owner_user_id
       left join 72crm_admin_dept as cad on cad.dept_id = cau.dept_id
       left join 72crm_admin_record as a on cct.customer_id = a.types_id and a.types = 'crm_customer'
      WHERE
         cau.user_id in (
         #for(i : userIds)
            #(for.index > 0 ? "," : "")#para(i)
         #end
         )
        GROUP BY cau.user_id
        ORDER BY count(a.record_id) DESC
  #end
  #sql ("recordCountRanKing")
      SELECT count(1) as `count`,cau.realname,ccr.create_user_id,cad.name as structureName
      FROM
        72crm_admin_record as ccr
      LEFT JOIN 72crm_admin_user as cau on cau.user_id = ccr.create_user_id
       left join 72crm_admin_dept as cad on cad.dept_id = cau.dept_id
      WHERE
         ccr.create_user_id in (
         #for(i : userIds)
            #(for.index > 0 ? "," : "")#para(i)
         #end
         )
        #if(type == 1)
          and to_days(NOW()) = TO_DAYS(ccr.create_time)
          #end
           #if(type == 2)
          and to_days(NOW()) - TO_DAYS(ccr.create_time) = 1
          #end
           #if(type == 3)
          and YEARWEEK(date_format(ccr.create_time,'%Y-%m-%d')) = YEARWEEK(now())
          #end
           #if(type == 4)
          and YEARWEEK(date_format(ccr.create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1
          #end
           #if(type == 5)
          and date_format(ccr.create_time,'%Y-%m')=date_format(now(),'%Y-%m')
          #end
           #if(type == 6)
          and date_format(ccr.create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m')
          #end
           #if(type == 7)
          and QUARTER(ccr.create_time)=QUARTER(now()) AND YEAR(ccr.create_time)=YEAR(NOW())
          #end
           #if(type == 8)
          and QUARTER(ccr.create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(ccr.create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER))
          #end
           #if(type == 9)
          and YEAR(ccr.create_time)=YEAR(NOW())
          #end
           #if(type == 10)
          and YEAR(ccr.create_time)=YEAR(date_sub(now(),interval 1 year))
          #end
           #if(type == 11)
            and  TO_DAYS(ccr.create_time) >= TO_DAYS(#para(startTime))
            and  TO_DAYS(ccr.create_time) <= TO_DAYS(#para(endTime))
          #end
        GROUP BY ccr.create_user_id
        ORDER BY count(1) DESC
  #end
   #sql ("contractProductRanKing")
     SELECT IFNULL(SUM(cccp.num),0) as num ,ccpc.`name` as categoryName,ccp.`name` as productName,ccpc.category_id , ccp.product_id
      FROM 72crm_crm_contract_product  as cccp
      LEFT JOIN 72crm_crm_contract as cct on cct.contract_id = cccp.contract_id
      LEFT JOIN 72crm_crm_product as ccp on ccp.product_id = cccp.product_id
      LEFT JOIN 72crm_crm_product_category as ccpc ON ccpc.category_id = ccp.category_id
      WHERE
        check_status = 1
        and  cct.owner_user_id in (
         #for(i : userIds)
            #(for.index > 0 ? "," : "")#para(i)
         #end
         )
        #if(type == 1)
          and to_days(NOW()) = TO_DAYS(cct.order_date)
          #end
           #if(type == 2)
          and to_days(NOW()) - TO_DAYS(cct.order_date) = 1
          #end
           #if(type == 3)
          and YEARWEEK(date_format(cct.order_date,'%Y-%m-%d')) = YEARWEEK(now())
          #end
           #if(type == 4)
          and YEARWEEK(date_format(cct.order_date,'%Y-%m-%d')) = YEARWEEK(now()) -1
          #end
           #if(type == 5)
          and date_format(cct.order_date,'%Y-%m')=date_format(now(),'%Y-%m')
          #end
           #if(type == 6)
          and date_format(cct.order_date,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m')
          #end
           #if(type == 7)
          and QUARTER(cct.order_date)=QUARTER(now()) AND YEAR(cct.order_date)=YEAR(NOW())
          #end
           #if(type == 8)
          and QUARTER(cct.order_date)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(cct.order_date,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER))
          #end
           #if(type == 9)
          and YEAR(cct.order_date)=YEAR(NOW())
          #end
           #if(type == 10)
          and YEAR(cct.order_date)=YEAR(date_sub(now(),interval 1 year))
          #end
           #if(type == 11)
            and  TO_DAYS(cct.order_date) >= TO_DAYS(#para(startTime))
            and  TO_DAYS(cct.order_date) <= TO_DAYS(#para(endTime))
          #end
        GROUP BY cccp.product_id
  #end
  #sql ("travelCountRanKing")
      SELECT count(1) as `count`,cau.realname,coe.create_user_id,cad.name as structureName
      FROM
        72crm_oa_examine_travel as coet
      LEFT JOIN 72crm_oa_examine as coe on coe.examine_id = coet.examine_id
      left join `72crm_oa_examine_category` a on coe.category_id = a.category_id
      LEFT JOIN 72crm_admin_user as cau on cau.user_id = coe.create_user_id
       left join 72crm_admin_dept as cad on cad.dept_id = cau.dept_id
      WHERE a.type = 3 and
         coe.create_user_id in (
         #for(i : userIds)
            #(for.index > 0 ? "," : "")#para(i)
         #end
         )
        #if(type == 1)
          and to_days(NOW()) = TO_DAYS(coet.start_time)
          #end
           #if(type == 2)
          and to_days(NOW()) - TO_DAYS(coet.start_time) = 1
          #end
           #if(type == 3)
          and YEARWEEK(date_format(coet.start_time,'%Y-%m-%d')) = YEARWEEK(now())
          #end
           #if(type == 4)
          and YEARWEEK(date_format(coet.start_time,'%Y-%m-%d')) = YEARWEEK(now()) -1
          #end
           #if(type == 5)
          and date_format(coet.start_time,'%Y-%m')=date_format(now(),'%Y-%m')
          #end
           #if(type == 6)
          and date_format(coet.start_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m')
          #end
           #if(type == 7)
          and QUARTER(coet.start_time)=QUARTER(now()) AND YEAR(coet.start_time)=YEAR(NOW())
          #end
           #if(type == 8)
          and QUARTER(coet.start_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(coet.start_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER))
          #end
           #if(type == 9)
          and YEAR(coet.start_time)=YEAR(NOW())
          #end
           #if(type == 10)
          and YEAR(coet.start_time)=YEAR(date_sub(now(),interval 1 year))
          #end
           #if(type == 11)
            and  TO_DAYS(coet.start_time) >= TO_DAYS(#para(startTime))
            and  TO_DAYS(coet.start_time) <= TO_DAYS(#para(endTime))
          #end
        GROUP BY coe.create_user_id
        ORDER BY count(1) DESC
  #end
  #sql("productSellRanKing")
    SELECT cct.contract_id,ccs.customer_id,ccp.product_id,ccpc.category_id,cct.`name` as contractName,
      ccpc.`name` as categoryName,ccs.customer_name as customerName,cccp.num as num,cccp.subtotal ,cccp.sales_price as salesPrice,
      cct.num as contractNum,cct.owner_user_id  ,cau.realname,cccp.unit,cccp.discount,cccp.price
      FROM 72crm_crm_contract as cct
      LEFT JOIN 72crm_crm_contract_product as cccp ON cccp.contract_id = cct.contract_id
      LEFT JOIN 72crm_crm_customer as ccs on ccs.customer_id = cct.customer_id
      LEFT JOIN 72crm_crm_product as ccp on ccp.product_id = cccp.product_id
      LEFT JOIN 72crm_crm_product_category as ccpc ON ccp.category_id = ccpc.category_id
      LEFT JOIN 72crm_admin_user as cau on cau.user_id = cct.owner_user_id
      where
          cct.check_status = 1
        and  cct.owner_user_id in (
         #for(i : userIds)
            #(for.index > 0 ? "," : "")#para(i)
         #end
         )
        #if(type == 1)
          and to_days(NOW()) = TO_DAYS(cct.order_date)
          #end
           #if(type == 2)
          and to_days(NOW()) - TO_DAYS(cct.order_date) = 1
          #end
           #if(type == 3)
          and YEARWEEK(date_format(cct.order_date,'%Y-%m-%d')) = YEARWEEK(now())
          #end
           #if(type == 4)
          and YEARWEEK(date_format(cct.order_date,'%Y-%m-%d')) = YEARWEEK(now()) -1
          #end
           #if(type == 5)
          and date_format(cct.order_date,'%Y-%m')=date_format(now(),'%Y-%m')
          #end
           #if(type == 6)
          and date_format(cct.order_date,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m')
          #end
           #if(type == 7)
          and QUARTER(cct.order_date)=QUARTER(now()) AND YEAR(cct.order_date)=YEAR(NOW())
          #end
           #if(type == 8)
          and QUARTER(cct.order_date)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(cct.order_date,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER))
          #end
           #if(type == 9)
          and YEAR(cct.order_date)=YEAR(NOW())
          #end
           #if(type == 10)
          and YEAR(cct.order_date)=YEAR(date_sub(now(),interval 1 year))
          #end
           #if(type == 11)
            and  TO_DAYS(cct.order_date) >= TO_DAYS(#para(startTime))
            and  TO_DAYS(cct.order_date) <= TO_DAYS(#para(endTime))
          #end
      #end
      #sql("addressAnalyse")
        SELECT  COUNT(1) as allCustomer,
        ifnull((SELECT COUNT(1) FROM 72crm_crm_customer where deal_status = 0 and left(address,INSTR(address, ',') - 1) = left(ccc.address,INSTR(ccc.address, ',') - 1)) ,0)as dealCustomer,
        #para(address) as address
        FROM 72crm_crm_customer as ccc
        where  left(ccc.address,INSTR(ccc.address, ',') - 1) like concat('%',#para(address),'%')
      #end
       #sql("portrait")
        SELECT count(*) as allCustomer,
        sum(a.deal_status) as dealCustomer,
        (case when (IFNULL(b.`value`,'') = '') then '未知' else b.`value` end) as industry
        FROM 72crm_crm_customer as a
        LEFT JOIN 72crm_admin_fieldv as b on a.batch_id=b.batch_id and b.`name`='客户行业'
         where   a.owner_user_id in (
         #for(i : userIds)
            #(for.index > 0 ? "," : "")#para(i)
         #end
         )
          #if(type == 1)
          and to_days(NOW()) = TO_DAYS(a.create_time)
          #end
           #if(type == 2)
          and to_days(NOW()) - TO_DAYS(a.create_time) = 1
          #end
           #if(type == 3)
          and YEARWEEK(date_format(a.create_time,'%Y-%m-%d')) = YEARWEEK(now())
          #end
           #if(type == 4)
          and YEARWEEK(date_format(a.create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1
          #end
           #if(type == 5)
          and date_format(a.create_time,'%Y-%m')=date_format(now(),'%Y-%m')
          #end
           #if(type == 6)
          and date_format(a.create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m')
          #end
           #if(type == 7)
          and QUARTER(a.create_time)=QUARTER(now()) AND YEAR(a.create_time)=YEAR(NOW())
          #end
           #if(type == 8)
          and QUARTER(a.create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(a.create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER))
          #end
           #if(type == 9)
          and YEAR(a.create_time)=YEAR(NOW())
          #end
           #if(type == 10)
          and YEAR(a.create_time)=YEAR(date_sub(now(),interval 1 year))
          #end
           #if(type == 11)
            and  TO_DAYS(a.create_time) >= TO_DAYS(#para(startTime))
            and  TO_DAYS(a.create_time) <= TO_DAYS(#para(endTime))
          #end
        GROUP BY IFNULL(b.value,'')
       #end
       #sql("portraitLevel")
         SELECT count(*) as allCustomer,
        sum(a.deal_status) as dealCustomer,
        (case when (IFNULL(b.`value`,'') = '') then '未知' else b.`value` end) as level
        FROM 72crm_crm_customer as a
        LEFT JOIN 72crm_admin_fieldv as b on a.batch_id=b.batch_id and b.`name`='客户级别'
        where   a.owner_user_id in (
         #for(i : userIds)
            #(for.index > 0 ? "," : "")#para(i)
         #end
         )
          #if(type == 1)
          and to_days(NOW()) = TO_DAYS(a.create_time)
          #end
           #if(type == 2)
          and to_days(NOW()) - TO_DAYS(a.create_time) = 1
          #end
           #if(type == 3)
          and YEARWEEK(date_format(a.create_time,'%Y-%m-%d')) = YEARWEEK(now())
          #end
           #if(type == 4)
          and YEARWEEK(date_format(a.create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1
          #end
           #if(type == 5)
          and date_format(a.create_time,'%Y-%m')=date_format(now(),'%Y-%m')
          #end
           #if(type == 6)
          and date_format(a.create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m')
          #end
           #if(type == 7)
          and QUARTER(a.create_time)=QUARTER(now()) AND YEAR(a.create_time)=YEAR(NOW())
          #end
           #if(type == 8)
          and QUARTER(a.create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(a.create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER))
          #end
           #if(type == 9)
          and YEAR(a.create_time)=YEAR(NOW())
          #end
           #if(type == 10)
          and YEAR(a.create_time)=YEAR(date_sub(now(),interval 1 year))
          #end
           #if(type == 11)
            and  TO_DAYS(a.create_time) >= TO_DAYS(#para(startTime))
            and  TO_DAYS(a.create_time) <= TO_DAYS(#para(endTime))
          #end
        GROUP BY IFNULL(b.value,'')
       #end
       #sql("portraitSource")
        SELECT count(*) as allCustomer,
        sum(a.deal_status) as dealCustomer,
        (case when (IFNULL(b.`value`,'') = '') then '未知' else b.`value` end) as source
        FROM 72crm_crm_customer as a
        LEFT JOIN 72crm_admin_fieldv as b on a.batch_id=b.batch_id and b.`name`='客户来源'
          where   a.owner_user_id in (
         #for(i : userIds)
            #(for.index > 0 ? "," : "")#para(i)
         #end
         )
          #if(type == 1)
          and to_days(NOW()) = TO_DAYS(a.create_time)
          #end
           #if(type == 2)
          and to_days(NOW()) - TO_DAYS(a.create_time) = 1
          #end
           #if(type == 3)
          and YEARWEEK(date_format(a.create_time,'%Y-%m-%d')) = YEARWEEK(now())
          #end
           #if(type == 4)
          and YEARWEEK(date_format(a.create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1
          #end
           #if(type == 5)
          and date_format(a.create_time,'%Y-%m')=date_format(now(),'%Y-%m')
          #end
           #if(type == 6)
          and date_format(a.create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m')
          #end
           #if(type == 7)
          and QUARTER(a.create_time)=QUARTER(now()) AND YEAR(a.create_time)=YEAR(NOW())
          #end
           #if(type == 8)
          and QUARTER(a.create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(a.create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER))
          #end
           #if(type == 9)
          and YEAR(a.create_time)=YEAR(NOW())
          #end
           #if(type == 10)
          and YEAR(a.create_time)=YEAR(date_sub(now(),interval 1 year))
          #end
           #if(type == 11)
            and  TO_DAYS(a.create_time) >= TO_DAYS(#para(startTime))
            and  TO_DAYS(a.create_time) <= TO_DAYS(#para(endTime))
          #end
        GROUP BY IFNULL(b.value,'')
       #end
#end
