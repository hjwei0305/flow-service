package com.ecmp.flow.service;

import com.ecmp.config.util.ApiJsonUtils;
import com.ecmp.flow.entity.AppModule;
import com.ecmp.vo.ResponseData;
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
 * @version 1.0.1 2018-09-10 14:49
 */
public class AppModuleServiceTest extends BaseContextTestCase{
    @Autowired
    private AppModuleService service;
    @Autowired
    private DefaultFlowBaseService defaultFlowBaseService;

    @Test
    public void findAll() {
        List<AppModule> appModules = service.findAll();
        Assert.assertNotNull(appModules);
        System.out.println(ApiJsonUtils.toJson(appModules));
    }

    @Test
    public void startFlow() {
        String businessModelCode="com.ecmp.srm.pp.entity.PurchaseOrder";
        String businessKey="CAF50148-D961-11E9-8337-0242C0A8450A";
        String opinion ="";
        String typeId ="8F8F1030-4C6B-11E9-9485-0242C0A8450A";
        String flowDefKey="purchaseOrderCOPY";
        String taskList ="[{\"nodeId\":\"ServiceTask_19\",\"userVarName\":\"ServiceTask_19_ServiceTask\",\"option\":\"待审批\",\"flowTaskType\":\"serviceTask\",\"instancyStatus\":null,\"userIds\":\"7d345ed4-4443-4b71-aa00-29e23c191a9e\"}]";
        String anonymousNodeId =null;

        ResponseData responseData=null;
        try{
             responseData = defaultFlowBaseService.startFlow(businessModelCode,
                    businessKey,opinion,typeId,flowDefKey,taskList,anonymousNodeId);
        }catch (Exception e){
            e.printStackTrace();
        }
        Assert.assertNotNull(responseData);
        System.out.println(ApiJsonUtils.toJson(responseData));
    }

}