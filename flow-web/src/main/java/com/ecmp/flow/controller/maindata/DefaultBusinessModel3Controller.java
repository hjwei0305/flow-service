package com.ecmp.flow.controller.maindata;

import com.ecmp.annotation.IgnoreCheckAuth;
import com.ecmp.config.util.ApiClient;
import com.ecmp.core.json.JsonUtil;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.*;
import com.ecmp.flow.api.common.api.IBaseService;
import com.ecmp.flow.common.web.controller.FlowBaseController;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.entity.DefaultBusinessModel;
import com.ecmp.flow.entity.DefaultBusinessModel2;
import com.ecmp.flow.entity.DefaultBusinessModel3;
import com.ecmp.flow.vo.FlowStartResultVO;
import com.ecmp.flow.vo.FlowStartVO;
import com.ecmp.flow.vo.FlowTaskCompleteVO;
import com.ecmp.flow.vo.FlowTaskCompleteWebVO;
import com.ecmp.vo.OperateResultWithData;
import com.fasterxml.jackson.core.JsonProcessingException;
import net.sf.json.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * 1.0.00      2017/4/26 9:32      詹耀(xxxlimit)                    新建
 * <br>
 * *************************************************************************************************<br>
 */
@Controller
@RequestMapping(value = "/defaultBusinessModel3")
@IgnoreCheckAuth
public class DefaultBusinessModel3Controller extends FlowBaseController<IDefaultBusinessModel3Service,DefaultBusinessModel3> {

    public DefaultBusinessModel3Controller(){
        super(IDefaultBusinessModel3Service.class);
    }

    @RequestMapping(value = "show", method = RequestMethod.GET)
    public String show() {
        return "maindata/DefaultBusinessModel3View";
    }

    @RequestMapping(value = "showBill", method = RequestMethod.GET)
    public String showBill() {
        return "maindata/LookApproveBillView3";
    }


    @RequestMapping(value = "approve", method = RequestMethod.GET)
    public String showApprove(){
        return "approve/ApproveView3";
    }


    /**
     * 销售申请审批界面(查看)
     *
     * @return
     */
    @RequestMapping(value = "look", method = RequestMethod.GET)
    public String look() {
        return "approve/ApproveView3";
    }

    /**
     * 销售申请表单查看
     *
     * @return
     */
    @RequestMapping(value = "orderLook", method = RequestMethod.GET)
    public String orderLook() {
        return "maindata/ReadyOnlyApproveView3";
    }

    /**
     * 销售申请审批界面(编辑)
     *
     * @return
     */
    @RequestMapping(value = "edit", method = RequestMethod.GET)
    public String edit() {
        return "approve/ApproveEditView3";
    }

    /**
     * 销售申请表单编辑
     *
     * @return
     */
    @RequestMapping(value = "orderEdit", method = RequestMethod.GET)
    public String orderEdit() {
        return "maindata/DefauleOrderEditView3";
    }


    @RequestMapping(value = "getApproveBill3")
    @ResponseBody
    public String getApproveBill3(String id) throws JsonProcessingException {
        // id="0C0E00EA-3AC2-11E7-9AC5-3C970EA9E0F7";
        IDefaultBusinessModel3Service proxy = ApiClient.createProxy(IDefaultBusinessModel3Service.class);
        DefaultBusinessModel3 result = proxy.findOne(id);
        OperateStatus status = new OperateStatus(true,OperateStatus.COMMON_SUCCESS_MSG,result);
        return JsonUtil.serialize(status,JsonUtil.DATE_TIME);
    }


