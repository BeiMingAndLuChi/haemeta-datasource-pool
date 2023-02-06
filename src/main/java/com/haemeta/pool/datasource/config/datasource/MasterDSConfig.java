package com.haemeta.pool.datasource.config.datasource;

import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.haemeta.common.utils.lang.StringUtil;
import com.haemeta.pool.datasource.config.AutoGetConfig;
import com.haemeta.pool.datasource.config.HaeDataSourceTransactionManager;
import com.haemeta.pool.datasource.entity.DataSourcePool;
import com.haemeta.pool.datasource.entity.pojo.HaemetaDS;
import com.haemeta.pool.datasource.properties.HaeMyBaitsConfiguration;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * 2022年1月4日10:03:41
 * Master 主库 数据源的相关配置
 */
@AutoConfigureOrder(10000)
@Configuration
@ConditionalOnMissingBean(value = MasterDSConfig.class)
public class MasterDSConfig {

    public static final String configuration = "masterConfiguration";
    public static final String DataSource = "masterDataSource";
    public static final String SqlSessionFactory = "masterSqlSessionFactory";
    public static final String SqlSessionTemplate = "masterSqlSessionTemplate";
    public static final String TransactionManager = "masterTransactionManager";

    @Resource
    public AutoGetConfig autoGetConfig;

    @Resource
    public GlobalConfig globalConfig;

    @Resource(name = configuration)
    public HaeMyBaitsConfiguration haeMyBaitsConfiguration;

    /**
     * 啊哈，重点来了，这就是我们要的数据源
     * 将该数据源配置 和 公共配置交配一下，就能获得一个我们想要的数据源
     * dataSourceGlobalConfig.getMasterMapperProperties() driver , url , name , pwd
     * @return
     */
    @Primary
    @Bean(name = DataSource)
    public DataSource dataSource() {
        HikariConfig hikariConfig = autoGetConfig.getMasterMapperProperties();
        if (StringUtil.isEmptyWithOutSpace(hikariConfig.getPoolName()))
            hikariConfig.setPoolName(DataSource);
        HaemetaDS dataSource = new HaemetaDS(hikariConfig);
        DataSourcePool.register(dataSource);
        return dataSource;
    }

    /**
     * 当前数据源的 会话工厂，这个也非常重要
     * TypeAliasesPackage : Mybatis 中该数据源的 类包
     * MapperLocations : 这个属性决定你哪些包下面的 Mapper 用当前的数据源，所以这个非常重要
     * 但是我还没有试过，如果两个 工厂的Mapper 路径都指向一个包会怎么样，后来的年轻人哟，来帮我试试吧
     * 可以 +我Q: 1030365197，问答直接填你干嘛来的就行
     * @param dataSource
     * @return
     * @throws Exception
     */
    @Primary
    @Bean(name = SqlSessionFactory)

    public SqlSessionFactory sqlSessionFactory(
            @Qualifier(DataSource) DataSource dataSource
    ) throws Exception {
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setTypeAliasesPackage(autoGetConfig.getMasterMapperProperties().getTypeAliasesPackage());
        factoryBean.setMapperLocations(
                autoGetConfig.getMasterMapperProperties().getResources()
        );
        factoryBean.setConfiguration(haeMyBaitsConfiguration);
        factoryBean.setGlobalConfig(globalConfig);
        DataSourcePool.bindFactory((HikariDataSource) dataSource, factoryBean);
        return factoryBean.getObject();
    }

    /**
     * 数据源事务管理器，这玩意儿也很重要
     * 害，反正这个配置类都很重要
     * 搭配 DataSourceTransactionAOP(切面)、ThreadDataSourceConnection (线程连接控制)、TransactionUtil (事物工具类) 使用
     *
     * @param dataSource
     * @return
     */
    @Primary
    @Bean(name = TransactionManager)
    public DataSourceTransactionManager transactionManager(@Qualifier(DataSource) DataSource dataSource) {
        return new HaeDataSourceTransactionManager(dataSource);
    }

    /**
     * MySql 会话模板
     *
     * @param sqlSessionFactory
     * @return
     * @throws Exception
     */
    @Primary
    @Bean(name = SqlSessionTemplate)
    public SqlSessionTemplate testSqlSessionTemplate(
            @Qualifier(SqlSessionFactory) SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }


}
