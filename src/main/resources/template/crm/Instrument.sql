#namespace("crm.Instrument")
   #sql("intraday")
       select distinct
      (SELECT COUNT(*) FROM 72crm_crm_business WHERE TO_DAYS(create_time) = TO_DAYS(now()) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as businessCount,
      (SELECT COUNT(*) FROM 72crm_crm_contacts WHERE TO_DAYS(create_time) = TO_DAYS(now()) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as contactsCount,
      (SELECT COUNT(*) FROM 72crm_crm_contract WHERE TO_DAYS(create_time) = TO_DAYS(now()) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as contractCount,
      (SELECT COUNT(*) FROM 72crm_crm_customer WHERE TO_DAYS(create_time) = TO_DAYS(now()) AND owner_user_id is not null AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as customerCount,
      (SELECT COUNT(*) FROM 72crm_crm_product WHERE TO_DAYS(create_time) = TO_DAYS(now()) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as productCount,
      (SELECT COUNT(*) FROM 72crm_crm_leads WHERE TO_DAYS(create_time) = TO_DAYS(now()) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as leadsCount,
      (SELECT COUNT(*) FROM 72crm_crm_receivables WHERE TO_DAYS(create_time) = TO_DAYS(now()) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as receivablesCount,
      (SELECT COUNT(*) FROM 72crm_admin_record WHERE TO_DAYS(create_time) = TO_DAYS(now()) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as recordCount,
      (SELECT COUNT(*) FROM 72crm_crm_business_change as a inner join 72crm_crm_business as b on a.business_id = b.business_id WHERE TO_DAYS(a.create_time) = TO_DAYS(now()) AND b.owner_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as recordStatusCount
      FROM 72crm_crm_customer
   #end
   #sql("yesterday")
      select distinct
      (SELECT COUNT(*) FROM 72crm_crm_business WHERE to_days(NOW()) - TO_DAYS(create_time) = 1 AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as businessCount,
      (SELECT COUNT(*) FROM 72crm_crm_contacts WHERE to_days(NOW()) - TO_DAYS(create_time) = 1 AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as contactsCount,
      (SELECT COUNT(*) FROM 72crm_crm_contract WHERE to_days(NOW()) - TO_DAYS(create_time) = 1 AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as contractCount,
      (SELECT COUNT(*) FROM 72crm_crm_customer WHERE to_days(NOW()) - TO_DAYS(create_time) = 1 AND owner_user_id is not null AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as customerCount,
      (SELECT COUNT(*) FROM 72crm_crm_product WHERE to_days(NOW()) - TO_DAYS(create_time) = 1 AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as productCount,
      (SELECT COUNT(*) FROM 72crm_crm_leads WHERE to_days(NOW()) - TO_DAYS(create_time) = 1 AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as leadsCount,
      (SELECT COUNT(*) FROM 72crm_crm_receivables WHERE to_days(NOW()) - TO_DAYS(create_time) = 1 AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as receivablesCount,
      (SELECT COUNT(*) FROM 72crm_admin_record WHERE to_days(NOW()) - TO_DAYS(create_time) = 1 AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as recordCount,
      (SELECT COUNT(*) FROM 72crm_crm_business_change as a inner join 72crm_crm_business as b on a.business_id = b.business_id WHERE to_days(NOW()) - TO_DAYS(a.create_time) = 1 AND b.owner_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as recordStatusCount
      FROM 72crm_crm_customer
   #end
   #sql("thisWeek")
       select distinct
      (SELECT COUNT(*) FROM 72crm_crm_business WHERE YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as businessCount,
      (SELECT COUNT(*) FROM 72crm_crm_contacts WHERE YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as contactsCount,
      (SELECT COUNT(*) FROM 72crm_crm_contract WHERE YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as contractCount,
      (SELECT COUNT(*) FROM 72crm_crm_customer WHERE YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) AND owner_user_id is not null AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as customerCount,
      (SELECT COUNT(*) FROM 72crm_crm_product WHERE YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as productCount,
      (SELECT COUNT(*) FROM 72crm_crm_leads WHERE YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as leadsCount,
      (SELECT COUNT(*) FROM 72crm_crm_receivables WHERE YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as receivablesCount,
      (SELECT COUNT(*) FROM 72crm_admin_record WHERE YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as recordCount,
      (SELECT COUNT(*) FROM 72crm_crm_business_change as a inner join 72crm_crm_business as b on a.business_id = b.business_id WHERE YEARWEEK(date_format(a.create_time,'%Y-%m-%d')) = YEARWEEK(now()) AND b.owner_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as recordStatusCount
      FROM 72crm_crm_customer
   #end
   #sql("lastWeek")
       select distinct
      (SELECT COUNT(*) FROM 72crm_crm_business WHERE YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1 AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as businessCount,
      (SELECT COUNT(*) FROM 72crm_crm_contacts WHERE YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1 AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as contactsCount,
      (SELECT COUNT(*) FROM 72crm_crm_contract WHERE YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1 AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as contractCount,
      (SELECT COUNT(*) FROM 72crm_crm_customer WHERE YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1 AND owner_user_id is not null AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as customerCount,
      (SELECT COUNT(*) FROM 72crm_crm_product WHERE YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1 AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as productCount,
      (SELECT COUNT(*) FROM 72crm_crm_leads WHERE YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1 AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as leadsCount,
      (SELECT COUNT(*) FROM 72crm_crm_receivables WHERE YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1 AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as receivablesCount,
      (SELECT COUNT(*) FROM 72crm_admin_record WHERE YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1 AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as recordCount,
      (SELECT COUNT(*) FROM 72crm_crm_business_change as a inner join 72crm_crm_business as b on a.business_id = b.business_id WHERE  YEARWEEK(date_format(a.create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1 AND b.owner_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as recordStatusCount
      FROM 72crm_crm_customer
   #end
   #sql("theSameMonth")
      select distinct
      (SELECT COUNT(*) FROM 72crm_crm_business WHERE date_format(create_time,'%Y-%m')=date_format(now(),'%Y-%m') AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as businessCount,
      (SELECT COUNT(*) FROM 72crm_crm_contacts WHERE date_format(create_time,'%Y-%m')=date_format(now(),'%Y-%m') AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as contactsCount,
      (SELECT COUNT(*) FROM 72crm_crm_contract WHERE date_format(create_time,'%Y-%m')=date_format(now(),'%Y-%m') AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as contractCount,
      (SELECT COUNT(*) FROM 72crm_crm_customer WHERE date_format(create_time,'%Y-%m')=date_format(now(),'%Y-%m') AND owner_user_id is not null AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as customerCount,
      (SELECT COUNT(*) FROM 72crm_crm_product WHERE date_format(create_time,'%Y-%m')=date_format(now(),'%Y-%m') AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as productCount,
      (SELECT COUNT(*) FROM 72crm_crm_leads WHERE date_format(create_time,'%Y-%m')=date_format(now(),'%Y-%m') AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as leadsCount,
      (SELECT COUNT(*) FROM 72crm_crm_receivables WHERE date_format(create_time,'%Y-%m')=date_format(now(),'%Y-%m') AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as receivablesCount,
      (SELECT COUNT(*) FROM 72crm_admin_record WHERE date_format(create_time,'%Y-%m')=date_format(now(),'%Y-%m') AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as recordCount,
      (SELECT COUNT(*) FROM 72crm_crm_business_change as a inner join 72crm_crm_business as b on a.business_id = b.business_id WHERE date_format(a.create_time,'%Y-%m')=date_format(now(),'%Y-%m') AND b.owner_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as recordStatusCount
      FROM 72crm_crm_customer
   #end
    #sql("lastMonth")
       select distinct
      (SELECT COUNT(*) FROM 72crm_crm_business WHERE date_format(create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as businessCount,
      (SELECT COUNT(*) FROM 72crm_crm_contacts WHERE date_format(create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as contactsCount,
      (SELECT COUNT(*) FROM 72crm_crm_contract WHERE date_format(create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as contractCount,
      (SELECT COUNT(*) FROM 72crm_crm_customer WHERE date_format(create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') AND owner_user_id is not null AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as customerCount,
      (SELECT COUNT(*) FROM 72crm_crm_product WHERE date_format(create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as productCount,
      (SELECT COUNT(*) FROM 72crm_crm_leads WHERE date_format(create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as leadsCount,
      (SELECT COUNT(*) FROM 72crm_crm_receivables WHERE date_format(create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as receivablesCount,
      (SELECT COUNT(*) FROM 72crm_admin_record WHERE date_format(create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as recordCount,
      (SELECT COUNT(*) FROM 72crm_crm_business_change as a inner join 72crm_crm_business as b on a.business_id = b.business_id WHERE date_format(a.create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') AND b.owner_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as recordStatusCount
      FROM 72crm_crm_customer
   #end
   #sql("currentSeason")
      select distinct
      (SELECT COUNT(*) FROM 72crm_crm_business WHERE QUARTER(create_time)=QUARTER(now()) AND YEAR(create_time)=YEAR(NOW()) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as businessCount,
      (SELECT COUNT(*) FROM 72crm_crm_contacts WHERE QUARTER(create_time)=QUARTER(now()) AND YEAR(create_time)=YEAR(NOW()) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as contactsCount,
      (SELECT COUNT(*) FROM 72crm_crm_contract WHERE QUARTER(create_time)=QUARTER(now()) AND YEAR(create_time)=YEAR(NOW()) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as contractCount,
      (SELECT COUNT(*) FROM 72crm_crm_customer WHERE QUARTER(create_time)=QUARTER(now()) AND YEAR(create_time)=YEAR(NOW()) AND owner_user_id is not null AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as customerCount,
      (SELECT COUNT(*) FROM 72crm_crm_product WHERE QUARTER(create_time)=QUARTER(now()) AND YEAR(create_time)=YEAR(NOW()) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as productCount,
      (SELECT COUNT(*) FROM 72crm_crm_leads WHERE QUARTER(create_time)=QUARTER(now()) AND YEAR(create_time)=YEAR(NOW()) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as leadsCount,
      (SELECT COUNT(*) FROM 72crm_crm_receivables WHERE QUARTER(create_time)=QUARTER(now()) AND YEAR(create_time)=YEAR(NOW()) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as receivablesCount,
      (SELECT COUNT(*) FROM 72crm_admin_record WHERE QUARTER(create_time)=QUARTER(now()) AND YEAR(create_time)=YEAR(NOW()) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as recordCount,
      (SELECT COUNT(*) FROM 72crm_crm_business_change as a inner join 72crm_crm_business as b on a.business_id = b.business_id WHERE  QUARTER(a.create_time)=QUARTER(now()) AND YEAR(a.create_time)=YEAR(NOW()) AND b.owner_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as recordStatusCount
      FROM 72crm_crm_customer
   #end
    #sql("precedingQuarter")
       select distinct
      (SELECT COUNT(*) FROM 72crm_crm_business WHERE QUARTER(create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as businessCount,
      (SELECT COUNT(*) FROM 72crm_crm_contacts WHERE QUARTER(create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as contactsCount,
      (SELECT COUNT(*) FROM 72crm_crm_contract WHERE QUARTER(create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as contractCount,
      (SELECT COUNT(*) FROM 72crm_crm_customer WHERE QUARTER(create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) AND owner_user_id is not null and YEAR(DATE_SUB(create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as customerCount,
      (SELECT COUNT(*) FROM 72crm_crm_product WHERE QUARTER(create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as productCount,
      (SELECT COUNT(*) FROM 72crm_crm_leads WHERE QUARTER(create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as leadsCount,
      (SELECT COUNT(*) FROM 72crm_crm_receivables WHERE QUARTER(create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as receivablesCount,
      (SELECT COUNT(*) FROM 72crm_admin_record WHERE QUARTER(create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as recordCount,
      (SELECT COUNT(*) FROM 72crm_crm_business_change as a inner join 72crm_crm_business as b on a.business_id = b.business_id WHERE  QUARTER(a.create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(a.create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) AND b.owner_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as recordStatusCount
      FROM 72crm_crm_customer
   #end
   #sql("thisYear")
     select distinct
      (SELECT COUNT(*) FROM 72crm_crm_business WHERE YEAR(create_time)=YEAR(NOW()) AND create_user_id in (
      #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as businessCount,
      (SELECT COUNT(*) FROM 72crm_crm_contacts WHERE YEAR(create_time)=YEAR(NOW()) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as contactsCount,
      (SELECT COUNT(*) FROM 72crm_crm_contract WHERE YEAR(create_time)=YEAR(NOW()) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as contractCount,
      (SELECT COUNT(*) FROM 72crm_crm_customer WHERE YEAR(create_time)=YEAR(NOW()) AND owner_user_id is not null AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as customerCount,
      (SELECT COUNT(*) FROM 72crm_crm_product WHERE YEAR(create_time)=YEAR(NOW()) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as productCount,
      (SELECT COUNT(*) FROM 72crm_crm_leads WHERE YEAR(create_time)=YEAR(NOW()) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as leadsCount,
      (SELECT COUNT(*) FROM 72crm_crm_receivables WHERE YEAR(create_time)=YEAR(NOW()) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as receivablesCount,
      (SELECT COUNT(*) FROM 72crm_admin_record WHERE YEAR(create_time)=YEAR(NOW()) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as recordCount,
      (SELECT COUNT(*) FROM 72crm_crm_business_change as a inner join 72crm_crm_business as b on a.business_id = b.business_id WHERE YEAR(a.create_time)=YEAR(NOW()) AND b.owner_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as recordStatusCount
      FROM 72crm_crm_customer
   #end
   #sql("lastYear")
       select distinct
      (SELECT COUNT(*) FROM 72crm_crm_business WHERE YEAR(create_time)=YEAR(date_sub(now(),interval 1 year)) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as businessCount,
      (SELECT COUNT(*) FROM 72crm_crm_contacts WHERE YEAR(create_time)=YEAR(date_sub(now(),interval 1 year)) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as contactsCount,
      (SELECT COUNT(*) FROM 72crm_crm_contract WHERE YEAR(create_time)=YEAR(date_sub(now(),interval 1 year)) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as contractCount,
      (SELECT COUNT(*) FROM 72crm_crm_customer WHERE YEAR(create_time)=YEAR(date_sub(now(),interval 1 year)) AND owner_user_id is not null AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as customerCount,
      (SELECT COUNT(*) FROM 72crm_crm_product WHERE YEAR(create_time)=YEAR(date_sub(now(),interval 1 year)) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as productCount,
      (SELECT COUNT(*) FROM 72crm_crm_leads WHERE YEAR(create_time)=YEAR(date_sub(now(),interval 1 year)) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as leadsCount,
      (SELECT COUNT(*) FROM 72crm_crm_receivables WHERE YEAR(create_time)=YEAR(date_sub(now(),interval 1 year)) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as receivablesCount,
      (SELECT COUNT(*) FROM 72crm_admin_record WHERE YEAR(create_time)=YEAR(date_sub(now(),interval 1 year)) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as recordCount,
      (SELECT COUNT(*) FROM 72crm_crm_business_change as a inner join 72crm_crm_business as b on a.business_id = b.business_id WHERE  YEAR(a.create_time)=YEAR(date_sub(now(),interval 1 year)) AND b.owner_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as recordStatusCount
      FROM 72crm_crm_customer
   #end
   #sql("custom")
       select distinct
      (SELECT COUNT(*) FROM 72crm_crm_business WHERE  TO_DAYS(create_time) >= TO_DAYS(#para(startTime)) and  TO_DAYS(create_time) <= TO_DAYS(#para(endTime)) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as businessCount,
      (SELECT COUNT(*) FROM 72crm_crm_contacts WHERE  TO_DAYS(create_time) >= TO_DAYS(#para(startTime)) and  TO_DAYS(create_time) <= TO_DAYS(#para(endTime)) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as contactsCount,
      (SELECT COUNT(*) FROM 72crm_crm_contract WHERE  TO_DAYS(create_time) >= TO_DAYS(#para(startTime)) and  TO_DAYS(create_time) <= TO_DAYS(#para(endTime)) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as contractCount,
      (SELECT COUNT(*) FROM 72crm_crm_customer WHERE  TO_DAYS(create_time) >= TO_DAYS(#para(startTime)) AND owner_user_id is not null and  TO_DAYS(create_time) <= TO_DAYS(#para(endTime)) AND create_user_id in (
      #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as customerCount,
      (SELECT COUNT(*) FROM 72crm_crm_product WHERE  TO_DAYS(create_time) >= TO_DAYS(#para(startTime)) and  TO_DAYS(create_time) <= TO_DAYS(#para(endTime)) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as productCount,
      (SELECT COUNT(*) FROM 72crm_crm_leads WHERE  TO_DAYS(create_time) >= TO_DAYS(#para(startTime)) and  TO_DAYS(create_time) <= TO_DAYS(#para(endTime)) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as leadsCount,
      (SELECT COUNT(*) FROM 72crm_crm_receivables WHERE  TO_DAYS(create_time) >= TO_DAYS(#para(startTime)) and  TO_DAYS(create_time) <= TO_DAYS(#para(endTime)) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as receivablesCount,
      (SELECT COUNT(*) FROM 72crm_admin_record WHERE  TO_DAYS(create_time) >= TO_DAYS(#para(startTime)) and  TO_DAYS(create_time) <= TO_DAYS(#para(endTime)) AND create_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as recordCount,
      (SELECT COUNT(*) FROM 72crm_crm_business_change as a inner join 72crm_crm_business as b on a.business_id = b.business_id WHERE  TO_DAYS(a.create_time) >= TO_DAYS(#para(startTime)) and  TO_DAYS(a.create_time) <= TO_DAYS(#para(endTime)) AND b.owner_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as recordStatusCount
      FROM 72crm_crm_customer
   #end
   #sql("sellMonth")
     SELECT distinct
    (SELECT IFNULL(SUM(money),0) FROM 72crm_crm_contract WHERE DATE_FORMAT( create_time, '%Y%m' ) = #para(timeYearMonth) AND create_user_id in (#for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as contractMoneys,
    (SELECT  IFNULL(SUM(money),0) FROM 72crm_crm_receivables WHERE DATE_FORMAT( create_time, '%Y%m' ) = #para(timeYearMonth) AND create_user_id in (#for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as receivablesMoneys
      FROM 72crm_crm_contract
   #end
   #sql("sellYears")
     SELECT distinct
    (SELECT IFNULL(SUM(money),0) FROM 72crm_crm_contract WHERE quarter(create_time) = #para(quarter)  and YEAR(create_time) = #para(year) AND create_user_id in (#for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as contractMoneys,
    (SELECT  IFNULL(SUM(money),0) FROM 72crm_crm_receivables WHERE quarter(create_time) = #para(quarter) and YEAR(create_time) = #para(year) AND create_user_id in (#for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as receivablesMoneys
     FROM 72crm_crm_contract
   #end
   #sql("queryMoneys")
     SELECT distinct
    (select IFNULL(SUM(money),0) FROM 72crm_crm_contract where check_status = 1 and DATE_FORMAT(order_date,'%Y%m') between  #para(startTime)  and #para(endTime) AND
     create_user_id in (
     #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as contractMoneys,
    (select IFNULL(SUM(money),0) FROM 72crm_crm_receivables where check_status = 1 and DATE_FORMAT(return_time,'%Y%m') between  #para(startTime)  and #para(endTime) AND
     create_user_id in (
     #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)) as receivablesMoneys
     FROM 72crm_crm_contract
   #end
   #sql("queryTarget")
     SELECT sum(january + february + march + april + may  + june + july + august  + september + october + november + december) as achievementTarget
      FROM 72crm_crm_achievement
       WHERE year = #para(year)
      #if(userIds)
       and type = 3 and obj_id in (#for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)
      #end
       #if(deptIds)
       and type = 2 and obj_id in (#for(x:deptIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)
      #end
       and status = #para(status)
   #end
   #sql("queryTargets")
     SELECT january , february , march , april , may  , june , july , august  , september , october , november , december
      FROM 72crm_crm_achievement
      WHERE year = #para(year)
      #if(userIds)
       and type = 3 and obj_id in (#for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)
      #end
       #if(deptIds)
       and type = 2 and obj_id in (#for(x:deptIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)
      #end
       and status = #para(status)
   #end
   #sql("queryBusinessStatistics")
     SELECT DISTINCT (SELECT COUNT(*) from 72crm_crm_business WHERE scb.status_id = status_id) as  businessNum,
      scb.status_id,(SELECT IFNULL(sum(money),0) from 72crm_crm_business WHERE scb.status_id = status_id)as  total_price,scbs.`name`
      from 72crm_crm_business as scb
      LEFT JOIN 72crm_crm_business_product as scbp on scbp.business_id = scb.business_id
      LEFT JOIN 72crm_crm_business_status as scbs on scbs.status_id = scb.status_id
      LEFT JOIN 72crm_admin_user as sau on sau.user_id = scb.owner_user_id
      LEFT JOIN 72crm_crm_business_type as scbt on scbt.type_id = scb.type_id
      where 1 = 1
        #if(userIds)
        and scb.owner_user_id in (#for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)
      #end
       #if(deptIds)
        and sau.dept_id in (#for(x:deptIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)
      #end
       #if(typeId)
        and scb.type_id = #para(typeId)
      #end
      #if(startTime)
        and unix_timestamp(#para(startTime)) - unix_timestamp(scb.create_time) < 0
      #end
      #if(endTime)
        and unix_timestamp(#para(endTime)) - unix_timestamp(scb.create_time) > 0
      #end
        group by status_id
   #end
   #sql("queryBusiness")
    SELECT DISTINCT IFNULL((
    SELECT sum(scb.money) from 72crm_crm_business as scb
    LEFT JOIN 72crm_crm_business_product as scbp on scbp.business_id = scb.business_id
      LEFT JOIN 72crm_crm_business_status as scbs on scbs.status_id = scb.status_id
      LEFT JOIN 72crm_admin_user as sau on sau.user_id = scb.owner_user_id
      LEFT JOIN 72crm_crm_business_type as scbt on scbt.type_id = scb.type_id
       WHERE scb.is_end = 1
          #if(userIds)
        and scb.owner_user_id in (#for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)
      #end
       #if(deptIds)
        and sau.dept_id in (#for(x:deptIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)
      #end
       #if(typeId)
        and scb.type_id = #para(typeId)
      #end
      #if(startTime)
        and unix_timestamp(#para(startTime)) - unix_timestamp(scb.create_time) < 0
      #end
      #if(endTime)
        and unix_timestamp(#para(endTime)) - unix_timestamp(scb.create_time) > 0
      #end),0)as  winSingle ,
        IFNULL((
          SELECT sum(scb.money) from 72crm_crm_business as scb
        LEFT JOIN 72crm_crm_business_product as scbp on scbp.business_id = scb.business_id
      LEFT JOIN 72crm_crm_business_status as scbs on scbs.status_id = scb.status_id
      LEFT JOIN 72crm_admin_user as sau on sau.user_id = scb.owner_user_id
      LEFT JOIN 72crm_crm_business_type as scbt on scbt.type_id = scb.type_id
       WHERE scb.is_end = 2
        #if(userIds)
        and scb.owner_user_id in (#for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)
      #end
       #if(deptIds)
        and sau.dept_id in (#for(x:deptIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)
      #end
       #if(typeId)
        and scb.type_id = #para(typeId)
      #end
      #if(startTime)
        and unix_timestamp(#para(startTime)) - unix_timestamp(scb.create_time) < 0
      #end
      #if(endTime)
        and unix_timestamp(#para(endTime)) - unix_timestamp(scb.create_time) > 0
      #end),0) as  loseSingle
    from 72crm_crm_business
   #end
    #sql("queryContractMoeny")
       SELECT IFNULL(SUM(money),0) as money
      FROM 72crm_crm_contract
      where check_status = 1
      and  owner_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)
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
    #end
    #sql("queryReceivablesMoeny")
       SELECT IFNULL(SUM(money),0) as money
      FROM 72crm_crm_receivables
      where check_status = 1
      and  owner_user_id in ( #for(x:userIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end)
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
    #end
    #sql ("queryRecordCount")
        SELECT types as crmType,COUNT(record_id) as count FROM 72crm_admin_record WHERE
            create_user_id in ( #for(x:userIds)
                                      #(for.index == 0 ? "" : ",")
                                      #para(x)
                                      #end)
        #if(type == 1)
          AND TO_DAYS(create_time) = TO_DAYS(now())
        #end
        #if(type == 2)
        AND to_days(NOW()) - TO_DAYS(create_time) = 1
        #end
        #if(type == 3)
        AND   YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now())
        #end
        #if(type == 4)
        AND  YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1
        #end
        #if(type == 5)
        AND  date_format(create_time,'%Y-%m')=date_format(now(),'%Y-%m')
        #end
        #if(type == 6)
        AND  date_format(create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m')
        #end
        #if(type == 7)
        AND  QUARTER(create_time)=QUARTER(now())
        #end
        #if(type == 8)
        AND  QUARTER(create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER))
        #end
        #if(type == 9)
        AND   YEAR(create_time)=YEAR(NOW())
        #end
        #if(type == 10)
        AND  YEAR(create_time)=YEAR(date_sub(now(),interval 1 year))
        #end
        #if(type == 11)
        AND  TO_DAYS(create_time) >= TO_DAYS(#para(startTime)) and  TO_DAYS(create_time) <= TO_DAYS(#para(endTime))
        #end
        group by types
    #end
    #sql ("queryRecordList")
      select a.record_id,b.img as user_img,b.realname,a.create_time,a.content,a.category,a.next_time,a.batch_id,a.business_ids,a.contacts_ids,a.types,a.types_id,
      (case a.types
         when 'crm_customer' then (select customer_name from `72crm_crm_customer` where customer_id = a.types_id)
         when 'crm_leads' then (select leads_name from `72crm_crm_leads` where leads_id = a.types_id)
         when 'crm_business' then (select business_name from `72crm_crm_business` where business_id = a.types_id)
         when 'crm_contract' then (select name from `72crm_crm_contract` where contract_id = a.types_id)
         when 'crm_contacts' then (select name from `72crm_crm_contacts` where contacts_id = a.types_id)
      end) as 'typesName'
      from 72crm_admin_record as a inner join 72crm_admin_user as b on a.create_user_id = b.user_id where a.types = #para(crmType)
        and a.create_user_id in ( #for(x:userIds)
        #(for.index == 0 ? "" : ",")
        #para(x)
        #end)
      #if(type == 1)
      AND TO_DAYS(a.create_time) = TO_DAYS(now())
      #end
      #if(type == 2)
      AND to_days(NOW()) - TO_DAYS(a.create_time) = 1
      #end
      #if(type == 3)
      AND   YEARWEEK(date_format(a.create_time,'%Y-%m-%d')) = YEARWEEK(now())
      #end
      #if(type == 4)
      AND  YEARWEEK(date_format(a.create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1
      #end
      #if(type == 5)
      AND  date_format(a.create_time,'%Y-%m')=date_format(now(),'%Y-%m')
      #end
      #if(type == 6)
      AND  date_format(a.create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m')
      #end
      #if(type == 7)
      AND  QUARTER(a.create_time)=QUARTER(now())
      #end
      #if(type == 8)
      AND  QUARTER(a.create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER))
      #end
      #if(type == 9)
      AND   YEAR(a.create_time)=YEAR(NOW())
      #end
      #if(type == 10)
      AND  YEAR(a.create_time)=YEAR(date_sub(now(),interval 1 year))
      #end
      #if(type == 11)
      AND  TO_DAYS(a.create_time) >= TO_DAYS(#para(startTime)) and  TO_DAYS(a.create_time) <= TO_DAYS(#para(endTime))
      #end
    #end
#end
