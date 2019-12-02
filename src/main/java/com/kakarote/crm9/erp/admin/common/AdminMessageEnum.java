package com.kakarote.crm9.erp.admin.common;


/**
 * 消息通知枚举类
 */
public enum AdminMessageEnum {

    NULL(0,"NULL"),
    OA_TASK_ALLOCATION(1,"分配给我的任务"),
    OA_TASK_JOIN(2,"我参与的任务"),
    OA_TASK_OVER(3,"任务结束通知"),
    OA_LOG_REPLY(4,"日志回复提醒"),
    OA_LOG_SEND(5,"日志发送提醒"),
    OA_EXAMINE_REJECT(6,"OA审批拒绝提醒"),
    OA_EXAMINE_PASS(7,"OA审批通过提醒"),
    OA_NOTICE_MESSAGE(8,"公告通知提醒"),
    OA_EVENT_MESSAGE(9,"日程通知"),
    CRM_CONTRACT_REJECT(10,"合同拒绝通知"),
    CRM_CONTRACT_PASS(11,"合同全部通过通知"),
    CRM_RECEIVABLES_REJECT(12,"回款拒绝通知"),
    CRM_RECEIVABLES_PASS(13,"回款通过通知"),
    CRM_CUSTOMER_IMPORT(14,"导入客户通知"),
    CRM_CUSTOMER_IMPORT_CANCEL(15,"导入客户取消通知"),
    CRM_CONTACTS_IMPORT(16,"导入联系人通知"),
    CRM_CONTACTS_IMPORT_CANCEL(17,"导入联系人取消通知"),
    CRM_LEADS_IMPORT(18,"导入线索通知"),
    CRM_LEADS_IMPORT_CANCEL(19,"导入线索取消通知"),
    CRM_PRODUCT_IMPORT(20,"导入产品通知"),
    CRM_PRODUCT_IMPORT_CANCEL(21,"导入产品取消通知"),
    CRM_BUSINESS_USER(22,"商机团队成员通知"),
    CRM_CUSTOMER_USER(23,"客户团队成员通知"),
    CRM_CONTRACT_USER(24,"合同团队成员通知")
    ;

    AdminMessageEnum(Integer type,String remarks){
        this.type=type;
        this.remarks=remarks;
    }
    private final int  type;
    private final String remarks;

    public int getType() {
        return type;
    }

    public String getRemarks() {
        return remarks;
    }

    public static AdminMessageEnum parse(int type) {
        for (AdminMessageEnum Enum : AdminMessageEnum.values()) {
            if (Enum.getType()==type) {
                return Enum;
            }
        }
        return NULL;
    }
}
