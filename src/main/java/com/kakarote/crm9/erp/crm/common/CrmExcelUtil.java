package com.kakarote.crm9.erp.crm.common;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.poi.excel.BigExcelWriter;
import cn.hutool.poi.excel.ExcelUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Aop;
import com.jfinal.kit.Kv;
import com.jfinal.kit.PathKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.TableMapping;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.erp.admin.common.AdminMessageEnum;
import com.kakarote.crm9.erp.admin.entity.AdminFile;
import com.kakarote.crm9.erp.admin.entity.AdminMessage;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminFieldService;
import com.kakarote.crm9.erp.crm.entity.*;
import com.kakarote.crm9.erp.crm.service.CrmContactsService;
import com.kakarote.crm9.erp.crm.service.CrmCustomerService;
import com.kakarote.crm9.erp.crm.service.CrmLeadsService;
import com.kakarote.crm9.erp.crm.service.CrmProductService;
import com.kakarote.crm9.utils.AuthUtil;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.io.File;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.*;

public class CrmExcelUtil {
    //超出的线程直接抛出异常
    private static final ExecutorService threadPool = new ThreadPoolExecutor(1, 20, 5L, TimeUnit.SECONDS, new LinkedBlockingDeque<>(2048), new ThreadPoolExecutor.AbortPolicy());
    //暂存导入线程
    private static final ConcurrentHashMap<Long, ExcelImport> threadMap = new ConcurrentHashMap<>(32, 0.5f);

    public CrmExcelUtil() {
    }

    public final Long addWork(CrmEnum crmEnum, String filePath, Long ownerUserId,Integer repeatHandling) {
        AdminMessage adminMessage = new AdminMessage();
        adminMessage.setCreateUser(BaseUtil.getUserId());
        adminMessage.setCreateTime(DateUtil.date());
        adminMessage.setRecipientUser(BaseUtil.getUserId());
        adminMessage.setLabel(6);
        switch (crmEnum) {
            case CRM_CUSTOMER:
                adminMessage.setType(AdminMessageEnum.CRM_CUSTOMER_IMPORT.getType());
                break;
            case CRM_LEADS:
                adminMessage.setType(AdminMessageEnum.CRM_LEADS_IMPORT.getType());
                break;
            case CRM_CONTACTS:
                adminMessage.setType(AdminMessageEnum.CRM_CONTACTS_IMPORT.getType());
                break;
            case CRM_PRODUCT:
                adminMessage.setType(AdminMessageEnum.CRM_PRODUCT_IMPORT.getType());
                break;
            default:
        }
        if (!adminMessage.save()) {
            return null;
        }
        ExcelImport excelImport = new ExcelImport(crmEnum, filePath, ownerUserId, adminMessage.getMessageId(), BaseUtil.getUser(),repeatHandling);
        //注意执行顺序
        threadPool.execute(excelImport);
        threadMap.put(adminMessage.getMessageId(), excelImport);
        return adminMessage.getMessageId();
    }

    /**
     * 根据消息ID获取当前导入数量
     *
     * @param messageId 消息ID
     * @return 导入数量
     */
    public static Integer getThreadMapNum(Long messageId) {
        if(messageId==null){
            return null;
        }
        return threadMap.containsKey(messageId) ? threadMap.get(messageId).num : null;
    }

    public static void interrupt(Long messageId) {
        ExcelImport excelImport = threadMap.get(messageId);
        if (excelImport != null) {
            excelImport.interrupted = true;
            AdminMessage adminMessage = AdminMessage.dao.findById(messageId);
            adminMessage.setTitle(excelImport.num-2 + "");
            adminMessage.setContent(excelImport.errorList.size()-2 + "");
            adminMessage.update();
        }
    }

