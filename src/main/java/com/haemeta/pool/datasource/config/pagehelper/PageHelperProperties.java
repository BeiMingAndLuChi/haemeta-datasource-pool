package com.haemeta.pool.datasource.config.pagehelper;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Properties;

@ConfigurationProperties(
        prefix = "pagehelper"
)
public class PageHelperProperties extends Properties{

    public static final String PAGEHELPER_PREFIX = "pagehelper";

    public PageHelperProperties() {
    }

    public Boolean getOffsetAsPageNum() {
        return Boolean.valueOf(this.getProperty("offsetAsPageNum"));
    }

    public void setOffsetAsPageNum(Boolean offsetAsPageNum) {
        this.setProperty("offsetAsPageNum", offsetAsPageNum.toString());
    }

    public Boolean getRowBoundsWithCount() {
        return Boolean.valueOf(this.getProperty("rowBoundsWithCount"));
    }

    public void setRowBoundsWithCount(Boolean rowBoundsWithCount) {
        this.setProperty("rowBoundsWithCount", rowBoundsWithCount.toString());
    }

    public Boolean getPageSizeZero() {
        return Boolean.valueOf(this.getProperty("pageSizeZero"));
    }

    public void setPageSizeZero(Boolean pageSizeZero) {
        this.setProperty("pageSizeZero", pageSizeZero.toString());
    }

    public Boolean getReasonable() {
        return Boolean.valueOf(this.getProperty("reasonable"));
    }

    public void setReasonable(Boolean reasonable) {
        this.setProperty("reasonable", reasonable.toString());
    }

    public Boolean getSupportMethodsArguments() {
        return Boolean.valueOf(this.getProperty("supportMethodsArguments"));
    }

    public void setSupportMethodsArguments(Boolean supportMethodsArguments) {
        this.setProperty("supportMethodsArguments", supportMethodsArguments.toString());
    }

    public String getDialect() {
        return this.getProperty("dialect");
    }

    public void setDialect(String dialect) {
        this.setProperty("dialect", dialect);
    }

    public String getHelperDialect() {
        return this.getProperty("helperDialect");
    }

    public void setHelperDialect(String helperDialect) {
        this.setProperty("helperDialect", helperDialect);
    }

    public Boolean getAutoRuntimeDialect() {
        return Boolean.valueOf(this.getProperty("autoRuntimeDialect"));
    }

    public void setAutoRuntimeDialect(Boolean autoRuntimeDialect) {
        this.setProperty("autoRuntimeDialect", autoRuntimeDialect.toString());
    }

    public Boolean getAutoDialect() {
        return Boolean.valueOf(this.getProperty("autoDialect"));
    }

    public void setAutoDialect(Boolean autoDialect) {
        this.setProperty("autoDialect", autoDialect.toString());
    }

    public Boolean getCloseConn() {
        return Boolean.valueOf(this.getProperty("closeConn"));
    }

    public void setCloseConn(Boolean closeConn) {
        this.setProperty("closeConn", closeConn.toString());
    }

    public String getParams() {
        return this.getProperty("params");
    }

    public void setParams(String params) {
        this.setProperty("params", params);
    }

    public Boolean getDefaultCount() {
        return Boolean.valueOf(this.getProperty("defaultCount"));
    }

    public void setDefaultCount(Boolean defaultCount) {
        this.setProperty("defaultCount", defaultCount.toString());
    }

    public String getDialectAlias() {
        return this.getProperty("dialectAlias");
    }

    public void setDialectAlias(String dialectAlias) {
        this.setProperty("dialectAlias", dialectAlias);
    }

    public String getAutoDialectClass() {
        return this.getProperty("autoDialectClass");
    }

    public void setAutoDialectClass(String autoDialectClass) {
        this.setProperty("autoDialectClass", autoDialectClass);
    }

}
