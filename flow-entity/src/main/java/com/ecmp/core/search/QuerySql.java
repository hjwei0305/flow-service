package com.ecmp.core.search;

import java.io.Serializable;

/**
 * <strong>实现功能:</strong>
 * <p>QUERY查询的SQL类</p>
 *
 * @author 王锦光 wangj
 * @version 1.0.1 2017-11-27 20:33
 */
public class QuerySql implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 查询部分
     */
    private String select;
    /**
     * 获取数据量的查询部分
     */
    private String countSelect;
    /**
     * 查询语句的FROM和WHERE部分
     */
    private String fromAndwhere;
    /**
     * 排序部分
     */
    private String orderBy;

    /**
     * 构造函数
     * @param select 查询语句的查询部分
     * @param fromAndwhere 查询语句的FROM和WHERE部分
     */
    public QuerySql(String select, String fromAndwhere) {
        this.select = select;
        this.fromAndwhere = fromAndwhere;
    }

    public String getSelect() {
        return select;
    }

    public void setSelect(String select) {
        this.select = select;
    }

    public String getCountSelect() {
        return countSelect;
    }

    public void setCountSelect(String countSelect) {
        this.countSelect = countSelect;
    }

    public String getFromAndwhere() {
        return fromAndwhere;
    }

    public void setFromAndwhere(String fromAndwhere) {
        this.fromAndwhere = fromAndwhere;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }
}
