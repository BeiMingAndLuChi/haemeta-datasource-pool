package com.haemeta.pool.datasource.jdbc.entity;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DtoInfo {

    /**
     * BeanName
     */
    private String name;
    /**
     * xx.xxx.xxx.BeanName
     */
    private String className;
    /**
     * 字段列表
     */
    private List<String> fields;
    /**
     * 字段的 get 方法
     */
    private Map<String, Method> getMethods;
    /**
     * 字段的 set 方法
     */
    private Map<String, Method> setMethods;

    /**
     * 通过 数据库列 获取 字段
     */
    private Map<String,String> getFieldByColumn;

    /**
     * 数据库 表名
     */
    private String tableName;

    /**
     * 数据库列名 key:field , value:column
     */
    private Map<String,String> columns;

    /**
     * 通过列名 获取 get 方法
     */
    private Map<String,Method> columnsGet;

    /**
     * 通过列名 获取 set 方法
     */
    private Map<String,Method> columnsSet;

    public DtoInfo(){

    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public Map<String, Method> getGetMethods() {
        return getMethods;
    }

    public void setGetMethods(Map<String, Method> getMethods) {
        this.getMethods = getMethods;
    }

    public Map<String, Method> getSetMethods() {
        return setMethods;
    }

    public void setSetMethods(Map<String, Method> setMethods) {
        this.setMethods = setMethods;
    }

    public Map<String, String> getColumns() {
        return columns;
    }

    public void setColumns(Map<String, String> columns) {
        this.columns = columns;
    }

    public Map<String, Method> getColumnsGet() {
        return columnsGet;
    }

    public void setColumnsGet(Map<String, Method> columnsGet) {
        this.columnsGet = columnsGet;
    }

    public Map<String, Method> getColumnsSet() {
        return columnsSet;
    }

    public void setColumnsSet(Map<String, Method> columnsSet) {
        this.columnsSet = columnsSet;
    }

    public Map<String, String> getGetFieldByColumn() {
        return getFieldByColumn;
    }

    public void setGetFieldByColumn(Map<String, String> getFieldByColumn) {
        this.getFieldByColumn = getFieldByColumn;
    }
}
