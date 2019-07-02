package com.kakarote.crm9.erp.crm.common;

public enum CrmEnum {
    /**
     * CRM Enum
     */
    LEADS_TYPE_KEY("线索", "1","CrmLeads"),
    CUSTOMER_TYPE_KEY("客户", "2","CrmCustomer"),
    CONTACTS_TYPE_KEY("联系人", "3","CrmContacts"),
    PRODUCT_TYPE_KEY("产品", "4","CrmProduct"),
    BUSINESS_TYPE_KEY("商机","5","CrmBusiness"),
    CONTRACT_TYPE_KEY("合同","6","CrmContract"),
    RECEIVABLES_TYPE_KEY("回款","7","CrmReceivables");

    private final String name;
    private final String types;
    private final String sign;
    CrmEnum(String name, String types,String sign) {
        this.name = name;
        this.types = types;
        this.sign = sign;
    }
    public static String getName(String types) {
        for (CrmEnum c : CrmEnum.values()) {
            if (c.getTypes().equals(types)) {
                return c.name;
            }
        }
        return null;
    }

    public static String getSign(Integer types) {
        for (CrmEnum c : CrmEnum.values()) {
            if (c.getTypes().equals(types.toString())) {
                return c.sign;
            }
        }
        return "error";
    }

    public String getName() {
        return name;
    }

    public String getTypes() {
        return types;
    }


    public String getSign(){
        return sign;
    }

}
