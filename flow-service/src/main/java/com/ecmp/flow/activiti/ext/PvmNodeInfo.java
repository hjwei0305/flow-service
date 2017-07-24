package com.ecmp.flow.activiti.ext;

import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：用于获取当前节点下一步可到达的节点信息
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/7/21 11:23      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public class PvmNodeInfo implements java.io.Serializable {
  private  PvmActivity currActivity;
  private PvmNodeInfo parent;
  private Set<PvmNodeInfo> children = new LinkedHashSet<PvmNodeInfo>();

    public PvmActivity getCurrActivity() {
        return currActivity;
    }

    public void setCurrActivity(PvmActivity currActivity) {
        this.currActivity = currActivity;
    }

    public PvmNodeInfo getParent() {
        return parent;
    }

    public void setParent(PvmNodeInfo parent) {
        this.parent = parent;
    }

    public Set<PvmNodeInfo> getChildren() {
        return children;
    }

    public void setChildren(Set<PvmNodeInfo> children) {
        this.children = children;
    }
}
