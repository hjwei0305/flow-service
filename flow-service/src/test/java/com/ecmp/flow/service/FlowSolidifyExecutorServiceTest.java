package com.ecmp.flow.service;

import com.ecmp.util.JsonUtils;
import com.ecmp.vo.ResponseData;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;



public class FlowSolidifyExecutorServiceTest extends BaseContextTestCase{

    @Autowired
    private FlowSolidifyExecutorService service;


    @Test
    public void getExecuteInfoByBusinessId() {
        String businessId ="5AE9E1AB-B6DC-11EC-9AFE-0242C0A84609";
        service.selfMotionExecuteTask(businessId);
    }




}
