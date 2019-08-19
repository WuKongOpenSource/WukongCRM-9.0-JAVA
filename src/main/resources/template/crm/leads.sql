#namespace("crm.leads")
    #sql("getLeadsPageList")
    select leads_id,leads_name,owner_user_name from leadsview where 1=1
      #if(leadsName)
      and leads_name like CONCAT('%',#para(leadsName),'%')
      #end
      #if(telephone)
      and telephone = #para(telephone)
      #end
      #if(mobile)
      and mobile = #para(mobile)
      #end
    #end

    #sql("count")
    select count(*) as nameNum from leadsview where leads_name = #para(leadsName)
    #if (leadsId)
    and leads_id != #para(leadsId)
    #end
    #end

    #sql("queryById")
    select *,leads_name as name from leadsview
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
    select leads_name,线索来源,客户行业,客户级别,next_time,telephone,mobile,address,remark,create_user_name,owner_user_name,create_time,update_time
    from leadsview
    where leads_id in (
        #for(i:ids)
          #(for.index > 0 ? "," : "")#para(i)
        #end
    ) order by update_time desc
    #end

    #sql ("queryBatchIdByIds")
    select batch_id from 72crm_crm_leads where leads_id in (
        #for(i:ids)
          #(for.index > 0 ? "," : "")#para(i)
        #end
    )
    #end
#end
