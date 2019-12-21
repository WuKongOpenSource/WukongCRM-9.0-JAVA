package com.kakarote.crm9.erp.crm.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Aop;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.jfinal.kit.Kv;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.entity.AdminFieldSort;
import com.kakarote.crm9.erp.admin.service.AdminFieldService;
import com.kakarote.crm9.erp.admin.service.AdminSceneService;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.common.CrmExcelUtil;
import com.kakarote.crm9.erp.crm.entity.CrmProduct;
import com.kakarote.crm9.erp.crm.service.CrmProductService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
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

    @Inject
    private AdminFieldService adminFieldService;

    @Inject
    private AdminSceneService adminSceneService;

    /**
     * @author wyq
     * 查看列表页
     */
    @Permissions({"crm:product:index"})
    public void queryPageList(BasePageRequest basePageRequest) {
        JSONObject jsonObject = basePageRequest.getJsonObject().fluentPut("type", 4);
        basePageRequest.setJsonObject(jsonObject);
        renderJson(adminSceneService.filterConditionAndGetPageList(basePageRequest));
    }

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
    @Permissions({"crm:product:save", "crm:product:update"})
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
        Integer number = Db.queryInt("select count(1) from `72crm_crm_product` where product_id = ? and status != 3",productId);
        if (number == 0) {
            renderJson(R.error("产品已删除"));
            return;
        }
        renderJson(R.ok().put("data",crmProductService.queryById(productId)));
    }

    /**
     * 根据id查删除产品
     *
     * @author zxy
     */
    @Permissions("crm:product:delete")
    @NotNullValidate(value = "productIds", message = "产品id不能为空")
    public void deleteByIds(@Para("productIds") String productIds) {
        renderJson(crmProductService.deleteByIds(productIds));
    }

    /**
     * 产品上下架 status 0:下架 1：上架（默认除了0之外其他都是上架）
     *
     * @author zxy
     */
    @Permissions("crm:product:status")
    public void updateStatus(@Para("ids") String ids, @Para("status") Integer status) {
        if (status == null) {
            status = 1;
        }
        renderJson(crmProductService.updateStatus(ids, status));
    }

    /**
     * @author wyq
     * 批量导出产品
     */
    @Permissions("crm:product:excelexport")
    public void batchExportExcel(BasePageRequest basePageRequest) {
        JSONObject jsonObject=basePageRequest.getJsonObject();
        String ids=jsonObject.getString("ids");
        JSONObject data =new JSONObject();
        data.fluentPut("productExport",new JSONObject().fluentPut("name","product_id").fluentPut("condition","in").fluentPut("value", ids));
        jsonObject.fluentPut("data",data).fluentPut("search","").fluentPut("type",4);
        basePageRequest.setJsonObject(jsonObject);
        JSONObject resultData = (JSONObject)adminSceneService.getCrmPageList(basePageRequest).get("data");
        List<Record> recordList = resultData.getJSONArray("list").toJavaList(Record.class);
        export(recordList);
        renderNull();
    }

    /**
     * @author wyq
     * 导出全部产品
     */
    @Permissions("crm:product:excelexport")
    public void allExportExcel(BasePageRequest basePageRequest){
        JSONObject jsonObject = basePageRequest.getJsonObject();
        jsonObject.fluentPut("excel", "yes").fluentPut("type", "4");
        AdminSceneService adminSceneService = new AdminSceneService();
        JSONObject data = (JSONObject)adminSceneService.filterConditionAndGetPageList(basePageRequest).get("data");
        List<Record> recordList = data.getJSONArray("list").toJavaList(Record.class);
        export(recordList);
        renderNull();
    }

    private void export(List<Record> recordList){
        try (ExcelWriter writer = ExcelUtil.getWriter()) {
            AdminFieldSort adminFieldSort = new AdminFieldSort();
            adminFieldSort.setLabel(CrmEnum.CRM_PRODUCT.getType());
            List<Record> headList = Aop.get(AdminFieldService.class).queryListHead(adminFieldSort);
            headList.forEach(head -> writer.addHeaderAlias(StrUtil.toUnderlineCase(head.getStr("fieldName")), head.getStr("name")));
            writer.merge(headList.size() - 1, "产品信息");
            HttpServletResponse response = getResponse();
            List<Map<String, Object>> list = new ArrayList<>();
            if (recordList.size() == 0) {
                Record record = new Record();
                headList.forEach(head -> record.set(StrUtil.toUnderlineCase(head.getStr("fieldName")), ""));
                list.add(record.getColumns());
            }
            recordList.forEach(record -> list.add(record.getColumns()));
            writer.setOnlyAlias(true);
            writer.write(list, true);
            writer.setRowHeight(0, 20);
            writer.setRowHeight(1, 20);
            for (int i = 0; i < headList.size(); i++) {
                writer.setColumnWidth(i, 20);
            }
            Cell cell = writer.getCell(0, 0);
            CellStyle cellStyle = cell.getCellStyle();
            cellStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font font = writer.createFont();
            font.setBold(true);
            font.setFontHeightInPoints((short) 16);
            cellStyle.setFont(font);
            cell.setCellStyle(cellStyle);
            //自定义标题别名
            //response为HttpServletResponse对象
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
            response.setHeader("Content-Disposition", "attachment;filename=product.xls");
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
        } catch (Exception e) {
            Log.getLog(getClass()).error("导出产品错误：",e);
        }
    }

    /**
     * @author zxy
     * 获取导入模板
     */
    public void downloadExcel() {
        List<Record> recordList = adminFieldService.queryAddField(CrmEnum.CRM_PRODUCT);
        recordList.removeIf(record -> "file".equals(record.getStr("formType")) || "checkbox".equals(record.getStr("formType")) || "user".equals(record.getStr("formType")) || "structure".equals(record.getStr("formType")));
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("产品导入表");
        sheet.setDefaultRowHeight((short)400);
        CellStyle textStyle = wb.createCellStyle();
        DataFormat format = wb.createDataFormat();
        textStyle.setDataFormat(format.getFormat("@"));
        for (int i=0;i < recordList.size();i++){
            sheet.setDefaultColumnStyle(i,textStyle);
            sheet.setColumnWidth(i,20*256);
        }
        HSSFRow titleRow = sheet.createRow(0);
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short)16);
        cellStyle.setFont(font);
        titleRow.createCell(0).setCellValue("产品导入模板(*)为必填项");
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        titleRow.getCell(0).setCellStyle(cellStyle);
        CellRangeAddress region = new CellRangeAddress(0,0 , 0, recordList.size()-1);
        sheet.addMergedRegion(region);
        List<String> categoryList = Db.query("select name from 72crm_crm_product_category");
        try {
            HSSFRow row = sheet.createRow(1);
            for (int i = 0; i < recordList.size(); i++) {
                Record record = recordList.get(i);
                String[] setting = record.get("setting");
                HSSFCell cell = row.createCell(i);
                if (record.getInt("is_null") == 1) {
                    cell.setCellValue(record.getStr("name") + "(*)");
                } else {
                    cell.setCellValue(record.getStr("name"));
                }
                if ("产品类型".equals(record.getStr("name"))) {
                    setting = categoryList.toArray(new String[categoryList.size()]);
                }
                if (setting.length != 0) {
                    CellRangeAddressList regions = new CellRangeAddressList(2, Integer.MAX_VALUE, i, i);
                    DVConstraint constraint = DVConstraint.createExplicitListConstraint(setting);
                    HSSFDataValidation dataValidation = new HSSFDataValidation(regions, constraint);
                    sheet.addValidationData(dataValidation);
                }
            }
            HttpServletResponse response = getResponse();

            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
            response.setHeader("Content-Disposition", "attachment;filename=product_import.xls");
            wb.write(response.getOutputStream());
        } catch (Exception e) {
            Log.getLog(getClass()).error("error:", e);
        } finally {
            try {
                wb.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        renderNull();
    }

    /**
     * @author zxy
     * 导入产品
     */
    @Permissions("crm:product:excelimport")
    public void uploadExcel() {
        String prefix= BaseUtil.getDate();
        UploadFile file=getFile("file",prefix);
        Integer repeatHandling=getParaToInt("repeatHandling");
        Long ownerUserId=getParaToLong("ownerUserId");
        CrmExcelUtil excelUtil=new CrmExcelUtil();
        Long messageId = excelUtil.addWork(CrmEnum.CRM_PRODUCT,file.getFile().getAbsolutePath(),ownerUserId,repeatHandling);
        renderJson(R.ok().put("data",messageId));
    }

    /**
     * @author zxy
     * 获取上架商品
     */
    public void queryByStatus(BasePageRequest<CrmProduct> basePageRequest) {
        renderJson(crmProductService.queryByStatus(basePageRequest));
    }

}
