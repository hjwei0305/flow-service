package com.ecmp.flow.entity;

import com.ecmp.annotation.Remark;
import com.ecmp.flow.constant.BusinessEntityAnnotaion;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Transient;


/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：单据测试
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
@Entity(name = "default_business_model")
@DynamicInsert
@DynamicUpdate
@BusinessEntityAnnotaion(conditionBean="com.ecmp.flow.vo.conditon.DefaultBusinessModelCondition",daoBean="defaultBusinessModelDao")
public class DefaultBusinessModel extends AbstractBusinessModel{
    /**
     * 单价
     */
    private double unitPrice=0;

    /**
     * 数量
     */
    private int count=0;

    /**
     * 金额
     */
    private double sum = 0;

    /**
     * 申请说明
     */
    private  String applyCaption;

    @Remark("单价")
    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    @Remark("数量")
    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getSum() {
        return sum = unitPrice* count;
    }

    public void setSum(double sum) {
        this.sum =  unitPrice* count;
    }

    public String getApplyCaption() {
        return applyCaption;
    }

    public void setApplyCaption(String applyCaption) {
        this.applyCaption = applyCaption;
    }

    public DefaultBusinessModel() {
    }

    public DefaultBusinessModel(double unitPrice, int count, double sum, String applyCaption) {
        this.unitPrice = unitPrice;
        this.count = count;
        this.sum = sum;
        this.applyCaption = applyCaption;
    }

    @Transient
    private DefaultBusinessModel2 defaultBusinessModel2;

    @Transient
    private DefaultBusinessModel3 defaultBusinessModel3;

    @Remark("采购")
    public DefaultBusinessModel2 getDefaultBusinessModel2() {
        return defaultBusinessModel2;
    }

    public void setDefaultBusinessModel2(DefaultBusinessModel2 defaultBusinessModel2) {
        this.defaultBusinessModel2 = defaultBusinessModel2;
    }

    @Remark("销售")
    public DefaultBusinessModel3 getDefaultBusinessModel3() {
        return defaultBusinessModel3;
    }

    public void setDefaultBusinessModel3(DefaultBusinessModel3 defaultBusinessModel3) {
        this.defaultBusinessModel3 = defaultBusinessModel3;
    }
}
