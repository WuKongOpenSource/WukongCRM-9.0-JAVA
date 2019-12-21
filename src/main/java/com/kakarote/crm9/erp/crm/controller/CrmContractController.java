package com.kakarote.crm9.erp.crm.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Aop;
import com.jfinal.aop.Before;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.entity.AdminFieldSort;
import com.kakarote.crm9.erp.admin.entity.AdminRecord;
import com.kakarote.crm9.erp.admin.service.AdminFieldService;
import com.kakarote.crm9.erp.admin.service.AdminSceneService;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.entity.CrmContract;
import com.kakarote.crm9.erp.crm.entity.CrmContractProduct;
import com.kakarote.crm9.erp.crm.entity.CrmReceivables;
import com.kakarote.crm9.erp.crm.service.CrmContractService;
import com.kakarote.crm9.erp.crm.service.CrmReceivablesPlanService;
import com.kakarote.crm9.erp.crm.service.CrmReceivablesService;
import com.kakarote.crm9.utils.AuthUtil;
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


public class CrmContractController extends Controller {
    @Inject
    private CrmContractService crmContractService;
    @Inject
    private CrmReceivablesService receivablesService;
    @Inject
    private CrmReceivablesPlanService receivablesPlanService;

    @Inject
    private AdminSceneService adminSceneService;

    /**
     * @author wyq
     * 查看列表页
     */
    @Permissions({"crm:contract:index"})
    public void queryPageList(BasePageRequest basePageRequest){
        JSONObject jsonObject = basePageRequest.getJsonObject().fluentPut("type",6);
        basePageRequest.setJsonObject(jsonObject);
        renderJson(adminSceneService.filterConditionAndGetPageList(basePageRequest));
    }

    /**
     * 分页条件查询合同
     * @author zxy
     */
    public void queryPage(BasePageRequest<CrmContract> basePageRequest){
        renderJson(R.ok().put("data",crmContractService.queryPage(basePageRequest)));
    }
    /**
     * 根据id查询合同
     * @author zxy
     */
    @Permissions("crm:contract:read")
    @NotNullValidate(value = "contractId",message = "合同id不能为空")
    public void queryById(@Para("contractId") Integer id){
        renderJson(R.ok().put("data",crmContractService.queryById(id)));
    }
    /**
     * 根据id删除合同
     * @author zxy
     */
    @Permissions("crm:contract:delete")
    @NotNullValidate(value = "contractIds",message = "合同id不能为空")
    public void deleteByIds(@Para("contractIds") String contractIds){
        renderJson(crmContractService.deleteByIds(contractIds));
    }
    /**
     * @author wyq
     * 合同转移
     */
    @Permissions("crm:contract:transfer")
    @NotNullValidate(value = "contractIds",message = "合同id不能为空")
    @NotNullValidate(value = "newOwnerUserId",message = "负责人id不能为空")
    @NotNullValidate(value = "transferType",message = "移除方式不能为空")
    @Before(Tx.class)
    public void transfer(@Para("")CrmContract crmContract){
        renderJson(crmContractService.transfer(crmContract));
    }
    /**
     * 添加或修改
     * @author zxy
     */
    @Permissions({"crm:contract:save","crm:contract:update"})
    public void saveAndUpdate(){
        String data = getRawData();
        JSONObject jsonObject = JSON.parseObject(data);
        renderJson(crmContractService.saveAndUpdate(jsonObject));
    }
    /**
     * 根据条件查询合同
     * @author zxy
     */
    public void queryList(@Para("")CrmContract crmContract){
        renderJson(R.ok().put("data",crmContractService.queryList(crmContract)));
    }

    /**
     * @author wyq
     * 查询团队成员
     */
    public void getMembers(@Para("contractId")Integer contractId){
        boolean auth = AuthUtil.isCrmAuth(CrmEnum.CRM_CONTRACT, contractId);
        if(auth){renderJson(R.noAuth()); return; }
        renderJson(R.ok().put("data",crmContractService.getMembers(contractId)));
    }

