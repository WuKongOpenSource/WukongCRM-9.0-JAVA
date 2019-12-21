#namespace("crm.leads")
  #sql("queryById")
    select a.*,a.leads_name as name,b.realname as ownerUserName
    from 72crm_crm_leads as a left join 72crm_admin_user as b on a.owner_user_id = b.user_id
    where leads_id = ?
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

  #sql ("getRecord")
    select a.record_id,b.img as user_img,b.realname,a.create_time,a.content,a.category,a.next_time,a.batch_id
    from 72crm_admin_record as a inner join 72crm_admin_user as b
    where a.create_user_id = b.user_id and types = 'crm_leads' and types_id = ? order by a.create_time desc
  #end

  #sql ("queryBatchIdByIds")
    select batch_id from 72crm_crm_leads where leads_id in (#fori(ids))
  #end
#end
