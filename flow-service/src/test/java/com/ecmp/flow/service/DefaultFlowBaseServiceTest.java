package com.ecmp.flow.service;

import com.ecmp.flow.vo.CompleteTaskVo;
import com.ecmp.flow.vo.SolidifyStartFlowVo;
import com.ecmp.flow.vo.StartFlowBusinessAndTypeVo;
import com.ecmp.flow.vo.StartFlowVo;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.util.JsonUtils;
import com.ecmp.vo.ResponseData;
import net.sf.json.JSONObject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultFlowBaseServiceTest extends BaseContextTestCase {


    @Autowired
    private DefaultFlowBaseService defaultFlowBaseService;

    @Test
    public void startFlowByBusinessAndType() {
        StartFlowBusinessAndTypeVo vo = new StartFlowBusinessAndTypeVo();
        vo.setBusinessKey("526F49F9-483E-11EC-8BD7-0242C0A84611");
        vo.setBusinessModelCode("com.ecmp.flow.entity.DefaultBusinessModel");
        vo.setFlowTypeCode("FLOW_TEST");
        try {
            ResponseData responseData = defaultFlowBaseService.startFlowByBusinessAndType(vo);
            System.out.println(JsonUtils.toJson(responseData));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    @Test
    public void getSelectedNodesInfo() {
        try {
            ResponseData responseData = defaultFlowBaseService.getSelectedNodesInfo("D44004B5-8FC4-11EC-B0E7-0242C0A84609",
                    "true",
                    null,
                    false);
            System.out.println(JsonUtils.toJson(responseData));
        } catch (Exception e) {
        }
    }


    @Test
    public void getWhetherLastByBusinessId() {
        ResponseData responseData = defaultFlowBaseService.getWhetherLastByBusinessId("D2203EB8-A884-11EB-AD06-0242C0A8462A");
        System.out.println(JsonUtils.toJson(responseData));
    }


    @Test
    public void solidifyCheckAndSetAndStart() {
        SolidifyStartFlowVo bean = new SolidifyStartFlowVo();
        bean.setBusinessId("CDEBF14B-BC66-11EB-B633-0242C0A8462A");
        bean.setBusinessModelCode("com.ecmp.flow.entity.DefaultBusinessModel");
        bean.setFlowDefinationId("31719D16-5EA7-11EA-B07D-0242C0A8460D");
        try {
            ResponseData responseData = defaultFlowBaseService.solidifyCheckAndSetAndStart(bean);
            System.out.println(JsonUtils.toJson(responseData));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void sart_one() {
        StartFlowVo vo = new StartFlowVo();
        vo.setBusinessKey("5EFC6266-88B1-11EC-9A63-0242C0A84627");
        vo.setBusinessModelCode("com.rcsit.prc.entity.BudgetApply");
        ResponseData res = null;
        try {
            res = defaultFlowBaseService.startFlow(vo);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println(JsonUtils.toJson(res));

//        try{
//            Thread.sleep(1000*20);  //20秒  因为有异步事件需要执行
//        }catch (Exception e){
//
//        }
    }


    @Test
    public void sart_three() {
        StartFlowVo vo = new StartFlowVo();
        vo.setBusinessKey("CDEBF14B-BC66-11EB-B633-0242C0A8462A");
        vo.setBusinessModelCode("com.ecmp.flow.entity.DefaultBusinessModel");
        vo.setTypeId("B2FC0C5F-5E87-11EA-AEE3-0242C0A8460D");
        vo.setFlowDefKey("ak00002");
        vo.setOpinion("");
        vo.setTaskList("[{\"nodeId\":\"UserTask_75\",\"userVarName\":\"UserTask_75_Approve\",\"flowTaskType\":\"approve\",\"instancyStatus\":false,\"solidifyFlow\":false,\"userIds\":\"B54E8964-D14D-11E8-A64B-0242C0A8441B\"}]");
        vo.setAnonymousNodeId("");
        ResponseData res = null;
        try {
            res = defaultFlowBaseService.startFlow(vo);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println(JsonUtils.toJson(res));


        try {
            Thread.sleep(1000 * 100);  //20秒  因为有异步事件需要执行
        } catch (Exception e) {

        }
    }


    @Test
    public void complete111() {
        CompleteTaskVo completeTaskVo = new CompleteTaskVo();
        completeTaskVo.setApproved("true");
        completeTaskVo.setBusinessId("BD240A0B-5684-11EC-B29C-0242C0A84611");
        completeTaskVo.setEndEventId("false");
        completeTaskVo.setLoadOverTime(null);
        completeTaskVo.setManualSelected(true);
        completeTaskVo.setOpinion("1111");
        completeTaskVo.setTaskId("7314B95F-5E82-11EC-8E73-0242C0A84611");
        completeTaskVo.setTaskList("[{\"nodeId\":\"ServiceTask_102\",\"flowTaskType\":\"serviceTask\",\"userIds\":\"B54E8964-D14D-11E8-A64B-0242C0A8441B\",\"userVarName\":\"ServiceTask_102_ServiceTask\",\"callActivityPath\":null,\"instancyStatus\":false,\"solidifyFlow\":false}]");
        ResponseData res = null;
        try {
            res = defaultFlowBaseService.completeTask(completeTaskVo);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try {
            Thread.sleep(1000 * 60 * 5);  //20秒  因为有异步事件需要执行
        } catch (Exception e) {
        }
        System.out.println(JsonUtils.toJson(res));
    }

    @Test
    public void complete() {
        CompleteTaskVo completeTaskVo = new CompleteTaskVo();
        completeTaskVo.setApproved("true");
        completeTaskVo.setBusinessId("BD9ACB90-7839-11EC-BB08-0242C0A84609");
        completeTaskVo.setEndEventId("false");
        completeTaskVo.setLoadOverTime(null);
        completeTaskVo.setManualSelected(false);
        completeTaskVo.setOpinion("1111");
        completeTaskVo.setTaskId("67A46D76-783D-11EC-BB08-0242C0A84609");
        completeTaskVo.setTaskList("[{\"nodeId\":\"UserTask_118\",\"flowTaskType\":\"common\",\"userIds\":\"B54E8964-D14D-11E8-A64B-0242C0A8441B\",\"userVarName\":\"UserTask_118_Normal\",\"callActivityPath\":null,\"instancyStatus\":false,\"solidifyFlow\":false}]");
        ResponseData res = null;
        try {
            res = defaultFlowBaseService.completeTask(completeTaskVo);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try {
            Thread.sleep(1000 * 60 * 5);  //20秒  因为有异步事件需要执行
        } catch (Exception e) {
        }
        System.out.println(JsonUtils.toJson(res));
    }


}
