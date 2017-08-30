package com.ecmp.flow.common.web.controller;

import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.core.json.JsonUtil;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchUtil;
import com.ecmp.core.vo.OperateStatus;
import com.ecmp.flow.api.*;
import com.ecmp.flow.api.common.api.IBaseService;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.entity.*;
import com.ecmp.flow.vo.*;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import io.swagger.annotations.OAuth2Definition;
import net.sf.json.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletRequest;
import java.text.ParseException;
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
 * <p>
 * <br>
 * ------------------------------------------------------------------------------------------------
 * <br>
 * 1.0.00      2017/5/26 9:32      谭军（tanjun）                    流程Conral抽象类
 * <br>
 * *************************************************************************************************<br>
 */
public abstract class FlowBaseController<T extends IBaseService, V extends AbstractBusinessModel> {


    protected Class<T> apiClass;

    public FlowBaseController() {

    }

    public FlowBaseController(Class<T> apiClass) {
        this.apiClass = apiClass;
    }

    /**
     * 查询默认业务实体
     *
     * @param request
     * @return
     * @throws ParseException
     */
    @RequestMapping(value = "list")
    @ResponseBody
    public String list(ServletRequest request) {
        IBaseService baseService = ApiClient.createProxy(apiClass);
        Search search = SearchUtil.genSearch(request);
        PageResult<V> defaultBusinessModelPageResult = baseService.findByPage(search);
        return JsonUtil.serialize(defaultBusinessModelPageResult);
    }

