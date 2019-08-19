#namespace("admin.scene")
    #sql ("queryScene")
    select a.scene_id,a.data,a.name,if(b.default_id is null,0,1) as is_default,a.is_system,a.bydata
    from 72crm_admin_scene as a left join 72crm_admin_scene_default as b on a.scene_id = b.scene_id
    where a.type = ? and a.user_id = ? and is_hide = 0 order by a.sort asc
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
    (TO_DAYS(
      IFNULL((SELECT car.create_time FROM 72crm_admin_record as car where car.types = 'crm_customer' and car.types_id = customerview.customer_id ORDER BY car.create_time DESC LIMIT 1),create_time))
      + CAST((SELECT value FROM 72crm_admin_config WHERE name= 'customerPoolSettingFollowupDays') as SIGNED) - TO_DAYS(NOW())
    ) as pool_day,
    (select count(*) from 72crm_crm_business as a where a.customer_id = customerview.customer_id) as business_count
    #end
#end
