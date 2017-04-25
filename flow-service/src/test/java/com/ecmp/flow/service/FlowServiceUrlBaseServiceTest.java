package com.ecmp.flow.service;


import com.ecmp.flow.BasicContextTestCase;
import com.ecmp.flow.entity.FlowServiceUrl;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class FlowServiceUrlBaseServiceTest extends BasicContextTestCase {

    @Autowired
    private FlowServiceUrlService flowServiceUrlService;

    @Test
    public void save() {
        FlowServiceUrl flowServiceUrl = new FlowServiceUrl();
        flowServiceUrl.setCode("ecmp-flow-flowServiceUrl22_" + System.currentTimeMillis());
        flowServiceUrl.setName("流程类型测试22");
        OperateResultWithData<FlowServiceUrl> result  = flowServiceUrlService.save(flowServiceUrl);
        flowServiceUrl=result.getData();
        logger.debug("id = {}", flowServiceUrl.getId());
        logger.debug("create结果：{}", flowServiceUrl);
    }

    @Test
    public void update() {
        List<FlowServiceUrl> flowServiceUrlList = flowServiceUrlService.findAll();
        if (flowServiceUrlList != null && flowServiceUrlList.size() > 0) {
            FlowServiceUrl flowServiceUrl = flowServiceUrlList.get(0);
            logger.debug("update前：{}", flowServiceUrl);
            flowServiceUrl.setCode("ecmp-flow-flowServiceUrl2_" + System.currentTimeMillis());
            flowServiceUrl.setName("流程类型测试2");
            flowServiceUrlService.save(flowServiceUrl);
            logger.debug("update后：{}", flowServiceUrl);
        } else {
            logger.warn("未能取到数据");
        }
    }

}
