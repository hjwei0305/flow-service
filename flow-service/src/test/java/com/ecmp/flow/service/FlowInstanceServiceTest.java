package com.ecmp.flow.service;

import com.ecmp.core.search.Search;
import com.ecmp.flow.dto.UserFlowBillsQueryParam;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.flow.vo.FlowNodeVO;
import com.ecmp.flow.vo.JumpTaskVo;
import com.ecmp.flow.vo.SignalPoolTaskVO;
import com.ecmp.flow.vo.TargetNodeInfoVo;
import com.ecmp.util.JsonUtils;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.ResponseData;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * <strong>实现功能:</strong>
 * <p></p>
 *
 * @author 王锦光 wangj
 * @version 1.0.1 2019-05-30 17:51
 */
public class FlowInstanceServiceTest extends BaseContextTestCase {
    @Autowired
    private FlowInstanceService service;


    @Test
    public void getAllMyBills() {
        UserFlowBillsQueryParam userFlowBillsQueryParam = new UserFlowBillsQueryParam();
        service.getAllMyBills(userFlowBillsQueryParam);
    }

    @Test
    public void getCanToHistoryNodeInfos() {
        ResponseData responseData = service.getCanToHistoryNodeInfos("3417325C-BC6B-11EB-87A2-0242C0A8462A");
        System.out.println(JsonUtils.toJson(responseData));
    }


    @Test
    public void endCommon() {
        service.endCommon("D4D4FE95-A884-11EB-AD06-0242C0A8462A", false);
    }


    @Test
    public void jumpToTargetNode() {
        JumpTaskVo jumpTaskVo = new JumpTaskVo();
        jumpTaskVo.setInstanceId("CEDA0E98-48DA-11EC-A62D-0242C0A84611");
        jumpTaskVo.setTargetNodeId("UserTask_78");
        jumpTaskVo.setCurrentNodeAfterEvent(true);
        jumpTaskVo.setTargetNodeBeforeEvent(true);
        jumpTaskVo.setJumpDepict("测试跳转");
        jumpTaskVo.setTaskList("[{\"nodeId\":\"UserTask_78\",\"flowTaskType\":\"common\",\"userIds\":\"B54E8964-D14D-11E8-A64B-0242C0A8441B\",\"userVarName\":\"UserTask_78_Normal\",\"callActivityPath\":null,\"instancyStatus\":false,\"solidifyFlow\":false,\"allowJumpBack\":false}]");
        ResponseData responseData = service.jumpToTargetNode(jumpTaskVo);
        System.out.println(JsonUtils.toJson(responseData));
    }

    @Test
    public void getTargetNodeInfo() {
        ResponseData<TargetNodeInfoVo> targetNodeInfoVo = service.getTargetNodeInfo("25E78D95-8636-11EB-8A4B-0242C0A84413", "PoolTask_205");
        System.out.println(JsonUtils.toJson(targetNodeInfoVo));
    }

    @Test
    public void checkAndGetCanJumpNodeInfos() {
        ResponseData<List<FlowNodeVO>> FlowNodeVOList = service.checkAndGetCanJumpNodeInfos("25E78D95-8636-11EB-8A4B-0242C0A84413");
        System.out.println(JsonUtils.toJson(FlowNodeVOList));
    }

    @Test
    public void signalByBusinessId() {
        Map<String, Object> variables = new HashMap<>();
        OperateResult operateResult = service.signalByBusinessId("5ADAB785-5EEF-11EB-A837-0242C0A84413", "ReceiveTask_179", variables);
        System.out.println(JsonUtils.toJson(operateResult));
    }

    @Test
    public void taskFailTheCompensation() {
        String instanceId = "A9BCDB5C-A97B-11EB-A5F0-0242C0A8462A";
        service.taskFailTheCompensation(instanceId);
    }


    @Test
    public void findTaskByBusinessIdAndActTaskKey() {
        String id = "BC808F8B-81BB-11E9-9D74-0242C0A84410";
        String flowTaskId = "PoolTask_6";
        FlowTask task = service.findTaskByBusinessIdAndActTaskKey(id, flowTaskId);
        Assert.assertNotNull(task);
        System.out.println(JsonUtils.toJson(task));
    }

    /**
     * 工作池任务确定执行人
     */
    @Test
    public void signalPoolTaskByBusinessId() {
        String businessId = "D2203EB8-A884-11EB-AD06-0242C0A8462A";
        String poolTaskActDefId = "PoolTask_70";
        String userId = "B54E8964-D14D-11E8-A64B-0242C0A8441B";
        Map<String, Object> v = new HashMap<>();
        OperateResult operateResult = service.signalPoolTaskByBusinessId(businessId, poolTaskActDefId, userId, v);
        System.out.println(JsonUtils.toJson(operateResult));
    }

    /**
     * 工作池任务确定多执行人
     */
    @Test
    public void signalPoolTaskByBusinessIdAndUserList() {
        SignalPoolTaskVO signalPoolTaskVO = new SignalPoolTaskVO();
        signalPoolTaskVO.setBusinessId("D2203EB8-A884-11EB-AD06-0242C0A8462A");
        signalPoolTaskVO.setPoolTaskActDefId("PoolTask_70");
        List<String> list = new ArrayList<>();
        list.add("02620F45-5EAF-11EA-A2E3-0242C0A84605");
        list.add("73819619-6FD9-11EA-9C1B-0242C0A84603");
        Map<String, Object> v = new HashMap<>();
        signalPoolTaskVO.setUserIds(list);
        signalPoolTaskVO.setMap(v);
        ResponseData responseData = service.signalPoolTaskByBusinessIdAndUserList(signalPoolTaskVO);
        System.out.println(JsonUtils.toJson(responseData));
    }
}