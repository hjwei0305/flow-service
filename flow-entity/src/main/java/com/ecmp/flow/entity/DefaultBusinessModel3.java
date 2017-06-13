package com.ecmp.flow.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;


/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * 工作流程默认业务实体定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/05/15 10:20      谭军(tanjun)                新建
 * <p/>
 * *************************************************************************************************
 */
@JsonIgnoreProperties(value={"conditionPojo"})
@Entity(name = "default_business_model3")
@DynamicInsert
@DynamicUpdate
public class DefaultBusinessModel3 extends AbstractBusinessModel{
    /**
     * 单价
     */
    private double unitPrice;

    /**
     * 数量
     */
    private int count;

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
}
