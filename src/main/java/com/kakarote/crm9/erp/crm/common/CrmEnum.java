package com.kakarote.crm9.erp.crm.common;

public enum CrmEnum {
    /**
     * CRM Enum
     */
    CRM_LEADS("crm_leads",1,"线索"),
    CRM_CUSTOMER("crm_customer",2,"客户"),
    CRM_CONTACTS("crm_contacts",3,"联系人"),
    CRM_PRODUCT("crm_product", 4,"产品"),
    CRM_BUSINESS("crm_business",5,"商机"),
    CRM_CONTRACT("crm_contract",6,"合同"),
    CRM_RECEIVABLES("crm_receivables",7,"回款"),
    CRM_RECEIVABLES_PLAN("crm_receivables_plan",8,"回款计划"),
    CRM_CUSTOMER_POOL("crm_customer_pool",9,"客户公海"),
    CRM_NULL("NULL",0,"NULL")
    ;

    private final String name;
    private final int  type;
    private final String remarks;

    CrmEnum(String name, int type,String remarks) {
        this.name = name;
        this.type = type;
        this.remarks = remarks;
    }
    public static CrmEnum parse(int type) {
        for (CrmEnum Enum : CrmEnum.values()) {
            if (Enum.getType()==type) {
                return Enum;
            }
        }
        return CRM_NULL;
    }

    public static CrmEnum parse(String name) {
        for (CrmEnum Enum : CrmEnum.values()) {
            if (Enum.getName().equals(name)) {
                return Enum;
            }
        }
        return CRM_NULL;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public String getRemarks() {
        return remarks;
    }

}
