package com.ecmp.flow.util;

import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.flow.basic.vo.Executor;
import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.dao.FlowDefVersionDao;
import com.ecmp.flow.dao.FlowHistoryDao;
import com.ecmp.flow.dao.FlowInstanceDao;
import com.ecmp.flow.dao.FlowServiceUrlDao;
import com.ecmp.flow.entity.*;
import com.ecmp.flow.service.FlowHistoryService;
import com.ecmp.flow.service.FlowTaskService;
import com.ecmp.flow.vo.FlowInvokeParams;
import com.ecmp.flow.vo.FlowOperateResult;
import com.ecmp.flow.vo.NodeInfo;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.log.util.LogUtil;
import com.ecmp.util.JsonUtils;
import net.sf.json.JSONObject;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.GenericType;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能： 流程监听工具类
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/12/19 16:04      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
//@Component
public class FlowListenerTool {

    @Autowired
    private FlowDefVersionDao flowDefVersionDao;

    @Autowired
   private FlowTaskService flowTaskService;

    @Autowired
    FlowHistoryDao flowHistoryDao;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private FlowTaskTool flowTaskTool;

    @Autowired
    private FlowInstanceDao flowInstanceDao;

    @Autowired
    private FlowServiceUrlDao flowServiceUrlDao;

    private final Logger logger = LoggerFactory.getLogger(FlowListenerTool.class);


    //选择下一步执行人
    public  List<NodeInfo>  nextNodeInfoList(FlowTask flowTask,DelegateExecution delegateTask) throws NoSuchMethodException{
        List<NodeInfo> nodeInfoList = flowTaskService.findNexNodesWithUserSet(flowTask);
        List<NodeInfo> results = null;
        results = nodeInfoList;
        FlowInstance parentFlowInstance = flowTask.getFlowInstance().getParent();
        FlowTask flowTaskTempSrc = new FlowTask();
        org.springframework.beans.BeanUtils.copyProperties(flowTask,flowTaskTempSrc);
        //针对子流程结束，循环向上查找父任务下一步的节点执行人信息
        ProcessInstance instanceSon = ((ExecutionEntity) delegateTask).getProcessInstance();
        while (instanceSon!=null && parentFlowInstance != null&&nodeInfoList != null && !nodeInfoList.isEmpty()&& nodeInfoList.size()==1&&"EndEvent".equalsIgnoreCase(nodeInfoList.get(0).getType()) ){//针对子流程结束节点
            FlowTask flowTaskTemp = new FlowTask();
            org.springframework.beans.BeanUtils.copyProperties(flowTaskTempSrc,flowTaskTemp);
            flowTaskTemp.setFlowInstance(parentFlowInstance);
            // 取得父流程实例
            ExecutionEntity superExecution =instanceSon.getSuperExecution();
            if (superExecution != null) {
                String activityId = superExecution.getActivityId();
                flowTaskTemp.setActTaskKey(activityId);
                flowTaskTemp.setActTaskDefKey(activityId);
                String flowDefJsonP = parentFlowInstance.getFlowDefVersion().getDefJson();
                JSONObject defObjP = JSONObject.fromObject(flowDefJsonP);
                Definition definitionP = (Definition) JSONObject.toBean(defObjP, Definition.class);
                JSONObject currentNodeP = definitionP.getProcess().getNodes().getJSONObject(activityId);
                flowTaskTemp.setTaskJsonDef(currentNodeP.toString());
                results = flowTaskService.findNexNodesWithUserSet( flowTaskTemp);
            }
            parentFlowInstance=parentFlowInstance.getParent();
            nodeInfoList=results;
            flowTaskTempSrc =flowTaskTemp;
            instanceSon=superExecution.getProcessInstance();
        }
        return results;
    }

