package com.kakarote.crm9.erp.crm.controller;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;
import com.kakarote.crm9.common.annotation.LoginFormCookie;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.entity.AdminRecord;
import com.kakarote.crm9.erp.admin.service.AdminFieldService;
import com.kakarote.crm9.erp.admin.service.AdminSceneService;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.entity.CrmContacts;
import com.kakarote.crm9.erp.crm.service.CrmContactsService;
import com.kakarote.crm9.utils.AuthUtil;
import com.kakarote.crm9.utils.R;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
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

public class CrmContactsController extends Controller {

    @Inject
    private CrmContactsService crmContactsService;

    @Inject
    private AdminFieldService adminFieldService;

    @Inject
    private AdminSceneService adminSceneService;

    /**
     * @author wyq
     * 查看列表页
     */
    @Permissions({"crm:contacts:index"})
    public void queryPageList(BasePageRequest basePageRequest) {
        JSONObject jsonObject = basePageRequest.getJsonObject().fluentPut("type", 3);
        basePageRequest.setJsonObject(jsonObject);
        renderJson(adminSceneService.filterConditionAndGetPageList(basePageRequest));
    }

    /**
     * @author wyq
     * 分页条件查询联系人
     */
    public void queryList(BasePageRequest<CrmContacts> basePageRequest) {
        renderJson(R.ok().put("data", crmContactsService.queryList(basePageRequest)));
    }

    /**
     * @author wyq
     * 根据id查询联系人
     */
    @Permissions("crm:contacts:read")
    public void queryById(@Para("contactsId") Integer contactsId) {
        renderJson(R.ok().put("data", crmContactsService.queryById(contactsId)));
    }

    /**
     * @author wyq
     * 根据联系人名称查询
     */
    public void queryByName(@Para("name") String name) {
        renderJson(R.ok().put("data", crmContactsService.queryByName(name)));
    }

