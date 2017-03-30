package com.ecmp.flow.service;

import com.ecmp.flow.BasicContextTestCase;
import com.ecmp.flow.entity.FlowInstance;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by xxxlimit on 2017/3/29.
 */
public class FlowInstanceBaseServiceTest extends BasicContextTestCase {

    @Autowired
    private FlowInstanceService flowInstanceService;

    @Test
    public void save(){
//        AppModule appModule = new AppModule();
//        appModule.setCode("ecmp-flow-appModule22_" + System.currentTimeMillis());
//        appModule.setName("应用模块测试22");
//        appModule = appModuleService.save(appModule);
//        logger.debug("id = {}", appModule.getId());
//        logger.debug("create结果：{}", appModule);
        FlowInstance flowInstance = new FlowInstance();
        flowInstance.setFlowName("流程名称2");
        flowInstance.setBusinessId("业务id2");
        flowInstance.setStartDate(new Timestamp(System.currentTimeMillis()));
        flowInstance.setEndDate(new Timestamp(System.currentTimeMillis()+1));
        flowInstanceService.save(flowInstance);
    }

    @Test
    public void findAll(){
        List<FlowInstance> flowInstanceList = flowInstanceService.findAll();
        System.out.print(flowInstanceList);
    }

    @Test
    public void delete(){
        List<FlowInstance> flowInstanceList = flowInstanceService.findAll();
        if(flowInstanceList !=null && flowInstanceList.size()>0){
            FlowInstance flowInstance = flowInstanceList.get(0);
            flowInstanceService.delete(flowInstance);
        }
    }

}