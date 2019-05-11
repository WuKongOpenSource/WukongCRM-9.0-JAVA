package com.kakarote.crm9.erp.crm.service;

import com.kakarote.crm9.erp.crm.entity.CrmProductCategory;
import com.kakarote.crm9.utils.R;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CrmProductCategoryService {

    /**
     * 根据id查询类别
     */
    public CrmProductCategory queryById(Integer id){
        return CrmProductCategory.dao.findById(id);
    }
    /**
     * 添加或修改类别
     */
    public R saveAndUpdate(CrmProductCategory category){
        if (category.getCategoryId() == null){
            return category.save()?R.ok():R.error();
        }else {
            return category.update()?R.ok():R.error();
        }
    }
    /**
     * 根据ID删除类别
     */
    public R deleteById(Integer id){
        Integer number = Db.queryInt(Db.getSql("crm.product.queryByCategoryId"),id);
        Integer catagoryNum = Db.queryInt(Db.getSql("crm.product.queryCategoryByParentId"),id);
        if (number > 0){
            return R.error("该产品类别已关联产品，不能删除！");
        }
        if (catagoryNum > 0 ){
            return R.error("该类别下有其他产品类别！");
        }
        return CrmProductCategory.dao.deleteById(id)?R.ok():R.error();
    }
    /**
     * 迭代查询全部产品类别
     */
    public R queryList(){
        return R.ok().put("data",queryListById(null,0));
    }

    public List<Record> queryListByPid(Integer pid){
        StringBuffer sql = new StringBuffer("select * from 72crm_crm_product_category ");
        if (pid != null){
            sql.append(" where pid = ").append(pid);
        }
        return Db.find(sql.toString());
    }
    /**
     * 递归查询全部产品类别
     */
    public List<Record> queryListById(List<Record> records,Integer level){
        if (records == null){
            records = queryListByPid(0);
            return queryListById(records,level);
        }else {
            level = level + 1;
            for (Record c :records) {
                c.set("level",level);
                c.set("label",c.getStr("name"));
                List<Record> list =  queryListById(queryListByPid(c.getInt("category_id")),level);
                if (list != null && list.size() != 0){
                    c.set("children",list);
                }

            }
            return records;
        }
    }

    public List<Integer> queryId(List<Integer> list,Integer categoryId){

        if (list == null){
            list = new ArrayList<>();
        }
        list.add(categoryId);
        CrmProductCategory productCategory = CrmProductCategory.dao.findById(categoryId);
        if (productCategory != null && productCategory.getPid() != 0){
            queryId(list,productCategory.getPid());
        }else {
            Collections.reverse(list);
        }

        return list;
    }
}
