#namespace("crm.receivables")
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
        select  rec.receivables_id,rec.number as receivables_num,scco.name contract_name,scco.money as contract_money
            ,b.realname as owner_user_name,
            rec.check_status,rec.return_time,rec.money as receivables_money,a.num as plan_num
        FROM `72crm_crm_receivables` as rec left join `72crm_crm_receivables_plan` a on rec.receivables_id = a.receivables_id
        left join 72crm_crm_contract as scco on scco.contract_id = rec.contract_id
        left join `72crm_admin_user` b on rec.owner_user_id = b.user_id
        where rec.contract_id = ?;
     #end
     #sql("queryReceivablesByContractIds")
       select * from 72crm_crm_receivables where contract_id in (#fori(contractIds))
     #end
     #sql("queryReceivablesById")
       select a.*,b.customer_name,c.num as contract_num,c.name as contract_name,c.money as contract_money,d.realname as owner_user_name,e.num as plan_num from
       (select * from 72crm_crm_receivables where receivables_id = ?)as a left join 72crm_crm_customer as b on a.customer_id = b.customer_id
       left join 72crm_crm_contract as c on a.contract_id = c.contract_id
       left join 72crm_admin_user as d on a.owner_user_id = d.user_id
       left join 72crm_crm_receivables_plan e on a.plan_id = e.plan_id
     #end
     #sql ("queryByNumber")
       select count(*) from 72crm_crm_receivables where number = ?
     #end
     #sql ("updateCheckStatusById")
      update 72crm_crm_receivables set check_status = ? where receivables_id = ?
     #end
     #sql("queryReceivablesByRecordId")
      SELECT number,receivables_id,owner_user_id FROM 72crm_crm_receivables WHERE examine_record_id=? limit 0,1
     #end
#end
