package com.kakarote.crm9.erp.crm.controller;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Clear;
import com.jfinal.plugin.activerecord.Db;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.erp.admin.service.AdminFieldService;
import com.kakarote.crm9.erp.admin.service.AdminSceneService;
import com.kakarote.crm9.erp.crm.entity.CrmProduct;
import com.kakarote.crm9.erp.crm.service.CrmProductService;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.utils.R;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CrmProductController extends Controller {

    @Inject
    private CrmProductService crmProductService;

    /**
     * 分页条件查询产品
     *
     * @author zxy
     */
    public void queryList(BasePageRequest<CrmProduct> basePageRequest) {
        renderJson(R.ok().put("data", crmProductService.queryPage(basePageRequest)));
    }

    /**
     * 添加或修改产品
     *
     * @author zxy
     */
    @Permissions({"crm:product:save","crm:product:update"})
    public void saveAndUpdate() {
        String data = getRawData();
        JSONObject jsonObject = JSON.parseObject(data);
        renderJson(crmProductService.saveAndUpdate(jsonObject));
    }

    /**
     * 根据id查询产品
     *
     * @author zxy
     */
    @Permissions("crm:product:read")
    @NotNullValidate(value = "productId", message = "产品id不能为空")
    public void queryById(@Para("productId") Integer productId) {
        renderJson(crmProductService.queryById(productId));
    }

    /**
     * 根据id查删除产品
     *
     * @author zxy
     */
    @Permissions("crm:product:delete")
    @NotNullValidate(value = "productId", message = "产品id不能为空")
    public void deleteById(@Para("productId") Integer productId) {
        renderJson(crmProductService.deleteById(productId));
    }

    /**
     * 产品上下架 status 0:下架 1：上架（默认除了0之外其他都是上架）
     *
     * @author zxy
     */
    @Permissions("crm:product:status")
    public void updateStatus(@Para("ids") String ids, @Para("status") Integer status) {
        if (status == null)
        { status = 1;}
        renderJson(crmProductService.updateStatus(ids, status));
    }

    /**
     * 查询产品自定义字段
     *
     * @author zxy
     */
    public void queryField() {
        renderJson(R.ok().put("data", crmProductService.queryField()));
    }

    /**
     * @author wyq
     * 批量导出产品
     */
    @Permissions("crm:product:excelexport")
    public void batchExportExcel(@Para("ids")String productIds) throws IOException {
        List<Record> recordList = crmProductService.exportProduct(productIds);
        export(recordList);
        renderNull();
    }

    /**
     * @author wyq
     * 导出全部产品
     */
    @Permissions("crm:product:excelexport")
    public void allExportExcel(BasePageRequest basePageRequest) throws IOException{
        JSONObject jsonObject = basePageRequest.getJsonObject();
        jsonObject.fluentPut("excel","yes").fluentPut("type","4");
        AdminSceneService adminSceneService = new AdminSceneService();
        List<Record> recordList = (List<Record>)adminSceneService.filterConditionAndGetPageList(basePageRequest).get("data");
        export(recordList);
        renderNull();
    }

    private void export(List<Record> recordList) throws IOException{
        ExcelWriter writer = ExcelUtil.getWriter();
        AdminFieldService adminFieldService = new AdminFieldService();
        List<Record> fieldList = adminFieldService.list("4");
        writer.addHeaderAlias("name","产品名称");
        writer.addHeaderAlias("num","产品编码");
        writer.addHeaderAlias("category_name","产品类别");
        writer.addHeaderAlias("price","价格");
        writer.addHeaderAlias("description","产品描述");
        writer.addHeaderAlias("create_user_name","创建人");
        writer.addHeaderAlias("owner_user_name","负责人");
        writer.addHeaderAlias("create_time","创建时间");
        writer.addHeaderAlias("update_time","更新时间");
        for (Record field:fieldList){
            writer.addHeaderAlias(field.getStr("name"),field.getStr("name"));
        }
        writer.merge(8+fieldList.size(),"产品信息");
        HttpServletResponse response = getResponse();
        List<Map<String,Object>> list = new ArrayList<>();
        for (Record record : recordList){
            list.add(record.remove("batch_id","status","unit","category_id","product_id","owner_user_id","create_user_id").getColumns());
        }
        writer.write(list,true);
        for (int i=0; i < fieldList.size()+15;i++){
            writer.setColumnWidth(i,20);
        }
        //自定义标题别名
        //response为HttpServletResponse对象
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setCharacterEncoding("UTF-8");
        //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
        response.setHeader("Content-Disposition", "attachment;filename=product.xls");
        ServletOutputStream out = response.getOutputStream();
        writer.flush(out);
        // 关闭writer，释放内存
        writer.close();
    }

    /**
     * @author zxy
     * 获取导入模板
     */
    public void downloadExcel(){
        List<Record> recordList = crmProductService.queryField();
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("产品导入表");
        HSSFRow row = sheet.createRow(0);
        List<String> categoryList = Db.query("select name from 72crm_crm_product_category");
        for (int i=0;i < recordList.size();i++){
            Record record = recordList.get(i);
            String[] setting = record.get("setting");
            HSSFCell cell = row.createCell(i);
            if (record.getInt("is_null") == 1){
                cell.setCellValue(record.getStr("name")+"(*)");
            }else {
                cell.setCellValue(record.getStr("name"));
            }
            if ("产品分类".equals(record.getStr("name"))){
                setting = categoryList.toArray(new String[categoryList.size()]);
            }
            if (setting.length != 0){
                CellRangeAddressList regions = new CellRangeAddressList(0, Integer.MAX_VALUE, i, i);
                DVConstraint constraint = DVConstraint.createExplicitListConstraint(setting);
                HSSFDataValidation dataValidation = new HSSFDataValidation(regions,constraint);
                sheet.addValidationData(dataValidation);
            }
        }
        HttpServletResponse response = getResponse();
        try {
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
            response.setHeader("Content-Disposition", "attachment;filename=product_import.xls");
            wb.write(response.getOutputStream());
            wb.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        renderNull();
    }

    /**
     * @author zxy
     * 导入产品
     */
    @Permissions("crm:product:excelimport")
    public void uploadExcel(@Para("file") UploadFile file, @Para("repeatHandling") Integer repeatHandling, @Para("ownerUserId") Integer ownerUserId){
        renderJson(crmProductService.uploadExcel(file,repeatHandling,ownerUserId));
    }
    /**
     * @author zxy
     * 获取上架商品
     */
    public void queryByStatus(BasePageRequest<CrmProduct> basePageRequest){
        renderJson(crmProductService.queryByStatus(basePageRequest));
    }

}
