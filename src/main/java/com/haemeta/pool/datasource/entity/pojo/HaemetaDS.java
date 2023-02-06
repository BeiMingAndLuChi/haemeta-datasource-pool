package com.haemeta.pool.datasource.entity.pojo;

import com.haemeta.pool.datasource.utils.TransactionUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.core.NamedThreadLocal;

import java.sql.Connection;
import java.sql.SQLException;

public class HaemetaDS extends HikariDataSource {

    private boolean dynamic;

    public HaemetaDS(HikariConfig config){
        super(config);
        this.dynamic = false;
    }

    public HaemetaDS(){
        super();
        this.dynamic = false;
    }

    public void isDynamic(){
        this.dynamic = true;
    }

    public boolean dynamic(){
        return this.dynamic;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return super.getConnection();
    }
}
