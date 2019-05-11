package com.kakarote.crm9.common.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD,ElementType.TYPE})
@Inherited
@Documented
@Repeatable(NotNullValidates.class)
/**
 * 非空校验注解，暂定
 */
public @interface NotNullValidate {
    String value();
    HttpEnum type() default HttpEnum.PARA;
    String message() default "参数错误";

}
