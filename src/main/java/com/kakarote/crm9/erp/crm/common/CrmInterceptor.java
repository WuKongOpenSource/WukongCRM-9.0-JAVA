package com.kakarote.crm9.erp.crm.common;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.erp.crm.entity.*;
import com.kakarote.crm9.utils.AuthUtil;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class CrmInterceptor implements Interceptor {
    @Override
    public void intercept(Invocation inv){
        Controller controller = inv.getController();
        HttpServletRequest request = controller.getRequest();
        String requestURI = request.getRequestURI();
        String[] split = requestURI.split("/");
        Long userId = BaseUtil.getUserId();
        boolean flag = false;
        Map<String,String> tablePara =  AuthUtil.getCrmTablePara(CrmEnum.parse(split[1]));
        if(! userId.equals(BaseConstant.SUPER_ADMIN_USER_ID)){
            if(tablePara != null){
                if("addOrUpdate".equals(split[2]) || "saveAndUpdate".equals(split[2])){
                    String rawData = controller.getRawData();
                    JSONObject jsonObject = JSON.parseObject(rawData);
                    if("72crm_crm_customer".equals(tablePara.get("tableName"))){
                        CrmCustomer entity = jsonObject.getObject("entity", CrmCustomer.class);
                        if(entity.getCustomerId() != null){
                            flag =  AuthUtil.isCrmAuth(tablePara, entity.getCustomerId());
                        }
                    }else if("72crm_crm_leads".equals(tablePara.get("tableName"))){
                        CrmLeads entity = jsonObject.getObject("entity", CrmLeads.class);
                        if(entity.getLeadsId() != null){
                            flag =   AuthUtil.isCrmAuth(tablePara, entity.getLeadsId());
                        }
                    }else if("72crm_crm_contract".equals(tablePara.get("tableName"))){
                        CrmContract entity = jsonObject.getObject("entity", CrmContract.class);
                        if(entity.getContractId() != null){
                            flag =   AuthUtil.isCrmAuth(tablePara, entity.getContractId());
                        }
                    }else if("72crm_crm_contacts".equals(tablePara.get("tableName"))){
                        CrmContacts entity = jsonObject.getObject("entity", CrmContacts.class);
                        if(entity.getContactsId() != null){
                            flag =   AuthUtil.isCrmAuth(tablePara, entity.getContactsId());
                        }
                    }else if("72crm_crm_business".equals(tablePara.get("tableName"))){
                        CrmBusiness entity = jsonObject.getObject("entity", CrmBusiness.class);
                        if(entity.getBusinessId() != null){
                            flag =   AuthUtil.isCrmAuth(tablePara, entity.getBusinessId());
                        }
                    }else if("72crm_crm_receivables".equals(tablePara.get("tableName"))){
                        CrmReceivables entity = jsonObject.getObject("entity", CrmReceivables.class);
                        if(entity.getReceivablesId() != null){
                            flag =   AuthUtil.isCrmAuth(tablePara,entity.getReceivablesId());
                        }
                    }
                }else if("deleteByIds".equals(split[2])){
                    String[] next = controller.getParaMap().values().iterator().next();
                    String[] idsArr = next[0].split(",");
                    for(String id : idsArr){
                        if(StrUtil.isNotEmpty(id)){
                            flag =   AuthUtil.isCrmAuth(tablePara, Integer.valueOf(id));
                        }
                    }
                }else if("queryById".equals(split[2])){
                    //客户公海单独处理
                    if(! "CrmCustomer".equals(split[1])){
                        String[] next = controller.getParaMap().values().iterator().next();
                        if(next != null && next.length > 0){
                            flag =   AuthUtil.isCrmAuth(tablePara, Integer.valueOf(next[0]));
                        }
                    }
                }
                if(flag){
                    if("queryById".equals(split[2])){
                        controller.renderJson(R.ok().put("data",new Record().set("dataAuth",0)));
                        return ;
                    }else {
                        controller.renderJson(R.error("无权操作"));
                        return;
                    }

                }
            }
        }
        inv.invoke();
    }
}
