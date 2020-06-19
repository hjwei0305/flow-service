package com.ecmp.flow.service;

import com.ecmp.context.ContextUtil;
import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.search.*;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.flow.api.IFlowInstanceService;
import com.ecmp.flow.basic.vo.Executor;
import com.ecmp.flow.common.util.Constants;
import com.ecmp.flow.constant.FlowExecuteStatus;
import com.ecmp.flow.constant.FlowStatus;
import com.ecmp.flow.dao.*;
import com.ecmp.flow.dto.UserFlowBillsQueryParam;
import com.ecmp.flow.entity.*;
import com.ecmp.flow.util.*;
import com.ecmp.flow.vo.*;
import com.ecmp.flow.vo.phone.MyBillPhoneVO;
import com.ecmp.log.util.LogUtil;
import com.ecmp.util.DateUtils;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import com.ecmp.vo.ResponseData;
import com.ecmp.vo.SessionUser;
import net.sf.json.JSONObject;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：实例管理
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

    protected BaseEntityDao<FlowInstance> getDao() {
        return this.flowInstanceDao;
    }

    @Autowired
    private FlowTaskDao flowTaskDao;

    @Autowired
    private FlowHistoryDao flowHistoryDao;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private FlowListenerTool flowListenerTool;

    @Autowired
    private TaskService taskService;

    @Autowired
    private FlowTaskService flowTaskService;

    @Autowired
    private FlowSolidifyExecutorDao flowSolidifyExecutorDao;

    @Autowired
    private FlowCommonUtil flowCommonUtil;

    @Autowired
    private FlowDefinationDao flowDefinationDao;

    @Autowired
    private BusinessModelDao businessModelDao;

    @Autowired
    private TaskMakeOverPowerService taskMakeOverPowerService;

    @Autowired
    private FlowHistoryService flowHistoryService;

    /**
     * 撤销流程实例
     * 清除有关联的流程版本及对应的流程引擎数据
     *
     * @param id 待操作数据ID
     */
    public OperateResult delete(String id) {
        FlowInstance entity = flowInstanceDao.findOne(id);
        String actInstanceId = entity.getActInstanceId();
        this.deleteActiviti(actInstanceId, null);
        flowInstanceDao.delete(entity);
        // 流程实例删除成功！
        return OperateResult.operationSuccess("10061");
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

    /**
     * 将流程实例挂起
     *
     * @param id
     */
    @Override
    public OperateResult suspend(String id) {
        FlowInstance entity = flowInstanceDao.findOne(id);
        String actInstanceId = entity.getActInstanceId();
        this.suspendActiviti(actInstanceId);
        OperateResult result = OperateResult.operationSuccess("00001");
        return result;
    }

    /**
     * 获取流程实例在线任务id列表
     *
     * @param id
     */
    public Map<String, String> currentNodeIds(String id) {
        FlowInstance flowInstance = flowInstanceDao.findOne(id);
        Map<String, String> nodeIds = new HashMap<String, String>();
        List<FlowTask> flowTaskList = flowTaskDao.findByInstanceId(id);
        if (flowTaskList != null && !flowTaskList.isEmpty()) {
            for (FlowTask flowTask : flowTaskList) {
                nodeIds.put(flowTask.getActTaskDefKey(), "");
            }
        }
        if (flowInstance == null) {
            return null;
        }
        List<FlowInstance> children = flowInstanceDao.findByParentId(flowInstance.getId());
        if (children != null && !children.isEmpty()) {
            for (FlowInstance child : children) {
                Map<String, String> resultTemp = currentNodeIds(child.getId());
                if (resultTemp != null && !resultTemp.isEmpty()) {
                    // 取得流程实例
                    ProcessInstance instanceSon = runtimeService
                            .createProcessInstanceQuery()
                            .processInstanceId(child.getActInstanceId())
                            .singleResult();
                    String superExecutionId = instanceSon.getSuperExecutionId();
                    HistoricActivityInstance historicActivityInstance = null;
                    HistoricActivityInstanceQuery his = historyService.createHistoricActivityInstanceQuery()
                            .executionId(superExecutionId).activityType("callActivity").unfinished();
                    historicActivityInstance = his.singleResult();
                    HistoricActivityInstanceEntity he = (HistoricActivityInstanceEntity) historicActivityInstance;
                    String activityId = he.getActivityId();
                    nodeIds.put(activityId, child.getId());
                }
            }
        }
        return nodeIds;
    }

    /**
     * 获取流程实例任务历史id列表，以完成时间升序排序
     *
     * @param id
     */
    public List<String> nodeHistoryIds(String id) {
        List<String> nodeIds = new ArrayList<String>();
        List<FlowHistory> flowHistoryList = flowHistoryDao.findByInstanceId(id);
        if (flowHistoryList != null && !flowHistoryList.isEmpty()) {
            for (FlowHistory flowHistory : flowHistoryList) {
                nodeIds.add(flowHistory.getActTaskDefKey());
            }
        }
        return nodeIds;
    }

    public List<FlowHistory> findAllByBusinessId(String businessId) {
        return flowHistoryDao.findAllByBusinessId(businessId);
    }

    public List<FlowHistory> findLastByBusinessId(String businessId) {
        List<FlowHistory> flowHistoryList = flowHistoryDao.findLastByBusinessId(businessId);
        flowHistoryService.initFlowTaskAppModule(flowHistoryList);
        return  flowHistoryList;
    }

    //因为中泰时间服务器问题，所以不能按照时间倒序查询
    public FlowInstance findLastInstanceByBusinessId(String businessId) {
        //流程中如果存在未终止的实例，肯定是最后一个
        FlowInstance bean = flowInstanceDao.findByBusinessIdNoEnd(businessId);
        if (bean != null) {
            return bean;
        }
        List<FlowInstance> list = flowInstanceDao.findByBusinessIdOrder(businessId);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        } else {
            return null;
        }
    }

    public List<FlowTask> findCurrentTaskByBusinessId(String businessId) {
        FlowInstance flowInstance = this.findLastInstanceByBusinessId(businessId);
        if (flowInstance == null || flowInstance.isEnded()) {
            return null;
        } else {
            return flowTaskDao.findByInstanceId(flowInstance.getId());
        }
    }

    public FlowTask findTaskByBusinessIdAndActTaskKey(String businessId, String taskActDefId) {
        if (StringUtils.isEmpty(taskActDefId)) {
            return null;
        }
        FlowInstance flowInstance = this.findLastInstanceByBusinessId(businessId);
        if (flowInstance != null && !flowInstance.isEnded()) {
            String actInstanceId = flowInstance.getActInstanceId();
            HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().processInstanceId(actInstanceId).taskDefinitionKey(taskActDefId).unfinished().singleResult(); // 创建历史任务实例查询
            if (historicTaskInstance != null) {
                return flowTaskDao.findByActTaskId(historicTaskInstance.getId());
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public FlowTaskVO findTaskVOByBusinessIdAndActTaskKey(String businessId, String taskActDefId) {
        FlowTask flowTask = this.findTaskByBusinessIdAndActTaskKey(businessId, taskActDefId);
        FlowTaskVO flowTaskVO = null;
        if (flowTask != null) {
            flowTaskVO = new FlowTaskVO();
            flowTaskVO.setId(flowTask.getId());
            flowTaskVO.setName(flowTask.getTaskName());
            if (Objects.nonNull(flowTask.getWorkPageUrl())) {
                flowTaskVO.setWorkPageUrl(flowTask.getWorkPageUrl().getUrl());
            }
            if(Objects.nonNull(flowTask.getFlowInstance())){
                flowTaskVO.setInstanceId(flowTask.getFlowInstance().getId());
            }
        }
        return flowTaskVO;
    }

    /**
     * 通过业务单据id获取最新流程实例在线任务id列表
     *
     * @param businessId
     */
    public Set<String> getLastNodeIdsByBusinessId(String businessId) {
        FlowInstance flowInstance = this.findLastInstanceByBusinessId(businessId);
        Set<String> nodeIds = new HashSet<String>();
        List<FlowTask> flowTaskList = null;
        if (flowInstance != null && !flowInstance.isEnded()) {
            flowTaskList = flowTaskDao.findByInstanceId(flowInstance.getId());
        }
        if (flowTaskList != null && !flowTaskList.isEmpty()) {
            for (FlowTask flowTask : flowTaskList) {
                nodeIds.add(flowTask.getActTaskDefKey());
            }
        }
        return nodeIds;
    }

    /**
     * 将流程实例挂起
     *
     * @param processInstanceId
     */
    private void suspendActiviti(String processInstanceId) {
        runtimeService.suspendProcessInstanceById(processInstanceId);// 挂起该流程实例
    }

    /**
     * 删除流程引擎实例相关数据
     *
     * @param processInstanceId
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteActiviti(String processInstanceId, String deleteReason) {
        runtimeService.deleteProcessInstance(processInstanceId, deleteReason);
    }

    /**
     * 针对跨业务实体子流程情况，业务单据id不一样
     *
     * @param flowInstanceListReal 需要的真实实例对象列表
     * @param businessId           父业务单据id
     * @param flowInstance         当前实例
     * @return 结果集
     */
    private Set<FlowInstance> initSonFlowInstance(Set<FlowInstance> flowInstanceListReal, String businessId, FlowInstance flowInstance) {
        List<FlowInstance> children = flowInstanceDao.findByParentId(flowInstance.getId());
        if (children != null && !children.isEmpty()) {
            for (FlowInstance son : children) {
//                if(!businessId.equals(son.getBusinessId())){//跨业务实体子流程
                flowInstanceListReal.add(son);
//                }
                initSonFlowInstance(flowInstanceListReal, businessId, son);
            }
        }
        return flowInstanceListReal;
    }

    private void flowTaskSort(List<FlowTask> flowTaskList) {
        //去重复
        if (flowTaskList != null && !flowTaskList.isEmpty()) {
            Set<FlowTask> tempflowTaskSet = new LinkedHashSet<>();
            tempflowTaskSet.addAll(flowTaskList);
            flowTaskList.clear();
            flowTaskList.addAll(tempflowTaskSet);
            Collections.sort(flowTaskList, new Comparator<FlowTask>() {
                @Override
                public int compare(FlowTask flowTask1, FlowTask flowTask2) {
                    return timeCompare(flowTask1.getCreatedDate(), flowTask2.getCreatedDate());
                }
            });
        }
    }

    /**
     * 通过单据id，获取流程实例及关联待办及任务历史
     *
     * @param businessId
     * @return
     */
    public List<ProcessTrackVO> getProcessTrackVO(String businessId) {
        List<FlowInstance> flowInstanceList = flowInstanceDao.findByBusinessIdOrder(businessId);
        List<ProcessTrackVO> result = new ArrayList<ProcessTrackVO>();
        Set<FlowInstance> flowInstanceListReal = new LinkedHashSet<>();

        if (flowInstanceList != null && !flowInstanceList.isEmpty()) {
            flowInstanceListReal.addAll(flowInstanceList);
            for (FlowInstance flowInstance : flowInstanceList) {
                FlowInstance parent = flowInstance.getParent();
                while (parent != null) {
                    // flowInstanceListReal.add(parent);
                    initSonFlowInstance(flowInstanceListReal, businessId, parent);//初始化兄弟节点相关任务
                    flowInstanceListReal.remove(parent);
                    parent = parent.getParent();
                }
                initSonFlowInstance(flowInstanceListReal, businessId, flowInstance);
            }
        }
        Map<FlowInstance, ProcessTrackVO> resultMap = new LinkedHashMap<FlowInstance, ProcessTrackVO>();

        if (flowInstanceListReal != null && !flowInstanceListReal.isEmpty()) {
            for (FlowInstance flowInstance : flowInstanceListReal) {
                initFlowInstance(resultMap, flowInstance);
            }
        }
        result.addAll(resultMap.values());
        //排序，主要针对有子任务的场景
        if (!result.isEmpty()) {
            for (ProcessTrackVO processTrackVO : result) {
                List<FlowHistory> flowHistoryList = processTrackVO.getFlowHistoryList();
                List<FlowTask> flowTaskList = processTrackVO.getFlowTaskList();
                flowTaskSort(flowTaskList);

                if (flowHistoryList != null && !flowHistoryList.isEmpty()) {
                    //去重复
                    Set<FlowHistory> tempFlowHistorySet = new LinkedHashSet<>();
                    tempFlowHistorySet.addAll(flowHistoryList);
                    flowHistoryList.clear();
                    flowHistoryList.addAll(tempFlowHistorySet);
                    Collections.sort(flowHistoryList, new Comparator<FlowHistory>() {
                        @Override
                        public int compare(FlowHistory flowHistory1, FlowHistory flowHistory2) {
                            Date time1 = flowHistory1.getActEndTime();
                            Date time2 = flowHistory2.getActEndTime();
                            return timeCompare(time2, time1);
                        }
                    });
                }
            }
        }

        return result;
    }

    private int timeCompare(Date date1, Date date2) {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date1);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);
        return calendar2.compareTo(calendar1);
    }


    public List<ProcessTrackVO> getProcessTrackVOByTaskId(String taskId) {
        List<ProcessTrackVO> list = new ArrayList<ProcessTrackVO>();
        FlowTask flowTask = flowTaskService.findOne(taskId);
        if (flowTask != null && flowTask.getFlowInstance() != null) {
            list = this.getProcessTrackVOById(flowTask.getFlowInstance().getId());
        }
        return list;
    }


    public List<ProcessTrackVO> getProcessTrackVOById(String instanceId) {
        FlowInstance flowInstance = flowInstanceDao.findOne(instanceId);
        List<ProcessTrackVO> result = new ArrayList<ProcessTrackVO>();
        Set<FlowInstance> flowInstanceListReal = new LinkedHashSet<>();

        if (flowInstance != null) {
            flowInstanceListReal.add(flowInstance);
            String businessId = flowInstance.getBusinessId();
            FlowInstance parent = flowInstance.getParent();
            while (parent != null) {
                initSonFlowInstance(flowInstanceListReal, businessId, parent);//初始化兄弟节点相关任务
                flowInstanceListReal.remove(parent);
                parent = parent.getParent();
            }
            initSonFlowInstance(flowInstanceListReal, businessId, flowInstance);
        }
        Map<FlowInstance, ProcessTrackVO> resultMap = new LinkedHashMap<FlowInstance, ProcessTrackVO>();

        if (flowInstanceListReal != null && !flowInstanceListReal.isEmpty()) {
            for (FlowInstance flowInstanceTemp : flowInstanceListReal) {
                initFlowInstance(resultMap, flowInstanceTemp);
            }
        }
        result.addAll(resultMap.values());
        //排序，主要针对有子任务的场景
        if (!result.isEmpty()) {
            for (ProcessTrackVO processTrackVO : result) {
                List<FlowHistory> flowHistoryList = processTrackVO.getFlowHistoryList();
                List<FlowTask> flowTaskList = processTrackVO.getFlowTaskList();
                flowTaskSort(flowTaskList);


                if (flowHistoryList != null && !flowHistoryList.isEmpty()) {
                    //去重复
                    Set<FlowHistory> tempFlowHistorySet = new LinkedHashSet<>();
                    tempFlowHistorySet.addAll(flowHistoryList);
                    flowHistoryList.clear();
                    flowHistoryList.addAll(tempFlowHistorySet);
                    Collections.sort(flowHistoryList, new Comparator<FlowHistory>() {
                        @Override
                        public int compare(FlowHistory flowHistory1, FlowHistory flowHistory2) {
                            Date time1 = flowHistory1.getActEndTime();
                            Date time2 = flowHistory2.getActEndTime();
                            int result = timeCompare(time2, time1);
                            if (result == 0) {
                                time1 = flowHistory1.getActStartTime();
                                time2 = flowHistory2.getActStartTime();
                                result = timeCompare(time2, time1);
                            }
                            return result;
                        }
                    });
                }
            }
        }

        return result;
    }

    /**
     * 用于父子流程的实例合并
     *
     * @param resultMap
     * @param flowInstance
     */
    private void initFlowInstance(Map<FlowInstance, ProcessTrackVO> resultMap, FlowInstance flowInstance) {

        ProcessTrackVO pv = new ProcessTrackVO();
        pv.setFlowInstance(flowInstance);
        List<FlowTask> flowTaskList = flowTaskDao.findByInstanceId(flowInstance.getId());
        List<FlowTask> newFlowTaskList = new ArrayList<FlowTask>();
        for (FlowTask bean : flowTaskList) {
            if (bean.getTrustState() == null) {
                newFlowTaskList.add(bean);
            } else {
                if (bean.getTrustState() != 1) {
                    newFlowTaskList.add(bean);
                }
            }
        }
        List<FlowHistory> flowHistoryList = flowHistoryDao.findByInstanceId(flowInstance.getId());
        pv.setFlowHistoryList(flowHistoryList);
        pv.setFlowTaskList(newFlowTaskList);

        FlowInstance parent = flowInstance.getParent();
        ProcessTrackVO pProcessTrackVO = null;
        if (parent != null) {
            initFlowInstance(resultMap, parent);
            while (parent != null) {
                pProcessTrackVO = resultMap.get(parent);
                if (pProcessTrackVO != null) {
                    break;
                }
                parent = parent.getParent();
            }
            if (pProcessTrackVO != null) {
                pProcessTrackVO.getFlowHistoryList().addAll(pv.getFlowHistoryList());
                pProcessTrackVO.getFlowTaskList().addAll(pv.getFlowTaskList());
            }
        } else {
            if (resultMap.get(flowInstance) == null) {
                resultMap.put(flowInstance, pv);
            }
        }
    }


    /**
     * 检查当前实例是否允许执行终止流程实例操作
     *
     * @param id 待操作数据ID
     */
    public Boolean checkCanEnd(String id) {
        Boolean canEnd = false;
        List<FlowTask> flowTaskList = flowTaskDao.findByInstanceId(id);
        if (flowTaskList != null && !flowTaskList.isEmpty()) {
            int taskCount = flowTaskList.size();
            int index = 0;
            for (FlowTask flowTask : flowTaskList) {
                Boolean canCancel = flowTask.getCanSuspension();
                if (canCancel != null && canCancel == true) {
                    index++;
                }
            }
            if (index == taskCount) {
                canEnd = true;
            }
        }
        //针对并行、包容网关，只要有一条分支不允许终止，则全部符合条件的分支不允许终止
        if (!canEnd) {
            if (flowTaskList != null && !flowTaskList.isEmpty()) {
                for (FlowTask flowTask : flowTaskList) {
                    if (flowTask.getCanSuspension() == null || flowTask.getCanSuspension() == true) {
                        flowTask.setCanSuspension(false);
                        flowTaskDao.save(flowTask);
                    }
                }
            }
        }
        return canEnd;
    }

    /**
     * 检查实例集合是否允许执行终止流程实例操作
     *
     * @param ids 待操作数据ID集合
     */
    public List<Boolean> checkIdsCanEnd(List<String> ids) {
        List<Boolean> result = null;
        if (ids != null && !ids.isEmpty()) {
            result = new ArrayList<Boolean>(ids.size());
            for (String id : ids) {
                Boolean canEnd = this.checkCanEnd(id);
                result.add(canEnd);
            }
        }
        return result;
    }

    private boolean initTask(FlowInstance flowInstance, Boolean force) {
        boolean canEnd = false;
        List<FlowTask> flowTaskList = flowTaskDao.findByInstanceId(flowInstance.getId());
        if (force) {
            if (flowTaskList != null && !flowTaskList.isEmpty()) {
                flowInstance.getFlowTasks().addAll(flowTaskList);
            }
            canEnd = true;
        } else {
            if (flowTaskList != null && !flowTaskList.isEmpty()) {
                int taskCount = flowTaskList.size();
                int index = 0;
                for (FlowTask flowTask : flowTaskList) {
                    Boolean canCancel = flowTask.getCanSuspension();
                    if (canCancel != null && canCancel) {
                        index++;
                    }
                }
                if (index == taskCount) {
                    canEnd = true;
                    if (flowInstance.getFlowTasks().isEmpty()) {
                        flowInstance.getFlowTasks().addAll(flowTaskList);
                    }
                }
            } else {
                canEnd = true;
            }
        }

        return canEnd;
    }

    private Map<String, FlowInstance> initAllGulianInstance(Map<String, FlowInstance> flowInstanceMap, FlowInstance flowInstance, boolean force) {
        Map<String, FlowInstance> flowInstanceChildren = null;
        while (flowInstance != null) {
            if (!flowInstance.isEnded() && flowInstanceMap.get(flowInstance) == null) {
                flowInstanceChildren = initGulianSonInstance(flowInstanceMap, flowInstance, force);//子实例
                if (flowInstanceChildren == null) {
                    return null;
                }
            }
            flowInstance = flowInstance.getParent();
        }
        if (flowInstanceChildren != null && !flowInstanceChildren.isEmpty()) {
            return flowInstanceMap;
        } else {
            return null;
        }
    }

    private Map<String, FlowInstance> initGulianSonInstance(Map<String, FlowInstance> flowInstanceMapReal, FlowInstance flowInstance, boolean force) {
        List<FlowInstance> children = flowInstanceDao.findByParentId(flowInstance.getId());
        boolean canEnd = false;
        canEnd = initTask(flowInstance, force);
        if (canEnd) {
            if (flowInstanceMapReal.get(flowInstance.getId()) == null) {
                flowInstanceMapReal.put(flowInstance.getId(), flowInstance);
            }
            if (children != null && !children.isEmpty()) {
                for (FlowInstance son : children) {
                    Map<String, FlowInstance> flowInstanceChildren = initGulianSonInstance(flowInstanceMapReal, son, force);
                    if (flowInstanceChildren == null) {
                        return null;
                    }
                }
            }
        } else {
            return null;
        }
        return flowInstanceMapReal;
    }

    /**
     * 撤销流程实例
     * 清除有关联的流程版本及对应的流程引擎数据
     *
     * @param id 待操作数据ID
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public OperateResult end(String id) {
        return this.endCommon(id, false);
    }

    /**
     * 撤销流程实例（网关支持的模式）
     * 清除有关联的流程版本及对应的流程引擎数据
     *
     * @param id 待操作数据ID
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public OperateResult endByFlowInstanceId(String id) {
        return this.endCommon(id, false);
    }

    /**
     * 强制撤销流程实例
     * 清除有关联的流程版本及对应的流程引擎数据
     *
     * @param id 待操作数据ID
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public OperateResult endForce(String id) {
        return this.endCommon(id, true);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseData checkAndEndByBusinessId(String businessId) {
        if (StringUtils.isEmpty(businessId)) {
            return ResponseData.operationFailure("参数单据ID不能为空！");
        }
        FlowInstance flowInstance = this.findLastInstanceByBusinessId(businessId);
        if (flowInstance == null) {
            return ResponseData.operationFailure("该单据未发起过流程！");
        } else if (flowInstance.isEnded() == true) {
            return ResponseData.operationFailure("该单据没有流程中数据！");
        } else {
            ResponseData res = checkEnd(flowInstance);
            if (!res.getSuccess()) {
                return res;
            }
        }
        return this.endCommon(flowInstance.getId(), false);
    }


    /**
     * 检查运行实例当前节点是否允许发起人终止流程
     *
     * @param flowInstance
     * @return
     */
    public ResponseData checkEnd(FlowInstance flowInstance) {
        Set<FlowTask> taskSet = flowInstance.getFlowTasks();
        Iterator<FlowTask> it = taskSet.iterator();
        List<String> keyList = new ArrayList<>();
        while (it.hasNext()) {
            FlowTask flowTask = it.next();
            String key = flowTask.getActTaskDefKey();
            if (keyList.size() != 0) {
                if (keyList.contains(key)) {   //如果已经判断过该节点，直接跳过
                    continue;
                }
            }
            keyList.add(key);
            String defJson = flowTask.getTaskJsonDef();
            JSONObject defObj = JSONObject.fromObject(defJson);
            JSONObject normalInfo = defObj.getJSONObject("nodeConfig").getJSONObject("normal");
            Boolean canStartEnd = false;
            if (normalInfo.get("allowTerminate") != null) {   //是否允许发起人终止
                canStartEnd = normalInfo.getBoolean("allowTerminate");
            }
            if (canStartEnd) {
                continue;
            } else {
                return ResponseData.operationFailure("流程已到达【" + defObj.getString("name") + "】，不允许发起人终止流程！");
            }
        }
        return ResponseData.operationSuccess();
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public OperateResult endByBusinessId(String businessId) {
        FlowInstance flowInstance = this.findLastInstanceByBusinessId(businessId);
        return this.end(flowInstance.getId());
    }

    /**
     * .net项目专用
     *
     * @param businessIds 需要终止的单据ID集合
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseData endByBusinessIdList(List<String> businessIds) {
        if (businessIds != null && !businessIds.isEmpty()) {
            List<FlowInstance> list = flowInstanceDao.findByBusinessIdListNoEnd(businessIds);
            if (list != null && !list.isEmpty()) {
                try {
                    for (FlowInstance fTemp : list) {
                        //删除所有待办
                        flowTaskDao.deleteByFlowInstanceId(fTemp.getId());
                        //删除所有流程历史
                        flowHistoryDao.deleteByFlowInstanceId(fTemp.getId());
                        //删除当前实例(包括底层表参数)
                        this.delete(fTemp.getId());
                    }
                } catch (FlowException e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return ResponseData.operationFailure("终止失败！", e);
                }
                return ResponseData.operationSuccess("已全部终止！");
            }
            return ResponseData.operationFailure("未找到没有终止的流程！");
        }
        return ResponseData.operationFailure("需要终止的ID集合不能为空！");
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public OperateResult signalByBusinessId(String businessId, String receiveTaskActDefId, Map<String, Object> v) {
        if (StringUtils.isEmpty(receiveTaskActDefId)) {
            return OperateResult.operationFailure("10032");
        }
        OperateResult result = OperateResult.operationSuccess("10029");
        FlowInstance flowInstance = this.findLastInstanceByBusinessId(businessId);
        if (flowInstance != null && !flowInstance.isEnded()) {
            String actInstanceId = flowInstance.getActInstanceId();
            HistoricActivityInstance receiveTaskActivityInstance = historyService.createHistoricActivityInstanceQuery().processInstanceId(actInstanceId).activityId(receiveTaskActDefId).unfinished().singleResult();
            if (receiveTaskActivityInstance != null) {
                String executionId = receiveTaskActivityInstance.getExecutionId();
                runtimeService.signal(executionId, v);
            } else {
                result = OperateResult.operationFailure("10031");
            }
        } else {
            result = OperateResult.operationFailure("10030");
        }
        return result;
    }

    //任务池指定真实用户组，抢单池确定用户组签定（多执行人）
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseData<List<FlowTask>> poolTaskSignByUserList(HistoricTaskInstance historicTaskInstance, List<String> userList, Map<String, Object> v) {
        String actTaskId = historicTaskInstance.getId();
        //根据用户的id列表获取执行人列表
        List<Executor> executorList = flowCommonUtil.getBasicUserExecutors(userList);
        if (executorList != null) {
            FlowTask oldFlowTask = flowTaskDao.findByActTaskId(actTaskId);
            //是否推送信息到baisc
            Boolean pushBasic = flowTaskService.getBooleanPushTaskToBasic();
            //是否推送信息到业务模块或者直接配置的url
            Boolean pushModelOrUrl = flowTaskService.getBooleanPushModelOrUrl(oldFlowTask.getFlowInstance());
            List<FlowTask> needDelList = new ArrayList<FlowTask>();  //需要删除的待办
            List<FlowTask> needAddList = new ArrayList<FlowTask>(); //需要新增的待办

            String flowTypeId = oldFlowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getId();

            for (Executor executor : executorList) {
                FlowTask newFlowTask = new FlowTask();
                org.springframework.beans.BeanUtils.copyProperties(oldFlowTask, newFlowTask);
                newFlowTask.setId(null);
                //通过授权用户ID和流程类型返回转授权信息（转办模式）
                TaskMakeOverPower taskMakeOverPower = taskMakeOverPowerService.getMakeOverPowerByTypeAndUserId(executor.getId(),flowTypeId);
                if (taskMakeOverPower != null) {
                    newFlowTask.setExecutorId(taskMakeOverPower.getPowerUserId());
                    newFlowTask.setExecutorAccount(taskMakeOverPower.getPowerUserAccount());
                    newFlowTask.setExecutorName(taskMakeOverPower.getPowerUserName());
                    //添加组织机构信息
                    newFlowTask.setExecutorOrgId(taskMakeOverPower.getPowerUserOrgId());
                    newFlowTask.setExecutorOrgCode(taskMakeOverPower.getPowerUserOrgCode());
                    newFlowTask.setExecutorOrgName(taskMakeOverPower.getPowerUserOrgName());
                    if (StringUtils.isEmpty(newFlowTask.getDepict())) {
                        newFlowTask.setDepict("【转授权-" + executor.getName() + "授权】");
                    } else {
                        newFlowTask.setDepict("【转授权-" + executor.getName() + "授权】" + newFlowTask.getDepict());
                    }
                } else {
                    newFlowTask.setExecutorId(executor.getId());
                    newFlowTask.setExecutorAccount(executor.getCode());
                    newFlowTask.setExecutorName(executor.getName());
                    //添加组织机构信息
                    newFlowTask.setExecutorOrgId(executor.getOrganizationId());
                    newFlowTask.setExecutorOrgCode(executor.getOrganizationCode());
                    newFlowTask.setExecutorOrgName(executor.getOrganizationName());
                }
                newFlowTask.setOwnerId(executor.getId());
                newFlowTask.setOwnerName(executor.getName());
                newFlowTask.setOwnerAccount(executor.getCode());
                //添加组织机构信息
                newFlowTask.setOwnerOrgId(executor.getOrganizationId());
                newFlowTask.setOwnerOrgCode(executor.getOrganizationCode());
                newFlowTask.setOwnerOrgName(executor.getOrganizationName());
                newFlowTask.setTrustState(0);
                if (v.get("instancyStatus") != null) {
                    try {
                        if ((Boolean) v.get("instancyStatus") == true) {
                            newFlowTask.setPriority(3);//设置为紧急
                        }
                    } catch (Exception e) {
                        LogUtil.error(e.getMessage());
                    }
                }
                flowTaskDao.save(newFlowTask);
                needAddList.add(newFlowTask);
            }

            needDelList.add(oldFlowTask);
            flowTaskDao.delete(oldFlowTask);

            if (pushBasic || pushModelOrUrl) {
                if (pushBasic) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            flowTaskService.pushToBasic(needAddList, null, needDelList, null);
                        }
                    }).start();
                }
                if (pushModelOrUrl) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            flowTaskService.pushTaskToModelOrUrl(oldFlowTask.getFlowInstance(), needDelList, TaskStatus.DELETE);
                            flowTaskService.pushTaskToModelOrUrl(oldFlowTask.getFlowInstance(), needAddList, TaskStatus.INIT);
                        }
                    }).start();
                }
            }
            return ResponseData.operationSuccessWithData(needAddList);
        } else {
            return ResponseData.operationFailure("执行人列表查询结果为空!");
        }

    }

    //任务池指定真实用户，抢单池确定用户签定
    @Transactional(propagation = Propagation.REQUIRED)
    public OperateResultWithData<FlowTask> poolTaskSign(HistoricTaskInstance historicTaskInstance, String userId, Map<String, Object> v) {
        OperateResultWithData<FlowTask> result = null;
        String actTaskId = historicTaskInstance.getId();
        //根据用户的id获取执行人
        Executor executor = flowCommonUtil.getBasicUserExecutor(userId);
        if (executor != null) {
            FlowTask newFlowTask = flowTaskDao.findByActTaskId(actTaskId);
            FlowTask delFlowTask = new FlowTask();
            org.springframework.beans.BeanUtils.copyProperties(newFlowTask, delFlowTask);
            //是否推送信息到baisc
            Boolean pushBasic = flowTaskService.getBooleanPushTaskToBasic();
            //是否推送信息到业务模块或者直接配置的url
            Boolean pushModelOrUrl = flowTaskService.getBooleanPushModelOrUrl(newFlowTask.getFlowInstance());
            List<FlowTask> needDelList = new ArrayList<FlowTask>();  //需要删除的待办
            if (pushBasic || pushModelOrUrl) {
                needDelList.add(delFlowTask);
            }
            //通过授权用户ID和流程类型返回转授权信息（转办模式）
            TaskMakeOverPower taskMakeOverPower = taskMakeOverPowerService.getMakeOverPowerByTypeAndUserId(executor.getId(),newFlowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getId());
            if (taskMakeOverPower != null) {
                newFlowTask.setExecutorId(taskMakeOverPower.getPowerUserId());
                newFlowTask.setExecutorAccount(taskMakeOverPower.getPowerUserAccount());
                newFlowTask.setExecutorName(taskMakeOverPower.getPowerUserName());
                //添加组织机构信息
                newFlowTask.setExecutorOrgId(taskMakeOverPower.getPowerUserOrgId());
                newFlowTask.setExecutorOrgCode(taskMakeOverPower.getPowerUserOrgCode());
                newFlowTask.setExecutorOrgName(taskMakeOverPower.getPowerUserOrgName());
                if (StringUtils.isEmpty(newFlowTask.getDepict())) {
                    newFlowTask.setDepict("【转授权-" + executor.getName() + "授权】");
                } else {
                    newFlowTask.setDepict("【转授权-" + executor.getName() + "授权】" + newFlowTask.getDepict());
                }
            } else {
                newFlowTask.setExecutorId(executor.getId());
                newFlowTask.setExecutorAccount(executor.getCode());
                newFlowTask.setExecutorName(executor.getName());
                //添加组织机构信息
                newFlowTask.setExecutorOrgId(executor.getOrganizationId());
                newFlowTask.setExecutorOrgCode(executor.getOrganizationCode());
                newFlowTask.setExecutorOrgName(executor.getOrganizationName());
            }
            newFlowTask.setOwnerId(executor.getId());
            newFlowTask.setOwnerName(executor.getName());
            newFlowTask.setOwnerAccount(executor.getCode());
            //添加组织机构信息
            newFlowTask.setOwnerOrgId(executor.getOrganizationId());
            newFlowTask.setOwnerOrgCode(executor.getOrganizationCode());
            newFlowTask.setOwnerOrgName(executor.getOrganizationName());
            newFlowTask.setTrustState(0);
            if (v != null && v.get("instancyStatus") != null) {
                try {
                    if ((Boolean) v.get("instancyStatus") == true) {
                        newFlowTask.setPriority(3);//设置为紧急
                    }
                } catch (Exception e) {
                    LogUtil.error(e.getMessage());
                }
            }
            taskService.setAssignee(actTaskId, executor.getId());

            flowTaskDao.save(newFlowTask);

            List<FlowTask> needAddList = new ArrayList<FlowTask>(); //需要新增的待办
            if (pushBasic || pushModelOrUrl) {
                needAddList.add(newFlowTask);
                if (pushBasic) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            flowTaskService.pushToBasic(needAddList, null, needDelList, null);
                        }
                    }).start();
                }
                if (pushModelOrUrl) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            flowTaskService.pushTaskToModelOrUrl(newFlowTask.getFlowInstance(), needDelList, TaskStatus.DELETE);
                            flowTaskService.pushTaskToModelOrUrl(newFlowTask.getFlowInstance(), needAddList, TaskStatus.INIT);
                        }
                    }).start();
                }
            }

            result = OperateResultWithData.operationSuccess();
            result.setData(newFlowTask);
        } else {
            result = OperateResultWithData.operationFailure("10038");//执行人查询结果为空
        }
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseData signalPoolTaskByBusinessIdAndUserList(SignalPoolTaskVO signalPoolTaskVO) {
        if (signalPoolTaskVO == null) {
            return ResponseData.operationFailure("工作池任务设置多执行人失败，参数不能为空！");
        }
        if (StringUtils.isEmpty(signalPoolTaskVO.getBusinessId())) {
            return ResponseData.operationFailure("工作池任务设置多执行人失败，单据ID未传入！");
        }
        if (StringUtils.isEmpty(signalPoolTaskVO.getPoolTaskActDefId())) {
            return ResponseData.operationFailure("工作池任务设置多执行人失败,工作池节点ID未传入！");
        }
        if (signalPoolTaskVO.getUserIds() == null || signalPoolTaskVO.getUserIds().size() == 0) {
            return ResponseData.operationFailure("工作池任务设置多执行人失败,用户ID未传入！");
        }

        if (signalPoolTaskVO.getUserIds().size() == 1) {   //只有一个执行人
            return this.signalPoolTaskByBusinessId(signalPoolTaskVO.getBusinessId(), signalPoolTaskVO.getPoolTaskActDefId(), signalPoolTaskVO.getUserIds().get(0), signalPoolTaskVO.getMap());
        } else {
            FlowInstance flowInstance = this.findLastInstanceByBusinessId(signalPoolTaskVO.getBusinessId());
            if (flowInstance != null && !flowInstance.isEnded()) {
                String actInstanceId = flowInstance.getActInstanceId();
                HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().processInstanceId(actInstanceId).taskDefinitionKey(signalPoolTaskVO.getPoolTaskActDefId()).unfinished().singleResult(); // 创建历史任务实例查询
                if (historicTaskInstance != null) {
                    this.poolTaskSignByUserList(historicTaskInstance, signalPoolTaskVO.getUserIds(), signalPoolTaskVO.getMap());
                    return ResponseData.operationSuccess();
                } else {
                    return ResponseData.operationFailure("工作池任务设置多执行人失败,当前节点不存在！");
                }
            } else {
                return ResponseData.operationFailure("工作池任务设置多执行人失败,流程实例找不到，或者已经结束！");
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public OperateResult signalPoolTaskByBusinessId(String businessId, String poolTaskActDefId, String userId, Map<String, Object> v) {
        if (StringUtils.isEmpty(poolTaskActDefId)) {
            return OperateResult.operationFailure("工作池任务设置执行人失败,请传入工作池节点ID");
        }
        OperateResult result = null;
        FlowInstance flowInstance = this.findLastInstanceByBusinessId(businessId);
        if (flowInstance != null && !flowInstance.isEnded()) {
            String actInstanceId = flowInstance.getActInstanceId();
            HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().processInstanceId(actInstanceId).taskDefinitionKey(poolTaskActDefId).unfinished().singleResult(); // 创建历史任务实例查询
            if (historicTaskInstance != null) {
                OperateResultWithData<FlowTask> operateResultWithData = this.poolTaskSign(historicTaskInstance, userId, v);
                if (operateResultWithData.successful()) {
                    result = OperateResult.operationSuccess(operateResultWithData.getMessage());
                } else {
                    result = OperateResult.operationFailure(operateResultWithData.getMessage());
                }
            } else {
                result = OperateResult.operationFailure("工作池任务设置执行人失败,当前节点不存在！");
            }
        } else {
            result = OperateResult.operationFailure("工作池任务设置执行人失败,流程实例找不到，或者已经结束！");
        }
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public OperateResultWithData<FlowTask> signalPoolTaskByBusinessIdWithResult(String businessId, String poolTaskActDefId, String userId, Map<String, Object> v) {
        if (StringUtils.isEmpty(poolTaskActDefId)) {
            return OperateResultWithData.operationFailure("10032");
        }
        OperateResultWithData<FlowTask> result = null;
        FlowInstance flowInstance = this.findLastInstanceByBusinessId(businessId);
        if (flowInstance != null && !flowInstance.isEnded()) {
            String actInstanceId = flowInstance.getActInstanceId();
            HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().processInstanceId(actInstanceId).taskDefinitionKey(poolTaskActDefId).unfinished().singleResult(); // 创建历史任务实例查询
            if (historicTaskInstance != null) {
                result = this.poolTaskSign(historicTaskInstance, userId, v);
            } else {
                result = OperateResultWithData.operationFailure("10031");
            }
        } else {
            result = OperateResultWithData.operationFailure("10030");
        }
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public OperateResultWithData<FlowStatus> completePoolTask(String businessId, String poolTaskActDefId, String userId, FlowTaskCompleteVO flowTaskCompleteVO) throws Exception {
        if (StringUtils.isEmpty(poolTaskActDefId)) {
            return OperateResultWithData.operationFailure("10032");
        }
        OperateResultWithData<FlowStatus> result = null;
        FlowInstance flowInstance = this.findLastInstanceByBusinessId(businessId);
        if (flowInstance != null && !flowInstance.isEnded()) {
            OperateResultWithData<FlowTask> resultSignal = signalPoolTaskByBusinessIdWithResult(businessId, poolTaskActDefId, userId, flowTaskCompleteVO.getVariables());
            if (resultSignal != null && resultSignal.successful()) {
                FlowTask flowTask = resultSignal.getData();
                flowTaskCompleteVO.setTaskId(flowTask.getId());
                result = flowTaskService.complete(flowTaskCompleteVO);
            } else {
                result = OperateResultWithData.operationFailure("10031");
            }
        } else {
            result = OperateResultWithData.operationFailure("10030");
        }
        return result;
    }

    /**
     * 撤销流程实例
     * 清除有关联的流程版本及对应的流程引擎数据
     *
     * @param id 待操作数据ID
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public OperateResult endCommon(String id, boolean force) {
        OperateResult result = OperateResult.operationSuccess("10010");
        try {
            FlowInstance flowInstance = flowInstanceDao.findOne(id);
            Map<String, FlowInstance> flowInstanceMap = new HashMap<String, FlowInstance>();
            flowInstanceMap = initAllGulianInstance(flowInstanceMap, flowInstance, force);

            if (flowInstanceMap != null && !flowInstanceMap.isEmpty()) {
                List<FlowInstance> flowInstanceList = new ArrayList<FlowInstance>();
                flowInstanceList.addAll(flowInstanceMap.values());//加入排序，按照创建时候倒序，保证子流程先终止
                Collections.sort(flowInstanceList, new Comparator<FlowInstance>() {
                    @Override
                    public int compare(FlowInstance flowInstance1, FlowInstance flowInstance2) {
                        return timeCompare(flowInstance1.getCreatedDate(), flowInstance2.getCreatedDate());
                    }
                });

                //是否推送信息到baisc
                Boolean pushBasic = flowTaskService.getBooleanPushTaskToBasic();

                for (FlowInstance fTemp : flowInstanceList) {
                    if (fTemp.isEnded()) {
                        continue;
                    }
                    //是否推送信息到业务模块或者直接配置的url
                    Boolean pushModelOrUrl = flowTaskService.getBooleanPushModelOrUrl(fTemp);

                    Set<FlowTask> flowTaskList = fTemp.getFlowTasks();
                    if (flowTaskList != null && !flowTaskList.isEmpty()) {
                        List<FlowTask> needDelList = new ArrayList<FlowTask>();
                        for (FlowTask flowTask : flowTaskList) {
                            try {
                                FlowHistory flowHistory = new FlowHistory();
                                String preFlowHistoryId = flowTask.getPreId();
                                FlowHistory preFlowHistory = null;
                                if (StringUtils.isNotEmpty(preFlowHistoryId)) {
                                    preFlowHistory = flowHistoryDao.findOne(preFlowHistoryId);
                                }
                                BeanUtils.copyProperties(flowHistory, flowTask);
                                flowHistory.setId(null);
                                flowHistory.setOldTaskId(flowTask.getId());
                                flowHistory.setFlowDefId(flowTask.getFlowDefinitionId());
                                if (!force) {
                                    flowHistory.setDepict(ContextUtil.getMessage("10036"));//【被发起人终止流程】
                                } else {
                                    flowHistory.setDepict(ContextUtil.getMessage("10035"));//"【被管理员强制终止流程】"
                                }
                                flowHistory.setFlowTaskName(flowTask.getTaskName());
                                Date now = new Date();
                                if (preFlowHistory != null) {
                                    flowHistory.setActDurationInMillis(now.getTime() - preFlowHistory.getActEndTime().getTime());
                                } else {
                                    flowHistory.setActDurationInMillis(now.getTime() - flowTask.getCreatedDate().getTime());
                                }
                                flowHistory.setActEndTime(now);
                                flowHistory.setFlowExecuteStatus(FlowExecuteStatus.END.getCode());//终止
                                flowHistoryDao.save(flowHistory);
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                            if (pushBasic || pushModelOrUrl) {//是否推送信息到baisc、<业务模块>、<配置的url>
                                needDelList.add(flowTask);
                            }
                            flowTaskDao.delete(flowTask);
                        }
                        if (pushBasic) {  //流程终止时，异步推送需要删除待办到baisc
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    flowTaskService.pushToBasic(null, null, needDelList, null);
                                }
                            }).start();
                        }
                        if (pushModelOrUrl) {  //流程终止时，异步推送成已办<业务模块>、<配置的url>
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    flowTaskService.pushTaskToModelOrUrl(fTemp, needDelList, TaskStatus.DELETE);
                                }
                            }).start();
                        }

                    }

                    String actInstanceId = fTemp.getActInstanceId();
                    String deleteReason = null;
                    int endSign = 0;
                    if (force) {
                        deleteReason = "10035";//"被管理员强制终止流程";
                        endSign = 2;
                    } else {
                        deleteReason = "10036";// "被发起人终止流程";
                        endSign = 1;
                    }
                    callBeforeEndAndSon(flowInstance, endSign);

                    this.deleteActiviti(actInstanceId, deleteReason);

                    fTemp.setEndDate(new Date());
                    fTemp.setEnded(true);
                    fTemp.setManuallyEnd(true);
                    flowInstanceDao.save(fTemp);
                    //重置客户端表单流程状态
                    String businessId = fTemp.getBusinessId();
                    FlowStatus status = FlowStatus.INIT;
                    BusinessModel businessModel = flowInstance.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel();
                    ExpressionUtil.resetState(businessModel, businessId, status);
                    //查看是否为固化流程（如果是固化流程删除固化执行人列表）
                    flowSolidifyExecutorDao.deleteByBusinessId(businessId);
                    //结束后触发
                    try {
                        this.callEndServiceAndSon(flowInstance, endSign);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            } else {
                if (force) {
                    result = OperateResult.operationFailure("10002");//不能终止
                } else {
                    result = OperateResult.operationFailure("10011");//不能终止
                }
            }
        } catch (FlowException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            result = OperateResult.operationFailure(e.getMessage());//终止失败
        }
        return result;
    }

    @Override
    public PageResult<FlowInstance> findByPage(Search searchConfig) {
        PageResult<FlowInstance> result = super.findByPage(searchConfig);
        if (result != null) {
            List<FlowInstance> flowInstanceList = result.getRows();
            this.initUrl(flowInstanceList);
        }
        return result;
    }


    private List<FlowInstance> initUrl(List<FlowInstance> result) {
        if (result != null && !result.isEmpty()) {
            for (FlowInstance flowInstance : result) {
                String apiBaseAddressConfig = flowInstance.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getAppModule().getApiBaseAddress();
                String apiBaseAddress = Constants.getConfigValueByApi(apiBaseAddressConfig);
                if (StringUtils.isNotEmpty(apiBaseAddress)) {
                    flowInstance.setApiBaseAddressAbsolute(apiBaseAddress);
                    String[] tempApiBaseAddress = apiBaseAddress.split("/");
                    if (tempApiBaseAddress != null && tempApiBaseAddress.length > 0) {
                        apiBaseAddress = tempApiBaseAddress[tempApiBaseAddress.length - 1];
                        flowInstance.setApiBaseAddress("/" + apiBaseAddress);
                    }
                }
                String webBaseAddressConfig = flowInstance.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getAppModule().getWebBaseAddress();
                String webBaseAddress = Constants.getConfigValueByWeb(webBaseAddressConfig);
                if (StringUtils.isNotEmpty(webBaseAddress)) {
                    flowInstance.setWebBaseAddressAbsolute(webBaseAddress);
                    String[] tempWebBaseAddress = webBaseAddress.split("/");
                    if (tempWebBaseAddress != null && tempWebBaseAddress.length > 0) {
                        webBaseAddress = tempWebBaseAddress[tempWebBaseAddress.length - 1];
                        flowInstance.setWebBaseAddress("/" + webBaseAddress);
                    }
                }

            }
        }
        return result;
    }

    private void callEndServiceAndSon(FlowInstance flowInstance, int endSign) {
        FlowDefVersion flowDefVersion = flowInstance.getFlowDefVersion();
        List<FlowInstance> flowInstanceChildren = flowInstanceDao.findByParentId(flowInstance.getId());//针对子流程
        if (flowInstanceChildren != null && !flowInstanceChildren.isEmpty()) {
            for (FlowInstance son : flowInstanceChildren) {
                callEndServiceAndSon(son, endSign);
            }
        }
        flowListenerTool.callEndService(flowInstance.getBusinessId(), flowDefVersion, endSign, null);
    }


    /**
     * 对包含子流程在内进行终止前服务调用检查
     *
     * @param flowInstance
     * @param endSign
     * @return
     */
    private FlowOperateResult callBeforeEndAndSon(FlowInstance flowInstance, int endSign) {
        FlowDefVersion flowDefVersion = flowInstance.getFlowDefVersion();
        List<FlowInstance> flowInstanceChildren = flowInstanceDao.findByParentId(flowInstance.getId());//针对子流程
        if (flowInstanceChildren != null && !flowInstanceChildren.isEmpty()) {
            for (FlowInstance son : flowInstanceChildren) {
                callBeforeEndAndSon(son, endSign);
            }
        }
        BusinessModel businessModel = flowInstance.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel();
        AppModule appModule = businessModel.getAppModule();
        FlowOperateResult callBeforeEndResult = flowListenerTool.callBeforeEnd(flowInstance.getBusinessId(), flowDefVersion, endSign, null);
        if (callBeforeEndResult != null && callBeforeEndResult.isSuccess() != true) {
            String message = "BusinessId=" + flowInstance.getBusinessId()
                    + ",FlowDefVersion.id=" + flowInstance.getFlowDefVersion().getId()
                    + ",appModule.code=" + appModule.getCode()
                    + ",Check the error before the end of the process and return the message :" + callBeforeEndResult.getMessage();
            logger.info(message);
            throw new FlowException(message);
        }
        return callBeforeEndResult;
    }


    /**
     * 查询当前用户我的单据汇总信息
     *
     * @param orderType 流程状态：all-全部、inFlow-流程中、ended-正常完成、abnormalEnd-异常结束
     * @return 汇总信息
     */
    public List<TodoBusinessSummaryVO> findMyBillsSumHeader(String orderType, String appModelCode) {
        List<TodoBusinessSummaryVO> voList = new ArrayList<>();
        String userID = ContextUtil.getUserId();
        Boolean ended = null;
        Boolean manuallyEnd = null;
        if ("ended".equals(orderType)) {
            ended = true;
            manuallyEnd = false;
        } else if ("inFlow".equals(orderType)) {
            ended = false;
            manuallyEnd = false;
        } else if ("abnormalEnd".equals(orderType)) {
            ended = true;
            manuallyEnd = true;
        }

        List groupResultList;
        if (ended == null) {
            if (appModelCode == null) {
                groupResultList = flowInstanceDao.findBillsByGroup(userID);
            } else {
                groupResultList = flowInstanceDao.findBillsByGroupAndAppCode(userID, appModelCode);
            }
        } else {
            if (appModelCode == null) {
                groupResultList = flowInstanceDao.findBillsByExecutorIdGroup(userID, ended, manuallyEnd);
            } else {
                groupResultList = flowInstanceDao.findBillsByExecutorIdGroupAndAppCode(userID, ended, manuallyEnd, appModelCode);
            }
        }

        Map<BusinessModel, Integer> businessModelCountMap = new HashMap<>();
        if (groupResultList != null && !groupResultList.isEmpty()) {
            Iterator it = groupResultList.iterator();
            while (it.hasNext()) {
                Object[] res = (Object[]) it.next();
                int count = ((Number) res[0]).intValue();
                String flowDefinationId = res[1] + "";
                FlowDefination flowDefination = flowDefinationDao.findOne(flowDefinationId);
                if (flowDefination == null) {
                    continue;
                }
                // 获取业务类型
                BusinessModel businessModel = businessModelDao.findOne(flowDefination.getFlowType().getBusinessModel().getId());
                Integer oldCount = businessModelCountMap.get(businessModel);
                if (oldCount == null) {
                    oldCount = 0;
                }
                businessModelCountMap.put(businessModel, oldCount + count);
            }
        }

        if (!businessModelCountMap.isEmpty()) {
            for (Map.Entry<BusinessModel, Integer> map : businessModelCountMap.entrySet()) {
                TodoBusinessSummaryVO todoBusinessSummaryVO = new TodoBusinessSummaryVO();
                todoBusinessSummaryVO.setBusinessModelCode(map.getKey().getClassName());
                todoBusinessSummaryVO.setBusinessModeId(map.getKey().getId());
                todoBusinessSummaryVO.setCount(map.getValue());
                todoBusinessSummaryVO.setBusinessModelName(map.getKey().getName() + "(" + map.getValue() + ")");
                voList.add(todoBusinessSummaryVO);
            }
        }
        return voList;
    }

    @Override
    public ResponseData listMyBillsHeader(MyBillsHeaderVo myBillsHeaderVo) {
        try {
            List<TodoBusinessSummaryVO> list = this.findMyBillsSumHeader(myBillsHeaderVo.getOrderType(), myBillsHeaderVo.getAppModelCode());
            return ResponseData.operationSuccessWithData(list);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            return ResponseData.operationFailure("操作失败！");
        }
    }

    @Override
    public ResponseData listAllMyBillsHeader() {
        List<TodoBusinessSummaryVO> voList = new ArrayList<>();
        String userID = ContextUtil.getUserId();
        List groupResultList= flowInstanceDao.findBillsByGroup(userID);

        Map<BusinessModel, Integer> businessModelCountMap = new HashMap<>();
        if (groupResultList != null && !groupResultList.isEmpty()) {
            Iterator it = groupResultList.iterator();
            while (it.hasNext()) {
                Object[] res = (Object[]) it.next();
                int count = ((Number) res[0]).intValue();
                String flowDefinationId = res[1] + "";
                FlowDefination flowDefination = flowDefinationDao.findOne(flowDefinationId);
                if (flowDefination == null) {
                    continue;
                }
                // 获取业务类型
                BusinessModel businessModel = businessModelDao.findOne(flowDefination.getFlowType().getBusinessModel().getId());
                Integer oldCount = businessModelCountMap.get(businessModel);
                if (oldCount == null) {
                    oldCount = 0;
                }
                businessModelCountMap.put(businessModel, oldCount + count);
            }
        }

        if (!businessModelCountMap.isEmpty()) {
            for (Map.Entry<BusinessModel, Integer> map : businessModelCountMap.entrySet()) {
                TodoBusinessSummaryVO todoBusinessSummaryVO = new TodoBusinessSummaryVO();
                todoBusinessSummaryVO.setBusinessModelCode(map.getKey().getClassName());
                todoBusinessSummaryVO.setBusinessModeId(map.getKey().getId());
                todoBusinessSummaryVO.setCount(map.getValue());
                todoBusinessSummaryVO.setBusinessModelName(map.getKey().getName());
                voList.add(todoBusinessSummaryVO);
            }
        }
        return ResponseData.operationSuccessWithData(voList);
    }

    public String getExecutorStringByInstanceId(String instanceId) {
        List<FlowTask> list = flowTaskService.findByInstanceId(instanceId);
        StringBuilder executorString = new StringBuilder();
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                FlowTask flowTask = list.get(i);
                if (i == 0) {
                    executorString.append(flowTask.getExecutorName() + "【" + flowTask.getExecutorAccount() + "】");
                } else {
                    executorString.append("," + flowTask.getExecutorName() + "【" + flowTask.getExecutorAccount() + "】");
                }
            }
        }
        return executorString.toString();
    }

    @Override
    public ResponseData getMyBillsAndExecutorByModeId(String appModelCode, String modelId, Search search) {
        ResponseData responseData;
        if (search != null) {
            List<SearchFilter> listFilter = search.getFilters();
            if (StringUtils.isNotEmpty(appModelCode)) {
                listFilter.add(new SearchFilter("flowDefVersion.flowDefination.flowType.businessModel.appModule.code", appModelCode, SearchFilter.Operator.EQ));
            }
            if (StringUtils.isNotEmpty(modelId)) {
                listFilter.add(new SearchFilter("flowDefVersion.flowDefination.flowType.businessModel.id", modelId, SearchFilter.Operator.EQ));
            }
            responseData = this.getMyBills(search);
        } else {
            return ResponseData.operationFailure("获取我的单据时，search 对象不能为空。");
        }
        if (responseData.getSuccess()) {
            PageResult<MyBillVO> results = (PageResult<MyBillVO>) responseData.getData();
            ArrayList<MyBillVO> data = results.getRows();
            data.forEach(a -> {
                if (!a.getEnded()) {
                    a.setTaskExecutors(this.getExecutorStringByInstanceId(a.getFlowInstanceId()));
                }
            });
        }
        return responseData;
    }

    @Override
    public ResponseData getMyBillsByModeId(String modelId, Search search) {
        if (StringUtils.isEmpty(modelId)) {
            return this.getMyBills(search);
        } else {
            if (search != null) {
                List<SearchFilter> listFilter = search.getFilters();
                listFilter.add(new SearchFilter("flowDefVersion.flowDefination.flowType.businessModel.id", modelId, SearchFilter.Operator.EQ));
                return this.getMyBills(search);
            } else {
                return ResponseData.operationFailure("获取我的单据时，search 对象不能为空。");
            }
        }
    }

    @Override
    public ResponseData getAllMyBills(UserFlowBillsQueryParam queryParam) {
        //设置流程状态
        List<SearchFilter> searchFilters = queryParam.getFilters();
        if(searchFilters == null){
            searchFilters = new ArrayList<>();
        }
        if("inflow".equalsIgnoreCase(queryParam.getFlowStatus())){ //流程中
            SearchFilter filter1 = new SearchFilter("ended",false,SearchFilter.Operator.EQ);
            SearchFilter filter2 = new SearchFilter("manuallyEnd",false,SearchFilter.Operator.EQ);
            searchFilters.add(filter1);
            searchFilters.add(filter2);
        }else if("ended".equalsIgnoreCase(queryParam.getFlowStatus())){ //正常结束
            SearchFilter filter1 = new SearchFilter("ended",true,SearchFilter.Operator.EQ);
            SearchFilter filter2 = new SearchFilter("manuallyEnd",false,SearchFilter.Operator.EQ);
            searchFilters.add(filter1);
            searchFilters.add(filter2);
        }else if("abnormalEnd".equalsIgnoreCase(queryParam.getFlowStatus())){ //异常终止
            SearchFilter filter1 = new SearchFilter("ended",true,SearchFilter.Operator.EQ);
            SearchFilter filter2 = new SearchFilter("manuallyEnd",true,SearchFilter.Operator.EQ);
            searchFilters.add(filter1);
            searchFilters.add(filter2);
        }
        return     this.getMyBillsByModeId(queryParam.getModelId(),queryParam);
    }

    public ResponseData getMyBills(Search search) {
        ResponseData responseData = new ResponseData();
        if (search != null) {
            SessionUser user = ContextUtil.getSessionUser();
            String creatorId = user.getUserId();
            SearchFilter searchFilterCreatorId = new SearchFilter("creatorId", creatorId, SearchFilter.Operator.EQ);
            search.addFilter(searchFilterCreatorId);

            List<SearchFilter> listFilter = search.getFilters();
            listFilter.forEach(filter -> {
                if (filter.getFieldName().equals("startDate") || filter.getFieldName().equals("endDate")) {
                    SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd");
                    String startDateString;
                    if (filter.getValue() != null) {
                        startDateString = sim.format((Date) filter.getValue());
                    } else {
                        if (filter.getFieldName().equals("startDate")) {
                            startDateString = "1949-10-1";
                        } else {
                            startDateString = sim.format(new Date());
                        }
                    }
                    try {
                        Date newDate = sim.parse(startDateString);
                        filter.setValue(newDate);
                    } catch (Exception e) {
                    }
                }
            });

            try {
                PageResult<FlowInstance> flowInstancePageResult = this.findByPage(search);
                List<FlowInstance> flowInstanceList = flowInstancePageResult.getRows();
                PageResult<MyBillVO> results = new PageResult<MyBillVO>();
                ArrayList<MyBillVO> data = new ArrayList<MyBillVO>();
                if (flowInstanceList != null && !flowInstanceList.isEmpty()) {
                    List<String> flowInstanceIds = new ArrayList<String>();
                    for (FlowInstance f : flowInstanceList) {
                        FlowInstance parent = f.getParent();
                        if (parent != null) {
                            flowInstancePageResult.setRecords(flowInstancePageResult.getRecords() - 1);
                            continue;
                        }
                        flowInstanceIds.add(f.getId());
                        MyBillVO myBillVO = new MyBillVO();
                        myBillVO.setBusinessCode(f.getBusinessCode());
                        myBillVO.setBusinessId(f.getBusinessId());
                        myBillVO.setBusinessModelRemark(f.getBusinessModelRemark());
                        myBillVO.setBusinessName(f.getBusinessName());
                        myBillVO.setCreatedDate(f.getCreatedDate());
                        myBillVO.setCreatorAccount(f.getCreatorAccount());
                        myBillVO.setCreatorName(f.getCreatorName());
                        myBillVO.setCreatorId(f.getCreatorId());
                        myBillVO.setFlowName(f.getFlowName());
                        String lookUrl = f.getFlowDefVersion().getFlowDefination().getFlowType().getLookUrl();
                        String businessDetailServiceUrl = f.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessDetailServiceUrl();
                        if (StringUtils.isEmpty(lookUrl)) {
                            lookUrl = f.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getLookUrl();
                        }
                        if (StringUtils.isEmpty(businessDetailServiceUrl)) {
                            businessDetailServiceUrl = f.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getBusinessDetailServiceUrl();
                        }
                        myBillVO.setBusinessDetailServiceUrl(businessDetailServiceUrl);
                        myBillVO.setBusinessModelCode(f.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getClassName());
                        myBillVO.setLookUrl(lookUrl);
                        myBillVO.setEndDate(f.getEndDate());
                        myBillVO.setFlowInstanceId(f.getId());
                        myBillVO.setWebBaseAddress(f.getWebBaseAddress());
                        myBillVO.setWebBaseAddressAbsolute(f.getWebBaseAddressAbsolute());
                        myBillVO.setApiBaseAddress(f.getApiBaseAddress());
                        myBillVO.setApiBaseAddressAbsolute(f.getApiBaseAddressAbsolute());
                        myBillVO.setEnded(f.isEnded());
                        myBillVO.setManuallyEnd(f.isManuallyEnd());
                        data.add(myBillVO);
                    }

                    List<Boolean> canEnds = this.checkIdsCanEnd(flowInstanceIds);
                    if (canEnds != null && !canEnds.isEmpty()) {
                        for (int i = 0; i < canEnds.size(); i++) {
                            data.get(i).setCanManuallyEnd(canEnds.get(i));
                        }
                    }
                }
                results.setRows(data);
                results.setRecords(flowInstancePageResult.getRecords());
                results.setPage(flowInstancePageResult.getPage());
                results.setTotal(flowInstancePageResult.getTotal());
                responseData.setData(results);
            } catch (Exception e) {
                responseData.setSuccess(false);
                responseData.setMessage(e.getMessage());
                LogUtil.error(e.getMessage(), e);
            }
        } else {
            return ResponseData.operationFailure("获取我的单据时，search 对象不能为空。");
        }
        return responseData;
    }


    public PageResult<MyBillPhoneVO> getMyBillsOfMobile(int page, int rows, String quickValue, boolean ended) {
        String creatorId = ContextUtil.getUserId();
        Search search = new Search();
        SearchFilter searchFilterCreatorId = new SearchFilter("creatorId", creatorId, SearchFilter.Operator.EQ);
        search.addFilter(searchFilterCreatorId);
        SearchFilter searchFiltereEnded = new SearchFilter("ended", ended, SearchFilter.Operator.EQ);
        search.addFilter(searchFiltereEnded);


        //根据业务单据名称、业务单据号、业务工作说明快速查询
        search.addQuickSearchProperty("businessName");
        search.addQuickSearchProperty("businessCode");
        search.addQuickSearchProperty("businessModelRemark");
        search.setQuickSearchValue(quickValue);

        PageInfo pageInfo = new PageInfo();
        pageInfo.setPage(page);
        pageInfo.setRows(rows);
        search.setPageInfo(pageInfo);

        SearchOrder searchOrder = new SearchOrder("createdDate", SearchOrder.Direction.DESC);
        List<SearchOrder> list = new ArrayList<SearchOrder>();
        list.add(searchOrder);
        search.setSortOrders(list);

        PageResult<FlowInstance> flowInstancePageResult = this.findByPage(search);
        List<FlowInstance> flowInstanceList = flowInstancePageResult.getRows();
        PageResult<MyBillPhoneVO> results = new PageResult<MyBillPhoneVO>();
        ArrayList<MyBillPhoneVO> data = new ArrayList<MyBillPhoneVO>();
        if (flowInstanceList != null && !flowInstanceList.isEmpty()) {
            List<String> flowInstanceIds = new ArrayList<String>();
            for (FlowInstance f : flowInstanceList) {
                FlowInstance parent = f.getParent();
                if (parent != null) {
                    flowInstancePageResult.setRecords(flowInstancePageResult.getRecords() - 1);
                    continue;
                }
                flowInstanceIds.add(f.getId());
                MyBillPhoneVO myBillVO = new MyBillPhoneVO();
                myBillVO.setBusinessCode(f.getBusinessCode());
                myBillVO.setBusinessId(f.getBusinessId());
                myBillVO.setBusinessModelRemark(f.getBusinessModelRemark());
                myBillVO.setCreatedDate(f.getCreatedDate());
                myBillVO.setFlowName(f.getFlowName());
                myBillVO.setFlowInstanceId(f.getId());
                myBillVO.setFlowTypeId(f.getFlowDefVersion().getFlowDefination().getFlowType().getId());
                String businessDetailServiceUrl = f.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessDetailServiceUrl();
                myBillVO.setBusinessModelCode(f.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getClassName());
                if (StringUtils.isEmpty(businessDetailServiceUrl)) {
                    businessDetailServiceUrl = f.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getBusinessDetailServiceUrl();
                }
                myBillVO.setDetailUrl(f.getApiBaseAddressAbsolute() + businessDetailServiceUrl);
                data.add(myBillVO);
            }

            List<Boolean> canEnds = this.checkIdsCanEnd(flowInstanceIds);
            if (canEnds != null && !canEnds.isEmpty()) {
                for (int i = 0; i < canEnds.size(); i++) {
                    data.get(i).setCanManuallyEnd(canEnds.get(i));
                }
            }
        }
        results.setRows(data);
        results.setRecords(flowInstancePageResult.getRecords());
        results.setPage(flowInstancePageResult.getPage());
        results.setTotal(flowInstancePageResult.getTotal());

        return results;
    }


    public PageResult<MyBillVO> getMyBillsOfPhone(String property, String direction, int page, int rows,
                                                  String quickValue, String startDate, String endDate, boolean ended) {
        String creatorId = ContextUtil.getUserId();
        Search search = new Search();
        SearchFilter searchFilterCreatorId = new SearchFilter("creatorId", creatorId, SearchFilter.Operator.EQ);
        search.addFilter(searchFilterCreatorId);
        SearchFilter searchFiltereEnded = new SearchFilter("ended", ended, SearchFilter.Operator.EQ);
        search.addFilter(searchFiltereEnded);
        if (StringUtils.isNotEmpty(startDate)) {
            SearchFilter searchFilterStartDate = new SearchFilter("startDate", DateUtils.parseDate(startDate), SearchFilter.Operator.GE);
            search.addFilter(searchFilterStartDate);
        }
        if (StringUtils.isNotEmpty(endDate)) {
            SearchFilter searchFilterEndDate = new SearchFilter("endDate", DateUtils.parseDate(endDate), SearchFilter.Operator.LE);
            search.addFilter(searchFilterEndDate);
        }


        //根据业务单据名称、业务单据号、业务工作说明快速查询
        search.addQuickSearchProperty("businessName");
        search.addQuickSearchProperty("businessCode");
        search.addQuickSearchProperty("businessModelRemark");
        search.setQuickSearchValue(quickValue);

        PageInfo pageInfo = new PageInfo();
        pageInfo.setPage(page);
        pageInfo.setRows(rows);
        search.setPageInfo(pageInfo);

        SearchOrder searchOrder;
        if (StringUtils.isNotEmpty(property) && StringUtils.isNotEmpty(direction)) {
            if (SearchOrder.Direction.ASC.equals(direction)) {
                searchOrder = new SearchOrder(property, SearchOrder.Direction.ASC);
            } else {
                searchOrder = new SearchOrder(property, SearchOrder.Direction.DESC);
            }
        } else {
            searchOrder = new SearchOrder("createdDate", SearchOrder.Direction.DESC);
        }
        List<SearchOrder> list = new ArrayList<SearchOrder>();
        list.add(searchOrder);
        search.setSortOrders(list);

        PageResult<FlowInstance> flowInstancePageResult = this.findByPage(search);
        List<FlowInstance> flowInstanceList = flowInstancePageResult.getRows();
        PageResult<MyBillVO> results = new PageResult<MyBillVO>();
        ArrayList<MyBillVO> data = new ArrayList<MyBillVO>();
        if (flowInstanceList != null && !flowInstanceList.isEmpty()) {
            List<String> flowInstanceIds = new ArrayList<String>();
            for (FlowInstance f : flowInstanceList) {
                FlowInstance parent = f.getParent();
                if (parent != null) {
                    flowInstancePageResult.setRecords(flowInstancePageResult.getRecords() - 1);
                    continue;
                }
                flowInstanceIds.add(f.getId());
                MyBillVO myBillVO = new MyBillVO();
                myBillVO.setBusinessCode(f.getBusinessCode());
                myBillVO.setBusinessId(f.getBusinessId());
                myBillVO.setBusinessModelRemark(f.getBusinessModelRemark());
                myBillVO.setBusinessName(f.getBusinessName());
                myBillVO.setCreatedDate(f.getCreatedDate());
                myBillVO.setCreatorAccount(f.getCreatorAccount());
                myBillVO.setCreatorName(f.getCreatorName());
                myBillVO.setCreatorId(f.getCreatorId());
                myBillVO.setFlowName(f.getFlowName());
                String lookUrl = f.getFlowDefVersion().getFlowDefination().getFlowType().getLookUrl();
                String businessDetailServiceUrl = f.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessDetailServiceUrl();
                if (StringUtils.isEmpty(lookUrl)) {
                    lookUrl = f.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getLookUrl();
                }
                if (StringUtils.isEmpty(businessDetailServiceUrl)) {
                    businessDetailServiceUrl = f.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getBusinessDetailServiceUrl();
                }
                myBillVO.setBusinessDetailServiceUrl(businessDetailServiceUrl);
                myBillVO.setBusinessModelCode(f.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getClassName());
                myBillVO.setLookUrl(lookUrl);
                myBillVO.setEndDate(f.getEndDate());
                myBillVO.setFlowInstanceId(f.getId());
                myBillVO.setWebBaseAddress(f.getWebBaseAddress());
                myBillVO.setWebBaseAddressAbsolute(f.getWebBaseAddressAbsolute());
                myBillVO.setApiBaseAddress(f.getApiBaseAddress());
                myBillVO.setApiBaseAddressAbsolute(f.getApiBaseAddressAbsolute());
                data.add(myBillVO);
            }

            List<Boolean> canEnds = this.checkIdsCanEnd(flowInstanceIds);
            if (canEnds != null && !canEnds.isEmpty()) {
                for (int i = 0; i < canEnds.size(); i++) {
                    data.get(i).setCanManuallyEnd(canEnds.get(i));
                }
            }
        }
        results.setRows(data);
        results.setRecords(flowInstancePageResult.getRecords());
        results.setPage(flowInstancePageResult.getPage());
        results.setTotal(flowInstancePageResult.getTotal());

        return results;
    }


    public ResponseData getFlowHistoryInfoOfPhone(String businessId, String instanceId) {
        List<ProcessTrackVO> result = null;
        if (StringUtils.isNotEmpty(instanceId)) {
            result = this.getProcessTrackVOById(instanceId);
        } else if (StringUtils.isNotEmpty(businessId)) {
            result = this.getProcessTrackVO(businessId);
        }
        ResponseData responseData = new ResponseData();
        if (result == null || result.isEmpty()) {
            responseData.setSuccess(false);
            responseData.setMessage("历史记录为空！");
        } else {
            responseData.setSuccess(true);
            responseData.setMessage("操作成功！");
            responseData.setData(result);
        }
        return responseData;
    }


    public ApprovalHeaderVO getApprovalHeaderVo(String id) {
        FlowInstance flowInstance = flowInstanceDao.findOne(id);
        if (flowInstance == null) {
            return null;
        }
        ApprovalHeaderVO result = new ApprovalHeaderVO();
        result.setBusinessId(flowInstance.getBusinessId());
        result.setBusinessCode(flowInstance.getBusinessCode());
        result.setCreateUser(flowInstance.getCreatorName());
        result.setCreateTime(flowInstance.getCreatedDate());
        result.setWorkAndAdditionRemark(flowInstance.getBusinessModelRemark());
        //判断是否是固化流程
        if (flowInstance.getFlowDefVersion().getSolidifyFlow() == null
                || flowInstance.getFlowDefVersion().getSolidifyFlow() == false) {
            result.setSolidifyFlow(false);
        } else {
            result.setSolidifyFlow(true);
        }
        return result;
    }


    public int getMybillsSum(String userId) {
        String startDateString = "1949-10-01";
        SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd");
        String endDateString = sim.format(new Date());
        Date startDate;
        Date endDate;
        try {
            startDate = sim.parse(startDateString);
            endDate = sim.parse(endDateString);
        } catch (Exception e) {
            return 0;
        }
        Integer billSum = flowInstanceDao.getBillsSum(userId, false, startDate, endDate);
        return billSum == null ? 0 : billSum;
    }

    @Override
    public ResponseData getMyFlowCollectInfo(String userId) {
        //用户待办数（包括共同查看模式下的转授权待办）
        int todoSum = flowTaskService.getUserTodoSum(userId);
        //用户单据数（流程中）
        int billSum = this.getMybillsSum(userId);
        Map<String, Integer> map = new HashMap<>();
        map.put("todoSum", todoSum);
        map.put("billSum", billSum);
        return ResponseData.operationSuccessWithData(map);
    }
}
