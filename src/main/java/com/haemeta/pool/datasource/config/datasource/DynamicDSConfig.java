package com.haemeta.pool.datasource.config.datasource;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.haemeta.common.utils.lang.StringUtil;
import com.haemeta.pool.datasource.config.AutoGetConfig;
import com.haemeta.pool.datasource.config.HaeDataSourceTransactionManager;
import com.haemeta.pool.datasource.entity.DataSourcePool;
import com.haemeta.pool.datasource.entity.DynamicDataSource;
import com.haemeta.pool.datasource.entity.pojo.HaemetaDS;
import com.haemeta.pool.datasource.properties.HaeMyBaitsConfiguration;
import com.haemeta.pool.datasource.properties.datasource.DynamicMapperHikariConfig;
import com.zaxxer.hikari.HikariConfig;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 2022年1月4日18:03:41
 * Master 主库 数据源的相关配置
 */
@AutoConfigureOrder(10000)
@org.springframework.context.annotation.Configuration
@ConditionalOnProperty(prefix = "com.haemeta.db", name = "dynamic", havingValue = "true")
public class DynamicDSConfig {

    public static final String configuration = "dynamicConfiguration";
    public static final String DataSource = "dynamicDataSource";
    public static final String DataSourceInit = "dynamic-datasource-init-0";
    public static final String SqlSessionFactory = "dynamicSqlSessionFactory";
    public static final String SqlSessionTemplate = "dynamicSqlSessionTemplate";
    public static final String TransactionManager = "dynamicTransactionManager";
    public static final String DataSourceNamePre = "system-dynamic-ds-";
    public static final Function<String, String> getDataSourceFullName = (append) -> DataSourceNamePre + append;
    public static final Integer ORDER = 100000;

    @Resource
    public AutoGetConfig autoGetConfig;

    @Resource(name = configuration)
    public HaeMyBaitsConfiguration haeMyBaitsConfiguration;

    private DynamicMapperHikariConfig config;

    public HikariConfig getConfig() {
        return config.clone();
    }

    private DataSource dataSourceInit() {
//        DataSourceProperties properties = dataSourceGlobalConfig.cloneProperties();
//        properties.setBeanClassLoader(dataSourceGlobalConfig.getDynamicMapperProperties().getClassLoader());
//        properties.setDriverClassName(dataSourceGlobalConfig.getDynamicMapperProperties().getDriverClassName());
//        properties.setUrl(dataSourceGlobalConfig.getDynamicMapperProperties().getUrl());
//        properties.setUsername(dataSourceGlobalConfig.getDynamicMapperProperties().getUsername());
//        properties.setPassword(dataSourceGlobalConfig.getDynamicMapperProperties().getPassword());
//        DataSource source = properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
//        ((HikariDataSource) source).setPoolName(dataSourceGlobalConfig.getDynamicMapperProperties().getName());
//        dataSourceGlobalConfig.getDynamicMapperProperties().setInitDS(source);
//        DataSourcePool.register((HikariDataSource) source);
//        return source;
        if (StringUtil.isEmptyWithOutSpace(config.getPoolName()))
            config.setPoolName(DataSource);
        HaemetaDS dataSource = new HaemetaDS(config);
        dataSource.isDynamic();
        DataSourcePool.register(dataSource);
        return dataSource;
    }



    /**
     * 动态数据源统一管理器
     *
     * @return
     */
    @Bean(name = DataSource)
    public DynamicDataSource dataSource() {
        this.config = autoGetConfig.getDynamicMapperProperties();

        Map<Object, Object> targetDataSources = new HashMap<>(50);
        DynamicDataSource dataSourceSwitch = new DynamicDataSource();
        dataSourceSwitch.setTargetDataSources(targetDataSources);
        DataSourcePool.register(dataSourceSwitch);
        if (config.getInit()) {
            dataSourceSwitch.registerDataSource(DataSourceInit, dataSourceInit());
        }
        return dataSourceSwitch;
    }

    @Bean(name = SqlSessionFactory)
    public SqlSessionFactory sqlSessionFactory(
            @Qualifier(DataSource) DataSource dynamicDataSource
    ) throws Exception {
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(dynamicDataSource);
        factoryBean.setTypeAliasesPackage(autoGetConfig.getDynamicMapperProperties().getTypeAliasesPackage());
        factoryBean.setMapperLocations(
                autoGetConfig.getDynamicMapperProperties().getResources()
        );
        factoryBean.setConfiguration(haeMyBaitsConfiguration);
        DataSourcePool.bindFactory((DynamicDataSource) dynamicDataSource, factoryBean);
        return factoryBean.getObject();
    }

    @Bean(name = TransactionManager)
    public DataSourceTransactionManager transactionManager(@Qualifier(DataSource) DataSource dataSource) {
        return new HaeDataSourceTransactionManager(dataSource).dynamic();
    }

    /**
     * 动态源 会话模板
     *
     * @param sqlSessionFactory
     * @return
     * @throws Exception
     */
    @Bean(name = SqlSessionTemplate)
    public SqlSessionTemplate testSqlSessionTemplate(
            @Qualifier(SqlSessionFactory) SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }


}
