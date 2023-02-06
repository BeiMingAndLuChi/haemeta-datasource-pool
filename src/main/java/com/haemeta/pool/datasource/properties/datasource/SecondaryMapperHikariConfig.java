package com.haemeta.pool.datasource.properties.datasource;

import com.haemeta.common.utils.lang.ListUtil;
import com.zaxxer.hikari.HikariConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@ConditionalOnProperty(prefix = "com.haemeta.db",name = "secondary",havingValue = "true")
@ConfigurationProperties(prefix = "com.haemeta.db.secondary")
public class SecondaryMapperHikariConfig extends HikariConfig {

    /**
     * mapper映射地址，无限堆叠
     */
    private static String[] mapperLocations;

    private static String typeAliasesPackage;

    private DataSource initDS;

    private Boolean init;

    public static String[] getMapperLocations() {
        return mapperLocations;
    }

    public static void setMapperLocations(String[] setMapperLocations) {
        //空的就返回
        if (Objects.isNull(setMapperLocations) || setMapperLocations.length == 0) return;
        List<String> list;
        //如果当前 mapperLocations 是空的， 则将新的baseMapperLocations 赋值
        if(mapperLocations == null || mapperLocations.length == 0){
            mapperLocations = setMapperLocations;
            return;
        }
        list = ListUtil.merge(
                ListUtil.asList(setMapperLocations),
                ListUtil.asList(mapperLocations)
        );
        list = list.stream().distinct().collect(
                Collectors.collectingAndThen(
                        Collectors.toCollection(
                                () -> new TreeSet<>(Comparator.comparing(String::toString))
                        ),
                        ArrayList::new
                )
        );
        mapperLocations = list.toArray(new String[list.size()]);
    }

    public static String getTypeAliasesPackage() {
        return typeAliasesPackage;
    }

    public static void setTypeAliasesPackage(String typeAliasesPackage) {
        typeAliasesPackage = typeAliasesPackage;
    }

    public static Resource[] getResources() throws IOException {
        PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        if (mapperLocations == null || mapperLocations.length == 0)
            return new Resource[0];
        return resourcePatternResolver.getResources(String.join(",", mapperLocations));
    }

    public static String[] clearMapper(){
        List<String> mapper = ListUtil.asList(
                Optional.ofNullable(mapperLocations)
                        .orElse(new String[0])
        );
        mapperLocations = null;
        return mapper.toArray(new String[mapper.size()]);
    }

    public DataSource getInitDS() {
        return initDS;
    }

    public void setInitDS(DataSource initDS) {
        this.initDS = initDS;
    }

    public Boolean getInit() {
        return Optional.ofNullable(init).orElse(false);
    }

    public void setInit(Boolean init) {
        this.init = init;
    }
}
