package com.ecmp.flow.entity;

import com.ecmp.core.entity.ITenant;

import javax.persistence.*;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * 固化流程执行人Entity定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2019/1/10 10:45      何灿坤(hecankun)                新建
 * <p/>
 * *************************************************************************************************
 */
@Entity
@Table(name = "flow_solidify_executor")
public class FlowSolidifyExecutor  extends com.ecmp.core.entity.BaseAuditableEntity implements ITenant {

     /**
      * 所属流程实例
      */
     @ManyToOne(fetch = FetchType.EAGER)
     @JoinColumn(name = "flow_instance_id")
     private FlowInstance flowInstance;

    /**
     * 业务类全路径
     */
    @JoinColumn(name = "business_code")
    private  String businessCode;

    /**
     * 业务类主键
     */
    @JoinColumn(name = "business_id")
    private String  businessId;


    /**
     * 任务定义KEY
     */
    @JoinColumn(name = "act_task_def_key")
    private String  actTaskDefKey;

    /**
     * 是否紧急
     */
    @JoinColumn(name = "instancy_status")
    private Boolean instancyStatus = false;

    /**
     * 执行人ids
     */
    @JoinColumn(name = "executor_ids")
    private String  executorIds;

    /**
     * 上一节点任务key
     */
    @JoinColumn(name = "before_task_def_key")
    private String  beforeTaskDefKey;

    /**
     * 流程逻辑执行顺序
     */
    @JoinColumn(name = "task_order")
    private int taskOrder = 0;

    /**
     * 租户代码
     */
    @JoinColumn(name = "tenant_code")
    private String  tenantCode;




    public FlowInstance getFlowInstance() {
        return flowInstance;
    }

    public void setFlowInstance(FlowInstance flowInstance) {
        this.flowInstance = flowInstance;
    }

    public String getBusinessCode() {
        return businessCode;
    }

    public void setBusinessCode(String businessCode) {
        this.businessCode = businessCode;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getActTaskDefKey() {
        return actTaskDefKey;
    }

    public void setActTaskDefKey(String actTaskDefKey) {
        this.actTaskDefKey = actTaskDefKey;
    }

    public String getExecutorIds() {
        return executorIds;
    }

    public void setExecutorIds(String executorIds) {
        this.executorIds = executorIds;
    }

    public String getBeforeTaskDefKey() {
        return beforeTaskDefKey;
    }

    public void setBeforeTaskDefKey(String beforeTaskDefKey) {
        this.beforeTaskDefKey = beforeTaskDefKey;
    }

    public int getTaskOrder() {
        return taskOrder;
    }

    public void setTaskOrder(int taskOrder) {
        this.taskOrder = taskOrder;
    }

    public Boolean getInstancyStatus() {
        return instancyStatus;
    }

    public void setInstancyStatus(Boolean instancyStatus) {
        this.instancyStatus = instancyStatus;
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
