#namespace("crm.product")
    #sql("queryById")
       select ccp.*, sc.name as categoryName
       from 72crm_crm_product as ccp
       LEFT JOIN 72crm_crm_product_category as sc on sc.category_id = ccp.category_id
       where ccp.product_id = ?
    #end
    #sql("queryProductPageList")
        SELECT *,
        (select value from `72crm_admin_fieldv` a  where a.batch_id = c.batch_id and a.name = '单位') as unit,
        (select value from `72crm_admin_fieldv` b  where b.batch_id = c.batch_id and b.name = '是否上下架') as `是否上下架`
        from
        (select sccp.product_id,scp.name as product_name,sccp.price,sccp.sales_price,sccp.num,sccp.discount,sccp.subtotal,c.name as category_name,scp.batch_id
        FROM 72crm_crm_contract_product sccp
               LEFT JOIN 72crm_crm_product as scp on scp.product_id = sccp.product_id
                left join `72crm_crm_product_category` as c on scp.category_id = c.category_id
        where sccp.contract_id = ?) as c
    #end
     #sql("queryByCategoryId")
      select count(*) from 72crm_crm_product where category_id = ?
    #end
    #sql ("queryCategoryByParentId")
      select count(*) from 72crm_crm_product_category where pid = ?
    #end
    #sql("getByNum")
      select COUNT(*) from 72crm_crm_product where num = ?
    #end
    #sql("querySubtotalByContractId")
      select distinct ccc.discount_rate,
      round(((select SUM(subtotal) from 72crm_crm_contract_product WHERE contract_id = ccc.contract_id)/100*(100 - ccc.discount_rate) ),2)as money
      from 72crm_crm_contract as ccc
      where ccc.contract_id = ?
    #end
    #sql("querySubtotalByBusinessId")
      select distinct ccc.discount_rate,
      round((( select SUM(subtotal) from 72crm_crm_business_product WHERE business_id = ccc.business_id ) /100*(100 - ccc.discount_rate) ),2)as money
      from 72crm_crm_business as ccc
      where ccc.business_id = ?
    #end
    #sql ("queryInformationById")
      select a.name,b.name as category_name,a.num,a.price,a.description,a.batch_id
      from `72crm_crm_product` a
             left join `72crm_crm_product_category` b on a.category_id = b.category_id
      where product_id = ?
    #end
#end
