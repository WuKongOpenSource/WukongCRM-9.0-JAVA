#namespace("admin.user")
    #sql("queryByUserName")
      select a.*,(SELECT name FROM 72crm_admin_dept WHERE dept_id = a.dept_id) as deptName,(SELECT realname FROM 72crm_admin_user WHERE user_id=a.parent_id) as parentName from 72crm_admin_user as a where a.username = ?  limit 1
    #end
    #sql("queryUserList")
        select a.realname,a.username,a.user_id,a.sex,a.mobile,a.email,e.name as deptName,a.status,a.create_time,a.dept_id,
       a.post,a.parent_id,a.img,
       (select b.realname from 72crm_admin_user b where b.user_id = a.parent_id) as parentName,
       (select group_concat(d.role_id) from 72crm_admin_user_role as c left join 72crm_admin_role as d on c.role_id = d.role_id where c.user_id = a.user_id) as roleId,
       (select group_concat(d.role_name) from 72crm_admin_user_role as c left join 72crm_admin_role as d on c.role_id = d.role_id where c.user_id = a.user_id) as roleName
        from 72crm_admin_user a left join
        72crm_admin_dept e on a.dept_id = e.dept_id
        #if(roleId)
        left join 72crm_admin_user_role  f on a.user_id = f.user_id
        #end
        where  1=1
        #if(name)
          and a.realname like concat('%', #para(name), '%')
        #end
        #if(deptId.size()>0)
          and a.dept_id in (
          #for(x:deptId)
          #(for.index == 0 ? "" : ",")
              #para(x)
          #end
          )
        #end
        #if(status)
          and a.status = #para(status)
        #end
        #if(roleId)
        and f.role_id = #para(roleId)
        #end
    #end
    #sql("querySuperior")
      select id,realname from 72crm_admin_user where 1 = 1
      #if(name)
        and realname like concat('%', #para(name), '%')
      #end
    #end
    #sql("queryByIds")
      select user_id,realname,img from 72crm_admin_user where user_id in (#fori(ids))
    #end
    #sql("queryUserByRealName")
      select   au.realname,au.mobile,au.post as postName ,ad.name as deptName from 72crm_admin_user as au
      LEFT JOIN 72crm_admin_dept as ad on au.dept_id = ad.dept_id
      where 1=1
      #if(deptId!=null&&deptId!="")
        and au.dept_id = #para(deptId)
      #end
      #if(name!=null&&name!="")
        and au.realname like concat('%', #para(name), '%')
      #end
    #end
    #sql("queryUserCustomer")
         SELECT sau.realname,
    (select COUNT(customer_id) FROM 72crm_crm_customer where owner_user_id = sau.user_id) as khs,
    (select COUNT(customer_id) FROM 72crm_crm_customer where owner_user_id = sau.user_id AND deal_status = 1) as  cjkhs,
    (IFNULL((select COUNT(customer_id) FROM 72crm_crm_customer where owner_user_id = sau.user_id AND deal_status = 1) /
     (select COUNT(customer_id) FROM 72crm_crm_customer where owner_user_id = sau.user_id) * 100,0)) as khcjl,
    IFNULL((SELECT SUM(money) FROM 72crm_crm_contract where owner_user_id = sau.user_id),0) as htje,
    IFNULL((SELECT SUM(money) FROM 72crm_crm_receivables where owner_user_id = sau.user_id),0) as hkje,
    (IFNULL((SELECT SUM(money) FROM 72crm_crm_contract where owner_user_id = sau.user_id),0) -
    IFNULL((SELECT SUM(money) FROM 72crm_crm_receivables where owner_user_id = sau.user_id),0)) as whkje
    FROM 72crm_admin_user as sau
    WHERE sau.status = 1
     #end
    #sql("queryUserByUserId")
      select a.*,(SELECT name FROM 72crm_admin_dept WHERE dept_id = a.dept_id) as deptName,(SELECT realname FROM 72crm_admin_user WHERE user_id=a.parent_id) as parentName from 72crm_admin_user as a where a.user_id = ?
    #end
    #sql("queryUserIdByDeptId")
      select DISTINCT user_id from 72crm_admin_user where dept_id in (
        #for(x:deptIds)
          #(for.index == 0 ? "" : ",")
              #para(x)
        #end
      )
    #end
    #sql("queryUserByDeptId")
      select user_id as id,realname from 72crm_admin_user where dept_id =?
    #end

    #sql("queryAllUserByDeptId")
        select user_id as id,realname  from 72crm_admin_user where dept_id in (
            #for(i : ids)
                #(for.index > 0 ? "," : "")#para(i)
            #end
        )
    #end

    #sql ("updateScene")
      delete from 72crm_admin_scene where user_id in (#fori(ids))
    #end
#end
