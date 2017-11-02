package com.ecmp.flow.entity;

import com.ecmp.core.entity.BaseEntity;
import com.ecmp.flow.constant.BusinessEntityAnnotaion;
import com.ecmp.flow.constant.ConditionAnnotaion;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.vo.conditon.DefaultBusinessModelCondition;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.persistence.Version;
import java.lang.reflect.InvocationTargetException;


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
}