    public static String getRange(int offset, int rowId, int colCount) {
        char start = (char) ('A' + offset);
        if (colCount <= 25) {
            char end = (char) (start + colCount - 1);
            return "$" + start + "$" + rowId + ":$" + end + "$" + rowId;
        } else {
            char endPrefix = 'A';
            char endSuffix = 'A';
            if ((colCount - 25) / 26 == 0 || colCount == 51) {// 26-51之间，包括边界（仅两次字母表计算）
                if ((colCount - 25) % 26 == 0) {// 边界值
                    endSuffix = (char) ('A' + 25);
                } else {
                    endSuffix = (char) ('A' + (colCount - 25) % 26 - 1);
                }
            } else {// 51以上
                if ((colCount - 25) % 26 == 0) {
                    endSuffix = (char) ('A' + 25);
                    endPrefix = (char) (endPrefix + (colCount - 25) / 26 - 1);
                } else {
                    endSuffix = (char) ('A' + (colCount - 25) % 26 - 1);
                    endPrefix = (char) (endPrefix + (colCount - 25) / 26);
                }
            }
            return "$" + start + "$" + rowId + ":$" + endPrefix + endSuffix + "$" + rowId;
        }
    }

    /**
     * offset为主列, 市引用省一列的值，区引用市一列的值
     */
    public static void setDataValidation(String offset, HSSFSheet sheet, int rowNum, int colNum) {
        Integer i = rowNum + 1;
        DVConstraint formula = DVConstraint.createFormulaListConstraint("INDIRECT($" + offset + "$" + i + ")");
        CellRangeAddressList rangeAddressList = new CellRangeAddressList(rowNum, rowNum, colNum, colNum);
        HSSFDataValidation cacse = new HSSFDataValidation(rangeAddressList, formula);
        cacse.createErrorBox("error", "请选择正确的地区");
        sheet.addValidationData(cacse);
    }

    /**
     * 判断数字和excel字符列对应关系
     * @param columnNo 列
     * @return 对应列字符
     */
    public static String getCorrespondingLabel(int columnNo) {
        if (columnNo < 1 || columnNo > 16384) {
            throw new IllegalArgumentException();
        }
        String[] sources = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M"
                , "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
        StringBuilder sb = new StringBuilder(5);
        //求最右边的字母
        int remainder = columnNo % 26;
        //说明(num_n-1)=26，第26个字母是Z
        if (remainder == 0) {
            sb.append("Z");
            //因为接下来W-(num_n-1)也就是columnNo-remainder,所以需要把remainder赋值回26
            remainder = 26;
        } else {  //如果最右边字母不是Z的话，就去sources数组相应的位置取字母，remainder不用变
            sb.append(sources[remainder - 1]);
        }
        //用来判断接下来是否还有其他字母
        columnNo = (columnNo - remainder) / 26 - 1;

        //当前循环是求最后一个字母时（从右往左），(columnNo-remainder)/26就会是0，再减1也就是-1。
        //因此通过判断(columnNo-remainder)/26-1是否大于-1来判断结束
        while (columnNo > -1) {
            remainder = columnNo % 26;
            sb.append(sources[remainder]);
            columnNo = (columnNo - remainder) / 26 - 1;
        }
        //因为是从右往左解析的 所以需要反转
        return sb.reverse().toString();
    }

    /**
     * 获取市区对应关系
     * @return 对应关系
     */
    @SuppressWarnings("unchecked")
    public static Map<String,List<String>> getAreaMap(){
        cn.hutool.json.JSONObject readJSONObject= JSONUtil.readJSONObject(new File(PathKit.getRootClassPath()+"/config/area.json"), Charset.defaultCharset());
        return readJSONObject.toBean(Map.class);
    }

    private class ExcelImport implements Runnable {

        private String filePath;
        private CrmEnum crmEnum;
        private Long ownerUserId;
        private Integer num = 0;
        private Long messageId;
        private List<List<Object>> errorList = new ArrayList<>();
        private List<Record> fieldList;
        private List<Record> uniqueList = new ArrayList<>();
        private List<Integer> isNullList = new ArrayList<>();
        private boolean templateErr = false;
        private boolean interrupted = false;
        private Integer repeatHandling;
        private Kv kv = new Kv();
        private AdminUser adminUser;

        ExcelImport(CrmEnum crmEnum, String filePath, Long ownerUserId, Long messageId, AdminUser adminUser,Integer repeatHandling) {
            this.crmEnum = crmEnum;
            this.filePath = filePath;
            this.ownerUserId = ownerUserId;
            this.adminUser = adminUser;
            this.messageId = messageId;
            this.repeatHandling=repeatHandling;
        }

