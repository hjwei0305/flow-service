package com.ecmp.flow.service;

import com.ecmp.config.util.ApiClient;
import com.ecmp.context.ContextUtil;
import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.core.service.BaseService;
import com.ecmp.flow.api.IFlowInstanceService;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.dao.FlowHistoryDao;
import com.ecmp.flow.dao.FlowInstanceDao;
import com.ecmp.flow.dao.FlowTaskDao;
import com.ecmp.flow.entity.*;
import com.ecmp.flow.util.ExpressionUtil;
import com.ecmp.flow.vo.MyBillVO;
import com.ecmp.flow.vo.ProcessTrackVO;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import net.sf.json.JSONObject;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    private final Logger logger = LoggerFactory.getLogger(FlowInstanceService.class);

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
        OperateResult result =  OperateResult.operationSuccess("core_00003");
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
        OperateResult result =  OperateResult.operationSuccess("00001");
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

    /**
     * 获取流程实例任务历史id列表，以完成时间升序排序
     * @param id
     */
    public List<String>  nodeHistoryIds(String id){
        List<String> nodeIds = new ArrayList<String>();
        List<FlowHistory> flowHistoryList = flowHistoryDao.findByInstanceId(id);
        if(flowHistoryList!=null && !flowHistoryList.isEmpty()){
            for(FlowHistory flowHistory:flowHistoryList){
                nodeIds.add(flowHistory.getActTaskDefKey());
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
        if(list!=null && !list.isEmpty()){
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
                result.add(pv);
            }
        }
        return result;
    }


    /**
     * 检查当前实例是否允许执行终止流程实例操作
     * @param id 待操作数据ID
     */
    public Boolean checkCanEnd(String id) {
        Boolean canEnd = false;
        List<FlowTask> flowTaskList = flowTaskDao.findByInstanceId(id);
        if(flowTaskList!=null && !flowTaskList.isEmpty()){
            int taskCount = flowTaskList.size();
            int index=0;
            for(FlowTask flowTask:flowTaskList){
                Boolean canCancel = flowTask.getCanSuspension();
                if(canCancel!=null && canCancel==true){
                    index++;
                }
            }
            if(index == taskCount){
                canEnd = true;
            }
        }
        //针对并行、包容网关，只要有一条分支不允许终止，则全部符合条件的分支不允许终止
        if(!canEnd){
            if(flowTaskList!=null && !flowTaskList.isEmpty()){
                int taskCount = flowTaskList.size();
                int index=0;
                for(FlowTask flowTask:flowTaskList){
                     flowTask.setCanSuspension(false);
                     flowTaskDao.save(flowTask);
                 }
            }
        }
        return canEnd;
    }

    /**
     * 检查实例集合是否允许执行终止流程实例操作
     * @param ids 待操作数据ID集合
     */
    public List<Boolean> checkIdsCanEnd(List<String> ids){
        List<Boolean> result = null;
        if(ids!=null && !ids.isEmpty()){
            result = new ArrayList<Boolean>(ids.size());
            for(String id:ids){
              Boolean canEnd = this.checkCanEnd(id);
                result.add(canEnd);
            }
        }
        return result;
    }

    /**
     * 撤销流程实例
     * 清除有关联的流程版本及对应的流程引擎数据
     * @param id 待操作数据ID
     */
    @Transactional( propagation= Propagation.REQUIRED)
    public OperateResult end(String id) {
        OperateResult result =  OperateResult.operationSuccess("10010");
        boolean canEnd = false;
        List<FlowTask> flowTaskList = flowTaskDao.findByInstanceId(id);
        if(flowTaskList!=null && !flowTaskList.isEmpty()){
            int taskCount = flowTaskList.size();
            int index=0;
            for(FlowTask flowTask:flowTaskList){
                Boolean canCancel = flowTask.getCanSuspension();
                if(canCancel!=null && canCancel){
                    index++;
                }
            }
            if(index == taskCount){
                canEnd = true;
            }
        }
        if(canEnd){
            FlowInstance flowInstance = flowTaskList.get(0).getFlowInstance();
            for(FlowTask flowTask:flowTaskList){
                try {
                    FlowHistory flowHistory = new FlowHistory();
                    String preFlowHistoryId = flowTask.getPreId();
                    FlowHistory preFlowHistory = null;
                    if(StringUtils.isNotEmpty(preFlowHistoryId)){
                        preFlowHistory = flowHistoryDao.findOne(preFlowHistoryId);
                    }
                    BeanUtils.copyProperties(flowHistory, flowTask);
                    flowHistory.setId(null);
                    flowHistory.setFlowDefId(flowTask.getFlowDefinitionId());
                    flowHistory.setDepict("【被发起人终止流程】");
                    flowHistory.setFlowTaskName(flowTask.getTaskName());
                    Date now = new Date();
                    if(preFlowHistory!=null){
                        flowHistory.setActDurationInMillis(now.getTime()-preFlowHistory.getActEndTime().getTime());
                    }else{
                        flowHistory.setActDurationInMillis(now.getTime()-flowTask.getCreatedDate().getTime());
                    }
                    flowHistory.setActEndTime(now);

                    flowHistoryDao.save(flowHistory);
                }catch(Exception e){
                    logger.error(e.getMessage());
                }
                flowTaskDao.delete(flowTask);
            }
            String actInstanceId = flowInstance.getActInstanceId();
            this.deleteActiviti(actInstanceId);
            flowInstance.setEndDate(new Date());
            flowInstance.setEnded(true);
            flowInstance.setManuallyEnd(true);
            flowInstanceDao.save(flowInstance);


            //重置客户端表单流程状态
            BusinessModel businessModel = flowInstance.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel();
            String businessModelId = businessModel.getId();
            String appModuleId = businessModel.getAppModuleId();
            com.ecmp.basic.api.IAppModuleService proxy = ApiClient.createProxy(com.ecmp.basic.api.IAppModuleService.class);
            com.ecmp.basic.entity.AppModule appModule = proxy.findOne(appModuleId);
            String clientApiBaseUrl = ContextUtil.getAppModule(appModule.getCode()).getApiBaseAddress();
            String businessId = flowInstance.getBusinessId();
            FlowStatus status = FlowStatus.INIT;
            ExpressionUtil.resetState(clientApiBaseUrl, businessModelId,  businessId,  status);
        }else {
            result =  OperateResult.operationFailure("10011");//不能终止
        }
        return result;
    }

    @Transactional( propagation= Propagation.REQUIRED)
    public OperateResult endByBusinessId(String businessId){
        FlowInstance  flowInstance = this.findLastInstanceByBusinessId(businessId);
        return  this.end(flowInstance.getId());
    }

    @Transactional( propagation= Propagation.REQUIRED)
    public OperateResult signalByBusinessId(String businessId,String receiveTaskActDefId,Map<String,Object> v){
        if(StringUtils.isEmpty(receiveTaskActDefId)){
            return OperateResult.operationFailure("10032");
        }
        OperateResult result =  OperateResult.operationSuccess("10029");
        FlowInstance  flowInstance = this.findLastInstanceByBusinessId(businessId);
        if(flowInstance != null && !flowInstance.isEnded()){
            String actInstanceId = flowInstance.getActInstanceId();
            HistoricActivityInstance receiveTaskActivityInstance = historyService.createHistoricActivityInstanceQuery().processInstanceId(actInstanceId).activityId(receiveTaskActDefId).unfinished().singleResult();
            if(receiveTaskActivityInstance != null ){
                    String executionId = receiveTaskActivityInstance.getExecutionId();
                    runtimeService.signal(executionId,v);
            }else{
                    result =  OperateResult.operationFailure("10031");
            }
        }else{
            result =  OperateResult.operationFailure("10030");
        }
           return result;
    }


}
