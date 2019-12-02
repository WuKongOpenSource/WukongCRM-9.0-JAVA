#namespace("bi.employee")
  #sql ("totalContract")
  select count(distinct a.contract_id) as contractNum,
  IFNULL(sum(a.money),0) as contractMoney,
  IFNULL((select SUM(IFNULL(money,0)) from 72crm_crm_receivables as b where b.contract_id = a.contract_id),0) as receivablesMoney,
  (IFNULL(sum(a.money),0)-IFNULL((select SUM(IFNULL(money,0)) from 72crm_crm_receivables as b where b.contract_id = a.contract_id),0)) as unreceivedMoney
  from 72crm_crm_contract as a
  where DATE_FORMAT(a.order_date,#para(sqlDateFormat)) between #para(beginTime) and #para(finalTime) and a.check_status = 1
    #if(userIds)
    and a.owner_user_id in (
          #for(x:userIds)
          #(for.index == 0 ? "" : ",")
          #para(x)
          #end
          )
          #end
    #end
#end
