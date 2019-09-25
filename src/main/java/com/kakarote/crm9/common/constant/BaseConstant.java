package com.kakarote.crm9.common.constant;

import com.jfinal.plugin.activerecord.Db;
import com.kakarote.crm9.utils.BaseUtil;

import java.io.Serializable;

/**
 * 一些基础配置
 */
public class BaseConstant implements Serializable {

    public static final String NAME = "72CRM";

    public static final String VERSION = "1.3.3";

    private static final long serialVersionUID = 1L;
    /**
     * 默认的上传文件路径
     * TODO 默认配置文件路径
     */
    public final static String UPLOAD_PATH = BaseUtil.isWindows() ? "D:/upload/" : "/usr/local/upload/";


    /**
     * 角色类型列表
     */
    public static final Integer[] ROLE_TYPES = {1, 2, 7, 8, 0};

    /**
     * 超级管理员的roleId
     */
    public static final Integer SUPER_ADMIN_ROLE_ID = 1;

    /**
     * 最终的超级管理员ID，不可被删除
     */
    public static final Long SUPER_ADMIN_USER_ID = 3L;
    /**
     * 查询数据权限递归次数
     */
    public static final int AUTH_DATA_RECURSION_NUM = 20;

    /**
     * 用户登录错误缓存key
     */
    public static final String USER_LOGIN_ERROR_KEY = "LOGIN_ERROR_USER_";

    /**
     * 项目管理员角色ID
     */
    public  static  Integer WORK_ADMIN_ROLE_ID ;
    /**
     * 每个项目管理员角色ID
     */
    public static  Integer SMALL_WORK_ADMIN_ROLE_ID ;

    /**
     * 每个项目编辑角色ID
     */
    public static  Integer SMALL_WORK_EDIT_ROLE_ID;

    public static final String PUBILC_KEY="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA4ZiC5JTMV6C12+cuxDoBBUN3wYdHnSF/sUV9a6lST4GpQTpBkC5vvMhb5mqnDzzAkOiRgrbjBqTuf6PVSgJt+b2rsV2A1zvhXLdVhU4Zw2R+iHDb+mXvZnFXKkwjb5+GMkX2tj4DxqEq3QP0XZHxuvpl5h7kdIIp/QS80Pk7jdlSi92TY5C62j6WU2Cw0SC0Ie4bOJQL5E5WphY3k6xJekpHh6y5k1utgV4RVCyxGnBpp1S0u2aupa1gd0A/AEjfv6uJJh6B8HY8DUgWgLEb0jGF5IviUKPu5B2GcLtZg45jjzZfcIt5gLVxBo9w870khq95MkSW0kULtG+1NVjz2wIDAQAB";

    public static final String PRIVATE_KEY="MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDhmILklMxXoLXb5y7EOgEFQ3fBh0edIX+xRX1rqVJPgalBOkGQLm+8yFvmaqcPPMCQ6JGCtuMGpO5/o9VKAm35vauxXYDXO+Fct1WFThnDZH6IcNv6Ze9mcVcqTCNvn4YyRfa2PgPGoSrdA/RdkfG6+mXmHuR0gin9BLzQ+TuN2VKL3ZNjkLraPpZTYLDRILQh7hs4lAvkTlamFjeTrEl6SkeHrLmTW62BXhFULLEacGmnVLS7Zq6lrWB3QD8ASN+/q4kmHoHwdjwNSBaAsRvSMYXki+JQo+7kHYZwu1mDjmOPNl9wi3mAtXEGj3DzvSSGr3kyRJbSRQu0b7U1WPPbAgMBAAECggEBALerQJ4zmy6YtJdl4Ah4pQ4RAPY6Wo7OkDTWu6ckwtPiX9ewQ2LMu8I6ab/uAzd/odQY1SdUGP/21gpra6KG2dzjlF6iHtydbvzacrFGhekz72TMZfy2czO3gVz5gjxF568UkSmM0ewsmUY6CwEHaoRtivrRGfTVaLQL4GJPGDKCPkdhGIzM6Cj0ngh1I3slMiKF8ArRdYOyd8ixez2MsMjtf0rAHNOVQ+XsQ2siIIahqMyC7tjCz2C2m5SFHYtEuVjGSuy7cYpjDP5xnQLQZk3OqimoMOpzY4LsM0iA2TIbDa1T6AzmTbQQVNRhnt0tEiW97FzifFLocqNrQB7vuHECgYEA/L+dTbU05oPyPK0HVR/cnHN+g7Xyp6U18WjTHN0sH1nQ7tTnksJTY83bT0nodG2T+IXICujboAMAaHuPXE1befKfM5UMzWi2ABdrIB2uUuhXnwBHMkXDhoVMwapVVIi2H9fF8zfkuJLnfyugUTQgIcJNGUzbwPfrK3aZiQ/bWFkCgYEA5H95RqpHQGZ0KH7I9PpvYKdPi00Fe7FirbhP1VrPEZjgDukiAvuR4jv2IXfFQhkV9ia1XeXIx/cQw1NvlhH+8MYRqob6O5JsYA1q8s41LlvR6Kfe+spM8zqWkcoY16PkSzf2MzCfcsjDxhXqvNJ5XG94oaRsTtp1LZHJFgg751MCgYEA+hFzx9DD2FcwMsxXduehVMIIqLQ6s5gIeSPnzKUf5Jzu1X6c6F2QOC0TEa3kal2Ii9mBhRnDQtv1aWrm+sInAc8FqPmNwyYY/JovCYWUVr8/Ajg6OQlBXTVCLlMjPhJNb9ADMJNanyvqE67MXcufBwfBVIjw3Enuyf/8BNpFgXECgYBIROkeqIyQmdbzV+ZHVU4Uy9YkwN+TF6+WMk1NtcL4VdnH3YScXOTvIPff7fm9xiFkESob4Kl4VXQ/0wvf8yEnFf5QaRCL9uvAFX5V/VJkrzXc1/t2ZrWf2E/3HGxvB68U+0YrOp2zuDvzUstDnW41BiypM0/uQLn/B7UJ+uC3cQKBgHAK5DGsvRB5vOGzcdY+gG37iydh1a6gAzF2C/pJY9GDGuZTcv6VC15zJ7QpKUAICWJoPRZObitwPz7S2xJjvzcL8ZiCjGmLcmVBS0dop5tks0vDmx+fOkj5NhHjOHIlT1id3gRmI4QGskr8or3/6JuHO13sSXNu2w6FcadI0Ljv";
}
