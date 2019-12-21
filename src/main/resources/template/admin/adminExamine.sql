#namespace("admin.examine")
  #sql("getExaminePageList")
    select * from 72crm_admin_examine where  is_deleted = ?
  #end
  #sql("totalRowSql")
   select count(examine_id) from 72crm_admin_examine where  is_deleted = ?
  #end
  #sql("updateIsDeleted")
    UPDATE 72crm_admin_examine SET `is_deleted` = 1 WHERE examine_id = ?
  #end
  #sql("queryByFlowId")
   select * from 72crm_admin_examine_step where flow_id = ? order by sort
  #end
  #sql("updateStatus")
   UPDATE 72crm_admin_examine SET `status` = ABS(`status` - 1) WHERE examine_id = ?
  #end
  #sql("queryListByStatus")
   select * from 72crm_admin_examine where status = 1 and is_deleted = 0
  #end
  #sql("getExamineById")
    select * from 72crm_admin_examine where  examine_id = ?
  #end
  #sql("getExamineByCategoryType")
    select * from 72crm_admin_examine where  category_type = ? AND status = 1 order by update_time desc limit 1
  #end
  #sql("queryAllExamine")
    select adminExamine.*,adminUser.realname from 72crm_admin_examine as adminExamine
    LEFT JOIN 72crm_admin_user as adminUser on adminUser.user_id = adminExamine.update_user_id
    where  adminExamine.status = 1
  #end
  #sql("queryExaminePage")
     select adminExamine.*,adminUser.realname as updateUserName,createUser.realname as createUserName
     from 72crm_admin_examine as adminExamine
     LEFT JOIN 72crm_admin_user as adminUser on adminUser.user_id = adminExamine.update_user_id
     LEFT JOIN 72crm_admin_user as createUser on createUser.user_id = adminExamine.create_user_id
    where  adminExamine.status != 2
  #end
  #sql("queryExaminePageTotal")
    select count(*) from 72crm_admin_examine where status != 2
  #end
#end
