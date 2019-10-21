package com.ecmp.flow.vo;

import java.io.Serializable;
import java.util.Date;

public class MyBillsHeaderVo implements Serializable {

  private String orderType;

  private  Date startDate;

  private  Date endDate;

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
