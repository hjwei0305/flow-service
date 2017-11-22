package com.ecmp.flow.controller.maindata;

import com.ecmp.annotation.IgnoreCheckAuth;
import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.IFlowDefinationService;
import com.ecmp.flow.api.IFlowTaskService;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.entity.DefaultBusinessModel;
import com.ecmp.flow.entity.DefaultBusinessModel2;
import com.ecmp.flow.entity.DefaultBusinessModel3;
import com.ecmp.flow.entity.FlowDefination;
import com.ecmp.flow.vo.*;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
@IgnoreCheckAuth
public class FlowClientController {
    /**
     * 签收任务
     * @param taskId  任务id
     * @return
     */
    @RequestMapping(value = "claimTask")
    @ResponseBody
    public OperateStatus claimTask(String taskId){
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        String userId = ContextUtil.getUserId();
        OperateResult result =  proxy.claim(taskId,userId);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage());
        return operateStatus;
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
    public OperateStatus rollBackTo(String preTaskId, String opinion) throws CloneNotSupportedException{
        OperateStatus operateStatus = null;
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        OperateResult result = proxy.rollBackTo(preTaskId,opinion);
        operateStatus = new OperateStatus(result.successful(), result.getMessage());
        return operateStatus;
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
    public OperateStatus rejectTask(String taskId, String opinion) {
        OperateStatus operateStatus = null;
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        OperateResult result = proxy.taskReject(taskId, opinion, null);
        operateStatus = new OperateStatus(result.successful(), result.getMessage());
        return operateStatus;
    }


    /**
     * 获取当前审批任务的决策信息
     *
     * @param taskId
     * @return 操作结果
     */
    @RequestMapping(value = "nextNodesInfo")
    @ResponseBody
    public OperateStatus nextNodesInfo(String taskId) throws NoSuchMethodException {
        OperateStatus operateStatus = null;
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        List<NodeInfo> nodeInfoList = proxy.findNextNodes(taskId);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            operateStatus = new OperateStatus(true, "成功");
            operateStatus.setData(nodeInfoList);
        } else {
            operateStatus = new OperateStatus(false, "任务不存在，可能已经被处理");
        }
        return operateStatus;
    }

