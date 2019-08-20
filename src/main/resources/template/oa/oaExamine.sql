#namespace("oa.examine")
  #sql("myInitiate")
    select a.*,b.examine_status,b.record_id as examine_record_id,b.examine_step_id ,c.category_id,c.title as categoryTitle
    from 72crm_oa_examine a left join 72crm_oa_examine_record b on a.examine_id = b.examine_id left join 72crm_oa_examine_category c on a.category_id = c.category_id
    where a.create_user_id = #para(userId)
    #if(categoryId!=null&&categoryId!="")
    and a.category_id = #para(categoryId)
    #end
    #if(status!=null&&status!="")
    and  b.examine_status = #para(status)
    #end
    #if(startTime!=null && endTime!=null)
    and a.create_time between #para(startTime) and  #para(endTime)
    #end
    group by a.examine_id,b.record_id order by  a.create_time desc
  #end
  #sql("myOaExamine")
    select a.*,b.examine_status,b.record_id as examine_record_id,b.examine_step_id,c.category_id,c.title as categoryTitle  from 72crm_oa_examine a  left join  72crm_oa_examine_record b on a.examine_id = b.examine_id left join 72crm_oa_examine_category c on a.category_id = c.category_id left join 72crm_oa_examine_log d on d.record_id = b.record_id
    where 1 = 1
      #if(categoryId!=null&&categoryId!="")
      and a.category_id = #para(categoryId)
      #end
      #if(status==1)
      and (d.examine_user = #para(userId) and d.examine_status = 0 and ifnull(b.examine_step_id,1) = ifnull(d.examine_step_id,1) and d.is_recheck !=1)
      #end
      #if(status==2)
      and (d.examine_user = #para(userId) and d.examine_status != 0)
      #end
      #if(startTime!=null && endTime!=null)
      and a.create_time between #para(startTime) and  #para(endTime)
      #end
    group by a.examine_id,b.examine_status,b.record_id
     #if(status==1)
      order by  a.create_time desc
     #end
     #if(status==2)
      order by  d.examine_time desc
     #end

  #end
  #sql("examineCheck")
      select * from 72crm_oa_examine_check where  is_checked = 1
        #if(userIds)
        and check_user_id in (
          #for(x:userIds)
            #(for.index == 0 ? "" : ",")
            #para(x)
          #end)
        #end
  #end
  #sql("queryExamineStepByNextExamineIdOrderByStepId")
  SELECT * FROM 72crm_oa_examine_step
  WHERE category_id = ? and step_num =  (SELECT step_num FROM 72crm_oa_examine_step where step_id = ?) + 1
  #end
  #sql("queryNowadayExamineLogByRecordIdAndStepId")
    select * from 72crm_oa_examine_log where record_id = ? and examine_step_id = ? and examine_user = ? and is_recheck != 1
  #end
  #sql("queryExamineLogByRecordId")
    select soel.* ,
           (SELECT realname from 72crm_admin_user WHERE user_id = soel.create_user) createUserName,
           (SELECT realname from 72crm_admin_user WHERE user_id = soel.examine_user) examineUserName
    from 72crm_oa_examine_log as soel where soel.record_id = #para(recordId) and examine_time is not null order by examine_time desc
  #end
  #sql("queryExamineRecordById")
    SELECT saer.* ,sau.img,sau.realname
    from 72crm_oa_examine_record as  saer
           LEFT JOIN 72crm_admin_user as sau on sau.user_id = saer.create_user
    WHERE saer.record_id = ?
  #end
  #sql("queryExamineLogByRecordIdAndStepIdAndStatus")
    select sael.* ,
           (SELECT realname from 72crm_admin_user WHERE user_id = sael.create_user) createUserName,
           (SELECT realname from 72crm_admin_user WHERE user_id = sael.examine_user) examineUserName
    from 72crm_oa_examine_log as sael
    where record_id = ? and examine_step_id = ?  order by create_time
  #end
  #sql("queryUserByUserId")
    SELECT DISTINCT saud.user_id, saud.realname, 0 as examine_status from 72crm_admin_user as sau
    LEFT JOIN 72crm_admin_user as saud on saud.user_id = sau.parent_id
    WHERE sau.user_id = ?
  #end
  #sql("queryUserByUserIdAndStatus")
    SELECT DISTINCT user_id, realname , 0 as examine_status,img from 72crm_admin_user
    WHERE user_id = ?
  #end
  #sql("queryUserByRecordId")
    select sael.examine_time  as examineTime , sael.examine_status,sau.realname,sau.user_id,sau.img
    from 72crm_oa_examine_log as sael LEFT JOIN 72crm_admin_user as sau on sau.user_id = sael.examine_user
    where sael.record_id = ? and  sael.examine_status = 3 and sael.is_recheck != 1
  #end
  #sql("queryExamineLogAndUserByRecordId")
    select sael.examine_time  as examineTime , sael.examine_status,sau.realname,sau.img,sael.log_id
    from 72crm_oa_examine_log as sael LEFT JOIN 72crm_admin_user as sau on sau.user_id = sael.examine_user
    where sael.record_id = ? and sael.is_recheck != 1 order by sael.create_time
  #end
  #sql("queryUserByRecordIdAndStepIdAndStatus")
      select sael.examine_time  as examineTime , sael.examine_status,sau.realname,sau.user_id , sau.img
      from 72crm_oa_examine_log as sael LEFT JOIN 72crm_admin_user as sau on sau.user_id = sael.examine_user
      where record_id = ? and examine_step_id = ? and sael.is_recheck != 1 order by sael.create_time
  #end
  #sql("queryExamineLog")
    select * from 72crm_oa_examine_log where record_id = #para(recordId)  and examine_user = #para(examineUser)
      #if(stepId)
         and examine_step_id = #para(stepId)
      #end
      and examine_status = 0  and is_recheck != 1 order by create_time desc
  #end
  #sql("queryExamineTypeByRecordId")
    select examine_type from 72crm_oa_examine_category where category_id = (select 72crm_oa_examine.category_id from 72crm_oa_examine where examine_id = (select  72crm_oa_examine_record.examine_id from 72crm_oa_examine_record where record_id = ?))
  #end
  #sql("queryExamineLogByRecordIdByStep")
    select sael.order_id,ases.step_num as order_id , sau.user_id , sau.realname , sau.img ,sael.examine_status,sael.examine_time,sael.remarks
    from 72crm_oa_examine_log as sael LEFT JOIN 72crm_admin_user as sau on sau.user_id = sael.examine_user LEFT JOIN 72crm_oa_examine_step as ases on ases.step_id = sael.examine_step_id
    where sael.record_id = ? AND sael.examine_status != 0 order by sael.create_time
  #end
  #sql("queryExamineLogByRecordIdByStep1")
    select  sael.order_id,sau.user_id , sau.realname , sau.img,sael.examine_status,sael.examine_time,sael.remarks,sael.is_recheck
    from 72crm_oa_examine_log as sael  LEFT JOIN 72crm_admin_user as sau on sau.user_id = sael.examine_user
    where sael.record_id = ? AND sael.examine_status != 0 order by sael.create_time
  #end
  #sql("queryRecordAndId")
    select create_user, sau.realname , sau.user_id , sau.img, aser.create_time as examineTime, 5 as examine_status
    from 72crm_oa_examine_record  as aser LEFT JOIN 72crm_admin_user as sau on sau.user_id = aser.create_user
    where record_id = ?
  #end
  #sql("queryUserByUserIdAnd")
    SELECT DISTINCT user_id, realname ,img, 0 as examine_status from 72crm_admin_user
    WHERE user_id = ?
  #end
  #sql("queryRecordByUserIdAndStatus")
    SELECT DISTINCT user_id, realname  ,img, 5 as examine_status, #para(examineTime) as examineTime from 72crm_admin_user
    WHERE user_id = #para(create_user)
  #end
  #sql("queryExamineLogAndUserByLogId")
      select sael.examine_time  as examineTime , sael.examine_status,sau.realname ,sau.img , sael.log_id
      from 72crm_oa_examine_log as sael LEFT JOIN 72crm_admin_user as sau on sau.user_id = sael.examine_user
      where sael.log_id = ? and sael.is_recheck != 1 order by sael.create_time
  #end
  #sql("queryCountByStepId")
    SELECT  DISTINCT ((SELECT COUNT(log_id) FROM 72crm_oa_examine_log WHERE record_id = #para(recordId) and examine_step_id = #para(stepId))
  - (SELECT COUNT(log_id) FROM 72crm_oa_examine_log WHERE record_id = #para(recordId) and examine_step_id = #para(stepId) and examine_status = 2 )) as toCount
    FROM 72crm_admin_examine_log
  #end
  #sql("queryExamineRelation")
    select a.*,b.examine_status,b.record_id as examine_record_id,b.examine_step_id ,c.category_id,c.title as categoryTitle from 72crm_oa_examine_relation h left join 72crm_oa_examine a on h.examine_id = a.examine_id left join 72crm_oa_examine_record b on a.examine_id = b.examine_id left join 72crm_oa_examine_category c on a.category_id = c.category_id where 1 = 2
    #if(businessIds)
      or h.business_ids like concat('%,',#para(businessIds),',%')
    #end
    #if(contactsIds)
      or h.contacts_ids like concat('%,',#para(contactsIds),',%')
    #end
    #if(contractIds)
      or h.contract_ids like concat('%,',#para(contractIds),',%')
    #end
    #if(customerIds)
      or h.customer_ids like concat('%,',#para(customerIds),',%')
    #end
   #end
   #sql("queryTravel")
    select travel_id as travelId, examine_id as examineId, start_address as startAddress, start_time as startTime, end_address as endAddress, end_time as endTime, traffic, stay, diet, other, money, vehicle, trip, duration, description, batch_id as batchId from 72crm_oa_examine_travel where examine_id = ?
   #end
   #sql("queryAllExamineCategoryList")
      select category_id,title,type from 72crm_oa_examine_category where
        case when user_ids != '' or dept_ids != '' then (user_ids like concat('%,',#para(userId),',%') or dept_ids like concat('%,',#para(deptId),',%')) and is_deleted = 0 and status = 1
             else is_deleted = 0 and status = 1
        end
   #end
   #sql("queryExamineById")
      select a.*,b.title as category,b.type,c.examine_status  from 72crm_oa_examine as a left join 72crm_oa_examine_category b on a.category_id = b.category_id left join `72crm_oa_examine_record` c on a.examine_id = c.examine_id where a.examine_id = ?
   #end
  #sql("queryFieldByBatchId")
    select a.field_id,a.field_name,a.name,a.type,a.input_tips,a.options,a.is_unique,a.is_null,b.value,a.field_type from 72crm_admin_field a left join `72crm_admin_fieldv` b on a.field_id = b.field_id where a.examine_category_id = ? and b.batch_id = ? order by sorting
  #end
  #sql("queryExamineCategoryByType")
    SELECT * FROM 72crm_oa_examine_category WHERE type= ? limit 0,1
  #end
#end
