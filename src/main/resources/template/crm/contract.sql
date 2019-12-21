#namespace("crm.contract")
  #sql("queryById")
    select cc.* ,ccu.customer_name as customerName, cb.business_name as businessName
    from 72crm_crm_contract as cc left join 72crm_crm_customer as ccu on ccu.customer_id = cc.customer_id
    left join 72crm_crm_business as cb on cb.business_id = cc.business_id
    where cc.contract_id = ?
  #end

  #sql ("getRecord")
    select a.record_id,b.img as user_img,b.realname,a.create_time,a.content,a.category,a.next_time,a.batch_id
    from 72crm_admin_record as a inner join 72crm_admin_user as b
    where a.create_user_id = b.user_id and types = 'crm_contract' and types_id = ? order by a.create_time desc
  #end

  #sql ("deleteByIds")
   delete from 72crm_crm_contract where contract_id = ?  and check_status != 1 and check_status != 2
  #end

  #sql ("transfer")
     update 72crm_crm_contract
     set owner_user_id = #para(ownerUserId)
     where contract_id in (#fori(ids))
  #end

  #sql ("queryByIds")
      select * from  72crm_crm_contract
      where contract_id in (#fori(ids))
  #end
  #sql ("queryBusinessProduct")
      select c.product_id , c.name as name,d.name as category_name,b.unit,b.price,b.sales_price,b.num,b.discount,b.subtotal
      from 72crm_crm_contract as a inner join 72crm_crm_contract_product as b on a.contract_id = b.contract_id
      inner join 72crm_crm_product as c on b.product_id = c.product_id inner join 72crm_crm_product_category as d
      on c.category_id = d.category_id
      where a.contract_id = ?
  #end

  #sql ("deleteMember")
    update 72crm_crm_contract set rw_user_id = replace(rw_user_id,?,','),ro_user_id = replace(ro_user_id,?,',') where contract_id = ?
  #end
  #sql ("queryByContractId")
      select crt.*,ccc.customer_name,cau.realname as ownerUserName,ccb.business_name,a.name as contacts_name,b.realname as company_user_name,
      ( select count(1) from 72crm_crm_receivables where contract_id =  crt.contract_id ) as receivablesCount,
      ( select IFNULL(sum(money),0) from 72crm_crm_receivables where contract_id =  crt.contract_id and check_status = 1) as receivablesMoney
      from 72crm_crm_contract as crt left join 72crm_admin_user as cau on crt.owner_user_id = cau.user_id
      left join 72crm_crm_customer as ccc on crt.customer_id = ccc.customer_id
      left join 72crm_crm_business as ccb on crt.business_id = ccb.business_id
      left join 72crm_crm_contacts as a on crt.contacts_id = a.contacts_id
      left join 72crm_admin_user as b on crt.company_user_id = b.user_id
      where crt.contract_id = ?
  #end
  #sql ("deleteByContractId")
    delete from 72crm_crm_contract_product where contract_id = ?
  #end
  #sql ("queryByNum")
    select count(*) from 72crm_crm_contract where num = ?
  #end
  #sql ("updateCheckStatusById")
    update 72crm_crm_contract set check_status = ? where contract_id = ?
  #end

  #sql ("queryContractConfig")
    select status,value as contractDay from 72crm_admin_config where name = 'expiringContractDays' limit 1
  #end

  #sql ("setContractConfig")
    update 72crm_admin_config set status = #para(status)
    #if(contractDay)
    ,value = #para(contractDay)
    #end
     where name = 'expiringContractDays'
  #end
  #sql("queryContractByRecordId")
    SELECT contract_id,name,owner_user_id FROM 72crm_crm_contract WHERE examine_record_id = ? limit 0,1
  #end
  #sql("contractDiscard")
      update `72crm_crm_contract` set check_status = 6,update_time = now() where contract_id = #para(contractId)
  #end
  #sql ("queryIsAuth")
     SELECT count(*) FROM 72crm_crm_contract WHERE owner_user_id in ( #fori(userIds) ) and contract_id = #para(contractId)
  #end
  #sql ("queryIsRoUser")
    select count(*) from 72crm_crm_contract where find_in_set(?,ro_user_id) and contract_id = ?
  #end
#end
