package com.ecmp.flow.service;


import com.ecmp.config.util.ApiClient;
import com.ecmp.flow.BasicContextTestCase;
import com.ecmp.flow.api.IFlowHiVarinstService;
import com.ecmp.flow.entity.FlowHiVarinst;
import com.ecmp.vo.OperateResultWithData;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class FlowHiVarinstBaseServiceTest extends BasicContextTestCase {

    @Autowired
    private FlowHiVarinstService flowHiVarinstService;

    @Test
    public void save() {
        FlowHiVarinst flowHiVarinst = new FlowHiVarinst();
        flowHiVarinst.setName("field1");
        flowHiVarinst.setType("Integer");
        OperateResultWithData<FlowHiVarinst> result  = flowHiVarinstService.save(flowHiVarinst);
        flowHiVarinst=result.getData();
        logger.debug("id = {}", flowHiVarinst.getId());
        logger.debug("create结果：{}", flowHiVarinst);
    }

    @Test
    public void update() {
        List<FlowHiVarinst> flowHiVarinstList = flowHiVarinstService.findAll();
        if (flowHiVarinstList != null && flowHiVarinstList.size() > 0) {
            FlowHiVarinst flowHiVarinst = flowHiVarinstList.get(0);
            logger.debug("update前：{}", flowHiVarinst);
            flowHiVarinst.setName("应用模块测试2");
            flowHiVarinstService.save(flowHiVarinst);
            logger.debug("update后：{}", flowHiVarinst);
        } else {
            logger.warn("未能取到数据");
        }
    }
    @Test
    public void apiTest(){
        String baseAddress = "http://localhost:8080/flow";

        List<Object> providerList = new ArrayList<Object>();
        providerList.add(new JacksonJsonProvider());

        IFlowHiVarinstService flowHiVarinstService = JAXRSClientFactory.create(baseAddress, IFlowHiVarinstService.class, providerList);
        List<FlowHiVarinst> result  = flowHiVarinstService.findAll();
        System.out.println(result.size());

        IFlowHiVarinstService proxy = ApiClient.createProxy(IFlowHiVarinstService.class);
        result  = proxy.findAll();
        System.out.println(result.size());
    }

}
