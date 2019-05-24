#namespace("crm.product")
    #sql("getProductPageList")
      select * from productview
    #end
    #sql("totalRowSql")
     select count(*) from productview
    #end
    #sql("queryById")
       select CrmProductController.*, sc.name as categoryName
       from 72crm_crm_product as CrmProductController
       LEFT JOIN 72crm_crm_product_category as sc on sc.category_id = CrmProductController.category_id
       where CrmProductController.product_id = ?
    #end
    #sql("queryProductPageList")
      select sccp.product_id,scp.name as product_name,scp.`单位` as unit,sccp.price,
      sccp.sales_price,sccp.num,sccp.discount,sccp.subtotal,scp.`是否上下架` ,scp.category_name
      FROM 72crm_crm_contract_product sccp
      LEFT JOIN productview as scp on scp.product_id = sccp.product_id
      where sccp.contract_id = ?
    #end
     #sql("queryByCategoryId")
      select count(*) from 72crm_crm_product where category_id = ?
    #end
    #sql ("queryCategoryByParentId")
      select count(*) from 72crm_crm_product_category where pid = ?
    #end
    #sql("getByNum")
      select COUNT(*) from productview where num = ?
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

    #sql ("excelExport")
    select *
    from productview
    where product_id in (
        #for(i:ids)
          #(for.index > 0 ? "," : "")#para(i)
        #end
    ) order by update_time desc
    #end
#end
