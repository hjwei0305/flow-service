package com.ecmp.flow.service;

import com.ecmp.flow.ActivitiContextTestCase;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class ProcessTestS2 extends ActivitiContextTestCase {

	private String filename = "C:\\Users\\tanjun\\git\\activiti-in-action-codes\\activiti-in-action-codes\\bpmn20-example\\src\\main\\resources\\me\\kafeitu\\activiti\\helloworld\\pTestS1.bpmn";


	ProcessInstance processInstance ;

	@Test
	public void startProcess() throws Exception {

		repositoryService.createDeployment().addInputStream("pTestS1.bpmn",
				new FileInputStream(filename)).deploy();

		Map<String, Object> variableMap = new HashMap<String, Object>();
		variableMap.put("UserTask_8_Normal", "admin");
		
		processInstance = runtimeService.startProcessInstanceByKey("paramsdinsdTest", variableMap);
		assertNotNull(processInstance.getId());
		System.out.println("id " + processInstance.getId() + " "
				+ processInstance.getProcessDefinitionId());

		variableMap.clear();
		variableMap.put("UserTask_3_Normal", "admin");
		variableMap.put("UserTask_9_Normal", "admin");
		this.findMyPersonalTask("UserTask_8",variableMap);

		variableMap.clear();
		variableMap.put("UserTask_5_Normal", "admin");
		this.findMyPersonalTask("UserTask_3",variableMap);
		this.findMyPersonalTask("UserTask_9",variableMap);

		this.findMyPersonalTask("UserTask_5",variableMap);

	}

	/** 查询当前人的个人任务 */
	public void findMyPersonalTask(String taskDef,Map<String, Object> variableMap) {
		String assignee = "admin";
		List<Task> list = taskService// 与正在执行的任务管理相关的Service
				.createTaskQuery().processInstanceId(processInstance.getId())// 创建任务查询对象
				.taskDefinitionKey(taskDef)
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
	//	List<Task> unsignedTasks = taskService.createTaskQuery().taskCandidateUser(assignee).active().list();

		// 合并
	//	list.addAll(unsignedTasks);
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
				taskService// 与正在执行的任务管理相关的Service
						.complete(taskId,variableMap);
				System.out.println("完成任务：任务ID：" + taskId);
			}
		}
	}

}