package com.haemeta.pool.datasource.entity;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;

import javax.sql.DataSource;

public class TransactionInfo {
    private DataSourceTransactionManager manager;
    private TransactionStatus status;
    private DataSource dataSource;

    public TransactionInfo(DataSourceTransactionManager manager,TransactionStatus status){
        this.manager = manager;
        this.status = status;
        this.dataSource = manager.getDataSource();
    }
    public DataSourceTransactionManager getManager() {
        return manager;
    }

    public void setManager(DataSourceTransactionManager manager) {
        this.manager = manager;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
