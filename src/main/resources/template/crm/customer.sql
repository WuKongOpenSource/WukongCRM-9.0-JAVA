#namespace("crm.customer")
    #sql("getCustomerPageList")
      select customer_id,customer_name,owner_user_name from customerview where 1=1
      #if(customerName)
      and customer_name like CONCAT('%',#para(customerName),'%')
      #end
      #if(mobile)
      and mobile = #para(mobile)
      #end
      #if(telephone)
      and telephone = #para(telephone)
      #end
    #end

    #sql("count")
    select count(*) as customerNum from customerview where customer_name = ?
    #end

    #sql("queryById")
    select *,(IF(owner_user_id is null,1,0)) as is_pool from customerview
    where customer_id = ?
    #end

    #sql("queryByName")
    select * from customerview
    where customer_name = ?
    #end

    #sql("deleteByIds")
    delete from 72crm_crm_customer where customer_id = ?
    #end

    #sql("queryBusiness")
    select a.business_id,a.business_name,a.money,a.is_end,a.type_id,a.status_id,b.customer_name,c.name as type_name,d.name as status_name
    from 72crm_crm_business as a inner join 72crm_crm_customer as b inner join 72crm_crm_business_type as c inner join
    72crm_crm_business_status as d
    where a.customer_id = b.customer_id and a.type_id = c.type_id and a.status_id = d.status_id and a.customer_id = #para(customerId)
      #if(businessName)
      and a.business_name like concat('%',#para(businessName),'%')
      #end
    #end

    #sql ("queryBusinessNumber")
    select count(*) from 72crm_crm_business where customer_id = ?
    #end

    #sql("queryContacts")
    select contacts_id,name,mobile,post,telephone,是否关键决策人 from contactsview where customer_id = #para(customerId)
      #if(contactsName)
      and a.name like CONCAT('%',#para(contactsName),'%')
      #end
    #end

    #sql ("queryContactsNumber")
    select count(*) from 72crm_crm_contacts where customer_id = ?
    #end

    #sql("queryReceivablesPlan")
    select a.plan_id,a.num,b.customer_name,c.num as contract_num,a.money,a.return_date,a.return_type,a.remind,a.remark
    from 72crm_crm_receivables_plan as a inner join 72crm_crm_customer as b
    inner join 72crm_crm_contract as c
    where a.customer_id = b.customer_id and a.contract_id = c.contract_id and b.customer_id = ?
    #end

    #sql("queryReceivablesPlanPageSelect")
    select a.plan_id,a.num,b.customer_name,c.num as contract_num,a.money,a.return_date,a.return_type,a.remind,a.remark
    #end

    #sql("queryReceivablesPlanPageFrom")
    from 72crm_crm_receivables_plan as a inner join 72crm_crm_customer as b
    inner join 72crm_crm_contract as c
    where a.customer_id = b.customer_id and a.contract_id = c.contract_id and b.customer_id = ?
    #end

    #sql("queryReceivables")
    select a.receivables_id,a.number as receivables_num,b.name as contract_name,b.money as contract_money,a.money as receivables_money,c.realname,
    a.check_status,a.return_time
    from 72crm_crm_receivables as a inner join 72crm_crm_contract as b inner join 72crm_admin_user as c
    where a.contract_id = b.contract_id and a.owner_user_id = c.user_id and a.customer_id = ?
    #end

    #sql ("queryContract")
    select a.contract_id,a.num,a.name as contract_name,b.customer_name,a.money,a.start_time,a.end_time,
    ifnull((select sum(c.money) from `72crm_crm_receivables` c where c.contract_id = a.contract_id and c.check_status = 2),0) as receivablesMoneyCount
    from 72crm_crm_contract as a inner join 72crm_crm_customer as b on a.customer_id = b.customer_id
    where a.customer_id = #para(customerId)
      #if(contractName)
      and a.name like CONCAT('%',#para(contractName),'%')
      #end
    #end

    #sql ("queryPassContract")
    select a.contract_id,a.num,a.name as contract_name,b.customer_name,a.money,a.start_time,a.end_time
    from 72crm_crm_contract as a inner join 72crm_crm_customer as b on a.customer_id = b.customer_id
    where a.customer_id = #para(customerId) and a.check_status = #para(checkStatus)
      #if(contractName)
      and a.name like CONCAT('%',#para(contractName),'%')
      #end
    #end

    #sql ("getMembers")
    select a.user_id as id,a.realname,b.name
    from 72crm_admin_user as a inner join 72crm_admin_dept as b on a.dept_id = b.dept_id
    where a.user_id = ?
    #end

    #sql ("lock")
    update 72crm_crm_customer
    set is_lock = #para(isLock)
    where customer_id in (
        #for(i : ids)
            #(for.index > 0 ? "," : "")#para(i)
        #end
    )
    #end

    #sql ("updateOwnerUserId")
    UPDATE 72crm_crm_customer SET owner_user_id = null
    WHERE customer_id in (
        #for(i:ids)
             #(for.index > 0 ? "," : "")#para(i)
        #end
    )

    #end
    #sql ("selectOwnerUserId")
    select customer_id from 72crm_crm_customer as ccc
    WHERE owner_user_id != 0
        and deal_status ='未成交'
        and is_lock = 0
        and ((unix_timestamp(now()) - unix_timestamp(IFNULL((
		SELECT car.create_time
		 FROM 72crm_admin_record  as car
		where
		car.types = 'crm_customer'
		and car.types_id = ccc.customer_id
		ORDER BY car.create_time DESC LIMIT 1),ccc.create_time))) > ? or (unix_timestamp(now()) - unix_timestamp(create_time)) > ?)
    #end

    #sql ("getRecord")
    select a.record_id,b.img as user_img,b.realname,a.create_time,a.content,a.category,a.next_time,a.batch_id,a.business_ids,a.contacts_ids
    from 72crm_admin_record as a inner join 72crm_admin_user as b
    where a.create_user_id = b.user_id and types = 'crm_customer' and types_id = ? order by a.create_time desc
    #end

    #sql ("queryByIds")
    select * from 72crm_crm_customer
    where customer_id in (
        #for(i:ids)
          #(for.index > 0 ? "," : "")#para(i)
        #end
    )
    #end

    #sql ("excelExport")
    select * from customerview
    where customer_id in (
        #for(i:ids)
          #(for.index > 0 ? "," : "")#para(i)
        #end
    ) order by update_time desc
    #end
    #sql ("deleteMember")
    update 72crm_crm_customer set rw_user_id = replace(rw_user_id,?,','),ro_user_id = replace(ro_user_id,?,',') where customer_id = ?
    #end
    #sql ("updateDealStatusById")
      update 72crm_crm_customer set deal_status = ? where customer_id = ?
    #end

    #sql ("queryBatchIdByIds")
    select batch_id from 72crm_crm_customer where customer_id in (
        #for(i:ids)
          #(for.index > 0 ? "," : "")#para(i)
        #end
    )
    #end
    #sql ("getCustomersByIds")
    update 72crm_crm_customer set owner_user_id = #para(userId),followup = 0,create_time = #para(createTime) where customer_id in (
        #for(i:ids)
          #(for.index > 0 ? "," : "")#para(i)
        #end
    )
    #end
#end
