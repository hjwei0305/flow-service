/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl;

import java.util.List;

import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricDetailQuery;
import org.activiti.engine.history.HistoricIdentityLink;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.history.HistoricVariableInstanceQuery;
import org.activiti.engine.history.NativeHistoricActivityInstanceQuery;
import org.activiti.engine.history.NativeHistoricDetailQuery;
import org.activiti.engine.history.NativeHistoricProcessInstanceQuery;
import org.activiti.engine.history.NativeHistoricTaskInstanceQuery;
import org.activiti.engine.history.NativeHistoricVariableInstanceQuery;
import org.activiti.engine.history.ProcessInstanceHistoryLogQuery;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cmd.*;

/**
 * @author Tom Baeyens
 * @author Bernd Ruecker (camunda)
 * @author Christian Stettler
 */
public class HistoryServiceImpl extends ServiceImpl implements HistoryService {
	
	public HistoryServiceImpl() {
		
	}
	
	public HistoryServiceImpl(ProcessEngineConfigurationImpl processEngineConfiguration) {
		super(processEngineConfiguration);
	}

  public HistoricProcessInstanceQuery createHistoricProcessInstanceQuery() {
    return new HistoricProcessInstanceQueryImpl(commandExecutor);
  }

  public HistoricActivityInstanceQuery createHistoricActivityInstanceQuery() {
    return new HistoricActivityInstanceQueryImpl(commandExecutor);
  }

  public HistoricTaskInstanceQuery createHistoricTaskInstanceQuery() {
    return new HistoricTaskInstanceQueryImpl(commandExecutor, processEngineConfiguration.getDatabaseType());
  }

  public HistoricDetailQuery createHistoricDetailQuery() {
    return new HistoricDetailQueryImpl(commandExecutor);
  }

  @Override
  public NativeHistoricDetailQuery createNativeHistoricDetailQuery() {
    return new NativeHistoricDetailQueryImpl(commandExecutor);
  }

  public HistoricVariableInstanceQuery createHistoricVariableInstanceQuery() {
    return new HistoricVariableInstanceQueryImpl(commandExecutor);
  }

  @Override
  public NativeHistoricVariableInstanceQuery createNativeHistoricVariableInstanceQuery() {
    return new NativeHistoricVariableInstanceQueryImpl(commandExecutor);
  }

  public void deleteHistoricTaskInstance(String taskId) {
	    commandExecutor.execute(new DeleteHistoricTaskInstanceCmd(taskId));
	  }
  public void deleteHistoricProcessInstance(String processInstanceId) {
    commandExecutor.execute(new DeleteHistoricProcessInstanceCmd(processInstanceId));
  }

  @Override
  public void deleteHistoricActivityInstancesByTaskId(String taskId) {
  	// TODO Auto-generated method stub
	  commandExecutor.execute(new DeleteHistoricActivityInstancesByTaskIdCmd(taskId));
  }
  

@Override
public void deleteHistoricIdentityLinksByTaskId(String taskId) {
	// TODO Auto-generated method stub
	 commandExecutor.execute(new DeleteHistoricIdentityLinksByTaskIdCmd(taskId));
}

/**
 * 自定义，通过ID,删除节点执行历史记录
 */
public void deleteHistoricActivityInstanceById(String id){
	commandExecutor.execute(new DeleteHistoricActivityInstanceByIdCmd(id));
}
  
  public NativeHistoricProcessInstanceQuery createNativeHistoricProcessInstanceQuery() {
    return new NativeHistoricProcessInstanceQueryImpl(commandExecutor);
  }

  public NativeHistoricTaskInstanceQuery createNativeHistoricTaskInstanceQuery() {
    return new NativeHistoricTaskInstanceQueryImpl(commandExecutor);
  }

  public NativeHistoricActivityInstanceQuery createNativeHistoricActivityInstanceQuery() {
    return new NativeHistoricActivityInstanceQueryImpl(commandExecutor);
  }
  
  @Override
  public List<HistoricIdentityLink> getHistoricIdentityLinksForProcessInstance(String processInstanceId) {
    return commandExecutor.execute(new GetHistoricIdentityLinksForTaskCmd(null, processInstanceId));
  }
  
  @Override
  public List<HistoricIdentityLink> getHistoricIdentityLinksForTask(String taskId) {
    return commandExecutor.execute(new GetHistoricIdentityLinksForTaskCmd(taskId, null));
  }
  
  @Override
  public ProcessInstanceHistoryLogQuery createProcessInstanceHistoryLogQuery(String processInstanceId) {
  	return new ProcessInstanceHistoryLogQueryImpl(commandExecutor, processInstanceId);
  }

@Override
public void updateHistoricActivityInstance(HistoricActivityInstance historicActivityInstance) {
	// TODO Auto-generated method stub
	  commandExecutor.execute(new UpdateHistoricActivityInstanceCmd(historicActivityInstance));
}

/**
 * 自定义删除，不带关联删除
 */
@Override
public void deleteHistoricTaskInstanceOnly(String taskId) {
	// TODO Auto-generated method stub
	 commandExecutor.execute(new DeleteHistoricTaskInstanceOnlyCmd(taskId));
}


  
}
