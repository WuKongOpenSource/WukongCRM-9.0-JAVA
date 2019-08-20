package com.kakarote.crm9.erp.crm.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.service.AdminExamineRecordService;
import com.kakarote.crm9.erp.admin.service.AdminFieldService;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.entity.CrmReceivables;
import com.kakarote.crm9.erp.crm.entity.CrmReceivablesPlan;
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
import com.jfinal.plugin.activerecord.SqlPara;
import com.jfinal.plugin.activerecord.tx.Tx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CrmReceivablesService {

    @Inject
    private AdminFieldService adminFieldService;

    @Inject
    private FieldUtil fieldUtil;

    @Inject
    private CrmRecordService crmRecordService;

    @Inject
    private AdminExamineRecordService examineRecordService;

    @Inject
    private AuthUtil authUtil;

    /**
     * 获取用户审核通过和未通过的回款
     *
     * @param userId 用户ID
     * @author HJP
     */
    public List<CrmReceivables> queryListByUserId(Integer userId) {
        CrmReceivables crmReceivables = new CrmReceivables();
        String sql = "select re.receivables_id,re.contract_id,c.`name`,r.check_time,r.check_user_id,u.username,re.check_status from 72crm_crm_receivables re"
                + "left join 72crm_crm_contract c"
                + "on c.contract_id=re.contract_id"
                + "left join 72crm_admin_examine_step s"
                + "on re.flow_id=s.flow_id and re.order_id=s.order_id"
                + "left join 72crm_admin_examine_record r"
                + "on s.flow_id=r.flow_id and s.step_id=r.step_id"
                + "left join 72crm_admin_user u"
                + "on r.check_user_id=u.id"
                + "where re.check_status=2 or re.check_status=3 and re.create_user_id=" + userId;
        return crmReceivables.find(sql);
    }

    /**
     * 分页查询回款
     */
    public Page<Record> queryPage(BasePageRequest<CrmReceivables> basePageRequest) {
        return Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("crm.receivables.getReceivablesPageList"));
    }

    /**
     * 新建或者修改回款
     *
     * @param jsonObject
     */
    @Before(Tx.class)
    public R saveOrUpdate(JSONObject jsonObject) {
        CrmReceivables crmReceivables = jsonObject.getObject("entity", CrmReceivables.class);
        crmReceivables.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
        String batchId = StrUtil.isNotEmpty(crmReceivables.getBatchId()) ? crmReceivables.getBatchId() : IdUtil.simpleUUID();
        crmRecordService.updateRecord(jsonObject.getJSONArray("field"), batchId);
        adminFieldService.save(jsonObject.getJSONArray("field"), batchId);
        if (crmReceivables.getReceivablesId() == null) {
           Integer count =  Db.queryInt(Db.getSql("crm.receivables.queryByNumber"),crmReceivables.getNumber());
           if (count!=null&&count > 0){
               return R.error("回款编号已存在，请校对后再添加！");
           }
            crmReceivables.setCreateTime(DateUtil.date());
            crmReceivables.setUpdateTime(DateUtil.date());
            crmReceivables.setBatchId(batchId);
            crmReceivables.setCheckStatus(0);
            crmReceivables.setOwnerUserId(BaseUtil.getUser().getUserId().intValue());
            Map<String, Integer> map = examineRecordService.saveExamineRecord(2, jsonObject.getLong("checkUserId"), crmReceivables.getOwnerUserId(), null);
            if (map.get("status") == 0) {
                return R.error("没有启动的审核步骤，不能添加！");
            } else {
                crmReceivables.setExamineRecordId(map.get("id"));
            }
            boolean save = crmReceivables.save();
            CrmReceivablesPlan crmReceivablesPlan = CrmReceivablesPlan.dao.findById(crmReceivables.getPlanId());
            if (crmReceivablesPlan != null) {
                crmReceivablesPlan.setReceivablesId(crmReceivables.getReceivablesId());
                crmReceivablesPlan.setUpdateTime(DateUtil.date());
                crmReceivablesPlan.update();
            }
            crmRecordService.addRecord(crmReceivables.getReceivablesId(), CrmEnum.RECEIVABLES_TYPE_KEY.getTypes());
            return R.isSuccess(save);
        } else {
            CrmReceivables receivables = CrmReceivables.dao.findById(crmReceivables.getReceivablesId());
            if (receivables.getCheckStatus() != 4 && receivables.getCheckStatus() != 3) {
                return R.error("不能编辑，请先撤回再编辑！");
            }
            Map<String, Integer> map = examineRecordService.saveExamineRecord(2, jsonObject.getLong("checkUserId"), receivables.getOwnerUserId(), receivables.getExamineRecordId());
            if (map.get("status") == 0) {
                return R.error("没有启动的审核步骤，不能添加！");
            } else {
                crmReceivables.setExamineRecordId(map.get("id"));
            }
            crmRecordService.updateRecord(new CrmReceivables().dao().findById(crmReceivables.getReceivablesId()), crmReceivables, CrmEnum.RECEIVABLES_TYPE_KEY.getTypes());
            crmReceivables.setCheckStatus(0);
            crmReceivables.setUpdateTime(DateUtil.date());
            CrmReceivablesPlan crmReceivablesPlan = CrmReceivablesPlan.dao.findById(crmReceivables.getPlanId());
            if (crmReceivablesPlan != null) {
                crmReceivablesPlan.setReceivablesId(crmReceivables.getReceivablesId());
                crmReceivablesPlan.setUpdateTime(DateUtil.date());
                crmReceivablesPlan.update();
            }
            return crmReceivables.update() ? R.ok() : R.error();
        }
    }

    /**
     * 根据id查询回款
     */
    public R queryById(Integer id) {
        Record record = Db.findFirst(Db.getSqlPara("crm.receivables.queryReceivablesById", Kv.by("id", id)));
        return R.ok().put("data", record);
    }

    /**
     * 根据id查询回款基本信息
     */
    public List<Record> information(Integer id) {
        Record record = Db.findFirst(Db.getSqlPara("crm.receivables.queryReceivablesById", Kv.by("id", id)));
        if (record == null) {
            return null;
        }
        List<Record> fieldList = new ArrayList<>();
        FieldUtil field = new FieldUtil(fieldList);
        field.set("回款编号", record.getStr("number"))
                .set("客户名称", record.getStr("customer_name"))
                .set("合同编号", record.getStr("contract_num"))
                .set("回款日期", DateUtil.formatDate(record.getDate("return_time")))
                .set("回款金额", record.getStr("money"))
                .set("期数", record.getStr("plan_num"))
                .set("备注", record.getStr("remark"));
        List<Record> recordList = Db.find(Db.getSql("admin.field.queryCustomField"),record.getStr("batch_id"));
        fieldUtil.handleType(recordList);
        fieldList.addAll(recordList);
        return fieldList;
    }

    /**
     * 根据id删除回款
     */
    @Before(Tx.class)
    public R deleteByIds(String receivablesIds) {
        String[] idsArr = receivablesIds.split(",");
        List<CrmReceivables> list = CrmReceivables.dao.find(Db.getSqlPara("crm.receivablesplan.queryReceivablesReceivablesId", Kv.by("receivablesIds", idsArr)));
        if (list.size() > 0) {
            return R.error("该数据已被其他模块引用，不能被删除！");
        }
        for (String id : idsArr) {
                CrmReceivables receivables = CrmReceivables.dao.findById(id);
            if (receivables != null) {
                Db.delete("delete FROM 72crm_admin_fieldv where batch_id = ?",receivables.getBatchId());
            }
            if (!CrmReceivables.dao.deleteById(id)){
                return R.error();
            }
        }
            return R.ok();
    }

    /**
     * @author wyq
     * 查询编辑字段
     */
    public List<Record> queryField(Integer receivablesId) {
        Record receivables = Db.findFirst("select * from receivablesview where receivables_id = ?",receivablesId);
        List<Record> list = new ArrayList<>();
        list.add(new Record().set("customer_id",receivables.getInt("customer_id")).set("customer_name",receivables.getStr("customer_name")));
        receivables.set("customer_id",list);
        list = new ArrayList<>();
        list.add(new Record().set("contract_id", receivables.getStr("contract_id")).set("contract_num", receivables.getStr("contract_num")));
        receivables.set("contract_id",list);
        return adminFieldService.queryUpdateField(7,receivables);
    }

    /**
     * 根据条件查询回款
     */
    public List<Record> queryList(CrmReceivables receivables) {
        String sq = "select * from 72crm_crm_receivables where 1 = 1 ";
        StringBuffer sql = new StringBuffer(sq);
        if (receivables.getCustomerId() != null) {
            sql.append(" and customer_id = ").append(receivables.getCustomerId());
        }
        if (receivables.getContractId() != null) {
            sql.append(" and contract_id = ").append(receivables.getContractId());
        }
        return Db.find(sql.toString());
    }

    /**
     * 根据条件查询回款
     */
    public List<Record> queryListByType(String type, Integer id) {
        String sq = "select * from 72crm_crm_receivables where ";
        StringBuffer sql = new StringBuffer(sq);
        if (type.equals(CrmEnum.CUSTOMER_TYPE_KEY.getTypes())) {
            sql.append("  customer_id = ? ");
        }
        if (type.equals(CrmEnum.CONTRACT_TYPE_KEY.getTypes())) {
            sql.append("  contract_id = ? ");
        }

        return Db.find(sql.toString(), id);
    }

    /**
     * 根据合同id查询回款
     */
    public R qureyListByContractId(BasePageRequest<CrmReceivables> basePageRequest) {
        Integer pageType = basePageRequest.getPageType();
        if (0 == pageType) {
            return R.ok().put("data", Db.find(Db.getSql("crm.receivables.queryReceivablesPageList"),basePageRequest.getData().getContractId()));
        } else {
            return R.ok().put("data", Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(),new SqlPara().setSql(Db.getSql("crm.receivables.queryReceivablesPageList")).addPara(basePageRequest.getData().getContractId())));
        }
    }
}
