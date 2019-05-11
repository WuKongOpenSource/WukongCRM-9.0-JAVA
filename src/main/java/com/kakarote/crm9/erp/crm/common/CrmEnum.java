package com.kakarote.crm9.erp.crm.common;

public enum CrmEnum {
    /**
     * CRM Enum
     */
    PRODUCT_TYPE_KEY("产品", "4"),
    CONTACTS_TYPE_KEY("联系人", "3"),
    CUSTOMER_TYPE_KEY("客户", "2"),
    LEADS_TYPE_KEY("线索", "1"),
    CONTRACT_TYPE_KEY("合同","6"),
    RECEIVABLES_TYPE_KEY("回款","7"),
    BUSINESS_TYPE_KEY("商机","5");
    private String name;
    private String types;
    CrmEnum(String name, String types) {
        this.name = name;
        this.types = types;
    }
    public static String getName(String types) {
        for (CrmEnum c : CrmEnum.values()) {
            if (c.getTypes().equals(types)) {
                return c.name;
            }
        }
        return null;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getTypes() {
        return types;
    }

    public void setTypes(String types) {
        this.types = types;
    }
}
