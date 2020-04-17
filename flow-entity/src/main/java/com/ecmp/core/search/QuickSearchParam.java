package com.ecmp.core.search;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：快速分页查询参数
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017-06-23 10:03      王锦光(wangj)                新建
 * <p/>
 * *************************************************************************************************
 */
public class QuickSearchParam implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 快速搜索关键字
     */
    private String quickSearchValue;
    /**
     * 快速搜索属性清单
     */
    private Collection<String> quickSearchProperties;
    /**
     * 排序字段
     */
    private List<SearchOrder> sortOrders;
    /**
     * 分页信息
     */
    private PageInfo pageInfo;

    public String getQuickSearchValue() {
        return quickSearchValue;
    }

    public void setQuickSearchValue(String quickSearchValue) {
        this.quickSearchValue = quickSearchValue;
    }

    public Collection<String> getQuickSearchProperties() {
        return quickSearchProperties;
    }

    public void setQuickSearchProperties(Collection<String> quickSearchProperties) {
        this.quickSearchProperties = quickSearchProperties;
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
