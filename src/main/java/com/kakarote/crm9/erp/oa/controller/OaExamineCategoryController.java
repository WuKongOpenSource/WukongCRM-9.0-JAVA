package com.kakarote.crm9.erp.oa.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.oa.entity.OaExamineCategory;
import com.kakarote.crm9.erp.oa.entity.OaExamineStep;
import com.kakarote.crm9.erp.oa.service.OaExamineCategoryService;
import com.kakarote.crm9.utils.TagUtil;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;

import java.util.*;


/**
 * 审批类型
 *
 * @author hmb
 */
public class OaExamineCategoryController extends Controller {

    @Inject
    private OaExamineCategoryService oaExamineCategoryService;

    /**
     * 设置审批类型
     *
     * @author hmb
     */
    @Permissions("manage:oa")
    public void setExamineCategory() {
        JSONObject jsonObject = JSON.parseObject(getRawData());
        OaExamineCategory oaExamineCategory = new OaExamineCategory();
        List<OaExamineStep> oaExamineSteps = new ArrayList<>();
        oaExamineCategory.setCategoryId(jsonObject.getInteger("id"));
        oaExamineCategory.setTitle(jsonObject.getString("title"));
        oaExamineCategory.setRemarks(jsonObject.getString("remarks"));
        oaExamineCategory.setExamineType(jsonObject.getInteger("examineType"));
        if (jsonObject.getJSONArray("user_ids") != null) {
            List<Integer> list = jsonObject.getJSONArray("user_ids").toJavaList(Integer.class);
            oaExamineCategory.setUserIds(TagUtil.fromSet(new HashSet<>(list)));
        }
        if (jsonObject.getJSONArray("dept_ids") != null) {
            List<Integer> list = jsonObject.getJSONArray("dept_ids").toJavaList(Integer.class);
            oaExamineCategory.setDeptIds(TagUtil.fromSet(new HashSet<>(list)));
        }
        oaExamineCategory.setCreateTime(new Date());
        JSONArray step = jsonObject.getJSONArray("step");
        for (int i = 0; i < step.size(); i++) {
            OaExamineStep oaExamineStep = new OaExamineStep();
            JSONObject jsonObject1 = step.getJSONObject(i);
            if (jsonObject1.getJSONArray("checkUserId") != null) {
                List<Integer> list = jsonObject1.getJSONArray("checkUserId").toJavaList(Integer.class);
                oaExamineStep.setCheckUserId(TagUtil.fromSet(new HashSet<>(list)));
            }
            oaExamineStep.setStepNum(i + 1);
            oaExamineStep.setStepType(jsonObject1.getInteger("stepType"));
            oaExamineSteps.add(oaExamineStep);
        }
        renderJson(oaExamineCategoryService.setExamineCategory(oaExamineCategory, oaExamineSteps));
    }

    /**
     * 查询审批类型列表
     *
     * @param basePageRequest 分页对象
     * @author hmb
     */
    public void queryExamineCategoryList(BasePageRequest<Void> basePageRequest) {
        renderJson(oaExamineCategoryService.queryExamineCategoryList(basePageRequest));
    }

    /**
     * 查询审批类型列表
     *
     * @author hmb
     */
    public void queryAllExamineCategoryList() {
        renderJson(oaExamineCategoryService.queryAllExamineCategoryList());
    }


    /**
     * 删除审批类型
     *
     * @author hmb
     */
    @Permissions("manage:oa")
    public void deleteExamineCategory() {
        String id = getPara("id");
        renderJson(oaExamineCategoryService.deleteExamineCategory(id));
    }


    /**
     * 查询系统用户列表
     *
     * @author hmb
     */
    public void queryUserList() {
        renderJson(oaExamineCategoryService.queryUserList());
    }

    /**
     * 查询部门
     *
     * @author hmb
     */
    public void queryDeptList() {
        renderJson(oaExamineCategoryService.queryDeptList());
    }

    /**
     * 查询审批类型详情
     *
     * @author hmb
     */
    public void queryExamineCategoryById() {
        String id = getPara("id");
        renderJson(oaExamineCategoryService.queryExamineCategoryById(id));
    }

    /**
     * 启用/禁用
     */
    @Permissions("manage:oa")
    public void updateStatus() {
        String id = getPara("id");
        renderJson(oaExamineCategoryService.updateStatus(id));
    }


}
