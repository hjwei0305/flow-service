package com.ecmp.flow.service;

import com.ecmp.basic.api.IEmployeeService;
import com.ecmp.basic.api.IPositionService;
import com.ecmp.basic.entity.User;
import com.ecmp.basic.entity.vo.Executor;
import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.core.service.BaseService;
import com.ecmp.flow.api.IFlowDefinationService;
import com.ecmp.flow.util.ConditionUtil;
import com.ecmp.flow.util.ExpressionUtil;
import com.ecmp.flow.util.TaskStatus;
import com.ecmp.flow.dao.*;
import com.ecmp.flow.entity.*;
import com.ecmp.flow.vo.FlowStartResultVO;
import com.ecmp.flow.vo.FlowStartVO;
import com.ecmp.flow.vo.NodeInfo;
import com.ecmp.flow.vo.bpmn.BaseFlowNode;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.flow.vo.bpmn.StartEvent;
import com.ecmp.flow.vo.bpmn.UserTask;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/3/23 22:39      谭军(tanjun)               新建
 * <p/>
 * *************************************************************************************************
 */
@Service
@Transactional
public class FlowDefinationService extends BaseEntityService<FlowDefination> implements IFlowDefinationService {

    private final Logger logger = LoggerFactory.getLogger(FlowDefinationService.class);

    protected BaseEntityDao<FlowDefination> getDao() {
        return this.flowDefinationDao;
    }

    @Autowired
    private FlowDefVersionDao flowDefVersionDao;

    @Autowired
    private FlowDefinationDao flowDefinationDao;

    @Autowired
    private FlowInstanceDao flowInstanceDao;

    @Autowired
    private FlowTaskDao flowTaskDao;


    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private FlowTypeDao flowTypeDao;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private BusinessModelDao businessModelDao;

    /**
     * 新增修改操作
     *
     * @param entity
     * @return
     */
    @Override
    public OperateResultWithData<FlowDefination> save(FlowDefination entity) {
        FlowDefVersion flowDefVersion = entity.getCurrentFlowDefVersion();
        boolean isNew = entity.isNew();
        if (isNew) {
            preInsert(entity);
        } else {
            preUpdate(entity);
        }
        flowDefinationDao.save(entity);
        logger.debug("Saved FlowDefination id is {}", entity.getId());
        if (flowDefVersion != null) {
            flowDefVersion.setFlowDefination(entity);
            flowDefVersionDao.save(flowDefVersion);
            logger.debug("Saved FlowDefVersion id is {}", entity.getId());
            entity.setLastVersionId(flowDefVersion.getId());
            flowDefinationDao.save(entity);
            logger.debug("Saved FlowDefination id is {}", entity.getId());
        }
        OperateResultWithData<FlowDefination> operateResult;
        if (isNew) {
            operateResult = OperateResultWithData.OperationSuccess("core_00001");
        } else {
            operateResult = OperateResultWithData.OperationSuccess("core_00002");
        }
        operateResult.setData(entity);
        return operateResult;
    }

//    public FlowDefination save(FlowDefination entity) {
//        //        entity.setLastVersionId(0);
//        FlowDefVersion flowDefVersion = entity.getCurrentFlowDefVersion();
//
//        if (entity.isNew()) {
//            preInsert(entity);
//        } else {
//            preUpdate(entity);
//        }
//        flowDefinationDao.save(entity);
//        if(flowDefVersion!=null){
//            flowDefVersion.setFlowDefination(entity);
//            flowDefVersionDao.save(flowDefVersion);
//            entity.setLastVersionId(flowDefVersion.getId());
//            flowDefinationDao.save(entity);
//        }
//        logger.debug("Saved FlowDefination id is {}", entity.getId());
//        return entity;
//    }

    /**
     * 通过流程定义ID发布流程
     *
     * @param id
     * @return 流程发布ID
     */
    @Override
    @Transactional
    public String deployById(String id) throws UnsupportedEncodingException {
        String deployId = null;
        FlowDefination flowDefination = flowDefinationDao.findOne(id);
        if (flowDefination != null) {
            String versionId = flowDefination.getLastVersionId();
            deployId = deployByVersionId(versionId);
        }
        return deployId;
    }

