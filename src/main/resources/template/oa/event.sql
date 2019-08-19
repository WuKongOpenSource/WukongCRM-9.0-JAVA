#namespace("oa.event")
    #sql("queryList")
    select a.*,b.*,c.realname,GROUP_CONCAT(d.realname) as 'owner_user_name'
    from 72crm_oa_event as a left join 72crm_oa_event_relation as b on a.event_id = b.event_id
    left join 72crm_admin_user as c on a.create_user_id = c.user_id
    left join 72crm_admin_user as d on FIND_IN_SET(d.user_id,IFNULL(a.owner_user_ids, 0))
    where start_time < ? and end_time > ? and (a.create_user_id = ? or a.owner_user_ids like CONCAT('%',?,'%')) group by a.event_id,b.eventrelation_id
    #end

    #sql("queryById")
    select a.*,b.*,c.realname,GROUP_CONCAT(d.realname) as 'owner_user_name'
    from 72crm_oa_event as a left join 72crm_oa_event_relation as b on a.event_id = b.event_id
    left join 72crm_admin_user as c on a.create_user_id = c.user_id
    left join 72crm_admin_user as d on FIND_IN_SET(d.user_id,IFNULL(a.owner_user_ids, 0))
    where a.event_id = ?
    #end

    #sql ("queryOwnerList")
    select user_id, username,img, create_time, realname, num, mobile, email, sex, dept_id, post from 72crm_admin_user
    where user_id in (
        #for(i : ids)
            #(for.index > 0 ? "," : "")#para(i)
        #end
    )
    #end

    #sql ("queryCustomerList")
    select * from 72crm_crm_customer
    where customer_id in (
        #for(i : ids)
            #(for.index > 0 ? "," : "")#para(i)
        #end
    )
    #end

    #sql ("queryContactsList")
    select * from 72crm_crm_contacts
    where contacts_id in (
        #for(i : ids)
            #(for.index > 0 ? "," : "")#para(i)
        #end
    )
    #end

    #sql ("queryBusinessList")
    select * from 72crm_crm_business
    where business_id in (
        #for(i : ids)
            #(for.index > 0 ? "," : "")#para(i)
        #end
    )
    #end

    #sql ("queryContractList")
    select * from 72crm_crm_contract
    where contract_id in (
        #for(i : ids)
            #(for.index > 0 ? "," : "")#para(i)
        #end
    )
    #end

    #sql ("delete")
    delete from 72crm_oa_event where event_id = ?
    #end

    #sql("queryEventRelation")
    select a.*,b.*,c.realname,c.img,GROUP_CONCAT(d.realname) as 'owner_user_name'
    from 72crm_oa_event as a inner join 72crm_oa_event_relation as b on a.event_id = b.event_id
    left join 72crm_admin_user as c on a.create_user_id = c.user_id
    left join 72crm_admin_user as d on FIND_IN_SET(d.user_id,IFNULL(a.owner_user_ids, 0))
    where 1=2
    #if(businessIds)
      or b.business_ids like concat('%,',#para(businessIds),',%')
    #end
    #if(contactsIds)
      or b.contacts_ids like concat('%,',#para(contactsIds),',%')
    #end
    #if(contractIds)
      or b.contract_ids like concat('%,',#para(contractIds),',%')
    #end
    #if(customerIds)
      or b.customer_ids like concat('%,',#para(customerIds),',%')
    #end
    group by a.event_id,b.eventrelation_id
   #end
#end
