#namespace("admin.field")
    #sql ("queryAddField")
    select field_id,field_name,name,type,input_tips,options,is_unique,is_null,"" as value,field_type from 72crm_admin_field where label = ? order by sorting
    #end

    #sql ("queryUpdateField")
    select a.field_id,a.field_name,b.name,b.value,a.type,a.input_tips,a.options,a.is_unique,a.is_null,a.field_type,a.sorting
    from 72crm_admin_field as a left join 72crm_admin_fieldv as b on a.field_id = b.field_id
    where a.label = ?
    #end

    #sql ("queryFixedField")
    select field_id,field_name,name,type,options,is_null,field_type,sorting from 72crm_admin_field where label = ? and field_type = 1
    #end

    #sql("queryFields")
      SELECT IFNULL(label,'1') as label,IFNULL(MAX(update_time),'2000-01-01 00:00:00') as update_time,'线索管理' as name,'crm_leads' as types FROM 72crm_admin_field WHERE label='1'
      union all
      SELECT IFNULL(label,'2') as label,IFNULL(MAX(update_time),'2000-01-01 00:00:00') as update_time,'客户管理' as name,'crm_customer' as types FROM 72crm_admin_field WHERE label='2'
      union all
      SELECT IFNULL(label,'3') as label,IFNULL(MAX(update_time),'2000-01-01 00:00:00') as update_time,'联系人管理' as name,'crm_contacts' as types FROM 72crm_admin_field WHERE label='3'
      union all
      SELECT IFNULL(label,'4') as label,IFNULL(MAX(update_time),'2000-01-01 00:00:00') as update_time,'产品管理' as name,'crm_product' as types FROM 72crm_admin_field WHERE label='4'
      union all
      SELECT IFNULL(label,'5') as label,IFNULL(MAX(update_time),'2000-01-01 00:00:00') as update_time,'商机管理' as name,'crm_business' as types FROM 72crm_admin_field WHERE label='5'
      union all
      SELECT IFNULL(label,'6') as label,IFNULL(MAX(update_time),'2000-01-01 00:00:00') as update_time,'合同管理' as name,'crm_contract' as types FROM 72crm_admin_field WHERE label='6'
      union all
      SELECT IFNULL(label,'7') as label,IFNULL(MAX(update_time),'2000-01-01 00:00:00') as update_time,'回款管理' as name,'crm_receivables' as types FROM 72crm_admin_field WHERE label='7'
    #end
    #sql("deleteByChooseId")
      DELETE FROM 72crm_admin_field WHERE field_id not in
      (
        #for(i:ids)
           #(for.index > 0 ? "," : "") #para(i)
        #end
      )
      and (operating = '0' or operating = '2')
      and label=#para(label)
      #if(label==10)
        and examine_category_id=#para(categoryId)
      #end
    #end
    #sql("deleteByFieldValue")
      DELETE FROM 72crm_admin_fieldv WHERE field_id in
      (
      SELECT field_id FROM 72crm_admin_field WHERE field_id not in
      (
        #for(i:ids)
           #(for.index > 0 ? "," : "") #para(i)
        #end
      )
      and (operating = '0' or operating = '2')
      and label=#para(label)
      #if(label==10)
        and examine_category_id=#para(categoryId)
      #end
      )
    #end
    #sql ("updateFieldByParentId")
    update 72crm_admin_field
    set
        #if(name)
          name = #para(name)
        #end
        #if(remark)
          ,remark = #para(remark)
        #end
        #if(input_tips)
          ,input_tips = #para(input_tips)
        #end
        #if(max_length)
          ,max_length = #para(max_length)
        #end
        #if(default_value)
          ,default_value = #para(default_value)
        #end
        #if(is_unique)
          ,is_unique = #para(is_unique)
        #end
        #if(is_null)
          ,is_null = #para(is_null)
        #end
        #if(sorting)
          ,sorting = #para(sorting)
        #end
        #if(options)
          ,options = #para(options)
        #end
        where parent_id = #para(field_id)
    #end

    #sql ("updateFieldSortName")
    update 72crm_admin_field_sort set field_name = #para(name),name = #para(name) where field_id = #para(field_id)
    #end

    #sql ("deleteFieldSort")
    delete from 72crm_admin_field_sort where label = #para(label) and name in
    (
        #for (i:names)
            #(for.index > 0 ? "," : "")#para(i)
        #end
    )
    #end

    #sql ("customerFieldList")
    select field_id,field_name,name,type,options from 72crm_admin_field where field_type = 0 and label = ?
    #end

    #sql("list")
      SELECT field_id,field_name,name,type,label,remark,input_tips,max_length,default_value,is_unique,is_null,options,operating,update_time,examine_category_id,field_type,relevant FROM 72crm_admin_field WHERE label=#para(label)
      #if(categoryId)
        and examine_category_id=#para(categoryId)
      #end
      ORDER BY sorting asc
    #end
    #sql("queryFieldsByBatchId")
      SELECT a.`name` as fieldName,a.`name`,a.type,a.label,a.remark,a.input_tips,a.max_length,a.default_value,a.is_unique as isUnique,a.is_null as isNull,a.sorting,a.`options`,b.`value`,a.operating
      FROM 72crm_admin_field as a left join 72crm_admin_fieldv as b on a.field_id = b.field_id
      WHERE b.batch_id = #para(batchId)
      #if(names)
        and a.name in
        (
          #for(i:names)
            #(for.index > 0 ? "," : "")#para(i)
          #end
        )
      #end
      union all
      SELECT
        a.`name` AS fieldName,a.`name`,a.type,a.label,a.remark,a.input_tips,a.max_length,a.default_value,a.is_unique AS isUnique,a.is_null AS isNull,a.sorting,a.`options`,b.`value`,a.operating
      FROM
        72crm_admin_field AS a left join 72crm_admin_fieldv as b on a.field_id = b.field_id
      WHERE
        a.label = (SELECT a.label FROM 72crm_admin_field AS a left join 72crm_admin_fieldv as b on a.field_id = b.field_id WHERE b.batch_id =#para(batchId) limit 1) and a.field_id not in (SELECT field_id FROM 72crm_admin_fieldv WHERE batch_id =#para(batchId))
        #if(names)
          and a.name in
          (
            #for(i:names)
              #(for.index > 0 ? "," : "")#para(i)
            #end
          )
        #end
        #if(label)
          and a.examine_category_id = (SELECT a.examine_category_id FROM 72crm_admin_field AS a left join 72crm_admin_fieldv as b on a.field_id = b.field_id WHERE b.batch_id =#para(batchId) limit 1)
          and a.field_name not in ('content','remark')
        #end
    #end
    #sql("queryFieldStyle")
      SELECT * FROM 72crm_admin_field_style WHERE type=? and field_name =? and user_id=? limit 1
    #end
    #sql("queryFieldIsExist")
      SELECT COUNT(*)
      FROM 72crm_admin_field as a inner join 72crm_admin_fieldv as b on a.field_id = b.field_id
      WHERE a.label=#para(types) and a.name=#para(fieldName) and b.value=#para(val)
    #end

    #sql ("queryListHead")
    select field_name as fieldName,name from 72crm_admin_field_sort where is_hide = 0 and label = ? and user_id = ? order by sort asc
    #end

    #sql ("queryFieldConfig")
    select id,name from 72crm_admin_field_sort where is_hide = ? and label = ? and user_id = ? order by sort asc
    #end

    #sql ("sort")
    update 72crm_admin_field_sort set is_hide = 0,sort = ? where label = ? and user_id = ? and id = ?
    #end

    #sql ("isHide")
    update 72crm_admin_field_sort set is_hide = 1,sort = 0
    where id in (
       #for(i : ids)
           #(for.index > 0 ? "," : "")#para(i)
       #end
   ) and label = #para(label) and user_id = #para(userId)
   #end
   #sql ("leadsview")
    create or replace view leadsview as select a.*,b.realname as create_user_name,c.realname as owner_user_name,z.* from 72crm_crm_leads as a left join 72crm_admin_user as b on a.create_user_id = b.user_id left join 72crm_admin_user as c on a.owner_user_id = c.user_id left join fieldleadsview as z on a.batch_id = z.field_batch_id
   #end
   #sql ("customerview")
    create or replace view customerview  as select a.*,b.realname as create_user_name,c.realname as owner_user_name,z.* from 72crm_crm_customer as a left join 72crm_admin_user as b on a.create_user_id = b.user_id left join 72crm_admin_user as c on a.owner_user_id = c.user_id left join fieldcustomerview as z on a.batch_id = z.field_batch_id
   #end
   #sql ("contactsview")
    create or replace view contactsview as select a.*,a.name as contacts_name ,b.realname as create_user_name,c.realname as owner_user_name,d.customer_name,z.* from 72crm_crm_contacts as a left join 72crm_admin_user as b on a.create_user_id = b.user_id left join 72crm_admin_user as c on a.owner_user_id = c.user_id left join 72crm_crm_customer as d on a.customer_id = d.customer_id left join fieldcontactsview as z on a.batch_id = z.field_batch_id
   #end
   #sql ("productview")
    create or replace view productview as select a.*,b.realname as create_user_name,c.realname as owner_user_name,d.name as category_name,z.* from 72crm_crm_product as a left join 72crm_admin_user as b on a.create_user_id = b.user_id left join 72crm_admin_user as c on a.owner_user_id = c.user_id left join 72crm_crm_product_category as d on a.category_id = d.category_id left join fieldproductview as z on a.batch_id = z.field_batch_id
   #end
   #sql ("businessview")
    create or replace view businessview as select a.*,b.realname as create_user_name,c.realname as owner_user_name,d.customer_name,e.name as type_name,f.name as status_name,z.* from 72crm_crm_business as a left join 72crm_admin_user as b on a.create_user_id = b.user_id left join 72crm_admin_user as c on a.owner_user_id = c.user_id left join 72crm_crm_customer as d on a.customer_id = d.customer_id left join 72crm_crm_business_type as e on a.type_id = e.type_id left join 72crm_crm_business_status as f on a.status_id = f.status_id left join fieldbusinessview as z on a.batch_id = z.field_batch_id
   #end
   #sql ("contractview")
    create or replace view contractview as select a.*,b.realname as create_user_name,c.realname as owner_user_name,d.customer_name,e.business_name,f.name as contacts_name,g.realname as company_user_name,z.* from 72crm_crm_contract as a left join 72crm_admin_user as b on a.create_user_id = b.user_id left join 72crm_admin_user as c on a.owner_user_id = c.user_id left join 72crm_crm_customer as d on a.customer_id = d.customer_id left join 72crm_crm_business as e on a.business_id = e.business_id left join 72crm_crm_contacts as f on a.contacts_id = f.contacts_id left join 72crm_admin_user as g on a.company_user_id = g.user_id left join fieldcontractview as z on a.batch_id = z.field_batch_id
   #end
   #sql ("receivablesview")
     create or replace view receivablesview as select a.*,b.realname as create_user_name,c.realname as owner_user_name,d.customer_name,e.name as contract_name,e.num as contract_num,f.num as plan_num,z.* from 72crm_crm_receivables as a left join 72crm_admin_user as b on a.create_user_id = b.user_id left join 72crm_admin_user as c on a.owner_user_id = c.user_id left join 72crm_crm_customer as d on a.customer_id = d.customer_id left join 72crm_crm_contract as e on a.contract_id = e.contract_id left join 72crm_crm_receivables_plan as f on a.plan_id = f.plan_id left join fieldreceivablesview as z on a.batch_id = z.field_batch_id
   #end
   #sql ("fieldleadsview")
     create or replace view fieldleadsview as select %s batch_id as field_batch_id from 72crm_admin_fieldv as a inner join 72crm_admin_field as d on `a`.`field_id` = `d`.`field_id` %s where d.label = %s and a.batch_id is not null and a.batch_id != '' and d.field_type = 0 group by a.batch_id
   #end
   #sql ("fieldcustomerview")
     create or replace view fieldcustomerview  as select %s batch_id as field_batch_id from 72crm_admin_fieldv as a inner join 72crm_admin_field as d on `a`.`field_id` = `d`.`field_id` %s where d.label = %s and a.batch_id is not null and a.batch_id != '' and d.field_type = 0 group by a.batch_id
   #end
   #sql ("fieldcontactsview")
     create or replace view fieldcontactsview as select %s batch_id as field_batch_id from 72crm_admin_fieldv as a inner join 72crm_admin_field d on `a`.`field_id` = `d`.`field_id` %s where d.label = %s and a.batch_id is not null and a.batch_id != '' and d.field_type = 0 group by a.batch_id
   #end
   #sql ("fieldproductview")
     create or replace view fieldproductview as select %s batch_id as field_batch_id from 72crm_admin_fieldv as a inner join 72crm_admin_field as d on `a`.`field_id` = `d`.`field_id` %s where d.label = %s and a.batch_id is not null and a.batch_id != '' and d.field_type = 0 group by a.batch_id
   #end
   #sql ("fieldbusinessview")
     create or replace view fieldbusinessview as select %s batch_id as field_batch_id from 72crm_admin_fieldv as a inner join 72crm_admin_field as d on `a`.`field_id` = `d`.`field_id` %s where d.label = %s and a.batch_id is not null and a.batch_id != ''and d.field_type = 0 group by a.batch_id
   #end
   #sql ("fieldcontractview")
     create or replace view fieldcontractview as select %s batch_id as field_batch_id from 72crm_admin_fieldv as a inner join 72crm_admin_field as d on `a`.`field_id` = `d`.`field_id` %s where d.label = %s and a.batch_id is not null and a.batch_id != '' and d.field_type = 0 group by a.batch_id
   #end
   #sql ("fieldreceivablesview")
     create or replace view fieldreceivablesview as select %s batch_id as field_batch_id from 72crm_admin_fieldv as a inner join 72crm_admin_field as d on `a`.`field_id` = `d`.`field_id` %s where d.label = %s and a.batch_id is not null and a.batch_id != '' and d.field_type = 0 group by a.batch_id
   #end

   #sql ("queryCustomField")
   select a.name,a.value,b.type from 72crm_admin_fieldv as a left join 72crm_admin_field as b on a.field_id = b.field_id where batch_id = ?
   #end
#end
