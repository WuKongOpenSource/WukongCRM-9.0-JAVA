#namespace("crm.record")
  #sql("getProperties")
    select  COLUMN_NAME , COLUMN_COMMENT,'4' as type FROM INFORMATION_SCHEMA.COLUMNS  where table_name = '72crm_crm_product'
    union all
    select  COLUMN_NAME , COLUMN_COMMENT,'3' as type FROM INFORMATION_SCHEMA.COLUMNS  where table_name = '72crm_crm_contacts'
    union all
    select  COLUMN_NAME , COLUMN_COMMENT,'2' as type FROM INFORMATION_SCHEMA.COLUMNS  where table_name = '72crm_crm_customer'
    union all
    select  COLUMN_NAME , COLUMN_COMMENT,'1' as type FROM INFORMATION_SCHEMA.COLUMNS  where table_name = '72crm_crm_leads'
    union all
    select  COLUMN_NAME , COLUMN_COMMENT,'6' as type FROM INFORMATION_SCHEMA.COLUMNS  where table_name = '72crm_crm_contract'
    union all
    select  COLUMN_NAME , COLUMN_COMMENT,'7' as type FROM INFORMATION_SCHEMA.COLUMNS  where table_name = '72crm_crm_receivables'
    union all
    select  COLUMN_NAME , COLUMN_COMMENT,'5' as type FROM INFORMATION_SCHEMA.COLUMNS  where table_name = '72crm_crm_business'
  #end
#end