    /**
     * 删除默认业务实体
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    public String delete(String id) {
        IBaseService baseService = ApiClient.createProxy(apiClass);
        OperateResult result = baseService.delete(id);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage());
        return JsonUtil.serialize(operateStatus);
    }


    /**
     * 保存默认业务实体
     *
     * @param defaultBusinessModel
     * @return
     */
    @RequestMapping(value = "save")
    @ResponseBody
    public String save(V defaultBusinessModel) {
        IBaseService baseService = ApiClient.createProxy(apiClass);
        if(defaultBusinessModel.getFlowStatus()==null){
            defaultBusinessModel.setFlowStatus(FlowStatus.INIT);
        }
        OperateResultWithData<V> result = baseService.save(defaultBusinessModel);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage(), result.getData());
        return JsonUtil.serialize(operateStatus);
    }

    /**
     * 通过流程定义key启动流程
     *
     * @param businessModelCode
     * @return 操作结果
     */
    @RequestMapping(value = "startFlow")
    @ResponseBody
    public String startFlow(String businessModelCode, String businessKey,String opinion, String typeId,String taskList) throws NoSuchMethodException, SecurityException{
        IBaseService baseService = ApiClient.createProxy(apiClass);
        OperateStatus operateStatus = null;
        V defaultBusinessModel = (V) baseService.findOne(businessKey);
        List<FlowTaskCompleteWebVO> flowTaskCompleteList = null;
        if (defaultBusinessModel != null) {
            String startUserId = "admin";
            String startUserIdContext = ContextUtil.getSessionUser().getUserId();
            if (!StringUtils.isEmpty(startUserIdContext)) {
                startUserId = startUserIdContext;
            }
            IFlowDefinationService proxy = ApiClient.createProxy(IFlowDefinationService.class);
            Map<String, Object> userMap = new HashMap<String, Object>();//UserTask_1_Normal
//            userMap.put("UserTask_1_Normal", startUserId);
//            userMap.put("UserTask_2_Normal", startUserId);
//            userMap.put("UserTask_3_Normal", startUserId);
            FlowStartVO flowStartVO = new FlowStartVO();
            flowStartVO.setBusinessKey(businessKey);
            flowStartVO.setBusinessModelCode(businessModelCode);
            flowStartVO.setFlowTypeId(typeId);
            if (StringUtils.isNotEmpty(taskList)) {
                JSONArray jsonArray = JSONArray.fromObject(taskList);//把String转换为json
                flowTaskCompleteList = (List<FlowTaskCompleteWebVO>) JSONArray.toCollection(jsonArray, FlowTaskCompleteWebVO.class);

                if(flowTaskCompleteList!=null && !flowTaskCompleteList.isEmpty()){
                    for (FlowTaskCompleteWebVO f : flowTaskCompleteList) {
                        String flowTaskType = f.getFlowTaskType();
                        if ("common".equalsIgnoreCase(flowTaskType)||"approve".equalsIgnoreCase(flowTaskType)) {
                            userMap.put(f.getUserVarName(), f.getUserIds());
                        } else {
                            String[] idArray = f.getUserIds().split(",");
                            userMap.put(f.getUserVarName(), idArray);
                        }
                    }
                }
            }
            flowStartVO.setUserMap(userMap);
            FlowStartResultVO flowStartResultVO = proxy.startByVO(flowStartVO);
            if (flowStartResultVO != null) {
               if( flowStartResultVO.getFlowInstance()!=null){
                   defaultBusinessModel = (V) baseService.findOne(businessKey);
                   if(flowStartResultVO.getFlowInstance().isEnded()){
                       defaultBusinessModel.setFlowStatus(FlowStatus.COMPLETED);
                   }else {
                       defaultBusinessModel.setFlowStatus(FlowStatus.INPROCESS);
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
     * 签收任务
     * @param taskId  任务id
     * @return
     */
    @RequestMapping(value = "listFlowTask")
    @ResponseBody
    public String claimTask(String taskId){
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        String userId = ContextUtil.getUserId();
        OperateResult result =  proxy.claim(taskId,userId);
        OperateStatus operateStatus = new OperateStatus(result.successful(), result.getMessage());
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
    public String completeTask(String taskId, String businessId, String opinion, String taskList,String endEventId,boolean manualSelected,String approved ) {
        List<FlowTaskCompleteWebVO> flowTaskCompleteList = null;
        if (StringUtils.isNotEmpty(taskList)) {
            JSONArray jsonArray = JSONArray.fromObject(taskList);//把String转换为json
            flowTaskCompleteList = (List<FlowTaskCompleteWebVO>) JSONArray.toCollection(jsonArray, FlowTaskCompleteWebVO.class);
        }
        IBaseService baseService = ApiClient.createProxy(apiClass);
        OperateStatus operateStatus = null;
        V defaultBusinessModel = (V) baseService.findOne(businessId);
        if (defaultBusinessModel != null) {
            FlowTaskCompleteVO flowTaskCompleteVO = new FlowTaskCompleteVO();
            flowTaskCompleteVO.setTaskId(taskId);
            flowTaskCompleteVO.setOpinion(opinion);
            List<String> selectedNodeIds = new ArrayList<String>();
            Map<String, Object> v = new HashMap<String, Object>();
            if(flowTaskCompleteList!=null && !flowTaskCompleteList.isEmpty()){
                for (FlowTaskCompleteWebVO f : flowTaskCompleteList) {
                    selectedNodeIds.add(f.getNodeId());
                    String flowTaskType = f.getFlowTaskType();
                    if ("common".equalsIgnoreCase(flowTaskType)||"approve".equalsIgnoreCase(flowTaskType)) {
                        v.put(f.getUserVarName(), f.getUserIds());
                    } else {
                        String[] idArray = f.getUserIds().split(",");
                        v.put(f.getUserVarName(), idArray);
                    }
                }
            }else{
                if(StringUtils.isNotEmpty(endEventId)){
                    selectedNodeIds.add(endEventId);
                }
            }
            if(manualSelected){
                flowTaskCompleteVO.setManualSelectedNodeIds(selectedNodeIds);
            }

            //  Map<String,Object> v = new HashMap<String,Object>();
            v.put("approved",approved);//针对会签时同意、不同意、弃权等操作
            flowTaskCompleteVO.setVariables(v);
            IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
            OperateResultWithData<FlowStatus> operateResult = proxy.complete(flowTaskCompleteVO);
            if (FlowStatus.COMPLETED.toString().equalsIgnoreCase(operateResult.getData() + "")) {
                defaultBusinessModel = (V) baseService.findOne(businessId);
                defaultBusinessModel.setFlowStatus(FlowStatus.COMPLETED);
                baseService.save(defaultBusinessModel);
            }
            operateStatus = new OperateStatus(true, operateResult.getMessage());
        } else {
            operateStatus = new OperateStatus(false, "业务对象不存在");
        }
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
    public String rollBackTo(String preTaskId, String opinion) throws CloneNotSupportedException{
        OperateStatus operateStatus = null;
        IFlowTaskService proxy = ApiClient.createProxy(IFlowTaskService.class);
        OperateResult result = proxy.rollBackTo(preTaskId,opinion);
        operateStatus = new OperateStatus(result.successful(), result.getMessage());
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
        operateStatus = new OperateStatus(result.successful(), result.getMessage());
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
            operateStatus = new OperateStatus(false, "任务不存在，可能已经被处理");
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
    public String getSelectedNodesInfo(String taskId,String approved, String includeNodeIdsStr) throws NoSuchMethodException {
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
            operateStatus = new OperateStatus(false, "任务不存在，可能已经被处理");
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
            operateStatus.setData(approvalHeaderVO);
        } else {
            operateStatus = new OperateStatus(false, "任务不存在，可能已经被处理");
        }
        return JsonUtil.serialize(operateStatus);
    }



    /**
     *  仅针对跨业务实体子任务的测试初始化方法
     * @param defaultBusinessModelList
     * @param defaultBusinessModel2List
     * @param defaultBusinessModel3List
     * @param flowStatus
     */
    protected void initCallActivityBusinessStatus(List<DefaultBusinessModel> defaultBusinessModelList, List<DefaultBusinessModel2> defaultBusinessModel2List, List<DefaultBusinessModel3> defaultBusinessModel3List, FlowStatus flowStatus) {
        IDefaultBusinessModelService defaultBusinessModelService = ApiClient.createProxy(IDefaultBusinessModelService.class);
        IDefaultBusinessModel2Service defaultBusinessModel2Service = ApiClient.createProxy(IDefaultBusinessModel2Service.class);
        IDefaultBusinessModel3Service defaultBusinessModel3Service = ApiClient.createProxy(IDefaultBusinessModel3Service.class);
        if (!defaultBusinessModelList.isEmpty()) {
            for (DefaultBusinessModel defaultBusinessModel : defaultBusinessModelList) {
                defaultBusinessModel.setFlowStatus(flowStatus);
                defaultBusinessModelService.save(defaultBusinessModel);
            }
        }
        if (!defaultBusinessModel2List.isEmpty()) {
            for (DefaultBusinessModel2 defaultBusinessModel2 : defaultBusinessModel2List) {
                defaultBusinessModel2.setFlowStatus(flowStatus);
                defaultBusinessModel2Service.save(defaultBusinessModel2);
            }
        }
        if (!defaultBusinessModel3List.isEmpty()) {
            for (DefaultBusinessModel3 defaultBusinessModel3 : defaultBusinessModel3List) {
                defaultBusinessModel3.setFlowStatus(flowStatus);
                defaultBusinessModel3Service.save(defaultBusinessModel3);
            }
        }
    }

    /**
     * 仅针对跨业务实体子任务的测试初始化方法
     * @param defaultBusinessModelList
     * @param defaultBusinessModel2List
     * @param defaultBusinessModel3List
     * @param callActivityPathMap
     * @param variables
     * @param parentBusinessModel
     */
    protected void initCallActivityBusiness(List<DefaultBusinessModel> defaultBusinessModelList, List<DefaultBusinessModel2> defaultBusinessModel2List, List<DefaultBusinessModel3> defaultBusinessModel3List, Map<String, String> callActivityPathMap, Map<String, Object> variables, IBusinessFlowEntity parentBusinessModel) {
        IDefaultBusinessModelService defaultBusinessModelService = ApiClient.createProxy(IDefaultBusinessModelService.class);
        IDefaultBusinessModel2Service defaultBusinessModel2Service = ApiClient.createProxy(IDefaultBusinessModel2Service.class);
        IDefaultBusinessModel3Service defaultBusinessModel3Service = ApiClient.createProxy(IDefaultBusinessModel3Service.class);

        IFlowDefinationService flowDefinationService = ApiClient.createProxy(IFlowDefinationService.class);

        for (Map.Entry<String, String> entry : callActivityPathMap.entrySet()) {
            String realDefiniationKey = entry.getValue();
            String realPathKey = entry.getKey();
            FlowDefination flowDefination = flowDefinationService.findByKey(realDefiniationKey);
            String sonBusinessModelCode = flowDefination.getFlowType().getBusinessModel().getClassName();
            if ("com.ecmp.flow.entity.DefaultBusinessModel".equals(sonBusinessModelCode)) {
                DefaultBusinessModel defaultBusinessModel = new DefaultBusinessModel();
                BeanUtils.copyProperties(parentBusinessModel, defaultBusinessModel);
                String name = "temp_测试跨业务实体子流程_默认业务实体" + System.currentTimeMillis();
                defaultBusinessModel.setName(name);
                defaultBusinessModel.setFlowStatus(FlowStatus.INIT);
                defaultBusinessModel.setWorkCaption(parentBusinessModel.getWorkCaption()+"||"+name);
                defaultBusinessModel.setId(null);
                defaultBusinessModel.setBusinessCode(null);
                OperateResultWithData<DefaultBusinessModel> resultWithData = defaultBusinessModelService.save(defaultBusinessModel);
                String defaultBusinessModelId = resultWithData.getData().getId();
                variables.put(realPathKey, defaultBusinessModelId);
                defaultBusinessModel = resultWithData.getData();
                defaultBusinessModelList.add(defaultBusinessModel);
            } else if ("com.ecmp.flow.entity.DefaultBusinessModel2".equals(sonBusinessModelCode)) {
                DefaultBusinessModel2 defaultBusinessModel2Son = new DefaultBusinessModel2();
                BeanUtils.copyProperties(parentBusinessModel, defaultBusinessModel2Son);
                String name = "temp_测试跨业务实体子流程_采购实体" + System.currentTimeMillis();
                defaultBusinessModel2Son.setName(name);
                defaultBusinessModel2Son.setFlowStatus(FlowStatus.INIT);
                defaultBusinessModel2Son.setWorkCaption(parentBusinessModel.getWorkCaption()+"||"+name);
                defaultBusinessModel2Son.setId(null);
                defaultBusinessModel2Son.setBusinessCode(null);
                OperateResultWithData<DefaultBusinessModel2> resultWithData = defaultBusinessModel2Service.save(defaultBusinessModel2Son);
                String defaultBusinessModelId = resultWithData.getData().getId();
                variables.put(realPathKey, defaultBusinessModelId);
                defaultBusinessModel2Son = resultWithData.getData();
                defaultBusinessModel2List.add(defaultBusinessModel2Son);
            } else if ("com.ecmp.flow.entity.DefaultBusinessModel3".equals(sonBusinessModelCode)) {
                DefaultBusinessModel3 defaultBusinessModel3Son = new DefaultBusinessModel3();
                BeanUtils.copyProperties(parentBusinessModel, defaultBusinessModel3Son);
                String name = "temp_测试跨业务实体子流程_销售实体" + System.currentTimeMillis();
                defaultBusinessModel3Son.setName(name);
                defaultBusinessModel3Son.setFlowStatus(FlowStatus.INIT);
                defaultBusinessModel3Son.setWorkCaption(parentBusinessModel.getWorkCaption()+"||"+name);
                defaultBusinessModel3Son.setId(null);
                defaultBusinessModel3Son.setBusinessCode(null);
                OperateResultWithData<DefaultBusinessModel3> resultWithData = defaultBusinessModel3Service.save(defaultBusinessModel3Son);
                String defaultBusinessModelId = resultWithData.getData().getId();
                variables.put(realPathKey, defaultBusinessModelId);
                defaultBusinessModel3Son = resultWithData.getData();
                defaultBusinessModel3List.add(defaultBusinessModel3Son);
            }
        }
    }

    /**
     * 解析子流程绝对路径
     * @param callActivityPath 路径值
     * @return 路径值为key，子流程id为value的MAP对象
     */
    protected Map<String, String> initCallActivtiy(String callActivityPath) {
        Map<String, String> resultMap = new HashMap<String, String>();
        //  String str ="/caigouTestZhu/CallActivity_3/yewushengqing2";
        String str = callActivityPath;
        String[] resultArray = str.split("/");
        if ((resultArray.length < 4) || (resultArray.length % 2 != 0)) {
            throw new RuntimeException("子流程路径解析错误");
        }
        List<String> resultList = new ArrayList<String>();
        for (int i = 1; i < resultArray.length; i++) {
            resultList.add(resultArray[i]);
        }
        int size = resultList.size();
        for (int j = 1; j < size; ) {
            String key = resultList.get(size - j);
            int endIndex = str.lastIndexOf(key) + key.length();
            String path = str.substring(0, endIndex);
            resultMap.put(path, key);
            j += 2;
        }
        return resultMap;
    }

}

