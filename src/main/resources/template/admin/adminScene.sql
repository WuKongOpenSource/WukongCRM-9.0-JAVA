#namespace("admin.scene")
    #sql ("queryScene")
    select a.scene_id,a.data,a.name,if(b.default_id is null,0,1) as is_default,a.is_system,a.bydata
    from 72crm_admin_scene as a left join 72crm_admin_scene_default as b on a.scene_id = b.scene_id
    where a.type = ? and a.user_id = ? and is_hide = 0 order by a.sort asc
    #end
    #sql ("queryCrmPageListByFieldType1")
      #(select) FROM (
        SELECT
          `a`.*,
          (SELECT realname FROM 72crm_admin_user WHERE user_id=`a`.`create_user_id`) AS `create_user_name`,
          (SELECT realname FROM 72crm_admin_user WHERE user_id=`a`.`owner_user_id`) AS `owner_user_name`,
          #if(label==3)
          `d`.`customer_name` AS `customer_name`,
          #elseif(label==4)
          `d`.`name` AS `category_name`,
          #elseif(label==5)
          `d`.`customer_name` AS `customer_name`,
	        `e`.`name` AS `type_name`,
	        `f`.`name` AS `status_name`,
	        #elseif(label==6)
          `d`.`customer_name` AS `customer_name`,
          IFNULL(`e`.`business_name`,'') AS `business_name`,
          `f`.`name` AS `contacts_name`,
          `g`.`realname` AS `company_user_name`,
          #elseif(label==7)
          `d`.`customer_name` AS `customer_name`,
          `e`.`name` AS `contract_name`,
          `e`.`num` AS `contract_num`,
          `e`.`money` AS `contract_money`,
          `f`.`num` AS `plan_num`,
          #end
          `z`.*
        FROM
          `72crm_crm_#(realm)` as `a`
        #if(label==3)
          LEFT JOIN `72crm_crm_customer` `d` ON `a`.`customer_id` = `d`.`customer_id`
        #elseif(label==4)
          LEFT JOIN `72crm_crm_product_category` `d` ON `a`.`category_id` = `d`.`category_id`
        #elseif(label==5)
          LEFT JOIN `72crm_crm_customer` `d` ON `a`.`customer_id` = `d`.`customer_id`
          LEFT JOIN `72crm_crm_business_type` `e` ON `a`.`type_id` = `e`.`type_id`
          LEFT JOIN `72crm_crm_business_status` `f` ON `a`.`status_id` = `f`.`status_id`
        #elseif(label==6)
          LEFT JOIN `72crm_crm_customer` `d` ON `a`.`customer_id` = `d`.`customer_id`
					LEFT JOIN `72crm_crm_business` `e` ON `a`.`business_id` = `e`.`business_id`
					LEFT JOIN `72crm_crm_contacts` `f` ON `a`.`contacts_id` = `f`.`contacts_id`
					LEFT JOIN `72crm_admin_user` `g` ON `a`.`company_user_id` = `g`.`user_id`
				#elseif(label==7)
          LEFT JOIN `72crm_crm_customer` `d` ON `a`.`customer_id` = `d`.`customer_id`
          LEFT JOIN `72crm_crm_contract` `e` ON `a`.`contract_id` = `e`.`contract_id`
          LEFT JOIN `72crm_crm_receivables_plan` `f` ON `a`.`plan_id` = `f`.`plan_id`
        #end
        JOIN (
          SELECT
            b.batch_id as field_batch_id
            #for(field : fieldMap)
              #if(field.value&&field.value.get("field_type")==0)
                #if(field.value.get("type")==12)
                  ,GROUP_CONCAT(if(a.name = '#(field.key)',c.name,null)) AS `#(field.key)`
                #elseif(field.value.get("type")==10)
                  ,GROUP_CONCAT(if(a.name = '#(field.key)',d.realname,null)) AS `#(field.key)`
                #else
                  ,max(CASE WHEN `a`.`name` = '#(field.key)' THEN `a`.`value` END) AS `#(field.key)`
                #end
              #end
            #end
            FROM 72crm_admin_fieldv AS a RIGHT JOIN (SELECT batch_id FROM 72crm_crm_#(realm) as a
            WHERE 1=1
            #if(batchList&&batchList.size()>0)
              and batch_id in ( #fori(batchList))
            #end
            #for(query : queryList) #if(query.get("type")==1) #(query.get("sql")) #end #end #if(orderByKey!="create_user_name"&&orderByKey!="owner_user_name") ORDER BY #(orderByKey) #(orderByType) #end #if(!field) #if(page&&limit) LIMIT #(page),#(limit) #end #end

            ) AS b ON a.batch_id = b.batch_id
            #if(fieldMap.containsKey("user"))
              left join 72crm_admin_user d on find_in_set(d.user_id,ifnull(a.value,0))
            #end
            #if(fieldMap.containsKey("dept"))
              left join 72crm_admin_dept c on find_in_set(c.dept_id,ifnull(a.value,0))
            #end
            GROUP BY b.batch_id
        ) `z` ON `a`.`batch_id` = `z`.`field_batch_id`
      ) as views
        where 1=1
         #for(query : queryList) #if(query.get("type")==2) #(query.get("sql")) #end #end
        ORDER BY
        #(orderByKey) #(orderByType)
        #if(field) #if(page&&limit) LIMIT #(page),#(limit) #end #end
    #end
    #sql ("queryCrmPageListByFieldType2")
      #(select) FROM (
        SELECT
          `a`.*,
          (SELECT realname FROM 72crm_admin_user WHERE user_id=`a`.`create_user_id`) AS `create_user_name`,
          (SELECT realname FROM 72crm_admin_user WHERE user_id=`a`.`owner_user_id`) AS `owner_user_name`,
          #if(label==3)
          `d`.`customer_name` AS `customer_name`,
          #elseif(label==4)
          `d`.`name` AS `category_name`,
          #elseif(label==5)
          `d`.`customer_name` AS `customer_name`,
	        `e`.`name` AS `type_name`,
	        `f`.`name` AS `status_name`,
	        #elseif(label==6)
          `d`.`customer_name` AS `customer_name`,
          IFNULL(`e`.`business_name`,'') AS `business_name`,
          `f`.`name` AS `contacts_name`,
          `g`.`realname` AS `company_user_name`,
          #elseif(label==7)
          `d`.`customer_name` AS `customer_name`,
          `e`.`name` AS `contract_name`,
          `e`.`num` AS `contract_num`,
          `e`.`money` AS `contract_money`,
          `f`.`num` AS `plan_num`,
          #end
          `z`.*
        FROM
        `72crm_crm_#(realm)` as `a`
        #if(label==3)
          LEFT JOIN `72crm_crm_customer` `d` ON `a`.`customer_id` = `d`.`customer_id`
        #elseif(label==4)
          LEFT JOIN `72crm_crm_product_category` `d` ON `a`.`category_id` = `d`.`category_id`
        #elseif(label==5)
          LEFT JOIN `72crm_crm_customer` `d` ON `a`.`customer_id` = `d`.`customer_id`
          LEFT JOIN `72crm_crm_business_type` `e` ON `a`.`type_id` = `e`.`type_id`
          LEFT JOIN `72crm_crm_business_status` `f` ON `a`.`status_id` = `f`.`status_id`
        #elseif(label==6)
          LEFT JOIN `72crm_crm_customer` `d` ON `a`.`customer_id` = `d`.`customer_id`
					LEFT JOIN `72crm_crm_business` `e` ON `a`.`business_id` = `e`.`business_id`
					LEFT JOIN `72crm_crm_contacts` `f` ON `a`.`contacts_id` = `f`.`contacts_id`
					LEFT JOIN `72crm_admin_user` `g` ON `a`.`company_user_id` = `g`.`user_id`
		    #elseif(label==7)
          LEFT JOIN `72crm_crm_customer` `d` ON `a`.`customer_id` = `d`.`customer_id`
          LEFT JOIN `72crm_crm_contract` `e` ON `a`.`contract_id` = `e`.`contract_id`
          LEFT JOIN `72crm_crm_receivables_plan` `f` ON `a`.`plan_id` = `f`.`plan_id`
        #end
        LEFT JOIN (
          SELECT
            a.batch_id as field_batch_id
            #for(field : fieldMap)
              #if(field.value&&field.value.get("field_type")==0)
                #if(field.value.get("type")==12)
                  ,GROUP_CONCAT(if(a.name = '#(field.key)',c.name,null)) AS `#(field.key)`
                #elseif(field.value.get("type")==10)
                  ,GROUP_CONCAT(if(a.name = '#(field.key)',d.realname,null)) AS `#(field.key)`
                #else
                  ,max(CASE WHEN `a`.`name` = '#(field.key)'  THEN `a`.`value` END) AS `#(field.key)`
                #end
              #end
            #end
          FROM
            72crm_admin_fieldv AS a
          JOIN 72crm_admin_field as f on a.field_id=f.field_id and f.label=#para(label)
          #if(fieldMap.containsKey("user"))
            left join 72crm_admin_user d on find_in_set(d.user_id,ifnull(a.value,0))
          #end
          #if(fieldMap.containsKey("dept"))
            left join 72crm_admin_dept c on find_in_set(c.dept_id,ifnull(a.value,0))
          #end
          GROUP BY a.batch_id
        ) `z` ON `a`.`batch_id` = `z`.`field_batch_id`
      ) as views
      WHERE 1=1
        #for(query : queryList)
            #(query.get("sql"))
        #end
        ORDER BY #(orderByKey) #(orderByType)
        #if(page&&limit)
          LIMIT #(page),#(limit)
        #end
    #end
    #sql ("queryCrmPageListCount")
      SELECT count(1) FROM 72crm_crm_#(realm) as a
      WHERE 1=1
      #if(batchList&&batchList.size()>0)
          and batch_id in ( #for(str:batchList) #(for.index == 0 ? "" : ",") #para(str) #end)
      #end
      #for(query : queryList)
        #if(query.get("type")==1) #(query.get("sql")) #end
      #end
    #end
    #sql ("queryHideScene")
    select scene_id,name,data from 72crm_admin_scene where type = ? and user_id = ? and is_hide = 1
    #end

    #sql ("querySystemNumber")
    select count(*) from 72crm_admin_scene where is_system = 1 and type = ? and user_id = ?
    #end

    #sql ("sort")
    update 72crm_admin_scene set is_hide = 0,sort = ? where type = ? and user_id = ? and scene_id = ?
    #end

    #sql ("queryIsHideSystem")
    select count(scene_id) as number from 72crm_admin_scene where scene_id in (
       #for(i : ids)
           #(for.index > 0 ? "," : "")#para(i)
       #end
    ) and is_system = 1
    #end

    #sql ("isHide")
    update 72crm_admin_scene set is_hide = 1,sort = 0
    where scene_id in (
       #for(i : ids)
           #(for.index > 0 ? "," : "")#para(i)
       #end
    ) and type = #para(type) and user_id = #para(userId)
    #end

    #sql ("getCustomerPageList")
    select *,
    IF(deal_status = 0,(TO_DAYS(
      IFNULL((SELECT car.create_time FROM 72crm_admin_record as car where car.types = 'crm_customer' and car.types_id = views.customer_id ORDER BY car.create_time DESC LIMIT 1),create_time))
      + CAST((SELECT value FROM 72crm_admin_config WHERE name= 'customerPoolSettingFollowupDays') as SIGNED) - TO_DAYS(NOW())
    ),'') as pool_day,
    (select count(*) from 72crm_crm_business as a where a.customer_id = views.customer_id) as business_count
    #end

    #sql("queryPutInPoolToday")
    select IFNULL(GROUP_CONCAT(customer_id),0)
    from 72crm_crm_customer as a left join 72crm_crm_owner_record as b on a.customer_id = b.type_id
    where b.type = 8 and TO_DAYS(b.create_time) = TO_DAYS(NOW())
    #end

    #sql ("queryPutInPoolTodayNum")
      select count(*)
      from 72crm_crm_customer as a left join 72crm_crm_owner_record as b on a.customer_id = b.type_id
      where b.type = 8 and TO_DAYS(b.create_time) = TO_DAYS(NOW())
    #end
    #sql ("queryBatchId")
      select batch_id from 72crm_admin_fieldv where name=#para(name) and `value` #(connector) #(value)
      #if(batchList&&batchList.size()>0)
        and batch_id in ( #fori(batchList))
      #end
    #end
#end
