package com.ecmp.flow.service;


import com.ecmp.flow.BasicContextTestCase;
import com.ecmp.flow.entity.FlowDefination;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import org.activiti.engine.repository.Deployment;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class FlowDefinationBaseServiceTest extends BasicContextTestCase {

    @Autowired
    private FlowDefinationService flowDefinationService;

    @Test
    public void deloy(){

        // XML字符串
         String text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:activiti=\"http://activiti.org/bpmn\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:omgdc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:omgdi=\"http://www.omg.org/spec/DD/20100524/DI\" typeLanguage=\"http://www.w3.org/2001/XMLSchema\" expressionLanguage=\"http://www.w3.org/1999/XPath\" targetNamespace=\"http://www.kafeitu.me/activiti-in-action\">"
                + "  <process id=\"candidateUserInUserTask\" name=\"candidateUserInUserTask\">"
                + "    <startEvent id=\"startevent1\" name=\"Start\"></startEvent>"
                + "    <userTask id=\"usertask1\" name=\"用户任务包含多个直接候选人\" activiti:candidateUsers=\"jackchen, henryyan\"></userTask>"
                + "    <sequenceFlow id=\"flow1\" name=\"\" sourceRef=\"startevent1\" targetRef=\"usertask1\"></sequenceFlow>"
                + "    <endEvent id=\"endevent1\" name=\"End\"></endEvent>"
                + "    <sequenceFlow id=\"flow2\" name=\"\" sourceRef=\"usertask1\" targetRef=\"endevent1\"></sequenceFlow>"
                + "  </process>"
                + "</definitions>";
        try {
//            Deployment deploy =  flowDefinationService.deploy("nameTest",text);
            String id="c0a8016e-5bb2-1dbb-815b-b2d3f4680009";
            String resultId = flowDefinationService.deployByVersionId(id);
//            System.out.println(resultId);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void save() {
        FlowDefination flowDefination = new FlowDefination();
        flowDefination.setDefKey("ecmp-flow-flowDefination22_" + System.currentTimeMillis());
        flowDefination.setName("流程类型测试22");
        OperateResultWithData<FlowDefination> result  = flowDefinationService.save(flowDefination);
        flowDefination=result.getData();
        logger.debug("id = {}", flowDefination.getId());
        logger.debug("create结果：{}", flowDefination);
    }

    @Test
    public void update() {
        List<FlowDefination> flowDefinationList = flowDefinationService.findAll();
        if (flowDefinationList != null && flowDefinationList.size() > 0) {
            FlowDefination flowDefination = flowDefinationList.get(0);
            logger.debug("update前：{}", flowDefination);
            flowDefination.setDefKey("ecmp-flow-flowDefination2_" + System.currentTimeMillis());
            flowDefination.setName("流程类型测试2");
            flowDefinationService.save(flowDefination);
            logger.debug("update后：{}", flowDefination);
        } else {
            logger.warn("未能取到数据");
        }
    }

}
