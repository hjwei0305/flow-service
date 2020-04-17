package com.ecmp.core.entity;

import java.util.List;

/**
 * 实现功能：树形结构特征接口
 *
 * @param <T> TreeEntity的子类
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2017/3/12 13:08
 */
public interface TreeEntity<T extends TreeEntity<T>> extends IRank {
    /**
     * 代码路径分隔符
     */
    String CODE_DELIMITER = "|";
    /**
     * 名称路径分隔符
     */
    String NAME_DELIMITER = "/";


    /**
     * 属性
     */
    String CODE = "code";
    String NAME = "name";
    //@see IRank#RANK
    //String RANK = "rank";
    String NODE_LEVEL = "nodeLevel";
    String CODE_PATH = "codePath";
    String NAME_PATH = "namePath";
    String PARENT_ID = "parentId";

    /**
     * @return Id标识
     */
    String getId();

    /**
     * @return 代码
     */
    String getCode();

    /**
     * @return 名称
     */
    String getName();

    /**
     * @return 层级
     */
    Integer getNodeLevel();

    void setNodeLevel(Integer nodeLevel);

    /**
     * @return 代码路径
     */
    String getCodePath();

    void setCodePath(String codePath);

    /**
     * @return 名称路径
     */
    String getNamePath();

    void setNamePath(String namePath);

    /**
     * @return 父节点Id
     */
    String getParentId();

    void setParentId(String parentId);

    /**
     * @return 排序
     */
    @Override
    Integer getRank();

    List<T> getChildren();

    void setChildren(List<T> children);
}
