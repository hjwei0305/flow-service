package com.ecmp.flow.dto;

import com.ecmp.core.search.Search;



/**
 * 实现功能: 用户我的单据查询参数
 *
 * @author 何灿坤 AK
 * @version 2020-06-19 14:12
 */
public class UserFlowBillsQueryParam  extends Search {

    /**
     * 业务类型Id
     */
    private String modelId;


    /**
     * 流程状态：all-全部；inflow-流程中；ended-正常结束；abnormalEnd-异常终止
     */
    private String flowStatus;


    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getFlowStatus() {
        return flowStatus;
    }

    public void setFlowStatus(String flowStatus) {
        this.flowStatus = flowStatus;
    }
}
