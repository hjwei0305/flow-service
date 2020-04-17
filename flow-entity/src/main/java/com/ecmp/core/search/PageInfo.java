package com.ecmp.core.search;

import java.io.Serializable;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：表格分页信息
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/4/5 15:07      陈飞(fly)                  新建
 * <p/>
 * *************************************************************************************************
 */
public class PageInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    private int page = 1;
    /**
     * 每页条数,默认每页15条
     */
    private int rows = 15;

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

    @Override
    public String toString() {
        return "PageInfo{" +
                "page=" + page +
                ", rows=" + rows +
                '}';
    }
}
