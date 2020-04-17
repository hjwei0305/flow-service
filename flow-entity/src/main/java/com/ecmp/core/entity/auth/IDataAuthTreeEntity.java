package com.ecmp.core.entity.auth;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：权限管理的树形业务实体接口
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017-06-12 10:44      王锦光(wangj)                新建
 * <p/>
 * *************************************************************************************************
 */
public interface IDataAuthTreeEntity extends IDataAuthEntity {
    Integer getRank();

    void setRank(Integer rank);

    Integer getNodeLevel();

    void setNodeLevel(Integer nodeLevel);

    String getParentId();

    void setParentId(String parentId);

    String getCodePath();

    void setCodePath(String codePath);

    String getNamePath();

    void setNamePath(String namePath);
}
