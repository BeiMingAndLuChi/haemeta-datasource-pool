package com.haemeta.pool.datasource.entity;

import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.haemeta.pool.datasource.config.datasource.DynamicDSConfig;
import com.haemeta.pool.datasource.entity.pojo.HaemetaDS;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class DataSourcePool {

    private static final Map<String,PoolItem> dataSourceCache = new HashMap<>();

    /**
     * 收集所有的数据源
     * @param dataSource
     * @param <DS>
     */
    public synchronized static <DS extends HikariDataSource> void register(DS dataSource){
        dataSourceCache.put(dataSource.getPoolName(),new PoolItem(dataSource));
    }
    public synchronized static void register(DynamicDataSource dataSource){
        dataSourceCache.put(DynamicDSConfig.DataSource,new PoolItem(dataSource));
    }

    public static DynamicDataSource getDynamicDatasource(String key){
        return (DynamicDataSource) dataSourceCache.get(DynamicDSConfig.DataSource).source;
    }

    public static HikariDataSource getDatasource(String key){
        return (HikariDataSource) dataSourceCache.get(key).source;
    }

    public synchronized static void bindFactory(HikariDataSource dataSource, MybatisSqlSessionFactoryBean sqlSessionFactoryBean){
        PoolItem item = dataSourceCache.get(dataSource.getPoolName());
        if (item == null)
            item.setSqlSessionFactoryBean(sqlSessionFactoryBean);
        else
            dataSourceCache.put(dataSource.getPoolName(),new PoolItem(dataSource,sqlSessionFactoryBean));
    }
    public synchronized static void bindFactory(DynamicDataSource dataSource, MybatisSqlSessionFactoryBean sqlSessionFactoryBean){
        PoolItem item = dataSourceCache.get(DynamicDSConfig.DataSource);
        if (item == null)
            item.setSqlSessionFactoryBean(sqlSessionFactoryBean);
        else
            dataSourceCache.put(DynamicDSConfig.DataSource,new PoolItem(dataSource,sqlSessionFactoryBean));
    }

    private static class PoolItem{
        private DataSource source;
        private MybatisSqlSessionFactoryBean sqlSessionFactoryBean;
        PoolItem(DataSource source){this.source = source;}
        PoolItem(DataSource source,MybatisSqlSessionFactoryBean bean){this.source = source;this.sqlSessionFactoryBean = bean;}
        void setSqlSessionFactoryBean(MybatisSqlSessionFactoryBean bean){this.sqlSessionFactoryBean = bean;}
    }
}
