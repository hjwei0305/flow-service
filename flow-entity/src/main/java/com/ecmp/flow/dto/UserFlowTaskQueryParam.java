package com.ecmp.flow.dto;

import com.ecmp.core.search.Search;

/**
 * 实现功能: 用户待办工作查询参数
 *
 * @author 王锦光 wangjg
 * @version 2020-06-17 14:19
 */
public class UserFlowTaskQueryParam extends Search {
    /**
     * 业务类型Id
     */
    private String modelId;

    /**
     * 查询可以批量处理的任务
     */
    private Boolean canBatch = Boolean.FALSE;

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public Boolean getCanBatch() {
        return canBatch;
    }

    public void setCanBatch(Boolean canBatch) {
        this.canBatch = canBatch;
    }
}
