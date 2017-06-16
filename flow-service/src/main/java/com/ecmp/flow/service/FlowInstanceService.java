package com.ecmp.flow.service;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.core.service.BaseService;
import com.ecmp.flow.api.IFlowInstanceService;
import com.ecmp.flow.dao.FlowHistoryDao;
import com.ecmp.flow.dao.FlowInstanceDao;
import com.ecmp.flow.dao.FlowTaskDao;
import com.ecmp.flow.entity.FlowHiVarinst;
import com.ecmp.flow.entity.FlowHistory;
import com.ecmp.flow.entity.FlowInstance;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.flow.vo.ProcessTrackVO;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import org.activiti.engine.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
public class FlowInstanceService extends BaseEntityService<FlowInstance> implements IFlowInstanceService {

    @Autowired
    private FlowInstanceDao flowInstanceDao;

    protected BaseEntityDao<FlowInstance> getDao(){
        return this.flowInstanceDao;
    }

    @Autowired
    private FlowTaskDao flowTaskDao;

    @Autowired
    private FlowHistoryDao flowHistoryDao;

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
     * 获取流程实例在线任务id列表
     * @param id
     */
    public Set<String>  currentNodeIds(String id){
        FlowInstance flowInstance = flowInstanceDao.findOne(id);
        Set<String> nodeIds = new HashSet<String>();
        List<FlowTask> flowTaskList = flowTaskDao.findByInstanceId(id);
        if(flowTaskList!=null && !flowTaskList.isEmpty()){
            for(FlowTask flowTask:flowTaskList){
                nodeIds.add(flowTask.getActTaskDefKey());
            }
        }
        return nodeIds;
    }

  public List<FlowHistory>  findAllByBusinessId(String businessId){
          return flowHistoryDao.findAllByBusinessId(businessId);
  }

    public List<FlowHistory>  findLastByBusinessId(String businessId){
        return flowHistoryDao.findLastByBusinessId(businessId);
    }

    public FlowInstance  findLastInstanceByBusinessId(String businessId){
        List<FlowInstance> list =flowInstanceDao.findByBusinessIdOrder(businessId);
        if(list!=null){
            return list.get(0);
        }else {
            return null;
        }
    }

    public List<FlowTask>  findCurrentTaskByBusinessId(String businessId){
        FlowInstance flowInstance = this.findLastInstanceByBusinessId(businessId);
        if(flowInstance == null || flowInstance.isEnded()){
            return null;
        }else {
            return flowTaskDao.findByInstanceId(flowInstance.getId());
        }
    }

    /**
     * 通过业务单据id获取最新流程实例在线任务id列表
     * @param businessId
     */
    public Set<String>  getLastNodeIdsByBusinessId(String businessId){
        FlowInstance  flowInstance = this.findLastInstanceByBusinessId(businessId);
        Set<String> nodeIds = new HashSet<String>();
        List<FlowTask> flowTaskList = null;
        if(flowInstance != null && !flowInstance.isEnded()){
            flowTaskList = flowTaskDao.findByInstanceId(flowInstance.getId());
        }
        if(flowTaskList!=null && !flowTaskList.isEmpty()){
            for(FlowTask flowTask:flowTaskList){
                nodeIds.add(flowTask.getActTaskDefKey());
            }
        }
        return nodeIds;
    }

//    /**
//     * 获取流程实例在线任务id列表
//     * @param businessModelId  业务实体类型id
//     */
//    public Set<String>  currentNodeIdsByBusinessModelId(String businessModelId,String businessId){
//                      flowInstanceDao.
//    }

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

    /**
     * 通过单据id，获取流程实例及关联待办及任务历史
     * @param businessId
     * @return
     */
    public List<ProcessTrackVO> getProcessTrackVO(String businessId){
        List<FlowInstance> flowInstanceList = flowInstanceDao.findByBusinessIdOrder(businessId);
        List<ProcessTrackVO> result = null;
        if(flowInstanceList!=null && !flowInstanceList.isEmpty()){
            result = new ArrayList<ProcessTrackVO>();
            for(FlowInstance flowInstance:flowInstanceList){
                ProcessTrackVO pv = new ProcessTrackVO();
                pv.setFlowInstance(flowInstance);
                List<FlowTask> flowTaskList = flowTaskDao.findByInstanceId(flowInstance.getId());
                List<FlowHistory> flowHistoryList = flowHistoryDao.findByInstanceId(flowInstance.getId());
                pv.setFlowHistoryList(flowHistoryList);
                pv.setFlowTaskList(flowTaskList);
            }
        }
        return result;
    }
}
