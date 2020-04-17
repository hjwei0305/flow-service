package com.ecmp.core.search;

import java.io.Serializable;
import java.util.List;

/**
 * <strong>实现功能:</strong>
 * <p>Query分页查询参数</p>
 *
 * @author 王锦光 wangj
 * @version 1.0.1 2017-11-25 10:40
 */
public class QueryParam implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 排序字段
     */
    private List<SearchOrder> sortOrders;
    /**
     * 分页信息
     */
    private PageInfo pageInfo;

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