    /**
     * 获取下一步的节点信息任务
     *
     * @param taskId
     * @return 操作结果
     */
    @RequestMapping(value = "getSelectedNodesInfo")
    @ResponseBody
    public OperateStatus getSelectedNodesInfo(String taskId,String approved, String includeNodeIdsStr) throws NoSuchMethodException {
        OperateStatus operateStatus = null;
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        List<String> includeNodeIds = null;
        if (StringUtils.isNotEmpty(includeNodeIdsStr)) {
            String[] includeNodeIdsStringArray = includeNodeIdsStr.split(",");
            includeNodeIds = java.util.Arrays.asList(includeNodeIdsStringArray);
        }
        if(StringUtils.isEmpty(approved)){
            approved="APPROVED";
        }
        List<NodeInfo> nodeInfoList = proxy.findNexNodesWithUserSet(taskId,approved, includeNodeIds);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            operateStatus = new OperateStatus(true, "成功");
            if(nodeInfoList.size()==1&&"EndEvent".equalsIgnoreCase(nodeInfoList.get(0).getType())){//只存在结束节点
                operateStatus.setData("EndEvent");
            }else if(nodeInfoList.size()==1&&"CounterSignNotEnd".equalsIgnoreCase(nodeInfoList.get(0).getType())){
                operateStatus.setData("CounterSignNotEnd");
            }else {
                operateStatus.setData(nodeInfoList);
            }
        } else {
            operateStatus = new OperateStatus(false, "任务不存在，可能已经被处理");
        }
        return operateStatus;
    }

    /**
     * 获取下一步的节点信息任务(带用户信息)
     *
     * @param taskId
     * @return 操作结果
     */
    @RequestMapping(value = "nextNodesInfoWithUser")
    @ResponseBody
    public OperateStatus nextNodesInfoWithUser(String taskId) throws NoSuchMethodException {
        OperateStatus operateStatus = null;
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        List<NodeInfo> nodeInfoList = proxy.findNexNodesWithUserSet(taskId);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            operateStatus = new OperateStatus(true, "成功");
            operateStatus.setData(nodeInfoList);
        } else {
            operateStatus = new OperateStatus(false, "任务不存在，可能已经被处理");
        }
        return operateStatus;
    }

    /**
     * 获取任务抬头信息信息任务
     *
     * @param taskId
     * @return 操作结果
     */
    @RequestMapping(value = "getApprovalHeaderInfo")
    @ResponseBody
    public OperateStatus getApprovalHeaderInfo(String taskId) {
        OperateStatus operateStatus = null;
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        ApprovalHeaderVO approvalHeaderVO = proxy.getApprovalHeaderVO(taskId);
        if (approvalHeaderVO != null) {
            operateStatus = new OperateStatus(true, "成功");
            operateStatus.setData(approvalHeaderVO);
        } else {
            operateStatus = new OperateStatus(false, "任务不存在，可能已经被处理");
        }
        return operateStatus;
    }

    /**
     * 验证UEL表达式是否正常
     *
     * @param flowTypeId 流程类型ID
     * @param  expression  uel表达式内容
     * @return 操作结果
     */
    @RequestMapping(value = "validateExpression")
    @ResponseBody
    public OperateStatus validateExpression(String flowTypeId,String expression) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException,
            InvocationTargetException{
        OperateStatus operateStatus = null;
        IFlowDefinationService proxy = ApiClient.createProxy(IFlowDefinationService.class);
        OperateResultWithData<FlowDefination> result= proxy.validateExpression(flowTypeId,expression);
        operateStatus = new OperateStatus(result.successful(), result.getMessage());
        return operateStatus;
    }


    /**
     * 获取下一步的节点信息任务
     *
     * @param taskIds
     * @return 操作结果
     */
    @RequestMapping(value = "getSelectedCanBatchNodesInfo")
    @ResponseBody
    public OperateStatus getSelectedCanBatchNodesInfo(String taskIds) throws NoSuchMethodException {
        OperateStatus operateStatus = null;
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        List<NodeGroupByFlowVersionInfo> nodeInfoList = proxy.findNexNodesGroupByVersionWithUserSetCanBatch(taskIds);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            operateStatus = new OperateStatus(true, "成功");
             operateStatus.setData(nodeInfoList);
        } else {
            operateStatus = new OperateStatus(false, "选取的任务不存在，可能已经被处理");
        }
        return operateStatus;
    }

    @RequestMapping(value = "completeTaskBatch")
    @ResponseBody
    public OperateStatus completeTaskBatch(String flowTaskBatchCompleteWebVoStrs) {
        OperateStatus operateStatus=null;
        List<FlowTaskBatchCompleteWebVO> flowTaskBatchCompleteWebVOList = null;
        if (StringUtils.isNotEmpty(flowTaskBatchCompleteWebVoStrs)) {
            JSONArray jsonArray = JSONArray.fromObject(flowTaskBatchCompleteWebVoStrs);//把String转换为json
            if(jsonArray !=null && !jsonArray.isEmpty()){
                flowTaskBatchCompleteWebVOList = new ArrayList<FlowTaskBatchCompleteWebVO>();
                for(int i=0;i<jsonArray.size();i++){
                    FlowTaskBatchCompleteWebVO flowTaskBatchCompleteWebVO = new FlowTaskBatchCompleteWebVO();
                   JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                   JSONArray taskIdListJsonArray = (JSONArray)jsonObject.get("taskIdList");
                    JSONArray flowTaskCompleteListJsonArray = (JSONArray)jsonObject.get("flowTaskCompleteList");
                   List<FlowTaskCompleteWebVO> flowTaskCompleteWebVOList = (List<FlowTaskCompleteWebVO>) JSONArray.toCollection(flowTaskCompleteListJsonArray, FlowTaskCompleteWebVO.class);
                    flowTaskBatchCompleteWebVO.setFlowTaskCompleteList(flowTaskCompleteWebVOList);
                    List<String> taskIdList = ( List<String>) JSONArray.toCollection(taskIdListJsonArray, String.class);
                    flowTaskBatchCompleteWebVO.setTaskIdList(taskIdList);
                    flowTaskBatchCompleteWebVOList.add(flowTaskBatchCompleteWebVO);
                }

            }
            String opinion = "同意";
            if(flowTaskBatchCompleteWebVOList!=null && !flowTaskBatchCompleteWebVOList.isEmpty()){
                 int total=0;//记录处理任务总数
                 StringBuffer failMessage = new StringBuffer();
                for (FlowTaskBatchCompleteWebVO flowTaskBatchCompleteWebVO:flowTaskBatchCompleteWebVOList){
                    FlowTaskBatchCompleteVO flowTaskBatchCompleteVO = new FlowTaskBatchCompleteVO();
                    flowTaskBatchCompleteVO.setTaskIdList(flowTaskBatchCompleteWebVO.getTaskIdList());
                    flowTaskBatchCompleteVO.setOpinion(opinion);
                    Map<String,String> selectedNodesMap = new HashMap<>();
                    Map<String, Object> v = new HashMap<String, Object>();
                    List<FlowTaskCompleteWebVO>   flowTaskCompleteList = flowTaskBatchCompleteWebVO.getFlowTaskCompleteList();

                    if (flowTaskCompleteList != null && !flowTaskCompleteList.isEmpty()) {
                        for (FlowTaskCompleteWebVO f : flowTaskCompleteList) {
                            String flowTaskType = f.getFlowTaskType();
                                selectedNodesMap.put(f.getNodeId(),f.getNodeId());
                                if ("common".equalsIgnoreCase(flowTaskType) || "approve".equalsIgnoreCase(flowTaskType)) {
                                    String userId = f.getUserIds().replaceAll(",","");
                                    v.put(f.getUserVarName(),userId);
                                } else {
                                    String[] idArray = f.getUserIds().split(",");
                                    if(StringUtils.isNotEmpty(f.getUserVarName())){
                                        v.put(f.getUserVarName(), idArray);
                                    }
                                }
                        }
                    }
                    v.put("approved", true);//针对会签时同意、不同意、弃权等操作
                    flowTaskBatchCompleteVO.setVariables(v);
                    IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
                    OperateResultWithData<Integer> operateResult = proxy.completeBatch(flowTaskBatchCompleteVO);
                    total+=operateResult.getData();
                    if(operateResult.successful()){
                    }else {
                        failMessage.append(operateResult.getMessage()+";");
                    }
                }
                if(total>0){
                          operateStatus = new OperateStatus(true, "成功处理任务"+total+"条");
                 }else{
                          operateStatus = new OperateStatus(false, failMessage.toString());
                 }
            }
        }else {
            operateStatus = new OperateStatus(false, "参数值错误！");
        }
        return operateStatus;
    }
}
