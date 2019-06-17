package com.kakarote.crm9.erp.crm.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.entity.AdminRecord;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminFieldService;
import com.kakarote.crm9.erp.admin.service.AdminSceneService;
import com.kakarote.crm9.erp.crm.entity.AdminCustomerSetting;
import com.kakarote.crm9.erp.crm.entity.CrmBusiness;
import com.kakarote.crm9.erp.crm.entity.CrmContract;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import com.kakarote.crm9.erp.crm.service.CrmBusinessService;
import com.kakarote.crm9.erp.crm.service.CrmContactsService;
import com.kakarote.crm9.erp.crm.service.CrmContractService;
import com.kakarote.crm9.erp.crm.service.CrmCustomerService;
import com.kakarote.crm9.utils.R;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CrmCustomerController extends Controller {
    @Inject
    private CrmCustomerService crmCustomerService;

    @Inject
    private CrmContactsService crmContactsService;//联系人

    @Inject
    private CrmBusinessService crmBusinessService;//商机

    @Inject
    private CrmContractService crmContractService;//合同

    /**
     * @author wyq
     *分页条件查询客户
     */
    public void queryList(BasePageRequest<CrmCustomer> basePageRequest){
        renderJson(R.ok().put("data",crmCustomerService.getCustomerPageList(basePageRequest)));
    }

    /**
     * @author wyq
     * 新增或更新客户
     */
    @Permissions({"crm:customer:save","crm:customer:update"})
    public void addOrUpdate(){
        JSONObject jsonObject= JSON.parseObject(getRawData());
        renderJson(crmCustomerService.addOrUpdate(jsonObject,"noImport"));
    }

    /**
     * @author wyq
     * 根据客户id查询
     */
    @Permissions("crm:customer:read")
    @NotNullValidate(value = "customerId",message = "客户id不能为空")
    public void queryById(@Para("customerId")Integer customerId){
        renderJson(R.ok().put("data",crmCustomerService.queryById(customerId)));
    }

    /**
     * @author wyq
     * 根据客户名称查询
     */
    @NotNullValidate(value = "name",message = "客户名称不能为空")
    public void queryByName(@Para("name")String name){
        renderJson(R.ok().put("data",crmCustomerService.queryByName(name)));
    }

    /**
     * @author wyq
     * 根据客户id查询联系人
     */
    public void queryContacts(BasePageRequest<CrmCustomer> basePageRequest){
        renderJson(crmCustomerService.queryContacts(basePageRequest));
    }

    /**
     * @author wyq
     * 根据id删除客户
     */
    @Permissions("crm:customer:delete")
    @NotNullValidate(value = "customerIds",message = "客户id不能为空")
    public void deleteByIds(@Para("customerIds")String customerIds){
        renderJson(crmCustomerService.deleteByIds(customerIds));
    }

    /**
     * @author wyq
     * 根据客户id查找商机
     */
    public void queryBusiness(BasePageRequest<CrmCustomer> basePageRequest){
        renderJson(crmCustomerService.queryBusiness(basePageRequest));
    }

    /**
     * @author wyq
     * 根据客户id查询合同
     */
    public void queryContract(BasePageRequest<CrmCustomer> basePageRequest){
        renderJson(crmCustomerService.queryContract(basePageRequest));
    }

    /**
     * @author zxy
     * 条件查询客户公海
     */
    public void queryPageGH(BasePageRequest basePageRequest){
        renderJson(R.ok().put("data",crmCustomerService.queryPageGH(basePageRequest)));
    }

    /**
     * @author wyq
     * 根据客户id查询回款计划
     */
    public void queryReceivablesPlan(BasePageRequest<CrmCustomer> basePageRequest){
        renderJson(crmCustomerService.queryReceivablesPlan(basePageRequest));
    }

    /**
     * @author zxy
     * 根据客户id查询回款
     */
    public void queryReceivables(BasePageRequest<CrmCustomer> basePageRequest){
        renderJson(crmCustomerService.queryReceivables(basePageRequest));
    }

    /**
     * @author wyq
     * 客户锁定
     */
    @Permissions("crm:customer:lock")
    @NotNullValidate(value = "ids",message = "客户id不能为空")
    @NotNullValidate(value = "isLock",message = "锁定状态不能为空")
    public void lock(@Para("")CrmCustomer crmCustomer){
        renderJson(crmCustomerService.lock(crmCustomer));
    }

    /**
     * 客户转移
     * @author wyq
     */
    @Permissions("crm:customer:transfer")
    @NotNullValidate(value = "customerIds",message = "客户id不能为空")
    @NotNullValidate(value = "newOwnerUserId",message = "新负责人不能为空")
    @NotNullValidate(value = "transferType",message = "移除方式不能为空")
    @Before(Tx.class)
    public void transfer(@Para("")CrmCustomer crmCustomer){
        String[] customerIdsArr = crmCustomer.getCustomerIds().split(",");
        for (String customerId : customerIdsArr){
            crmCustomer.setCustomerId(Integer.valueOf(customerId));
            renderJson(crmCustomerService.updateOwnerUserId(crmCustomer));
            String changeType = crmCustomer.getChangeType();
            if(StrUtil.isNotEmpty(changeType)) {
                String[] changeTypeArr = changeType.split(",");
                for(String type : changeTypeArr) {
                    if("1".equals(type)) {//更新联系人负责人
                        renderJson(crmContactsService.updateOwnerUserId(crmCustomer.getCustomerId(),crmCustomer.getNewOwnerUserId()) ? R.ok() : R.error());
                    }
                    if("2".equals(type)) {//更新商机负责人
                        renderJson(crmBusinessService.updateOwnerUserId(crmCustomer));
                    }
                    if("3".equals(type)) {//更新合同负责人
                        renderJson(crmContractService.updateOwnerUserId(crmCustomer));
                    }
                }
            }
        }
    }

    /**
     * @author wyq
     * 查询团队成员
     */
    @NotNullValidate(value = "customerId",message = "客户id不能为空")
    public void getMembers(@Para("customerId")Integer customerId){
        renderJson(R.ok().put("data",crmCustomerService.getMembers(customerId)));
    }

    /**
     * @author wyq
     * 添加团队成员
     */
    @Permissions("crm:customer:teamsave")
    @NotNullValidate(value = "ids",message = "客户id不能为空")
    @NotNullValidate(value = "memberIds",message = "成员id不能为空")
    @NotNullValidate(value = "power",message = "读写权限不能为空")
    @Before(Tx.class)
    public void addMembers(@Para("")CrmCustomer crmCustomer){
        String changeType=crmCustomer.getChangeType();
        if(StrUtil.isNotEmpty(changeType)) {
            String[] changeTypeArr = changeType.split(",");
            for(String type : changeTypeArr) {
                if("2".equals(type)) {//更新商机
                    CrmBusiness crmBusiness = new CrmBusiness();
                    crmBusiness.setIds(crmCustomerService.getBusinessIdsByCustomerIds(crmCustomer.getIds()));
                    crmBusiness.setMemberIds(crmCustomer.getMemberIds());
                    crmBusiness.setPower(crmCustomer.getPower());
                    crmBusiness.setTransferType(crmCustomer.getTransferType());
                    crmBusinessService.addMember(crmBusiness);
                }
                if("3".equals(type)) {//更新合同
                    CrmContract crmContract = new CrmContract();
                    crmContract.setIds(crmCustomerService.getContractIdsByCustomerIds(crmCustomer.getIds()));
                    crmContract.setMemberIds(crmCustomer.getMemberIds());
                    crmContract.setPower(crmCustomer.getPower());
                    crmContract.setTransferType(crmCustomer.getTransferType());
                    crmCustomerService.addMember(crmCustomer);
                }
            }
            crmCustomerService.addMember(crmCustomer);
        }
        renderJson(crmCustomerService.addMember(crmCustomer));
    }

    /**
     * @author wyq
     * 编辑团队成员
     */
    @NotNullValidate(value = "ids",message = "商机id不能为空")
    @NotNullValidate(value = "memberIds",message = "成员id不能为空")
    @NotNullValidate(value = "power",message = "读写权限不能为空")
    public void updateMembers(@Para("")CrmCustomer crmCustomer){
        renderJson(crmCustomerService.addMember(crmCustomer));
    }

    /**
     * @author wyq
     * 删除团队成员
     */
    @NotNullValidate(value = "ids",message = "客户id不能为空")
    @NotNullValidate(value = "memberIds",message = "成员id不能为空")
    public void deleteMembers(@Para("")CrmCustomer crmCustomer){
        renderJson(crmCustomerService.deleteMembers(crmCustomer));
    }
    /**
     * @author zxy
     * 客户保护规则设置
     */
    @NotNullValidate(value = "followupDay",message = "跟进天数不能为空")
    @NotNullValidate(value = "dealDay",message = "成交天数不能为空")
    @NotNullValidate(value = "type",message = "启用状态不能为空")
    public void updateRulesSetting(){
        //跟进天数
        Integer  followupDay = getParaToInt("followupDay");
        //成交天数
        Integer dealDay = getParaToInt("dealDay");
        //启用状态
        Integer type = getParaToInt("type");
        AdminCustomerSetting adminCustomerSetting = new AdminCustomerSetting();
        adminCustomerSetting.setDealDay(dealDay);
        adminCustomerSetting.setFollowupDay( followupDay);
        adminCustomerSetting.setType(type);
        renderJson(crmCustomerService.updateRulesSetting(adminCustomerSetting));
    }
    /**
     * @author zxy
     * 获取客户保护规则设置
     */
    public void getRulesSetting(){
        renderJson(crmCustomerService.getRulesSetting());
    }

    /**
     * @author wyq
     * 查询自定义字段
     */
    public void queryField(){
        renderJson(R.ok().put("data",crmCustomerService.queryField()));
    }

    /**
     * @author wyq
     * 添加跟进记录
     */
    @NotNullValidate(value = "typesId",message = "客户id不能为空")
    @NotNullValidate(value = "content",message = "内容不能为空")
    @NotNullValidate(value = "category",message = "跟进类型不能为空")
    public void addRecord(@Para("")AdminRecord adminRecord){
        renderJson(crmCustomerService.addRecord(adminRecord));
    }

    /**
     * @author wyq
     * 查看跟进记录
     */
    public void getRecord(BasePageRequest<CrmCustomer> basePageRequest){
        renderJson(R.ok().put("data",crmCustomerService.getRecord(basePageRequest)));
    }

    /**
     * @author HJP
     * 员工客户分析
     */
    public void getUserCustomerAnalysis(BasePageRequest<AdminUser> basePageRequest){
        renderJson(crmCustomerService.getUserCustomerAnalysis(basePageRequest));
    }

    /**
     * @author wyq
     * 客户批量导出
     */
    @Permissions("crm:customer:excelexport")
    public void batchExportExcel(@Para("ids")String customerIds) throws IOException {
        List<Record> recordList = crmCustomerService.exportCustomer(customerIds);
        export(recordList);
        renderNull();
    }

    /**
     * @author wyq
     * 全部导出
     */
    @Permissions("crm:customer:excelexport")
    public void allExportExcel(BasePageRequest basePageRequest) throws IOException{
        JSONObject jsonObject = basePageRequest.getJsonObject();
        jsonObject.fluentPut("excel","yes").fluentPut("type","2");
        AdminSceneService adminSceneService = new AdminSceneService();
        List<Record> recordList = (List<Record>)adminSceneService.filterConditionAndGetPageList(basePageRequest).get("data");
        export(recordList);
        renderNull();
    }

    private void export(List<Record> recordList) throws IOException{
        ExcelWriter writer = ExcelUtil.getWriter();
        AdminFieldService adminFieldService = new AdminFieldService();
        List<Record> fieldList = adminFieldService.list("2");
        writer.addHeaderAlias("customer_name","客户名称");
        writer.addHeaderAlias("telephone","电话");
        writer.addHeaderAlias("website","网址");
        writer.addHeaderAlias("next_time","下次联系时间");
        writer.addHeaderAlias("deal_status","成交状态");
        writer.addHeaderAlias("create_user_name","创建人");
        writer.addHeaderAlias("owner_user_name","负责人");
        writer.addHeaderAlias("address","省市区");
        writer.addHeaderAlias("location","定位信息");
        writer.addHeaderAlias("detail_address","详细地址");
        writer.addHeaderAlias("lng","地理位置经度");
        writer.addHeaderAlias("lat","地理位置维度");
        writer.addHeaderAlias("create_time","创建时间");
        writer.addHeaderAlias("update_time","更新时间");
        writer.addHeaderAlias("remark","备注");
        for (Record field:fieldList){
            writer.addHeaderAlias(field.getStr("name"),field.getStr("name"));
        }
        writer.merge(fieldList.size()+14,"客户信息");
        HttpServletResponse response = getResponse();
        List<Map<String,Object>> list = new ArrayList<>();
        for (Record record : recordList){
            list.add(record.remove("batch_id","create_user_id","customer_id","is_lock","owner_user_id","ro_user_id","rw_user_id").getColumns());
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
        response.setHeader("Content-Disposition", "attachment;filename=customer.xls");
        ServletOutputStream out = response.getOutputStream();
        writer.flush(out);
        // 关闭writer，释放内存
        writer.close();
    }
    /**
     * 客户放入公海
     * @author zxy
     */
    @Permissions("crm:customer:putinpool")
    public void updateCustomerByIds(){
        String ids = get("ids");
        renderJson(crmCustomerService.updateCustomerByIds(ids));
    }
    /**
     * 领取或分配客户
     * @author zxy
     */
    @Permissions("crm:customer:distribute")
    public void getCustomersByIds(){
        String ids = get("ids");
        Long userId = getLong("userId");
       /* JSONObject jsonObject= JSON.parseObject(getRawData());*/
        renderJson(crmCustomerService.getCustomersByIds(ids,userId));
    }

    /**
     * @author wyq
     * 获取导入模板
     */
    public void downloadExcel(){
        List<Record> recordList = crmCustomerService.queryExcelField();
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("客户导入表");
        HSSFRow row = sheet.createRow(0);
        for (int i=0;i < recordList.size();i++){
            Record record = recordList.get(i);
            if ("map_address".equals(record.getStr("fieldName"))){
                record.set("name","详细地址").set("setting",new String[]{});
            }
            String[] setting = record.get("setting");
            HSSFCell cell = row.createCell(i);
            if (record.getInt("is_null") == 1){
                cell.setCellValue(record.getStr("name")+"(*)");
            }else {
                cell.setCellValue(record.getStr("name"));
            }
            if (setting != null && setting.length != 0){
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
            response.setHeader("Content-Disposition", "attachment;filename=customer_import.xls");
            wb.write(response.getOutputStream());
            wb.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        renderNull();
    }

    /**
     * @author wyq
     * 导入客户
     */
    @Permissions("crm:customer:excelimport")
    @NotNullValidate(value = "ownerUserId",message = "请选择负责人")
    public void uploadExcel(@Para("file") UploadFile file, @Para("repeatHandling") Integer repeatHandling, @Para("ownerUserId") Integer ownerUserId){
        renderJson(crmCustomerService.uploadExcel(file,repeatHandling,ownerUserId));
    }
}
