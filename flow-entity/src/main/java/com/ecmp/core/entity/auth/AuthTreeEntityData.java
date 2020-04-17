package com.ecmp.core.entity.auth;

import com.ecmp.core.entity.TreeEntity;

import java.util.List;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：权限对象的数据（树形实体类）
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017-06-12 10:26      王锦光(wangj)                新建
 * <p/>
 * *************************************************************************************************
 */
public class AuthTreeEntityData extends AuthEntityData implements TreeEntity<AuthTreeEntityData> {
    private static final long serialVersionUID = 1L;
    /**
     * 排序
     */
    protected Integer rank;
    /**
     * 层级
     */
    protected Integer nodeLevel;
    /**
     * 父节点Id
     */
    protected String parentId;
    /**
     * 代码路径
     */
    protected String codePath;
    /**
     * 名称路径
     */
    protected String namePath;
    /**
     * 子节点清单
     */
    protected List<AuthTreeEntityData> children;

    /**
     * 默认构造函数
     */
    public AuthTreeEntityData() {
    }

    /**
     * 构造函数（业务实体）
     *
     * @param entity 业务实体
     */
    public AuthTreeEntityData(IDataAuthTreeEntity entity) {
        super(entity);
        rank = entity.getRank();
        nodeLevel = entity.getNodeLevel();
        parentId = entity.getParentId();
        codePath = entity.getCodePath();
        namePath = entity.getNamePath();
    }

    @Override
    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
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
    public String getParentId() {
        return parentId;
    }

    @Override
    public void setParentId(String parentId) {
        this.parentId = parentId;
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
    public List<AuthTreeEntityData> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List<AuthTreeEntityData> children) {
        this.children = children;
    }
}
