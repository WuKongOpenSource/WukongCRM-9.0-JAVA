#namespace("crm.customer")
    #sql("getCustomerPageList")
      select a.customer_id,a.customer_name,b.realname as ownerUserName
      from 72crm_crm_customer as a left join 72crm_admin_user as b on a.owner_user_id = b.user_id
      where 1=1
      #if(customerName)
      and a.customer_name like CONCAT('%',#para(customerName),'%')
      #end
      #if(mobile)
      and a.mobile = #para(mobile)
      #end
      #if(telephone)
      and a.telephone = #para(telephone)
      #end
    #end

    #sql ("queryIsRoUser")
    select count(*) from 72crm_crm_customer where find_in_set(?,ro_user_id) and customer_id = ?
    #end

    #sql("count")
    select count(*) as customerNum from 72crm_crm_customer where customer_name = ?
    #end

    #sql("queryById")
    select a.*,b.realname as ownerUserName,(IF(a.owner_user_id is null,1,0)) as is_pool
    from `72crm_crm_customer` as a left join 72crm_admin_user as b on a.owner_user_id = b.user_id
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
      select a.contacts_id,a.name,a.mobile,a.post,a.telephone,
      (select value from `72crm_admin_fieldv` as b where b.batch_id = a.batch_id and b.name = '是否关键决策人') as '是否关键决策人'
      from 72crm_crm_contacts as a where a.customer_id = #para(customerId)
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
    select a.receivables_id,a.number as receivables_num,b.name as contract_name,b.money as contract_money,a.money as receivables_money,c.realname as owner_user_name,
    a.check_status,a.return_time,d.num
    from 72crm_crm_receivables as a inner join 72crm_crm_contract as b inner join 72crm_admin_user as c
    left join 72crm_crm_receivables_plan as d on a.plan_id = d.plan_id
    where a.contract_id = b.contract_id and a.owner_user_id = c.user_id and a.customer_id = ?
    #end

    #sql ("queryContract")
    select a.contract_id,a.num,a.name as contract_name,b.customer_name,a.money,a.start_time,a.end_time,a.check_status,
    ifnull((select sum(c.money) from `72crm_crm_receivables` c where c.contract_id = a.contract_id and c.check_status = 2),0) as receivablesMoneyCount
    from 72crm_crm_contract as a inner join 72crm_crm_customer as b on a.customer_id = b.customer_id
    where a.customer_id = #para(customerId)
      #if(contractName)
      and a.name like CONCAT('%',#para(contractName),'%')
      #end
    #end

    #sql ("queryPassContract")
    select a.contract_id,a.num,a.name as contract_name,b.customer_name,a.money,a.start_time,a.end_time,a.check_status
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

    #sql ("queryDealNum")
    select count(*) from 72crm_crm_customer
    where deal_status = '已成交' and customer_id in (
        #for(i : ids)
            #(for.index > 0 ? "," : "")#para(i)
        #end
    )
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

    #sql ("setDealStatus")
    update 72crm_crm_customer set is_lock = 0,deal_status = #para(dealStatus)
    where customer_id in (
        #for(i : ids)
            #(for.index > 0 ? "," : "")#para(i)
        #end
    )
    #end

    #sql ("lockNum")
    SELECT count(owner_user_id) as num,owner_user_id as userId FROM 72crm_crm_customer
    where owner_user_id is not null and is_lock = 0
    and customer_id in (
        #for(i : ids)
            #(for.index > 0 ? "," : "")#para(i)
        #end
    ) group by owner_user_id
    #end

    #sql ("ownerNum")
    SELECT count(customer_id) FROM 72crm_crm_customer
    where owner_user_id != #para(ownerUserId)
    and customer_id in (
        #for(i : ids)
            #(for.index > 0 ? "," : "")#para(i)
        #end
    )
    #end

    #sql ("updateOwnerUserId")
    UPDATE 72crm_crm_customer SET owner_user_id = null,ro_user_id = ',',rw_user_id = ','
    WHERE customer_id in (
        #for(i:ids)
             #(for.index > 0 ? "," : "")#para(i)
        #end
    )
    #end

    #sql ("updateContacts")
    UPDATE 72crm_crm_contacts SET owner_user_id = null,ro_user_id = ',',rw_user_id = ','
    WHERE customer_id in (
        #for(i:ids)
             #(for.index > 0 ? "," : "")#para(i)
        #end
    )
    #end

    #sql ("selectOwnerUserId")
    select customer_id from 72crm_crm_customer as ccc
    WHERE owner_user_id != 0
        and deal_status = '未成交'
        and is_lock = 0
        and ((unix_timestamp(now()) - unix_timestamp(IFNULL((
		SELECT car.create_time
		 FROM 72crm_admin_record  as car
		where
		car.types = 'crm_customer'
		and car.types_id = ccc.customer_id
		ORDER BY car.create_time DESC LIMIT 1),ccc.create_time))) > ? or
		(unix_timestamp(NOW()) - unix_timestamp((SELECT cors.create_time FROM 72crm_crm_owner_record  as cors
    where cors.type = 8
    and cors.type_id = ccc.customer_id and cors.post_owner_user_id = ccc.owner_user_id
		ORDER BY cors.create_time DESC LIMIT 1))) > ?)
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
    SELECT
      `a`.*,
      `b`.`realname` AS `create_user_name`,
      `c`.`realname` AS `owner_user_name`,
      `z`.*
    FROM
      `72crm_crm_customer` as `a`
    left JOIN `72crm_admin_user` `b` ON `a`.`create_user_id` = `b`.`user_id`
    left JOIN `72crm_admin_user` `c` ON `a`.`owner_user_id` = `c`.`user_id`
    INNER JOIN (
      SELECT
        b.batch_id as field_batch_id
        #for(field : fieldMap)
          #if(field.value&&field.value.get("field_type")==0)
            #if(field.value.get("type")==12)
              ,GROUP_CONCAT(if(a.name = #para(field.key),c.name,null)) AS `#(field.key)`
            #elseif(field.value.get("type")==10)
              ,GROUP_CONCAT(if(a.name = #para(field.key),d.realname,null)) AS `#(field.key)`
            #else
              ,max(CASE WHEN `a`.`name` = #para(field.key) THEN `a`.`value` END) AS `#(field.key)`
            #end
          #end
        #end
        FROM 72crm_admin_fieldv AS a RIGHT JOIN (SELECT batch_id FROM 72crm_crm_customer WHERE customer_id in (#for(i:ids) #(for.index > 0 ? "," : "")#para(i) #end)) AS b ON a.batch_id = b.batch_id
        #if(fieldMap.containsKey("user"))
          left join 72crm_admin_user d on find_in_set(d.user_id,ifnull(a.value,0))
        #end
        #if(fieldMap.containsKey("dept"))
          left join 72crm_admin_dept c on find_in_set(c.dept_id,ifnull(a.value,0))
        #end
        GROUP BY b.batch_id
    ) `z` ON `a`.`batch_id` = `z`.`field_batch_id`
    order by update_time desc
    #end
    #sql ("deleteMember")
    update 72crm_crm_customer set rw_user_id = replace(rw_user_id,?,','),ro_user_id = replace(ro_user_id,?,',') where customer_id = ?
    #end
    #sql ("updateDealStatusById")
      update 72crm_crm_customer set deal_status = ?, is_lock = 0 where customer_id = ?
    #end

    #sql ("queryBatchIdByIds")
    select batch_id from 72crm_crm_customer where customer_id in (
        #for(i:ids)
          #(for.index > 0 ? "," : "")#para(i)
        #end
    )
    #end
    #sql ("getCustomersByIds")
    update 72crm_crm_customer set owner_user_id = #para(userId),followup = 0,create_time = #para(createTime),update_time = #para(createTime) where customer_id in (
        #for(i:ids)
          #(for.index > 0 ? "," : "")#para(i)
        #end
    )
    #end
    #sql ("queryCustomerSetting")
      SELECT a.setting_id,a.setting_name,a.customer_num,a.customer_deal,CONCAT(ifnull(GROUP_CONCAT(d.realname),''),',',ifnull(GROUP_CONCAT(c.name),'')) as `range`,
       ifnull(GROUP_CONCAT(d.user_id),'') as userIds,ifnull(GROUP_CONCAT(c.dept_id),'') as deptIds
    FROM 72crm_crm_customer_setting as a
           LEFT JOIN 72crm_crm_customer_settinguser as b on a.setting_id=b.setting_id
           LEFT JOIN 72crm_admin_dept as c on b.dept_id=c.dept_id and b.type='2'
           LEFT JOIN 72crm_admin_user as d on b.user_id=d.user_id and b.type='1'
    WHERE a.type=#para(type) GROUP BY a.setting_id
    #end
    #sql("queryCustomerSettingByUserId")
      SELECT a.customer_num,a.customer_deal,b.type FROM 72crm_crm_customer_setting  as a
                                                          LEFT JOIN 72crm_crm_customer_settinguser as b on a.setting_id=b.setting_id and b.type='1'
      WHERE  b.user_id=#para(userId) and a.type=#para(type)
      union all
      SELECT a.customer_num,a.customer_deal,b.type FROM 72crm_crm_customer_setting  as a
                                                          LEFT JOIN 72crm_crm_customer_settinguser as b on a.setting_id=b.setting_id and b.type='2'
                                                          LEFT JOIN 72crm_admin_user as c on b.dept_id=c.dept_id
      WHERE c.user_id=#para(userId) and a.type=#para(type)
    #end
    #sql("queryCustomerSettingNum")
      SELECT COUNT(*) FROM 72crm_crm_customer WHERE 1=1
      #if(deal==0)
        and deal_status='未成交'
      #end
      #if(type==2)
        and is_lock='1'
      #end
    #end
    #sql ("getCustomerSettingCount")
      select count(*)
      from `72crm_crm_customer_setting` a
             left join `72crm_crm_customer_settinguser` b on a.setting_id = b.setting_id
      where a.type = #para(type)
        #if(userId)
        and user_id = #para(userId)
        #end
        #if(deptId)
        and dept_id = #para(deptId)
        #end
    #end
#end
