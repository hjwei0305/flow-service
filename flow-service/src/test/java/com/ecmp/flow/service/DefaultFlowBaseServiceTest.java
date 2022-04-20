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
        vo.setBusinessKey("2E6FC6D6-B942-11EC-863E-0242C0A84609");
        vo.setBusinessModelCode("com.ecmp.flow.entity.DefaultBusinessModel");
        vo.setFlowTypeCode("AAAAA");
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
        bean.setBusinessId("DAB21775-90A9-11EC-B627-0242C0A84609");
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
        vo.setBusinessKey("A23F2FB3-90AB-11EC-B627-0242C0A84609");
        vo.setBusinessModelCode("com.ecmp.flow.entity.DefaultBusinessModel");
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
        completeTaskVo.setBusinessId("9D30A32B-BFBC-11EC-81DE-0242C0A84609");
        completeTaskVo.setEndEventId("false");
        completeTaskVo.setLoadOverTime(null);
        completeTaskVo.setManualSelected(false);
        completeTaskVo.setOpinion("1111");
        completeTaskVo.setTaskId("AC797337-BFBC-11EC-81DE-0242C0A84609");
        completeTaskVo.setTaskList("[{\"nodeId\":\"EndEvent_4\",\"flowTaskType\":\"common\",\"userIds\":\"7A406B37-823D-11EB-8686-0242C0A8461F,64BA6CFD-2FE8-11EC-A6FB-0242C0A8460C\",\"userVarName\":\"EndEvent_4_end\",\"callActivityPath\":null,\"instancyStatus\":false,\"solidifyFlow\":false}]");
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
        completeTaskVo.setBusinessId("0B925DC9-C054-11EC-81DE-0242C0A84609");
        completeTaskVo.setEndEventId("EndEvent_161");
        completeTaskVo.setLoadOverTime(null);
        completeTaskVo.setManualSelected(true);
        completeTaskVo.setOpinion("1111");
        completeTaskVo.setTaskId("20D68BE5-C054-11EC-81DE-0242C0A84609");
        completeTaskVo.setTaskList("[]");

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
