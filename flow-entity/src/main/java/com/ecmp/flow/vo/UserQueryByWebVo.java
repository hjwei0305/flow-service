package com.ecmp.flow.vo;

import com.ecmp.core.search.PageInfo;

public class UserQueryByWebVo {

    /**
     * 当前页码
     */
    private int page = 1;
    /**
     * 每页条数,默认每页15条
     */
    private int rows = 15;

    /**
     * 组织机构ID
     */
    private String organizationId;


    /**
     * 是否包含组织机构子节点
     */
    private Boolean includeSubNode = Boolean.TRUE;


    /**
     * 快速查询值
     */
    private String quickSearchValue;



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

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public Boolean getIncludeSubNode() {
        return includeSubNode;
    }

    public void setIncludeSubNode(Boolean includeSubNode) {
        this.includeSubNode = includeSubNode;
    }

    public String getQuickSearchValue() {
        return quickSearchValue;
    }

    public void setQuickSearchValue(String quickSearchValue) {
        this.quickSearchValue = quickSearchValue;
    }

}
