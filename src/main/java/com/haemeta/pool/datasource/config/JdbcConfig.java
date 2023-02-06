package com.haemeta.pool.datasource.config;

import com.haemeta.pool.datasource.config.datasource.DynamicDSConfig;
import com.haemeta.pool.datasource.config.datasource.MasterDSConfig;
import com.haemeta.pool.datasource.config.datasource.SecondaryDSConfig;
import com.haemeta.pool.datasource.jdbc.JdbcUtil;
import com.haemeta.pool.datasource.utils.TransactionUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
@ConditionalOnProperty(prefix = "com.haemeta",name = "jdbc",havingValue = "true")
public class JdbcConfig {

    public static final String Master = "masterJdbc";
    public static final String Secondary = "secondaryJdbc";
    public static final String Dynamic = "dynamicJdbc";


    @Bean(name = Master)
    public JdbcUtil master(@Qualifier(MasterDSConfig.DataSource)DataSource dataSource){
        return new JdbcUtil(dataSource,(conn,ps,rs)->{
            try {
                if(rs != null) {
                    rs.close();
                    rs = null;
                }

                if(ps != null){
                    ps.close();
                    ps = null;
                }

                if(conn != null && !TransactionUtil.inTrans().master()){
                    conn.close();
                    conn = null;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Bean(name = Secondary)
    @ConditionalOnProperty(prefix = "com.haemeta.db",name = "secondary",havingValue = "true")
    public JdbcUtil secondary(@Qualifier(SecondaryDSConfig.DataSource)DataSource dataSource){
        return new JdbcUtil(dataSource,(conn,ps,rs)->{
            try {
                if(rs != null) {
                    rs.close();
                    rs = null;
                }

                if(ps != null){
                    ps.close();
                    ps = null;
                }

                if(conn != null && !TransactionUtil.inTrans().secondary()){
                    conn.close();
                    conn = null;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Bean(name = Dynamic)
    @ConditionalOnProperty(prefix = "com.haemeta.db", name = "dynamic", havingValue = "true")
    public JdbcUtil dynamic(@Qualifier(DynamicDSConfig.DataSource)DataSource dataSource){
        return new JdbcUtil(dataSource,(conn,ps,rs)->{
            try {
                if(rs != null) {
                    rs.close();
                    rs = null;
                }

                if(ps != null){
                    ps.close();
                    ps = null;
                }

                if(conn != null && !TransactionUtil.inTrans().dynamic()){
                    conn.close();
                    conn = null;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }


}