    public List<String> getCallActivitySonPaths(FlowTask flowTask) throws NoSuchMethodException{
        List<NodeInfo> nodeInfoList = flowTaskService.findNexNodesWithUserSet(flowTask);
        List<String> paths = new ArrayList<String>();
        if(nodeInfoList!=null && !nodeInfoList.isEmpty()){
            for(NodeInfo nodeInfo :nodeInfoList){
                if(StringUtils.isNotEmpty(nodeInfo.getCallActivityPath())){
                    paths.add(nodeInfo.getCallActivityPath());
                }
            }
        }
        return paths;
    }

    public List<NodeInfo>  initNodeUsers(List<NodeInfo> results,DelegateExecution delegateTask,String actTaskDefKey){
        List<NodeInfo> nextNodes = new ArrayList<NodeInfo>();
        if(results !=null &&  !results.isEmpty()){
            Map<String,Object> userVarNameMap = new HashMap<>();
            List<String> userVarNameList = null;
            for(NodeInfo nodeInfo:results){
                if ("EndEvent".equalsIgnoreCase(nodeInfo.getType())) {
                    nodeInfo.setType("EndEvent");
                    continue;
                }else if("ServiceTask".equalsIgnoreCase(nodeInfo.getType())){//服务任务也不做处理
                    continue;
                }
                nextNodes.add(nodeInfo);
                String taskType = nodeInfo.getFlowTaskType();
                String uiUserType = nodeInfo.getUiUserType();
                String callActivityPath = nodeInfo.getCallActivityPath();
                String varUserName = nodeInfo.getUserVarName();

                if (StringUtils.isNotEmpty(callActivityPath)) {
                    userVarNameList = (List<String>) userVarNameMap.get(callActivityPath+"_sonProcessSelectNodeUserV");
                    if(userVarNameList==null){
                        userVarNameList = new ArrayList<String>();
                        userVarNameMap.put(callActivityPath+"_sonProcessSelectNodeUserV",userVarNameList);//选择的变量名,子流程存在选择了多个的情况
                    }
                    userVarNameList.add(varUserName);
                }
                if("AnyOne".equalsIgnoreCase(uiUserType)){//任意执行人默认规则为当前执行人
//                                IEmployeeService proxy = ApiClient.createProxy(IEmployeeService.class);
                    String currentUserId = ContextUtil.getUserId();
                    List<String> usrIdList = new ArrayList<String>(1);
                    usrIdList.add(currentUserId);
//                                List<Executor> employees = proxy.getExecutorsByEmployeeIds(usrIdList);
                    Map<String,Object> params = new HashMap();
                    params.put("employeeIds",usrIdList);
                    String url = Constants.getBasicEmployeeFindbyorganizationidUrl();
                    List<Executor> employees= ApiClient.getEntityViaProxy(url,new GenericType<List<Executor>>() {},params);
                    Set<Executor> employeeSet = new HashSet<Executor>();
                    employeeSet.addAll(employees);
                    nodeInfo.setExecutorSet(employeeSet);
                }
                if ("SingleSign".equalsIgnoreCase(taskType) || "CounterSign".equalsIgnoreCase(taskType)||"ParallelTask".equalsIgnoreCase(taskType)||"SerialTask".equalsIgnoreCase(taskType)) {
                    Set<Executor> executorSet = nodeInfo.getExecutorSet();
                    if(executorSet != null && !executorSet.isEmpty()){
                        List<String> userIdArray = new ArrayList<String>();
                        for(Executor executor:executorSet){
                            userIdArray.add(executor.getId());
                        }
                        if (StringUtils.isNotEmpty(callActivityPath)) {
                            userVarNameMap.put(callActivityPath+"/"+varUserName, userIdArray);
                        }else {
                            userVarNameMap.put(varUserName, userIdArray);
                        }
                    }
                }else {
                    Set<Executor> executorSet = nodeInfo.getExecutorSet();
                    if(executorSet != null && !executorSet.isEmpty()){
                        String userId = ((Executor)executorSet.toArray()[0]).getId();
                        if (StringUtils.isNotEmpty(callActivityPath)) {
                            userVarNameMap.put(callActivityPath+"/"+varUserName, userId);
                        }else {
                            userVarNameMap.put(varUserName, userId);
                        }
                    }
                }
            }
            runtimeService.setVariable(delegateTask.getProcessInstanceId(),actTaskDefKey+"_nextNodeIds",  nextNodes);
            runtimeService.setVariables(delegateTask.getProcessInstanceId(),userVarNameMap);
        }

        return nextNodes;
    }

