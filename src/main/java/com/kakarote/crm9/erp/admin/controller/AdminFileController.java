package com.kakarote.crm9.erp.admin.controller;

import com.jfinal.core.paragetter.Para;
import com.kakarote.crm9.erp.admin.entity.AdminFile;
import com.kakarote.crm9.erp.admin.service.AdminFileService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;

import java.io.File;

public class AdminFileController extends Controller {
    @Inject
    private AdminFileService adminFileService;

    public void index(){
        renderJson(R.ok());
    }

    /**
     * @author zhangzhiwei
     * 上传附件
     *
     */
    public void upload(){
        String prefix=BaseUtil.getDate();
        renderJson(adminFileService.upload(getFile("file",prefix),getPara("batchId"),getPara("type"),prefix,getRequest()));
    }

    /**
     * @author zhangzhiwei
     * 通过批次ID查询
     */
    public void queryByBatchId(){
        renderJson(R.ok().put("data", adminFileService.queryByBatchId(getPara("batchId"))));
    }

    /**
     * @author zhangzhiwei
     * 通过ID查询
     */
    public void queryById(){
        renderJson(adminFileService.queryById(getPara("id")));
    }

    /**
     * @author zhangzhiwei
     * 通过ID删除
     */
    public void removeById(){
        renderJson(adminFileService.removeById(getPara("id")));
    }

    /**
     * @author zhangzhiwei
     * 通过批次ID删除
     */
    public void removeByBatchId(){
        adminFileService.removeByBatchId(getPara("batchId"));
        renderJson(R.ok());
    }

    /**
     * @author zhangzhiwei
     * 重命名文件
     */
    public void renameFileById(){
        AdminFile file=new AdminFile();
        file.setFileId(getInt("fileId"));
        file.setName(getPara("name"));
        renderJson(adminFileService.renameFileById(file)?R.ok():R.error());
    }

    /**
     * 下载文件
     */
    public void downFile(@Para("fileId") String fileId) {
        fileId="/file/downFile?fileId="+fileId;
        AdminFile adminFile = AdminFile.dao.findFirst("SELECT * FROM `72crm_admin_file` where file_path=? LIMIT 0,1",fileId);
        if (adminFile != null) {
            renderFile(new File(adminFile.getPath()));
            return;
        }
        renderNull();
    }
}
