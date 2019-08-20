package com.kakarote.crm9.erp.crm.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.log.Log;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.service.AdminFieldService;
import com.kakarote.crm9.erp.admin.service.AdminSceneService;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.entity.CrmProduct;
import com.kakarote.crm9.utils.AuthUtil;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.FieldUtil;
import com.kakarote.crm9.utils.R;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CrmProductService {

    @Inject
    private AdminFieldService adminFieldService;

    @Inject
    private FieldUtil fieldUtil;

    @Inject
    private CrmProductCategoryService crmProductCategoryService;

    @Inject
    private CrmRecordService crmRecordService;

    @Inject
    private AdminSceneService adminSceneService;

    @Inject
    private AuthUtil authUtil;

    /**
     * 分页条件查询产品
     */
    public Page<Record> queryPage(BasePageRequest<CrmProduct> basePageRequest) {
        return Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("crm.product.getProductPageList"));
    }

    /**
     * 添加或修改产品
     *
     * @param jsonObject
     */
    @Before(Tx.class)
    public R saveAndUpdate(JSONObject jsonObject) {
        CrmProduct crmProduct = jsonObject.getObject("entity", CrmProduct.class);
        String batchId = StrUtil.isNotEmpty(crmProduct.getBatchId()) ? crmProduct.getBatchId() : IdUtil.simpleUUID();
        crmRecordService.updateRecord(jsonObject.getJSONArray("field"), batchId);
        adminFieldService.save(jsonObject.getJSONArray("field"), batchId);
        if (crmProduct.getProductId() == null) {
            Integer product = Db.queryInt(Db.getSql("crm.product.getByNum"),crmProduct.getNum());
            if (product != 0){
                return R.error("产品编号已存在，请校对后再添加！");
            }
            crmProduct.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
            crmProduct.setCreateTime(DateUtil.date());
            crmProduct.setUpdateTime(DateUtil.date());
            crmProduct.setOwnerUserId(BaseUtil.getUser().getUserId().intValue());
            crmProduct.setBatchId(batchId);
            boolean save = crmProduct.save();
            crmRecordService.addRecord(crmProduct.getProductId(), CrmEnum.PRODUCT_TYPE_KEY.getTypes());
            return save ? R.ok() : R.error();
        } else {
            CrmProduct oldCrmProduct = new CrmProduct().dao().findById(crmProduct.getProductId());
            crmRecordService.updateRecord(oldCrmProduct, crmProduct, CrmEnum.PRODUCT_TYPE_KEY.getTypes());
            crmProduct.setUpdateTime(DateUtil.date());
        }
        return crmProduct.update() ? R.ok() : R.error();
    }

    /**
     * 根据id查询产品
     */
    public R queryById(Integer id) {
        Record record = Db.findFirst("select * from productview where product_id = ?", id);
        return R.ok().put("data", record);
    }

    /**
     * 根据id查询产品基本信息
     */
    public List<Record> information(Integer id) {
        Record record = Db.findFirst("select * from productview where product_id = ?", id);
        if (record == null) {
            return null;
        }
        List<Record> fieldList = new ArrayList<>();
        FieldUtil field = new FieldUtil(fieldList);
        field.set("产品名称", record.getStr("name"))
                .set("产品类别", record.getStr("category_name"))
                .set("产品编码", record.getStr("num"))
                .set("标准价格", record.getStr("price"))
                .set("产品描述", record.getStr("description"));
        List<Record> recordList = Db.find(Db.getSql("admin.field.queryCustomField"),record.getStr("batch_id"));
        fieldUtil.handleType(recordList);
        fieldList.addAll(recordList);
        return fieldList;
    }

    /**
     * 根据id删除产品
     */
    public R deleteById(Integer id) {
        CrmProduct product = CrmProduct.dao.findById(id);
        if (product != null) {
            Db.delete("delete FROM 72crm_admin_fieldv where batch_id = ?",product.getBatchId());
        }

        return CrmProduct.dao.deleteById(id) ? R.ok() : R.error();
    }

    /**
     * 上架或者下架
     */
    public R updateStatus(String ids, Integer status) {
        List<Record> recordList = Db.find("select batch_id from 72crm_crm_product where  product_id in (" + ids + ")");
        StringBuilder batchIds = new StringBuilder();
        for (Record record : recordList) {
            if (batchIds.length() == 0) {
                batchIds.append("'").append(record.getStr("batch_id")).append("'");
            } else {
                batchIds.append(",'").append(record.getStr("batch_id")).append("'");
            }
        }
        String a;
        if (status == 0) {
            a = "下架";
        } else {
            a = "上架";
        }
        StringBuilder sqlfield = new StringBuilder("update 72crm_admin_fieldv set value = '" + a + "' where name = '是否上下架' and batch_id in ( ");
        sqlfield.append(batchIds.toString());
        sqlfield.append(" )");
        int f = Db.update(sqlfield.toString());
        return R.isSuccess(f > 0);
    }

    /**
     * @author wyq
     * 查询编辑字段
     */
    public List<Record> queryField(Integer productId) {
        Record product = Db.findFirst("select * from productview where product_id = ?",productId);
        List<Integer> list = crmProductCategoryService.queryId(null, product.getInt("category_id"));
        Integer[] categoryIds = new Integer[list.size()];
        categoryIds = list.toArray(categoryIds);
        product.set("category_id",categoryIds);
        return adminFieldService.queryUpdateField(4,product);
    }

    /**
     * @author wyq
     * 产品导出
     */
    public List<Record> exportProduct(String productIds) {
        String[] productIdsArr = productIds.split(",");
        return Db.find(Db.getSqlPara("crm.product.excelExport", Kv.by("ids", productIdsArr)));
    }

    /**
     * @author wyq
     * 获取产品导入查重字段
     */
    public R getCheckingField(){
        return R.ok().put("data","产品名称");
    }

    /**
     * 导入产品
     *
     * @author zxy
     */
    public R uploadExcel(UploadFile file, Integer repeatHandling, Integer ownerUserId) {
        ExcelReader reader = ExcelUtil.getReader(FileUtil.file(file.getUploadPath() + "\\" + file.getFileName()));
        AdminFieldService adminFieldService = new AdminFieldService();
        Kv kv = new Kv();
        Integer errNum = 0;
        try {
            List<List<Object>> read = reader.read();
            List<Object> list = read.get(1);
            List<Record> recordList = adminFieldService.customFieldList("4");
            recordList.removeIf(record -> "file".equals(record.getStr("formType")) || "checkbox".equals(record.getStr("formType"))|| "user".equals(record.getStr("formType"))|| "structure".equals(record.getStr("formType")));
            List<Record> fieldList = adminFieldService.queryAddField(4);
            fieldList.removeIf(record -> "file".equals(record.getStr("formType")) || "checkbox".equals(record.getStr("formType"))|| "user".equals(record.getStr("formType"))|| "structure".equals(record.getStr("formType")));
            fieldList.forEach(record -> {
                if (record.getInt("is_null") == 1){
                    record.set("name",record.getStr("name")+"(*)");
                }
            });
            List<String> nameList = fieldList.stream().map(record -> record.getStr("name")).collect(Collectors.toList());
            if (nameList.size() != list.size() || !nameList.containsAll(list)){
                return R.error("请使用最新导入模板");
            }
            Kv nameMap = new Kv();
            fieldList.forEach(record -> nameMap.set(record.getStr("name"),record.getStr("field_name")));
            for (int i = 0; i < list.size(); i++) {
                kv.set(nameMap.get(list.get(i)), i);
            }
            if (read.size() > 2) {
                JSONObject object = new JSONObject();
                for (int i = 2; i < read.size(); i++) {
                    errNum = i;
                    List<Object> productList = read.get(i);
                    if (productList.size() < list.size()) {
                        for (int j = productList.size() - 1; j < list.size(); j++) {
                            productList.add(null);
                        }
                    }
                    String productName = productList.get(kv.getInt("name")).toString();
                    Integer number = Db.queryInt("select count(*) from 72crm_crm_product where name = ?", productName);
                    Integer categoryId = Db.queryInt("select category_id from 72crm_crm_product_category where name = ?",productList.get(kv.getInt("产品类型(*)")));
                    if (categoryId == null){
                        return R.error("第"+errNum+1+"行填写的产品类型不存在");
                    }
                    if (0 == number) {
                        object.fluentPut("entity", new JSONObject().fluentPut("name", productName)
                                .fluentPut("num", productList.get(kv.getInt("num")))
                                .fluentPut("unit", productList.get(kv.getInt("unit")))
                                .fluentPut("price", productList.get(kv.getInt("price")))
                                .fluentPut("category_id", categoryId)
                                .fluentPut("description", productList.get(kv.getInt("description")))
                                .fluentPut("owner_user_id", ownerUserId));
                    } else if (number > 0 && repeatHandling == 1) {
                        Record product = Db.findFirst("select product_id,batch_id from 72crm_crm_product where name = ?", productName);
                        object.fluentPut("entity", new JSONObject().fluentPut("product_id", product.getInt("product_id"))
                                .fluentPut("name", productName)
                                .fluentPut("num", productList.get(kv.getInt("num")))
                                .fluentPut("unit", productList.get(kv.getInt("unit")))
                                .fluentPut("price", productList.get(kv.getInt("price")))
                                .fluentPut("category_id", categoryId)
                                .fluentPut("description", productList.get(kv.getInt("description")))
                                .fluentPut("owner_user_id", ownerUserId)
                                .fluentPut("batch_id", product.getStr("batch_id")));
                    } else if (number > 0 && repeatHandling == 2) {
                        continue;
                    }
                    JSONArray jsonArray = new JSONArray();
                    for (Record record : recordList) {
                        record.set("value", productList.get(kv.getInt(record.getStr("name"))!=null?kv.getInt(record.getStr("name")):kv.getInt(record.getStr("name")+"(*)")));
                        jsonArray.add(JSONObject.parseObject(record.toJson()));
                    }
                    object.fluentPut("field", jsonArray);
                    saveAndUpdate(object);
                }
            }
        }catch (Exception e) {
            Log.getLog(getClass()).error("",e);
            if (errNum != 0){
                return R.error("第" + (errNum+1) + "行错误!");
            }
            return R.error();
        } finally {
            reader.close();
        }
        return R.ok();
    }
    /**
     * @author zxy
     * 获取上架商品
     */
    public R queryByStatus(BasePageRequest<CrmProduct> basePageRequest) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.fluentPut("status",new JSONObject().fluentPut("name","status").fluentPut("condition","is").fluentPut("value","1"));
        basePageRequest.setJsonObject(jsonObject);
        return adminSceneService.getCrmPageList(basePageRequest);
    }
}