    /**
     * @author wyq
     * 根据联系人id查询商机
     */
    public void queryBusiness(BasePageRequest<CrmContacts> basePageRequest) {
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.CONTACTS_TYPE_KEY.getSign()), basePageRequest.getData().getContactsId());
        if (auth) {
            renderJson(R.noAuth());
            return;
        }
        renderJson(crmContactsService.queryBusiness(basePageRequest));
    }

    /**
     * @author wyq
     * 联系人关联商机
     */
    public void relateBusiness(@Para("contactsId") Integer contactsId, @Para("businessIds") String businessIds) {
        renderJson(crmContactsService.relateBusiness(contactsId, businessIds));
    }

    /**
     * @author wyq
     * 联系人解除关联商机
     */
    public void unrelateBusiness(@Para("contactsId") Integer contactsId, @Para("businessIds") String businessIds) {
        renderJson(crmContactsService.unrelateBusiness(contactsId, businessIds));
    }

    /**
     * @author wyq
     * 新建或更新联系人
     */
    @Permissions({"crm:contacts:save", "crm:contacts:update"})
    public void addOrUpdate() {
        JSONObject jsonObject = JSON.parseObject(getRawData());
        renderJson(crmContactsService.addOrUpdate(jsonObject));
    }

    /**
     * @author wyq
     * 根据id删除联系人
     */
    @Permissions("crm:contacts:delete")
    public void deleteByIds(@Para("contactsIds") String contactsIds) {
        renderJson(crmContactsService.deleteByIds(contactsIds));
    }

    /**
     * @author wyq
     * 联系人转移
     */
    @Permissions("crm:contacts:transfer")
    @NotNullValidate(value = "contactsIds", message = "联系人id不能为空")
    @NotNullValidate(value = "newOwnerUserId", message = "新负责人不能为空")
    public void transfer(@Para("") CrmContacts crmContacts) {
        renderJson(crmContactsService.transfer(crmContacts));
    }


    /**
     * @author wyq
     * 添加跟进记录
     */
    @NotNullValidate(value = "typesId", message = "联系人id不能为空")
    @NotNullValidate(value = "content", message = "内容不能为空")
    @NotNullValidate(value = "category", message = "跟进类型不能为空")
    public void addRecord(@Para("") AdminRecord adminRecord) {
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.CONTACTS_TYPE_KEY.getSign()), adminRecord.getTypesId());
        if (auth) {
            renderJson(R.noAuth());
            return;
        }
        renderJson(crmContactsService.addRecord(adminRecord));
    }

    /**
     * @author wyq
     * 查看跟进记录
     */
    public void getRecord(BasePageRequest<CrmContacts> basePageRequest) {
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.CONTACTS_TYPE_KEY.getSign()), basePageRequest.getData().getContactsId());
        if (auth) {
            renderJson(R.noAuth());
            return;
        }
        renderJson(R.ok().put("data", crmContactsService.getRecord(basePageRequest)));
    }

    /**
     * @author wyq
     * 批量导出线索
     */
    @Permissions("crm:contacts:excelexport")
    public void batchExportExcel(@Para("ids") String contactsIds) throws IOException {
        List<Record> recordList = crmContactsService.exportContacts(contactsIds);
        export(recordList);
        renderNull();
    }

    /**
     * @author wyq
     * 导出全部联系人
     */
    @Permissions("crm:contacts:excelexport")
    public void allExportExcel(BasePageRequest basePageRequest) throws IOException {
        JSONObject jsonObject = basePageRequest.getJsonObject();
        jsonObject.fluentPut("excel", "yes").fluentPut("type", "3");
        AdminSceneService adminSceneService = new AdminSceneService();
        List<Record> recordList = (List<Record>) adminSceneService.filterConditionAndGetPageList(basePageRequest).get("data");
        export(recordList);
        renderNull();
    }

    private void export(List<Record> recordList) throws IOException {
        ExcelWriter writer = null;
        try {
            writer = ExcelUtil.getWriter();
            AdminFieldService adminFieldService = new AdminFieldService();
            List<Record> fieldList = adminFieldService.customFieldList("3");
            writer.addHeaderAlias("name", "姓名");
            writer.addHeaderAlias("customer_name", "客户名称");
            writer.addHeaderAlias("next_time", "下次联系时间");
            writer.addHeaderAlias("telephone", "电话");
            writer.addHeaderAlias("mobile", "手机号");
            writer.addHeaderAlias("email", "电子邮箱");
            writer.addHeaderAlias("post", "职务");
            writer.addHeaderAlias("address", "地址");
            writer.addHeaderAlias("remark", "备注");
            writer.addHeaderAlias("create_user_name", "创建人");
            writer.addHeaderAlias("owner_user_name", "负责人");
            writer.addHeaderAlias("create_time", "创建时间");
            writer.addHeaderAlias("update_time", "更新时间");
            for (Record field : fieldList) {
                writer.addHeaderAlias(field.getStr("name"), field.getStr("name"));
            }
            writer.merge(12 + fieldList.size(), "联系人信息");
            HttpServletResponse response = getResponse();
            List<Map<String, Object>> list = new ArrayList<>();
            for (Record record : recordList) {
                list.add(record.remove("batch_id", "contacts_name", "customer_id", "contacts_id", "owner_user_id", "create_user_id", "field_batch_id").getColumns());
            }
            writer.write(list, true);
            writer.setRowHeight(0, 20);
            writer.setRowHeight(1, 20);
            for (int i = 0; i < fieldList.size() + 15; i++) {
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
            response.setHeader("Content-Disposition", "attachment;filename=contacts.xls");
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            // 关闭writer，释放内存
            writer.close();
        }
    }

    /**
     * @author wyq
     * 获取联系人导入模板
     */
    @LoginFormCookie
    public void downloadExcel() {
        List<Record> recordList = adminFieldService.queryAddField(3);
        recordList.removeIf(record -> "file".equals(record.getStr("formType")) || "checkbox".equals(record.getStr("formType")) || "user".equals(record.getStr("formType")) || "structure".equals(record.getStr("formType")));
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("联系人导入表");
        sheet.setDefaultColumnWidth(12);
        sheet.setDefaultRowHeight((short)400);
        HSSFRow titleRow = sheet.createRow(0);
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short)16);
        cellStyle.setFont(font);
        titleRow.createCell(0).setCellValue("联系人导入模板(*)为必填项");
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        titleRow.getCell(0).setCellStyle(cellStyle);
        CellRangeAddress region = new CellRangeAddress(0,0 , 0, recordList.size()-1);
        sheet.addMergedRegion(region);
        try {
            HSSFRow row = sheet.createRow(1);
            for (int i = 0; i < recordList.size(); i++) {
                Record record = recordList.get(i);
                String[] setting = record.get("setting");
                // 在第一行第一个单元格，插入选项
                HSSFCell cell = row.createCell(i);
                // 普通写入操作
                if (record.getInt("is_null") == 1) {
                    cell.setCellValue(record.getStr("name") + "(*)");
                } else {
                    cell.setCellValue(record.getStr("name"));
                }
                if (setting.length != 0) {
                    // 生成下拉列表
                    CellRangeAddressList regions = new CellRangeAddressList(2, Integer.MAX_VALUE, i, i);
                    // 生成下拉框内容
                    DVConstraint constraint = DVConstraint.createExplicitListConstraint(setting);
                    // 绑定下拉框和作用区域
                    HSSFDataValidation dataValidation = new HSSFDataValidation(regions, constraint);
                    // 对sheet页生效
                    sheet.addValidationData(dataValidation);
                }
            }
            HttpServletResponse response = getResponse();
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
            response.setHeader("Content-Disposition", "attachment;filename=contacts_import.xls");
            wb.write(response.getOutputStream());
        } catch (Exception e) {
            Log.getLog(getClass()).error("error", e);
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
     * @author wyq
     * 联系人导入
     */
    @Permissions("crm:contacts:excelimport")
    @NotNullValidate(value = "ownerUserId", message = "请选择负责人")
    public void uploadExcel(@Para("file") UploadFile file, @Para("repeatHandling") Integer repeatHandling, @Para("ownerUserId") Integer ownerUserId) {
        Db.tx(() -> {
            R result = crmContactsService.uploadExcel(file, repeatHandling, ownerUserId);
            renderJson(result);
            if (result.get("code").equals(500)) {
                return false;
            }
            return true;
        });
    }
}
