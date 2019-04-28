package com.ecmp.flow.service;


import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.flow.api.IFlowDefinationService;
import com.ecmp.flow.basic.vo.Executor;
import com.ecmp.flow.basic.vo.Organization;
import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.constant.FlowDefinationStatus;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.dao.*;
import com.ecmp.flow.entity.*;
import com.ecmp.flow.util.*;
import com.ecmp.flow.vo.*;
import com.ecmp.flow.vo.bpmn.*;
import com.ecmp.log.util.LogUtil;
import com.ecmp.util.JsonUtils;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import com.ecmp.vo.ResponseData;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.ws.rs.core.GenericType;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：流程定义相关服务
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
    private FlowTaskTool flowTaskTool;

    @Autowired
    private FlowDefVersionDao flowDefVersionDao;

    @Autowired
    private FlowDefinationDao flowDefinationDao;

    @Autowired
    private FlowInstanceDao flowInstanceDao;

    @Autowired
    private FlowExecutorConfigDao flowExecutorConfigDao;

    @Autowired
    private FlowCommonUtil flowCommonUtil;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private FlowTypeDao flowTypeDao;

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private BusinessModelDao businessModelDao;

    @Autowired
    private FlowServiceUrlDao flowServiceUrlDao;

    @Autowired
    private DefaultBusinessModelDao defaultBusinessModelDao;


    @Override
    public ResponseData listAllOrgs() {
        String url = Constants.getBasicOrgListallorgsUrl();
        ResponseData responseData = new ResponseData();
        try {
            List<Organization> result = ApiClient.getEntityViaProxy(url, new GenericType<List<Organization>>() {
            }, null);
            responseData.setMessage("操作成功！");
            responseData.setData(result);
        } catch (Exception e) {
            responseData.setSuccess(false);
            responseData.setMessage("请求接口错误！");
            LogUtil.error(e.getMessage());
        }
        return responseData;
    }

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
        entity.setTenantCode(ContextUtil.getTenantCode());
        flowDefinationDao.save(entity);
        logger.debug("Saved FlowDefination id is {}", entity.getId());
        if (flowDefVersion != null) {
            flowDefVersion.setFlowDefination(entity);
            flowDefVersion.setTenantCode(ContextUtil.getTenantCode());
            flowDefVersionDao.save(flowDefVersion);
            logger.debug("Saved FlowDefVersion id is {}", entity.getId());
            entity.setLastVersionId(flowDefVersion.getId());
            flowDefinationDao.save(entity);
            logger.debug("Saved FlowDefination id is {}", entity.getId());
        }
        OperateResultWithData<FlowDefination> operateResult;
        // 流程定义保存成功！
        operateResult = OperateResultWithData.operationSuccess("10054");
        operateResult.setData(entity);
        return operateResult;
    }

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
        flowDefVersion.setFlowDefinationStatus(FlowDefinationStatus.Activate);
        flowDefVersionDao.save(flowDefVersion);
//        clearFlowDefVersion(id);//清除缓存
        clearFlowDefVersion();

        FlowDefination flowDefination = flowDefVersion.getFlowDefination();
        flowDefination.setFlowDefinationStatus(FlowDefinationStatus.Activate);
        flowDefination.setLastDeloyVersionId(flowDefVersion.getId());
        flowDefination.setStartUel(flowDefVersion.getStartUel());
        flowDefination.setName(flowDefVersion.getName());
        flowDefinationDao.save(flowDefination);
