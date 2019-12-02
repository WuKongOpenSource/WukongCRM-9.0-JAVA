package com.kakarote.crm9.erp.oa.common;

public enum OaEnum {
    /**
     * OaEnum
     */
    COMMENT_TYPE_KEY( "评论", 6),
    LOG_TYPE_KEY ( "日志", 1 ),
    EXAMINE_TYPE_KEY ( "审批", 5 ),
    TASK_TYPE_KEY ( "任务", 4 ),
    EVENT_TYPE_KEY ( "日程", 2 ),
    ANNOUNCEMENT_TYPE_KEY ( "公告", 3 ),
    ALL_TYPE_KEY ( "全部", 0 );
    private String name;
    private Integer types;

    OaEnum(String name, Integer types) {
        this.name = name;
        this.types = types;
    }

    public static String getName(Integer types) {
        for (OaEnum c : OaEnum.values ()) {
            if ( c.getTypes ().equals ( types ) ) {
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

    public Integer getTypes() {
        return types;
    }

    public void setTypes(Integer types) {
        this.types = types;
    }

}
