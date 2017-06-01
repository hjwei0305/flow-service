package com.ecmp.flow.service;

import com.ecmp.context.ContextUtil;
import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.core.service.BaseService;
import com.ecmp.flow.api.IFlowDefinationService;
import com.ecmp.flow.util.TaskStatus;
import com.ecmp.flow.dao.*;
import com.ecmp.flow.entity.*;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import org.activiti.engine.*;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
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

    protected BaseEntityDao<FlowDefination> getDao(){
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
    private HistoryService historyService;

    @Autowired
    private ProcessEngine processEngine;

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
        if(flowDefVersion!=null){
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
     * @param id
     * @return  流程发布ID
     */
    @Override
    @Transactional
    public String deployById(String id) throws UnsupportedEncodingException {
        String deployId = null;
        FlowDefination  flowDefination = flowDefinationDao.findOne(id);
        if(flowDefination!=null){
          String  versionId =  flowDefination.getLastVersionId();
            deployId  =  deployByVersionId(versionId);
        }
        return deployId;
    }

    /**
     * 通过流程版本ID发布流程
     * @param id
     * @return  流程发布ID
     */
    @Override
//    @Transactional
    public String deployByVersionId(String id) throws UnsupportedEncodingException {
        String deployId = null;
        FlowDefVersion  flowDefVersion = flowDefVersionDao.findOne(id);
        Deployment deploy=null;
             deploy = this.deploy(flowDefVersion.getName(),flowDefVersion.getDefXml());
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
     * @param id 流程id
     * @param businessKey 业务KEY
     * @param variables  其他参数
     * @return 流程实例
     */
    @Override
    public FlowInstance startById(String id,String businessKey, Map<String, Object> variables){
        return this.startById(id,null, businessKey, variables);
    }

    /**
     * 通过ID启动流程实体
     * @param id 流程id
     * @param startUserId 流程启动人
     * @param businessKey 业务KEY
     * @param variables  其他参数
     * @return 流程实例
     */
    @Override
    public FlowInstance startById(String id,String startUserId,String businessKey, Map<String, Object> variables){
        FlowInstance flowInstance = null;

        FlowDefination  flowDefination = flowDefinationDao.findOne(id);
        if(flowDefination != null){
            String  versionId =  flowDefination.getLastVersionId();
            FlowDefVersion  flowDefVersion =  flowDefVersionDao.findOne(versionId);
            if(flowDefVersion != null && flowDefVersion.getActDefId() != null){
                String proessDefId = flowDefVersion.getActDefId();
                ProcessInstance processInstance = null;
                if((startUserId!=null) && (!"".equals(startUserId))){
                    processInstance = this.startFlowById( proessDefId, startUserId, businessKey, variables);
                }else {
                    processInstance = this.startFlowById( proessDefId, businessKey, variables);
                }

                flowInstance = new FlowInstance();
                flowInstance.setBusinessId(processInstance.getBusinessKey());
                flowInstance.setFlowDefVersion(flowDefVersion);
                flowInstance.setStartDate(new Date());

//                String processInstanceName = processInstance.getName();
//                if(processInstanceName == null){
//                    processInstanceName = processInstance.getProcessDefinitionKey();
//                }
                flowInstance.setFlowName(flowDefVersion.getName()+":"+businessKey);
                flowInstance.setActInstanceId(processInstance.getId());
                flowInstanceDao.save(flowInstance);
                initTask(processInstance);
            }
        }
        return flowInstance;
    }

    /**
     * 通过Key启动流程实体
     * @param key 定义Key
     * @param businessKey 业务KEY
     * @param variables  其他参数
     * @return 流程实例
     */
    @Override
    public FlowInstance startByKey(String key,String businessKey, Map<String, Object> variables){
        return this.startByKey(key,null, businessKey, variables);
//        return null;
    }

    /**
     * 通过Key启动流程实体
     * @param key 定义Key
     * @param startUserId 流程启动人
     * @param businessKey 业务KEY
     * @param variables  其他参数
     * @return 流程实例
     */
    @Override
    public FlowInstance startByKey(String key,String startUserId,String businessKey, Map<String, Object> variables){
        FlowInstance flowInstance = null;

        FlowDefination  flowDefination = flowDefinationDao.findByDefKey(key);
        if(flowDefination != null){
            String  versionId =  flowDefination.getLastVersionId();
            FlowDefVersion  flowDefVersion =  flowDefVersionDao.findOne(versionId);
            if(flowDefVersion != null && flowDefVersion.getActDefId() != null){
                String proessDefId = flowDefVersion.getActDefId();
                ProcessInstance processInstance = null;
                if((startUserId!=null) && (!"".equals(startUserId))){
                    processInstance = this.startFlowByKey( key, startUserId, businessKey, variables);
                }else {
                    processInstance = this.startFlowByKey( key, businessKey, variables);
                }

                flowInstance = new FlowInstance();
                flowInstance.setBusinessId(processInstance.getBusinessKey());
                flowInstance.setFlowDefVersion(flowDefVersion);
                flowInstance.setStartDate(new Date());
                flowInstance.setFlowName(flowDefVersion.getName()+":"+businessKey);
                flowInstance.setActInstanceId(processInstance.getId());
                flowInstanceDao.save(flowInstance);
                initTask(processInstance);
            }
        }
        return flowInstance;
    }

    /**
     * 数据删除操作
     * 并清除有关联的流程引擎数据
     * @param id 待操作数据ID
     */
    @Override
    public OperateResult delete(String id) {
        List<FlowDefVersion> flowDefVersions = flowDefVersionDao.findByFlowDefinationId(id);
        for(FlowDefVersion flowDefVersion:flowDefVersions){
            String actDeployId = flowDefVersion.getActDeployId();
            if((actDeployId!=null) && (!"".equals(actDeployId))){
                this.deleteActivtiProcessDefinition(actDeployId,false);
                flowDefVersionDao.delete(flowDefVersion);
            }
        }
        flowDefinationDao.delete(id);
        OperateResult result =  OperateResult.OperationSuccess("core_00003");
        return result;
    }

    /**
     * 通过ID批量删除
     * @param ids
     */
    @Override
    public void delete(Collection<String>  ids) {
        for (String id : ids) {
            this.delete(id);
        }
    }

    public FlowDefVersion getFlowDefVersion(String id,Integer versionCode){
        FlowDefVersion flowDefVersion = null;
        if(versionCode > -1){
            flowDefVersion = flowDefVersionDao.findByDefIdAndVersionCode(id,versionCode);
        }else {
            FlowDefination  flowDefination = flowDefinationDao.findOne(id);           ;
            flowDefVersion = flowDefVersionDao.findOne( flowDefination.getLastVersionId());
        }
        return  flowDefVersion;
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
     *使用部署ID，删除流程引擎数据定义
     * @param deploymentId  发布ID
     * @param force 是否强制删除（能删除启动的流程，会删除和当前规则相关的所有信息，正在执行的信息，也包括历史信息）
     */
    private void deleteActivtiProcessDefinition(String deploymentId,Boolean force ){
        /*
         * 不带级联的删除
         * 只能删除没有启动的流程，如果流程启动，就会抛出异常
         */
        if(force){
            /*
         * 能级联的删除
         * 能删除启动的流程，会删除和当前规则相关的所有信息，正在执行的信息，也包括历史信息
         */
            processEngine.getRepositoryService()//与流程定义和部署对象相关的Service
                    .deleteDeployment(deploymentId, true);
        }else{
            processEngine.getRepositoryService()//与流程定义和部署对象相关的Service
                    .deleteDeployment(deploymentId);
        }
    }

    /**
     * 通过Key删除对应的流程定义
     * @param processDefinitionKey 定义流程定义的key
     * @param force 是否强制删除
     */
    private void deleteActivtiProcessDefinitionByKey(String processDefinitionKey,Boolean force ) {
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
     * @param name  流程名称
     * @param xml   流程定义XML
     * @return  流程发布
     * @throws UnsupportedEncodingException
     */
//    @Transactional( propagation= Propagation.REQUIRES_NEW)
    public Deployment deploy(String name, String xml) throws UnsupportedEncodingException {
        // InputStream stream = new ByteArrayInputStream(xml.getBytes("utf-8"));
        Deployment deploy=null;
        org.springframework.orm.jpa.JpaTransactionManager  transactionManager =(org.springframework.orm.jpa.JpaTransactionManager) ContextUtil.getApplicationContext().getBean("transactionManager");
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW); // 事物隔离级别，开启新事务，这样会比较安全些。
        TransactionStatus status = transactionManager.getTransaction(def); // 获得事务状态
        try {
            //逻辑代码，可以写上你的逻辑处理代码
            DeploymentBuilder deploymentBuilder = this.repositoryService.createDeployment();
//            deploymentBuilder.name(name);3
            deploymentBuilder.addString(name+".bpmn", xml);
            // deploymentBuilder.addInputStream("bpmn20.xml", stream);
            deploy= deploymentBuilder.deploy();
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
     * @param deployId 流程部署ID
     * @return Activiti流程定义实体
     */
    private ProcessDefinitionEntity getProcessDefinitionByDeployId(String deployId) {
        org.springframework.orm.jpa.JpaTransactionManager transactionManager = (org.springframework.orm.jpa.JpaTransactionManager) ContextUtil.getApplicationContext().getBean("transactionManager");
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW); // 事物隔离级别，开启新事务，这样会比较安全些。
        TransactionStatus status = transactionManager.getTransaction(def); // 获得事务状态
        ProcessDefinitionEntity  result = null;
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
     * @param proessDefId 流程定义ID
     * @param variables  其他参数
     * @return Activiti流程定义实体
     */
    private ProcessInstance startFlowById(String proessDefId, Map<String, Object> variables) {
        ProcessInstance instance = this.runtimeService.startProcessInstanceById(proessDefId, variables);
        initTask(instance);
        return instance;
    }

    /**
     * 通过Activiti流程定义ID启动流程实体
     * @param proessDefId 流程定义ID
     * @param startUserId 流程启动人
     * @param businessKey 业务KEY
     * @param variables  其他参数
     * @return Activiti流程定义实体
     */
    private ProcessInstance startFlowById(String proessDefId,String startUserId,String businessKey, Map<String, Object> variables) {
        // 用来设置启动流程的人员ID，引擎会自动把用户ID保存到activiti:initiator中
        if((startUserId!=null) && (!"".equals(startUserId))){
            identityService.setAuthenticatedUserId(startUserId);
        }
        ProcessInstance instance = this.runtimeService.startProcessInstanceById(proessDefId,businessKey,variables);

        return instance;
    }

    /**
     * 通过Activiti流程定义ID启动流程实体
     * @param proessDefId 流程定义ID
     * @param businessKey 业务KEY
     * @param variables  其他参数
     * @return Activiti流程定义实体
     * @return
     */
    private ProcessInstance startFlowById(String proessDefId, String businessKey, Map<String, Object> variables) {
        return startFlowById( proessDefId,null, businessKey,  variables);
    }

    /**
     * 通过Activiti流程定义Key启动流程实体
     * @param processDefKey 流程定义Key
     * @param variables  其他参数
     * @return Activiti流程定义实体
     * @return
     */
    private ProcessInstance startFlowByKey(String processDefKey, Map<String, Object> variables) {
        ProcessInstance instance = this.runtimeService.startProcessInstanceByKey(processDefKey, variables);

        return instance;
    }

    /**
     * 通过Activiti流程定义Key启动流程实体
     * @param processDefKey 流程定义Key
     * @param startUserId 流程启动人
     * @param businessKey 业务KEY
     * @param variables  其他参数
     * @return Activiti流程定义实体
     */
    private ProcessInstance startFlowByKey(String processDefKey,String startUserId,String businessKey, Map<String, Object> variables) {
        // 用来设置启动流程的人员ID，引擎会自动把用户ID保存到activiti:initiator中
        if((startUserId!=null) && (!"".equals(startUserId))){
            identityService.setAuthenticatedUserId(startUserId);
        }
        ProcessInstance instance = this.runtimeService.startProcessInstanceByKey(processDefKey,businessKey,variables);
        return instance;
    }

    /**
     * 通过Activiti流程定义Key启动流程实体
     * @param processDefKey 流程定义Key
     * @param businessKey 业务KEY
     * @param variables  其他参数
     * @return Activiti流程定义实体
     */
    private ProcessInstance startFlowByKey(String processDefKey, String businessKey, Map<String, Object> variables) {
        return startFlowByKey( processDefKey,null, businessKey,  variables);
    }

    private void initTask(ProcessInstance instance){
        FlowInstance flowInstance = flowInstanceDao.findByActInstanceId(instance.getId());
        List<Task> tasks = new ArrayList<Task>();
        // 根据当流程实例查询任务
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(instance.getId()).active().list();
         String flowName = flowInstance.getFlowName();
//        if(flowName == null){
//            flowName = instance.getProcessDefinitionKey();
//        }
        if(taskList!=null && taskList.size()>0){
            for(Task task:taskList){
                    if(task.getAssignee()!=null && !"".equals(task.getAssignee())){
                        List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(task.getId());
                        for(IdentityLink identityLink:identityLinks){
                            FlowTask  flowTask = new FlowTask();
                            flowTask.setFlowDefinitionId(flowInstance.getFlowDefVersion().getFlowDefination().getId());
                            flowTask.setFlowName(flowName);
                            flowTask.setTaskName(task.getName());
                            flowTask.setActTaskId(task.getId());
                            flowTask.setOwnerAccount(task.getOwner());
                            flowTask.setPriority(task.getPriority());
                            flowTask.setExecutorAccount(identityLink.getUserId());
                            flowTask.setActType(identityLink.getType());
                            flowTask.setDepict(task.getDescription());
                            flowTask.setTaskStatus(TaskStatus.INIT.toString());
                            flowTask.setActTaskDefKey(task.getTaskDefinitionKey());
                            flowTask.setFlowInstance(flowInstance);
                            flowTaskDao.save(flowTask);
                        }
                    }else{
                        FlowTask  flowTask = new FlowTask();
                        flowTask.setFlowDefinitionId(flowInstance.getFlowDefVersion().getFlowDefination().getId());
                        flowTask.setFlowName(flowName);
                        flowTask.setTaskName(task.getName());
                        flowTask.setActTaskId(task.getId());
                        flowTask.setOwnerAccount(task.getOwner());
                        flowTask.setPriority(task.getPriority());
                        flowTask.setExecutorAccount(task.getAssignee());
                        flowTask.setDepict(task.getDescription());
                        flowTask.setActType("assignee");
                        flowTask.setTaskStatus(TaskStatus.INIT.toString());
                        flowTask.setActTaskDefKey(task.getTaskDefinitionKey());
                        flowTask.setFlowInstance(flowInstance);
                        flowTaskDao.save(flowTask);
                    }


//                flowTask.setCandidateAccount(instance.get);

            }
        }

    }

}
