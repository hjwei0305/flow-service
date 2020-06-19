package com.ecmp.flow.dto;

import com.ecmp.core.search.SearchOrder;

import java.io.Serializable;
import java.util.List;

/**
 * 实现功能: 门户待办查询参数
 *
 * @author 王锦光 wangjg
 * @version 2020-06-04 17:25
 */
public class PortalFlowTaskParam implements Serializable {
    /**
     * 业务类型Id
     */
    private String modelId;

    /**
     * 获取条目数
     */
    private Integer recordCount = 10;

    /**
     * 排序属性清单
     */
    private List<SearchOrder> searchOrders;

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public Integer getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(Integer recordCount) {
        this.recordCount = recordCount;
    }

    public List<SearchOrder> getSearchOrders() {
        return searchOrders;
    }

    public void setSearchOrders(List<SearchOrder> searchOrders) {
        this.searchOrders = searchOrders;
    }
}
