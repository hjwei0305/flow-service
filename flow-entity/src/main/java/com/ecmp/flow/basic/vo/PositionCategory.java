package com.ecmp.flow.basic.vo;

import com.ecmp.core.entity.BaseAuditableEntity;
import com.ecmp.core.entity.ICodeUnique;
import com.ecmp.core.entity.ITenant;

import java.io.Serializable;


public class PositionCategory extends BaseAuditableEntity
        implements ITenant,ICodeUnique, Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 代码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 租户代码
     */
    private String tenantCode;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }
}
