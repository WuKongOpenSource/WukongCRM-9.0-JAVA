#namespace("crm.leads")
    #sql("getLeadsPageList")
    select a.leads_id,a.leads_name,b.realname as ownerUserName
    from 72crm_crm_leads as a left join 72crm_admin_user as b on a.owner_user_id = b.user_id
    where 1=1
      #if(leadsName)
      and a.leads_name like CONCAT('%',#para(leadsName),'%')
      #end
      #if(telephone)
      and a.telephone = #para(telephone)
      #end
      #if(mobile)
      and a.mobile = #para(mobile)
      #end
    #end

    #sql("count")
    select count(*) as nameNum from leadsview where leads_name = #para(leadsName)
    #if (leadsId)
    and leads_id != #para(leadsId)
    #end
    #end

    #sql("queryById")
    select a.*,a.leads_name as name,b.realname as ownerUserName
    from 72crm_crm_leads as a left join 72crm_admin_user as b on a.owner_user_id = b.user_id
    where leads_id = ?
    #end

    #sql("queryByName")
    select * from leadsview
    where leads_name = ?
    #end

    #sql("deleteByIds")
    delete from 72crm_crm_leads where leads_id = ?
    #end

    #sql ("updateOwnerUserId")
    update 72crm_crm_leads
    set owner_user_id = #para(ownerUserId),followup = 0
    where leads_id in (
        #for(i:ids)
          #(for.index > 0 ? "," : "")#para(i)
        #end
    )
    #end

    #sql ("updateIsTransform")
    update 72crm_crm_leads set is_transform = 1 where leads_id = ?
    #end

    #sql ("getRecord")
    select a.record_id,b.img as user_img,b.realname,a.create_time,a.content,a.category,a.next_time,a.batch_id
    from 72crm_admin_record as a inner join 72crm_admin_user as b
    where a.create_user_id = b.user_id and types = 'crm_leads' and types_id = ? order by a.create_time desc
    #end

    #sql ("excelExport")
    SELECT
      `a`.*,
      `b`.`realname` AS `create_user_name`,
      `c`.`realname` AS `owner_user_name`,
      `z`.*
    FROM
      `72crm_crm_leads` as `a`
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
        FROM 72crm_admin_fieldv AS a RIGHT JOIN (SELECT batch_id FROM 72crm_crm_leads WHERE leads_id in (#for(i:ids) #(for.index > 0 ? "," : "")#para(i) #end)) AS b ON a.batch_id = b.batch_id
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

    #sql ("queryBatchIdByIds")
    select batch_id from 72crm_crm_leads where leads_id in (
        #for(i:ids)
          #(for.index > 0 ? "," : "")#para(i)
        #end
    )
    #end
#end