    /**
     * @author wyq
     * 编辑团队成员
     */
    @Permissions("crm:contract:teamsave")
    public void updateMembers(@Para("")CrmContract crmContract){
        renderJson(crmContractService.addMember(crmContract));
    }

    /**
     * @author wyq
     * 添加团队成员
     */
    @Permissions("crm:contract:teamsave")
    public void addMembers(@Para("")CrmContract crmContract){
        renderJson(crmContractService.addMember(crmContract));
    }

    /**
     * @author wyq
     * 删除团队成员
     */
    @Permissions("crm:contract:teamsave")
    public void deleteMembers(@Para("")CrmContract crmContract){
        renderJson(crmContractService.deleteMembers(crmContract));
    }

    /**
     * @author wyq
     * 添加跟进记录
     */
    @NotNullValidate(value = "typesId",message = "合同id不能为空")
    @NotNullValidate(value = "content",message = "内容不能为空")
    @NotNullValidate(value = "category",message = "跟进类型不能为空")
    public void addRecord(@Para("")AdminRecord adminRecord){
        boolean auth = AuthUtil.isCrmAuth(CrmEnum.CRM_CONTRACT, adminRecord.getTypesId());
        if(auth){renderJson(R.noAuth()); return; }
        renderJson(crmContractService.addRecord(adminRecord));
    }

    /**
     * @author wyq
     * 查看跟进记录
     */
    public void getRecord(BasePageRequest<CrmContract> basePageRequest){
        boolean auth = AuthUtil.isCrmAuth(CrmEnum.CRM_CONTRACT, basePageRequest.getData().getContractId());
        if(auth){renderJson(R.noAuth()); return; }
        renderJson(R.ok().put("data",crmContractService.getRecord(basePageRequest)));
    }
    /**
     * 根据合同ID查询回款
     * @author zxy
     */
    public void qureyReceivablesListByContractId(BasePageRequest<CrmReceivables> basePageRequest){
        boolean auth = AuthUtil.isCrmAuth(CrmEnum.CRM_CONTRACT, basePageRequest.getData().getContractId());
        if(auth){renderJson(R.noAuth()); return; }
        renderJson(receivablesService.qureyListByContractId(basePageRequest));
    }
    /**
     * 根据合同ID查询产品
     * @author zxy
     */
    public void qureyProductListByContractId(BasePageRequest<CrmContractProduct> basePageRequest){
        boolean auth = AuthUtil.isCrmAuth(CrmEnum.CRM_CONTRACT, basePageRequest.getData().getContractId());
        if(auth){renderJson(R.noAuth()); return; }
        renderJson(crmContractService.qureyProductListByContractId(basePageRequest));
    }
    /**
     * 根据合同ID查询回款计划
     * @author zxy
     */
    public void qureyReceivablesPlanListByContractId(BasePageRequest<CrmReceivables> basePageRequest){
        boolean auth = AuthUtil.isCrmAuth(CrmEnum.CRM_CONTRACT, basePageRequest.getData().getContractId());
        if(auth){renderJson(R.noAuth()); return; }
        renderJson(receivablesPlanService.qureyListByContractId(basePageRequest));
    }

    /**
     * 新建汇款时根据合同ID查询回款计划
     * @author wyq
     */
    public void queryReceivablesPlansByContractId(@Para("contractId") Integer contractId){
        renderJson(receivablesPlanService.queryReceivablesPlansByContractId(contractId));
    }

    /**
     * 查询合同到期提醒设置
     */
    public void queryContractConfig(){
        renderJson(crmContractService.queryContractConfig());
    }

    /**
     * 修改合同到期提醒设置
     */
    @NotNullValidate(value = "status",message = "status不能为空")
    public void setContractConfig(@Para("status") Integer status,@Para("contractDay") Integer contractDay){
        renderJson(crmContractService.setContractConfig(status,contractDay));
    }

