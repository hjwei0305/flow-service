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
        String businessId ="9A8008E3-5AA0-11E9-8D4A-0242C0A8441A";
        ResponseData res = service.getExecuteInfoByBusinessId(businessId);
        Assert.assertNotNull(res);
        System.out.println(JsonUtils.toJson(res));
    }




}
