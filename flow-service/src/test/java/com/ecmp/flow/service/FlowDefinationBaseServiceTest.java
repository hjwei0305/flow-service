package com.ecmp.flow.service;


import com.ecmp.flow.BasicContextTestCase;
import com.ecmp.flow.entity.FlowDefination;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class FlowDefinationBaseServiceTest extends BasicContextTestCase {

    @Autowired
    private FlowDefinationService flowDefinationService;

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
