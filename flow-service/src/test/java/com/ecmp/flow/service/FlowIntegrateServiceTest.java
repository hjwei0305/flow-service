package com.ecmp.flow.service;

import com.ecmp.flow.dto.PortalFlowTask;
import com.ecmp.flow.dto.PortalFlowTaskParam;
import com.ecmp.flow.vo.DefaultStartParam;
import com.ecmp.util.JsonUtils;
import com.ecmp.vo.OperateResult;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

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
        String entityId = "18CDBA22-76B3-11E9-A8D8-0242C0A84410";
        DefaultStartParam startParam = new DefaultStartParam(modelCode, entityId);
        OperateResult result =service.startDefaultFlow(startParam);
        System.out.println(JsonUtils.toJson(result));
        Assert.assertTrue(result.successful());
    }

    @Test
    public void getPortalFlowTask() {
        PortalFlowTaskParam param = new PortalFlowTaskParam();
        param.setModelId("838651D7-325F-11E8-BE29-0242C0A84204");
        param.setRecordCount(10);
        List<PortalFlowTask> tasks = service.getPortalFlowTask(param);
        Assert.assertTrue(CollectionUtils.isNotEmpty(tasks));
        System.out.println(JsonUtils.toJson(tasks));
    }
}