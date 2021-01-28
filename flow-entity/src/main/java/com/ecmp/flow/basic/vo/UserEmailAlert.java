package com.ecmp.flow.basic.vo;

import com.ecmp.core.entity.BaseAuditableEntity;
import com.ecmp.core.entity.ITenant;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


public class UserEmailAlert extends BaseAuditableEntity implements ITenant, Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 用户Id
     */
    private String userId;

    /**
     * 待办工作数量
     */
    private Integer toDoAmount=0;

    /**
     * 间隔时间（小时）
     */
    private Integer hours=0;

    /**
     * 最后提醒时间
     */
    private Date lastTime;

    /**
     * 租户代码
     */
    private String tenantCode;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getToDoAmount() {
        return toDoAmount;
    }

    public void setToDoAmount(Integer toDoAmount) {
        this.toDoAmount = toDoAmount;
    }

    public Integer getHours() {
        return hours;
    }

    public void setHours(Integer hours) {
        this.hours = hours;
    }

    public Date getLastTime() {
        return lastTime;
    }

    public void setLastTime(Date lastTime) {
        this.lastTime = lastTime;
    }

    @Override
    public String getTenantCode() {
        return tenantCode;
    }

    @Override
    public void setTenantCode(String tenantCode) {
       this.tenantCode=tenantCode;
    }
}
