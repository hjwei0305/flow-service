package com.ecmp.flow.service;

import com.ecmp.flow.vo.CompleteTaskVo;
import com.ecmp.flow.vo.StartFlowVo;
import com.ecmp.util.JsonUtils;
import com.ecmp.vo.ResponseData;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultFlowBaseServiceTest extends BaseContextTestCase{


    @Autowired
    private  DefaultFlowBaseService defaultFlowBaseService;

    @Test
    public void sart_one() {
        StartFlowVo vo =new StartFlowVo();
        vo.setBusinessKey("2BEA9832-12F6-11E9-B111-0242C0A8440B");
        vo.setBusinessModelCode("com.ecmp.flow.entity.DefaultBusinessModel");
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
    public void sart_three() {
        StartFlowVo vo =new StartFlowVo();
        vo.setBusinessKey("2BEA9832-12F6-11E9-B111-0242C0A8440B");
        vo.setBusinessModelCode("com.ecmp.flow.entity.DefaultBusinessModel");
        vo.setTypeId("C35B0B09-3640-11E7-9617-3C970EA9E0F7");
        vo.setFlowDefKey("test2222");
        vo.setOpinion("");
        vo.setTaskList("[{\"nodeId\":\"UserTask_12\",\"userVarName\":\"UserTask_12_List_CounterSign\",\"flowTaskType\":\"countersign\",\"instancyStatus\":false,\"userIds\":\"1592D012-A330-11E7-A967-02420B99179E\"}]");
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
        completeTaskVo.setApproved(null);
        completeTaskVo.setBusinessId("65D5D42E-0074-11EA-9F73-0242C0A8440A");
        completeTaskVo.setEndEventId("false");
        completeTaskVo.setLoadOverTime(null);
        completeTaskVo.setManualSelected(true);
        completeTaskVo.setOpinion("已扫描");
        completeTaskVo.setTaskId("69A8ADF1-0074-11EA-AC4A-0242C0A8450D");
        completeTaskVo.setTaskList("[{\"nodeId\":\"ServiceTask_28\",\"flowTaskType\":\"serviceTask\",\"userIds\":\"161bb98cc55aedf1ba41e4248c6b98d5\",\"userVarName\":\"ServiceTask_28_ServiceTask\",\"callActivityPath\":null,\"instancyStatus\":false,\"solidifyFlow\":false}]");
        ResponseData res=null;
        try{
            res =  defaultFlowBaseService.completeTask(completeTaskVo);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        System.out.println(JsonUtils.toJson(res));
    }






}
