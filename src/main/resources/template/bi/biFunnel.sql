#namespace("bi.funnel")
    #sql ("sellFunnel")
      SELECT COUNT(ccb.status_id) as count,
      ccbs.`name`,
      ccbs.order_num,IFNULL(SUM(ccb.money),0) as money,
      ccb.type_id
      FROM 72crm_crm_business as ccb
      LEFT JOIN 72crm_crm_business_status as ccbs ON ccbs.status_id = ccb.status_id
      where
        ccbs.type_id = #para(typeId)
        and is_end = 0
        and  ccb.owner_user_id in (#for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)
           #if(type == 1)
          and to_days(NOW()) = TO_DAYS(ccb.create_time)
          #end
           #if(type == 2)
          and to_days(NOW()) - TO_DAYS(ccb.create_time) = 1
          #end
           #if(type == 3)
          and YEARWEEK(date_format(ccb.create_time,'%Y-%m-%d')) = YEARWEEK(now())
          #end
           #if(type == 4)
          and YEARWEEK(date_format(ccb.create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1
          #end
           #if(type == 5)
          and date_format(ccb.create_time,'%Y-%m')=date_format(now(),'%Y-%m')
          #end
           #if(type == 6)
          and date_format(ccb.create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m')
          #end
           #if(type == 7)
          and QUARTER(ccb.create_time)=QUARTER(now()) AND YEAR(ccb.create_time)=YEAR(NOW())
          #end
           #if(type == 8)
          and QUARTER(ccb.create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(ccb.create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER))
          #end
           #if(type == 9)
          and YEAR(ccb.create_time)=YEAR(NOW())
          #end
           #if(type == 10)
          and YEAR(ccb.create_time)=YEAR(date_sub(now(),interval 1 year))
          #end
           #if(type == 11)
            and  TO_DAYS(ccb.create_time) >= TO_DAYS(#para(startTime))
            and  TO_DAYS(ccb.create_time) <= TO_DAYS(#para(endTime))
          #end
          GROUP BY ccbs.status_id
    #end
    #sql ("sellFunnelSum")
      SELECT IFNULL(SUM(ccb.money),0) as money
      FROM 72crm_crm_business as ccb
      LEFT JOIN 72crm_crm_business_status as ccbs ON ccbs.status_id = ccb.status_id
      where
        ccbs.type_id = #para(typeId)
         and  ccb.owner_user_id in (#for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)
        #if(isEnd)
          and ccb.is_end = #para(isEnd)
        #end
           #if(type == 1)
          and to_days(NOW()) = TO_DAYS(ccb.create_time)
          #end
           #if(type == 2)
          and to_days(NOW()) - TO_DAYS(ccb.create_time) = 1
          #end
           #if(type == 3)
          and YEARWEEK(date_format(ccb.create_time,'%Y-%m-%d')) = YEARWEEK(now())
          #end
           #if(type == 4)
          and YEARWEEK(date_format(ccb.create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1
          #end
           #if(type == 5)
          and date_format(ccb.create_time,'%Y-%m')=date_format(now(),'%Y-%m')
          #end
           #if(type == 6)
          and date_format(ccb.create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m')
          #end
           #if(type == 7)
          and QUARTER(ccb.create_time)=QUARTER(now()) AND YEAR(ccb.create_time)=YEAR(NOW())
          #end
           #if(type == 8)
          and QUARTER(ccb.create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(ccb.create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER))
          #end
           #if(type == 9)
          and YEAR(ccb.create_time)=YEAR(NOW())
          #end
           #if(type == 10)
          and YEAR(ccb.create_time)=YEAR(date_sub(now(),interval 1 year))
          #end
           #if(type == 11)
            and  TO_DAYS(ccb.create_time) >= TO_DAYS(#para(startTime))
            and  TO_DAYS(ccb.create_time) <= TO_DAYS(#para(endTime))
          #end
          GROUP BY ccbs.`name`
    #end
    #sql("sellFunnelList")
        SELECT
         a.business_id,a.business_name,a.create_time,a.create_user_id,
        b.realname as create_user_name,a.customer_id,d.customer_name,
         a.deal_date,a.money,a.owner_user_id,c.realname as owner_user_name,
         a.status_id,
         CASE WHEN a.is_end = 0 THEN e.name
             WHEN a.is_end = 1 THEN '赢单'
             WHEN a.is_end = 2 THEN '输单'
             WHEN a.is_end = 3 THEN '无效' END as status_name,
         a.type_id,f.name as type_name
        FROM 72crm_crm_business  a
        left join 72crm_admin_user b on a.create_user_id = b.user_id
        left join 72crm_admin_user c on a.owner_user_id = c.user_id
        left join 72crm_crm_customer d on d.customer_id = a.customer_id
        left join 72crm_crm_business_status e on e.status_id = a.status_id
        left join 72crm_crm_business_type f on f.type_id = a.type_id
        where
        a.owner_user_id in (#for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)
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
    #end
#end
