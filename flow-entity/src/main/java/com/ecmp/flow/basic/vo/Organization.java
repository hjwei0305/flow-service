package com.ecmp.flow.basic.vo;

import com.ecmp.core.entity.BaseAuditableEntity;
import com.ecmp.core.entity.IFrozen;
import com.ecmp.core.entity.ITenant;
import com.ecmp.core.entity.TreeEntity;
import com.ecmp.core.entity.auth.IDataAuthTreeEntity;

import java.util.List;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/08/19 12:08        谭军                     新建
 * <p/>
 * *************************************************************************************************
 */
public class Organization extends BaseAuditableEntity
        implements TreeEntity<Organization>, ITenant, IFrozen, IDataAuthTreeEntity {
    /**
     * 组织机构代码
     */
    private String code;

    /**
     * 组织机构名称
     */
    private String name;

    /**
     * 简称
     */
    private String shortName;

    /**
     * 参考码
     */
    private String refCode;

    /**
     * 层级
     */
    private Integer nodeLevel = 0;

    /**
     * 代码路径
     */
    private String codePath;

    /**
     * 名称路径
     */
    private String namePath;

    /**
     * 父节点Id
     */
    private String parentId;

    /**
     * 排序
     */
    private Integer rank = 0;

    /**
     * 租户代码
     */
    private String tenantCode;

    /**
     * 是否冻结
     */
    private Boolean frozen = Boolean.FALSE;

    private List<Organization> children;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getRefCode() {
        return refCode;
    }

    public void setRefCode(String refCode) {
        this.refCode = refCode;
    }

    @Override
    public Integer getNodeLevel() {
        return nodeLevel;
    }

    @Override
    public void setNodeLevel(Integer nodeLevel) {
        this.nodeLevel = nodeLevel;
    }

    @Override
    public String getCodePath() {
        return codePath;
    }

    @Override
    public void setCodePath(String codePath) {
        this.codePath = codePath;
    }

    @Override
    public String getNamePath() {
        return namePath;
    }

    @Override
    public void setNamePath(String namePath) {
        this.namePath = namePath;
    }

    @Override
    public String getParentId() {
        return parentId;
    }

    @Override
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @Override
    public Integer getRank() {
        return rank;
    }

    @Override
    public void setRank(Integer rank) {
        this.rank = rank;
    }

    @Override
    public String getTenantCode() {
        return tenantCode;
    }

    @Override
    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    @Override
    public Boolean getFrozen() {
        return frozen;
    }

    @Override
    public void setFrozen(Boolean frozen) {
        this.frozen = frozen;
    }

    @Override
    public List<Organization> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List<Organization> children) {
        this.children = children;
    }
}
