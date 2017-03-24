package com.ecmp.flow.service;


import com.ecmp.flow.BasicContextTestCase;
import com.ecmp.flow.entity.FlowType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class FlowTypeBaseServiceTest extends BasicContextTestCase {

    @Autowired
    private FlowTypeService flowTypeService;

    @Test
    public void save() {
        FlowType flowType = new FlowType();
        flowType.setCode("ecmp-flow-flowType22_" + System.currentTimeMillis());
        flowType.setName("流程类型测试22");
        flowType = flowTypeService.save(flowType);
        logger.debug("id = {}", flowType.getId());
        logger.debug("create结果：{}", flowType);
    }

    @Test
    public void update() {
        List<FlowType> flowTypeList = flowTypeService.findAll();
        if (flowTypeList != null && flowTypeList.size() > 0) {
            FlowType flowType = flowTypeList.get(0);
            logger.debug("update前：{}", flowType);
            flowType.setCode("ecmp-flow-flowType2_" + System.currentTimeMillis());
            flowType.setName("流程类型测试2");
            flowTypeService.save(flowType);
            logger.debug("update后：{}", flowType);
        } else {
            logger.warn("未能取到数据");
        }
    }

}
