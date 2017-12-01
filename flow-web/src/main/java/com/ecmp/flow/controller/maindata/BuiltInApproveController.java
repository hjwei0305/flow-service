package com.ecmp.flow.controller.maindata;

import com.ecmp.annotation.IgnoreCheckAuth;
import com.ecmp.config.util.ApiClient;
import com.ecmp.core.api.IBaseEntityService;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.IDefaultBusinessModelService;
import com.ecmp.flow.api.IFlowDefinationService;
import com.ecmp.flow.api.IFlowInstanceService;
import com.ecmp.flow.api.IFlowTaskService;
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


//    /**
//     * 查询默认业务实体
//     *
//     * @param request
//     * @return
//     */
//    @RequestMapping(value = "list")
//    @ResponseBody
//    public PageResult list(ServletRequest request) {
//        IDefaultBusinessModelService baseService = ApiClient.createProxy(apiClass);
//        Search search = SearchUtil.genSearch(request);
//        PageResult<DefaultBusinessModel> defaultBusinessModelPageResult = baseService.findByPage(search);
//        return defaultBusinessModelPageResult;
//    }
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
    public OperateStatus startFlow(String businessModelCode, String businessKey, String opinion, String typeId, String taskList) throws NoSuchMethodException, SecurityException {
        IBaseEntityService baseService = ApiClient.createProxy(apiClass);
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
            Map<String, Object> variables = new HashMap<String, Object>();
            flowStartVO.setVariables(variables);

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

                        //测试跨业务实体子流程,并发多级子流程测试
                        String callActivityPath = f.getCallActivityPath();
                        if (StringUtils.isNotEmpty(callActivityPath)) {
                            Map<String, String> callActivityPathMap = initCallActivtiy(callActivityPath,true);


                            initCallActivityBusiness(defaultBusinessModelList, defaultBusinessModel2List, defaultBusinessModel3List, callActivityPathMap, variables, defaultBusinessModel);
                            List<String> userVarNameList = (List)userMap.get(callActivityPath+"_sonProcessSelectNodeUserV");
                            if(userVarNameList!=null){
                                userVarNameList.add(f.getUserVarName());
                            }else{
                                userVarNameList = new ArrayList<>();
                                userVarNameList.add(f.getUserVarName());
                                userMap.put(callActivityPath+"_sonProcessSelectNodeUserV",userVarNameList);//选择的变量名,子流程存在选择了多个的情况
                            }
                            if ("common".equalsIgnoreCase(flowTaskType) || "approve".equalsIgnoreCase(flowTaskType)) {
                                userMap.put(callActivityPath+"/"+f.getUserVarName(), f.getUserIds());
                            } else {
                                String[] idArray = f.getUserIds().split(",");
                                userMap.put(callActivityPath+"/"+f.getUserVarName(), idArray);
                            }
                        }else{
                            if ("common".equalsIgnoreCase(flowTaskType) || "approve".equalsIgnoreCase(flowTaskType)) {
                                userMap.put(f.getUserVarName(), f.getUserIds());
                            } else {
                                String[] idArray = f.getUserIds().split(",");
                                userMap.put(f.getUserVarName(), idArray);
                            }
                        }
                    }
                }
            }
            flowStartVO.setUserMap(userMap);
            OperateResultWithData<FlowStartResultVO> operateResultWithData = proxy.startByVO(flowStartVO);
            if(operateResultWithData.successful()){
                FlowStartResultVO flowStartResultVO = operateResultWithData.getData();
                if(flowStartResultVO!=null){
                    if (flowStartResultVO.getCheckStartResult()) {
                        operateStatus = new OperateStatus(true, "成功");
                        operateStatus.setData(flowStartResultVO);
                    }else {
                        operateStatus=  new OperateStatus(false, "启动流程失败,启动检查服务返回false!");
                    }
                }
                else {
                    operateStatus=  new OperateStatus(false, "启动流程失败");
                }
            }else {
                operateStatus=  new OperateStatus(false, operateResultWithData.getMessage());
            }
        } else {
            operateStatus = new OperateStatus(false, "业务对象不存在");
        }
        return operateStatus;
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
    public OperateStatus completeTask(String taskId, String businessId, String opinion, String taskList, String endEventId, boolean manualSelected, String approved) throws Exception{
        List<FlowTaskCompleteWebVO> flowTaskCompleteList = null;
        if (StringUtils.isNotEmpty(taskList)) {
            JSONArray jsonArray = JSONArray.fromObject(taskList);//把String转换为json
            flowTaskCompleteList = (List<FlowTaskCompleteWebVO>) JSONArray.toCollection(jsonArray, FlowTaskCompleteWebVO.class);
        }
        IBaseEntityService baseService = ApiClient.createProxy(apiClass);
        OperateStatus operateStatus = null;
        DefaultBusinessModel defaultBusinessModel = (DefaultBusinessModel) baseService.findOne(businessId);
        if (defaultBusinessModel != null) {
            FlowTaskCompleteVO flowTaskCompleteVO = new FlowTaskCompleteVO();
            flowTaskCompleteVO.setTaskId(taskId);
            flowTaskCompleteVO.setOpinion(opinion);
            Map<String,String> selectedNodesMap = new HashMap<>();
            Map<String, Object> v = new HashMap<String, Object>();

            //测试跨业务实体子流程,并发多级子流程测试
            List<DefaultBusinessModel> defaultBusinessModelList = new ArrayList<>();
            List<DefaultBusinessModel2> defaultBusinessModel2List = new ArrayList<>();
            List<DefaultBusinessModel3> defaultBusinessModel3List = new ArrayList<>();

            if (flowTaskCompleteList != null && !flowTaskCompleteList.isEmpty()) {
                for (FlowTaskCompleteWebVO f : flowTaskCompleteList) {
                    String flowTaskType = f.getFlowTaskType();

                    //测试跨业务实体子流程,并发多级子流程测试
                    String callActivityPath = f.getCallActivityPath();
                    if (StringUtils.isNotEmpty(callActivityPath)) {
                        Map<String, String> callActivityPathMap = initCallActivtiy(callActivityPath,true);
                        initCallActivityBusiness(defaultBusinessModelList, defaultBusinessModel2List, defaultBusinessModel3List, callActivityPathMap, v, defaultBusinessModel);
                        selectedNodesMap.put(callActivityPath,f.getNodeId());

                        List<String> userVarNameList = (List)v.get(callActivityPath+"_sonProcessSelectNodeUserV");
                        if(userVarNameList!=null){
                            userVarNameList.add(f.getUserVarName());
                        }else{
                            userVarNameList = new ArrayList<>();
                            userVarNameList.add(f.getUserVarName());
                            v.put(callActivityPath+"_sonProcessSelectNodeUserV",userVarNameList);//选择的变量名,子流程存在选择了多个的情况
                        }
                        if ("common".equalsIgnoreCase(flowTaskType) || "approve".equalsIgnoreCase(flowTaskType)) {
                            v.put(callActivityPath+"/"+f.getUserVarName(), f.getUserIds());
                        } else {
                            String[] idArray = f.getUserIds().split(",");
                            v.put(callActivityPath+"/"+f.getUserVarName(), idArray);
                        }
                    }else {
                        selectedNodesMap.put(f.getNodeId(),f.getNodeId());
                        if ("common".equalsIgnoreCase(flowTaskType) || "approve".equalsIgnoreCase(flowTaskType)) {
                            v.put(f.getUserVarName(), f.getUserIds());
                        } else {
                            String[] idArray = f.getUserIds().split(",");
                            v.put(f.getUserVarName(), idArray);
                        }
                    }
                }
            } else {
                if (StringUtils.isNotEmpty(endEventId)) {
                    selectedNodesMap.put(endEventId,endEventId);
                }
            }
            if (manualSelected) {
                flowTaskCompleteVO.setManualSelectedNode(selectedNodesMap);
            }

            v.put("approved", approved);//针对会签时同意、不同意、弃权等操作
            flowTaskCompleteVO.setVariables(v);
            IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
            OperateResultWithData<FlowStatus> operateResult = proxy.complete(flowTaskCompleteVO);
            operateStatus = new OperateStatus(operateResult.successful(), operateResult.getMessage());
        } else {
            operateStatus = new OperateStatus(false, "业务对象不存在");
        }
        return operateStatus;
    }

    @RequestMapping(value = "testReciveTask")
    @ResponseBody
    public OperateStatus testReciveTask(String businessId,String fReceiveTaskActDefId){
        IFlowInstanceService proxy = ApiClient.createProxy(IFlowInstanceService.class);
        OperateResult operateResult = proxy.signalByBusinessId(businessId,fReceiveTaskActDefId,null);
        OperateStatus   operateStatus = new OperateStatus(operateResult.successful(), operateResult.getMessage());
        return operateStatus;
    }

    /**
     * 通过流程定义key启动流程,
     *
     * @param businessModelCode
     * @return 操作结果
     */
    @RequestMapping(value = "startFlowTest")
    @ResponseBody
    public OperateStatus startFlowTest(String businessModelCode, String businessKey, String opinion, String typeId, String taskList) throws NoSuchMethodException, SecurityException {

        OperateStatus operateStatus = new OperateStatus(true, "成功");
        IFlowDefinationService proxy = ApiClient.createProxy(IFlowDefinationService.class);
        FlowStartVO flowStartVO = new FlowStartVO();
        flowStartVO.setBusinessKey(businessKey);
        flowStartVO.setBusinessModelCode(businessModelCode);
        flowStartVO.setFlowTypeId(typeId);
        proxy.startByVO(flowStartVO);


        return operateStatus;
    }

    /**
     * 通过流程定义key启动流程,
     *
     * @return 操作结果
     */
    @RequestMapping(value = "startTest")
    @ResponseBody
    public OperateStatus startTest() throws NoSuchMethodException, SecurityException {
        String businessModelCode="com.ecmp.brm.act.entity.BusinessRequest";
        String businessKey="109B9C6A-BE1A-11E7-9296-00FF36C681BD";
        String opinion=null;
        String typeId=null;
        String taskList="[{\"nodeId\":\"UserTask_4\",\"userVarName\":\"UserTask_4_Approve\",\"flowTaskType\":\"approve\",\"callActivityPath\":null,\"userIds\":\"1C67DAA0-3530-11E7-9C56-ACE010C46AFD\"}]";
        OperateStatus operateStatus = this.startFlowTTT( businessModelCode,  businessKey,  opinion,  typeId,  taskList);


        return operateStatus;
    }

    public OperateStatus startFlowTTT(String businessModelCode, String businessKey, String opinion, String typeId, String taskList) throws NoSuchMethodException, SecurityException {
        IBaseEntityService baseService = ApiClient.createProxy(apiClass);
        OperateStatus operateStatus = null;

        List<FlowTaskCompleteWebVO> flowTaskCompleteList = null;
        if (true) {
            IFlowDefinationService proxy = ApiClient.createProxy(IFlowDefinationService.class);
            Map<String, Object> userMap = new HashMap<String, Object>();//UserTask_1_Normal
            FlowStartVO flowStartVO = new FlowStartVO();
            flowStartVO.setBusinessKey(businessKey);
            flowStartVO.setBusinessModelCode(businessModelCode);
            flowStartVO.setFlowTypeId(typeId);
            Map<String, Object> variables = new HashMap<String, Object>();
            flowStartVO.setVariables(variables);

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

                        //测试跨业务实体子流程,并发多级子流程测试
                        String callActivityPath = f.getCallActivityPath();
                        if (StringUtils.isNotEmpty(callActivityPath)) {
                            Map<String, String> callActivityPathMap = initCallActivtiy(callActivityPath,true);


//                            initCallActivityBusiness(defaultBusinessModelList, defaultBusinessModel2List, defaultBusinessModel3List, callActivityPathMap, variables, defaultBusinessModel);
                            List<String> userVarNameList = (List)userMap.get(callActivityPath+"_sonProcessSelectNodeUserV");
                            if(userVarNameList!=null){
                                userVarNameList.add(f.getUserVarName());
                            }else{
                                userVarNameList = new ArrayList<>();
                                userVarNameList.add(f.getUserVarName());
                                userMap.put(callActivityPath+"_sonProcessSelectNodeUserV",userVarNameList);//选择的变量名,子流程存在选择了多个的情况
                            }
                            if ("common".equalsIgnoreCase(flowTaskType) || "approve".equalsIgnoreCase(flowTaskType)) {
                                userMap.put(callActivityPath+"/"+f.getUserVarName(), f.getUserIds());
                            } else {
                                String[] idArray = f.getUserIds().split(",");
                                userMap.put(callActivityPath+"/"+f.getUserVarName(), idArray);
                            }
                        }else{
                            if ("common".equalsIgnoreCase(flowTaskType) || "approve".equalsIgnoreCase(flowTaskType)) {
                                userMap.put(f.getUserVarName(), f.getUserIds());
                            } else {
                                String[] idArray = f.getUserIds().split(",");
                                userMap.put(f.getUserVarName(), idArray);
                            }
                        }
                    }
                }
            }
            flowStartVO.setUserMap(userMap);
            OperateResultWithData<FlowStartResultVO> operateResultWithData = proxy.startByVO(flowStartVO);
            if(operateResultWithData.successful()){
                FlowStartResultVO flowStartResultVO = operateResultWithData.getData();
                if(flowStartResultVO!=null){
                    if (flowStartResultVO.getCheckStartResult()) {
                        operateStatus = new OperateStatus(true, "成功");
                        operateStatus.setData(flowStartResultVO);
                    }else {
                        operateStatus=  new OperateStatus(false, "启动流程失败,启动检查服务返回false!");
                    }
                }
                else {
                    operateStatus=  new OperateStatus(false, "启动流程失败");
                }
            }else {
                operateStatus=  new OperateStatus(false, operateResultWithData.getMessage());
            }
        } else {
            operateStatus = new OperateStatus(false, "业务对象不存在");
        }
        return operateStatus;
    }

}

