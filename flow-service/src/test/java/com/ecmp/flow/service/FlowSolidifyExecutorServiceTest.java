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
        String businessId ="04483B02-1F18-11EA-91AC-0242C0A84503";
        service.selfMotionExecuteTask(businessId);
    }




}
