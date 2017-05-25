package com.ecmp.flow.service;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.core.service.BaseService;
import com.ecmp.flow.api.IFlowDefVersionService;
import com.ecmp.flow.dao.FlowDefVersionDao;
import com.ecmp.flow.dao.FlowDefinationDao;
import com.ecmp.flow.dao.FlowTypeDao;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.entity.FlowDefination;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.flow.entity.FlowType;
import com.ecmp.flow.util.XmlUtil;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.flow.vo.bpmn.Process;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import org.activiti.engine.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import java.util.Collection;
import java.util.List;

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
public class FlowDefVersionService extends BaseEntityService<FlowDefVersion> implements IFlowDefVersionService {

    private final Logger logger = LoggerFactory.getLogger(FlowDefVersion.class);

    protected BaseEntityDao<FlowDefVersion> getDao(){
        return this.flowDefVersionDao;
    }
    @Autowired
    private FlowDefVersionDao flowDefVersionDao;

    @Autowired
    private FlowDefinationDao flowDefinationDao;

    @Autowired
    private FlowTypeDao flowTypeDao;

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
    public OperateResultWithData<FlowDefVersion> save(FlowDefVersion entity) {
        FlowDefination flowDefination = entity.getFlowDefination();
        if(flowDefination == null){ //流程版本必须指定流程定义
            return null;
        }
        flowDefination = flowDefinationDao.findOne(flowDefination.getId());
        boolean isNew = entity.isNew();
        if (entity.isNew()) {
            preInsert(entity);
        } else {
            preUpdate(entity);
            entity.setId(null);//更改时默认版本向上加+,重新建立一条版本数据
            entity.setVersionCode(entity.getVersionCode()+1);
        }
        entity.setFlowDefination(flowDefination);
        flowDefVersionDao.save(entity);
        logger.info("Saved FlowDefVersion id is {}", entity.getId());
        flowDefination.setLastVersionId(entity.getId());
        flowDefinationDao.save(flowDefination);
        logger.info("Saved FlowDefination id is {}", flowDefination.getId());
        OperateResultWithData<FlowDefVersion> operateResult;
        if (isNew) {
            operateResult = OperateResultWithData.OperationSuccess("core_00001");
        } else {
            operateResult = OperateResultWithData.OperationSuccess("core_00002");
        }
        operateResult.setData(entity);
        return operateResult;
    }

