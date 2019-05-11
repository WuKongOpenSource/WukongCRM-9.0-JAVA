package com.kakarote.crm9.common.annotation;

import com.jfinal.plugin.activerecord.Model;

import java.lang.annotation.*;

/**
 * 数据分页注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE,ElementType.PARAMETER })
@Inherited
@Documented
@Deprecated
public @interface PageEntity {
    Class<? extends Model> value();
}
