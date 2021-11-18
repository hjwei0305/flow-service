package com.ecmp.flow.service;

import com.ecmp.core.search.SearchOrder;
import com.ecmp.flow.dto.PortalFlowHistory;
import com.ecmp.flow.dto.PortalFlowTask;
import com.ecmp.flow.dto.PortalFlowTaskParam;
import com.ecmp.flow.vo.DefaultStartParam;
import com.ecmp.flow.vo.MyBillVO;
import com.ecmp.util.JsonUtils;
import com.ecmp.vo.OperateResult;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
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
        String modelCode = "com.ecmp.flow.entity.DefaultBusinessModel";
        String entityId = "526F49F9-483E-11EC-8BD7-0242C0A84611";
        DefaultStartParam startParam = new DefaultStartParam(modelCode, entityId);
        OperateResult result =service.startDefaultFlow(startParam);
        System.out.println(JsonUtils.toJson(result));
        Assert.assertTrue(result.successful());
    }

    @Test
    public void getPortalFlowTask() {
        PortalFlowTaskParam param = new PortalFlowTaskParam();
        param.setModelId("6C0B8FB-D993-11E7-BD3F-6C498F234A3D");
        param.setRecordCount(10);
        List<SearchOrder> searchOrders = new ArrayList<>();
        SearchOrder s = new SearchOrder("createdDate", SearchOrder.Direction.ASC);
        searchOrders.add(s);
        param.setSearchOrders(searchOrders);
        List<PortalFlowTask> tasks = service.getPortalFlowTask(param);
        Assert.assertTrue(CollectionUtils.isNotEmpty(tasks));
        System.out.println(JsonUtils.toJson(tasks));
    }

    @Test
    public void getPortalFlowHistory() {
        int recordCount = 10;
        List<PortalFlowHistory> histories = service.getPortalFlowHistory(recordCount);
        Assert.assertTrue(CollectionUtils.isNotEmpty(histories));
        System.out.println(JsonUtils.toJson(histories));
    }

    @Test
    public void getPortalMyBill() {
        int recordCount = 10;
        List<MyBillVO> myBillS = service.getPortalMyBill(recordCount);
        Assert.assertTrue(CollectionUtils.isNotEmpty(myBillS));
        System.out.println(JsonUtils.toJson(myBillS));
    }
}