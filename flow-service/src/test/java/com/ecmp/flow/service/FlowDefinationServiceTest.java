package com.ecmp.flow.service;

import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.util.JsonUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <strong>实现功能:</strong>
 * <p></p>
 *
 * @author 王锦光 wangj
 * @version 1.0.1 2019-05-14 11:02
 */
public class FlowDefinationServiceTest extends BaseContextTestCase{
    @Autowired
    private FlowDefinationService service;

    @Test
    public void getFlowDefVersion(){
        String id = "1B5E5E2F-035A-11E9-A604-0242C0A84402";
        Integer versionCode = -1;
        FlowDefVersion defVersion = service.getFlowDefVersion(id, versionCode, null, null);
        Assert.assertNotNull(defVersion);
        System.out.println(JsonUtils.toJson(defVersion));
    }
}