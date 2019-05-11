#namespace("admin.examinecategory")
  #sql("queryListByStatus")
    select * from 72crm_admin_examine_category where status = 1 and is_deleted = 0
  #end
   #sql("updateStatus")
    UPDATE 72crm_admin_examine_category SET `status` = ABS(`status` - 1) WHERE category_id = ?
  #end
  #sql("queryListByIsDeleted")
   select * from 72crm_admin_examine_category where  is_deleted = 0
  #end
#end
