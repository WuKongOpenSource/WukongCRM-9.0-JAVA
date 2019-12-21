package com.kakarote.crm9.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.jfinal.aop.Aop;
import com.jfinal.aop.Inject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminRoleService;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wyq
 */
public class AuthUtil {

    public static Map<String,String> getCrmTablePara(CrmEnum Enum){
        Map<String,String> tableParaMap = new HashMap<>();
        switch(Enum){
            case CRM_CUSTOMER:
                tableParaMap.put("tableName", "72crm_crm_customer");
                tableParaMap.put("tableId", "customer_id");
                tableParaMap.put("realm","customer");
                break;
            case CRM_LEADS:
                tableParaMap.put("tableName", "72crm_crm_leads");
                tableParaMap.put("tableId", "leads_id");
                tableParaMap.put("realm","leads");
                break;
            case CRM_CONTRACT:
                tableParaMap.put("tableName", "72crm_crm_contract");
                tableParaMap.put("tableId", "contract_id");
                tableParaMap.put("realm","contract");
                break;
            case CRM_CONTACTS:
                tableParaMap.put("tableName", "72crm_crm_contacts");
                tableParaMap.put("tableId", "contacts_id");
                tableParaMap.put("realm","contacts");
                break;
            case CRM_BUSINESS:
                tableParaMap.put("tableName", "72crm_crm_business");
                tableParaMap.put("tableId", "business_id");
                tableParaMap.put("realm","business");
                break;
            case CRM_RECEIVABLES:
                tableParaMap.put("tableName", "72crm_crm_receivables");
                tableParaMap.put("tableId", "receivables_id");
                tableParaMap.put("realm","receivables");
                break;
            default:
                return null;
        }
        return tableParaMap;
    }
    public static boolean isCrmAuth(CrmEnum Enum, Integer id){
        return isCrmAuth(getCrmTablePara(Enum),id);
    }
    public static boolean isCrmAuth(Map<String,String> tablePara, Integer id){
        if(tablePara == null){
            return false;
        }
        Long userId = BaseUtil.getUserId();
        List<Integer> roles = BaseUtil.getUser().getRoles();
        List<Long> longs = Aop.get(AdminUserService.class).queryUserByAuth(userId,tablePara.get("realm"));
        StringBuilder authSql = new StringBuilder("select count(*) from ");
        authSql.append(tablePara.get("tableName")).append(" where ").append(tablePara.get("tableId")).append(" = ").append(id);
        if(! BaseConstant.SUPER_ADMIN_USER_ID.equals(userId) && !roles.contains(BaseConstant.SUPER_ADMIN_ROLE_ID)){
            if(longs != null && longs.size() > 0){
                authSql.append(" and (owner_user_id in (").append(StrUtil.join(",", longs)).append(")");
                if("72crm_crm_customer".equals(tablePara.get("tableName")) || "72crm_crm_business".equals(tablePara.get("tableName")) || "72crm_crm_contract".equals(tablePara.get("tableName"))){
                    authSql.append(" or ro_user_id like CONCAT('%,','").append(userId).append("',',%')").append(" or rw_user_id like CONCAT('%,','").append(userId).append("',',%')");
                }
                authSql.append(")");
            }
        }
        return Db.queryInt(authSql.toString()) == 0;
    }

    public static boolean oaAuth(Record record){
        boolean auth = false;
        if(record.getStr("business_ids") != null){
            auth = isCrmAuth(CrmEnum.CRM_BUSINESS,Integer.valueOf(record.getStr("business_ids")));
        }else if(record.getStr("contacts_ids") != null){
            auth = isCrmAuth(CrmEnum.CRM_CONTACTS,Integer.valueOf(record.getStr("contacts_ids")));
        }else if(record.getStr("contract_ids") != null){
            auth = isCrmAuth(CrmEnum.CRM_CONTRACT,Integer.valueOf(record.getStr("contract_ids")));
        }else if(record.getStr("customer_ids") != null){
            auth = isCrmAuth(CrmEnum.CRM_CUSTOMER,Integer.valueOf(record.getStr("customer_ids")));
        }
        return auth;
    }

