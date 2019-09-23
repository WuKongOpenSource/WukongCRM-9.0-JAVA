package com.kakarote.crm9.erp.bi.controller;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.bi.service.BiWorkService;
import com.kakarote.crm9.utils.R;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BiWorkController extends Controller {

    @Inject
    private BiWorkService biWorkService;


    /**
     * 查询日志统计信息
     * @author zhang
     */
    @Permissions("bi:oa:read")
    public void logStatistics(@Para("deptId")Integer deptId, @Para("userId")Long userId, @Para("type")String type){
        renderJson(R.ok().put("data",biWorkService.logStatistics(deptId,userId,type)));
    }

    /**
     * 导出日志信息
     * @author zhangzhiwei
     */
    public void logStatisticsExport(@Para("deptId")Integer deptId, @Para("userId")Long userId, @Para("type")String type)throws IOException {
        List<Record> recordList=biWorkService.logStatistics(deptId,userId,type);
        List<Map<String,Object>> mapList=new LinkedList<>();
        recordList.forEach(record -> mapList.add(record.remove("img","user_id","username").getColumns()));
        ExcelWriter writer=null;
        try {
            writer = ExcelUtil.getWriter();
            writer.addHeaderAlias("realname", "员工");
            writer.addHeaderAlias("count", "填写数");
            writer.addHeaderAlias("unReadCont", "接收人未读数");
            writer.addHeaderAlias("unCommentCount", "未评论数");
            writer.addHeaderAlias("commentCount", "已评论数");
            writer.setColumnWidth(0,20).setColumnWidth(1,20).setColumnWidth(2,30).setColumnWidth(3,20).setColumnWidth(4,20);
            HttpServletResponse response = getResponse();
            writer.write(mapList, true);
            //自定义标题别名
            //response为HttpServletResponse对象
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
            response.setHeader("Content-Disposition", "attachment;filename=logStatistics.xls");
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
        }finally {
            if(writer!=null){
                writer.close();
            }
        }
        renderNull();
    }

    /**
     * 查询审批统计信息
     * @author zhang
     */
    @Permissions("bi:oa:read")
    public void examineStatistics(@Para("deptId")Integer deptId, @Para("userId")Long userId, @Para("type")String type){
        renderJson(R.ok().put("data",biWorkService.examineStatistics(deptId,userId,type)));
    }

    /**
     * 导出日志信息
     * @author zhangzhiwei
     */
    @SuppressWarnings("unchecked")
    public void examineStatisticsExport(@Para("deptId")Integer deptId, @Para("userId")Long userId, @Para("type")String type)throws IOException {
        JSONObject object=biWorkService.examineStatistics(deptId,userId,type);
        List<Map<String,Object>> mapList=new LinkedList<>();
        ExcelWriter writer=null;
        try {
            writer = ExcelUtil.getWriter();
            List<Record> categoryList= (List<Record>) object.get("categoryList");
            writer.addHeaderAlias("realname", "员工");
            for (Record record : categoryList) {
                writer.addHeaderAlias("count_"+record.get("category_id"), record.get("title"));
            }
            writer.setColumnWidth(0,20).setColumnWidth(1,20).setColumnWidth(2,30).setColumnWidth(3,20).setColumnWidth(4,20);
            HttpServletResponse response = getResponse();
            ((List<Record>)object.get("userList")).forEach(record -> mapList.add(record.remove("img","user_id","username").getColumns()));
            writer.write(mapList, true);
            //自定义标题别名
            //response为HttpServletResponse对象
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
            response.setHeader("Content-Disposition", "attachment;filename=examineStatistics.xls");
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
        }finally {
            if(writer!=null){
                writer.close();
            }
        }
        renderNull();
    }
    /**
     * 查询审批详情
     * @author zhangzhiwei
     */
    public void examineInfo(BasePageRequest basePageRequest){
        renderJson(R.ok().put("data",biWorkService.examineInfo(basePageRequest)));
    }
}
