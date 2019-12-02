package com.kakarote.crm9.erp.crm.common;

/**
 * @author hmb
 */

public enum FormTypeEnum{
    /**
     * 自定义字段类型
     */
    TEXT(1,"text"),
    TEXTAREA(2,"textarea"),
    SELECT(3,"select"),
    DATE(4,"date"),
    NUMBER(5,"number"),
    FLOATNUMBER(6,"floatnumber"),
    MOBILE(7,"mobile"),
    FILE(8,"file"),
    CHECKBOX(9,"checkbox"),
    USER(10,"user"),
    STRUCTURE(12,"structure"),
    DATETIME(13,"datetime"),
    EMAIL(14,"email"),
    CUSTOMER(15,"customer"),
    BUSINESS(16,"business"),
    CONTACTS(17,"contacts"),
    MAP_ADDRESS(18,"map_address"),
    CATEGORY(19,"category"),
    CONTRACT(20,"contract"),
    RECEIVABLES_PLAN(21,"receivables_plan"),
    BUSINESS_CAUSE(22,"business_cause"),
    EXAMINE_CAUSE(23,"examine_cause"),
    NULL(0,"null")
    ;

    FormTypeEnum(int type, String formType){
        this.type = type;
        this.formType = formType;
    }

    private int type;
    private String formType;

    public static FormTypeEnum parse(int type) {
        for (FormTypeEnum Enum : FormTypeEnum.values()) {
            if (Enum.getType()==type) {
                return Enum;
            }
        }
        return NULL;
    }

    public int getType(){
        return type;
    }

    public String getFormType(){
        return formType;
    }}
