package com.ecmp.flow.service;

import com.ecmp.core.service.BaseService;
import com.ecmp.flow.api.IFlowInstanceService;
import com.ecmp.flow.dao.FlowInstanceDao;
import com.ecmp.flow.dao.FlowTaskDao;
import com.ecmp.flow.entity.FlowInstance;
import com.ecmp.vo.OperateResult;
import org.activiti.engine.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

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
public class FlowInstanceService extends BaseService<FlowInstance, String> implements IFlowInstanceService {

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
     * 撤销流程实例
     * 清除有关联的流程版本及对应的流程引擎数据
     * @param id 待操作数据ID
     */
    public OperateResult delete(String id) {
        FlowInstance entity = flowInstanceDao.findOne(id);
        String actInstanceId = entity.getActInstanceId();
        this.deleteActiviti(actInstanceId);
        flowInstanceDao.delete(entity);
        OperateResult result =  OperateResult.OperationSuccess("core_00003");
        return result;
    }
    /**
     * 通过ID批量删除
     * @param ids
     */
    @Override
    public void delete(Collection<String> ids) {
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
//        FlowInstance entity = flowInstanceDao.findOne(id);
//        return this.delete(entity);
//    }

    /**
     * 将流程实例挂起
     * @param id
     */
    @Override
    public   OperateResult  suspend(String id){
        FlowInstance entity = flowInstanceDao.findOne(id);
        String actInstanceId = entity.getActInstanceId();
        this.suspendActiviti(actInstanceId);
        OperateResult result =  OperateResult.OperationSuccess("00001");
        return result;
    }

    /**
     * 将流程实例挂起
     * @param processInstanceId
     */
    private  void  suspendActiviti(String processInstanceId){
        runtimeService.suspendProcessInstanceById(processInstanceId);// 挂起该流程实例
    }

    /**
     * 删除流程引擎实例相关数据
     * @param processInstanceId
     */
    private  void  deleteActiviti(String processInstanceId){
        runtimeService.deleteProcessInstance(processInstanceId,null);
    }
}
