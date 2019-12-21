package com.kakarote.crm9.common.annotation;

import java.lang.annotation.*;

/**
 * 系统日志注解
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Inherited
@Documented
public @interface SysLog {
    public String value() default "";
}
