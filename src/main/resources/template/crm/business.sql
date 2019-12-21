#namespace("crm.business")
    #sql ("clearBusinessProduct")
      delete from 72crm_crm_business_product where business_id = ?
    #end
    #sql("queryInformationById")
      select a.business_name,c.name as type_name,d.name status_name,a.deal_date,b.customer_name,a.money,a.remark,a.batch_id
      from `72crm_crm_business` as a
      left join `72crm_crm_customer` b on a.customer_id = b.customer_id
      left join `72crm_crm_business_status` c on c.status_id = a.status_id
      left join `72crm_crm_business_type` d on d.type_id = a.type_id
      where business_id = ?;
    #end

    #sql ("queryById")
      select a.*,b.customer_name,c.name as typeName,d.name as statusName,e.realname as owner_user_name
      from 72crm_crm_business as a left join 72crm_crm_customer as b on a.customer_id = b.customer_id
      left join 72crm_crm_business_type as c on a.type_id = c.type_id
      left join 72crm_crm_business_status as d on a.status_id = d.status_id
      left join 72crm_admin_user as e on a.owner_user_id = e.user_id
      where a.business_id = ?
    #end

    #sql("deleteByIds")
      delete from 72crm_crm_business where business_id = ?
    #end

    #sql ("transfer")
      update 72crm_crm_business
      set owner_user_id = #para(ownerUserId)
      where business_id in (#fori(ids))
    #end

    #sql ("queryProduct")
        select b.product_id,b.name,b.name as productName,c.name as category_name,a.price,a.sales_price,a.num,a.discount,a.subtotal,
        (select value from `72crm_admin_fieldv` as d where d.batch_id = b.batch_id and d.name = '单位') as 'unit',
        (select value from `72crm_admin_fieldv` as d where d.batch_id = b.batch_id and d.name = '是否上下架') as '是否上下架'
        from 72crm_crm_business_product as a inner join 72crm_crm_product as b inner join 72crm_crm_product_category as c
        where a.product_id = b.product_id and b.category_id = c.category_id and a.business_id = ?;
    #end

    #sql ("queryContract")
      select a.contract_id,a.num,a.name as contract_name,b.customer_name,a.money,a.start_time,a.end_time,a.check_status
      from 72crm_crm_contract as a left join 72crm_crm_customer as b on a.customer_id = b.customer_id
      where a.business_id = ?
    #end

    #sql ("queryContacts")
      select a.contacts_id,a.name,a.mobile,a.post
      from 72crm_crm_contacts as a inner join 72crm_crm_contacts_business as b
      where a.contacts_id = b.contacts_id and b.business_id = ?
    #end

    #sql ("queryContractNumber")
      select count(*)
      from 72crm_crm_contract as a left join 72crm_crm_customer as b on a.customer_id = b.customer_id
      where a.business_id in (?)
    #end

    #sql ("queryBusinessStatusList")
      select b.status_id,b.name,b.rate,b.order_num
      from 72crm_crm_business as a inner join 72crm_crm_business_status as b on a.type_id = b.type_id
      where a.business_id = ?
    #end

    #sql ("queryBusinessStatus")
      select business_id,status_id,is_end,status_remark from 72crm_crm_business where business_id = ?
    #end

    #sql ("queryOrderId")
      select b.order_num
      from 72crm_crm_business as a inner join 72crm_crm_business_status as b on a.status_id = b.status_id
      where a.business_id = ?
    #end

    #sql ("getRecord")
      select a.record_id,b.img as user_img,b.realname,a.create_time,a.content,a.category,a.next_time,a.batch_id,a.contacts_ids,a.business_ids
      from 72crm_admin_record as a inner join 72crm_admin_user as b
      where a.create_user_id = b.user_id and ((types = 'crm_business' and types_id = ?) or
      (types = 'crm_customer' and FIND_IN_SET(?,IFNULL(a.business_ids, 0)))) order by a.create_time desc
    #end

    #sql ("queryByIds")
      select * from 72crm_crm_business
      where business_id in (#fori(ids))
    #end

    #sql ("queryBusinessProduct")
      select b.product_id,c.name as name,d.name as category_name,b.unit,b.price,b.sales_price,b.num,b.discount,b.subtotal
      from 72crm_crm_business as a inner join 72crm_crm_business_product as b on a.business_id = b.business_id
      inner join 72crm_crm_product as c on b.product_id = c.product_id inner join 72crm_crm_product_category as d
      on c.category_id = d.category_id
      where a.business_id = ?
    #end

    #sql ("deleteMember")
    update 72crm_crm_business set rw_user_id = replace(rw_user_id,?,','),ro_user_id = replace(ro_user_id,?,',') where business_id = ?
    #end

    #sql ("queryBatchIdByIds")
      select batch_id from 72crm_crm_business where business_id in (#fori(ids))
    #end

    #sql ("unrelateContacts")
      delete from 72crm_crm_contacts_business where business_id = #para(businessId) and contacts_id in (#fori(ids))
    #end
    #sql ("queryIsAuth")
      select count(*) from 72crm_crm_business where owner_user_id in ( #fori(userIds) ) and business_id = #para(businessId)
    #end
    #sql ("queryIsRoUser")
      select count(*) from 72crm_crm_business where find_in_set(?,ro_user_id) and business_id = ?
    #end
#end
