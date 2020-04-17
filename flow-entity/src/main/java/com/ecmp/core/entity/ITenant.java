package com.ecmp.core.entity;

/**
 * 实现功能：租户业务实体特征接口
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2017/4/21 12:36
 */
public interface ITenant {
    /**
     * 租户代码属性
     */
    String TENANT_CODE = "tenantCode";

    /**
     * @return 租户代码
     */
    String getTenantCode();

    /**
     * 设置租户代码
     *
     * @param tenantCode 租户代码
     */
    void setTenantCode(String tenantCode);
}
