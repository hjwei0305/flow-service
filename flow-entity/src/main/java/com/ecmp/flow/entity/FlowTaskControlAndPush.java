package com.ecmp.flow.entity;

import com.ecmp.core.entity.BaseAuditableEntity;
import com.ecmp.core.entity.RelationEntity;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;


/**
 * 推送任务关联表
 */
@Entity
@Access(AccessType.FIELD)
@Table(name = "flow_task_control_and_push")
@DynamicInsert
@DynamicUpdate
public class FlowTaskControlAndPush extends BaseAuditableEntity implements RelationEntity<FlowTaskPushControl, FlowTaskPush> {

    /**
     * 推送任务控制表（父实体）
     */
    @ManyToOne
    @JoinColumn(name = "control_id", nullable = false)
    private FlowTaskPushControl parent;

    /**
     * 推送任务记录表（子实体）
     */
    @ManyToOne
    @JoinColumn(name = "push_id", nullable = false)
    private FlowTaskPush child;


    @Override
    public FlowTaskPushControl getParent() {
        return parent;
    }

    @Override
    public void setParent(FlowTaskPushControl parent) {
        this.parent = parent;
    }


    @Override
    public FlowTaskPush getChild() {
        return child;
    }

    @Override
    public void setChild(FlowTaskPush child) {
        this.child = child;
    }
}
