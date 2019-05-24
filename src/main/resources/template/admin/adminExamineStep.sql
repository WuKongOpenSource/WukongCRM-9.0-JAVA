#namespace("admin.examineStep")
   #sql("queryExamineStepByExamineId")
        select * from 72crm_admin_examine_step where  examine_id = ?
   #end
   #sql("deleteExamineStepByExamineId")
        DELETE  FROM  72crm_admin_examine_step WHERE examine_id = ?
   #end
   #sql("queryExamineStepByExamineIdOrderByStepNum")
        SELECT * FROM 72crm_admin_examine_step WHERE examine_id = ? ORDER BY step_num LIMIT 0,1
   #end
   #sql("queryExamineStepByNextExamineIdOrderByStepId")
        SELECT * FROM 72crm_admin_examine_step WHERE examine_id = ?
        and step_num =  (SELECT step_num FROM 72crm_admin_examine_step where step_id = ?) + 1
   #end
   #sql("queryExamineRecordById")
     SELECT saer.* ,sau.img,sau.realname from 72crm_admin_examine_record as  saer
     LEFT JOIN 72crm_admin_user as sau on sau.user_id = saer.create_user
     WHERE saer.record_id = ?
   #end
#end
