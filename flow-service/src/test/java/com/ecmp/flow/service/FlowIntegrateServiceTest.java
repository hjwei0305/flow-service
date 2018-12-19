package com.ecmp.flow.service;

import com.ecmp.flow.vo.DefaultStartParam;
import com.ecmp.util.JsonUtils;
import com.ecmp.vo.OperateResult;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * <strong>实现功能:</strong>
 * <p></p>
 *
 * @author 王锦光 wangj
 * @version 1.0.1 2018-12-19 10:58
 */
public class FlowIntegrateServiceTest extends BaseContextTestCase{
    @Autowired
    private FlowIntegrateService service;

    @Test
    public void startDefaultFlow() {
        String modelCode = "com.ecmp.fsop.soms.entity.ShareOrder";
        String entityId = "1DF5B8B4-029B-11E9-A05B-080058000005";
        DefaultStartParam startParam = new DefaultStartParam(modelCode, entityId);
        OperateResult result =service.startDefaultFlow(startParam);
        System.out.println(JsonUtils.toJson(result));
        Assert.assertTrue(result.successful());
    }
}