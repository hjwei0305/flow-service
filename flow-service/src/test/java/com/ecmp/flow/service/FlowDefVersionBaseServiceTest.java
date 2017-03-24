package com.ecmp.flow.service;


import com.ecmp.flow.BasicContextTestCase;
import com.ecmp.flow.entity.FlowDefVersion;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class FlowDefVersionBaseServiceTest extends BasicContextTestCase {

    @Autowired
    private FlowDefVersionService flowDefVersionService;

    @Test
    public void save() {
        FlowDefVersion flowDefVersion = new FlowDefVersion();
        flowDefVersion.setDefKey("ecmp-flow-flowDefVersion22_" + System.currentTimeMillis());
        flowDefVersion.setName("流程类型测试22");
        flowDefVersion = flowDefVersionService.save(flowDefVersion);
        logger.debug("id = {}", flowDefVersion.getId());
        logger.debug("create结果：{}", flowDefVersion);
    }

    @Test
    public void update() {
        List<FlowDefVersion> flowDefVersionList = flowDefVersionService.findAll();
        if (flowDefVersionList != null && flowDefVersionList.size() > 0) {
            FlowDefVersion flowDefVersion = flowDefVersionList.get(0);
            logger.debug("update前：{}", flowDefVersion);
            flowDefVersion.setDefKey("ecmp-flow-flowDefVersion2_" + System.currentTimeMillis());
            flowDefVersion.setName("流程类型测试2");
            flowDefVersionService.save(flowDefVersion);
            logger.debug("update后：{}", flowDefVersion);
        } else {
            logger.warn("未能取到数据");
        }
    }

}
