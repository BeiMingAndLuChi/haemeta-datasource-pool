package com.haemeta.pool.datasource.entity;

import com.haemeta.pool.datasource.config.HaeDataSourceTransactionManager;
import com.haemeta.pool.datasource.utils.TransactionUtil;
import org.springframework.core.NamedThreadLocal;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 2021年12月31日13:43:54 SJF
 * 这是动态数据源，用于手动切换 数据源表的
 * 如果需要使用该数据源，请将该 数据源类  作为主唯一数据源
 * 否则切换数据源 无效
 *
 * 但是，该类已被我废弃，暂时用不到，因为我们有更好的方法
 * 如果非要使用这个的话，还要开启 DataSourceAOP 中的切面，DataSourceAOP中会有说道
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    public DynamicDataSource(){}

    /**
     * 当前线程的数据源
     */
    private static final ThreadLocal<String> local = new ThreadLocal<>();

    private static final Map<Object,Object> dataSourceMap = new HashMap<>();

    private static final Map<String, DataSourceTransactionManager> transactionManagerMap = new HashMap<>();

    /**
     * 线程事务池
     */
    private static final ThreadLocal<Map<String,TransactionInfo>> transactionInfoPool = new NamedThreadLocal<>("transaction-info-pool");

    //处于注册状态
    private static Boolean inRegister;

    /**
     * 获取被注册在列的数据源
     * @return
     */
    public Map<Object, DataSource> getDataSourceMap(){
        return super.getResolvedDataSources();
    }

    /**
     * 获取当前数据源 name 、 code 、 或者是其他什么的代号之类的
     * 自定义的
     * @return
     */
    public static String local() {
        return local.get();
    }
    public static void local(String key) {
        local.set(key);
    }
    public static DataSource localDataSource() {
        return (DataSource) dataSourceMap.get(local.get());
    }
    public static DataSourceTransactionManager localTransManager(){
        return transactionManagerMap.get(local.get());
    }

    /**
     * 获取指定代号的数据源
     * @param key
     * @return
     */
    public static DataSource getDataSource(String key){
        return (DataSource) dataSourceMap.get(key);
    }

    /**
     * 获取当前数据源
     * @return
     */
    public static DataSource getDataSource(){
        if (local.get() == null) return null;
        return (DataSource) dataSourceMap.get(local.get());
    }

    /**
     * 清空当前线程正在使用的数据源
     */
    public static void clearDataSource() {
        local.remove();
    }

    /**
     * 顶替父级的 setTargetDataSources
     * 将参数丢到我定义的 dataSourceMap 中
     * @param targetDataSources
     */
    @Override
    public void setTargetDataSources(Map<Object, Object> targetDataSources) {
        dataSourceMap.putAll(targetDataSources);
        super.setTargetDataSources(targetDataSources);
    }

    /**
     * 将 数据源 注册在列
     * @param key
     * @param value
     */
    public synchronized void registerDataSource(String key , DataSource value , HaeDataSourceTransactionManager manager){
        inRegister = true;
        transactionManagerMap.put(key,manager);
        dataSourceMap.put(key,value);
        super.setTargetDataSources(dataSourceMap);
        super.afterPropertiesSet();
        inRegister = false;
    }
    public void registerDataSource(String key , DataSource value){
        registerDataSource(key,value,new HaeDataSourceTransactionManager(value));
    }

    /**
     * 最关键的方法，AbstractRoutingDataSource 就是通过这个方法，获取动态数据源的
     * @return
     */
    @Override
    protected Object determineCurrentLookupKey() {
        String key = Optional.ofNullable(local.get()).orElseThrow(()->new NullPointerException("未选择数据源"));
        Optional.ofNullable(dataSourceMap.get(key)).orElseThrow(()->new NullPointerException("该数据源未注册在列"));
        return key;
    }

    /**
     * 获取连接 注册保护
     * @return
     * @throws SQLException
     */
    @Override
    public Connection getConnection() throws SQLException {
        while(!inRegister){
            break;
        }

        //溢出处理，每次getConnection 如果是动态多数据源 就会加载 事务
        if(TransactionUtil.inTrans().dynamic()){
            String key = (String) determineCurrentLookupKey();

            Map<String,TransactionInfo> tsPool = Optional.ofNullable(transactionInfoPool.get()).orElseGet(()->{
                Map<String,TransactionInfo> tsPoolCreate = new HashMap<>();
                transactionInfoPool.set(tsPoolCreate);
                return tsPoolCreate;
            });
            TransactionInfo info  = tsPool.get(key);
            if(info == null)
                info = TransactionUtil.openTransaction(this.transactionManagerMap.get(key));

            tsPool.put(key,info);
        }
        return super.getConnection();
    }

    public void commitKey(String key){
        if(!TransactionUtil.inTrans().dynamic()) return;
        Map<String,TransactionInfo> tsPool = transactionInfoPool.get();
        if(tsPool == null) return;
        TransactionInfo info = tsPool.get(key);
        commit(info);
    }

    public void commitAll(){
        if(!TransactionUtil.inTrans().dynamic()) return;
        Map<String,TransactionInfo> tsPool = transactionInfoPool.get();
        if(tsPool == null) return;
        tsPool.forEach((key,info)->{
            commit(info);
        });
    }

    private void commit(TransactionInfo info){
        if(info == null || info.getStatus() == null) return;
        TransactionUtil.commit(info);
    }

}