    /**
     * 合同作废
     */
    @Permissions("crm:contract:discard")
    public void contractDiscard(@Para("contractId") Integer contractId){
        renderJson(crmContractService.contractDiscard(contractId));
    }

    /**
     * @author wyq
     * 批量导出合同
     */
    @Permissions("crm:contract:excelexport")
    public void batchExportExcel(BasePageRequest basePageRequest) {
        JSONObject jsonObject=basePageRequest.getJsonObject();
        String ids=jsonObject.getString("ids");
        JSONObject data =new JSONObject();
        data.fluentPut("contractExport",new JSONObject().fluentPut("name","contract_id").fluentPut("condition","in").fluentPut("value", ids));
        jsonObject.fluentPut("data",data).fluentPut("search","").fluentPut("type",6);
        basePageRequest.setJsonObject(jsonObject);
        JSONObject resultData = (JSONObject)adminSceneService.getCrmPageList(basePageRequest).get("data");
        List<Record> recordList = resultData.getJSONArray("list").toJavaList(Record.class);
        export(recordList);
        renderNull();
    }

    /**
     * @author wyq
     * 导出全部合同
     */
    @Permissions("crm:contract:excelexport")
    public void allExportExcel(BasePageRequest basePageRequest) {
        JSONObject jsonObject = basePageRequest.getJsonObject();
        jsonObject.fluentPut("excel", "yes").fluentPut("type", 6);
        AdminSceneService adminSceneService = new AdminSceneService();
        JSONObject data = (JSONObject)adminSceneService.filterConditionAndGetPageList(basePageRequest).get("data");
        List<Record> recordList = data.getJSONArray("list").toJavaList(Record.class);
        export(recordList);
        renderNull();
    }

    private void export(List<Record> recordList) {
        try (ExcelWriter writer = ExcelUtil.getWriter()) {
            AdminFieldSort adminFieldSort = new AdminFieldSort();
            adminFieldSort.setLabel(CrmEnum.CRM_CONTRACT.getType());
            List<Record> headList = Aop.get(AdminFieldService.class).queryListHead(adminFieldSort);
            headList.forEach(head -> writer.addHeaderAlias(StrUtil.toUnderlineCase(head.getStr("fieldName")), head.getStr("name")));
            writer.addHeaderAlias("check_status","审核状态");
            writer.merge(headList.size() - 1, "合同信息");
            HttpServletResponse response = getResponse();
            List<Map<String, Object>> list = new ArrayList<>();
            if (recordList.size() == 0) {
                Record record = new Record();
                headList.forEach(head -> record.set(StrUtil.toUnderlineCase(head.getStr("fieldName")), ""));
                list.add(record.getColumns());
            }
            recordList.forEach(record -> {
                String checkStatus;
                //0待审核、1通过、2拒绝、3审核中 4:撤回 5 未提交 6 创建 7 已删除 8 作废
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
                    case 6:
                        checkStatus = "作废";
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
            response.setHeader("Content-Disposition", "attachment;filename=contract.xls");
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
        } catch (Exception e) {
            Log.getLog(getClass()).error("导出合同错误：",e);
        }
    }

//    private void export(List<Record> recordList) {
//        try (ExcelWriter writer = ExcelUtil.getWriter()) {
//            AdminFieldSort adminFieldSort=new AdminFieldSort();
//            adminFieldSort.setLabel(CrmEnum.CRM_CONTRACT.getType());
//            List<Record> headList = Aop.get(AdminFieldService.class).queryListHead(adminFieldSort);
//            headList.forEach(head -> writer.addHeaderAlias(StrUtil.toUnderlineCase(head.getStr("fieldName")), head.getStr("name")));
//            writer.merge(headList.size() - 1, "合同信息");
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
//            response.setHeader("Content-Disposition", "attachment;filename=contract.xls");
//            ServletOutputStream out = response.getOutputStream();
//            writer.flush(out);
//        } catch (Exception e) {
//            Log.getLog(getClass()).error("导出合同错误：",e);
//        }
//    }
}
