package com.kakarote.crm9.erp.crm.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Aop;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.entity.AdminFieldSort;
import com.kakarote.crm9.erp.admin.service.AdminFieldService;
import com.kakarote.crm9.erp.admin.service.AdminSceneService;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.entity.CrmReceivables;
import com.kakarote.crm9.erp.crm.service.CrmReceivablesService;
import com.kakarote.crm9.utils.R;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import org.apache.poi.ss.usermodel.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CrmReceivablesController extends Controller {

    @Inject
    private CrmReceivablesService crmReceivablesService;

    @Inject
    private AdminSceneService adminSceneService;

    /**
     * @author wyq
     * 查看列表页
     */
    @Permissions({"crm:receivables:index"})
    public void queryPageList(BasePageRequest basePageRequest){
        JSONObject jsonObject = basePageRequest.getJsonObject().fluentPut("type",7);
        basePageRequest.setJsonObject(jsonObject);
        renderJson(adminSceneService.filterConditionAndGetPageList(basePageRequest));
    }

    /**@author zxy
     * 分页查询回款
     */
    public void  queryPage(BasePageRequest<CrmReceivables> basePageRequest){
        renderJson(R.ok().put("data",crmReceivablesService.queryPage(basePageRequest)));
    }
    /**
     * @author zxy
     * 添加或者修改
     */
    @Permissions({"crm:receivables:save","crm:receivables:update"})
    public void  saveOrUpdate(){
        JSONObject jsonObject = JSON.parseObject(getRawData());
        renderJson(crmReceivablesService.saveOrUpdate(jsonObject));
    }
    /**
     * @author zxy
     * 根据回款id查询
     */
    @Permissions("crm:receivables:read")
    @NotNullValidate(value = "receivablesId",message = "回款id不能为空")
    public void  queryById(@Para("receivablesId") Integer receivablesId){
        renderJson(R.ok().put("data",crmReceivablesService.queryById(receivablesId)));
    }
    /**
     * @author zxy
     * 根据回款id删除
     */
    @Permissions("crm:receivables:delete")
    @NotNullValidate(value = "receivablesIds",message = "回款id不能为空")
    public void  deleteByIds(@Para("receivablesIds") String receivablesIds){
        renderJson(crmReceivablesService.deleteByIds(receivablesIds));
    }
    /**
     * 根据条件查询回款
     * @author zxy
     */
    public void queryList(@Para("") CrmReceivables receivables){
        renderJson(R.ok().put("data",crmReceivablesService.queryList(receivables)));
    }

    /**
     * @author wyq
     * 批量导出回款
     */
    @Permissions("crm:receivables:excelexport")
    public void batchExportExcel(BasePageRequest basePageRequest) {
        JSONObject jsonObject=basePageRequest.getJsonObject();
        String ids=jsonObject.getString("ids");
        JSONObject data =new JSONObject();
        data.fluentPut("receivablesExport",new JSONObject().fluentPut("name","receivables_id").fluentPut("condition","in").fluentPut("value", ids));
        jsonObject.fluentPut("data",data).fluentPut("search","").fluentPut("type",7);
        basePageRequest.setJsonObject(jsonObject);
        JSONObject resultData = (JSONObject)adminSceneService.getCrmPageList(basePageRequest).get("data");
        List<Record> recordList = resultData.getJSONArray("list").toJavaList(Record.class);
        export(recordList);
        renderNull();
    }

    /**
     * @author wyq
     * 导出全部回款
     */
    @Permissions("crm:receivables:excelexport")
    public void allExportExcel(BasePageRequest basePageRequest) {
        JSONObject jsonObject = basePageRequest.getJsonObject();
        jsonObject.fluentPut("excel", "yes").fluentPut("type", 7);
        AdminSceneService adminSceneService = new AdminSceneService();
        JSONObject data = (JSONObject)adminSceneService.filterConditionAndGetPageList(basePageRequest).get("data");
        List<Record> recordList = data.getJSONArray("list").toJavaList(Record.class);
        export(recordList);
        renderNull();
    }

    private void export(List<Record> recordList) {
        try (ExcelWriter writer = ExcelUtil.getWriter()) {
            AdminFieldSort adminFieldSort = new AdminFieldSort();
            adminFieldSort.setLabel(CrmEnum.CRM_RECEIVABLES.getType());
            List<Record> headList = Aop.get(AdminFieldService.class).queryListHead(adminFieldSort);
            headList.forEach(head -> writer.addHeaderAlias(StrUtil.toUnderlineCase(head.getStr("fieldName")), head.getStr("name")));
            writer.addHeaderAlias("check_status","审核状态");
            writer.merge(headList.size() - 1, "回款信息");
            HttpServletResponse response = getResponse();
            List<Map<String, Object>> list = new ArrayList<>();
            if (recordList.size() == 0) {
                Record record = new Record();
                headList.forEach(head -> record.set(StrUtil.toUnderlineCase(head.getStr("fieldName")), ""));
                list.add(record.getColumns());
            }
            recordList.forEach(record -> {
                String checkStatus;
                //0待审核、1通过、2拒绝、3审核中 4:撤回 5 未提交
                switch (record.getInt("check_status")) {
                    case 1:
                        checkStatus = "通过";
                        break;
                    case 2:
                        checkStatus = "拒绝";
                        break;
                    case 3:
                        checkStatus = "审核中";
                        break;
                    case 4:
                        checkStatus = "撤回";
                        break;
                    case 5:
                        checkStatus = "未提交";
                        break;
                    default:
                        checkStatus = "待审核";
                }
                record.set("check_status", checkStatus);
                list.add(record.getColumns());
            });
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
            response.setHeader("Content-Disposition", "attachment;filename=receivables.xls");
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
        } catch (Exception e) {
            Log.getLog(getClass()).error("导出回款错误：",e);
        }
    }

//    private void export(List<Record> recordList) {
//        try (ExcelWriter writer = ExcelUtil.getWriter()) {
//            AdminFieldSort adminFieldSort = new AdminFieldSort();
//            adminFieldSort.setLabel(CrmEnum.CRM_RECEIVABLES.getType());
//            List<Record> headList = Aop.get(AdminFieldService.class).queryListHead(adminFieldSort);
//            headList.forEach(head -> writer.addHeaderAlias(StrUtil.toUnderlineCase(head.getStr("fieldName")), head.getStr("name")));
//            writer.merge(headList.size() - 1, "回款信息");
//            HttpServletResponse response = getResponse();
//            List<Map<String, Object>> list = new ArrayList<>();
//            if (recordList.size() == 0) {
//                Record record = new Record();
//                headList.forEach(head -> record.set(head.getStr("fieldName"), ""));
//                list.add(record.getColumns());
//            }
//            recordList.forEach(record -> list.add(record.getColumns()));
//            writer.setOnlyAlias(true);
//            writer.write(list, true);
//            writer.setRowHeight(0, 20);
//            writer.setRowHeight(1, 20);
//            for (int i = 0; i < headList.size(); i++) {
//                writer.setColumnWidth(i, 20);
//            }
//            Cell cell = writer.getCell(0, 0);
//            CellStyle cellStyle = cell.getCellStyle();
//            cellStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
//            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//            Font font = writer.createFont();
//            font.setBold(true);
//            font.setFontHeightInPoints((short) 16);
//            cellStyle.setFont(font);
//            cell.setCellStyle(cellStyle);
//            //自定义标题别名
//            //response为HttpServletResponse对象
//            response.setContentType("application/vnd.ms-excel;charset=utf-8");
//            response.setCharacterEncoding("UTF-8");
//            //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
//            response.setHeader("Content-Disposition", "attachment;filename=receivables.xls");
//            ServletOutputStream out = response.getOutputStream();
//            writer.flush(out);
//        } catch (Exception e) {
//            Log.getLog(getClass()).error("导出回款错误：",e);
//        }
//    }
}