    /**
     * 通过流程版本ID发布流程
     *
     * @param id
     * @return 流程发布ID
     */
    @Override
//    @Transactional
    public String deployByVersionId(String id) throws UnsupportedEncodingException {
        String deployId = null;
        FlowDefVersion flowDefVersion = flowDefVersionDao.findOne(id);
        Deployment deploy = null;
        deploy = this.deploy(flowDefVersion.getName(), flowDefVersion.getDefXml());
        deployId = deploy.getId();
        flowDefVersion.setActDeployId(deployId);//回写流程发布ID
        ProcessDefinitionEntity activtiFlowDef = getProcessDefinitionByDeployId(deployId);
        flowDefVersion.setVersionCode(activtiFlowDef.getVersion());//回写版本号
        flowDefVersion.setActDefId(activtiFlowDef.getId());//回写引擎对应流程定义ID
        flowDefVersionDao.save(flowDefVersion);
        return deployId;
    }

    /**
     * 通过ID启动流程实体
     *
     * @param id          流程id
     * @param businessKey 业务KEY
     * @param variables   其他参数
     * @return 流程实例
     */
    @Override
    public FlowInstance startById(String id, String businessKey, Map<String, Object> variables) {
        return this.startById(id, null, businessKey, variables);
    }

    /**
     * 通过ID启动流程实体
     *
     * @param id          流程id
     * @param startUserId 流程启动人
     * @param businessKey 业务KEY
     * @param variables   其他参数
     * @return 流程实例
     */
    @Override
    public FlowInstance startById(String id, String startUserId, String businessKey, Map<String, Object> variables) {
        FlowInstance flowInstance = null;

        FlowDefination flowDefination = flowDefinationDao.findOne(id);
        if (flowDefination != null) {
            String versionId = flowDefination.getLastVersionId();
            FlowDefVersion flowDefVersion = flowDefVersionDao.findOne(versionId);
            if (flowDefVersion != null && flowDefVersion.getActDefId() != null) {
                String proessDefId = flowDefVersion.getActDefId();
                ProcessInstance processInstance = null;
                if ((startUserId != null) && (!"".equals(startUserId))) {
                    processInstance = this.startFlowById(proessDefId, startUserId, businessKey, variables);
                } else {
                    processInstance = this.startFlowById(proessDefId, businessKey, variables);
                }

                flowInstance = new FlowInstance();
                flowInstance.setBusinessId(processInstance.getBusinessKey());
                flowInstance.setFlowDefVersion(flowDefVersion);
                flowInstance.setStartDate(new Date());

//                String processInstanceName = processInstance.getName();
//                if(processInstanceName == null){
//                    processInstanceName = processInstance.getProcessDefinitionKey();
//                }
                flowInstance.setFlowName(flowDefVersion.getName() + ":" + businessKey);
                flowInstance.setActInstanceId(processInstance.getId());
                flowInstanceDao.save(flowInstance);
                initTask(processInstance);
            }
        }
        return flowInstance;
    }

    /**
     * 通过Key启动流程实体
     *
     * @param key         定义Key
     * @param businessKey 业务KEY
     * @param variables   其他参数
     * @return 流程实例
     */
    @Override
    public FlowInstance startByKey(String key, String businessKey, Map<String, Object> variables) {
        return this.startByKey(key, null, businessKey, variables);
//        return null;
    }

    /**
     * 通过Key启动流程实体
     *
     * @param key         定义Key
     * @param startUserId 流程启动人
     * @param businessKey 业务KEY
     * @param variables   其他参数
     * @return 流程实例
     */
    @Override
    public FlowInstance startByKey(String key, String startUserId, String businessKey, Map<String, Object> variables) {
        FlowInstance flowInstance = null;

        FlowDefination flowDefination = flowDefinationDao.findByDefKey(key);
        if (flowDefination != null) {
            String versionId = flowDefination.getLastVersionId();
            FlowDefVersion flowDefVersion = flowDefVersionDao.findOne(versionId);
            if (flowDefVersion != null && flowDefVersion.getActDefId() != null) {
//                String proessDefId = flowDefVersion.getActDefId();
                ProcessInstance processInstance = null;
                if ((startUserId != null) && (!"".equals(startUserId))) {
                    processInstance = this.startFlowByKey(key, startUserId, businessKey, variables);
                } else {
                    processInstance = this.startFlowByKey(key, businessKey, variables);
                }

                flowInstance = new FlowInstance();
                flowInstance.setBusinessId(processInstance.getBusinessKey());
                String workCaption = variables.get("workCaption")+"";//工作说明
                flowInstance.setBusinessModelRemark(workCaption);
                flowInstance.setFlowDefVersion(flowDefVersion);
                flowInstance.setStartDate(new Date());
                flowInstance.setFlowName(flowDefVersion.getName() + ":" + businessKey);
                flowInstance.setActInstanceId(processInstance.getId());
                flowInstanceDao.save(flowInstance);
                initTask(processInstance);
            }
        }
        return flowInstance;
    }

