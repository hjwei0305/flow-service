package com.ecmp.core.entity;

/**
 * 实现功能: 分配关系业务实体基类接口
 *
 * @param <P> AbstractEntity的子类
 * @param <C> AbstractEntity的子类
 * @author 王锦光(wangj)
 * @version 2017-05-10 10:19
 */
public interface RelationEntity<P extends AbstractEntity<String>, C extends AbstractEntity<String>> {
    //父实体属性名
    String PARENT_FIELD = "parent";
    //父实体属性名
    String CHILD_FIELD = "child";

    /**
     * 父实体
     *
     * @return 父实体
     */
    P getParent();

    void setParent(P parent);

    /**
     * 子实体
     *
     * @return 子实体
     */
    C getChild();

    void setChild(C child);
}
