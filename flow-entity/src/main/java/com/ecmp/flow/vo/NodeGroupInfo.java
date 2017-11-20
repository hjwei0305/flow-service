package com.ecmp.flow.vo;


import com.ecmp.flow.basic.vo.Executor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：定义任务节点信息
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/5/23 10:33      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public class NodeGroupInfo implements Serializable{

    private List<String> ids = new ArrayList<>();
    private String name;
    private String gateWayName;//网关线名称
	private String preLineName;//入口线名称
    private String type;//目前暂时只支持 ----userTask、EndEvent（结束节点）
    private String uiType;//radiobox\checkbox\readOnly
    private String flowTaskType;//自定义任务类型,common(普通),单签(singleSign),审批任务(approve),会签(CounterSign),(网关)gateWay，服务任务（ServiceTask）
	private String uiUserType;//流程设计器定义的用户选择类型，StartUser、Position、PositionType、SelfDefinition、AnyOne
	private Set<Executor> executorSet;//记录流程设计阶段所选择的执行人
	private String userVarName;//流程节点用户变量名称
    private String currentTaskType;//当前任务节点类型，自定义任务类型,common(普通),单签(singleSign),审批任务(approve),会签(CounterSign)
	private Boolean counterSignLastTask;//是否是最后一个会签/并、串子任务执行人;

	private String callActivityPath;//调用子流程中的节点路径

//	private MultiInstanceConfig multiInstanceConfig;//记录会签任务信息

	public List<String> getIds() {
		return ids;
	}
	public void setIds(List<String> ids) {
		this.ids = ids;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getUiType() {
		return uiType;
	}
	public void setUiType(String uiType) {
		this.uiType = uiType;
	}
	public String getFlowTaskType() {
		return flowTaskType;
	}
	public void setFlowTaskType(String flowTaskType) {
		this.flowTaskType = flowTaskType;
	}

	public String getUiUserType() {
		return uiUserType;
	}

	public void setUiUserType(String uiUserType) {
		this.uiUserType = uiUserType;
	}

//	public Set<Employee> getEmployeeSet() {
//		return employeeSet;
//	}
//
//	public void setEmployeeSet(Set<Employee> employeeSet) {
//		this.employeeSet = employeeSet;
//	}

	public String getUserVarName() {
		return userVarName;
	}

	public void setUserVarName(String userVarName) {
		this.userVarName = userVarName;
	}

	public Set<Executor> getExecutorSet() {
		return executorSet;
	}

	public void setExecutorSet(Set<Executor> executorSet) {
		this.executorSet = executorSet;
	}

	//	public MultiInstanceConfig getMultiInstanceConfig() {
//		return multiInstanceConfig;
//	}
//
//	public void setMultiInstanceConfig(MultiInstanceConfig multiInstanceConfig) {
//		this.multiInstanceConfig = multiInstanceConfig;
//	}


	public String getGateWayName() {
		return gateWayName;
	}

	public void setGateWayName(String gateWayName) {
		this.gateWayName = gateWayName;
	}

	public String getPreLineName() {
		return preLineName;
	}

	public void setPreLineName(String preLineName) {
		this.preLineName = preLineName;
	}

	public Boolean getCounterSignLastTask() {
		return counterSignLastTask;
	}

	public void setCounterSignLastTask(Boolean counterSignLastTask) {
		this.counterSignLastTask = counterSignLastTask;
	}

	public String getCurrentTaskType() {
		return currentTaskType;
	}

	public void setCurrentTaskType(String currentTaskType) {
		this.currentTaskType = currentTaskType;
	}

	public String getCallActivityPath() {
		return callActivityPath;
	}

	public void setCallActivityPath(String callActivityPath) {
		this.callActivityPath = callActivityPath;
	}
}
