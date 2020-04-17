package com.ecmp.core.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 实现功能: 移除和添加关系的输入参数
 *
 * @author 王锦光 wangj
 * @version 1.0.1 2019-03-07 10:36
 */
public class RelationParam implements Serializable {
    private static final long serialVersionUID = 1820045234329536004L;
    /**
     * 父实体Id
     */
    protected String parentId;

    /**
     * 子实体Id清单
     */
    protected List<String> childIds;

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public List<String> getChildIds() {
        return childIds;
    }

    public void setChildIds(List<String> childIds) {
        this.childIds = childIds;
    }
}
