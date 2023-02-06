package com.haemeta.pool.datasource.config.datasource;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.annotation.Resource;
import javax.sql.DataSource;

@AutoConfigureOrder(10000)
@org.springframework.context.annotation.Configuration
@ConditionalOnProperty(prefix = "com.haemeta.db",name = "secondary",havingValue = "true")
public class SecondaryDSConfig {

    public static final String configuration = "secondaryConfiguration";
    public static final String DataSource = "secondaryDataSource";
    public static final String SqlSessionFactory = "secondarySqlSessionFactory";
    public static final String SqlSessionTemplate = "secondarySqlSessionTemplate";
    public static final String TransactionManager = "secondaryTransactionManager";

    @Resource
    public AutoGetConfig autoGetConfig;

    @Resource(name = configuration)
    public HaeMyBaitsConfiguration haeMyBaitsConfiguration;

    /**
     * 啊哈，重点来了，这就是我们要的数据源
     * 将该数据源配置 和 公共配置交配一下，就能获得一个我们想要的数据源
     * dataSourceGlobalConfig.getMasterMapperProperties() driver , url , name , pwd
     * @return
     */
    @Bean(name = DataSource)
    public javax.sql.DataSource dataSource() {
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
    @Bean(name = SqlSessionFactory)
    public org.apache.ibatis.session.SqlSessionFactory sqlSessionFactory(
            @Qualifier(DataSource) DataSource dataSource
    ) throws Exception {
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setTypeAliasesPackage(autoGetConfig.getSecondaryMapperProperties().getTypeAliasesPackage());
        factoryBean.setMapperLocations(
                autoGetConfig.getSecondaryMapperProperties().getResources()
        );
        factoryBean.setConfiguration(haeMyBaitsConfiguration);
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
    @Bean(name = SqlSessionTemplate)
    public org.mybatis.spring.SqlSessionTemplate testSqlSessionTemplate(
            @Qualifier(SqlSessionFactory) SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }


}
