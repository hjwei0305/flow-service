package com.ecmp.flow.dto;

import com.ecmp.core.search.Search;


/**
 * 实现功能: 用户待办工作查询参数
 *
 * @author 何灿坤 AK
 * @version 2020-06-19 14:12
 */
public class UserFlowHistoryQueryParam  extends Search {

    /**
     * 业务类型Id
     */
    private String modelId;


    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }
}
