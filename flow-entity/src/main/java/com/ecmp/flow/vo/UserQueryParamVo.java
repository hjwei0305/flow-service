package com.ecmp.flow.vo;

import com.ecmp.core.search.QuickSearchParam;



/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：企业员工用户查询参数
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2019/7/30            何灿坤                    新建
 * <p/>
 * *************************************************************************************************
 */
public class UserQueryParamVo extends QuickSearchParam {

    /**
     * 是否包含组织机构子节点
     */
    private Boolean includeSubNode = Boolean.FALSE;

    /**
     * 组织机构Id
     */
    private String organizationId;

    /**
     * 岗位id
     */
    private String positionId;

    public Boolean getIncludeSubNode() {
        return includeSubNode;
    }

    public void setIncludeSubNode(Boolean includeSubNode) {
        this.includeSubNode = includeSubNode;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

}
