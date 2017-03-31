package com.ecmp.flow.service;

import com.ecmp.core.service.BaseService;
import com.ecmp.flow.api.IFlowDefinationService;
import com.ecmp.flow.dao.FlowDefVersionDao;
import com.ecmp.flow.dao.FlowDefinationDao;
import com.ecmp.flow.dao.FlowTypeDao;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.entity.FlowDefination;
import com.ecmp.flow.entity.FlowType;
import com.ecmp.vo.OperateResult;
import org.activiti.engine.*;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

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
public class FlowDefinationService extends BaseService<FlowDefination, String> implements IFlowDefinationService {

    private final Logger logger = LoggerFactory.getLogger(FlowDefinationService.class);

    @Autowired
    private FlowDefVersionDao flowDefVersionDao;

    @Autowired
    private FlowDefinationDao flowDefinationDao;

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
    public OperateResult<FlowDefination> save(FlowDefination entity) {
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
        OperateResult<FlowDefination> operateResult;
        if (isNew) {
            operateResult = OperateResult.OperationSuccess("core_00001");
        } else {
            operateResult = OperateResult.OperationSuccess("core_00002");
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
    public String deployByVersionId(String id) throws UnsupportedEncodingException {
        String deployId = null;
        FlowDefVersion  flowDefVersion = flowDefVersionDao.findOne(id);
        Deployment deploy = this.deploy(flowDefVersion.getName(),flowDefVersion.getDefXml());
        deployId = deploy.getId();
        flowDefVersion.setActDeployId(deployId);//回写流程发布ID
        ProcessDefinitionEntity activtiFlowDef = getProcessDefinitionByDeployId(deployId);
        flowDefVersion.setVersionCode(activtiFlowDef.getVersion());//回写版本号
        flowDefVersionDao.save(flowDefVersion);
        return deployId;
    }


    /**
     * 将流程定义发布到流程引擎，建立关联
     * @param name  流程名称
     * @param xml   流程定义XML
     * @return  流程发布
     * @throws UnsupportedEncodingException
     */
    private Deployment deploy(String name, String xml) throws UnsupportedEncodingException {
        // InputStream stream = new ByteArrayInputStream(xml.getBytes("utf-8"));
        DeploymentBuilder deploymentBuilder = this.repositoryService.createDeployment();
        deploymentBuilder.name(name);
        deploymentBuilder.addString(name, xml);
        // deploymentBuilder.addInputStream("bpmn20.xml", stream);
        Deployment deploy = deploymentBuilder.deploy();
        return deploy;
    }

    /**
     * 获取Activiti流程定义实体
     * @param deployId 流程部署ID
     * @return Activiti流程定义实体
     */
    private ProcessDefinitionEntity getProcessDefinitionByDeployId(String deployId) {
        ProcessDefinition proDefinition = (ProcessDefinition) this.repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployId).singleResult();
        if (proDefinition == null)
            return null;
        return getProcessDefinitionByDefId(proDefinition.getId());
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



}