    /**
     * 通过流程定义key启动流程,
     *
     * @param businessModelCode
     * @return 操作结果
     */
    @RequestMapping(value = "startFlow")
    @ResponseBody
    public String startFlow(String businessModelCode, String businessKey, String opinion, String typeId, String taskList) throws NoSuchMethodException, SecurityException {
        IBaseService baseService = ApiClient.createProxy(apiClass);
        OperateStatus operateStatus = null;
        DefaultBusinessModel3 defaultBusinessModel3 = (DefaultBusinessModel3) baseService.findOne(businessKey);
        List<FlowTaskCompleteWebVO> flowTaskCompleteList = null;
        if (defaultBusinessModel3 != null) {
            IFlowDefinationService proxy = ApiClient.createProxy(IFlowDefinationService.class);
            Map<String, Object> userMap = new HashMap<String, Object>();//UserTask_1_Normal
            FlowStartVO flowStartVO = new FlowStartVO();
            flowStartVO.setBusinessKey(businessKey);
            flowStartVO.setBusinessModelCode(businessModelCode);
            flowStartVO.setFlowTypeId(typeId);


            //测试跨业务实体子流程,并发多级子流程测试
            List<DefaultBusinessModel> defaultBusinessModelList = new ArrayList<>();
            List<DefaultBusinessModel2> defaultBusinessModel2List = new ArrayList<>();
            List<DefaultBusinessModel3> defaultBusinessModel3List = new ArrayList<>();
            if (StringUtils.isNotEmpty(taskList)) {
                JSONArray jsonArray = JSONArray.fromObject(taskList);//把String转换为json
                flowTaskCompleteList = (List<FlowTaskCompleteWebVO>) JSONArray.toCollection(jsonArray, FlowTaskCompleteWebVO.class);

                if (flowTaskCompleteList != null && !flowTaskCompleteList.isEmpty()) {
                    for (FlowTaskCompleteWebVO f : flowTaskCompleteList) {
                        String flowTaskType = f.getFlowTaskType();
                        if ("common".equalsIgnoreCase(flowTaskType) || "approve".equalsIgnoreCase(flowTaskType)) {
                            userMap.put(f.getUserVarName(), f.getUserIds());
                        } else {
                            String[] idArray = f.getUserIds().split(",");
                            userMap.put(f.getUserVarName(), idArray);
                        }

                        //测试跨业务实体子流程,并发多级子流程测试
                        String callActivityPath = f.getCallActivityPath();
                        if (StringUtils.isNotEmpty(callActivityPath)) {
                            Map<String, String> callActivityPathMap = initCallActivtiy(callActivityPath);
                            Map<String, Object> variables = new HashMap<String, Object>();
                            flowStartVO.setVariables(variables);
                            initCallActivityBusiness(defaultBusinessModelList, defaultBusinessModel2List, defaultBusinessModel3List, callActivityPathMap, variables, defaultBusinessModel3);
                        }
                    }
                }
            }
            flowStartVO.setUserMap(userMap);
            FlowStartResultVO flowStartResultVO = proxy.startByVO(flowStartVO);
            if (flowStartResultVO != null) {
                if (flowStartResultVO.getFlowInstance() != null) {
                    defaultBusinessModel3 = (DefaultBusinessModel3) baseService.findOne(businessKey);
                    if (flowStartResultVO.getFlowInstance().isEnded()) {
                        defaultBusinessModel3.setFlowStatus(FlowStatus.COMPLETED);
                        initCallActivityBusinessStatus(defaultBusinessModelList, defaultBusinessModel2List, defaultBusinessModel3List, FlowStatus.COMPLETED);
                    } else {
                        defaultBusinessModel3.setFlowStatus(FlowStatus.INPROCESS);
                        initCallActivityBusinessStatus(defaultBusinessModelList, defaultBusinessModel2List, defaultBusinessModel3List, FlowStatus.INPROCESS);
                    }
                    baseService.save(defaultBusinessModel3);
                }
                operateStatus = new OperateStatus(true, "成功");
                operateStatus.setData(flowStartResultVO);
            } else {
                new OperateStatus(false, "启动流程失败");
            }
        } else {
            operateStatus = new OperateStatus(false, "业务对象不存在");
        }
        return JsonUtil.serialize(operateStatus);
    }


