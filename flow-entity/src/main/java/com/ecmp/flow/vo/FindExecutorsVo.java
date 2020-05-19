package com.ecmp.flow.vo;

import java.io.Serializable;


public class FindExecutorsVo  implements Serializable {

    private String requestExecutorsVos;

    private String businessModelCode;

    private String businessId;

    private String instanceId;//流程实例ID，可以获取到businessModelCode和businessId

    public String getRequestExecutorsVos() {
        return requestExecutorsVos;
    }

    public void setRequestExecutorsVos(String requestExecutorsVos) {
        this.requestExecutorsVos = requestExecutorsVos;
    }

    public String getBusinessModelCode() {
        return businessModelCode;
    }

    public void setBusinessModelCode(String businessModelCode) {
        this.businessModelCode = businessModelCode;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }
}
