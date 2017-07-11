package com.ecmp.flow.vo;

import com.ecmp.basic.entity.Employee;
import com.ecmp.basic.entity.vo.Executor;
import com.ecmp.flow.vo.bpmn.MultiInstanceConfig;

import java.io.Serializable;
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
public class NodeInfo implements Serializable{

    private String id;
    private String name;
    private String gateWayName;//网关线名称
	private String preLineName;//入口线名称
    private String type;//目前暂时只支持 ----userTask、EndEvent（结束节点）
    private String uiType;//radiobox\checkbox\readOnly
    private String flowTaskType;//自定义任务类型,common(普通),单签(singleSign),审批任务(approve),会签(CounterSign),(网关)gateWay
	private String uiUserType;//流程设计器定义的用户选择类型，StartUser、Position、PositionType、SelfDefinition、AnyOne
//    private Set<Employee> employeeSet;//记录流程设计阶段所选择的员工
	private Set<Executor> executorSet;//记录流程设计阶段所选择的执行人
	private String userVarName;//流程节点用户变量名称

	private Boolean counterSignLastTask;//是否是最后一个会签子任务执行人;

//	private MultiInstanceConfig multiInstanceConfig;//记录会签任务信息


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
}
