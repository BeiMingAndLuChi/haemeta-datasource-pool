package com.haemeta.pool.datasource.config.pagehelper;

import com.github.pagehelper.PageInterceptor;
import com.haemeta.pool.datasource.config.datasource.MasterDSConfig;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.Iterator;
import java.util.List;

@Configuration
@ConditionalOnBean({SqlSessionFactory.class})
@EnableConfigurationProperties({PageHelperProperties.class})
@AutoConfigureAfter({MasterDSConfig.class})
@Lazy(false)
public class PageHelperConfig implements InitializingBean {
    private final List<SqlSessionFactory> sqlSessionFactoryList;
    private final PageHelperProperties properties;

    public PageHelperConfig(List<SqlSessionFactory> sqlSessionFactoryList, PageHelperProperties properties) {
        this.sqlSessionFactoryList = sqlSessionFactoryList;
        this.properties = properties;
    }

    public void afterPropertiesSet() throws Exception {
        PageInterceptor interceptor = new PageInterceptor();
        interceptor.setProperties(this.properties);
        Iterator var2 = this.sqlSessionFactoryList.iterator();

        while(var2.hasNext()) {
            SqlSessionFactory sqlSessionFactory = (SqlSessionFactory)var2.next();
            org.apache.ibatis.session.Configuration configuration = sqlSessionFactory.getConfiguration();
            if (!this.containsInterceptor(configuration, interceptor)) {
                configuration.addInterceptor(interceptor);
            }
        }

    }

    private boolean containsInterceptor(org.apache.ibatis.session.Configuration configuration, Interceptor interceptor) {
        try {
            return configuration.getInterceptors().contains(interceptor);
        } catch (Exception var4) {
            return false;
        }
    }

}
