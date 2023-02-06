package com.haemeta.pool.datasource.properties;

import com.zaxxer.hikari.HikariConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConditionalOnMissingBean(value = HikariConfigClone.class)
@ConfigurationProperties( prefix = "spring.datasource.hikari" )
public class HikariConfigClone extends HikariConfig implements Cloneable{
    @Override
    public HikariConfigClone clone() throws CloneNotSupportedException {
        return (HikariConfigClone) super.clone();
    }
}
