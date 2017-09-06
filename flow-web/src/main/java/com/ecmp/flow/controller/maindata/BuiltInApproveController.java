package com.ecmp.flow.controller.maindata;

import com.ecmp.annotation.IgnoreCheckAuth;
import com.ecmp.config.util.ApiClient;
import com.ecmp.core.json.JsonUtil;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.IDefaultBusinessModelService;
import com.ecmp.flow.api.IFlowDefinationService;
import com.ecmp.flow.api.IFlowInstanceService;
import com.ecmp.flow.api.IFlowTaskService;
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
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
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
 * 实现功能：默认表单（业务申请）控制器
 * <br>
 * ------------------------------------------------------------------------------------------------
 * <br>
 * 版本          变更时间             变更人                     变更原因
 * <p>
 * <br>
 * ------------------------------------------------------------------------------------------------
 * <br>
 * 1.0.00      2017/4/26 9:32      詹耀(xxxlimit)                    新建
 * 1.0.00      2017/5/26 9:32      谭军（tanjun）                    增加启动流程，完成任务
 * <br>
 * *************************************************************************************************<br>
 */
@Controller
@RequestMapping(value = "/builtInApprove")
@IgnoreCheckAuth
public class BuiltInApproveController extends FlowBaseController<IDefaultBusinessModelService, DefaultBusinessModel> {


    public BuiltInApproveController() {
        super(IDefaultBusinessModelService.class);
    }

    @RequestMapping(value = "show", method = RequestMethod.GET)
    public String show() {
        return "maindata/BuiltInApproveView";
    }

    /**
     * 业务申请审批界面(查看)
     *
     * @return
     */
    @RequestMapping(value = "look", method = RequestMethod.GET)
    public String look() {
        return "approve/ApproveView";
    }

    /**
     * 业务申请表单查看
     *
     * @return
     */
    @RequestMapping(value = "orderLook", method = RequestMethod.GET)
    public String orderLook() {
        return "maindata/ReadyOnlyApproveView";
    }

    /**
     * 业务申请审批界面(编辑)
     *
     * @return
     */
    @RequestMapping(value = "edit", method = RequestMethod.GET)
    public String edit() {
        return "approve/ApproveEditView";
    }

    /**
     * 业务申请表单编辑
     *
     * @return
     */
    @RequestMapping(value = "orderEdit", method = RequestMethod.GET)
    public String orderEdit() {
        return "maindata/DefauleOrderEditView";
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
        DefaultBusinessModel defaultBusinessModel = (DefaultBusinessModel) baseService.findOne(businessKey);
        List<FlowTaskCompleteWebVO> flowTaskCompleteList = null;
        if (defaultBusinessModel != null) {
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
                            Map<String, String> callActivityPathMap = initCallActivtiy(callActivityPath,true);
                            Map<String, Object> variables = new HashMap<String, Object>();
                            flowStartVO.setVariables(variables);
                            initCallActivityBusiness(defaultBusinessModelList, defaultBusinessModel2List, defaultBusinessModel3List, callActivityPathMap, variables, defaultBusinessModel);
                        }
                    }
                }
            }
            flowStartVO.setUserMap(userMap);
            FlowStartResultVO flowStartResultVO = proxy.startByVO(flowStartVO);
            if (flowStartResultVO != null) {
                if (flowStartResultVO.getFlowInstance() != null) {
                    defaultBusinessModel = (DefaultBusinessModel) baseService.findOne(businessKey);
                    if (flowStartResultVO.getFlowInstance().isEnded()) {
                        defaultBusinessModel.setFlowStatus(FlowStatus.COMPLETED);
                        initCallActivityBusinessStatus(defaultBusinessModelList, defaultBusinessModel2List, defaultBusinessModel3List, FlowStatus.COMPLETED);
                    } else {
                        defaultBusinessModel.setFlowStatus(FlowStatus.INPROCESS);
                        initCallActivityBusinessStatus(defaultBusinessModelList, defaultBusinessModel2List, defaultBusinessModel3List, FlowStatus.INPROCESS);
                    }
                    baseService.save(defaultBusinessModel);
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
        DefaultBusinessModel defaultBusinessModel = (DefaultBusinessModel) baseService.findOne(businessId);
        if (defaultBusinessModel != null) {
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
                        Map<String, String> callActivityPathMap = initCallActivtiy(callActivityPath,false);
                        initCallActivityBusiness(defaultBusinessModelList, defaultBusinessModel2List, defaultBusinessModel3List, callActivityPathMap, v, defaultBusinessModel);
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
                defaultBusinessModel = (DefaultBusinessModel) baseService.findOne(businessId);
                defaultBusinessModel.setFlowStatus(FlowStatus.COMPLETED);
                baseService.save(defaultBusinessModel);
            }
            operateStatus = new OperateStatus(true, operateResult.getMessage());
        } else {
            operateStatus = new OperateStatus(false, "业务对象不存在");
        }
        return JsonUtil.serialize(operateStatus);
    }

    @RequestMapping(value = "testReciveTask")
    @ResponseBody
    public String testReciveTask(String businessId,String fReceiveTaskActDefId){
        IFlowInstanceService proxy = ApiClient.createProxy(IFlowInstanceService.class);
        OperateResult operateResult = proxy.signalByBusinessId(businessId,fReceiveTaskActDefId,null);
        OperateStatus   operateStatus = new OperateStatus(operateResult.successful(), operateResult.getMessage());
        return JsonUtil.serialize(operateStatus);
    }
}

