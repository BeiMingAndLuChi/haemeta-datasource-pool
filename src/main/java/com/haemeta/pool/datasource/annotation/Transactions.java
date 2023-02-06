package com.haemeta.pool.datasource.annotation;

import com.haemeta.pool.datasource.config.datasource.MasterDSConfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Transactions {

    boolean master() default true;

    boolean secondary() default false;

    boolean dynamic() default false;

    boolean dynamicOnly() default false;

}