    public void initNextAllTask(FlowInstance flowInstance,FlowHistory flowHistory){
        Calendar startTreadTime =  Calendar.getInstance();
        ScheduledExecutorService service = Executors
                .newSingleThreadScheduledExecutor();
        Runnable runnable = new Runnable() {
            public void run() {
                Calendar nowTime = Calendar.getInstance();
                nowTime.add(Calendar.MINUTE, -2);//不能超过2分钟
                if(nowTime.after(startTreadTime)){
                    service.shutdown();
                }
                flowTaskTool.initTask(flowInstance,flowHistory,null,null);
            }
        };
        // 第二个参数为首次执行的延时时间，第三个参数为定时执行的间隔时间
        service.scheduleWithFixedDelay(runnable, 1, 20, TimeUnit.SECONDS);
    }

    public void initNextAllTask(List<NodeInfo> nextNodes,ExecutionEntity taskEntity,FlowHistory flowHistory){
        if(!nextNodes.isEmpty()){
            ExecutionEntity parent = taskEntity.getSuperExecution();
            if(parent!=null){//针对作为子任务的情况
                ExecutionEntity parentTemp = parent;
                ProcessInstance parentProcessInstance = null;
                ExecutionEntity zhuzhongEntity = parentTemp;
                while (parentTemp!=null){
                    parentProcessInstance = parentTemp.getProcessInstance();
                    zhuzhongEntity =parentTemp;
                    parentTemp = ((ExecutionEntity)parentProcessInstance).getSuperExecution();
                }
                FlowInstance flowInstanceZhu = flowInstanceDao.findByActInstanceId(zhuzhongEntity.getProcessInstanceId());
                new Thread(new Runnable() {//异步
                    @Override
                    public void run() {
                        initNextAllTask(flowInstanceZhu,flowHistory);//初始化相关联的所有待办任务
                    }
                }).start();
            }else{
                new Thread(new Runnable() {//异步
                    @Override
                    public void run() {
                        FlowInstance flowInstance = flowHistory.getFlowInstance();
                        initNextAllTask(flowInstance,flowHistory);
                    }
                }).start();
            }
        }
    }


    public void callEndService(String businessKey, FlowDefVersion flowDefVersion, int endSign){
        if(flowDefVersion!=null && StringUtils.isNotEmpty(businessKey)){
            String endCallServiceUrlId = flowDefVersion.getEndCallServiceUrlId();
            if(StringUtils.isNotEmpty(endCallServiceUrlId)){
                FlowServiceUrl flowServiceUrl = flowServiceUrlDao.findOne(endCallServiceUrlId);
                String checkUrl = flowServiceUrl.getUrl();
                if(StringUtils.isNotEmpty(checkUrl)){
//                    String baseUrl= flowDefVersion.getFlowDefination().getFlowType().getBusinessModel().getAppModule().getApiBaseAddress();
                    String apiBaseAddressConfig = flowDefVersion.getFlowDefination().getFlowType().getBusinessModel().getAppModule().getApiBaseAddress();
                    String baseUrl =  ContextUtil.getGlobalProperty(apiBaseAddressConfig);
                    String endCallServiceUrlPath = baseUrl+checkUrl;
                    FlowInvokeParams flowInvokeParams = new FlowInvokeParams();
                    flowInvokeParams.setId(businessKey);
                    Map<String,String> params = new HashMap<String,String>();
                    params.put("endSign",endSign+"");
                    flowInvokeParams.setParams(params);
                    new Thread(new Runnable() {//模拟异步
                        @Override
                        public void run() {
                            FlowOperateResult flowOperateResult =   ApiClient.postViaProxyReturnResult(endCallServiceUrlPath,new GenericType<FlowOperateResult>() {},flowInvokeParams);
                            logger.info(flowOperateResult.toString());
                            new Thread(new Runnable() {//模拟异步
                                @Override
                                public void run() {
                                    LogUtil.addExceptionLog("businessKey="+businessKey+";endSign="+endSign+";"+flowOperateResult.toString());
                                }
                            }).start();
                        }
                    }).start();
                }
            }
        }
    }

