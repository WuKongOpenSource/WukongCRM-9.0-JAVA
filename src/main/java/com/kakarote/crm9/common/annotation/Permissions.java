package com.kakarote.crm9.common.annotation;

import java.lang.annotation.*;

/**
 * 权限标识
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE,ElementType.PARAMETER })
@Inherited
@Documented
public @interface Permissions {
    String[] value();
}
