package com.kakarote.crm9.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.jfinal.aop.Aop;
import com.jfinal.aop.Inject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.erp.crm.common.CrmEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wyq
 */
public class AuthUtil {
    @Inject
    private AdminUserService adminUserService;

    public boolean dataAuth( String table,String fieldName,Integer dataId){
        List<Integer> roles = BaseUtil.getUser().getRoles();
        List<Integer> dataAuth = Db.query("select data_type from 72crm_admin_role where role_id in ("+CollectionUtil.join(roles,",")+")");
        List<Long> userIdList = new ArrayList<>();
        Integer deptId = BaseUtil.getUser().getDeptId();
        if (dataAuth.contains(5)){
            return true;
        }else if (dataAuth.contains(4)){
            List<Integer> deptIdList = adminUserService.queryChileDeptIds(deptId, BaseConstant.AUTH_DATA_RECURSION_NUM);
            deptIdList.add(deptId);
            List<Long> userIds = Db.query("select user_id from 72crm_admin_user where dept_id in ("+CollectionUtil.join(deptIdList,",")+")");
            userIdList.addAll(userIds);
        }else if (dataAuth.contains(3)){
            userIdList.addAll(Db.query("select user_id from 72crm_admin_user where dept_id = ?",deptId));
        }else if (dataAuth.contains(2)){
            userIdList.addAll(adminUserService.queryChileUserIds(BaseUtil.getUserId(),BaseConstant.AUTH_DATA_RECURSION_NUM));
        }else if (dataAuth.contains(1)){
            userIdList.add(BaseUtil.getUserId());
        }
        Long ownerUserId = Db.queryLong("select owner_user_id from 72crm_crm_"+table+" where "+fieldName+" = ?",dataId);
        return userIdList.contains(ownerUserId);
    }

    public static Map<String,String> getCrmTablePara(String urlPrefix){
        Map<String,String> tableParaMap = new HashMap<>();
        switch(urlPrefix){
            case "CrmCustomer":
                tableParaMap.put("tableName", "72crm_crm_customer");
                tableParaMap.put("tableId", "customer_id");
                break;
            case "CrmLeads":
                tableParaMap.put("tableName", "72crm_crm_leads");
                tableParaMap.put("tableId", "leads_id");
                break;
            case "CrmContract":
                tableParaMap.put("tableName", "72crm_crm_contract");
                tableParaMap.put("tableId", "contract_id");
                break;
            case "CrmContacts":
                tableParaMap.put("tableName", "72crm_crm_contacts");
                tableParaMap.put("tableId", "contacts_id");
                break;
            case "CrmBusiness":
                tableParaMap.put("tableName", "72crm_crm_business");
                tableParaMap.put("tableId", "business_id");
                break;
            case "CrmReceivables":
                tableParaMap.put("tableName", "72crm_crm_receivables");
                tableParaMap.put("tableId", "receivables_id");
                break;
            default:
                return null;
        }
        return tableParaMap;
    }

    public static boolean isCrmAuth(Map<String,String> tablePara, Integer id){
        if(tablePara == null){
            return false;
        }
        Long userId = BaseUtil.getUserId();
        List<Long> longs = Aop.get(AdminUserService.class).queryUserByAuth(userId);
        StringBuilder authSql = new StringBuilder("select count(*) from ");
        authSql.append(tablePara.get("tableName")).append(" where ").append(tablePara.get("tableId")).append(" = ").append(id);
        if(longs != null && longs.size() > 0){
            authSql.append(" and owner_user_id in (").append(StrUtil.join(",", longs)).append(")");
            if("72crm_crm_customer".equals(tablePara.get("tableName")) || "72crm_crm_business".equals(tablePara.get("tableName")) || "72crm_crm_contract".equals(tablePara.get("tableName"))){
                authSql.append(" or ro_user_id like CONCAT('%,','" + userId + "',',%')" + " or rw_user_id like CONCAT('%,','" + userId + "',',%')");
            }
        }
        return Db.queryInt(authSql.toString()) == 0;
    }

    public static boolean oaAnth(Record record){
        boolean auth = false;
        if(record.getStr("business_ids") != null){
            auth = isCrmAuth(getCrmTablePara(CrmEnum.BUSINESS_TYPE_KEY.getSign()),Integer.valueOf(record.getStr("business_ids")));
        }else if(record.getStr("contacts_ids") != null){
            auth = isCrmAuth(getCrmTablePara(CrmEnum.CONTACTS_TYPE_KEY.getSign()),Integer.valueOf(record.getStr("contacts_ids")));
        }else if(record.getStr("contract_ids") != null){
            auth = isCrmAuth(getCrmTablePara(CrmEnum.CONTRACT_TYPE_KEY.getSign()),Integer.valueOf(record.getStr("contract_ids")));
        }else if(record.getStr("customer_ids") != null){
            auth = isCrmAuth(getCrmTablePara(CrmEnum.CUSTOMER_TYPE_KEY.getSign()),Integer.valueOf(record.getStr("customer_ids")));
        }
        return auth;
    }

    public static boolean isOaAuth(Integer type, Integer id){
        if(id == null){
            return false;
        }
        Map<String,String> tablePara = getOaTablePara(type);
        Long userId = BaseUtil.getUserId();
        if(tablePara == null){
            List<Long> userIds = Db.query("select create_user_id from `72crm_oa_examine` where examine_id = ? union all select b.examine_user from `72crm_oa_examine_record` as a left join `72crm_oa_examine_log` b on a.record_id = b.record_id where a.examine_id = ?", id, id);
            return ! userIds.contains(userId);
        }
        StringBuilder authSql = new StringBuilder("select count(*) from  ");
        if(!"72crm_task".equals(tablePara.get("tableName"))){
            authSql.append(tablePara.get("tableName")).append(" where ").append(tablePara.get("tableId")).append(" = ").append(id).append(" and create_user_id = ").append(userId);
        }else {
            authSql.append(tablePara.get("tableName")).append(" where ").append(tablePara.get("tableId")).append(" = ").append(id).append(" and (create_user_id = ").append(userId).append(" or owner_user_id like CONCAT('%,','" + userId + "',',%')" ).append(" or main_user_id = ").append(userId).append(")");
        }
        return Db.queryInt(authSql.toString()) == 0;
    }

    private static Map<String,String> getOaTablePara(Integer type){
        Map<String,String> tableParaMap = new HashMap<>();
        switch(type){
            case 1:
                tableParaMap.put("tableName", "72crm_oa_log");
                tableParaMap.put("tableId", "log_id");
                break;
            case 2:
                tableParaMap.put("tableName", "72crm_oa_event");
                tableParaMap.put("tableId", "event_id");
                break;
            case 3:
                tableParaMap.put("tableName", "72crm_oa_announcement");
                tableParaMap.put("tableId", "announcement_id");
                break;
            case 4:
                tableParaMap.put("tableName", "72crm_task");
                tableParaMap.put("tableId", "task_id");
                break;
            case 5:
                tableParaMap.put("tableName", "72crm_oa_examine");
                tableParaMap.put("tableId", "examine_id");
                break;
            default:
                return null;
        }
        return tableParaMap;
    }

}
