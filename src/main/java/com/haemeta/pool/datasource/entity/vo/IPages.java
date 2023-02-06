package com.haemeta.pool.datasource.entity.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.haemeta.common.entity.vo.Page;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class IPages<T> implements IPage<T> {

    private Page<T> page;
    private List<OrderItem> orders;
    protected boolean optimizeCountSql;
    protected boolean searchCount;
    protected boolean optimizeJoinOfCountSql;
    protected String countId;
    protected Long maxLimit;

    public IPages(){
        page = new Page<>();
        page.setTotal(Long.valueOf(0));
        page.setPage(0);
        page.setPageSize(0);
    }

    public IPages(Page<T> page){
        this.page = page;
    }

    @Override
    public List<OrderItem> orders() {
        return orders;
    }

    @Override
    public boolean optimizeCountSql() {
        return optimizeCountSql;
    }

    @Override
    public boolean optimizeJoinOfCountSql() {
        return optimizeJoinOfCountSql;
    }

    @Override
    public boolean searchCount() {
        return searchCount;
    }

    @Override
    public Long maxLimit() {
        return maxLimit;
    }

    @Override
    public long getPages() {
        return IPage.super.getPages();
    }

    @Override
    public List getRecords() {
        return (List) page.getData();
    }

    @Override
    public IPage setRecords(List records) {
        page.setData(records);
        return this;
    }

    @Override
    public long getTotal() {
        return page.getTotal();
    }

    @Override
    public IPage setTotal(long total) {
        page.setTotal(total);
        return this;
    }

    @Override
    public long getSize() {
        return page.getPageSize().longValue();
    }

    @Override
    public IPage setSize(long size) {
        page.setPageSize(Long.valueOf(size).intValue());
        return this;
    }

    @Override
    public long getCurrent() {
        return page.getPage().longValue();
    }

    @Override
    public IPage setCurrent(long current) {
        page.setPage(Long.valueOf(current).intValue());
        return this;
    }

    @Override
    public String countId() {
        return IPage.super.countId();
    }

    @Override
    public IPage convert(Function mapper) {
        return IPage.super.convert(mapper);
    }

    public void setCause(Map<String, Object> cause) {
        page.setCause(cause);
    }

    public Map<String, Object> cause() {
        return page.getCause();
    }

    public Page<T> page(){
        return page;
    }

    public void setPage(Integer page){
        this.page.setPage(page);
    }

    public void setPageSize(Integer pageSize){
        this.page.setPageSize(pageSize);
    }

    public Integer getPage(){
        return page.getPage();
    }

    public Integer pageSize(){
        return page.getPageSize();
    }
}