    /**
     * 路由流程定义
     * @param businessModelMap  业务单据数据Map
     * @param flowType  流程定义
     * @param orgCodes  代码路径数组
     * @param level   遍历层次
     * @return
     */
    private FlowDefination  flowDefLuYou(Map<String,Object> businessModelMap,FlowType flowType ,String[] orgCodes,int level) throws NoSuchMethodException, SecurityException{
        FlowDefination finalFlowDefination = null;
        String orgCode =orgCodes[orgCodes.length-level];
        List<FlowDefination> flowDefinationList = flowDefinationDao.findByTypeCodeAndOrgCode(flowType.getCode(),orgCode);
        Boolean ifAllNoStartUel = true;//是否所有的流程定义未配置启动UEL
        if(flowDefinationList!=null && flowDefinationList.size()>0){

                for(FlowDefination flowDefination:flowDefinationList){
                    String startUelStr = flowDefination.getStartUel();
                    if(!StringUtils.isEmpty(startUelStr)){
                        JSONObject startUelObject = JSONObject.fromObject(startUelStr);
                        String conditionText = startUelObject.getString("groovyUel");
                        if (conditionText != null) {
                            ifAllNoStartUel = false;
                            if (conditionText.startsWith("#{")) {// #{开头代表自定义的groovy表达式
                                String conditonFinal = conditionText.substring(conditionText.indexOf("#{") + 2,
                                        conditionText.lastIndexOf("}"));
                                if (ConditionUtil.groovyTest(conditonFinal, businessModelMap)) {
                                    finalFlowDefination = flowDefination;
                                    break;
                                }
                            } else {//其他的用UEL表达式验证
                                Object tempResult = ConditionUtil.uelResult(conditionText, businessModelMap);
                                if (tempResult instanceof Boolean) {
                                    Boolean resultB = (Boolean) tempResult;
                                    if (resultB == true) {
                                        finalFlowDefination = flowDefination;
                                        break;
                                    }
                                }
                            }
                        }

                    }
                }
                if(ifAllNoStartUel){
                    finalFlowDefination = flowDefinationList.get(0);
                }
        }
        if(finalFlowDefination == null) {//同级组织机构找不到流程定义，自动向上级组织机构查找所属类型的流程定义
            level++;
            if(level>orgCodes.length){
                return null;
            }
            return this.flowDefLuYou( businessModelMap, flowType, orgCodes , level);
        }
//       if(finalFlowDefination == null && flowDefinationList!=null && !flowDefinationList.isEmpty()){
//            finalFlowDefination = flowDefinationList.get(0);
//        }
        return finalFlowDefination;

    }
    private FlowInstance startByTypeCode( FlowType flowType, String startUserId, String businessKey, Map<String, Object> variables) throws NoSuchMethodException, SecurityException{
        // BusinessModel  businessModel = businessModelDao.findByProperty("className",businessModelCode);
//        FlowType   flowType = flowTypeDao.findByProperty("code","")
        //  typeCode="ecmp-flow-flowType2_1494902655299";

        //获取当前业务实体表单的条件表达式信息，（目前是任务执行时就注入，后期根据条件来优化)
        String businessId = businessKey;
        FlowDefination finalFlowDefination = null;
        if(startUserId==null){
            startUserId = ContextUtil.getUserId();
        }
        BusinessModel businessModel = flowType.getBusinessModel();
        String businessModelId = businessModel.getId();
        String appModuleId = businessModel.getAppModuleId();
        com.ecmp.basic.api.IAppModuleService proxy = ApiClient.createProxy(com.ecmp.basic.api.IAppModuleService.class);
        com.ecmp.basic.entity.AppModule appModule = proxy.findOne(appModuleId);
        String clientApiBaseUrl = ContextUtil.getAppModule(appModule.getCode()).getApiBaseAddress();
        Map<String, Object> v = ExpressionUtil.getConditonPojoValueMap(clientApiBaseUrl, businessModelId, businessId);
//        String orgId = v.get("orgId")+"";
//        String orgCode = v.get("orgCode")+"";
        String orgCodePath = v.get("orgPath")+"";
//        String workCaption = v.get("workCaption")+"";//工作说明
        String[] orgCodes =  orgCodePath.split("\\|");
        finalFlowDefination = this.flowDefLuYou( v, flowType, orgCodes , 1);
        if(v!=null && !v.isEmpty()){
            if(variables == null){
                variables = new HashMap<String,Object>();
            }
            variables.putAll(v);
        }
        //flowTask.getFlowInstance().setBusinessModelRemark(v.get("workCaption")+"");
        String defKey = finalFlowDefination.getDefKey();
        return startByKey(defKey, startUserId, businessKey, variables);
    }

