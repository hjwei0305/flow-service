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

package com.ecmp.flow.service;

import com.ecmp.flow.ActivitiContextTestCase;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.junit.Test;


/**
 * 用文本方式部署,测试并行网关
 *
 * @author tj
 */
public class StringDeploymentByContextTestP1 extends ActivitiContextTestCase {

    // XML字符串
    private String text = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" expressionLanguage=\"http://www.w3.org/1999/XPath\" typeLanguage=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"bpmn\" xmlns:omgdi=\"http://www.omg.org/spec/DD/20100524/DI\" xmlns:activiti=\"http://activiti.org/bpmn\" xmlns:omgdc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
            "    <process name=\"testTJ01\" isExecutable=\"true\" id=\"testTJ01\">\n" +
            "        <startEvent activiti:initiator=\"startUserId\" name=\"开始\" id=\"StartEvent_0\"/>\n" +
            "        <endEvent name=\"结束\" id=\"EndEvent_5\"/>\n" +
            "        <userTask activiti:assignee=\"${UserTask_1_Normal}\" name=\"普通任务testTJ01\" id=\"UserTask_1\"/>\n" +
            "        <userTask name=\"会签任务testTJ01\" id=\"UserTask_3\">\n" +

            "            <extensionElements>\n" +
            "                <activiti:taskListener event=\"complete\" delegateExpression=\"${commonCounterSignCompleteListener}\"/>\n" +
            "            </extensionElements>\n" +
            "            <multiInstanceLoopCharacteristics isSequential=\"false\" activiti:collection=\"${UserTask_3_List_CounterSign}\" activiti:elementVariable=\"${UserTask_3_CounterSign}\"/>\n" +
            "        </userTask>\n" +
            "        <userTask activiti:assignee=\"${UserTask_4_Normal}\" name=\"快结束的任务\" id=\"UserTask_4\"/>\n" +
            "        <sequenceFlow sourceRef=\"StartEvent_0\" targetRef=\"UserTask_1\" id=\"flow1\"/>\n" +
            "        <sequenceFlow sourceRef=\"UserTask_1\" targetRef=\"UserTask_3\" id=\"flow2\"/>\n" +
            "        <sequenceFlow sourceRef=\"UserTask_3\" targetRef=\"UserTask_4\" id=\"flow3\"/>\n" +
            "        <sequenceFlow sourceRef=\"UserTask_4\" targetRef=\"EndEvent_5\" id=\"flow4\"/>\n" +
            "    </process>\n" +
            "</definitions>\n";

    @Test
    public void testCharsDeployment() {
        // 以candidateUserInUserTask.bpmn为资源名称，以text的内容为资源内容部署到引擎
        repositoryService.createDeployment().addString("test.bpmn", text).deploy();

        // 验证流程定义是否部署成功
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        long count = processDefinitionQuery.processDefinitionKey("candidateUserInUserTask").count();
        assert(count>=1);


    }

    @Test
    public void testS(){
        String old = "1001|1003|1993|322";
        String[] result = old.split("\\|");
        System.out.println(result.length);
    }

}