    public static boolean isOaAuth(Integer type, Integer id){
        if(id == null){
            return false;
        }
        Map<String,String> tablePara = getOaTablePara(type);
        Long userId = BaseUtil.getUserId();
        List<Integer>  roleIds=Aop.get(AdminRoleService.class).queryRoleIdsByUserId(userId);

        //超级管理员角色拥有最高权限
        if (roleIds.contains(BaseConstant.SUPER_ADMIN_ROLE_ID)||userId.equals(BaseConstant.SUPER_ADMIN_USER_ID)) {
            return false;
        }
        if(tablePara == null){
            List<Long> userIds = Db.query("select create_user_id from `72crm_oa_examine` where examine_id = ? union all select b.examine_user from `72crm_oa_examine_record` as a left join `72crm_oa_examine_log` b on a.record_id = b.record_id where a.examine_id = ?", id, id);
            return ! userIds.contains(userId);
        }
        StringBuilder authSql = new StringBuilder("select count(*) from  ");
        if("72crm_task".equals(tablePara.get("tableName"))){
            List<Long> childIdList = Aop.get(AdminUserService.class).queryChileUserIds(userId, 20);
            authSql.append(tablePara.get("tableName")).append(" where ").append(tablePara.get("tableId")).append(" = ").append(id);
            if(childIdList != null && childIdList.size()>0){
                authSql.append(" and (").append(" (owner_user_id like CONCAT('%,','").append(userId).append("',',%')").append(" or main_user_id = ").append(userId).append(") ");
                childIdList.forEach(childId->{
                    authSql.append(" or (owner_user_id like CONCAT('%,','").append(childId).append("',',%')").append(" or main_user_id = ").append(childId).append(") ");
                });
                authSql.append(") ");
            }else {
                authSql.append(" and ").append(" (owner_user_id like CONCAT('%,','").append(userId).append("',',%')").append(" or main_user_id = ").append(userId).append(") ");
            }
        }else if ("72crm_task_comment".equals(tablePara.get("tableName"))) {
            authSql.append(tablePara.get("tableName")).append(" where ").append(tablePara.get("tableId")).append(" = ").append(id).append(" and user_id = ").append(userId);
        }else {
            authSql.append(tablePara.get("tableName")).append(" where ").append(tablePara.get("tableId")).append(" = ").append(id).append(" and create_user_id = ").append(userId);
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
            case 6:
                tableParaMap.put("tableName", "72crm_task_comment");
                tableParaMap.put("tableId", "comment_id");
                break;
            default:
                return null;
        }
        return tableParaMap;
    }


    public static boolean isWorkAdmin(){
        if(BaseUtil.getUserId().equals(BaseConstant.SUPER_ADMIN_USER_ID)){
           return true;
        }else {
            return BaseUtil.getUser().getRoles().contains(BaseConstant.WORK_ADMIN_ROLE_ID) || BaseUtil.getUser().getRoles().contains(BaseConstant.SUPER_ADMIN_ROLE_ID);
        }
    }

    public static  boolean isWorkAuth(String workId,String realm){
        AdminUser user = BaseUtil.getUser();
        Integer roleId = Db.queryInt("select role_id from `72crm_work_user` where work_id = ? and user_id = ?", workId, user.getUserId());
        //判断是否是超级管理员、项目管理员
        if(user.getUserId().equals(BaseConstant.SUPER_ADMIN_USER_ID) || user.getRoles().contains(BaseConstant.SUPER_ADMIN_ROLE_ID) ||user.getRoles().contains(BaseConstant.WORK_ADMIN_ROLE_ID) || (roleId != null&&roleId.equals(BaseConstant.SMALL_WORK_ADMIN_ROLE_ID))){
            return true;
        }else {
            List<String>  realmData= Db.query(Db.getSql("work.queryRealmByRoleId"), roleId);
            if(realmData == null || realmData.size() == 0){
                return false;
            }
            return realmData.contains(realm);
        }
    }

