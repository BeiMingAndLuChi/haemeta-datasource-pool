package com.haemeta.pool.datasource.config;

import com.haemeta.pool.datasource.entity.DynamicDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;

import javax.sql.DataSource;

public class HaeDataSourceTransactionManager extends DataSourceTransactionManager {

    private boolean dynamic;

    public HaeDataSourceTransactionManager(DataSource dataSource) {
        super(dataSource);
        this.dynamic = false;
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        if(getDataSource() instanceof DynamicDataSource)
            System.out.println("动态数据源，开启了事务");
        else
            System.out.println(((HikariDataSource)getDataSource()).getPoolName()+"，开始了事务");
        if(!isDynamic()){

        }

        super.doBegin(transaction, definition);
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) {
        if(!isDynamic()){
            System.out.println("master_trans,有人提交了");
        }
        super.doCommit(status);

    }

    @Override
    public void doRollback(DefaultTransactionStatus status) {
        if(!isDynamic()){
            System.out.println("master_trans,有人回滚了");
        }
        super.doRollback(status);

    }

    public boolean isDynamic() {
        return dynamic;
    }

    public HaeDataSourceTransactionManager dynamic() {
        this.dynamic = true;
        return this;
    }
}
