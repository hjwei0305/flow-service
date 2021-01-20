package com.ecmp.flow.service;

import com.ecmp.config.util.ApiClient;
import com.ecmp.flow.vo.CompleteTaskVo;
import com.ecmp.flow.vo.SolidifyStartFlowVo;
import com.ecmp.flow.vo.StartFlowVo;
import com.ecmp.notify.api.INotifyService;
import com.ecmp.util.JsonUtils;
import com.ecmp.vo.ResponseData;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultFlowBaseServiceTest extends BaseContextTestCase{


    @Autowired
    private  DefaultFlowBaseService defaultFlowBaseService;


    @Test
    public void solidifyCheckAndSetAndStart(){
        SolidifyStartFlowVo bean  = new SolidifyStartFlowVo();
        bean.setBusinessId("300EA92B-64CA-11EA-B339-0242C0A84421");
        bean.setBusinessModelCode("com.ecmp.flow.entity.DefaultBusinessModel");
        bean.setFlowDefinationId("98307F87-5150-11EA-BBE4-0242C0A84421");
        try{
            ResponseData responseData = defaultFlowBaseService.solidifyCheckAndSetAndStart(bean);
            System.out.println(JsonUtils.toJson(responseData));
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @Test
    public void sart_one() {
        StartFlowVo vo =new StartFlowVo();
        vo.setBusinessKey("81A975D4-5ABC-11EB-801D-0242C0A84620");
        vo.setBusinessModelCode("com.ecmp.flow.entity.DefaultBusinessModel");
        ResponseData res = null;
        try{
            res =  defaultFlowBaseService.startFlow(vo);
        }catch (Exception e){
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
        StartFlowVo vo =new StartFlowVo();
        vo.setBusinessKey("216ECD7C-4A79-11EB-BBC4-0242C0A84620");
        vo.setBusinessModelCode("com.ecmp.flow.entity.DefaultBusinessModel");
        vo.setTypeId("B2FC0C5F-5E87-11EA-AEE3-0242C0A8460D");
        vo.setFlowDefKey("ak00002");
        vo.setOpinion("");
        vo.setTaskList("[{\"nodeId\":\"UserTask_24\",\"userVarName\":\"UserTask_24_List_CounterSign\",\"flowTaskType\":\"countersign\",\"instancyStatus\":false,\"userIds\":\"B54E8964-D14D-11E8-A64B-0242C0A8441B\"}]");
        vo.setAnonymousNodeId("");
        ResponseData res = null;
       try{
           res =  defaultFlowBaseService.startFlow(vo);
       }catch (Exception e){
         System.out.println(e.getMessage());
       }
        System.out.println(JsonUtils.toJson(res));


        try{
            Thread.sleep(1000*20);  //20秒  因为有异步事件需要执行
        }catch (Exception e){

        }
    }




    @Test
    public void complete() {
        CompleteTaskVo completeTaskVo  =new CompleteTaskVo();
        completeTaskVo.setApproved("true");
        completeTaskVo.setBusinessId("04483B02-1F18-11EA-91AC-0242C0A84503");
        completeTaskVo.setEndEventId("false");
        completeTaskVo.setLoadOverTime(null);
        completeTaskVo.setManualSelected(false);
        completeTaskVo.setOpinion("同意");
        completeTaskVo.setTaskId("8A5C5D6E-1FD7-11EA-84D4-0242C0A84516");
        completeTaskVo.setTaskList("[{\"nodeId\":\"UserTask_15\",\"flowTaskType\":\"approve\",\"userVarName\":\"UserTask_15_Approve\",\"callActivityPath\":null,\"instancyStatus\":false,\"solidifyFlow\":true}]");
        ResponseData res=null;
        try{
            res =  defaultFlowBaseService.completeTask(completeTaskVo);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        System.out.println(JsonUtils.toJson(res));
    }






}
