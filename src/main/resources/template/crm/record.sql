#namespace("crm.record")
  #sql("getProperties")
    select  distinct COLUMN_NAME , COLUMN_COMMENT,'4' as type FROM INFORMATION_SCHEMA.COLUMNS  where table_name = '72crm_crm_product'
    union all
    select  distinct COLUMN_NAME , COLUMN_COMMENT,'3' as type FROM INFORMATION_SCHEMA.COLUMNS  where table_name = '72crm_crm_contacts'
    union all
    select  distinct COLUMN_NAME , COLUMN_COMMENT,'2' as type FROM INFORMATION_SCHEMA.COLUMNS  where table_name = '72crm_crm_customer'
    union all
    select  distinct COLUMN_NAME , COLUMN_COMMENT,'1' as type FROM INFORMATION_SCHEMA.COLUMNS  where table_name = '72crm_crm_leads'
    union all
    select  distinct COLUMN_NAME , COLUMN_COMMENT,'6' as type FROM INFORMATION_SCHEMA.COLUMNS  where table_name = '72crm_crm_contract'
    union all
    select  distinct COLUMN_NAME , COLUMN_COMMENT,'7' as type FROM INFORMATION_SCHEMA.COLUMNS  where table_name = '72crm_crm_receivables'
    union all
    select  distinct COLUMN_NAME , COLUMN_COMMENT,'5' as type FROM INFORMATION_SCHEMA.COLUMNS  where table_name = '72crm_crm_business'
  #end

  #sql ("queryRecordList")
  select a.*,b.realname,b.img
  from 72crm_crm_action_record a left join 72crm_admin_user b on a.create_user_id = b.user_id
  where action_id = ? and types = ? order by create_time desc
  #end
#end
