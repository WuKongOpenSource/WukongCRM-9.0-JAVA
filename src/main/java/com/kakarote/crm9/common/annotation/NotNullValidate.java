package com.kakarote.crm9.common.annotation;

import java.lang.annotation.*;

/**
 * 非空校验注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD,ElementType.TYPE})
@Inherited
@Documented
@Repeatable(NotNullValidates.class)
public @interface NotNullValidate {
    String value();
    HttpEnum type() default HttpEnum.PARA;
    String message() default "参数错误";

}