    public FlowStartResultVO startByVO(FlowStartVO flowStartVO) throws NoSuchMethodException, SecurityException{
        FlowStartResultVO flowStartResultVO = null;
        Map<String, Object> userMap = flowStartVO.getUserMap();
        BusinessModel businessModel = businessModelDao.findByProperty("className", flowStartVO.getBusinessModelCode());
        FlowType flowType = null;
        if (StringUtils.isEmpty(flowStartVO.getFlowTypeId())) {//判断是否选择的有类型
            List<FlowType> flowTypeList = flowTypeDao.findListByProperty("businessModel", businessModel);
            if (flowTypeList != null && !flowTypeList.isEmpty()) {
                flowStartResultVO = new FlowStartResultVO();
//                if (flowTypeList.size() > 1) {//流程类型大于2，让用户选择
                    flowStartResultVO.setFlowTypeList(flowTypeList);
//                    return flowStartResultVO;
//                }
                flowType = flowTypeList.get(0);
            } else {
                flowStartResultVO = null;
            }
        } else {
            flowType = flowTypeDao.findOne(flowStartVO.getFlowTypeId());
        }

        if (userMap != null && !userMap.isEmpty()) {//判断是否选择了下一步的用户
                Map<String, Object> v = new HashMap<String, Object>();
                v.putAll(userMap);
                if(flowStartVO.getVariables()!=null && !flowStartVO.getVariables().isEmpty()){
                    v.putAll(flowStartVO.getVariables());
                }
                FlowInstance flowInstance = this.startByTypeCode(flowType, flowStartVO.getStartUserId(), flowStartVO.getBusinessKey(), v);
                flowStartResultVO.setFlowInstance(flowInstance);
        }else {
            //获取当前业务实体表单的条件表达式信息，（目前是任务执行时就注入，后期根据条件来优化)
            String businessId = flowStartVO.getBusinessKey();
            FlowDefination finalFlowDefination = null;
            String businessModelId = businessModel.getId();
            String appModuleId = businessModel.getAppModuleId();
            com.ecmp.basic.api.IAppModuleService proxy = ApiClient.createProxy(com.ecmp.basic.api.IAppModuleService.class);
            com.ecmp.basic.entity.AppModule appModule = proxy.findOne(appModuleId);
            String clientApiBaseUrl = ContextUtil.getAppModule(appModule.getCode()).getApiBaseAddress();
            Map<String, Object> v = ExpressionUtil.getConditonPojoValueMap(clientApiBaseUrl, businessModelId, businessId);
//        String orgId = v.get("orgId")+"";
//        String orgCode = v.get("orgCode")+"";
            String orgCodePath = v.get("orgPath")+"";
            orgCodePath = orgCodePath.substring(orgCodePath.indexOf("|")+1);
            String[] orgCodes =  orgCodePath.split("\\|");
            finalFlowDefination = this.flowDefLuYou( v, flowType, orgCodes , 1);
            List<NodeInfo> nodeInfoList = this.findStartNextNodes(finalFlowDefination);
            flowStartResultVO.setNodeInfoList(nodeInfoList);
        }
         return flowStartResultVO;
    }

