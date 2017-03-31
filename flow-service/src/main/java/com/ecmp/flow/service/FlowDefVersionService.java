package com.ecmp.flow.service;

import com.ecmp.core.service.BaseService;
import com.ecmp.flow.api.IFlowDefVersionService;
import com.ecmp.flow.dao.FlowDefVersionDao;
import com.ecmp.flow.dao.FlowDefinationDao;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.entity.FlowDefination;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.vo.OperateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
public class FlowDefVersionService extends BaseService<FlowDefVersion,String> implements IFlowDefVersionService {

    private final Logger logger = LoggerFactory.getLogger(FlowDefVersion.class);

    @Autowired
    private FlowDefVersionDao flowDefVersionDao;

    @Autowired
    private FlowDefinationDao flowDefinationDao;

    /**
     * 新增修改操作
     *
     * @param entity
     * @return
     */
    public OperateResult<FlowDefVersion> save(FlowDefVersion entity) {
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
        OperateResult<FlowDefVersion> operateResult;
        if (isNew) {
            operateResult = OperateResult.OperationSuccess("core_00001");
        } else {
            operateResult = OperateResult.OperationSuccess("core_00002");
        }
        operateResult.setData(entity);
        return operateResult;
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