    /**
     * 团队成员是否有操作权限
     */
    public static boolean isRwAuth(Integer id, String realm){
        Long userId = BaseUtil.getUserId();
        Record record = Db.findFirst("select owner_user_id,rw_user_id from 72crm_crm_"+realm+" where "+realm+"_id = ?",id);
        String idsArr = record.getStr("rw_user_id");
        return idsArr.contains("," + userId + ",") || userId.equals(record.getLong("owner_user_id"));
    }

    /**
     * 客户接口判断公海不需要拦截
     * @param customerId
     * @return
     */
    public static boolean isPoolAuth(Integer customerId){
        Integer ownerUserId = Db.queryInt("select owner_user_id from `72crm_crm_customer` where customer_id = ?", customerId);
        if(ownerUserId==null){
            return false;
        }else {
            return AuthUtil.isCrmAuth(CrmEnum.CRM_CUSTOMER, customerId);
        }
    }

    public static List<String> queryAuth(Map<String,Object> map,String key){
        List<String> permissions=new ArrayList<>();
        map.keySet().forEach(str->{
            if(map.get(str) instanceof Map){
                permissions.addAll(queryAuth((Map<String, Object>) map.get(str),key+str+":"));
            }else {
                permissions.add(key+str);
            }
        });
        return permissions;
    }
    /**
     * 拼客户管理数据权限sql
     * @param crmEnum
     * @return
     */
    public static String getCrmAuthSql(CrmEnum crmEnum, String alias, Integer readOnly) {
        AdminUser user = BaseUtil.getUser();
        List<Integer> roles = user.getRoles();
        Long userId = user.getUserId();
        if (BaseConstant.SUPER_ADMIN_USER_ID.equals(userId) || roles.contains(BaseConstant.SUPER_ADMIN_ROLE_ID) || crmEnum.equals(CrmEnum.CRM_PRODUCT) || crmEnum.equals(CrmEnum.CRM_CUSTOMER_POOL)) {
            return "";
        }
        Map<String, String> tablePara = getCrmTablePara(crmEnum);
        StringBuilder conditions = new StringBuilder();
        List<Long> longs = Aop.get(AdminUserService.class).queryUserByAuth(userId,StrUtil.subAfter(tablePara.get("tableName"),"72crm_crm_",false));
        if(longs != null && longs.size() > 0){
            conditions.append(" and ({alias}owner_user_id in (").append(StrUtil.join(",", longs)).append(")");
            boolean b = crmEnum.equals(CrmEnum.CRM_CUSTOMER) || crmEnum.equals(CrmEnum.CRM_BUSINESS) || crmEnum.equals(CrmEnum.CRM_CONTRACT);
            if (readOnly != null && b) {
                if (0 == readOnly) {
                    conditions.append(" or {alias}rw_user_id like CONCAT('%,','").append(userId).append("',',%')");
                } else if (1 == readOnly) {
                    conditions.append(" or {alias}ro_user_id like CONCAT('%,','").append(userId).append("',',%')").append(" or {alias}rw_user_id like CONCAT('%,','").append(userId).append("',',%')");
                }
            }
            conditions.append(")");
        }
        Map<String, String> map = new HashMap<>();
        if (StrUtil.isEmpty(alias)){
            map.put("alias","");
        }else {
            map.put("alias",alias+".");
        }
        return StrUtil.format(conditions.toString(),map);
    }

    public static String getCrmAuthSql(CrmEnum crmEnum, Integer readOnly) {
        return getCrmAuthSql(crmEnum, "", readOnly);
    }
    public static String getCrmAuthSql(CrmEnum crmEnum,  String alias) {
        return getCrmAuthSql(crmEnum, alias, 1);
    }


}