        @Override
        public void run() {
            if (interrupted) {
                return;
            }
            BaseUtil.setUser(adminUser);
            try {
                switch (crmEnum) {
                    case CRM_CUSTOMER:
                        customerImport();
                        break;
                    case CRM_LEADS:
                        leadsImport();
                        break;
                    case CRM_CONTACTS:
                        contactsImport();
                        break;
                    case CRM_PRODUCT:
                        productImport();
                        break;
                    default:
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.getLog(getClass()).error("导入数据错误：",e);
            } finally {
                BaseUtil.removeThreadLocal();
                CrmExcelUtil.threadMap.remove(messageId);
            }
            AdminMessage adminMessage = AdminMessage.dao.findById(messageId);
            adminMessage.setTitle(String.valueOf(num-2));
            adminMessage.setContent(String.valueOf(errorList.size()-2));
            if(errorList.size()>2){
                File file=new File(BaseConstant.UPLOAD_PATH+"excel/"+BaseUtil.getDate()+"/"+ IdUtil.simpleUUID()+".xlsx");
                BigExcelWriter writer= ExcelUtil.getBigWriter(file);
                CellStyle textStyle = writer.getWorkbook().createCellStyle();
                DataFormat format = writer.getWorkbook().createDataFormat();
                textStyle.setDataFormat(format.getFormat("@"));
                for (int i = 0; i < errorList.get(1).size(); i++) {
                    writer.setColumnWidth(i,20);
                    writer.getSheet().setDefaultColumnStyle(i,textStyle);
                }
                writer.merge(errorList.get(1).size()-1,errorList.remove(0).get(0));
                writer.setDefaultRowHeight(20);
                Cell cell = writer.getCell(0, 0);
                CellStyle cellStyle = cell.getCellStyle();
                cellStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
                cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                Font font = writer.createFont();
                font.setBold(true);
                font.setFontHeightInPoints((short) 16);
                cellStyle.setFont(font);
                cell.setCellStyle(cellStyle);
                // 一次性写出内容，使用默认样式
                writer.write(errorList);
                // 关闭writer，释放内存
                writer.close();
                AdminFile adminFile=new AdminFile();
                adminFile.setName("EXCEL导入");
                adminFile.setCreateUserId(adminUser.getUserId());
                adminFile.setPath(file.getAbsolutePath());
                adminFile.setFilePath("/file/downFile?fileId="+IdUtil.simpleUUID());
                adminFile.setCreateTime(new Date());
                adminFile.setSize(file.length());
                adminFile.save();
                adminMessage.setTypeId(adminFile.getFileId());
            }
            adminMessage.update();
        }

        private void customerImport() {
            ExcelUtil.readBySax(filePath, 0, (int sheetIndex, int rowIndex, List<Object> rowList) -> {
                num++;
                if (interrupted) {
                    return;
                }
                if (rowList.size() < kv.entrySet().size()) {
                    for (int i = rowList.size() - 1; i < kv.entrySet().size(); i++) {
                        rowList.add(null);
                    }
                }
                if(num>=10000){
                    rowList.add(0, "最多同时导入10000条数据");
                    errorList.add(rowList);
                    return;
                }
                if (rowIndex > 1) {
                    if (templateErr) {
                        rowList.add(0, "请使用最新的模板");
                        errorList.add(rowList);
                    } else {
                        Map<String, List<String>> areaMap = getAreaMap();
                        try {
                            CrmCustomer customer = null;
                            for (Integer integer : isNullList) {
                                if (ObjectUtil.isEmpty(rowList.get(integer))) {
                                    rowList.add(0, "必填字段未填写");
                                    errorList.add(rowList);
                                    return;
                                }
                            }
                            for (Record record : uniqueList) {
                                record.set("value", rowList.get(kv.getInt(record.getStr("field_name"))));
                                Integer i = Aop.get(AdminFieldService.class).verify(crmEnum, record);
                                if (i > 0) {
                                    if(repeatHandling.equals(2)){
                                        return;
                                    }
                                    if (customer == null) {
                                        record.set("primaryKey", TableMapping.me().getTable(CrmCustomer.class).getPrimaryKey()[0]);
                                        record.set("tableName", crmEnum.getName());
                                        customer = CrmCustomer.dao.template("admin.field.queryFieldValueNoDelete", record.getColumns()).findFirst();
                                    } else {
                                        rowList.add(0, "数据与多条唯一性字段重复");
                                        errorList.add(rowList);
                                        return;
                                    }
                                }
                            }
                            String province = ObjectUtil.isEmpty(rowList.get(kv.getInt("province"))) ? "" : rowList.get(kv.getInt("province")).toString();
                            String city = ObjectUtil.isEmpty(rowList.get(kv.getInt("city"))) ? "" : rowList.get(kv.getInt("city")).toString();
                            String site = ObjectUtil.isEmpty(rowList.get(kv.getInt("site"))) ? "" : rowList.get(kv.getInt("site")).toString();
                            if (StrUtil.isNotEmpty(city) && (!areaMap.containsKey(province) || !areaMap.get(province).contains(city))) {
                                rowList.add(0, "省市区不合要求，请核对");
                                errorList.add(rowList);
                                return;
                            }
                            if (StrUtil.isNotEmpty(site) && (!areaMap.containsKey(city) || !areaMap.get(city).contains(site))) {
                                rowList.add(0, "省市区不合要求，请核对");
                                errorList.add(rowList);
                                return;
                            }
                            JSONObject object = new JSONObject();
                            JSONObject entityObject = new JSONObject();
                            entityObject
                                    .fluentPut("customer_name", rowList.get(kv.getInt("customer_name")))
                                    .fluentPut("mobile", rowList.get(kv.getInt("mobile")))
                                    .fluentPut("telephone", rowList.get(kv.getInt("telephone")))
                                    .fluentPut("website", rowList.get(kv.getInt("website")))
                                    .fluentPut("next_time", rowList.get(kv.getInt("next_time")))
                                    .fluentPut("remark", rowList.get(kv.getInt("remark")))
                                    .fluentPut("address", province + "," + city + "," + site)
                                    .fluentPut("owner_user_id", ownerUserId);
                            if (customer != null) {
                                boolean auth = AuthUtil.isCrmAuth(CrmEnum.CRM_CUSTOMER, customer.getCustomerId());
                                if (auth) {
                                    rowList.add(0, "数据无覆盖权限");
                                    errorList.add(rowList);
                                    return;
                                }
                                entityObject.put("customer_id", customer.getCustomerId());
                                entityObject.put("batch_id", customer.getBatchId());
                            }
                            object.put("field", addFieldArray(rowList));
                            object.put("entity", entityObject);
                            Db.tx(() -> {
                                R r = Aop.get(CrmCustomerService.class).addOrUpdate(object, null);
                                if (!r.get("code").equals(0)) {
                                    rowList.add(0, r.get("msg"));
                                    errorList.add(rowList);
                                    return false;
                                }
                                return true;
                            });
                        } catch (Exception ex) {
                            Log.getLog(getClass()).error("导入数据异常:", ex);
                            rowList.add(0, "导入异常");
                            errorList.add(rowList);
                        }
                    }
                } else if (rowIndex == 1) {
                    queryExcelHead(rowList);
                } else {
                    errorList.add(0,rowList);
                }
            });
        }


        private void leadsImport() {
            ExcelUtil.readBySax(filePath, 0, (int sheetIndex, int rowIndex, List<Object> rowList) -> {
                num++;
                if (interrupted) {
                    return;
                }
                if (rowList.size() < kv.entrySet().size()) {
                    for (int i = rowList.size() - 1; i < kv.entrySet().size(); i++) {
                        rowList.add(null);
                    }
                }
                if(num>=10000){
                    rowList.add(0, "最多同时导入10000条数据");
                    errorList.add(rowList);
                    return;
                }
                if (rowIndex > 1) {
                    if (templateErr) {
                        rowList.add(0, "请使用最新的模板");
                        errorList.add(rowList);
                    } else {
                        try {
                            CrmLeads leads = null;
                            for (Integer integer : isNullList) {
                                if (ObjectUtil.isEmpty(rowList.get(integer))) {
                                    rowList.add(0, "必填字段未填写");
                                    errorList.add(rowList);
                                    return;
                                }
                            }
                            for (Record record : uniqueList) {
                                record.set("value", rowList.get(kv.getInt(record.getStr("field_name"))));
                                Integer i = Aop.get(AdminFieldService.class).verify(crmEnum, record);
                                if (i > 0) {
                                    if(repeatHandling.equals(2)){
                                        return;
                                    }
                                    if (leads == null) {
                                        record.set("primaryKey", TableMapping.me().getTable(CrmLeads.class).getPrimaryKey()[0]);
                                        record.set("tableName", crmEnum.getName());
                                        leads = CrmLeads.dao.template("admin.field.queryFieldValue", record.getColumns()).findFirst();
                                    } else {
                                        rowList.add(0, "数据与多条唯一性字段重复");
                                        errorList.add(rowList);
                                        return;
                                    }
                                }
                            }
                            JSONObject object = new JSONObject();
                            JSONObject entityObject = new JSONObject();
                            entityObject
                                    .fluentPut("leads_name", rowList.get(kv.getInt("leads_name")))
                                    .fluentPut("mobile", rowList.get(kv.getInt("mobile")))
                                    .fluentPut("telephone", rowList.get(kv.getInt("telephone")))
                                    .fluentPut("next_time", rowList.get(kv.getInt("next_time")))
                                    .fluentPut("remark", rowList.get(kv.getInt("remark")))
                                    .fluentPut("address", rowList.get(kv.getInt("address")))
                                    .fluentPut("owner_user_id", ownerUserId);
                            if (leads != null) {
                                boolean auth = AuthUtil.isCrmAuth(CrmEnum.CRM_LEADS, leads.getLeadsId());
                                if (auth) {
                                    rowList.add(0, "数据无覆盖权限");
                                    errorList.add(rowList);
                                    return;
                                }
                                entityObject.put("leads_id", leads.getLeadsId());
                                entityObject.put("batch_id", leads.getBatchId());
                            }
                            object.put("field", addFieldArray(rowList));
                            object.put("entity", entityObject);
                            Db.tx(() -> {
                                R r = Aop.get(CrmLeadsService.class).addOrUpdate(object);
                                if (!r.get("code").equals(0)) {
                                    rowList.add(0, r.get("msg"));
                                    errorList.add(rowList);
                                    return false;
                                }
                                return true;
                            });
                        } catch (Exception ex) {
                            Log.getLog(getClass()).error("导入数据异常:", ex);
                            rowList.add(0, "导入异常");
                            errorList.add(rowList);
                        }
                    }
                } else if (rowIndex == 1) {
                    queryExcelHead(rowList);
                }else {
                    errorList.add(0,rowList);
                }
            });
        }

        private void contactsImport() {
            ExcelUtil.readBySax(filePath, 0, (int sheetIndex, int rowIndex, List<Object> rowList) -> {
                num++;
                if (interrupted) {
                    return;
                }
                if (rowList.size() < kv.entrySet().size()) {
                    for (int i = rowList.size() - 1; i < kv.entrySet().size(); i++) {
                        rowList.add(null);
                    }
                }
                if(num>=10000){
                    rowList.add(0, "最多同时导入10000条数据");
                    errorList.add(rowList);
                    return;
                }
                if (rowIndex > 1) {
                    if (templateErr) {
                        rowList.add(0, "请使用最新的模板");
                        errorList.add(rowList);
                    } else {
                        try {
                            String customeName = rowList.get(kv.getInt("customer_id")).toString();
                            CrmCustomer customer = CrmCustomer.dao.findFirstByCache("CUSTOME_NAME", customeName, "select customer_id from 72crm_crm_customer where customer_name = ? limit 1", customeName);
                            if (customer == null) {
                                rowList.add(0, "填写的客户不存在");
                                errorList.add(rowList);
                                return;
                            }

                            CrmContacts contacts = null;
                            for (Integer integer : isNullList) {
                                if (ObjectUtil.isEmpty(rowList.get(integer))) {
                                    rowList.add(0, "必填字段未填写");
                                    errorList.add(rowList);
                                    return;
                                }
                            }
                            for (Record record : uniqueList) {
                                record.set("value", rowList.get(kv.getInt(record.getStr("field_name"))));
                                Integer i = Aop.get(AdminFieldService.class).verify(crmEnum, record);
                                if (i > 0) {
                                    if(repeatHandling.equals(2)){
                                        return;
                                    }
                                    if (contacts == null) {
                                        record.set("primaryKey", TableMapping.me().getTable(CrmContacts.class).getPrimaryKey()[0]);
                                        record.set("tableName", crmEnum.getName());
                                        contacts = CrmContacts.dao.template("admin.field.queryFieldValue", record.getColumns()).findFirst();
                                    } else {
                                        rowList.add(0, "数据与多条唯一性字段重复");
                                        errorList.add(rowList);
                                        return;
                                    }
                                }
                            }
                            JSONObject object = new JSONObject();
                            JSONObject entityObject = new JSONObject();
                            entityObject
                                    .fluentPut("name", rowList.get(kv.getInt("name")))
                                    .fluentPut("customer_id", customer.getCustomerId())
                                    .fluentPut("mobile", rowList.get(kv.getInt("mobile")))
                                    .fluentPut("telephone", rowList.get(kv.getInt("telephone")))
                                    .fluentPut("email", rowList.get(kv.getInt("email")))
                                    .fluentPut("post", rowList.get(kv.getInt("post")))
                                    .fluentPut("next_time", rowList.get(kv.getInt("next_time")))
                                    .fluentPut("remark", rowList.get(kv.getInt("remark")))
                                    .fluentPut("address", rowList.get(kv.getInt("address")))
                                    .fluentPut("owner_user_id", ownerUserId);
                            if (contacts != null) {
                                boolean auth = AuthUtil.isCrmAuth(CrmEnum.CRM_CONTACTS, contacts.getContactsId());
                                if (auth) {
                                    rowList.add(0, "数据无覆盖权限");
                                    errorList.add(rowList);
                                    return;
                                }
                                entityObject.put("contacts_id", contacts.getContactsId());
                                entityObject.put("batch_id", contacts.getBatchId());
                            }
                            object.put("field", addFieldArray(rowList));
                            object.put("entity", entityObject);
                            Db.tx(() -> {
                                R r = Aop.get(CrmContactsService.class).addOrUpdate(object);
                                if (!r.get("code").equals(0)) {
                                    rowList.add(0, r.get("msg"));
                                    errorList.add(rowList);
                                    return false;
                                }
                                return true;
                            });
                        } catch (Exception ex) {
                            Log.getLog(getClass()).error("导入数据异常:", ex);
                            rowList.add(0, "导入异常");
                            errorList.add(rowList);
                        }
                    }
                } else if (rowIndex == 1) {
                    queryExcelHead(rowList);
                }else {
                    errorList.add(0,rowList);
                }
            });
        }

        private void productImport() {

            ExcelUtil.readBySax(filePath, 0, (int sheetIndex, int rowIndex, List<Object> rowList) -> {
                num++;
                if (interrupted) {
                    return;
                }
                if (rowList.size() < kv.entrySet().size()) {
                    for (int i = rowList.size() - 1; i < kv.entrySet().size(); i++) {
                        rowList.add("");
                    }
                }
                if(num>=10000){
                    rowList.add(0, "最多同时导入10000条数据");
                    errorList.add(rowList);
                    return;
                }
                if (rowIndex > 1) {
                    if (templateErr) {
                        rowList.add(0, "请使用最新的模板");
                        errorList.add(rowList);
                    } else {
                        try {
                            String categoryName = rowList.get(kv.getInt("category_id")).toString();
                            CrmProductCategory category = CrmProductCategory.dao.findFirstByCache("CATEGORY_NAME", categoryName, "select category_id from 72crm_crm_product_category where name = ? limit 1", categoryName);
                            if (category == null) {
                                rowList.add(0, "填写的产品类型不存在");
                                errorList.add(rowList);
                                return;
                            }
                            CrmProduct product = null;
                            for (Integer integer : isNullList) {
                                if (ObjectUtil.isEmpty(rowList.get(integer))) {
                                    rowList.add(0, "必填字段未填写");
                                    errorList.add(rowList);
                                    return;
                                }
                            }
                            for (Record record : uniqueList) {
                                record.set("value", rowList.get(kv.getInt(record.getStr("field_name"))));
                                Integer i = Aop.get(AdminFieldService.class).verify(crmEnum, record);
                                if(repeatHandling.equals(2)){
                                    return;
                                }
                                if (i > 0) {
                                    if (product == null) {
                                        record.set("primaryKey", TableMapping.me().getTable(CrmProduct.class).getPrimaryKey()[0]);
                                        record.set("tableName", crmEnum.getName());
                                        product = CrmProduct.dao.template("admin.field.queryFieldValue", record.getColumns()).findFirst();
                                    } else {
                                        rowList.add(0, "数据与多条唯一性字段重复");
                                        errorList.add(rowList);
                                        return;
                                    }
                                }
                            }
                            JSONObject object = new JSONObject();
                            JSONObject entityObject = new JSONObject();
                            if (product != null) {
                                boolean auth = AuthUtil.isCrmAuth(CrmEnum.CRM_PRODUCT, product.getProductId());
                                if (auth) {
                                    rowList.add(0, "数据无覆盖权限");
                                    errorList.add(rowList);
                                    return;
                                }
                                entityObject.put("product_id", product.getProductId());
                                entityObject.put("batch_id", product.getBatchId());
                            }
                            entityObject
                                    .fluentPut("name", rowList.get(kv.getInt("name")))
                                    .fluentPut("num", rowList.get(kv.getInt("num")))
                                    .fluentPut("price", rowList.get(kv.getInt("price")))
                                    .fluentPut("category_id", category.getCategoryId())
                                    .fluentPut("description", rowList.get(kv.getInt("description")))
                                    .fluentPut("owner_user_id", ownerUserId);
                            object.put("field", addFieldArray(rowList));
                            object.put("entity", entityObject);
                            Db.tx(() -> {
                                R r = Aop.get(CrmProductService.class).saveAndUpdate(object);
                                if (!r.get("code").equals(0)) {
                                    rowList.add(0, r.get("msg"));
                                    errorList.add(rowList);
                                    return false;
                                }
                                return true;
                            });
                        } catch (Exception ex) {
                            Log.getLog(getClass()).error("导入数据异常:", ex);
                            rowList.add(0, "导入异常");
                            errorList.add(rowList);
                        }
                    }
                } else if (rowIndex == 1) {
                    queryExcelHead(rowList);
                }else {
                    errorList.add(0,rowList);
                }
            });
        }

        /**
         * 查询导入顺序
         *
         * @param rowList 行数据
         * @author zhangzhiwei
         */
        private void queryExcelHead(List<Object> rowList) {
            fieldList = Aop.get(AdminFieldService.class).queryAddField(crmEnum);
            fieldList.removeIf(record -> Arrays.asList("file", "checkbox", "user", "structure").contains(record.getStr("formType")));
            HashMap<String, String> nameMap = new HashMap<>();
            HashMap<String, Integer> isNullMap = new HashMap<>();
            fieldList.forEach(record -> {
                if (crmEnum.getType() == CrmEnum.CRM_CUSTOMER.getType() && "map_address".equals(record.getStr("field_name"))) {
                    nameMap.put("省", "province");
                    nameMap.put("市", "city");
                    nameMap.put("区", "site");
                    isNullMap.put("省", 0);
                    isNullMap.put("市", 0);
                    isNullMap.put("区", 0);
                } else {
                    if (record.get("is_unique", 0).equals(1)) {
                        uniqueList.add(record);
                    }
                    if (record.getInt("is_null").equals(1)) {
                        nameMap.put(record.getStr("name") + "(*)", record.getStr("field_name"));
                        isNullMap.put(record.getStr("name") + "(*)", record.getInt("is_null"));
                    } else {
                        nameMap.put(record.getStr("name"), record.getStr("field_name"));
                        isNullMap.put(record.getStr("name"), record.getInt("is_null"));
                    }
                }

            });
            List<String> nameList = new ArrayList<>(nameMap.keySet());
            if (nameList.size() != rowList.size() || !nameList.containsAll(rowList)) {
                templateErr = true;
            } else {
                for (int i = 0; i < rowList.size(); i++) {
                    kv.set(nameMap.get(rowList.get(i).toString()), i);
                    if (1 == isNullMap.get(rowList.get(i).toString())) {
                        isNullList.add(i);
                    }
                }
            }
            rowList.add(0, "错误原因");
            errorList.add(rowList);
        }

        /**
         * 将自定义字段组装成想要的格式
         *
         * @param rowList 行数据
         * @return 自定义字段数组
         */
        private JSONArray addFieldArray(List<Object> rowList) {
            JSONArray jsonArray = new JSONArray();
            fieldList.forEach(record -> {
                if (record.getInt("field_type").equals(0)) {
                    Integer columnsNum = kv.getInt(record.getStr("name"));
                    record.set("value", rowList.get(columnsNum));
                    jsonArray.add(JSONObject.parseObject(record.toJson()));
                }
            });
            return jsonArray;
        }
    }
}
