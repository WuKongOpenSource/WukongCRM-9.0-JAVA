package com.kakarote.crm9.erp.admin.controller;

import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.kakarote.crm9.erp.admin.entity.AdminFile;
import com.kakarote.crm9.erp.admin.entity.AdminMessage;
import com.kakarote.crm9.erp.crm.common.CrmExcelUtil;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;

import java.io.File;

/**
 * 消息提醒
 * @author zhangzhiwei
 */
public class AdminMessageController extends Controller {

    /**
     * 查询当前导入数量
     * @param messageId 消息ID
     * @author zhangzhiwei
     */
    public void queryImportNum(@Para("messageId") Long messageId){
        Integer num = CrmExcelUtil.getThreadMapNum(messageId);
        renderJson(R.ok().put("data",num));
    }

    /**
     * 终止当前导入
     * @param messageId 消息ID
     * @author zhangzhiwei
     */
    public void stopImport(@Para("messageId") Long messageId){
        AdminMessage adminMessage=AdminMessage.dao.findById(messageId);
        if(adminMessage==null||!adminMessage.getCreateUser().equals(BaseUtil.getUserId())){
            renderJson(R.noAuth());
            return;
        }
        CrmExcelUtil.interrupt(messageId);
        renderJson(R.ok());
    }

    /**
     * 下载错误数据
     * @param messageId 消息ID
     * @author zhangzhiwei
     */
    public void downImportError(@Para("messageId") Long messageId){
        AdminMessage adminMessage=AdminMessage.dao.findById(messageId);
        if(adminMessage==null){
            renderNull();
            return;
        }
        AdminFile adminFile=AdminFile.dao.findById(adminMessage.getTypeId());
        if(adminFile!=null&&adminFile.getCreateUserId().equals(BaseUtil.getUserId())){
            File file=new File(adminFile.getPath());
            if(file.exists()){
                renderFile(file,"import_error.xlsx");
                return;
            }
        }
        renderNull();
    }

    /**
     * 查询当前导入详情
     * @param messageId 消息ID
     * @author zhangzhiwei
     */
    public void queryImportInfo(@Para("messageId") Long messageId){
        AdminMessage adminMessage=AdminMessage.dao.findById(messageId);
        if(adminMessage!=null){
            renderJson(R.ok().put("totalSize",adminMessage.getTitle()).put("errSize",adminMessage.getContent()));
        }else {
            renderJson(R.ok().put("totalSize",0).put("errSize",0));
        }
    }

}
