package com.haemeta.pool.datasource.annotation;

import org.mybatis.spring.annotation.MapperScannerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({MapperScannerPool.RepeatingRegistrar.class})
public @interface MapperScansPlus {
    MapperScanPlus [] value();
}
