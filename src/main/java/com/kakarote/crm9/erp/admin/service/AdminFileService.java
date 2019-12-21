package com.kakarote.crm9.erp.admin.service;

import cn.hutool.core.util.ClassLoaderUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.jfinal.config.Constants;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.kakarote.crm9.erp.admin.entity.AdminFile;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class AdminFileService {
    public final static Prop prop = PropKit.use("config/crm9-config.txt");
    /**
     * @param file    文件
     * @param batchId 批次ID
     */
    public R upload(UploadFile file, String batchId, String fileType, String prefix, HttpServletRequest request) {
        if (batchId == null || "".equals(batchId)) {
            batchId = IdUtil.simpleUUID();
        }
        AdminFile adminFile = new AdminFile();
        adminFile.setBatchId(batchId);
        adminFile.setCreateTime(new Date());
        adminFile.setCreateUserId(BaseUtil.getUser().getUserId());
        adminFile.setPath(file.getFile().getAbsolutePath());
        if(ClassLoaderUtil.isPresent("com.jfinal.server.undertow.UndertowServer")){
            adminFile.setFilePath(BaseUtil.getIpAddress(request) + prefix + "/" + file.getFileName());
        }else {
            adminFile.setFilePath(BaseUtil.getIpAddress(request)+new Constants().getBaseUploadPath()+"/"+ prefix + "/" + file.getFileName());
        }
        adminFile.setName(file.getFileName());
        if(StrUtil.isNotBlank(fileType)){
            adminFile.setFileType(fileType);
        }
        adminFile.setSize(file.getFile().length());
        return adminFile.save() ? R.ok().put("batchId", batchId).put("name",file.getFileName()).put("url", adminFile.getFilePath()).put("size",file.getFile().length()/1000+"KB").put("file_id",adminFile.getFileId()) : R.error();
    }

    /**
     * 通过批次ID查询
     *
     * @param batchId 批次ID
     */
    public void queryByBatchId(String batchId,Record record) {
        if (batchId == null||"".equals(batchId)) {
            record.set("img",new ArrayList<>()).set("file",new ArrayList<>());
            return;
        }
        List<AdminFile> adminFiles=AdminFile.dao.find(Db.getSql("admin.file.queryByBatchId"), batchId);

        Map<String, List<AdminFile>> collect = adminFiles.stream().collect(Collectors.groupingBy(AdminFile::getFileType));
        collect.forEach(record::set);
        if(!record.getColumns().containsKey("img")||record.get("img")==null){
            record.set("img",new ArrayList<>());
        }
        if(!record.getColumns().containsKey("file")||record.get("file")==null){
            record.set("file",new ArrayList<>());
        }

    }

    public List<AdminFile> queryByBatchId(String batchId) {
        if (batchId == null) {
            return new ArrayList<>();
        }
        return AdminFile.dao.find(Db.getSql("admin.file.queryByBatchId"), batchId);
    }
    /**
     * 通过ID查询
     *
     * @param id 文件ID
     */
    public R queryById(String id) {
        if (id == null) {
            return R.error("id参数为空");
        }
        return R.ok().put("data", AdminFile.dao.findById(id));
    }

    /**
     * 通过ID删除
     *
     * @param id 文件ID
     */
    @SuppressWarnings("all")
    @Before(Tx.class)
    public R removeById(String id) {
        if (id == null) {
            return R.error("id参数为空");
        }
        AdminFile adminFile=AdminFile.dao.findById(id);
        if(adminFile!=null){
            File file=new File(adminFile.getPath());
            if(file.exists()&&!file.isDirectory()){
                file.delete();
                adminFile.delete();
            }
        }
        return R.ok();
    }

    /**
     * 通过批次ID删除
     *
     * @param batchId 批次ID
     */
    public void removeByBatchId(String batchId) {
        if(StrUtil.isEmpty(batchId)){
            return;
        }
        List<String> paths=Db.query(Db.getSql("admin.file.queryPathByBatchId"), batchId);

        paths.stream().map(File::new).filter(file -> file.exists() && !file.isDirectory()).forEach(File::delete);
        Db.deleteById("72crm_admin_file","batch_id",batchId);
    }
    public boolean renameFileById(AdminFile file){
        return Db.update("72crm_admin_file","file_id",file.toRecord());
    }
}