    private List<NodeInfo> findStartNextNodes(FlowDefination flowDefination) {
        List<NodeInfo> result = null;
        if (flowDefination != null) {
            result = new ArrayList<NodeInfo>();
            String startUEL = flowDefination.getStartUel();
            String versionId = flowDefination.getLastVersionId();
            FlowDefVersion flowDefVersion = flowDefVersionDao.findOne(versionId);
            JSONObject defObj = JSONObject.fromObject(flowDefVersion.getDefJson());
            Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
            List<StartEvent> startEventList = definition.getProcess().getStartEvent();
            if (startEventList != null && startEventList.size() == 1) {
                StartEvent startEvent = startEventList.get(0);
                JSONArray targetNodes = startEvent.getTarget();
                for(int i=0;i<targetNodes.size();i++){
                    JSONObject jsonObject = targetNodes.getJSONObject(i);
                    String targetId = jsonObject.getString("targetId");
//                }
//                for (BaseFlowNode targetNode : targetNodes) {
                    NodeInfo nodeInfo = new NodeInfo();
                    nodeInfo.setId(targetId);

                    net.sf.json.JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(nodeInfo.getId());
                    net.sf.json.JSONObject executor = currentNode.getJSONObject("nodeConfig").getJSONObject("executor");
                    UserTask userTaskTemp = (UserTask) JSONObject.toBean(currentNode, UserTask.class);
                    nodeInfo.setName(userTaskTemp.getName());
                    nodeInfo.setType(userTaskTemp.getType());
                    if ("EndEvent".equalsIgnoreCase(userTaskTemp.getType())) {
                        nodeInfo.setType("EndEvent");
                        continue;
                    }
                    if ("Normal".equalsIgnoreCase(userTaskTemp.getNodeType())) {
                        nodeInfo.setUserVarName(userTaskTemp.getId() + "_Normal");
                        nodeInfo.setUiType("radiobox");
                        nodeInfo.setFlowTaskType("common");
                    } else if ("SingleSign".equalsIgnoreCase(userTaskTemp.getNodeType())) {
                        nodeInfo.setUserVarName(userTaskTemp.getId() + "_SingleSign");
                        nodeInfo.setUiType("checkbox");
                        nodeInfo.setFlowTaskType("singleSign");
                    } else if ("CounterSign".equalsIgnoreCase(userTaskTemp.getNodeType())) {
                        nodeInfo.setUserVarName(userTaskTemp.getId() + "_List_CounterSign");
                        nodeInfo.setUiType("checkbox");
                        nodeInfo.setFlowTaskType("countersign");
//                    MultiInstanceConfig multiInstanceConfig = new MultiInstanceConfig();
//                    multiInstanceConfig.setUserIds("${"+userTaskTemp.getId()+"_List_CounterSign}");
//                    multiInstanceConfig.setVariable("${"+userTaskTemp.getId()+"_CounterSign}");
                    }

                    if (executor != null) {
                        String userType = (String) executor.get("userType");
                        String ids = (String) executor.get("ids");
                        Set<Executor> employeeSet = new HashSet<Executor>();
                        List<Executor> employees = null;
                        if ("StartUser".equalsIgnoreCase(userType)) {//获取流程实例启动者
                           // continue;
//                            ProcessInstance instance = runtimeService.createProcessInstanceQuery()
//                                    .processInstanceId(flowTask.getFlowInstance().getActInstanceId()).singleResult();
//                            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(flowTask.getFlowInstance().getActInstanceId()).singleResult();
////                            Map<String,Object> v = instance.getProcessVariables();
//                            String startUserId = historicProcessInstance.getStartUserId();
                            IEmployeeService iEmployeeService = ApiClient.createProxy(IEmployeeService.class);
                           String  startUserId =  ContextUtil.getSessionUser().getUserId();
                            employees = iEmployeeService.getExecutorsByEmployeeIds(java.util.Arrays.asList(startUserId));
//                            if(v != null){
//                                startUserId = (String) v.get("startUserId");
//                            }
//                            if(StringUtils.isEmpty(startUserId)){
//                                startUserId = flowTask.getFlowInstance().getCreatedBy();
//                            }

                        } else {
                            if (!StringUtils.isEmpty(ids)) {
                                nodeInfo.setUiUserType(userType);
                                String[] idsShuZhu = ids.split(",");
                                List<String> idList = java.util.Arrays.asList(idsShuZhu);
                                //StartUser、Position、PositionType、SelfDefinition、AnyOne
                                if ("Position".equalsIgnoreCase(userType)) {//调用岗位获取用户接口
                                    IPositionService iPositionService = ApiClient.createProxy(IPositionService.class);
                                    employees = iPositionService.getExecutorsByPositionIds(idList);
                                } else if ("PositionType".equalsIgnoreCase(userType)) {//调用岗位类型获取用户接口
                                    IPositionService iPositionService = ApiClient.createProxy(IPositionService.class);
                                    employees = iPositionService.getExecutorsByPosCateIds(idList);
                                } else if ("SelfDefinition".equalsIgnoreCase(userType)) {//通过业务ID获取自定义用户
                                    IEmployeeService iEmployeeService = ApiClient.createProxy(IEmployeeService.class);
                                    employees = iEmployeeService.getExecutorsByEmployeeIds(idList);
                                } else if ("AnyOne".equalsIgnoreCase(userType)) {//任意执行人不添加用户
                                }
                            }
                        }
                        if (employees != null && !employees.isEmpty()) {
                            employeeSet.addAll(employees);
                            nodeInfo.setExecutorSet(employeeSet);
                        }
                    }
                    result.add(nodeInfo);
//                   if( "UserTask".equalsIgnoreCase(targetNode.getType())){
//                       UserTask userTask = (UserTask) targetNode;
//                       userTask.get
//                   }
                }
            }
        }
        return result;
    }

