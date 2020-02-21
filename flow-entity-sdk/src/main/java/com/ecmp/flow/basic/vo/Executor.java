package com.ecmp.flow.basic.vo;

import java.io.Serializable;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：执行人Vo
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/6/5 11:28      tj                    新建
 * <p/>
 * *************************************************************************************************
 */
public class Executor implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    private String id;

    /**
     * 用户员工编号
     */
    private String code;

    /**
     * 用户名
     */
    private String name;

    /**
     * 组织机构id
     */
    private String organizationId;

    /**
     * 组织机构code
     */
    private String organizationCode;

    /**
     * 组织机构名称
     */
    private String organizationName;

    /**
     * 岗位id
     */
    private String positionId;

    /**
     * 岗位代码
     */
    private String positionCode;

    /**
     * 岗位名称
     */
    private String positionName;

    /**
     * 备注
     */
    private String remark;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrganizationCode() {
        return organizationCode;
    }

    public void setOrganizationCode(String organizationCode) {
        this.organizationCode = organizationCode;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public String getPositionCode() {
        return positionCode;
    }

    public void setPositionCode(String positionCode) {
        this.positionCode = positionCode;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass()!=obj.getClass())
            return false;
        Executor p = (Executor)obj;
        if(id == null){
            if(p.getId() !=null)
                return false;
        }else if(!id.equals(p.getId()))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime*result+((id==null)?0:id.hashCode());
        return result;
    }
}
