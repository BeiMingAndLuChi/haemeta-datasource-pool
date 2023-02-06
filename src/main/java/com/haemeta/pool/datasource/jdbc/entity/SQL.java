package com.haemeta.pool.datasource.jdbc.entity;

import com.haemeta.common.utils.lang.ListUtil;

import java.util.List;

public class SQL<Bean> {

    private String sql;

    private Bean bean;

    private String totalSql;

    private List parameters;

    private Object result;

    private Boolean success;

    private String appendSql;
    private List appendParam;

    public SQL(){

    }

    public SQL(String sql){
        this.sql = sql;
    }

    public SQL(String sql, List parameters){
        this.sql = sql;
        this.parameters = parameters;
    }

    public static <Bean> SQL of(Bean bean){
        SQL sql = new SQL();
        sql.setBean(bean);
        return sql;
    }

    public SQL append(String sql,Object...param){
        this.appendSql = sql;
        this.appendParam = ListUtil.asList(param);
        return this;
    }

    public String getAppendSql() {
        return appendSql;
    }

    public List getAppendParam() {
        return appendParam;
    }

    public void setAppendParam(List appendParam) {
        this.appendParam = appendParam;
    }

    public void setAppendSql(String appendSql) {
        this.appendSql = appendSql;
    }

    public Bean getBean() {
        return bean;
    }

    public void setBean(Bean bean) {
        this.bean = bean;
    }


    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getTotalSql() {
        return totalSql;
    }

    public void setTotalSql(String totalSql) {
        this.totalSql = totalSql;
    }

    public List getParameters() {
        return parameters;
    }

    public void setParameters(List parameters) {
        this.parameters = parameters;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}
