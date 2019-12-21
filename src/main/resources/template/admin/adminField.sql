#namespace("admin.field")
  #sql ("queryAddField")
    select field_id,field_name,name,type,input_tips,options,is_unique,is_null,"" as value,field_type from 72crm_admin_field where label = ? order by sorting
  #end
  #sql ("queryInformationField")
    select field_name,name,type,field_type from 72crm_admin_field where label = ? order by sorting
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
      DELETE FROM 72crm_admin_field WHERE field_id not in (#fori(ids))
      and (operating = '0' or operating = '2')
      and label=#para(label)
      #if(label==10)
        and examine_category_id=#para(categoryId)
      #end
  #end
  #sql("deleteByFieldValue")
      DELETE FROM 72crm_admin_fieldv WHERE field_id in
      (
      SELECT field_id FROM 72crm_admin_field WHERE field_id not in (#fori(ids))
      and (operating = '0' or operating = '2')
      and label=#para(label)
      #if(label==10)
        and examine_category_id=#para(categoryId)
      #end
      )
  #end

  #sql ("updateFieldSortName")
    update 72crm_admin_field_sort set field_name = #para(name),name = #para(name) where field_id = #para(field_id)
  #end

  #sql ("deleteFieldSort")
    delete from 72crm_admin_field_sort where label = #para(label) and name in (#fori(names))
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
        and a.name in (#fori(names))
      #end
      union all
      SELECT
        a.`name` AS fieldName,a.`name`,a.type,a.label,a.remark,a.input_tips,a.max_length,a.default_value,a.is_unique AS isUnique,a.is_null AS isNull,a.sorting,a.`options`,b.`value`,a.operating
      FROM
        72crm_admin_field AS a left join 72crm_admin_fieldv as b on a.field_id = b.field_id
      WHERE
        a.label = (SELECT a.label FROM 72crm_admin_field AS a left join 72crm_admin_fieldv as b on a.field_id = b.field_id WHERE b.batch_id =#para(batchId) limit 1) and a.field_id not in (SELECT field_id FROM 72crm_admin_fieldv WHERE batch_id =#para(batchId))
        #if(names)
          and a.name in ((#fori(names)))
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
    SELECT COUNT(*) FROM 72crm_admin_fieldv WHERE field_id=#para(field_id)  and value=#para(value)
    #if(batchId)
      and batch_id!=#para(batchId)
    #end
  #end
  #sql("queryFixedIsExist")
    SELECT COUNT(*) FROM 72crm_#(tableName) WHERE #(field_name)=#para(value)
    #if(batchId)
      and batch_id!=#para(batchId)
    #end
  #end
  #sql("queryFieidValue")
    SELECT #(primaryKey),batch_id FROM 72crm_#(tableName) where 1=1
    #if(field_type==1)
      and #(field_name) = #para(value)
    #elseif(field_type==0)
      SELECT customer_id,batch_id FROM 72crm_crm_customer WHERE batch_id=(SELECT batch_id FROM 72crm_admin_fieldv WHERE field_id=#para(field_id) and value=#para(value) LIMIT 0,1)
    #end
  #end
  #sql ("queryListHead")
    select field_name as fieldName,name,type,field_id from 72crm_admin_field_sort where is_hide = 0 and label = ? and user_id = ? order by sort asc
  #end

  #sql ("queryFieldConfig")
    select id,name from 72crm_admin_field_sort where is_hide = ? and label = ? and user_id = ? order by sort asc
  #end

  #sql ("sort")
    update 72crm_admin_field_sort set is_hide = 0,sort = ? where label = ? and user_id = ? and id = ?
  #end

  #sql ("isHide")
    update 72crm_admin_field_sort set is_hide = 1,sort = 0
    where id in (#fori(ids))
    and label = #para(label) and user_id = #para(userId)
  #end

  #sql ("queryCustomField")
    select a.name,a.value,b.type from 72crm_admin_fieldv as a left join 72crm_admin_field as b on a.field_id = b.field_id where batch_id = ?
  #end

  #sql("queryFieldValueNoDelete")
      #if(field_type==1)
        SELECT #(primaryKey),batch_id FROM 72crm_#(tableName) where #if(type == 4)status != 3 and #end #(field_name) = #para(value)
      #elseif(field_type==0)
        SELECT #(primaryKey),batch_id FROM 72crm_#(tableName) WHERE #if(type == 4)status != 3 and #end batch_id=(SELECT batch_id FROM 72crm_admin_fieldv WHERE field_id=#para(field_id) and value=#para(value) LIMIT 0,1)
      #end
  #end
#end
