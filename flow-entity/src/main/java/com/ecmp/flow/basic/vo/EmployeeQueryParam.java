package com.ecmp.flow.basic.vo;

import java.io.Serializable;
import java.util.List;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：查询员工用户vo
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/5/31 16:32      秦有宝                     新建
 * <p/>
 * *************************************************************************************************
 */
public class EmployeeQueryParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 页数
     */
    private int page;

    /**
     * 行数
     */
    private int rows;

    /**
     * 需要排除的员工用户id列表
     */
    private List<String> ids;

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

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }
}
