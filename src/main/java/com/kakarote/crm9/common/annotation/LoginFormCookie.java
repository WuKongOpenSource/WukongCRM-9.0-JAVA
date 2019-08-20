package com.kakarote.crm9.common.annotation;

import java.lang.annotation.*;

/**
 * 有此注解的方法会尝试从cookie获取登录状态
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface LoginFormCookie {
}
