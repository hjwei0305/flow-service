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
        String businessId ="DBA4009B-9DE6-11EC-A2CE-0242C0A84609";
        service.selfMotionExecuteTask(businessId);
    }




}
