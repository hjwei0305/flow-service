package com.ecmp.flow.vo;


import com.ecmp.flow.basic.vo.Executor;
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
	private String preLineCode;//入口线代码
    private String type;//目前暂时只支持 ----userTask、EndEvent（结束节点）
    private String uiType;//radiobox\checkbox\readOnly
    private String flowTaskType;//自定义任务类型,common(普通),单签(singleSign),审批任务(approve),会签(CounterSign),(网关)gateWay，服务任务（ServiceTask）
	private String uiUserType;//流程设计器定义的用户选择类型，StartUser、Position、PositionType、SelfDefinition、AnyOne、PositionAndOrg、PositionAndOrgAndSelfDefinition、PositionTypeAndOrg
	private Set<Executor> executorSet;//记录流程设计阶段所选择的执行人
	private String userVarName;//流程节点用户变量名称
    private String currentTaskType;//当前任务节点类型，自定义任务类型,common(普通),单签(singleSign),审批任务(approve),会签(CounterSign)
	private Boolean counterSignLastTask;//是否是最后一个会签/并、串子任务执行人;

	private String callActivityPath;//调用子流程中的节点路径

	private String flowDefVersionId;//流程定义版本id
	private String flowDefVersionName;//流程定义版本name
	private Integer flowDefVersionCode;//流程定义版本Code
	private String flowTaskId;//任务id
	private Boolean allowChooseInstancy;//是否允许选择任务紧急状态

	private Boolean currentSingleTaskAuto; //当前节点是否配置了单人单任务自动执行

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

	public Boolean getCurrentSingleTaskAuto() {
		return currentSingleTaskAuto;
	}

	public void setCurrentSingleTaskAuto(Boolean currentSingleTaskAuto) {
		this.currentSingleTaskAuto = currentSingleTaskAuto;
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

	public String getFlowDefVersionId() {
		return flowDefVersionId;
	}

	public void setFlowDefVersionId(String flowDefVersionId) {
		this.flowDefVersionId = flowDefVersionId;
	}

	public String getFlowTaskId() {
		return flowTaskId;
	}

	public void setFlowTaskId(String flowTaskId) {
		this.flowTaskId = flowTaskId;
	}

	public String getFlowDefVersionName() {
		return flowDefVersionName;
	}

	public void setFlowDefVersionName(String flowDefVersionName) {
		this.flowDefVersionName = flowDefVersionName;
	}

	public Integer getFlowDefVersionCode() {
		return flowDefVersionCode;
	}

	public void setFlowDefVersionCode(Integer flowDefVersionCode) {
		this.flowDefVersionCode = flowDefVersionCode;
	}

	public String getPreLineCode() {
		return preLineCode;
	}

	public void setPreLineCode(String preLineCode) {
		this.preLineCode = preLineCode;
	}

	public Boolean getAllowChooseInstancy() {
		return allowChooseInstancy;
	}

	public void setAllowChooseInstancy(Boolean allowChooseInstancy) {
		this.allowChooseInstancy = allowChooseInstancy;
	}
}
