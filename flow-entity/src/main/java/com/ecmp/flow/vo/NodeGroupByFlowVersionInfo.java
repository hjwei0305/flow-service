package com.ecmp.flow.vo;


import com.ecmp.flow.basic.vo.Executor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：批量审批，通过版本分组
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/5/23 10:33      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public class NodeGroupByFlowVersionInfo implements Serializable{

    private String id;//流程版本id
    private String name;//流程版本名称
    private List<NodeGroupInfo> nodeGroupInfos = new ArrayList<>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<NodeGroupInfo> getNodeGroupInfos() {
		return nodeGroupInfos;
	}

	public void setNodeGroupInfos(List<NodeGroupInfo> nodeGroupInfos) {
		this.nodeGroupInfos = nodeGroupInfos;
	}
}
