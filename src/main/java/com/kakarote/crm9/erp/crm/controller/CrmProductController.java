package com.kakarote.crm9.erp.crm.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.common.annotation.Permissions;
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

import javax.servlet.http.HttpServletResponse;
import java.util.List;

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
     * @author zxy
     * 获取导入模板
     */
    public void downloadExcel(){
        List<Record> recordList = crmProductService.queryField();
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("产品导入表");
        HSSFRow row = sheet.createRow(0);
        for (int i=0;i < recordList.size();i++){
            Record record = recordList.get(i);
            String[] setting = record.get("setting");
            HSSFCell cell = row.createCell(i);
            if (record.getInt("is_null") == 0||record.getInt("isNull") == 0){
                cell.setCellValue(record.getStr("name"));
            }else if (record.getInt("is_null") == 1||record.getInt("isNull") == 1){
                cell.setCellValue(record.getStr("name")+"(*)");
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
