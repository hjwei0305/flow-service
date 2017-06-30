package com.ecmp.flow.service;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.FileInputStream;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.junit.Rule;
import org.junit.Test;

public class ProcessTestS1 {

	private String filename = "C:\\Users\\tanjun\\git\\activiti-in-action-codes\\activiti-in-action-codes\\bpmn20-example\\src\\main\\resources\\me\\kafeitu\\activiti\\helloworld\\pTestS1.bpmn";

	@Rule
	public ActivitiRule activitiRule = new ActivitiRule();
	ProcessInstance processInstance ;

	@Test
	public void startProcess() throws Exception {
		RepositoryService repositoryService = activitiRule.getRepositoryService();
		repositoryService.createDeployment().addInputStream("pTestS1.bpmn",
				new FileInputStream(filename)).deploy();
		RuntimeService runtimeService = activitiRule.getRuntimeService();
		Map<String, Object> variableMap = new HashMap<String, Object>();
		variableMap.put("name", "Activiti");
		variableMap.put("UserTask_3_Normal", "admin");
		variableMap.put("UserTask_5_Normal", "admin");
		variableMap.put("UserTask_8_Normal", "admin");
		variableMap.put("UserTask_9_Normal", "admin");
		
		processInstance = runtimeService.startProcessInstanceByKey("paramsdinsdTest", variableMap);
		assertNotNull(processInstance.getId());
		System.out.println("id " + processInstance.getId() + " "
				+ processInstance.getProcessDefinitionId());
		this.findMyPersonalTask();
		this.findMyPersonalTask();
		this.findMyPersonalTask2();


		this.findMyPersonalTask();
	}

	/** 查询当前人的个人任务 */
	@Test
	public void findMyPersonalTask() {
		String assignee = "admin";
		List<Task> list = activitiRule.getTaskService()// 与正在执行的任务管理相关的Service
				.createTaskQuery()// 创建任务查询对象
				/** 查询条件（where部分） */
				.taskAssignee(assignee)// 指定个人任务查询，指定办理人
				// .taskCandidateUser(candidateUser)//组任务的办理人查询
				// .processDefinitionId(processDefinitionId)//使用流程定义ID查询
				// .processInstanceId(processInstanceId)//使用流程实例ID查询
				// .executionId(executionId)//使用执行对象ID查询
				/** 排序 */
				.orderByTaskCreateTime().asc()// 使用创建时间的升序排列
				/** 返回结果集 */
				// .singleResult()//返回惟一结果集
				// .count()//返回结果集的数量
				// .listPage(firstResult, maxResults);//分页查询
				.list();// 返回列表
		List<Task> unsignedTasks = activitiRule.getTaskService().createTaskQuery().taskCandidateUser(assignee).active().list();

		// 合并
		list.addAll(unsignedTasks);
		if (list != null && list.size() > 0) {
			for (Task task : list) {
				System.out.println("任务ID:" + task.getId());
				System.out.println("任务名称:" + task.getName());
				System.out.println("任务的创建时间:" + task.getCreateTime());
				System.out.println("任务的办理人:" + task.getAssignee());
				System.out.println("流程实例ID：" + task.getProcessInstanceId());
				System.out.println("执行对象ID:" + task.getExecutionId());
				System.out.println("流程定义ID:" + task.getProcessDefinitionId());
				System.out.println("########################################################");
				
				// 任务ID
				String taskId =  task.getId();
				activitiRule.getTaskService()// 与正在执行的任务管理相关的Service
						.complete(taskId);
				System.out.println("完成任务：任务ID：" + taskId);
			}
		}
	}
	/** 查询当前人的个人任务 */
	@Test
	public void findMyPersonalTask2() {
		String assignee = "admin";
		List<Task> list = activitiRule.getTaskService()// 与正在执行的任务管理相关的Service
				.createTaskQuery()// 创建任务查询对象
				.processInstanceId(processInstance.getId())
				.taskDefinitionKey("UserTask_9_Normal").active()
				/** 查询条件（where部分） */
				//.taskAssignee(assignee)// 指定个人任务查询，指定办理人
				// .taskCandidateUser(candidateUser)//组任务的办理人查询
				// .processDefinitionId(processDefinitionId)//使用流程定义ID查询
				// .processInstanceId(processInstanceId)//使用流程实例ID查询
				// .executionId(executionId)//使用执行对象ID查询
				/** 排序 */
				.orderByTaskCreateTime().asc()// 使用创建时间的升序排列
				/** 返回结果集 */
				// .singleResult()//返回惟一结果集
				// .count()//返回结果集的数量
				// .listPage(firstResult, maxResults);//分页查询
				.list();// 返回列表
		//List<Task> unsignedTasks = activitiRule.getTaskService().createTaskQuery().taskCandidateUser(assignee).active().list();

		// 合并
		//list.addAll(unsignedTasks);
		if (list != null && list.size() > 0) {
			for (Task task : list) {
				System.out.println("任务ID:" + task.getId());
				System.out.println("任务名称:" + task.getName());
				System.out.println("任务的创建时间:" + task.getCreateTime());
				System.out.println("任务的办理人:" + task.getAssignee());
				System.out.println("流程实例ID：" + task.getProcessInstanceId());
				System.out.println("执行对象ID:" + task.getExecutionId());
				System.out.println("流程定义ID:" + task.getProcessDefinitionId());
				System.out.println("########################################################");
				
				// 任务ID
				String taskId =  task.getId();
				activitiRule.getTaskService()// 与正在执行的任务管理相关的Service
						.complete(taskId);
				System.out.println("完成任务：任务ID：" + taskId);
			}
		}
	}

	/** 完成我的任务 */
	@Test
	public void completeMyPersonalTask() {
		// 任务ID
		String taskId = "12507";
		activitiRule.getTaskService()// 与正在执行的任务管理相关的Service
				.complete(taskId);
		System.out.println("完成任务：任务ID：" + taskId);
	}
}