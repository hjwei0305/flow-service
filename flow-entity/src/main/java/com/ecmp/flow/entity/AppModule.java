package com.ecmp.flow.entity;

import com.ecmp.core.entity.BaseAuditableEntity;

import javax.persistence.*;

import com.ecmp.core.entity.ICodeUnique;
import com.ecmp.core.entity.IRank;
import com.ecmp.core.entity.ITenant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * <p>
 * *************************************************************************************************
 * </p><p>
 * 实现功能：应用模块
 * </p><p>
 * ------------------------------------------------------------------------------------------------
 * </p><p>
 * 版本          变更时间             变更人                     变更原因
 * </p><p>
 * ------------------------------------------------------------------------------------------------
 * </p><p>
 * 1.0.00      2017/09/06 11:39      谭军(tanjun)                新建
 * </p><p>
 * *************************************************************************************************
 * </p>
 */
@Access(AccessType.FIELD)
@Entity()
@Table(name = "app_module")
@DynamicInsert
@DynamicUpdate
//@Cache(usage = CacheConcurrencyStrategy.READ_ONLY,region="mycache")
@Cacheable(true)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AppModule extends BaseAuditableEntity
        implements ICodeUnique, IRank ,ITenant {

    /**
     * 名称
     */
    @Column(name = "name", length = 30, nullable = false)
    private String name;

    /**
     * 代码
     */
    @Column(name = "code", length = 20, nullable = false, unique = true)
    private String code;

    /**
     * 备注
     */
    @Column(name = "remark")
    private String remark;


    /**
     * web基地址
     */
    @Column(name = "web_base_address")
    private String webBaseAddress;

    /**
     * api基地址
     */
    @Column(name = "api_base_address")
    private String apiBaseAddress;


    /**
     * 租户代码
     */
    @Column(name = "tenant_code", length = 10)
    private String tenantCode;

    /**
     * 排序号
     */
    @Column(name = "rank", nullable = false)
    private Integer rank;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public String getWebBaseAddress() {
        return webBaseAddress;
    }

    public void setWebBaseAddress(String webBaseAddress) {
        this.webBaseAddress = webBaseAddress;
    }

    public String getApiBaseAddress() {
        return apiBaseAddress;
    }

    public void setApiBaseAddress(String apiBaseAddress) {
        this.apiBaseAddress = apiBaseAddress;
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
