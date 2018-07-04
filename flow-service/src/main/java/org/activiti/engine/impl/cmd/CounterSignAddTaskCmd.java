/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.impl.cmd;

import com.ecmp.flow.util.UUIDGenerator;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.task.Task;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author tj
 * 自定义类，处理任务恢复
 */
public class CounterSignAddTaskCmd implements Command<Void>, Serializable {

	private static final long serialVersionUID = 1L;

	protected TaskEntity task;

	protected ExecutionEntity executionEntity;

	protected HistoricTaskInstance currTask;

	String userId;

	public CounterSignAddTaskCmd(String userId, Execution executionOld,HistoricTaskInstance currTask) {
		this.userId = userId;
		this.executionEntity  = (ExecutionEntity)executionOld;
		this.currTask = currTask;
	}
	
	public Void execute(CommandContext commandContext) {
		ExecutionEntity parent = executionEntity.getParent();
		ExecutionEntity executionEntityNew = executionEntity.createExecution(parent);
		executionEntityNew.setParent(parent);
		executionEntityNew.setParentId(parent.getId());

		List<IdentityLinkEntity> oldIdentityLinkEntityList = executionEntity.getIdentityLinks();
		if(oldIdentityLinkEntityList!=null && !oldIdentityLinkEntityList.isEmpty()){
			IdentityLinkEntity  oldIdentityLinkEntity =  oldIdentityLinkEntityList.get(0);
			executionEntityNew.addIdentityLink( userId, oldIdentityLinkEntity.getGroupId(), oldIdentityLinkEntity.getType());
		}
		TaskEntity newTask = TaskEntity.create(new Date());
		newTask.setAssignee(currTask.getAssignee());
		newTask.setCategory(currTask.getCategory());
		// newTask.setDelegationState(delegationState);
		newTask.setDescription(currTask.getDescription());
		newTask.setDueDate(currTask.getDueDate());
		newTask.setFormKey(currTask.getFormKey());
		// newTask.setLocalizedDescription(currTask.getl);
		// newTask.setLocalizedName(currTask.get);
		newTask.setName(currTask.getName());
//		newTask.setOwner(userId);
		newTask.setParentTaskId(currTask.getParentTaskId());
		newTask.setPriority(currTask.getPriority());
		newTask.setTenantId(currTask.getTenantId());
		newTask.setCreateTime(new Date());

		newTask.setId(UUIDGenerator.getUUID());
		newTask.setExecutionId(executionEntityNew.getId());
		newTask.setProcessDefinitionId(currTask.getProcessDefinitionId());
		newTask.setProcessInstanceId(currTask.getProcessInstanceId());
		newTask.setVariables(currTask.getProcessVariables());
		newTask.setTaskDefinitionKey(currTask.getTaskDefinitionKey());
		this.task = newTask;
//    if (task.getRevision()==0) {
		executionEntityNew.setActive(true);
		executionEntityNew.setConcurrent(true);
		executionEntityNew.setScope(false);
//		executionEntityNew.update();
         task.insert(executionEntityNew);

      
      // Need to to be done here, we can't make it generic for standalone tasks 
      // and tasks from a process, as the order of setting properties is
      // completely different.
//      if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
//        Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
//          ActivitiEventBuilder.createEntityEvent(ActivitiEventType.TASK_CREATED, task));
//
//        if (task.getAssignee() != null) {
//	        // The assignment event is normally fired when calling setAssignee. However, this
//	        // doesn't work for standalone tasks as the commandcontext is not availble.
//	        Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
//	            ActivitiEventBuilder.createEntityEvent(ActivitiEventType.TASK_ASSIGNED, task));
//        }
//      }
     
//    executionEntity.
//      task.setExecution((DelegateExecution)executionEntity);
//    task.setExecution(executionEntity);
//    
//    task.upda    } else {
//      task.update();
//    }te();
//

    return null;
	}

}
