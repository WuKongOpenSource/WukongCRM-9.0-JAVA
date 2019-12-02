#namespace("crm.backLog")
  #sql ("todayCustomerNum")
  select count(*) from 72crm_crm_customer
  where customer_id not in (IFNULL((select GROUP_CONCAT(types_id) from 72crm_admin_record where types = 'crm_customer' and to_days(create_time) = to_days(now())),0))
  and to_days(next_time) = to_days(now()) and owner_user_id = ?
  #end

  #sql ("setLeadsFollowup")
    update 72crm_crm_leads set followup = 1 where leads_id in (#fori(ids))
  #end

  #sql ("followLeadsNum")
  select count(*) from 72crm_crm_leads as a
  where followup = 0
  and is_transform = 0  and owner_user_id = ?
  #end

  #sql ("setCustomerFollowup")
  update 72crm_crm_customer set followup = 1 where customer_id in (
    #for(i : ids)
      #(for.index > 0 ? "," : "")#para(i)
    #end
  )
  #end

  #sql ("followCustomerNum")
  select count(*) from 72crm_crm_customer as a
  where followup = 0
  and owner_user_id = ?
  #end

  #sql ("checkContractNum")
  select  count(*) from 72crm_crm_contract as a inner join 72crm_admin_examine_record as b on a.examine_record_id = b.record_id left join 72crm_admin_examine_log as c on b.record_id = c.record_id where c.is_recheck != 1 and ifnull(b.examine_step_id, 1) = ifnull(c.examine_step_id, 1) and
   a.check_status in (0,3) and c.examine_status in (0,3) and c.examine_user = ?
  #end

  #sql ("checkReceivablesNum")
    select count(*) from 72crm_crm_receivables as a inner join 72crm_admin_examine_record as b on a.examine_record_id = b.record_id left join 72crm_admin_examine_log as c on b.record_id = c.record_id where ifnull(b.examine_step_id, 1) = ifnull(c.examine_step_id, 1) and
    a.check_status in (0,3) and c.examine_status in (0,3) and c.examine_user = ?
  #end

  #sql ("remindReceivablesPlanNum")
  select count(*)
  from 72crm_crm_receivables_plan as a inner join 72crm_crm_customer as b on a.customer_id = b.customer_id
  inner join 72crm_crm_contract as c on a.contract_id = c.contract_id
  where to_days(a.return_date) >= to_days(now()) and to_days(a.return_date) <= to_days(now())+a.remind
  and receivables_id is null and c.owner_user_id = ?
  #end

  #sql ("endContractNum")
  select count(*) from 72crm_crm_contract as a
  where a.check_status = 1 and  to_days(a.end_time) >= to_days(now()) and to_days(a.end_time) <= to_days(now()) + IFNULL(?,0) and a.owner_user_id = ?
  #end

  #sql ("putInPoolRemindNum")
  select count(*) from 72crm_crm_customer as a
  where owner_user_id is not null and deal_status =0 and is_lock = 0  and owner_user_id = ? and
      ((to_days(now()) - to_days(IFNULL((
            SELECT car.create_time
            FROM 72crm_admin_record as car
            where car.types = 'crm_customer' and car.types_id = a.customer_id ORDER BY car.create_time DESC LIMIT 1)
      ,a.create_time))) between ? and ? or (to_days(now()) - to_days(create_time)) between ? and ?)
  #end
#end
