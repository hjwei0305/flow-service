package com.ecmp.flow.vo;

import java.io.Serializable;

public class ListFlowTypeVo  implements Serializable {

    /**
     * 当前页数
     */
    private int page;

    /**
     * 每页行数
     */
    private int rows;

    /**
     * 排序字段
     */
    private String sidx;

    /**
     * 排序规则
     */
    private String sord;

    /**
     *快速查询值
     */
    private String Quick_value;


    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public String getSidx() {
        return sidx;
    }

    public void setSidx(String sidx) {
        this.sidx = sidx;
    }

    public String getSord() {
        return sord;
    }

    public void setSord(String sord) {
        this.sord = sord;
    }

    public String getQuick_value() {
        return Quick_value;
    }

    public void setQuick_value(String quick_value) {
        Quick_value = quick_value;
    }
}
