#namespace("crm.receivables")
     #sql("getReceivablesPageList")
      select * from receivablesview
     #end
     #sql("totalRowSql")
      select count(*) from receivablesview
     #end
     #sql("queryById")
        select  scr.* ,scc.customer_name as customerName,scco.num as contractNum,sccp.num as planNum
         from 72crm_crm_receivables as scr
        LEFT JOIN 72crm_crm_customer as scc on scc.customer_id = scr.customer_id
         LEFT JOIN 72crm_crm_contract as scco on scco.contract_id = scr.contract_id
         LEFT JOIN 72crm_crm_receivables_plan as sccp on sccp.receivables_id = scr.receivables_id
        where scr.receivables_id = ?
     #end
     #sql ("deleteByIds")
     delete from 72crm_crm_receivables where receivables_id = ? and check_status != 1 and check_status != 2
     #end
     #sql("queryReceivablesPageList")
       select  rec.receivables_id,rec.number as receivables_num,rec.contract_name as contract_name,scco.money as contract_money
                ,rec.owner_user_name,
                case rec.check_status
                when 1 then '审核中'
                when 3 then '审核未通过'
                when 2 then '审核通过'
                when 4 then '已撤回'
                ELSE '未审核' END
							as check_status,rec.return_time,rec.money as receivables_money,rec.plan_num
        FROM receivablesview as rec
        LEFT JOIN 72crm_crm_contract as scco on scco.contract_id = rec.contract_id
        where rec.contract_id = ?
     #end
     #sql("queryReceivablesByContractIds")
       select * from 72crm_crm_receivables where contract_id in (
            #for(contractId:contractIds)
              #(for.index == 0 ? "" : ",")
                  #para(contractId)
            #end
       )
     #end
     #sql("queryReceivablesById")
        select rb.* ,scc.money as contract_money ,saf.value as receivable_way
        from receivablesview as rb
        LEFT JOIN 72crm_crm_contract as scc on scc.contract_id = rb.contract_id
        LEFT JOIN 72crm_admin_fieldv as saf on saf.batch_id = rb.batch_id AND saf.name = '回款方式'
        where rb.receivables_id = #para(id)
     #end
     #sql("queryReceivablesByContractId")
       select * from 72crm_crm_receivables where contract_id = ?
     #end
     #sql ("queryByNumber")
       select * from 72crm_crm_receivables where number = ?
     #end
     #sql ("updateCheckStatusById")
      update 72crm_crm_receivables set check_status = ? where receivables_id = ?
    #end
#end
