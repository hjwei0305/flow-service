package com.ecmp.flow.vo;

import java.io.Serializable;

public class FindSolidifyExecutorVO  implements Serializable {

   private String executorsVos;

   private String businessModelCode;

   private String businessId;


    public String getExecutorsVos() {
        return executorsVos;
    }

    public void setExecutorsVos(String executorsVos) {
        this.executorsVos = executorsVos;
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
}
