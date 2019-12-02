package com.kakarote.crm9.erp.oa.service;

import com.kakarote.crm9.erp.admin.entity.AdminField;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminFieldService;
import com.kakarote.crm9.erp.oa.entity.OaExamineCategory;
import com.kakarote.crm9.erp.oa.entity.OaExamineStep;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
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
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class OaExamineCategoryService{

    @Inject
    private FieldUtil fieldUtil;

    @Inject
    private AdminFieldService adminFieldService;

    @Before(Tx.class)
    public R setExamineCategory(OaExamineCategory oaExamineCategory, List<OaExamineStep> examineStepList){
        boolean bol;
        Integer categoryId;
        if(oaExamineCategory.getCategoryId() == null){
            oaExamineCategory.setCreateUserId(BaseUtil.getUser().getUserId());
            oaExamineCategory.setCreateTime(new Date());
            oaExamineCategory.setUpdateTime(new Date());
            bol = oaExamineCategory.save();
            categoryId = oaExamineCategory.getCategoryId();
            AdminField content = new AdminField();
            content.setName("审批事由");
            content.setFieldName("content");
            content.setMaxLength(0);
            content.setType(2);
            content.setLabel(10);
            content.setIsNull(1);
            content.setUpdateTime(new Date());
            content.setOperating(1);
            content.setFieldType(1);
            content.setExamineCategoryId(categoryId);
            content.save();
            content.setFieldId(null);
            content.setFieldName("remark");
            content.setIsNull(0);
            content.setName("备注");
            content.save();
        }else{
            oaExamineCategory.setUpdateTime(new Date());
            categoryId = oaExamineCategory.getCategoryId();
            bol = oaExamineCategory.update();
        }
        //设置审批步骤
        if(oaExamineCategory.getExamineType() == 1){
            if(examineStepList.size() != 0){
                Db.delete("delete from 72crm_oa_examine_step where category_id = ?", categoryId);
                examineStepList.forEach(x -> {
                    x.setCategoryId(categoryId);
                    x.save();
                });
            }
        }
        return bol ? R.ok().put("data", Kv.by("categoryId",oaExamineCategory.getCategoryId())) : R.error();
    }

    public R queryExamineCategoryList(BasePageRequest basePageRequest){
        Page<Record> paginate = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), "select * ", "from 72crm_oa_examine_category where is_deleted = 0");
        paginate.getList().forEach(record -> {
                    List<Record> stepList = Db.find("select * from 72crm_oa_examine_step where category_id = ?", record.getStr("category_id"));
                    stepList.forEach(step -> {
                        if(step.getStr("check_user_id") != null && step.getStr("check_user_id").split(",").length > 0){
                            List<Record> userList = Db.find(Db.getSqlPara("admin.user.queryByIds", Kv.by("ids", step.getStr("check_user_id").split(","))));
                            step.set("userList", userList);
                        }else{
                            step.set("userList", new ArrayList<>());
                        }
                    });
                    record.set("stepList", stepList);
                    if(record.getStr("user_ids") != null && record.getStr("user_ids").split(",").length > 0){
                        List<Record> userList = Db.find(Db.getSqlPara("admin.user.queryByIds", Kv.by("ids", record.getStr("user_ids").split(","))));
                        record.set("userIds", userList);
                    }else{
                        record.set("userIds", new ArrayList<>());
                    }
                    if(record.getStr("dept_ids") != null && record.getStr("dept_ids").split(",").length > 0){
                        List<Record> deptList = Db.find(Db.getSqlPara("admin.dept.queryByIds", Kv.by("ids", record.getStr("dept_ids").split(","))));
                        record.set("deptIds", deptList);
                    }else{
                        record.set("deptIds", new ArrayList<>());
                    }
                }
        );
        return R.ok().put("data", paginate);
    }

    public R deleteExamineCategory(String id){
        int update = Db.update("update 72crm_oa_examine_category set is_deleted = 1,delete_user_id = ?,delete_time = now() where category_id = ?", BaseUtil.getUser().getUserId(), id);
        return update > 0 ? R.ok() : R.error();
    }


    public R queryUserList(){
        List<Record> recordList = Db.find("select a.user_id,a.realname,a.username,a.img,b.name as deptName from 72crm_admin_user a left join 72crm_admin_dept b on a.dept_id = b.dept_id");
        return R.ok().put("data", recordList);
    }

    public R queryDeptList(){
        List<Record> recordList = Db.find("select * from 72crm_admin_dept");
        return R.ok().put("data", recordList);
    }

    public R queryExamineCategoryById(String id){
        Record examineCategory = Db.findFirst("select * from 72crm_oa_examine_category where category_id = ?", id);
        List<Record> stepList = Db.find("select a.*,b.realname,b.img from 72crm_oa_examine_step a left join 72crm_admin_user b on a.user_id = b.user_id where category_id = ?", id);
        examineCategory.set("stepList", stepList);
        return R.ok().put("data", examineCategory);
    }

    public R queryAllExamineCategoryList(){
        AdminUser user = BaseUtil.getUser();
        List<Record> recordList = Db.find(Db.getSqlPara("oa.examine.queryAllExamineCategoryList",Kv.by("userId",user.getUserId()).set("deptId",user.getDeptId())));
        return R.ok().put("data", recordList);
    }

    public List<Record> queryField(Integer id){
        List<Record> list = Db.find("select * from `72crm_admin_field` where examine_category_id = ?", id);
        adminFieldService.recordToFormType(list);
        return list;
    }

    public List<Record> queryField(){
        List<Record> fieldList = new LinkedList<>();
        String[] settingArr = new String[]{};
        List<Record> fixedFieldList = adminFieldService.list(10);
        fieldUtil.getFixedField(fieldList, "title", "审批内容","", "text", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "remark", "备注", "","text", settingArr, 1);
        fieldList.addAll(fixedFieldList);
        return fieldList;
    }

    public List<Record> queryFieldList(){
        List<Record> fieldList = new ArrayList<>();
        fieldUtil.addListHead(fieldList, "title", "审批内容");
        fieldUtil.addListHead(fieldList, "remark", "备注");
        fieldList.addAll(adminFieldService.list(10));
        return fieldList;
    }

    public R updateStatus(String id){
        int update = Db.update("update 72crm_oa_examine_category set status = abs(status-1) where category_id = ?", id);
        return update > 0 ? R.ok() : R.error();
    }


}
