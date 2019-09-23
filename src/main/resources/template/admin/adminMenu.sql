#namespace("admin.menu")
    #sql("queryMenuByUserId")
      SELECT  c.realm,c.menu_id,c.parent_id FROM 72crm_admin_user_role as a
      right JOIN 72crm_admin_role_menu as b on a.role_id=b.role_id
      right JOIN 72crm_admin_menu as c on b.menu_id=c.menu_id
      WHERE a.user_id=?
    #end
    #sql("queryAllMenu")
      SELECT  c.realm,c.menu_id,c.parent_id FROM 72crm_admin_menu as c
    #end
    #sql("queryMenuByParentId")
      select * from 72crm_admin_menu where parent_id = ?
    #end
    #sql("queryWorkMenuByRoleId")
    SELECT  c.realm,c.menu_id,c.parent_id from
      72crm_admin_role_menu as b
        LEFT JOIN 72crm_admin_menu as c on b.menu_id=c.menu_id
    WHERE b.role_id = ?
    #end
#end


