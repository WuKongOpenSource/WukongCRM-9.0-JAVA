#namespace("crm.contact")
    #sql("getContactsPageList")
    select contacts_id,name,customer_name,owner_user_name from contactsview where 1=1
      #if(contactsName)
      and name like CONCAT('%',#para(contactsName),'%')
      #end
      #if(customerName)
      and customer_name like CONCAT('%',#para(customerName),'%')
      #end
      #if(telephone)
      and telephone = #para(telephone)
      #end
      #if(mobile)
      and mobile = #para(mobile)
      #end
    #end

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
    select a.business_id,a.business_name,a.money,f.customer_name,d.name as type_name,e.name as status_name
    from 72crm_crm_business as a inner join 72crm_crm_contacts_business as b inner join 72crm_crm_contacts as c
    inner join 72crm_crm_business_type as d inner join 72crm_crm_business_status as e
    inner join 72crm_crm_customer as f on a.customer_id = f.customer_id
    where a.business_id = b.business_id and b.contacts_id = c.contacts_id and a.type_id = d.type_id
    and a.status_id = e.status_id and c.contacts_id = ?
    #end

    #sql ("queryBusinessNumber")
    select count(*)
    from 72crm_crm_business as a inner join 72crm_crm_customer as b inner join 72crm_crm_contacts as c
    where a.customer_id = b.customer_id and b.customer_id = c.customer_id and c.contacts_id in (?)
    #end

    #sql ("getRecord")
    select a.record_id,b.img as user_img,b.realname,a.create_time,a.content,a.category,a.next_time,a.batch_id,a.contacts_ids,a.business_ids
    from 72crm_admin_record as a inner join 72crm_admin_user as b
    where a.create_user_id = b.user_id and ((types = 'crm_contacts' and types_id = ?) or
    (types = 'crm_customer' and FIND_IN_SET(?,IFNULL(a.contacts_ids,0)))) order by a.create_time desc
    #end

    #sql ("queryByIds")
    select * from 72crm_crm_contacts
    where contacts_id in (
        #for(i:ids)
          #(for.index > 0 ? "," : "")#para(i)
        #end
    )
    #end

    #sql ("excelExport")
    select *
    from contactsview
    where contacts_id in (
        #for(i:ids)
          #(for.index > 0 ? "," : "")#para(i)
        #end
    ) order by update_time desc
    #end

    #sql ("queryRepeatFieldNumber")
    select count(*) as number from 72crm_crm_contacts where 1=2
      #if(contactsName)
      or name = #para(contactsName)
      #end
      #if(telephone)
      or telephone = #para(telephone)
      #end
      #if(mobile)
      or mobile = #para(mobile)
      #end
    #end

    #sql ("queryRepeatField")
    select contacts_id,batch_id from 72crm_crm_contacts where 1=2
      #if(contactsName)
      or name = #para(contactsName)
      #end
      #if(telephone)
      or telephone = #para(telephone)
      #end
      #if(mobile)
      or mobile = #para(mobile)
      #end
    #end

    #sql ("queryBatchIdByIds")
    select batch_id from 72crm_crm_contacts where contacts_id in (
        #for(i:ids)
          #(for.index > 0 ? "," : "")#para(i)
        #end
    )
    #end

    #sql ("unrelateBusiness")
    delete from 72crm_crm_contacts_business where contacts_id = #para(contactsId) and business_id in (
        #for(i:ids)
          #(for.index > 0 ? "," : "")#para(i)
        #end
    )
    #end
#end
