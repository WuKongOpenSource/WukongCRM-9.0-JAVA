package com.kakarote.crm9.erp.crm.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.service.AdminFieldService;
import com.kakarote.crm9.erp.admin.service.AdminSceneService;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.entity.CrmProduct;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
            crmProduct.setStatus(1);
            crmProduct.setCreateUserId(BaseUtil.getUser().getUserId());
            crmProduct.setCreateTime(DateUtil.date());
            crmProduct.setUpdateTime(DateUtil.date());
            crmProduct.setOwnerUserId(BaseUtil.getUser().getUserId());
            crmProduct.setBatchId(batchId);
            boolean save = crmProduct.save();
            crmRecordService.addRecord(crmProduct.getProductId(), CrmEnum.CRM_PRODUCT);
            return save ? R.ok() : R.error();
        } else {
            CrmProduct oldCrmProduct = new CrmProduct().dao().findById(crmProduct.getProductId());
            crmRecordService.updateRecord(oldCrmProduct, crmProduct, CrmEnum.CRM_PRODUCT);
            crmProduct.setUpdateTime(DateUtil.date());
        }
        return crmProduct.update() ? R.ok() : R.error();
    }

    /**
     * 根据id查询产品
     */
    public Record queryById(Integer id) {
        Record crmProduct = Db.findFirst(Db.getSql("crm.product.queryById"),id);
        List<Record> recordList = Db.find("select name,value from `72crm_admin_fieldv` where batch_id = ?", crmProduct.getStr("batch_id"));
        recordList.forEach(field->crmProduct.set(field.getStr("name"),field.getStr("value")));
        return crmProduct;
    }

    /**
     * 根据id查询产品基本信息
     */
    public List<Record> information(Integer id) {
        Record record = queryById(id);
        List<String> keyList = Arrays.asList("name", "num","price","description");
        List<Record> recordList = adminFieldService.queryInformation(CrmEnum.CRM_PRODUCT,record, keyList);
        recordList.add(new Record().set("name","产品类型").set("value",new Record().set("categoryId",record.getInt("category_id")).set("categoryName",record.getStr("categoryName"))).set("formType","category").set("field_type",1));
        return recordList.stream().sorted(Comparator.comparingInt(r->-r.getInt("field_type"))).map(r-> r.remove("field_type","field_name","setting","type")).collect(Collectors.toList());
    }

    /**
     * 根据id列表删除产品
     */
    @Before(Tx.class)
    public R deleteByIds(String productIds) {
        List<String> ids=StrUtil.splitTrim(productIds,",");
        for (String id : ids) {
            CrmProduct product = CrmProduct.dao.findById(id);
            if (product != null) {
                Db.delete("delete FROM 72crm_admin_fieldv where batch_id = ?",product.getBatchId());
                product.setStatus(3);
                product.update();
            }
        }
        return R.ok();
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
        Record product = queryById(productId);
        List<Integer> list = crmProductCategoryService.queryId(null, product.getInt("category_id"));
        Integer[] categoryIds = new Integer[list.size()];
        categoryIds = list.toArray(categoryIds);
        product.set("category_id",categoryIds);
        return adminFieldService.queryUpdateField(CrmEnum.CRM_PRODUCT.getType(),product);
    }

    /**
     * @author wyq
     * 获取产品导入查重字段
     */
    public R getCheckingField(){
        return R.ok().put("data","产品名称");
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
