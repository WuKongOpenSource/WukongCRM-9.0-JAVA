#namespace("oa.log")
  #sql("queryList")
    SELECT
	    a.*, b.dept_id,
      b.realname,
      b.img as userImg,
      soal.customer_ids,
      soal.contacts_ids,
      soal.business_ids,
      soal.contract_ids
    FROM
	    72crm_oa_log as a
    LEFT JOIN 72crm_admin_user as b on a.create_user_id=b.user_id
    LEFT JOIN 72crm_oa_log_relation as soal on soal.log_id=a.log_id
    WHERE 1 = 1
    #if(create_user_id)
      and a.create_user_id = #para(create_user_id)
    #end
    #if(by&&!create_user_id)
      and (a.send_user_ids like concat("%,",#para(send_user_ids),",%") or a.send_dept_ids like concat("%,",#para(send_dept_ids),",%")
        #if(userIds.size()>0)
            or a.create_user_id in (
              #for(i:userIds)
                #(for.index > 0 ? "," : "")#para(i)
              #end
            )
        #end
        )
    #end
    #if(create_time)
      and to_days(a.create_time) = to_days(#para(create_time))
    #end
    #if(category_id)
      and category_id = #para(category_id)
    #end
    #if(logId)
      and a.log_id = #para(logId)
    #end
    #if(by==3)
      and read_user_ids not like concat("%,",#para(userId),",%")
    #end
    order by log_id desc
  #end

  #sql ("queryLogRelation")
  SELECT
	    a.*, b.dept_id,
      b.realname,
      b.img as userImg,
      soal.customer_ids,
      soal.contacts_ids,
      soal.business_ids,
      soal.contract_ids
    FROM 72crm_oa_log as a left join 72crm_oa_log_relation as d on a.log_id = d.log_id
    LEFT JOIN 72crm_admin_user as b on a.create_user_id=b.user_id
     LEFT JOIN 72crm_oa_log_relation as soal on soal.log_id=a.log_id
    WHERE 1 = 2
    #if(businessIds)
      or d.business_ids like concat('%,',#para(businessIds),',%')
    #end
    #if(contactsIds)
      or d.contacts_ids like concat('%,',#para(contactsIds),',%')
    #end
    #if(contractIds)
      or d.contract_ids like concat('%,',#para(contractIds),',%')
    #end
    #if(customerIds)
      or d.customer_ids like concat('%,',#para(customerIds),',%')
    #end
  #end
#end
