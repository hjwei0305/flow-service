package com.ecmp.flow.service;

import com.ecmp.config.util.ApiJsonUtils;
import com.ecmp.flow.entity.DisagreeReason;
import com.ecmp.vo.OperateResultWithData;
import com.ecmp.vo.ResponseData;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


public class DisagreeReasonServiceTest extends BaseContextTestCase{

    @Autowired
    private DisagreeReasonService disagreeReasonService;


    @Test
    public void save() {
        DisagreeReason  bean = new DisagreeReason();
        bean.setCode("1");
        bean.setName("1");
        bean.setFlowTypeId("1");
        bean.setFlowTypeName("1");
        OperateResultWithData<DisagreeReason> res = disagreeReasonService.save(bean);
        Assert.assertNotNull(res);
        System.out.println(ApiJsonUtils.toJson(res));
    }

    @Test
    public void getDisagreeReasonByTypeId(){
        ResponseData res = disagreeReasonService.getDisagreeReasonByTypeId("commonReason");
        Assert.assertNotNull(res);
        System.out.println(ApiJsonUtils.toJson(res));
    }


}