    /**
     *  流程即将结束时调用服务检查，如果失败流程结束失败，同步
     * @param businessKey
     * @param flowDefVersion
     * @return
     */
    public FlowOperateResult callBeforeEnd(String businessKey, FlowDefVersion flowDefVersion,int endSign){
        FlowOperateResult result = null;
        if(flowDefVersion!=null && StringUtils.isNotEmpty(businessKey)){
            String endBeforeCallServiceUrlId = flowDefVersion.getEndBeforeCallServiceUrlId();

            if(StringUtils.isNotEmpty(endBeforeCallServiceUrlId)){
                FlowServiceUrl flowServiceUrl = flowServiceUrlDao.findOne(endBeforeCallServiceUrlId);
                String checkUrl = flowServiceUrl.getUrl();
                if(StringUtils.isNotEmpty(checkUrl)){
                    String apiBaseAddressConfig = flowDefVersion.getFlowDefination().getFlowType().getBusinessModel().getAppModule().getApiBaseAddress();
                    String baseUrl =  ContextUtil.getGlobalProperty(apiBaseAddressConfig);
                    String checkUrlPath = baseUrl+checkUrl;
                    FlowInvokeParams flowInvokeParams = new FlowInvokeParams();
                    flowInvokeParams.setId(businessKey);
                    Map<String,String> params = new HashMap<String,String>();
                    params.put("endSign",endSign+"");
                    flowInvokeParams.setParams(params);
                    result = ApiClient.postViaProxyReturnResult(checkUrlPath,new GenericType<FlowOperateResult>() {},flowInvokeParams);
                }
            }
        }
        return result;
    }

