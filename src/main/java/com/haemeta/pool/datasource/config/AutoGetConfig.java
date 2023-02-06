package com.haemeta.pool.datasource.config;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.haemeta.pool.datasource.config.datasource.DynamicDSConfig;
import com.haemeta.pool.datasource.config.datasource.MasterDSConfig;
import com.haemeta.pool.datasource.config.datasource.SecondaryDSConfig;
import com.haemeta.pool.datasource.properties.datasource.DynamicMapperHikariConfig;
import com.haemeta.pool.datasource.properties.HikariConfigClone;
import com.haemeta.pool.datasource.properties.datasource.MasterMapperHikariConfig;
import com.haemeta.pool.datasource.properties.HaeMyBaitsConfiguration;
import com.haemeta.pool.datasource.properties.datasource.SecondaryMapperHikariConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
@ConditionalOnMissingBean(value = AutoGetConfig.class)
@EnableConfigurationProperties(
        value = {
                HikariConfigClone.class,
                MasterMapperHikariConfig.class,
                SecondaryMapperHikariConfig.class,
                DynamicMapperHikariConfig.class
        }
)
public class AutoGetConfig {

    @Resource
    private HikariConfigClone globalProperties;

    @Resource
    private MasterMapperHikariConfig masterMapperHikariConfig;

    @Resource
    private SecondaryMapperHikariConfig secondaryMapperHikariConfig;

    @Resource
    private DynamicMapperHikariConfig dynamicMapperHikariConfig;


    public HikariConfigClone cloneProperties(){
        try {
            return globalProperties.clone();
        } catch (CloneNotSupportedException e) {
            return new HikariConfigClone();
        }
    }

    @Bean
    @ConditionalOnMissingBean(value = GlobalConfig.class)
    @ConfigurationProperties(prefix = "mybatis-plus.global-config")
    public GlobalConfig globalConfig(){
        return new GlobalConfig().setIdentifierGenerator(new DefaultIdentifierGenerator());
    }


    @Bean(MasterDSConfig.configuration)
    @ConditionalOnMissingBean(name = MasterDSConfig.configuration)
    @ConfigurationProperties(prefix = "com.haemeta.master.mybatis")
    public HaeMyBaitsConfiguration haeMyBaitsConfigurationMaster(){
        return new HaeMyBaitsConfiguration().setName(MasterDSConfig.configuration);
    }

    @Bean(SecondaryDSConfig.configuration)
    @ConditionalOnMissingBean(name = SecondaryDSConfig.configuration)
    @ConfigurationProperties(prefix = "com.haemeta.master.secondary")
    public HaeMyBaitsConfiguration haeMyBaitsConfigurationSecondary(){
        return new HaeMyBaitsConfiguration().setName(SecondaryDSConfig.configuration);
    }

    @Bean(DynamicDSConfig.configuration)
    @ConditionalOnMissingBean(name = DynamicDSConfig.configuration)
    @ConfigurationProperties(prefix = "com.haemeta.dynamic.mybatis")
    public HaeMyBaitsConfiguration haeMyBaitsConfigurationDynamic(){
        return new HaeMyBaitsConfiguration().setName(DynamicDSConfig.configuration);
    }


    public MasterMapperHikariConfig getMasterMapperProperties() {
        return masterMapperHikariConfig;
    }

    public void setMasterMapperProperties(MasterMapperHikariConfig masterMapperHikariConfig) {
        this.masterMapperHikariConfig = masterMapperHikariConfig;
    }

    public DynamicMapperHikariConfig getDynamicMapperProperties() {
        return dynamicMapperHikariConfig;
    }

    public void setDynamicMapperProperties(DynamicMapperHikariConfig dynamicMapperHikariConfig) {
        this.dynamicMapperHikariConfig = dynamicMapperHikariConfig;
    }

    public SecondaryMapperHikariConfig getSecondaryMapperProperties() {
        return secondaryMapperHikariConfig;
    }

    public void setSecondaryMapperProperties(SecondaryMapperHikariConfig secondaryMapperHikariConfig) {
        this.secondaryMapperHikariConfig = secondaryMapperHikariConfig;
    }
}
