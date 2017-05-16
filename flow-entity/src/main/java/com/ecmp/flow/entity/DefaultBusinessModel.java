package com.ecmp.flow.entity;

import com.ecmp.core.entity.BaseEntity;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.vo.conditon.DefaultBusinessModelCondition;
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

@Entity(name = "default_business_model")
@DynamicInsert
@DynamicUpdate
public class DefaultBusinessModel extends com.ecmp.core.entity.BaseAuditableEntity implements IBusinessFlowEntity{

    @Transient
    protected static final Log logger = LogFactory
            .getLog(DefaultBusinessModel.class);
    /**
     * 业务名称
     */
    @Column(name = "name",length = 80,nullable = false)
    private  String name;

    /**
     * 乐观锁-版本
     */
    @Version
    @Column(name = "version")
    private Integer version=0;

    /**
     * 当前流程状态
     */
    @Column(name = "flowStatus",length = 10)
    private FlowStatus flowStatus;

    /**
     * 组织机构代码
     */
    @Column(name = "orgCode",length = 20)
    private String orgCode;

    /**
     * 组织机构Id
     */
    @Column(name = "orgId",length = 36)
    private String orgId;

    /**
     * 组织机构名称
     */
    @Column(name = "orgName",length = 80)
    private String orgName;

    /**
     * 组织机构层级路径
     */
    @Column(name = "orgPath",length = 500)
    private String orgPath;

    /**
     * 优先级别
     */
    @Column(name = "priority")
    private int priority;

    /**
     * 工作说明
     */
    @Column(name = "workCaption",length = 1000)
    private  String workCaption;



    /**
     * 获取条件实体
     * @return
     */
   public IConditionPojo getConditionPojo() {
        IConditionPojo conditionPojo = new DefaultBusinessModelCondition();
       if(this.getId()!=null){
           try {
               BeanUtils.copyProperties(conditionPojo,this);
           } catch (IllegalAccessException e) {
               e.printStackTrace();
               logger.error(e.getMessage());
               conditionPojo = null;
           } catch (InvocationTargetException e) {
               e.printStackTrace();
               logger.error(e.getMessage());
               conditionPojo = null;
           }
       }
        return conditionPojo;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public FlowStatus getFlowStatus() {
        return flowStatus;
    }

    @Override
    public void setFlowStatus(FlowStatus flowStatus) {
        this.flowStatus = flowStatus;
    }

    @Override
    public String getOrgCode() {
        return orgCode;
    }

    @Override
    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    @Override
    public String getOrgId() {
        return orgId;
    }

    @Override
    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    @Override
    public String getOrgName() {
        return orgName;
    }

    @Override
    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    @Override
    public String getOrgPath() {
        return orgPath;
    }

    @Override
    public void setOrgPath(String orgPath) {
        this.orgPath = orgPath;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public String getWorkCaption() {
        return workCaption;
    }

    @Override
    public void setWorkCaption(String workCaption) {
        this.workCaption = workCaption;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", this.getId())
                .append("name", this.name)
                .append("orgPath", this.orgPath)
                .append("orgId", this.orgId)
                .append("orgName", this.orgName)
                .append("version", this.version)
                .append("flowStatus", this.orgPath)
                .append("priority", this.priority)
                .toString();
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
}