//        //清除缓存
//        String pattern = "FlowFindByTypeCodeAndOrgCode_*";
//        Set<String> keys = redisTemplate.keys(pattern);
//        if (keys!=null&&!keys.isEmpty()){
//            redisTemplate.delete(keys);
//        }
        return deployId;
    }

    private void clearFlowDefVersion() {
        String pattern = "FLowGetLastFlowDefVersion_*";
        if (redisTemplate != null) {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        }
    }

    private void clearFlowDefVersion(String defVersionId) {
        String key = "FLowGetLastFlowDefVersion_" + defVersionId;
        if (redisTemplate != null) {
            if (redisTemplate.hasKey(key)) {
                redisTemplate.delete(key);
            }
        }
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
            String versionId = flowDefination.getLastDeloyVersionId();
            FlowDefVersion flowDefVersion = flowCommonUtil.getLastFlowDefVersion(versionId);
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
                flowInstance.setFlowName(flowDefVersion.getName() + ":" + businessKey);
                flowInstance.setActInstanceId(processInstance.getId());
                flowInstanceDao.save(flowInstance);
                initTask(flowInstance, variables);
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
            String versionId = flowDefination.getLastDeloyVersionId();
            FlowDefVersion flowDefVersion = flowCommonUtil.getLastFlowDefVersion(versionId);
            if (flowDefVersion != null && flowDefVersion.getActDefId() != null) {
                ProcessInstance processInstance = null;
                if ((startUserId != null) && (!"".equals(startUserId))) {
                    processInstance = this.startFlowByKey(key, startUserId, businessKey, variables);
                } else {
                    processInstance = this.startFlowByKey(key, businessKey, variables);
                }
                flowInstance = new FlowInstance();
                flowInstance.setBusinessId(processInstance.getBusinessKey());
                String workCaption = (String) variables.get(Constants.WORK_CAPTION);//工作说明
                flowInstance.setBusinessModelRemark(workCaption);
                String businessCode = (String) variables.get(Constants.BUSINESS_CODE);//工作说明
                flowInstance.setBusinessCode(businessCode);
                String businessName = (String) variables.get(Constants.NAME);//业务单据名称
                flowInstance.setBusinessName(businessName);

                flowInstance.setFlowDefVersion(flowDefVersion);
                flowInstance.setStartDate(new Date());
                flowInstance.setFlowName(flowDefVersion.getName());
                flowInstance.setActInstanceId(processInstance.getId());
                flowInstanceDao.save(flowInstance);
                initTask(flowInstance, variables);
            }
        }
        return flowInstance;
    }

    /**
     * 路由流程定义
     *
     * @param businessModelMap  业务单据数据Map
     * @param flowType          流程定义
     * @param orgParentCodeList 代码路径数组
     * @param level             遍历层次
     * @return
     */
    private FlowDefination flowDefLuYou(Map<String, Object> businessModelMap, FlowType flowType, List<String> orgParentCodeList, int level) throws NoSuchMethodException, SecurityException {
        FlowDefination finalFlowDefination = null;
        if (orgParentCodeList.isEmpty()) {
            return null;
        }
        String orgCode = orgParentCodeList.get(level);
        List<FlowDefination> flowDefinationList = flowDefinationDao.findByTypeCodeAndOrgCode(flowType.getCode(), orgCode);
        if (flowDefinationList != null && flowDefinationList.size() > 0) {
            for (FlowDefination flowDefination : flowDefinationList) {
                String startUelStr = flowDefination.getStartUel();
                if (!StringUtils.isEmpty(startUelStr)) {
                    JSONObject startUelObject = JSONObject.fromObject(startUelStr);
                    String conditionText = startUelObject.getString(Constants.GROOVY_UEL);
                    if (StringUtils.isNotEmpty(conditionText)) {
                        if (conditionText.startsWith("#{")) {// #{开头代表自定义的groovy表达式
                            String conditonFinal = conditionText.substring(conditionText.indexOf("#{") + 2,
                                    conditionText.lastIndexOf("}"));
                            if (ConditionUtil.groovyTest(conditonFinal, businessModelMap)) {
                                if (flowDefination.getFlowDefinationStatus() == FlowDefinationStatus.Activate) {
                                    finalFlowDefination = flowDefination;
                                    break;
                                }
                            }
                        } else {//其他的用UEL表达式验证
                            Object tempResult = ConditionUtil.uelResult(conditionText, businessModelMap);
                            if (tempResult instanceof Boolean) {
                                Boolean resultB = (Boolean) tempResult;
                                if (resultB == true) {
                                    if (flowDefination.getFlowDefinationStatus() == FlowDefinationStatus.Activate) {
                                        finalFlowDefination = flowDefination;
                                        break;
                                    }
                                }
                            }
                        }
                    } else {
                        if (finalFlowDefination == null && flowDefination.getFlowDefinationStatus() == FlowDefinationStatus.Activate) {//未配置启动UEL
                            finalFlowDefination = flowDefination;
                        }
                    }
                } else {
                    if (finalFlowDefination == null && flowDefination.getFlowDefinationStatus() == FlowDefinationStatus.Activate) {//未配置启动UEL
                        finalFlowDefination = flowDefination;
                    }
                }
            }
        }
        if (finalFlowDefination == null) {//同级组织机构找不到符合条件流程定义，自动向上级组织机构查找所属类型的流程定义
            level++;
            if (level >= orgParentCodeList.size()) {
                return null;
            }
            return this.flowDefLuYou(businessModelMap, flowType, orgParentCodeList, level);
        }
        return finalFlowDefination;
    }

    private FlowOperateResult checkStart(String businessKey, FlowDefVersion flowDefVersion) {
        FlowOperateResult flowOpreateResult = null;
        if (flowDefVersion != null && StringUtils.isNotEmpty(businessKey)) {
            String startCheckServiceUrlId = flowDefVersion.getStartCheckServiceUrlId();
            if (StringUtils.isNotEmpty(startCheckServiceUrlId)) {
                FlowServiceUrl flowServiceUrl = flowServiceUrlDao.findOne(startCheckServiceUrlId);
                String checkUrl = flowServiceUrl.getUrl();
                if (StringUtils.isNotEmpty(checkUrl)) {
                    String apiBaseAddressConfig = flowDefVersion.getFlowDefination().getFlowType().getBusinessModel().getAppModule().getApiBaseAddress();
                    String baseUrl = ContextUtil.getGlobalProperty(apiBaseAddressConfig);
//                  String baseUrl= flowDefVersion.getFlowDefination().getFlowType().getBusinessModel().getAppModule().getApiBaseAddress();
                    String checkUrlPath = baseUrl + checkUrl;
                    FlowInvokeParams flowInvokeParams = new FlowInvokeParams();
                    flowInvokeParams.setId(businessKey);
                    flowOpreateResult = ApiClient.postViaProxyReturnResult(checkUrlPath, new GenericType<FlowOperateResult>() {
                    }, flowInvokeParams);
                }
            }
        }
        return flowOpreateResult;
    }

    private FlowInstance startByTypeCode(String flowDefKey, FlowStartVO flowStartVO, FlowStartResultVO flowStartResultVO, Map<String, Object> variables) throws NoSuchMethodException, RuntimeException {
        String startUserId = flowStartVO.getStartUserId();
        String businessKey = flowStartVO.getBusinessKey();
        variables.put(Constants.OPINION, ContextUtil.getMessage("10050"));//所有流程启动描述暂时都设计为“流程启动”
        if (startUserId == null) {
            startUserId = ContextUtil.getUserId();
        }
        FlowInstance flowInstance = null;
        FlowDefination flowDefination = flowDefinationDao.findByDefKey(flowDefKey);
        if (flowDefination != null) {
            String versionId = flowDefination.getLastDeloyVersionId();
            FlowDefVersion flowDefVersion = flowCommonUtil.getLastFlowDefVersion(versionId);
            if (flowDefVersion != null && flowDefVersion.getActDefId() != null && (flowDefVersion.getFlowDefinationStatus() == FlowDefinationStatus.Activate)) {
                FlowOperateResult flowOperateResult = checkStart(businessKey, flowDefVersion);
                if (flowOperateResult != null && !flowOperateResult.isSuccess()) {
                    flowStartResultVO.setCheckStartResult(false);
                    throw new FlowException(flowOperateResult.getMessage());
                }
                String actDefId = flowDefVersion.getActDefId();
                variables.put(Constants.FLOW_DEF_VERSION_ID, flowDefVersion.getId());
                ProcessInstance processInstance = null;
                if ((startUserId != null) && (!"".equals(startUserId))) {
                    processInstance = this.startFlowById(actDefId, startUserId, businessKey, variables);
                } else {
                    processInstance = this.startFlowById(actDefId, businessKey, variables);
                }
                try {
                    flowInstance = flowInstanceDao.findByActInstanceId(processInstance.getId());
                    if (processInstance != null && !processInstance.isEnded()) {//针对启动时只有服务任务这种情况（即启动就结束）
                        initTask(flowInstance, variables);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    if (flowInstance != null) {
                        BusinessModel businessModel = businessModelDao.findByProperty("className", flowStartVO.getBusinessModelCode());
                        AppModule appModule = businessModel.getAppModule();
                        Map<String, Object> params = new HashMap<String, Object>();
                        ;
                        params.put(Constants.BUSINESS_MODEL_CODE, businessModel.getClassName());
                        params.put(Constants.ID, flowInstance.getBusinessId());
                        params.put(Constants.STATUS, FlowStatus.INIT);
                        String apiBaseAddressConfig = appModule.getApiBaseAddress();
                        String baseUrl = ContextUtil.getGlobalProperty(apiBaseAddressConfig);
                        String url = baseUrl + "/" + businessModel.getConditonStatusRest();
                        ApiClient.postViaProxyReturnResult(url, new GenericType<Boolean>() {
                        }, params);
                    }
                    throw new FlowException(e.getMessage());
                }
            }
        } else {
            throw new FlowException("流程定义未找到！");
        }

        return flowInstance;
    }


    private boolean checkFlowInstanceActivate(String businessKey) {
        boolean result = false;
        if (StringUtils.isNotEmpty(businessKey)) {
            List<FlowInstance> flowInstanceList = flowInstanceDao.findByBusinessId(businessKey);
            if (flowInstanceList != null && !flowInstanceList.isEmpty()) {
                for (FlowInstance flowInstance : flowInstanceList) {
                    if (flowInstance.isEnded() != true) {
                        result = true;
                    }
                }
            }
        } else {
            throw new FlowException("business's id is null");
        }
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OperateResultWithData<FlowStartResultVO> startByVO(FlowStartVO flowStartVO) throws NoSuchMethodException, SecurityException {
//        System.setProperty("http.proxyHost", "localhost");
//        System.setProperty("https.proxyHost", "localhost");
//        System.setProperty("http.proxyPort", "8888");
//        System.setProperty("https.proxyPort", "8888");
        if (checkFlowInstanceActivate(flowStartVO.getBusinessKey())) {
            String message = ContextUtil.getMessage("10051", flowStartVO.getBusinessKey());
            return OperateResultWithData.operationFailure(message);
        }
        OperateResultWithData resultWithData = OperateResultWithData.operationSuccess();
        try {
            FlowStartResultVO flowStartResultVO = new FlowStartResultVO();
            resultWithData.setData(flowStartResultVO);
            Map<String, Object> userMap = flowStartVO.getUserMap();
            BusinessModel businessModel = businessModelDao.findByProperty("className", flowStartVO.getBusinessModelCode());
            if(businessModel == null){
                return OperateResultWithData.operationFailure("业务实体未进行配置！");
            }
            Map<String, Object> businessV = null;
            String businessId = flowStartVO.getBusinessKey();
            businessV = ExpressionUtil.getPropertiesValuesMap(businessModel, businessId, true);
            if (flowStartVO.getVariables() == null) {
                flowStartVO.setVariables(businessV);
            } else {
                flowStartVO.getVariables().putAll(businessV);
            }
            if (userMap != null && !userMap.isEmpty()) {//判断是否选择了下一步的用户
                Map<String, Object> v = flowStartVO.getVariables();
                v.putAll(userMap);
                if (v.get("additionRemark") != null
                        && StringUtils.isNotEmpty(v.get("additionRemark").toString())
                        && !"null".equalsIgnoreCase(v.get("additionRemark").toString().trim())
                        && !"请填写附加说明".equals(v.get("additionRemark").toString().trim())) {
                    if(v.get("workCaption")!=null){
                        v.put("workCaption", v.get("workCaption").toString() + "【附加说明：" + v.get("additionRemark").toString() + "】");
                    }else{
                        v.put("workCaption", "【附加说明：" + v.get("additionRemark").toString() + "】");

                    }
                    flowStartVO.getVariables().put("workCaption", v.get("workCaption").toString());
                }
                String flowDefKey = flowStartVO.getFlowDefKey();
                this.startByTypeCode(flowDefKey, flowStartVO, flowStartResultVO, v);
            } else {
                FlowType flowType = null;
                FlowDefination finalFlowDefination = null;
                List<FlowType> flowTypeList = null;
                List<StartFlowTypeVO> flowTypeListVO = null;
                if (StringUtils.isEmpty(flowStartVO.getFlowTypeId())) {//判断是否选择的有类型
                    flowTypeList = flowTypeDao.findListByProperty("businessModel", businessModel);
                } else {
                    flowType = flowTypeDao.findOne(flowStartVO.getFlowTypeId());
                    flowTypeList = new ArrayList<FlowType>();
                    if (flowType != null) {
                        flowTypeList.add(flowType);
                    }
                }
                List<String> orgParentCodeList = null;
                if (flowTypeList != null && !flowTypeList.isEmpty()) {
                    flowTypeListVO = new ArrayList<>();
                    flowStartResultVO.setFlowTypeList(flowTypeListVO);
                    String orgId = (String) businessV.get(Constants.ORG_ID);
                    orgParentCodeList = flowTaskTool.getParentOrgCodes(orgId);
                    for (FlowType flowTypeTemp : flowTypeList) {
                        FlowDefination flowDefinationTemp = this.flowDefLuYou(businessV, flowTypeTemp, orgParentCodeList, 0);
                        if (flowDefinationTemp != null) {
                            StartFlowTypeVO startFlowTypeVO = new StartFlowTypeVO();
                            startFlowTypeVO.setId(flowTypeTemp.getId());
                            startFlowTypeVO.setName(flowTypeTemp.getName());
//                        flowTypeTemp.getFlowDefinations().add(flowDefinationTemp);
                            startFlowTypeVO.setFlowDefKey(flowDefinationTemp.getDefKey());
                            startFlowTypeVO.setFlowDefName(flowDefinationTemp.getName());
                            flowTypeListVO.add(startFlowTypeVO);
                            if (finalFlowDefination == null) {
                                finalFlowDefination = flowDefinationTemp;
                            }
                        }
                    }
//                for (FlowType flowTypeTemp : flowTypeList) {
//                    if (flowTypeTemp.getFlowDefinations() != null && !flowTypeTemp.getFlowDefinations().isEmpty()) {
//                        finalFlowDefination = (FlowDefination) flowTypeTemp.getFlowDefinations().toArray()[0];
//                        break;
//                    }
//                }
                } else {
                    flowStartResultVO = null;
                }
                if (finalFlowDefination != null) {
                    List<NodeInfo> nodeInfoList = this.findStartNextNodes(finalFlowDefination, flowStartVO);
                    //固化流程字段不为空（兼容以前版本），并且选择了固化流程,提供流程定义的id
                    if (finalFlowDefination.getSolidifyFlow() != null && finalFlowDefination.getSolidifyFlow() == true) {
                        flowStartResultVO.setSolidifyFlow(true);
                        nodeInfoList.get(0).setExecutorSet(null);//固化流程不需要下一节点人员
                        flowStartResultVO.setFlowDefinationId(finalFlowDefination.getId());
                    } else {
                        flowStartResultVO.setSolidifyFlow(false);
                    }
                    flowStartResultVO.setNodeInfoList(nodeInfoList);
                }
            }
//
//        if (flowTypeList != null && !flowTypeList.isEmpty()) {
//            for (FlowType flowTypeTemp : flowTypeList) {
//                Set<FlowDefination> flowDefinationSet = flowTypeTemp.getFlowDefinations();
//                if (flowDefinationSet != null && !flowDefinationSet.isEmpty()) {
//                    for (FlowDefination flowDefination : flowDefinationSet) {
//                        flowDefination.setFlowType(null);
//                    }
//                }
//            }
//        }
        } catch (FlowException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            resultWithData = OperateResultWithData.operationFailure(e.getMessage());
//            throw  e;
        }
        return resultWithData;
    }

    private List<NodeInfo> initNodesInfo(List<NodeInfo> result, FlowStartVO flowStartVO, Definition definition, String nodeId) {
        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.setId(nodeId);

        JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(nodeInfo.getId());
        JSONObject executor = null;
        JSONArray executorList = null;//针对两个条件以上的情况
        try {
            JSONObject normal = currentNode.getJSONObject(Constants.NODE_CONFIG).getJSONObject("normal");
            Boolean allowChooseInstancy = normal.getBoolean("allowChooseInstancy");
            nodeInfo.setAllowChooseInstancy(allowChooseInstancy);
        } catch (Exception e) {
        }
        if (currentNode.getJSONObject(Constants.NODE_CONFIG).has(Constants.EXECUTOR)) {
            try {
                executor = currentNode.getJSONObject(Constants.NODE_CONFIG).getJSONObject(Constants.EXECUTOR);
            } catch (Exception e) {
                if (executor == null) {
                    try {
                        executorList = currentNode.getJSONObject(Constants.NODE_CONFIG).getJSONArray(Constants.EXECUTOR);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                    if (executorList != null && executorList.size() == 1) {
                        executor = executorList.getJSONObject(0);
                    }
                }
            }
        }
        UserTask userTaskTemp = (UserTask) JSONObject.toBean(currentNode, UserTask.class);
        nodeInfo.setName(userTaskTemp.getName());
        nodeInfo.setType(userTaskTemp.getType());
        if ("EndEvent".equalsIgnoreCase(userTaskTemp.getType())) {
            nodeInfo.setType("EndEvent");
            return result;
        }
        if ("Normal".equalsIgnoreCase(userTaskTemp.getNodeType())) {
            nodeInfo.setUserVarName(userTaskTemp.getId() + "_Normal");
            nodeInfo.setUiType("radiobox");
            nodeInfo.setFlowTaskType("common");
        } else if ("SingleSign".equalsIgnoreCase(userTaskTemp.getNodeType())) {
            nodeInfo.setUserVarName(userTaskTemp.getId() + "_SingleSign");
            nodeInfo.setUiType("checkbox");
            nodeInfo.setFlowTaskType("singleSign");
        } else if ("Approve".equalsIgnoreCase(userTaskTemp.getNodeType())) {
            nodeInfo.setUserVarName(userTaskTemp.getId() + "_Approve");
            nodeInfo.setUiType("radiobox");
            nodeInfo.setFlowTaskType("approve");
        } else if ("CounterSign".equalsIgnoreCase(userTaskTemp.getNodeType())) {
            nodeInfo.setUserVarName(userTaskTemp.getId() + "_List_CounterSign");
            nodeInfo.setUiType("checkbox");
            nodeInfo.setFlowTaskType("countersign");
        } else if ("ParallelTask".equalsIgnoreCase(userTaskTemp.getNodeType()) || "SerialTask".equalsIgnoreCase(userTaskTemp.getNodeType())) {
            nodeInfo.setUserVarName(userTaskTemp.getId() + "_List_CounterSign");
            nodeInfo.setUiType("checkbox");
            nodeInfo.setFlowTaskType(userTaskTemp.getNodeType());
        } else if ("ServiceTask".equalsIgnoreCase(userTaskTemp.getNodeType())) {//服务任务
            ServiceTask serviceTaskTemp = (ServiceTask) JSONObject.toBean(currentNode, ServiceTask.class);
            nodeInfo.setName(serviceTaskTemp.getName());
            nodeInfo.setType(serviceTaskTemp.getType());
            nodeInfo.setUserVarName(nodeInfo.getId() + "_ServiceTask");
            nodeInfo.setUiType("radiobox");
            nodeInfo.setFlowTaskType("serviceTask");
            String startUserId = ContextUtil.getSessionUser().getUserId();
//            Map<String,Object> params = new HashMap();
//            params.put("employeeIds",java.util.Arrays.asList(startUserId));
//            String url = Constants.BASIC_SERVICE_URL+ Constants.BASIC_EMPLOYEE_GETEXECUTORSBYEMPLOYEEIDS_URL;
//            List<Executor> employees=ApiClient.getEntityViaProxy(url,new GenericType<List<Executor>>() {},params);

            List<Executor> employees = flowCommonUtil.getBasicUserExecutors(Arrays.asList(startUserId));
//            List<Executor> employees = flowCommonUtil.getBasicExecutors(Arrays.asList(startUserId));
            if (employees != null && !employees.isEmpty()) {//服务任务默认选择流程启动人
                Set<Executor> employeeSet = new HashSet<Executor>();
                employeeSet.addAll(employees);
                nodeInfo.setExecutorSet(employeeSet);
            }
        } else if ("ReceiveTask".equalsIgnoreCase(userTaskTemp.getNodeType())) {//接收任务
            ReceiveTask receiveTaskTemp = (ReceiveTask) JSONObject.toBean(currentNode, ReceiveTask.class);
            nodeInfo.setName(receiveTaskTemp.getName());
            nodeInfo.setType(receiveTaskTemp.getType());
            nodeInfo.setUserVarName(nodeInfo.getId() + "_ReceiveTask");
            nodeInfo.setUiType("radiobox");
            nodeInfo.setFlowTaskType("receiveTask");
            String startUserId = ContextUtil.getSessionUser().getUserId();
//            Map<String,Object> params = new HashMap();
//            params.put("employeeIds",java.util.Arrays.asList(startUserId));
//            String url = Constants.BASIC_SERVICE_URL+ Constants.BASIC_EMPLOYEE_GETEXECUTORSBYEMPLOYEEIDS_URL;
//            List<Executor> employees=ApiClient.getEntityViaProxy(url,new GenericType<List<Executor>>() {},params);

            List<Executor> employees = flowCommonUtil.getBasicUserExecutors(Arrays.asList(startUserId));
//            List<Executor> employees = flowCommonUtil.getBasicExecutors(Arrays.asList(startUserId));
            if (employees != null && !employees.isEmpty()) {//服务任务默认选择流程启动人
                Set<Executor> employeeSet = new HashSet<Executor>();
                employeeSet.addAll(employees);
                nodeInfo.setExecutorSet(employeeSet);
            }
        } else {
            nodeInfo.setUserVarName(userTaskTemp.getId() + "_Normal");
        }

        if (executor != null && !executor.isEmpty()) {
            String userType = executor.get("userType") + "";
            String ids = executor.get("ids") + "";
            Set<Executor> employeeSet = new HashSet<Executor>();
            List<Executor> employees = null;
            nodeInfo.setUiUserType(userType);
            if ("StartUser".equalsIgnoreCase(userType)) {//获取流程实例启动者
                String startUserId = flowStartVO.getStartUserId();
                if (StringUtils.isEmpty(startUserId)) {
                    startUserId = ContextUtil.getSessionUser().getUserId();
                }
//                employees = flowCommonUtil.getBasicExecutors(Arrays.asList(startUserId));
                employees = flowCommonUtil.getBasicUserExecutors(Arrays.asList(startUserId));
            } else {
                String selfDefId = executor.get("selfDefId") + "";
                if (StringUtils.isNotEmpty(ids)) {
                    if ("SelfDefinition".equalsIgnoreCase(userType)) {//通过业务ID获取自定义用户
                        if (StringUtils.isNotEmpty(selfDefId) && !Constants.NULL_S.equalsIgnoreCase(selfDefId)) {
                            FlowExecutorConfig flowExecutorConfig = flowExecutorConfigDao.findOne(selfDefId);
                            String path = flowExecutorConfig.getUrl();
                            AppModule appModule = flowExecutorConfig.getBusinessModel().getAppModule();
                            String appModuleCode = appModule.getApiBaseAddress();
                            String param = flowExecutorConfig.getParam();
                            FlowInvokeParams flowInvokeParams = new FlowInvokeParams();
                            flowInvokeParams.setId(flowStartVO.getBusinessKey());
                            flowInvokeParams.setOrgId("" + flowStartVO.getVariables().get("orgId"));
                            flowInvokeParams.setJsonParam(param);
                            employees = ApiClient.postViaProxyReturnResult(appModuleCode, path, new GenericType<List<Executor>>() {
                            }, flowInvokeParams);
                        } else {
                            throw new FlowException("SelfDefinition's selfDefId is null exception!");
                        }
                    } else {
                        //岗位或者岗位类型（Position、PositionType、AnyOne）、组织机构都改为单据的组织机构
                        String  startOrBusinessOrgId = "" + flowStartVO.getVariables().get("orgId");
                        employees = flowTaskTool.getExecutors(userType, ids, startOrBusinessOrgId);
                    }
                }
            }
            if (employees != null && !employees.isEmpty()) {
                employeeSet.addAll(employees);
                nodeInfo.setExecutorSet(employeeSet);
            }
        } else if (executorList != null && executorList.size() > 1) {
            Set<Executor> employeeSet = new HashSet<Executor>();
            List<Executor> employees = null;
            String selfDefId = null;
            List<String> orgDimensionCodes = null;//组织维度代码集合
            List<String> positionIds = null;//岗位代码集合
            List<String> orgIds = null; //组织机构id集合
            List<String> positionTypesIds = null;//岗位类别id集合
            for (Object executorObject : executorList.toArray()) {
                JSONObject executorTemp = (JSONObject) executorObject;
                String userType = executorTemp.get("userType") + "";
                String ids = executorTemp.get("ids") + "";
//                nodeInfo.setUiUserType(userType);
                List<String> tempList = null;
                if (StringUtils.isNotEmpty(ids)) {
                    String[] idsShuZhu = ids.split(",");
                    tempList = Arrays.asList(idsShuZhu);
                }
                if ("SelfDefinition".equalsIgnoreCase(userType)) {//通过业务ID获取自定义用户
                    selfDefId = executorTemp.get("selfDefOfOrgAndSelId") + "";
                } else if ("Position".equalsIgnoreCase(userType)) {
                    positionIds = tempList;
                } else if ("OrganizationDimension".equalsIgnoreCase(userType)) {
                    orgDimensionCodes = tempList;
                } else if ("PositionType".equalsIgnoreCase(userType)) {
                    positionTypesIds = tempList;
                } else if ("Org".equalsIgnoreCase(userType)) {
                    orgIds = tempList;
                }
            }
            if (StringUtils.isNotEmpty(selfDefId) && !Constants.NULL_S.equalsIgnoreCase(selfDefId)) {
                FlowExecutorConfig flowExecutorConfig = flowExecutorConfigDao.findOne(selfDefId);
                String path = flowExecutorConfig.getUrl();
                AppModule appModule = flowExecutorConfig.getBusinessModel().getAppModule();
                String appModuleCode = appModule.getApiBaseAddress();
                String param = flowExecutorConfig.getParam();
                FlowInvokeParams flowInvokeParams = new FlowInvokeParams();
                flowInvokeParams.setId(flowStartVO.getBusinessKey());
                flowInvokeParams.setOrgId("" + flowStartVO.getVariables().get("orgId"));
                flowInvokeParams.setOrgDimensionCodes(orgDimensionCodes);
                flowInvokeParams.setPositionIds(positionIds);
                flowInvokeParams.setJsonParam(param);
                employees = ApiClient.postViaProxyReturnResult(appModuleCode, path, new GenericType<List<Executor>>() {
                }, flowInvokeParams);
            } else {
                String path;
                Map<String, Object> params = new HashMap();
                if (positionTypesIds != null && orgIds != null) { //新增根据（岗位类别+组织机构）获得执行人
                    path = Constants.getExecutorsByPostCatAndOrgUrl();
                    params.put("orgIds", orgIds);
                    params.put("postCatIds", positionTypesIds);
                } else {
                    path = Constants.getBasicPositionGetExecutorsUrl();
                    params.put("orgId", flowStartVO.getVariables().get("orgId"));
                    params.put("orgDimIds", orgDimensionCodes);
                    params.put("positionIds", positionIds);
                }
                String messageLog = "开始调用‘获取执行人’接口，接口url=" + path + ",参数值" + JsonUtils.toJson(params);
                try {
                    employees = ApiClient.getEntityViaProxy(path, new GenericType<List<Executor>>() {
                    }, params);
                } catch (Exception e) {
                    messageLog += "-获取执行人异常：" + e.getMessage();
                    LogUtil.error(messageLog);
                }
            }
            if (employees != null && !employees.isEmpty()) {
                employeeSet.addAll(employees);
                nodeInfo.setExecutorSet(employeeSet);
            }
        }
        result.add(nodeInfo);
        return result;
    }

    /**
     * 递归遍启动节点（主要针对启动节点出口带网关的情况）
     *
     * @param result
     * @param flowStartVO
     * @param definition
     * @param jsonObjectNode
     * @return
     */
    public List<NodeInfo> findXunFanNodesInfo(List<NodeInfo> result, FlowStartVO flowStartVO, FlowDefination flowDefination, Definition definition, JSONObject jsonObjectNode, String businessVName) throws NoSuchMethodException, SecurityException {
        String busType = jsonObjectNode.get("busType") + "";
        String type = jsonObjectNode.get("type") + "";
        String nodeId = jsonObjectNode.get("id") + "";

        if ("ExclusiveGateway".equalsIgnoreCase(busType)) {//如果是系统排他网关
            JSONArray targetNodes = jsonObjectNode.getJSONArray("target");
            List<NodeInfo> resultDefault = new ArrayList<NodeInfo>();
            List<NodeInfo> resultCurrent = new ArrayList<NodeInfo>();
            for (int j = 0; j < targetNodes.size(); j++) {
                JSONObject jsonObject = targetNodes.getJSONObject(j);
                String targetId = jsonObject.getString("targetId");
                JSONObject uel = jsonObject.getJSONObject("uel");
                JSONObject nextNode = definition.getProcess().getNodes().getJSONObject(targetId);
                String busType2 = nextNode.get("busType") + "";
                if (uel != null && !uel.isEmpty()) {
                    String isDefault = uel.get("isDefault") + "";
                    if ("true".equalsIgnoreCase(isDefault)) {
                        if (checkGateway(busType2)) {
                            this.findXunFanNodesInfo(resultDefault, flowStartVO, flowDefination, definition, nextNode, businessVName);
                        } else if ("CallActivity".equalsIgnoreCase((String) nextNode.get("nodeType"))) {
                            result = getCallActivityNodeInfo(flowStartVO, flowDefination, definition, nextNode, result, businessVName);
                        } else {
                            resultDefault = initNodesInfo(resultDefault, flowStartVO, definition, targetId);
                        }
                        continue;
                    }
                    String groovyUel = uel.getString("groovyUel");
                    if (StringUtils.isNotEmpty(groovyUel)) {
                        BusinessModel businessModel = flowDefination.getFlowType().getBusinessModel();
                        Map<String, Object> v = ExpressionUtil.getPropertiesValuesMap(businessModel, flowStartVO.getBusinessKey(), false);
                        if (groovyUel.startsWith("#{")) {// #{开头代表自定义的groovy表达式
                            String conditonFinal = groovyUel.substring(groovyUel.indexOf("#{") + 2,
                                    groovyUel.lastIndexOf("}"));
                            if (ConditionUtil.groovyTest(conditonFinal, v)) {
                                if (checkGateway(busType2)) {
                                    this.findXunFanNodesInfo(resultCurrent, flowStartVO, flowDefination, definition, nextNode, businessVName);
                                } else if ("CallActivity".equalsIgnoreCase((String) nextNode.get("nodeType"))) {
                                    result = getCallActivityNodeInfo(flowStartVO, flowDefination, definition, nextNode, result, businessVName);
                                } else {
                                    resultCurrent = initNodesInfo(resultCurrent, flowStartVO, definition, targetId);
                                }
                                break;
                            }
                        } else {//其他的用UEL表达式验证
                            Object tempResult = ConditionUtil.uelResult(groovyUel, v);
                            if (tempResult instanceof Boolean) {
                                Boolean resultB = (Boolean) tempResult;
                                if (resultB == true) {
                                    if (checkGateway(busType2)) {
                                        this.findXunFanNodesInfo(resultCurrent, flowStartVO, flowDefination, definition, nextNode, businessVName);
                                    } else if ("CallActivity".equalsIgnoreCase((String) nextNode.get("nodeType"))) {
                                        result = getCallActivityNodeInfo(flowStartVO, flowDefination, definition, nextNode, result, businessVName);
                                    } else {
                                        resultCurrent = initNodesInfo(resultCurrent, flowStartVO, definition, targetId);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if ((resultCurrent == null || resultCurrent.isEmpty()) && (resultDefault != null && !resultDefault.isEmpty())) {
                resultCurrent.addAll(resultDefault);
            }
            result.addAll(resultCurrent);
        } else if ("ParallelGateway".equalsIgnoreCase(busType)) {//如果是并行网关
            JSONArray targetNodes = jsonObjectNode.getJSONArray("target");
            List<NodeInfo> resultCurrent = new ArrayList<NodeInfo>();
            for (int j = 0; j < targetNodes.size(); j++) {
                JSONObject jsonObject = targetNodes.getJSONObject(j);
                String targetId = jsonObject.getString("targetId");
                JSONObject nextNode = definition.getProcess().getNodes().getJSONObject(targetId);
                String busType2 = nextNode.get("busType") + "";
                if (checkGateway(busType2)) {
                    this.findXunFanNodesInfo(resultCurrent, flowStartVO, flowDefination, definition, nextNode, businessVName);
                } else if ("CallActivity".equalsIgnoreCase((String) nextNode.get("nodeType"))) {
                    result = getCallActivityNodeInfo(flowStartVO, flowDefination, definition, nextNode, result, businessVName);
                } else {
                    resultCurrent = initNodesInfo(resultCurrent, flowStartVO, definition, targetId);
                }
            }
            result.addAll(resultCurrent);
        } else if ("InclusiveGateway".equalsIgnoreCase(busType)) {//如果是系统包容网关
            JSONArray targetNodes = jsonObjectNode.getJSONArray("target");
            List<NodeInfo> resultDefault = new ArrayList<NodeInfo>();
            List<NodeInfo> resultCurrent = new ArrayList<NodeInfo>();
            for (int j = 0; j < targetNodes.size(); j++) {
                JSONObject jsonObject = targetNodes.getJSONObject(j);
                String targetId = jsonObject.getString("targetId");
                JSONObject uel = jsonObject.getJSONObject("uel");
                JSONObject nextNode = definition.getProcess().getNodes().getJSONObject(targetId);
                String busType2 = nextNode.get("busType") + "";

                if (uel != null && !uel.isEmpty()) {
                    String isDefault = uel.get("isDefault") + "";
                    if ("true".equalsIgnoreCase(isDefault)) {
                        if (checkGateway(busType2)) {
                            this.findXunFanNodesInfo(resultDefault, flowStartVO, flowDefination, definition, nextNode, businessVName);
                        } else if ("CallActivity".equalsIgnoreCase((String) nextNode.get("nodeType"))) {
                            result = getCallActivityNodeInfo(flowStartVO, flowDefination, definition, nextNode, result, businessVName);
                        } else {
                            resultDefault = initNodesInfo(resultDefault, flowStartVO, definition, targetId);
                        }
                        continue;
                    }
                    String groovyUel = uel.getString("groovyUel");
                    if (StringUtils.isNotEmpty(groovyUel)) {
                        BusinessModel businessModel = flowDefination.getFlowType().getBusinessModel();
                        Map<String, Object> v = ExpressionUtil.getPropertiesValuesMap(businessModel, flowStartVO.getBusinessKey(), false);
                        if (groovyUel.startsWith("#{")) {// #{开头代表自定义的groovy表达式
                            String conditonFinal = groovyUel.substring(groovyUel.indexOf("#{") + 2,
                                    groovyUel.lastIndexOf("}"));
                            if (ConditionUtil.groovyTest(conditonFinal, v)) {
                                if (checkGateway(busType2)) {
                                    this.findXunFanNodesInfo(resultCurrent, flowStartVO, flowDefination, definition, nextNode, businessVName);
                                } else if ("CallActivity".equalsIgnoreCase((String) nextNode.get("nodeType"))) {
                                    result = getCallActivityNodeInfo(flowStartVO, flowDefination, definition, nextNode, result, businessVName);
                                } else {
                                    initNodesInfo(resultCurrent, flowStartVO, definition, targetId);
                                }
//                                break;
                            }
                        } else {//其他的用UEL表达式验证
                            Object tempResult = ConditionUtil.uelResult(groovyUel, v);
                            if (tempResult instanceof Boolean) {
                                Boolean resultB = (Boolean) tempResult;
                                if (resultB == true) {
                                    if (checkGateway(busType2)) {
                                        this.findXunFanNodesInfo(resultCurrent, flowStartVO, flowDefination, definition, nextNode, businessVName);
                                    } else if ("CallActivity".equalsIgnoreCase((String) nextNode.get("nodeType"))) {
                                        result = getCallActivityNodeInfo(flowStartVO, flowDefination, definition, nextNode, result, businessVName);
                                    } else {
                                        initNodesInfo(resultCurrent, flowStartVO, definition, targetId);
                                    }
                                }
                            }
                        }
                    }
                }

            }
            result.addAll(resultCurrent);
            if ((result == null || result.isEmpty()) && (resultDefault != null && !resultDefault.isEmpty())) {
                result.addAll(resultDefault);
            }

        } else if ("ManualExclusiveGateway".equalsIgnoreCase(busType)) {//如果是人工排他网关
            throw new RuntimeException("开始节点不允许直接配置人工排他网关节点！");
        } else if ("ServiceTask".equalsIgnoreCase(type)) {//服务任务
            NodeInfo nodeInfo = new NodeInfo();
            nodeInfo.setId(nodeId);
            JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(nodeInfo.getId());
            ServiceTask serviceTaskTemp = (ServiceTask) JSONObject.toBean(currentNode, ServiceTask.class);
            nodeInfo.setName(serviceTaskTemp.getName());
            nodeInfo.setType(serviceTaskTemp.getType());
            nodeInfo.setUserVarName(nodeInfo.getId() + "_ServiceTask");
            nodeInfo.setUiType("radiobox");
            nodeInfo.setFlowTaskType("serviceTask");
            String startUserId = ContextUtil.getSessionUser().getUserId();
//            Map<String,Object> params = new HashMap();
//            params.put("employeeIds",java.util.Arrays.asList(startUserId));
//            String url = Constants.BASIC_SERVICE_URL+ Constants.BASIC_EMPLOYEE_GETEXECUTORSBYEMPLOYEEIDS_URL;
//            List<Executor> employees=ApiClient.getEntityViaProxy(url,new GenericType<List<Executor>>() {},params);

//            List<Executor> employees = flowCommonUtil.getBasicExecutors(Arrays.asList(startUserId));
            List<Executor> employees = flowCommonUtil.getBasicUserExecutors(Arrays.asList(startUserId));
            if (employees != null && !employees.isEmpty()) {//服务任务默认选择流程启动人
                Set<Executor> employeeSet = new HashSet<Executor>();
                employeeSet.addAll(employees);
                nodeInfo.setExecutorSet(employeeSet);
            }
            result.add(nodeInfo);
        } else if ("ReceiveTask".equalsIgnoreCase(type)) {//接收任务
            NodeInfo nodeInfo = new NodeInfo();
            nodeInfo.setId(nodeId);
            JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(nodeInfo.getId());
            ReceiveTask receiveTaskTemp = (ReceiveTask) JSONObject.toBean(currentNode, ReceiveTask.class);
            nodeInfo.setName(receiveTaskTemp.getName());
            nodeInfo.setType(receiveTaskTemp.getType());
            nodeInfo.setUserVarName(nodeInfo.getId() + "_ReceiveTask");
            nodeInfo.setUiType("radiobox");
            nodeInfo.setFlowTaskType("receiveTask");
            String startUserId = ContextUtil.getSessionUser().getUserId();
//            Map<String,Object> params = new HashMap();
//            params.put("employeeIds",java.util.Arrays.asList(startUserId));
//            String url = Constants.BASIC_SERVICE_URL+ Constants.BASIC_EMPLOYEE_GETEXECUTORSBYEMPLOYEEIDS_URL;
//            List<Executor> employees=ApiClient.getEntityViaProxy(url,new GenericType<List<Executor>>() {},params);

//            List<Executor> employees = flowCommonUtil.getBasicExecutors(Arrays.asList(startUserId));
            List<Executor> employees = flowCommonUtil.getBasicUserExecutors(Arrays.asList(startUserId));
            if (employees != null && !employees.isEmpty()) {//服务任务默认选择流程启动人
                Set<Executor> employeeSet = new HashSet<Executor>();
                employeeSet.addAll(employees);
                nodeInfo.setExecutorSet(employeeSet);
            }
            result.add(nodeInfo);
        } else if ("startEvent".equalsIgnoreCase(type)) {//开始节点向下遍历
            JSONArray targetNodes = jsonObjectNode.getJSONArray("target");
            for (int j = 0; j < targetNodes.size(); j++) {
                JSONObject jsonObject = targetNodes.getJSONObject(j);
                String nodeRealId = jsonObject.get("targetId") + "";
                JSONObject jsonObjectReal = definition.getProcess().getNodes().getJSONObject(nodeRealId);
                this.findXunFanNodesInfo(result, flowStartVO, flowDefination, definition, jsonObjectReal, businessVName);
            }
        } else if ("CallActivity".equalsIgnoreCase(type)) {
            result = getCallActivityNodeInfo(flowStartVO, flowDefination, definition, jsonObjectNode, result, businessVName);
        } else {
            result = initNodesInfo(result, flowStartVO, definition, nodeId);
        }
        return result;
    }


    private boolean checkGateway(String busType) {
        boolean result = false;
        if ("ManualExclusiveGateway".equalsIgnoreCase(busType) ||  //人工排他网关
                "exclusiveGateway".equalsIgnoreCase(busType) ||  //排他网关
                "inclusiveGateway".equalsIgnoreCase(busType)  //包容网关
                || "parallelGateWay".equalsIgnoreCase(busType)) { //并行网关
            result = true;
        }
        return result;
    }

    private List<NodeInfo> findStartNextNodes(FlowDefination flowDefination, FlowStartVO flowStartVO) throws NoSuchMethodException, SecurityException {
        List<NodeInfo> result = null;
        if (flowDefination != null) {
            result = new ArrayList<NodeInfo>();
//            String startUEL = flowDefination.getStartUel();
            String versionId = flowDefination.getLastDeloyVersionId();
            FlowDefVersion flowDefVersion = flowCommonUtil.getLastFlowDefVersion(versionId);
            if (flowDefVersion.getFlowDefinationStatus() != FlowDefinationStatus.Activate) {//当前
                List<FlowDefVersion> flowDefVersionsActivates = flowDefVersionDao.findByFlowDefinationIdActivate(flowDefination.getId());
                if (flowDefVersionsActivates != null && !flowDefVersionsActivates.isEmpty()) {
                    flowDefVersion = flowDefVersionsActivates.get(0);
                }
            }
            Definition definition = flowCommonUtil.flowDefinition(flowDefVersion);
            List<StartEvent> startEventList = definition.getProcess().getStartEvent();
            if (startEventList != null && startEventList.size() == 1) {
                StartEvent startEvent = startEventList.get(0);
                JSONObject startEventNode = definition.getProcess().getNodes().getJSONObject(startEvent.getId());
                result = this.findXunFanNodesInfo(result, flowStartVO, flowDefination, definition, startEventNode, null);
                if (!result.isEmpty()) {
                    for (NodeInfo nodeInfo : result) {
                        nodeInfo.setCurrentTaskType(startEvent.getType());
                    }
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
        OperateResult result = OperateResult.operationSuccess("core_00003");
        List<FlowDefVersion> flowDefVersions = flowDefVersionDao.findByFlowDefinationId(id);
        for (FlowDefVersion flowDefVersion : flowDefVersions) {
            String actDeployId = flowDefVersion.getActDeployId();
            List<FlowInstance> flowInstanceList = flowInstanceDao.findByFlowDefVersionId(flowDefVersion.getId());
            if (flowInstanceList != null && !flowInstanceList.isEmpty()) {
                result = OperateResult.operationFailure("10024");
                return result;
            }
            flowDefVersionDao.delete(flowDefVersion);
            if ((actDeployId != null) && (!"".equals(actDeployId))) {
                this.deleteActivtiProcessDefinition(actDeployId, false);
            }
        }
        flowDefinationDao.delete(id);

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
            if (flowDefination == null) {
                return null;
            }
            flowDefVersion = flowDefVersionDao.findOne(flowDefination.getLastVersionId());
        }
        return flowDefVersion;
    }

    public ResponseData resetPositionOfGateway(String id) {
        return  resetPosition(id);
    }


    public ResponseData resetPosition(String id) {
        ResponseData responseData = new ResponseData();
        if (StringUtils.isEmpty(id)) {
            return this.writeErrorLogAndReturnData(null, "参数不能为空");
        }
        FlowDefination flowDefination = flowDefinationDao.findOne(id);
        if (flowDefination == null) {
            return this.writeErrorLogAndReturnData(null, "未找到流程定义！");
        }
        FlowDefVersion flowDefVersion = flowDefVersionDao.findOne(flowDefination.getLastVersionId());
        String defJson = flowDefVersion.getDefJson();
        JSONObject defObj = JSONObject.fromObject(defJson);
        Object pocessKey = defObj.keySet().stream().filter(obj -> "process".equals(obj.toString())).findFirst().orElse(null);
        if (pocessKey == null) {
            return this.writeErrorLogAndReturnData(null, "数据格式1存在问题，转换失败！");
        }
        JSONObject pocessObj = JSONObject.fromObject(defObj.get(pocessKey));
        Object nodesKey = pocessObj.keySet().stream().filter(obj -> "nodes".equals(obj.toString())).findFirst().orElse(null);
        if (nodesKey == null) {
            return this.writeErrorLogAndReturnData(null, "数据格式2存在问题，转换失败！");
        }
        JSONObject nodeObj = JSONObject.fromObject(pocessObj.get(nodesKey));

        List<Integer> xList = new ArrayList<Integer>();
        List<Integer> yList = new ArrayList<Integer>();
        nodeObj.keySet().forEach(obj -> {
            JSONObject positionObj = JSONObject.fromObject(nodeObj.get(obj));
            FlowNodeVO node = (FlowNodeVO) JSONObject.toBean(positionObj, FlowNodeVO.class);
            xList.add(node.getX());
            yList.add(node.getY());
        });
        Integer xMin = Collections.min(xList);
        Integer yMin = Collections.min(yList);

        nodeObj.keySet().forEach(obj -> {
            JSONObject positionObj = JSONObject.fromObject(nodeObj.get(obj));
            FlowNodeVO node = (FlowNodeVO) JSONObject.toBean(positionObj, FlowNodeVO.class);
            node.setX(node.getX() - xMin);
            node.setY(node.getY() - yMin);
            nodeObj.put(obj, new JSONObject().fromObject(node).toString());
        });
        pocessObj.put(nodesKey, nodeObj);
        defObj.put(pocessKey, pocessObj);
        String newDefJson = new JSONObject().fromObject(defObj).toString();
        flowDefVersion.setDefJson(newDefJson);
        flowDefVersionDao.save(flowDefVersion);

        responseData.setMessage("重置成功！");
        return responseData;
    }


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
            deploymentBuilder.addString(name + ".bpmn", xml);
            deploy = deploymentBuilder.deploy();
            transactionManager.commit(status);

        } catch (Exception e) {
            e.printStackTrace();
            transactionManager.rollback(status);
            throw e;
        }
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
        FlowInstance flowInstance = flowInstanceDao.findByActInstanceId(instance.getId());
        initTask(flowInstance, variables);
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

    private void initTask(FlowInstance flowInstance, Map<String, Object> variables) {
        flowTaskTool.initTask(flowInstance, null, null, variables);
    }

    public OperateResultWithData<FlowDefination> changeStatusOfGateway(String id, FlowDefinationStatus status) {
        return  changeStatus(id,status);
    }

    public OperateResultWithData<FlowDefination> changeStatus(String id, FlowDefinationStatus status) {
        FlowDefination flowDefination = flowDefinationDao.findOne(id);
        if (flowDefination == null) {
            return OperateResultWithData.operationFailure("10003");
        }
        OperateResultWithData resultWithData = flowTaskTool.statusCheck(status, flowDefination.getFlowDefinationStatus());
        if (resultWithData != null && resultWithData.notSuccessful()) {
            return resultWithData;
        }
        flowDefination.setFlowDefinationStatus(status);
        flowDefinationDao.save(flowDefination);
        //10018=冻结成功
        //10019=激活成功
        return OperateResultWithData.operationSuccess(status == FlowDefinationStatus.Freeze ? "10018" : "10019");
    }

    public OperateResultWithData<FlowDefination> validateExpression(String flowTypeId, String expression)
            throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException,
            InvocationTargetException {
        if (expression.startsWith("#{")) {// #{开头代表自定义的groovy表达式
            String conditonFinal = expression.substring(expression.indexOf("#{") + 2,
                    expression.lastIndexOf("}"));
            if (StringUtils.isNotEmpty(flowTypeId)) {
                FlowType flowType = flowTypeDao.findOne(flowTypeId);
                if (flowType == null) {
                    return OperateResultWithData.operationFailure("10007");
                }
                BusinessModel businessModel = flowType.getBusinessModel();
                Boolean result = ExpressionUtil.validate(businessModel, conditonFinal);
                if (result == null) {
                    return OperateResultWithData.operationFailure("10025");
                } else {
                    return OperateResultWithData.operationSuccess("10026");
                }
            } else {
                return OperateResultWithData.operationFailure("10007");
            }
        } else {
            return OperateResultWithData.operationFailure("10025");
        }
    }

    public FlowDefination findByKey(String key) {
        return flowDefinationDao.findByDefKey(key);
    }

    private List<NodeInfo> getCallActivityNodeInfo(FlowStartVO flowStartVO, FlowDefination flowDefination, Definition definition, JSONObject jsonObjectNode, List<NodeInfo> result, String businessVName)
            throws NoSuchMethodException, SecurityException {
        JSONObject normal = jsonObjectNode.getJSONObject(Constants.NODE_CONFIG).getJSONObject("normal");
        String callActivityDefKey = (String) normal.get("callActivityDefKey");
        if (StringUtils.isEmpty(businessVName)) {
            businessVName = "/" + definition.getProcess().getId() + "/" + jsonObjectNode.get("id");
        } else {
            businessVName += "/" + definition.getProcess().getId() + "/" + jsonObjectNode.get("id");
        }
        String currentVersionId = (String) normal.get("currentVersionId");
        FlowDefVersion flowDefVersion = flowCommonUtil.getLastFlowDefVersion(currentVersionId);
        if (flowDefVersion != null && flowDefVersion.getFlowDefinationStatus() == FlowDefinationStatus.Activate) {
            Definition definitionSon = flowCommonUtil.flowDefinition(flowDefVersion);
            List<StartEvent> startEventList = definitionSon.getProcess().getStartEvent();
            if (startEventList != null && startEventList.size() == 1) {
                StartEvent startEvent = startEventList.get(0);
                JSONObject startEventNode = definitionSon.getProcess().getNodes().getJSONObject(startEvent.getId());
                result = this.findXunFanNodesInfo(result, flowStartVO, flowDefination, definitionSon, startEventNode, businessVName);
                if (!result.isEmpty()) {
                    for (NodeInfo nodeInfo : result) {
                        nodeInfo.setCurrentTaskType(startEvent.getType());
                        if (StringUtils.isEmpty(nodeInfo.getCallActivityPath())) {
                            businessVName += "/" + callActivityDefKey;
                            nodeInfo.setCallActivityPath(businessVName);
                        }
                    }
                }
            }
        } else {
            throw new FlowException("找不到子流程,或子流程处于挂起状态");
        }
        return result;
    }

    public void testStart(String flowKey, String businessKey) {
        Map<String, Object> v = new HashMap<>();
        v.put("UserTask_2_Normal", "8A6A1592-4A95-11E7-A011-960F8309DEA7");
        this.runtimeService.startProcessInstanceByKey(flowKey, businessKey, v);
        DefaultBusinessModel defaultBusinessModel = defaultBusinessModelDao.findOne(businessKey);
        defaultBusinessModel.setFlowStatus(FlowStatus.INPROCESS);
        defaultBusinessModelDao.save(defaultBusinessModel);
    }

    public ResponseData writeErrorLogAndReturnData(Exception e, String msg) {
        if (e != null) {
            LogUtil.error(e.getMessage());
        }
        ResponseData responseData = new ResponseData();
        responseData.setSuccess(false);
        responseData.setMessage(msg);
        return responseData;
    }
}
