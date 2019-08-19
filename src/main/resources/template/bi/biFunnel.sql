#namespace("bi.funnel")
    #sql ("sellFunnel")
      SELECT COUNT(1) as count,
      ccbs.`name`,
      ccbs.order_num,IFNULL(SUM(ccb.money),0) as money,
      ccb.type_id
      FROM 72crm_crm_business as ccb
      LEFT JOIN 72crm_crm_business_status as ccbs ON ccbs.status_id = ccb.status_id
      where
        ccbs.type_id = #para(typeId)
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
          GROUP BY ccbs.`name`,ccbs.order_num,ccb.type_id
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
         business_id,business_name,create_time,create_user_id,
        create_user_name,customer_id,customer_name,
        deal_date,money,owner_user_id,owner_user_name,
        status_id,status_name,type_id,type_name
        FROM businessview
        where
        owner_user_id in (#for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)
        #if(type == 1)
          and to_days(NOW()) = TO_DAYS(create_time)
          #end
           #if(type == 2)
          and to_days(NOW()) - TO_DAYS(create_time) = 1
          #end
           #if(type == 3)
          and YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now())
          #end
           #if(type == 4)
          and YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1
          #end
           #if(type == 5)
          and date_format(create_time,'%Y-%m')=date_format(now(),'%Y-%m')
          #end
           #if(type == 6)
          and date_format(create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m')
          #end
           #if(type == 7)
          and QUARTER(create_time)=QUARTER(now()) AND YEAR(create_time)=YEAR(NOW())
          #end
           #if(type == 8)
          and QUARTER(create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER))
          #end
           #if(type == 9)
          and YEAR(create_time)=YEAR(NOW())
          #end
           #if(type == 10)
          and YEAR(create_time)=YEAR(date_sub(now(),interval 1 year))
          #end
           #if(type == 11)
            and  TO_DAYS(create_time) >= TO_DAYS(#para(startTime))
            and  TO_DAYS(create_time) <= TO_DAYS(#para(endTime))
          #end
    #end
#end
