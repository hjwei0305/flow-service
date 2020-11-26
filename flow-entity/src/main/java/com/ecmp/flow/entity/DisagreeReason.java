package com.ecmp.flow.entity;

import com.ecmp.core.entity.BaseAuditableEntity;
import com.ecmp.core.entity.IRank;
import com.ecmp.core.entity.ITenant;

import javax.persistence.*;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：不同意原因模型Entity定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2020/11/25        何灿坤(ak)                    新建
 * <p/>
 * *************************************************************************************************
 */
@Entity
@Table(name = "flow_disagree_reason")
@Access(AccessType.FIELD)
public class DisagreeReason extends BaseAuditableEntity implements ITenant , IRank {


    /**
     * 流程类型
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "flow_type_id")
    private FlowType flowType;

    /**
     * 原因Code
     */
    @Column(name = "code")
    private String code;

    /**
     * 原因简称
     */
    @Column(name = "name")
    private String name;

    /**
     * 原因描述
     */
    @Column(name = "depict")
    private String depict;

    /**
     * 排序号
     */
    @Column(name = "rank")
    private Integer rank;

    /**
     * 租户代码
     */
    @Column(name = "tenant_code")
    private String tenantCode;


    public FlowType getFlowType() {
        return flowType;
    }

    public void setFlowType(FlowType flowType) {
        this.flowType = flowType;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepict() {
        return depict;
    }

    public void setDepict(String depict) {
        this.depict = depict;
    }

    @Override
    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    @Override
    public String getTenantCode() {
        return tenantCode;
    }

    @Override
    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }
}
