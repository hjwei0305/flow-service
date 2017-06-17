package com.ecmp.flow.controller.maindata;

import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.core.json.JsonUtil;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.IFlowTaskService;
import com.ecmp.flow.vo.ApprovalHeaderVO;
import com.ecmp.flow.vo.NodeInfo;
import com.ecmp.vo.OperateResult;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：提供给客户端通用的调用web controller
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/6/17 10:39      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
@Controller
@RequestMapping(value = "/flowClient")
public class FlowClientController {
    /**
     * 签收任务
     * @param taskId  任务id
     * @param userId  用户id
     * @return
     */
    @RequestMapping(value = "listFlowTask")
    @ResponseBody
    public String claimTask(String taskId, String userId){
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        OperateResult result =  proxy.claim(taskId,userId);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage());
        return JsonUtil.serialize(operateStatus);
    }
    /**
     * 回退（撤销）任务
     *
     * @param preTaskId 上一个任务ID
     * @param opinion   意见
     * @return 操作结果
     */
    @RequestMapping(value = "cancelTask")
    @ResponseBody
    public String rollBackTo(String preTaskId, String opinion) {
        OperateStatus operateStatus = null;
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        OperateResult result = proxy.rollBackTo(preTaskId);
        operateStatus = new OperateStatus(true, result.getMessage());
        return JsonUtil.serialize(operateStatus);
    }

    /**
     * 任务驳回
     *
     * @param taskId  任务ID
     * @param opinion 意见
     * @return 操作结果
     */
    @RequestMapping(value = "rejectTask")
    @ResponseBody
    public String rejectTask(String taskId, String opinion) {
        OperateStatus operateStatus = null;
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        OperateResult result = proxy.taskReject(taskId, opinion, null);
        operateStatus = new OperateStatus(true, result.getMessage());
        return JsonUtil.serialize(operateStatus);
    }


    /**
     * 获取当前审批任务的决策信息
     *
     * @param taskId
     * @return 操作结果
     */
    @RequestMapping(value = "nextNodesInfo")
    @ResponseBody
    public String nextNodesInfo(String taskId) throws NoSuchMethodException {
        OperateStatus operateStatus = null;
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        List<NodeInfo> nodeInfoList = proxy.findNextNodes(taskId);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            operateStatus = new OperateStatus(true, "成功");
            operateStatus.setData(nodeInfoList);
        } else {
            operateStatus = new OperateStatus(false, "不存在");
        }
        return JsonUtil.serialize(operateStatus);
    }

    /**
     * 获取下一步的节点信息任务
     *
     * @param taskId
     * @return 操作结果
     */
    @RequestMapping(value = "getSelectedNodesInfo")
    @ResponseBody
    public String getSelectedNodesInfo(String taskId, String includeNodeIdsStr) throws NoSuchMethodException {
        OperateStatus operateStatus = null;
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        List<String> includeNodeIds = null;
        if (!StringUtils.isEmpty(includeNodeIdsStr)) {
            String[] includeNodeIdsStringArray = includeNodeIdsStr.split(",");
            includeNodeIds = java.util.Arrays.asList(includeNodeIdsStringArray);
        } else {
            throw new RuntimeException("至少要传入一个节点ID！");
        }
        List<NodeInfo> nodeInfoList = proxy.findNexNodesWithUserSet(taskId, includeNodeIds);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            operateStatus = new OperateStatus(true, "成功");
            operateStatus.setData(nodeInfoList);
        } else {
            operateStatus = new OperateStatus(false, "不存在");
        }
        return JsonUtil.serialize(operateStatus);
    }

    /**
     * 获取下一步的节点信息任务(带用户信息)
     *
     * @param taskId
     * @return 操作结果
     */
    @RequestMapping(value = "nextNodesInfoWithUser")
    @ResponseBody
    public String nextNodesInfoWithUser(String taskId) throws NoSuchMethodException {
        OperateStatus operateStatus = null;
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        List<NodeInfo> nodeInfoList = proxy.findNexNodesWithUserSet(taskId);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            operateStatus = new OperateStatus(true, "成功");
            operateStatus.setData(nodeInfoList);
        } else {
            operateStatus = new OperateStatus(false, "不存在");
        }
        return JsonUtil.serialize(operateStatus);
    }

    /**
     * 获取任务抬头信息信息任务
     *
     * @param taskId
     * @return 操作结果
     */
    @RequestMapping(value = "getApprovalHeaderInfo")
    @ResponseBody
    public String getApprovalHeaderInfo(String taskId) {
        OperateStatus operateStatus = null;
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        ApprovalHeaderVO approvalHeaderVO = proxy.getApprovalHeaderVO(taskId);
        if (approvalHeaderVO != null) {
            operateStatus = new OperateStatus(true, "成功");
            approvalHeaderVO.setFlowBaseUrl(ContextUtil.getAppModule().getWebBaseAddress());
            operateStatus.setData(approvalHeaderVO);
        } else {
            operateStatus = new OperateStatus(false, "不存在");
        }
        return JsonUtil.serialize(operateStatus);
    }
}
