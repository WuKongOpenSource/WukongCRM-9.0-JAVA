#namespace("oa.backLog")
  #sql ("queryEventNum")
  select count(*) from 72crm_oa_event
  where TO_DAYS(start_time) <= TO_DAYS(NOW()) and TO_DAYS(end_time) >= TO_DAYS(NOW())
  and (create_user_id = ? or owner_user_ids like CONCAT('%',?,'%'))
  #end

  #sql ("queryTaskNum")
  select count(*) from 72crm_task where status = 1 and (main_user_id = ? or owner_user_id like CONCAT('%,',?,',%'))
  #end

  #sql ("queryAnnouncementNum")
  select count(*) from 72crm_oa_announcement
  where TO_DAYS(start_time) <= TO_DAYS(NOW()) and TO_DAYS(end_time) >= TO_DAYS(NOW())
  and (read_user_ids not like CONCAT('%',?,'%') or read_user_ids is null)
  #end

  #sql ("queryLogNum")
  select count(*) from 72crm_oa_log
  where read_user_ids not like CONCAT('%,',?,',%') and (send_user_ids like CONCAT('%,',?,',%') or send_dept_ids like CONCAT('%,',?,',%'))
  #end

  #sql ("queryExamineNum")
  select count(*) from 72crm_oa_examine_record as a left join 72crm_oa_examine_log as b ON a.record_id = b.record_id
  where b.examine_user = ? and b.examine_status = 0 and ifnull(a.examine_step_id, 1) = ifnull(b.examine_step_id, 1) and b.is_recheck != 1
  #end
#end
