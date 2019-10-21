package com.ecmp.flow.vo;

import java.io.Serializable;

public class MyBillsHeaderVo implements Serializable {

  private String orderType;

  private  Long startDate;

  private  Long endDate;


    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }
}
