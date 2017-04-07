package com.ecmp.flow.service;

import com.ecmp.flow.BasicContextTestCase;
import com.ecmp.flow.entity.FlowTask;
import com.sun.org.apache.bcel.internal.generic.NEW;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import static jdk.nashorn.internal.runtime.regexp.joni.Syntax.Java;
import static org.junit.Assert.*;

/**
 * Created by xxxlimit on 2017/3/28.
 */
public class FlowTaskServiceBaseTest extends BasicContextTestCase {

    @Autowired
    private FlowTaskService flowTaskService;

    @Test
    public void save() {
//        AppModule appModule = new AppModule();
//        appModule.setCode("ecmp-flow-appModule22_" + System.currentTimeMillis());
//        appModule.setName("应用模块测试22");
//        appModule = appModuleService.save(appModule);
//        logger.debug("id = {}", appModule.getId());
//        logger.debug("create结果：{}", appModule);
        FlowTask flowTask  = new FlowTask();
        flowTask.setFlowName("工作流名称");
        flowTask.setTaskName("任务名称");
        flowTask.setExecutorName("执行人名称");
        flowTask.setFlowDefinitionId("流程定义版本1");
        flowTask.setFlowInstanceId("流程实例1");
        flowTask.setTaskDefKey("任务定义key");
        flowTask.setTaskFormUrl("任务表单url");
        flowTask.setTaskStatus("任务状态");
        flowTask.setProxyStatus("代理状态");
        flowTask.setExecutorName("执行人");
        flowTask.setExecutorAccount(11111111);
        flowTask.setCandidateAccount(22222222);
        flowTask.setDepict("描述");
        flowTask.setExecuteDate(new Date(System.currentTimeMillis()));
        flowTaskService.save(flowTask);
    }

    @Test
    public void update() {
//        List<AppModule> appModuleList = appModuleService.findAll();
//        if (appModuleList != null && appModuleList.size() > 0) {
//            AppModule appModule = appModuleList.get(0);
//            logger.debug("update前：{}", appModule);
//            appModule.setCode("ecmp-flow-appModule2_" + System.currentTimeMillis());
//            appModule.setName("应用模块测试2");
//            appModuleService.save(appModule);
//            logger.debug("update后：{}", appModule);
//        } else {
//            logger.warn("未能取到数据");
//        }
        List<FlowTask> flowTaskList = flowTaskService.findAll();
        if(flowTaskList !=null && flowTaskList.size()>0){
            FlowTask flowTask = flowTaskList.get(0);
            flowTask.setTaskDefKey("任务定义key2");
            flowTask.setTaskName("任务名称2");
            flowTask.setFlowName("工作流名称2");
            flowTaskService.save(flowTask);
        }
        System.out.print(flowTaskList);
    }

    @Test
    public  void delete(){
        List<FlowTask> flowTaskList = flowTaskService.findAll();
        if(flowTaskList !=null && flowTaskList.size()>0){
            FlowTask flowTask = flowTaskList.get(0);
            flowTaskService.delete(flowTask);
        }
    }

}