    /**
     * 新增修改操作,保存前端json，
     * 转换成标准的BPMN
     *
     * @param definition
     * @return
     */
    public OperateResultWithData<FlowDefVersion> save(Definition definition) throws JAXBException {
        String flowTypeId =definition.getFlowTypeId();
        FlowType flowType = flowTypeDao.findOne(flowTypeId);
        if(flowType == null){ //流程版本必须指定流程类型
            return  OperateResultWithData.OperationFailure("10007");
        }
        Process process = definition.getProcess();
        FlowDefination flowDefination = null;
        if(definition.getId()!=null){
            flowDefination = flowDefinationDao.findOne(definition.getId());
        }

        String defBpm = XmlUtil.serialize(definition);
        FlowDefVersion entity = null;
        boolean isNew =true;
        if (flowDefination ==null) {//定义为空
//            preInsert(flowDefination);

            flowDefination = new FlowDefination();
            flowDefination.setName(process.getName());
            flowDefination.setDefKey(process.getId());
            flowDefination.setStartUel(process.getStartUEL());
            flowDefination.setFlowType(flowType);
            flowDefination.setOrgId(definition.getOrgId());
           // flowDefination.setCurrentFlowDefVersion(1L);
            flowDefinationDao.save(flowDefination);
             entity = new FlowDefVersion();
            entity.setActDefId(process.getId());
            entity.setName(process.getName());
            entity.setDefKey(process.getId());
//            entity.setVersionCode(1);
            entity.setFlowDefination(flowDefination);
            entity.setDefJson(definition.getDefJson());
            entity.setDefBpmn(defBpm);
            entity.setDefXml(defBpm);//后期添加自定义UEL转换
            flowDefVersionDao.save(entity);
            logger.info("Saved FlowDefVersion id is {}", entity.getId());
            flowDefination.setLastVersionId(entity.getId());
            flowDefinationDao.save(flowDefination);
            logger.info("Saved FlowDefination id is {}", flowDefination.getId());
        } else {
            if(process.getId()!=null){
                entity =  flowDefVersionDao.findOne(process.getId());
            }
            if(entity!=null){//版本不为空
                entity.setDefJson(definition.getDefJson());
                entity.setDefBpmn(defBpm);
                entity.setDefXml(defBpm);//后期添加自定义UEL转换
                if(StringUtils.isNotEmpty(entity.getActDeployId())){//对于已经有发布ID的对象进行拷贝
                    entity = (FlowDefVersion)entity.clone();
                    entity.setId(null);
//                    entity.setVersionCode(entity.getVersionCode()+1);
                }
                flowDefVersionDao.save(entity);
                logger.info("Saved FlowDefVersion id is {}", entity.getId());
            }else {//版本为空
                entity = new FlowDefVersion();
                entity.setActDefId(process.getId());
                entity.setName(process.getName());
                entity.setDefKey(process.getId());
//                entity.setVersionCode(1);
                entity.setFlowDefination(flowDefination);
                entity.setDefJson(definition.getDefJson());
                entity.setDefBpmn(defBpm);
                entity.setDefXml(defBpm);//后期添加自定义UEL转换
                flowDefVersionDao.save(entity);
                logger.info("Saved FlowDefVersion id is {}", entity.getId());
                flowDefination.setLastVersionId(entity.getId());
                flowDefinationDao.save(flowDefination);
                logger.info("Saved FlowDefination id is {}", flowDefination.getId());
            }

        }
        OperateResultWithData<FlowDefVersion> operateResult;
        if (isNew) {
            operateResult = OperateResultWithData.OperationSuccess("core_00001");
        } else {
            operateResult = OperateResultWithData.OperationSuccess("core_00002");
        }
        operateResult.setData(entity);
        return operateResult;
    }

    /**
     * 数据删除操作
     * 清除有关联的流程版本及对应的流程引擎数据
     * @param id 待操作数据
     */
    @Override
    public OperateResult delete(String id) {
        FlowDefVersion  entity = flowDefVersionDao.findOne(id);
        String actDeployId = entity.getActDeployId();
        this.deleteActivtiProcessDefinition(actDeployId,false);
        flowDefVersionDao.delete(entity);
        OperateResult result =  OperateResult.OperationSuccess("core_00003");
        return result;
    }

    /**
     * 通过ID批量删除
     * @param ids
     */
    @Override
    public void delete(Collection<String> ids ) {
        for (String id : ids) {
            this.delete(id);
        }
    }
//    /**
//     * 主键删除
//     *
//     * @param id 主键
//     * @return 返回操作结果对象
//     */
//    public OperateResult deleteById(String id) {
//        FlowDefVersion entity = flowDefVersionDao.findOne(id);
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
//    public FlowDefVersion save(FlowDefVersion entity) {
//        //        entity.setLastVersionId(0);
//        FlowDefination flowDefination = entity.getFlowDefination();
//        if(flowDefination == null){ //流程版本必须指定流程定义
//            return null;
//        }
//        flowDefination = flowDefinationDao.findOne(flowDefination.getId());
//        if (entity.isNew()) {
//            preInsert(entity);
//        } else {
//            preUpdate(entity);
//            entity.setId(null);//更改时默认版本向上加+,重新建立一条版本数据
//            entity.setVersionCode(entity.getVersionCode()+1);
//        }
//
//        if(flowDefination!=null){
//            entity.setFlowDefination(flowDefination);
//            flowDefVersionDao.save(entity);
//            flowDefination.setLastVersionId(entity.getId());
//            flowDefinationDao.save(flowDefination);
//        }
//        logger.debug("Saved FlowDefVersion id is {}", entity.getId());
//        return entity;
//    }


//    public FlowDefVersion findLastByDef(FlowDefination flowDefination){
//        FlowDefVersion flowDefVersion = flowDefVersionDao.findByDefIdAndVersionCode(flowDefination.getId(),flowDefination.getl);
//        return
//    }
}
