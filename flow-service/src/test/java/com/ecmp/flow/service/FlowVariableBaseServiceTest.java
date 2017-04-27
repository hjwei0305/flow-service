package com.ecmp.flow.service;


import com.ecmp.config.util.ApiClient;
import com.ecmp.flow.BasicContextTestCase;
import com.ecmp.flow.api.IFlowVariableService;
import com.ecmp.flow.entity.FlowVariable;
import com.ecmp.vo.OperateResultWithData;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class FlowVariableBaseServiceTest extends BasicContextTestCase {

    @Autowired
    private FlowVariableService flowVariableService;

    @Test
    public void save() {
        FlowVariable flowVariable = new FlowVariable();
        flowVariable.setName("field1");
        flowVariable.setType("Integer");
        OperateResultWithData<FlowVariable> result  = flowVariableService.save(flowVariable);
        flowVariable=result.getData();
        logger.debug("id = {}", flowVariable.getId());
        logger.debug("create结果：{}", flowVariable);
    }

    @Test
    public void update() {
        List<FlowVariable> flowVariableList = flowVariableService.findAll();
        if (flowVariableList != null && flowVariableList.size() > 0) {
            FlowVariable flowVariable = flowVariableList.get(0);
            logger.debug("update前：{}", flowVariable);
            flowVariable.setName("应用模块测试2");
            flowVariableService.save(flowVariable);
            logger.debug("update后：{}", flowVariable);
        } else {
            logger.warn("未能取到数据");
        }
    }
    @Test
    public void apiTest(){
        String baseAddress = "http://localhost:8080/flow";

        List<Object> providerList = new ArrayList<Object>();
        providerList.add(new JacksonJsonProvider());

        IFlowVariableService flowVariableService = JAXRSClientFactory.create(baseAddress, IFlowVariableService.class, providerList);
        List<FlowVariable> result  = flowVariableService.findAll();
        System.out.println(result.size());

        IFlowVariableService proxy = ApiClient.createProxy(IFlowVariableService.class);
        result  = proxy.findAll();
        System.out.println(result.size());
    }

}
