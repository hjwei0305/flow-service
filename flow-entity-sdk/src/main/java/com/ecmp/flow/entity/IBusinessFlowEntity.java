package com.ecmp.flow.entity;

import com.ecmp.core.entity.IAuditable;
import com.ecmp.flow.constant.FlowStatus;

import java.lang.reflect.InvocationTargetException;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：流程业务实体接口定义
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/5/15 9:15      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public interface IBusinessFlowEntity  extends IAuditable {

    //当前流程状态
    FlowStatus getFlowStatus();
    void setFlowStatus(FlowStatus flowStatus);

    //组织机构代码
    String  getOrgCode();
    void  setOrgCode(String orgCode);

    //业务实体的组织机构节点Id
    String getOrgId();
    void setOrgId(String orgId);

    // 业务实体的组织机构节点名称
    String getOrgName();
    void setOrgName(String orgName);

    // 业务实体的组织机构节点路径
    String getOrgPath();
    void setOrgPath(String orgPath);

    // 业务实体的处理优先级
    int getPriority();
    void setPriority(int priority);

    //工作说明
    String getWorkCaption();
    void setWorkCaption(String workCaption);

    //名称
    String getName();
    void setName(String name);

    //租户代码
   String getTenantCode();
   void setTenantCode(String tenantCode);

    //业务单据号
   void setBusinessCode(String businessCode);
    String getBusinessCode();


    // 获取条件实体
    IConditionPojo getConditionPojo() throws InvocationTargetException, IllegalAccessException;

}
