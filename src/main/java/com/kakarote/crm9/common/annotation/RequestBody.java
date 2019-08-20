package com.kakarote.crm9.common.annotation;

import java.lang.annotation.*;

/**
 * json数据注入
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Inherited
@Documented
public @interface RequestBody {
}