    public void taskEventServiceCall(DelegateExecution delegateTask,boolean async, String beforeExcuteServiceId ,String businessId){
        try {
            String multiInstance =  (String)((ExecutionEntity) delegateTask).getActivity().getProperty("multiInstance");
            Boolean isMmultiInstance = StringUtils.isNotEmpty(multiInstance);
            if(isMmultiInstance){//控制会签任务、串行任务、并行任务 所有执行完成时只触发一次完成事件（可能后续需要扩展控制）
                TransitionImpl transiton =  ((ExecutionEntity) delegateTask).getTransition();
                if(transiton==null){
                    return;
                }
            }
            String actProcessDefinitionId = delegateTask.getProcessDefinitionId();
            FlowDefVersion flowDefVersion = flowDefVersionDao.findByActDefId(actProcessDefinitionId);
            String flowDefJson = flowDefVersion.getDefJson();
            JSONObject defObj = JSONObject.fromObject(flowDefJson);
            Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);

            Map<String,Object> tempV = delegateTask.getVariables();
            Map<String,List<String>> selectedNodesUserMap = (Map<String, List<String>>) tempV.get("selectedNodesUserMap");
            Map<String,List<String>> selectedNodesUserMapNew = new HashMap<>();
            if(selectedNodesUserMap!=null && !selectedNodesUserMap.isEmpty()){//如果配置了自定义code，则进行替换
               for(Map.Entry<String,List<String>> temp:selectedNodesUserMap.entrySet()){
                  String actTaskDefKey = temp.getKey();
                  JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(actTaskDefKey);
                  JSONObject normalInfo = currentNode.getJSONObject("nodeConfig").getJSONObject("normal");
                   if(normalInfo!=null && !normalInfo.isEmpty() ){
                      String nodeCode = normalInfo.get("nodeCode")!=null?(String)normalInfo.get("nodeCode"):null;
                      if(StringUtils.isEmpty(nodeCode)){
                          nodeCode = actTaskDefKey;
                      }
                       selectedNodesUserMapNew.put(nodeCode,temp.getValue());
                   }
               }
                tempV.put("selectedNodesUserMap",selectedNodesUserMapNew);
            }
            String param = JsonUtils.toJson(tempV);
            if(async){
                new Thread(new Runnable() {//模拟异步
                    @Override
                    public void run() {
                        ServiceCallUtil.callService(beforeExcuteServiceId, businessId, param);
                    }
                }).start();
            }else {
                FlowOperateResult flowOperateResult = null;
                String callMessage = null;
                try{
                    flowOperateResult = ServiceCallUtil.callService(beforeExcuteServiceId, businessId, param);
                     callMessage = flowOperateResult!=null? flowOperateResult.getMessage():"接口调用异常！";
                }catch (Exception e){
                    logger.error(e.getMessage());
                    flowOperateResult=null;
                    callMessage = e.getMessage();
                }

                if((flowOperateResult==null || !flowOperateResult.isSuccess())){
//                        ExecutionEntity taskEntity = (ExecutionEntity) delegateTask;
//                        TransitionImpl transition = taskEntity.getTransition();
//                        String sourceType =transition.getSource().getProperties().get("type")+"";

//                        if("startEvent".equalsIgnoreCase(sourceType)){ //如果是开始节点，手动回滚
                    String actProcessInstanceId = delegateTask.getProcessInstanceId();
                    FlowInstance flowInstance = flowInstanceDao.findByActInstanceId(actProcessInstanceId);
                    List<FlowTask> flowTaskList = flowTaskService.findByInstanceId(flowInstance.getId());
                    List<FlowHistory> flowHistoryList = flowHistoryDao.findByInstanceId(flowInstance.getId());

                    if(flowTaskList.isEmpty()&&flowHistoryList.isEmpty()){ //如果是开始节点，手动回滚
//                            String actProcessInstanceId = delegateTask.getProcessInstanceId();
//                            FlowInstance flowInstance = flowInstanceDao.findByActInstanceId(actProcessInstanceId);
//                            String actProcessDefinitionId = delegateTask.getProcessDefinitionId();
//                            FlowDefVersion flowDefVersion = flowDefVersionDao.findByActDefId(actProcessDefinitionId);
                            AppModule appModule = flowDefVersion.getFlowDefination().getFlowType().getBusinessModel().getAppModule();
                            new Thread(){
                                public void run(){
                                    BusinessModel businessModel = flowInstance.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel();
                                    Map<String, Object> params = new HashMap<String,Object>();;
                                    params.put(Constants.BUSINESS_MODEL_CODE,businessModel.getClassName());
                                    params.put(Constants.ID,flowInstance.getBusinessId());
                                    params.put(Constants.STATUS, FlowStatus.INIT);
                                    String apiBaseAddressConfig = appModule.getApiBaseAddress();
                                    String baseUrl =  ContextUtil.getGlobalProperty(apiBaseAddressConfig);
                                    String url = baseUrl+"/"+businessModel.getConditonStatusRest();
                                    Boolean result = false;
                                    int index = 5;
                                    while (!result && index>0){
                                        try {
                                            Thread.sleep(1000*(6-index));
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        try {
                                            result =  ApiClient.postViaProxyReturnResult(url,new GenericType<Boolean>() {}, params);
                                        }catch (Exception e){
                                            logger.error(e.getMessage());
                                        }
                                        index--;
                                    }
//                                String actInstanceId = flowInstance.getActInstanceId();
//                                runtimeService.deleteProcessInstance(actInstanceId, null);
//                                flowInstanceDao.delete(flowInstance);
                                }
                            }.start();
                    }
                    throw new FlowException(callMessage);//抛出异常
                }
            }
        }catch (Exception e){
            logger.error(e.getMessage());
            if(!async){
                throw e;
            }
            new Thread(new Runnable() {//模拟异步
                @Override
                public void run() {
                    LogUtil.addExceptionLog(e.getMessage());
                }
            }).start();
        }
    }
}
