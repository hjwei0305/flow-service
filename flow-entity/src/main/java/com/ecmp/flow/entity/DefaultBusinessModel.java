package com.ecmp.flow.entity;

import com.ecmp.flow.constant.FlowStatus;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;



/**
 * 测试单据实体
 */
@Entity(name = "default_business_model")
public class DefaultBusinessModel extends com.ecmp.core.entity.BaseAuditableEntity {

    /**
     * 业务名称
     */
    @Column(name = "name")
    private String name;

    /**
     * 当前流程状态
     */
    @Column(name = "flow_status")
    private FlowStatus flowStatus = FlowStatus.INIT;

    /**
     * 租户代码
     */
    @Column(name = "tenant_code")
    private String tenantCode;


    /**
     * 组织机构代码
     */
    @Column(name = "org_code")
    private String orgCode;

    /**
     * 组织机构Id
     */
    @Column(name = "org_id")
    private String orgId;

    /**
     * 组织机构名称
     */
    @Column(name = "org_name")
    private String orgName;

    /**
     * 组织机构层级路径
     */
    @Column(name = "org_path")
    private String orgPath;

    /**
     * 优先级别
     */
    @Column(name = "priority")
    private int priority;

    /**
     * 工作说明
     */
    @Column(name = "work_caption")
    private String workCaption;

    /**
     * 业务单号
     */
    @Column(name = "business_code")
    private String businessCode;


    /**
     * 单价
     */
    @Column(name = "unit_price")
    private double unitPrice = 0;

    /**
     * 数量
     */
    @Column(name = "count")
    private int count = 0;

    /**
     * 金额
     */
    @Column(name = "sum")
    private double sum = 0;

    /**
     * 申请说明
     */
    @Column(name = "apply_caption")
    private String applyCaption;


    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getSum() {
        return sum = unitPrice * count;
    }

    public void setSum(double sum) {
        this.sum = unitPrice * count;
    }

    public String getApplyCaption() {
        return applyCaption;
    }

    public void setApplyCaption(String applyCaption) {
        this.applyCaption = applyCaption;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FlowStatus getFlowStatus() {
        return flowStatus;
    }

    public void setFlowStatus(FlowStatus flowStatus) {
        this.flowStatus = flowStatus;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOrgPath() {
        return orgPath;
    }

    public void setOrgPath(String orgPath) {
        this.orgPath = orgPath;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getWorkCaption() {
        return workCaption;
    }

    public void setWorkCaption(String workCaption) {
        this.workCaption = workCaption;
    }

    public String getBusinessCode() {
        return businessCode;
    }

    public void setBusinessCode(String businessCode) {
        this.businessCode = businessCode;
    }

    public DefaultBusinessModel() {
    }

    public DefaultBusinessModel(double unitPrice, int count, double sum, String applyCaption) {
        this.unitPrice = unitPrice;
        this.count = count;
        this.sum = sum;
        this.applyCaption = applyCaption;
    }


}
