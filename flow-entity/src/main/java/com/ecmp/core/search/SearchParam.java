package com.ecmp.core.search;

import java.io.Serializable;
import java.util.List;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：高级分页查询参数
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017-06-23 11:01      王锦光(wangj)                新建
 * <p/>
 * *************************************************************************************************
 */
public class SearchParam implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 筛选字段
     */
    private List<SearchFilter> filters;

    /**
     * 排序字段
     */
    private List<SearchOrder> sortOrders;

    /**
     * 分页信息
     */
    private PageInfo pageInfo;

    public List<SearchFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<SearchFilter> filters) {
        this.filters = filters;
    }

    public List<SearchOrder> getSortOrders() {
        return sortOrders;
    }

    public void setSortOrders(List<SearchOrder> sortOrders) {
        this.sortOrders = sortOrders;
    }

    public PageInfo getPageInfo() {
        return pageInfo;
    }

    public void setPageInfo(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }
}
