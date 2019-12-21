package com.kakarote.crm9.erp.admin.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.aop.Inject;
import com.jfinal.core.paragetter.Para;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.annotation.RequestBody;
import com.kakarote.crm9.erp.admin.entity.AdminConfig;
import com.kakarote.crm9.erp.admin.entity.AdminMenu;
import com.kakarote.crm9.erp.admin.service.AdminFileService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.jfinal.core.Controller;
import com.jfinal.kit.Kv;
import com.jfinal.upload.UploadFile;

import java.util.List;

/**
 * 系统配置
 * @author hmb
 */
public class AdminSysConfigController extends Controller {

    /**
     * 系统设置KEY
     */
    private static final String SYS_CONFIG_KEY = "SYS_CONFIG";

    @Inject
    private AdminFileService adminFileService;

    /**
     * 设置系统配置
     * @author zhangzhiwei
     */
    @Permissions("manage:system:update")
    @Before(Tx.class)
    public void setSysConfig(){
        String prefix=BaseUtil.getDate();
        UploadFile file = getFile("file", prefix);
        Kv kv = getKv();
        if(file!=null){
            R r=adminFileService.upload(file,null,"file","/"+prefix,getRequest());
            kv.set("logo",r.get("url"));
        }
        Db.deleteById("72crm_admin_config","name",SYS_CONFIG_KEY);
        AdminConfig adminConfig=new AdminConfig();
        adminConfig.setStatus(1);
        adminConfig.setName(SYS_CONFIG_KEY);
        adminConfig.setValue(kv.toJson());
        adminConfig.setDescription("系统配置");
        renderJson(R.isSuccess(adminConfig.save()));
    }

    /**
     * 查询系统配置
     * @author zhangzhiwei
     */
    @Clear
    public void querySysConfig(){
        AdminConfig adminConfig=AdminConfig.getConfig(SYS_CONFIG_KEY);
        if(adminConfig==null|| StrUtil.isEmpty(adminConfig.getValue())){
            adminConfig=new AdminConfig();
            adminConfig.setValue("{\"name\":\"\",\"logo\":\"\"}");
        }
        renderJson(R.ok().put("data",JSON.parseObject(adminConfig.getValue())));
    }

    /**
     * 查询模块启用设置
     * @author wyq
     */
    @Permissions("manage:configSet:read")
    public void queryModuleSetting(){
        List<Record> recordList = Db.find("select setting_id,name as module,status,value as type,description as name from 72crm_admin_config where name in ('oa','crm','project','hrm','jxc') ");
        renderJson(R.ok().put("data",recordList));
    }

    /**
     * 设置模块启用
     * @author wyq
     */
    @Permissions("manage:configSet:update")
    public void setModuleSetting(@Para("") AdminConfig adminConfig){
        renderJson(R.isSuccess(adminConfig.update()));
    }
}
