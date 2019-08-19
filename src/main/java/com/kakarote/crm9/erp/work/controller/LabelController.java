package com.kakarote.crm9.erp.work.controller;

/**
 *
 */
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.erp.work.entity.WorkTaskLabel;
import com.kakarote.crm9.erp.work.service.LabelService;

public class LabelController extends Controller{

    @Inject
    private LabelService labelService;
    /**
     * @author hmb
     * 设置任务标签
     * @param taskLabel 任务标签对象
     */
    public void setLabel(@Para("") WorkTaskLabel taskLabel){
        renderJson(labelService.setLabel(taskLabel));
    }
    /**
     * @author hmb
     * 删除任务标签
     */
    public void deleteLabel(){
        String labelId = getPara("labelId");
        renderJson(labelService.deleteLabel(labelId));
    }
    /**
     * @author hmb
     * 任务标签列表
     */
    public void getLabelList(){
        renderJson(labelService.getLabelList());
    }

    public void queryById(@Para("labelId")Integer labelId){
        renderJson(labelService.queryById(labelId));
    }

    /**
     * @author hmb
     * 根据用户参与的任务查询标签
     */
    public void getLabelListByOwn(){
        renderJson(labelService.getLabelListByOwn());
    }

    /**
     * @author wyq
     * 标签任务列表
     */
    @NotNullValidate(value = "labelId",message = "标签不能为空")
    public void getTaskList(@Para("labelId")Integer labelId){
        renderJson(labelService.getTaskList(labelId));
    }
}
