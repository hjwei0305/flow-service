package com.ecmp.flow.service;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.flow.api.IFlowDefVersionService;
import com.ecmp.flow.constant.FlowDefinationStatus;
import com.ecmp.flow.dao.FlowDefVersionDao;
import com.ecmp.flow.dao.FlowDefinationDao;
import com.ecmp.flow.dao.FlowInstanceDao;
import com.ecmp.flow.dao.FlowTypeDao;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.entity.FlowDefination;
import com.ecmp.flow.entity.FlowInstance;
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
 * 实现功能：流程版本明细对象
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
    private FlowInstanceDao flowInstanceDao;

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
            operateResult = OperateResultWithData.operationSuccess("core_00001");
        } else {
            operateResult = OperateResultWithData.operationSuccess("core_00002");
        }
        operateResult.setData(entity);
        return operateResult;
    }

    public OperateResultWithData<FlowDefVersion> changeStatus(String id,FlowDefinationStatus  status){
        FlowDefVersion flowDefVersion = flowDefVersionDao.findOne(id);
        if(flowDefVersion == null){
            return  OperateResultWithData.operationFailure("10003");
        }
        if(status==FlowDefinationStatus.Freeze){
            if (flowDefVersion.getFlowDefinationStatus() != FlowDefinationStatus.Activate) {
                //10021=当前非激活状态，禁止冻结！
                return  OperateResultWithData.operationFailure("10021");
            }
        }else if (status==FlowDefinationStatus.Activate){
            if (flowDefVersion.getFlowDefinationStatus() != FlowDefinationStatus.Freeze) {
                //10020=当前非冻结状态，禁止激活！
                return  OperateResultWithData.operationFailure("10020");
            }
        }
        flowDefVersion.setFlowDefinationStatus(status);
        flowDefVersionDao.save(flowDefVersion);
        //10018=冻结成功
        //10019=激活成功
        return  OperateResultWithData.operationSuccess(status==FlowDefinationStatus.Freeze?"10018":"10019");
    }


    /**
     * 新增修改操作,保存前端json，
     * 转换成标准的BPMN
     *
     * @param definition
     * @return
     */
    public OperateResultWithData<FlowDefVersion> save(Definition definition) throws JAXBException, CloneNotSupportedException {
        String flowTypeId =definition.getFlowTypeId();
        FlowType flowType = flowTypeDao.findOne(flowTypeId);
        if(flowType == null){ //流程版本必须指定流程类型
            return  OperateResultWithData.operationFailure("10007");
        }
        Process process = definition.getProcess();
        FlowDefination flowDefination = null;
        Boolean canAsSubProcess = definition.getSubProcess();
        if(definition.getId()!=null){
            flowDefination = flowDefinationDao.findOne(definition.getId());
        }
        //通过key来查找对应的流程定义是否已经存在
        if(flowDefination == null){
            flowDefination = flowDefinationDao.findByDefKey(process.getId());
            if(flowDefination!=null){
                return  OperateResultWithData.operationFailure("10028");
            }
        }
        String defBpm = XmlUtil.serialize(definition);
        FlowDefVersion entity = null;
        boolean isNew =true;
        if (flowDefination ==null) {//定义为空
//            preInsert(flowDefination);

            flowDefination = new FlowDefination();
            entity = new FlowDefVersion();

            flowDefination.setName(process.getName());
            flowDefination.setDefKey(process.getId());
            if(process.getStartUEL()!=null){
               // flowDefination.setStartUel(process.getStartUEL().toString());
                entity.setStartUel(process.getStartUEL().toString());
            }
            flowDefination.setFlowType(flowType);
            flowDefination.setOrgId(definition.getOrgId());
            flowDefination.setOrgCode(definition.getOrgCode());
           // flowDefination.setCurrentFlowDefVersion(1L);I
            flowDefination.setFlowDefinationStatus(FlowDefinationStatus.INIT);
            flowDefination.setPriority(definition.getPriority());
            flowDefination.setSubProcess(canAsSubProcess);
            flowDefinationDao.save(flowDefination);

            entity.setFlowDefinationStatus(FlowDefinationStatus.INIT);
            entity.setActDefId(process.getId());
            entity.setName(process.getName());
            entity.setDefKey(process.getId());
            entity.setStartCheckServiceUrlId(process.getBeforeStartServiceId());
            entity.setStartCheckServiceUrlName(process.getBeforeStartServiceName());
            entity.setAfterStartServiceId(process.getAfterStartServiceId());
            entity.setAfterStartServiceName(process.getAfterStartServiceName());
            entity.setAfterStartServiceAync(process.getAfterStartServiceAync());

            entity.setEndCallServiceUrlId(process.getAfterEndServiceId());
            entity.setEndCallServiceUrlName(process.getAfterEndServiceName());
            entity.setEndBeforeCallServiceUrlId(process.getBeforeEndServiceId());
            entity.setEndBeforeCallServiceUrlName(process.getBeforeEndServiceName());
//            entity.setVersionCode(1);
            entity.setFlowDefination(flowDefination);
            entity.setDefJson(definition.getDefJson());
//            entity.setDefBpmn(defBpm);
            entity.setDefXml(defBpm);
            entity.setPriority(definition.getPriority());
            entity.setSubProcess(canAsSubProcess);

            flowDefVersionDao.save(entity);
            logger.info("Saved FlowDefVersion id is {}", entity.getId());
            flowDefination.setLastVersionId(entity.getId());
            flowDefinationDao.save(flowDefination);
            logger.info("Saved FlowDefination id is {}", flowDefination.getId());
        } else {
            if(StringUtils.isNoneEmpty(process.getFlowDefVersionId())){
                entity =  flowDefVersionDao.findOne(process.getFlowDefVersionId());
            }

            if(entity!=null){//版本不为空
                if(!entity.getDefKey().equals(process.getId())){
                    throw new RuntimeException("版本key与当前流程定义key不一致！");
                }
                if(StringUtils.isNotEmpty(entity.getActDeployId())){//对于已经有发布ID的对象进行拷贝
                    FlowDefVersion old = entity;
                    entity  = new FlowDefVersion();
                    entity.setActDefId(old.getId());
                    entity.setDefKey(old.getDefKey());
                }else{
                    entity.setActDefId(process.getId());
                    entity.setDefKey(process.getId());
                }
                if(process.getStartUEL()!=null){
                    // flowDefination.setStartUel(process.getStartUEL().toString());
                    entity.setStartUel(process.getStartUEL().toString());
                }
                entity.setFlowDefinationStatus(FlowDefinationStatus.INIT);
                entity.setFlowDefination(flowDefination);
                entity.setDefJson(definition.getDefJson());
//                entity.setDefBpmn(defBpm);
                entity.setDefXml(defBpm);
                entity.setName(process.getName());
                entity.setPriority(definition.getPriority());
                entity.setSubProcess(canAsSubProcess);
                entity.setStartCheckServiceUrlId(process.getBeforeStartServiceId());
                entity.setStartCheckServiceUrlName(process.getBeforeStartServiceName());
                entity.setAfterStartServiceId(process.getAfterStartServiceId());
                entity.setAfterStartServiceName(process.getAfterStartServiceName());
                entity.setAfterStartServiceAync(process.getAfterStartServiceAync());

                entity.setEndCallServiceUrlId(process.getAfterEndServiceId());
                entity.setEndCallServiceUrlName(process.getAfterEndServiceName());
                entity.setEndBeforeCallServiceUrlId(process.getBeforeEndServiceId());
                entity.setEndBeforeCallServiceUrlName(process.getBeforeEndServiceName());

                flowDefVersionDao.save(entity);
                flowDefination.setLastVersionId(entity.getId());
                flowDefination.setPriority(definition.getPriority());
                flowDefination.setName(process.getName());
                flowDefination.setSubProcess(canAsSubProcess);
                flowDefinationDao.save(flowDefination);
                logger.info("Saved FlowDefVersion id is {}", entity.getId());
            }else {//版本为空
                entity = new FlowDefVersion();
                entity.setFlowDefinationStatus(FlowDefinationStatus.INIT);
                entity.setActDefId(process.getId());
                entity.setName(process.getName());
                entity.setDefKey(process.getId());
//                entity.setVersionCode(1);
                entity.setFlowDefination(flowDefination);
                entity.setDefJson(definition.getDefJson());
//                entity.setDefBpmn(defBpm);
                entity.setDefXml(defBpm);
                if(process.getStartUEL()!=null){
                   // flowDefination.setStartUel(process.getStartUEL().toString());
                    entity.setStartUel(process.getStartUEL().toString());
                }
                entity.setPriority(definition.getPriority());
                entity.setSubProcess(canAsSubProcess);
                entity.setStartCheckServiceUrlId(process.getBeforeStartServiceId());
                entity.setStartCheckServiceUrlName(process.getBeforeStartServiceName());
                entity.setAfterStartServiceId(process.getAfterStartServiceId());
                entity.setAfterStartServiceName(process.getAfterStartServiceName());
                entity.setAfterStartServiceAync(process.getAfterStartServiceAync());

                entity.setEndCallServiceUrlId(process.getAfterEndServiceId());
                entity.setEndCallServiceUrlName(process.getAfterEndServiceName());
                entity.setEndBeforeCallServiceUrlId(process.getBeforeEndServiceId());
                entity.setEndBeforeCallServiceUrlName(process.getBeforeEndServiceName());
                flowDefVersionDao.save(entity);
                logger.info("Saved FlowDefVersion id is {}", entity.getId());
                flowDefination.setName(process.getName());
                flowDefination.setLastVersionId(entity.getId());
                flowDefination.setPriority(definition.getPriority());
                flowDefination.setSubProcess(canAsSubProcess);
                flowDefinationDao.save(flowDefination);
                logger.info("Saved FlowDefination id is {}", flowDefination.getId());
            }

        }
        OperateResultWithData<FlowDefVersion> operateResult;
        if (isNew) {
            operateResult = OperateResultWithData.operationSuccess("core_00001");
        } else {
            operateResult = OperateResultWithData.operationSuccess("core_00002");
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
        OperateResult result =  OperateResult.operationSuccess("core_00003");
        FlowDefVersion  entity = flowDefVersionDao.findOne(id);
        List<FlowInstance> flowInstanceList = flowInstanceDao.findByFlowDefVersionId(entity.getId());
        if(flowInstanceList!=null && !flowInstanceList.isEmpty()){
            result = OperateResult.operationFailure("10024");
            return result;
        }
        FlowDefination flowDefination = entity.getFlowDefination();
        String actDeployId = entity.getActDeployId();
        if(StringUtils.isNotEmpty(actDeployId)){
            this.deleteActivtiProcessDefinition(actDeployId,false);
        }
        flowDefVersionDao.delete(entity);
        List<FlowDefVersion>  flowDefVersionList = flowDefVersionDao.findByFlowDefinationId(flowDefination.getId());
        if(flowDefVersionList==null || flowDefVersionList.isEmpty()){//找不到对应的版本，删除流程定义
            flowDefinationDao.delete(flowDefination);
        }

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