    /**
     * 完成任务
     *
     * @param taskId
     * @param businessId 业务表单ID
     * @param opinion    审批意见
     * @param taskList   任务完成传输对象
     * @param
     * @return 操作结果
     */
    @RequestMapping(value = "completeTask")
    @ResponseBody
    public String completeTask(String taskId, String businessId, String opinion, String taskList, String endEventId, boolean manualSelected, String approved) {
        List<FlowTaskCompleteWebVO> flowTaskCompleteList = null;
        if (StringUtils.isNotEmpty(taskList)) {
            JSONArray jsonArray = JSONArray.fromObject(taskList);//把String转换为json
            flowTaskCompleteList = (List<FlowTaskCompleteWebVO>) JSONArray.toCollection(jsonArray, FlowTaskCompleteWebVO.class);
        }
        IBaseService baseService = ApiClient.createProxy(apiClass);
        OperateStatus operateStatus = null;
        DefaultBusinessModel3 defaultBusinessModel3 = (DefaultBusinessModel3) baseService.findOne(businessId);
        if (defaultBusinessModel3 != null) {
            FlowTaskCompleteVO flowTaskCompleteVO = new FlowTaskCompleteVO();
            flowTaskCompleteVO.setTaskId(taskId);
            flowTaskCompleteVO.setOpinion(opinion);
            List<String> selectedNodeIds = new ArrayList<String>();
            Map<String, Object> v = new HashMap<String, Object>();

            //测试跨业务实体子流程,并发多级子流程测试
            List<DefaultBusinessModel> defaultBusinessModelList = new ArrayList<>();
            List<DefaultBusinessModel2> defaultBusinessModel2List = new ArrayList<>();
            List<DefaultBusinessModel3> defaultBusinessModel3List = new ArrayList<>();

            if (flowTaskCompleteList != null && !flowTaskCompleteList.isEmpty()) {
                for (FlowTaskCompleteWebVO f : flowTaskCompleteList) {
                    selectedNodeIds.add(f.getNodeId());
                    String flowTaskType = f.getFlowTaskType();
                    if ("common".equalsIgnoreCase(flowTaskType) || "approve".equalsIgnoreCase(flowTaskType)) {
                        v.put(f.getUserVarName(), f.getUserIds());
                    } else {
                        String[] idArray = f.getUserIds().split(",");
                        v.put(f.getUserVarName(), idArray);
                    }
                    //测试跨业务实体子流程,并发多级子流程测试
                    String callActivityPath = f.getCallActivityPath();
                    if (StringUtils.isNotEmpty(callActivityPath)) {
                        Map<String, String> callActivityPathMap = initCallActivtiy(callActivityPath);
                        initCallActivityBusiness(defaultBusinessModelList, defaultBusinessModel2List, defaultBusinessModel3List, callActivityPathMap, v, defaultBusinessModel3);
                    }
                }
            } else {
                if (StringUtils.isNotEmpty(endEventId)) {
                    selectedNodeIds.add(endEventId);
                }
            }
            if (manualSelected) {
                flowTaskCompleteVO.setManualSelectedNodeIds(selectedNodeIds);
            }

            //  Map<String,Object> v = new HashMap<String,Object>();
            v.put("approved", approved);//针对会签时同意、不同意、弃权等操作
            flowTaskCompleteVO.setVariables(v);
            IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
            OperateResultWithData<FlowStatus> operateResult = proxy.complete(flowTaskCompleteVO);
            if (FlowStatus.COMPLETED.toString().equalsIgnoreCase(operateResult.getData() + "")) {
                defaultBusinessModel3 = (DefaultBusinessModel3) baseService.findOne(businessId);
                defaultBusinessModel3.setFlowStatus(FlowStatus.COMPLETED);
                baseService.save(defaultBusinessModel3);
            }
            operateStatus = new OperateStatus(true, operateResult.getMessage());
        } else {
            operateStatus = new OperateStatus(false, "业务对象不存在");
        }
        return JsonUtil.serialize(operateStatus);
    }
}

