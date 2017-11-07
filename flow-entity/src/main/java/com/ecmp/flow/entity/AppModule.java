package com.ecmp.flow.entity;

import com.ecmp.core.entity.BaseAuditableEntity;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.ecmp.core.entity.ICodeUnique;
import com.ecmp.core.entity.IRank;
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
//@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AppModule extends BaseAuditableEntity
        implements ICodeUnique, IRank {

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

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }
}
