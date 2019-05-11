#namespace("crm.contact")
    #sql ("queryById")
    select * from contactsview where contacts_id = ?
    #end

    #sql ("queryByName")
    select * from contactsview where name = ?
    #end

    #sql("deleteByIds")
    delete from 72crm_crm_contacts where contacts_id = ?
    #end

    #sql ("transfer")
    update 72crm_crm_contacts
    set owner_user_id = #para(ownerUserId)
    where contacts_id in (
        #for(i : ids)
            #(for.index > 0 ? "," : "")#para(i)
        #end
    )
    #end

    #sql("queryBusiness")
    select a.business_id,a.business_name,a.money,b.customer_name,d.name as type_name,e.name as status_name
    from 72crm_crm_business as a inner join 72crm_crm_customer as b inner join 72crm_crm_contacts as c
    inner join 72crm_crm_business_type as d inner join 72crm_crm_business_status as e
    where a.customer_id = b.customer_id and b.customer_id = c.customer_id and a.type_id = d.type_id
    and a.status_id = e.status_id and c.contacts_id = ?
    #end

    #sql ("queryBusinessNumber")
    select count(*)
    from 72crm_crm_business as a inner join 72crm_crm_customer as b inner join 72crm_crm_contacts as c
    where a.customer_id = b.customer_id and b.customer_id = c.customer_id and c.contacts_id in (?)
    #end

    #sql ("getRecord")
    select a.record_id,b.img as user_img,b.realname,a.create_time,a.content,a.category,a.next_time,a.batch_id
    from 72crm_admin_record as a inner join 72crm_admin_user as b
    where a.create_user_id = b.user_id and types = 'crm_contacts' and types_id = ? order by a.create_time desc
    #end

    #sql ("queryByIds")
    select * from 72crm_crm_contacts
    where contacts_id in (
        #for(i:ids)
          #(for.index > 0 ? "," : "")#para(i)
        #end
    )
    #end
#end
