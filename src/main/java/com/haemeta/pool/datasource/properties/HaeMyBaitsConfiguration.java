package com.haemeta.pool.datasource.properties;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisMapperRegistry;
import com.baomidou.mybatisplus.core.MybatisXMLLanguageDriver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

public class HaeMyBaitsConfiguration extends MybatisConfiguration {
    private String name;
    public String getName() {
        return name;
    }
    public HaeMyBaitsConfiguration setName(String name) {
        this.name = name;
        return this;
    }
}
