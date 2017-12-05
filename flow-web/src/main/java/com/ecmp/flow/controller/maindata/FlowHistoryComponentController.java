package com.ecmp.flow.controller.maindata;

import com.ecmp.annotation.IgnoreCheckAuth;
import com.ecmp.config.util.ApiClient;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.IFlowInstanceService;
import com.ecmp.flow.vo.ProcessTrackVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;
import java.util.List;


/**
 * *************************************************************************************************
 * <br>
 * 实现功能：
 * <br>
 * ------------------------------------------------------------------------------------------------
 * <br>
 * 版本          变更时间             变更人                     变更原因
 * <br>
 * ------------------------------------------------------------------------------------------------
 * <br>
 * 1.0.00      2017/5/3 9:32      谭军(tanjun)                    新建
 * <br>
 * *************************************************************************************************<br>
 */
@Controller
@RequestMapping(value = "/flowHistoryInfo")
@IgnoreCheckAuth
public class FlowHistoryComponentController {
//    /**
//     * 根据流程实例id查询流程历史
//     * @param instanceId
//     * @return
//     * @throws JsonProcessingException
//     * @throws ParseException
//     */
//    @RequestMapping(value = "listFlowHistory")
//    @ResponseBody
//    public String listFlowHistory(String instanceId) throws JsonProcessingException, ParseException {
//        IFlowHistoryService proxy = ApiClient.createProxy(IFlowHistoryService.class);
//         List<FlowHistory> flowHistories = proxy.findByInstanceId(instanceId);
//        return JsonUtil.serialize(flowHistories, JsonUtil.DATE_TIME);
//    }
//
//    /**
//     * 根据流程实例id查询代办
//     * @param instanceId
//     * @return
//     * @throws JsonProcessingException
//     * @throws ParseException
//     */
//    @RequestMapping(value = "listFlowTask")
//    @ResponseBody
//    public String listFlowTask(String instanceId) throws JsonProcessingException, ParseException {
//        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
//        List<FlowTask>  flowTasks= proxy.findByInstanceId(instanceId);
//        return JsonUtil.serialize(flowTasks, JsonUtil.DATE_TIME);
//    }
//
//    /**
//     * 根据id获取流程实例
//     * @param instanceId
//     * @return
//     * @throws JsonProcessingException
//     * @throws ParseException
//     */
//    @RequestMapping(value = "getFlowInstance")
//    @ResponseBody
//    public String getFlowInstance(String instanceId) throws JsonProcessingException, ParseException {
//        IFlowInstanceService proxy = ApiClient.createProxy(IFlowInstanceService.class);
//        FlowInstance  flowInstance = proxy.findOne(instanceId);
//        return JsonUtil.serialize(flowInstance, JsonUtil.DATE_TIME);
//    }

    /**
     * 根据流程实例id获取流程信息
     *
     * @param
     * @return
     * @throws JsonProcessingException
     * @throws ParseException
     */
//    @RequestMapping(value = "getFlowHistoryInfo")
//    @ResponseBody
//    public String getFlowHistoryInfo(String instanceId) throws JsonProcessingException{
//        FlowHistoryInfoVO flowHistoryInfoVO = new FlowHistoryInfoVO();
//        /**
//         * 根据流程实例id获取流程历史
//         */
//        IFlowHistoryService proxy = ApiClient.createProxy(IFlowHistoryService.class);
//        List<FlowHistory> flowHistories = proxy.findByInstanceId(instanceId);
//        List<FlowHandleHistoryVO> flowHandleHistoryVOSet = new ArrayList<>();
//        for(int i=0;i<flowHistories.size();i++){
//            FlowHandleHistoryVO flowHandleHistoryVO = new FlowHandleHistoryVO();
//            flowHandleHistoryVO.setFlowHistoryTaskName(flowHistories.get(i).getFlowTaskName());
//            flowHandleHistoryVO.setFlowHistoryTaskExecutorName(flowHistories.get(i).getExecutorName());
//            flowHandleHistoryVO.setFlowHistoryTaskEndTime(flowHistories.get(i).getActEndTime());
//            flowHandleHistoryVO.setFlowHistoryTaskDurationInMillis(flowHistories.get(i).getActDurationInMillis());
//            flowHandleHistoryVO.setFlowHistoryTaskRemark(flowHistories.get(i).getDepict());
//            flowHandleHistoryVOSet.add(flowHandleHistoryVO);
//        }
//        flowHistoryInfoVO.setFlowHandleHistoryVOList(flowHandleHistoryVOSet);
//        /**
//         * 根据流程实例id获取待办
//         */
//        IFlowTaskService proxy2 = ApiClient.createProxy(IFlowTaskService.class);
//        List<FlowTask>  flowTasks= proxy2.findByInstanceId(instanceId);
//        List<FlowHandleStatusVO> flowHandleStatusVOSet = new ArrayList<>();
//        for(int i=0;i<flowTasks.size();i++){
//            FlowHandleStatusVO flowHandleStatusVO = new FlowHandleStatusVO();
//            flowHandleStatusVO.setFlowCurHandleStatusTaskName(flowTasks.get(i).getTaskName());
//            flowHandleStatusVO.setFlowWaitingPerson(flowTasks.get(i).getExecutorName());
//            flowHandleStatusVO.setFlowTaskArriveTime(flowTasks.get(i).getCreatedDate());
//            flowHandleStatusVOSet.add(flowHandleStatusVO);
//        }
//        flowHistoryInfoVO.setFlowHandleStatusVOList(flowHandleStatusVOSet);
//        /**
//         * 根据流程实例id获取流程启动信息
//         */
//        IFlowInstanceService proxy3 = ApiClient.createProxy(IFlowInstanceService.class);
//        FlowInstance  flowInstance = proxy3.findOne(instanceId);
//        flowHistoryInfoVO.setFlowStarter(flowInstance.getCreatorName());
//        flowHistoryInfoVO.setFlowStartTime(flowInstance.getStartDate());
//        return JsonUtil.serialize(flowHistoryInfoVO, JsonUtil.DATE_TIME);
//    }
    @RequestMapping(value = "getFlowHistoryInfo")
    @ResponseBody
    @IgnoreCheckAuth
    public OperateStatus getFlowHistoryInfo(String businessId, String instanceId) throws JsonProcessingException {
        List<ProcessTrackVO> result = null;
        IFlowInstanceService proxy = ApiClient.createProxy(IFlowInstanceService.class);
        if(StringUtils.isNotEmpty(instanceId)){
            result = proxy.getProcessTrackVOById(instanceId);
        }else if(StringUtils.isNotEmpty(businessId)){
            result = proxy.getProcessTrackVO(businessId);
        }
        OperateStatus operateStatus = null;
        if(result==null || result.isEmpty()){
            operateStatus =  new OperateStatus(false, "历史记录为空！");
        }else{
            operateStatus =  new OperateStatus(true, OperateStatus.COMMON_SUCCESS_MSG);
            operateStatus.setData(result);
        }
        return operateStatus;
    }
}