    /**
     * 数据删除操作
     * 并清除有关联的流程引擎数据
     *
     * @param id 待操作数据ID
     */
    @Override
    public OperateResult delete(String id) {
        List<FlowDefVersion> flowDefVersions = flowDefVersionDao.findByFlowDefinationId(id);
        for (FlowDefVersion flowDefVersion : flowDefVersions) {
            String actDeployId = flowDefVersion.getActDeployId();
            if ((actDeployId != null) && (!"".equals(actDeployId))) {
                this.deleteActivtiProcessDefinition(actDeployId, false);
                flowDefVersionDao.delete(flowDefVersion);
            }
        }
        flowDefinationDao.delete(id);
        OperateResult result = OperateResult.OperationSuccess("core_00003");
        return result;
    }

    /**
     * 通过ID批量删除
     *
     * @param ids
     */
    @Override
    public void delete(Collection<String> ids) {
        for (String id : ids) {
            this.delete(id);
        }
    }

    public FlowDefVersion getFlowDefVersion(String id, Integer versionCode) {
        FlowDefVersion flowDefVersion = null;
        if (versionCode > -1) {
            flowDefVersion = flowDefVersionDao.findByDefIdAndVersionCode(id, versionCode);
        } else {
            FlowDefination flowDefination = flowDefinationDao.findOne(id);
            ;
            flowDefVersion = flowDefVersionDao.findOne(flowDefination.getLastVersionId());
        }
        return flowDefVersion;
    }


//    /**
//     * 主键删除
//     *
//     * @param id 主键
//     * @return 返回操作结果对象
//     */
//    public OperateResult deleteById(String id) {
//        FlowDefination entity = flowDefinationDao.findOne(id);
//        return this.delete(entity);
//    }


    /**
     * 使用部署ID，删除流程引擎数据定义
     *
     * @param deploymentId 发布ID
     * @param force        是否强制删除（能删除启动的流程，会删除和当前规则相关的所有信息，正在执行的信息，也包括历史信息）
     */
    private void deleteActivtiProcessDefinition(String deploymentId, Boolean force) {
        /*
         * 不带级联的删除
         * 只能删除没有启动的流程，如果流程启动，就会抛出异常
         */
        if (force) {
            /*
         * 能级联的删除
         * 能删除启动的流程，会删除和当前规则相关的所有信息，正在执行的信息，也包括历史信息
         */
            processEngine.getRepositoryService()//与流程定义和部署对象相关的Service
                    .deleteDeployment(deploymentId, true);
        } else {
            processEngine.getRepositoryService()//与流程定义和部署对象相关的Service
                    .deleteDeployment(deploymentId);
        }
    }

    /**
     * 通过Key删除对应的流程定义
     *
     * @param processDefinitionKey 定义流程定义的key
     * @param force                是否强制删除
     */
    private void deleteActivtiProcessDefinitionByKey(String processDefinitionKey, Boolean force) {
        // 先使用流程定义的key, 查询出所有版本的流程定义
        List<ProcessDefinition> lists = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey) // 使用key查询
                .list();
        // 遍历,获取流程定义的id
        if (lists != null && lists.size() > 0) {
            for (ProcessDefinition processDefinition : lists) {
                // 获取部署ID
                String deploymentId = processDefinition.getDeploymentId();
                repositoryService.deleteDeployment(deploymentId, true);

            }
        }
    }


    /**
     * 将流程定义发布到流程引擎，建立关联
     *
     * @param name 流程名称
     * @param xml  流程定义XML
     * @return 流程发布
     * @throws UnsupportedEncodingException
     */
//    @Transactional( propagation= Propagation.REQUIRES_NEW)
    public Deployment deploy(String name, String xml) throws UnsupportedEncodingException {
        // InputStream stream = new ByteArrayInputStream(xml.getBytes("utf-8"));
        Deployment deploy = null;
        org.springframework.orm.jpa.JpaTransactionManager transactionManager = (org.springframework.orm.jpa.JpaTransactionManager) ContextUtil.getApplicationContext().getBean("transactionManager");
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW); // 事物隔离级别，开启新事务，这样会比较安全些。
        TransactionStatus status = transactionManager.getTransaction(def); // 获得事务状态
        try {
            //逻辑代码，可以写上你的逻辑处理代码
            DeploymentBuilder deploymentBuilder = this.repositoryService.createDeployment();
//            deploymentBuilder.name(name);3
            deploymentBuilder.addString(name + ".bpmn", xml);
            // deploymentBuilder.addInputStream("bpmn20.xml", stream);
            deploy = deploymentBuilder.deploy();
            transactionManager.commit(status);

        } catch (Exception e) {
            e.printStackTrace();
            transactionManager.rollback(status);
            throw e;
        }
//        TransactionStatus status = transactionManager.getTransaction(def);//设为false
        return deploy;
    }

    /**
     * 获取Activiti流程定义实体
     *
     * @param deployId 流程部署ID
     * @return Activiti流程定义实体
     */
    private ProcessDefinitionEntity getProcessDefinitionByDeployId(String deployId) {
        org.springframework.orm.jpa.JpaTransactionManager transactionManager = (org.springframework.orm.jpa.JpaTransactionManager) ContextUtil.getApplicationContext().getBean("transactionManager");
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW); // 事物隔离级别，开启新事务，这样会比较安全些。
        TransactionStatus status = transactionManager.getTransaction(def); // 获得事务状态
        ProcessDefinitionEntity result = null;
        try {
            //逻辑代码，可以写上你的逻辑处理代码
            ProcessDefinition proDefinition = (ProcessDefinition) this.repositoryService.createProcessDefinitionQuery()
                    .deploymentId(deployId).singleResult();
            if (proDefinition == null)
                return null;
            result = getProcessDefinitionByDefId(proDefinition.getId());

        } catch (Exception e) {
            e.printStackTrace();
            transactionManager.rollback(status);
            throw e;
        }
        return result;
    }

    /**
     * 获取Activiti流程定义实体
     *
     * @param actDefId 流程定义ID
     * @return Activiti流程定义实体
     */
    private ProcessDefinitionEntity getProcessDefinitionByDefId(String actDefId) {
        ProcessDefinitionEntity ent = (ProcessDefinitionEntity) ((RepositoryServiceImpl) this.repositoryService)
                .getDeployedProcessDefinition(actDefId);
        return ent;
    }

    /**
     * 通过Activiti流程定义ID启动流程实体
     *
     * @param proessDefId 流程定义ID
     * @param variables   其他参数
     * @return Activiti流程定义实体
     */
    private ProcessInstance startFlowById(String proessDefId, Map<String, Object> variables) {
        ProcessInstance instance = this.runtimeService.startProcessInstanceById(proessDefId, variables);
        initTask(instance);
        return instance;
    }

    /**
     * 通过Activiti流程定义ID启动流程实体
     *
     * @param proessDefId 流程定义ID
     * @param startUserId 流程启动人
     * @param businessKey 业务KEY
     * @param variables   其他参数
     * @return Activiti流程定义实体
     */
    private ProcessInstance startFlowById(String proessDefId, String startUserId, String businessKey, Map<String, Object> variables) {
        // 用来设置启动流程的人员ID，引擎会自动把用户ID保存到activiti:initiator中
        if ((startUserId != null) && (!"".equals(startUserId))) {
            identityService.setAuthenticatedUserId(startUserId);
        }
        ProcessInstance instance = this.runtimeService.startProcessInstanceById(proessDefId, businessKey, variables);

        return instance;
    }

    /**
     * 通过Activiti流程定义ID启动流程实体
     *
     * @param proessDefId 流程定义ID
     * @param businessKey 业务KEY
     * @param variables   其他参数
     * @return
     */
    private ProcessInstance startFlowById(String proessDefId, String businessKey, Map<String, Object> variables) {
        return startFlowById(proessDefId, null, businessKey, variables);
    }

    /**
     * 通过Activiti流程定义Key启动流程实体
     *
     * @param processDefKey 流程定义Key
     * @param variables     其他参数
     * @return
     */
    private ProcessInstance startFlowByKey(String processDefKey, Map<String, Object> variables) {
        ProcessInstance instance = this.runtimeService.startProcessInstanceByKey(processDefKey, variables);

        return instance;
    }

    /**
     * 通过Activiti流程定义Key启动流程实体
     *
     * @param processDefKey 流程定义Key
     * @param startUserId   流程启动人
     * @param businessKey   业务KEY
     * @param variables     其他参数
     * @return Activiti流程定义实体
     */
    private ProcessInstance startFlowByKey(String processDefKey, String startUserId, String businessKey, Map<String, Object> variables) {
        // 用来设置启动流程的人员ID，引擎会自动把用户ID保存到activiti:initiator中
        if ((startUserId != null) && (!"".equals(startUserId))) {
            identityService.setAuthenticatedUserId(startUserId);
        }
        ProcessInstance instance = this.runtimeService.startProcessInstanceByKey(processDefKey, businessKey, variables);
        return instance;
    }

    /**
     * 通过Activiti流程定义Key启动流程实体
     *
     * @param processDefKey 流程定义Key
     * @param businessKey   业务KEY
     * @param variables     其他参数
     * @return Activiti流程定义实体
     */
    private ProcessInstance startFlowByKey(String processDefKey, String businessKey, Map<String, Object> variables) {
        return startFlowByKey(processDefKey, null, businessKey, variables);
    }

    private void initTask(ProcessInstance instance) {
        FlowInstance flowInstance = flowInstanceDao.findByActInstanceId(instance.getId());
        if (instance.isEnded()) {//流程结束
            flowInstance.setEnded(true);
            flowInstance.setEndDate(new Date());
            flowInstanceDao.save(flowInstance);
            return;
        }
        // 根据当流程实例查询任务
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(instance.getId()).active().list();
        String flowName = null;
        if (taskList != null && taskList.size() > 0) {
            for (Task task : taskList) {
                String actTaskDefKey = task.getTaskDefinitionKey();
                String flowDefJson = flowInstance.getFlowDefVersion().getDefJson();
                JSONObject defObj = JSONObject.fromObject(flowDefJson);
                Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
                net.sf.json.JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(actTaskDefKey);
                flowName = definition.getProcess().getName();
                net.sf.json.JSONObject normalInfo = currentNode.getJSONObject("nodeConfig").getJSONObject("normal");
                Integer executeTime = normalInfo.getInt("executeTime");
                Boolean canReject = normalInfo.getBoolean("allowReject");
                Boolean canSuspension = normalInfo.getBoolean("allowTerminate");
                List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(task.getId());
                for (IdentityLink identityLink : identityLinks) {
                    IEmployeeService iEmployeeService = ApiClient.createProxy(IEmployeeService.class);
                    List<Executor> employees = iEmployeeService.getExecutorsByEmployeeIds(java.util.Arrays.asList(identityLink.getUserId()));
                    if(employees!=null && !employees.isEmpty()){
                        Executor executor = employees.get(0);
                        FlowTask flowTask = new FlowTask();
                        flowTask.setCanReject(canReject);
                        flowTask.setCanSuspension(canSuspension);
                        flowTask.setTaskJsonDef(currentNode.toString());
                        flowTask.setFlowDefinitionId(flowInstance.getFlowDefVersion().getFlowDefination().getId());
                        flowTask.setActTaskDefKey(actTaskDefKey);
                        flowTask.setFlowName(flowName);
                        flowTask.setTaskName(task.getName());
                        flowTask.setActTaskId(task.getId());
                        flowTask.setOwnerAccount(executor.getCode());
                        flowTask.setOwnerName(executor.getName());
                        flowTask.setExecutorAccount(executor.getCode());
                        flowTask.setExecutorName(executor.getName());
                        flowTask.setPriority(task.getPriority());
//                                flowTask.setExecutorAccount(identityLink.getUserId());
                        flowTask.setActType(identityLink.getType());
                        flowTask.setDepict(task.getDescription());
                        flowTask.setTaskStatus(TaskStatus.INIT.toString());
                        flowTask.setFlowInstance(flowInstance);
                        flowTaskDao.save(flowTask);
                    }
                }

            }
        }

    }

}
