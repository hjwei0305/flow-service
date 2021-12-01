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
import com.ecmp.flow.dto.CanToHistoryNode;
import com.ecmp.flow.dto.UserFlowBillsQueryParam;
import com.ecmp.flow.entity.*;
import com.ecmp.flow.util.*;
import com.ecmp.flow.vo.*;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.flow.vo.bpmn.UserTask;
import com.ecmp.flow.vo.phone.MyBillPhoneVO;
import com.ecmp.log.util.LogUtil;
import com.ecmp.util.DateUtils;
import com.ecmp.util.IdGenerator;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import com.ecmp.vo.ResponseData;
import com.ecmp.vo.SessionUser;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private FlowTaskTool flowTaskTool;

    @Autowired
    private FlowExecutorConfigDao flowExecutorConfigDao;

    @Autowired
    private FlowSolidifyExecutorService flowSolidifyExecutorService;


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

    public List<FlowHistory> findLastHisByBusinessId(String businessId) {
        //流程中如果存在未终止的实例，肯定是最后一个
        FlowInstance flowInstance = this.findLastInstanceByBusinessId(businessId);
        List<FlowHistory> flowHistoryList = new ArrayList<>();
        if (flowInstance != null) {
            flowHistoryList = flowHistoryDao.findByInstanceId(flowInstance.getId());
        }
        return flowHistoryList;
    }


    @Override
    public ResponseData<List<CanToHistoryNode>> getCanToHistoryNodeInfos(String businessId) {
        //流程中如果存在未终止的实例，肯定是最后一个
        FlowInstance flowInstance = this.findLastInstanceByBusinessId(businessId);
        if (flowInstance != null) {
            if (flowInstance.isEnded()) {
                //流程实例已结束！
                return ResponseData.operationFailure("10389");
            }
            String currentNodeKey;
            List<FlowTask> flowTaskList = flowTaskDao.findByInstanceId(flowInstance.getId());
            if (CollectionUtils.isEmpty(flowTaskList)) {
                //未获取到流程待办！
                return ResponseData.operationFailure("10390");
            } else if (flowTaskList.size() == 1) {
                currentNodeKey = flowTaskList.get(0).getActTaskDefKey();
            } else {
                FlowTask noSameTask = flowTaskList.stream().filter(task -> !flowTaskList.get(0).getActTaskDefKey().equals(task.getActTaskDefKey())).findFirst().orElse(null);
                if (noSameTask == null) {
                    currentNodeKey = flowTaskList.get(0).getActTaskDefKey();
                } else {
                    //流程存在不同的任务，可能处于并行网关中！
                    return ResponseData.operationFailure("10388");
                }
            }
            List<FlowHistory> flowHistoryList = flowHistoryDao.findByInstanceId(flowInstance.getId());
            String flowDefJson = flowInstance.getFlowDefVersion().getDefJson();
            JSONObject defObj = JSONObject.fromObject(flowDefJson);
            Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
            JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(currentNodeKey);
            List<NodeInfo> allExitNodeInfo = new ArrayList<>();
            findAllExitNodesInfo(allExitNodeInfo, definition, currentNode);
            List<CanToHistoryNode> resultList = new ArrayList<>();
            allExitNodeInfo.forEach(node -> {
                FlowHistory history = flowHistoryList.stream().filter(his -> node.getId().equalsIgnoreCase(his.getActTaskDefKey())).findFirst().orElse(null);
                if (history != null) {
                    CanToHistoryNode canToHistoryNode = new CanToHistoryNode();
                    canToHistoryNode.setId(history.getId());
                    canToHistoryNode.setFlowName(history.getFlowName());
                    canToHistoryNode.setFlowTaskName(history.getFlowTaskName());
                    canToHistoryNode.setCreatedDate(history.getCreatedDate());
                    canToHistoryNode.setDepict(history.getDepict());
                    canToHistoryNode.setExecutorId(history.getExecutorId());
                    canToHistoryNode.setExecutorName(history.getExecutorName());
                    canToHistoryNode.setExecutorAccount(history.getExecutorAccount());
                    try {
                        JSONObject jsonObject = JSONObject.fromObject(history.getTaskJsonDef());
                        JSONObject normalInfo = jsonObject.getJSONObject("nodeConfig").getJSONObject("normal");
                        String nodeCode = normalInfo.getString("poolTaskCode");
                        canToHistoryNode.setPoolTaskCode(nodeCode);
                    } catch (Exception e) {
                    }
                    resultList.add(canToHistoryNode);
                } else {
                    if ("StartUser".equalsIgnoreCase(node.getUiUserType())) { // 执行人为发起人
                        CanToHistoryNode canToHistoryNode = new CanToHistoryNode();
                        canToHistoryNode.setId(IdGenerator.uuid());
                        canToHistoryNode.setFlowName(flowInstance.getFlowName());
                        canToHistoryNode.setFlowTaskName(node.getName());
                        canToHistoryNode.setCreatedDate(new Date());
                        canToHistoryNode.setDepict("【执行人为流程发起人的节点】");
                        canToHistoryNode.setExecutorId(flowInstance.getCreatorId());
                        canToHistoryNode.setExecutorName(flowInstance.getCreatorName());
                        canToHistoryNode.setExecutorAccount(flowInstance.getCreatorAccount());
                        resultList.add(canToHistoryNode);
                    }
                }
            });
            return ResponseData.operationSuccessWithData(resultList);
        } else {
            //通过参数获取流程实例失败！
            return ResponseData.operationFailure("10249");
        }
    }


    public List<NodeInfo> findAllExitNodesInfo(List<NodeInfo> result, Definition definition, JSONObject jsonObjectNode) {
        JSONArray targetNodes = jsonObjectNode.getJSONArray("target"); //获取所有出口信息
        for (int j = 0; j < targetNodes.size(); j++) {
            JSONObject jsonObject = targetNodes.getJSONObject(j);
            String targetId = jsonObject.getString("targetId");
            JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(targetId);
            if (targetId.contains("Gateway")) {
                List<NodeInfo> resultGateway = new ArrayList<>();
                this.findAllExitNodesInfo(resultGateway, definition, currentNode);
                result.addAll(resultGateway);
            } else {
                NodeInfo newNode = new NodeInfo();
                newNode.setId(currentNode.get("id") + "");
                newNode.setName(currentNode.get("name") + "");
                try {
                    JSONArray executorList = currentNode.getJSONObject(Constants.NODE_CONFIG).getJSONArray(Constants.EXECUTOR);
                    if (executorList != null && executorList.size() == 1) {
                        JSONObject executor = executorList.getJSONObject(0);
                        String userType = (String) executor.get("userType");
                        if ("StartUser".equalsIgnoreCase(userType)) {
                            newNode.setUiUserType("StartUser");
                        }
                    }
                } catch (Exception e2) {
                }
                result.add(newNode);
            }
        }
        return result;
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
                List<FlowTask> flowTasks = flowTaskDao.findByActTaskId(historicTaskInstance.getId());
                if (!CollectionUtils.isEmpty(flowTasks)) {
                    return flowTasks.get(0);
                } else {
                    return null;
                }
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
            if (Objects.nonNull(flowTask.getFlowInstance())) {
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
        Set<String> nodeIds = new HashSet<>();
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


    @Override
    public ResponseData<List<ProcessTrackVO>> getProcessTrackVOOfMobile(String businessId) {
        try {
            List<ProcessTrackVO> result = getProcessTrackVO(businessId);
            return ResponseData.operationSuccessWithData(result);
        } catch (Exception e) {
            LogUtil.error("获取单据流程历史失败：{}", e.getMessage(), e);
            return ResponseData.operationFailure("10416", e.getMessage());
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
        List<ProcessTrackVO> result = new ArrayList<>();
        Set<FlowInstance> flowInstanceListReal = new LinkedHashSet<>();

        if (!CollectionUtils.isEmpty(flowInstanceList)) {
            flowInstanceListReal.addAll(flowInstanceList);
            for (FlowInstance flowInstance : flowInstanceList) {
                FlowInstance parent = flowInstance.getParent();
                while (parent != null) {
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

                if (!CollectionUtils.isEmpty(flowHistoryList)) {
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
        List<FlowTask> newFlowTaskList = new ArrayList<>();
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
        //计算流程历史任务是否超时
        flowHistoryService.setHistoryListIfTimeout(flowHistoryList);

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
        List<FlowTask> flowTaskList = flowTaskDao.findByInstanceIdNoVirtual(id);
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
                    //正常待办终止时，一起终止掉虚拟待办
                    if ((canCancel != null && canCancel) || TaskStatus.VIRTUAL.toString().equals(flowTask.getTaskStatus())) {
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
            return ResponseData.operationFailure("10119");
        }
        FlowInstance flowInstance = this.findLastInstanceByBusinessId(businessId);
        if (flowInstance == null) {
            return ResponseData.operationFailure("10120");
        } else if (flowInstance.isEnded() == true) {
            return ResponseData.operationFailure("10121");
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
                return ResponseData.operationFailure("10122", defObj.getString("name"));
            }
        }
        return ResponseData.operationSuccess();
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public OperateResult endByBusinessId(String businessId) {
        FlowInstance flowInstance = this.findLastInstanceByBusinessId(businessId);
        if (flowInstance == null) {
            return OperateResult.operationFailure("10123");
        }
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
                    return ResponseData.operationFailure("10124", e);
                }
                return ResponseData.operationSuccess("10125");
            }
            return ResponseData.operationFailure("10126");
        }
        return ResponseData.operationFailure("10127");
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
            List<FlowTask> oldFlowTasks = flowTaskDao.findByActTaskId(actTaskId);
            if (!CollectionUtils.isEmpty(oldFlowTasks)) {
                FlowTask oldFlowTask = oldFlowTasks.get(0);
                //是否推送信息到baisc
                Boolean pushBasic = flowTaskService.getBooleanPushTaskToBasic();
                List<FlowTask> needDelList = new ArrayList<>();  //需要删除的待办
                List<FlowTask> needAddList = new ArrayList<>(); //需要新增的待办

                String flowTypeId = oldFlowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getId();

                for (Executor executor : executorList) {
                    FlowTask newFlowTask = new FlowTask();
                    org.springframework.beans.BeanUtils.copyProperties(oldFlowTask, newFlowTask);
                    newFlowTask.setId(null);
                    //通过授权用户ID和流程类型返回转授权信息（转办模式）
                    TaskMakeOverPower taskMakeOverPower = taskMakeOverPowerService.getMakeOverPowerByTypeAndUserId(executor.getId(), flowTypeId);
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
                            LogUtil.error(e.getMessage(), e);
                        }
                    }
                    flowTaskDao.save(newFlowTask);
                    needAddList.add(newFlowTask);
                }

                needDelList.addAll(oldFlowTasks);
                flowTaskDao.deleteAll(oldFlowTasks);

                if (pushBasic) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            flowTaskService.pushToBasic(needAddList, null, needDelList, null);
                        }
                    }).start();
                }
                return ResponseData.operationSuccessWithData(needAddList);
            } else {
                return ResponseData.operationFailure("10128");
            }
        } else {
            return ResponseData.operationFailure("10129");
        }

    }

    //任务池指定真实用户，抢单池确定用户签定
    @Transactional(propagation = Propagation.REQUIRED)
    public OperateResultWithData<FlowTask> poolTaskSign(HistoricTaskInstance historicTaskInstance, String userId, Map<String, Object> v) {
        OperateResultWithData<FlowTask> result;
        String actTaskId = historicTaskInstance.getId();
        //根据用户的id获取执行人
        Executor executor = flowCommonUtil.getBasicUserExecutor(userId);
        if (executor != null) {
            List<FlowTask> oldFlowTasks = flowTaskDao.findByActTaskId(actTaskId);
            if (!CollectionUtils.isEmpty(oldFlowTasks)) {
                FlowTask oldFlowTask = oldFlowTasks.get(0);
                FlowTask newFlowTask = new FlowTask();
                org.springframework.beans.BeanUtils.copyProperties(oldFlowTask, newFlowTask);
                newFlowTask.setId(null);
                //是否推送信息到baisc
                Boolean pushBasic = flowTaskService.getBooleanPushTaskToBasic();
                List<FlowTask> needDelList = new ArrayList<>();  //需要删除的待办
                if (pushBasic) {
                    needDelList.addAll(oldFlowTasks);
                }
                //通过授权用户ID和流程类型返回转授权信息（转办模式）
                TaskMakeOverPower taskMakeOverPower = taskMakeOverPowerService.getMakeOverPowerByTypeAndUserId(executor.getId(), newFlowTask.getFlowInstance().getFlowDefVersion().getFlowDefination().getFlowType().getId());
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
                        LogUtil.error(e.getMessage(), e);
                    }
                }
                taskService.setAssignee(actTaskId, executor.getId());

                flowTaskDao.save(newFlowTask);
                flowTaskDao.deleteAll(oldFlowTasks);

                List<FlowTask> needAddList = new ArrayList<>(); //需要新增的待办
                if (pushBasic) {
                    needAddList.add(newFlowTask);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            flowTaskService.pushToBasic(needAddList, null, needDelList, null);
                        }
                    }).start();
                }
                result = OperateResultWithData.operationSuccess();
                result.setData(newFlowTask);
            } else {
                result = OperateResultWithData.operationFailure("10130");
            }
        } else {
            result = OperateResultWithData.operationFailure("10038");
        }
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseData signalPoolTaskByBusinessIdAndUserList(SignalPoolTaskVO signalPoolTaskVO) {
        if (signalPoolTaskVO == null) {
            return ResponseData.operationFailure("10140");
        }
        if (StringUtils.isEmpty(signalPoolTaskVO.getBusinessId())) {
            return ResponseData.operationFailure("10141");
        }
        if (StringUtils.isEmpty(signalPoolTaskVO.getPoolTaskActDefId())) {
            return ResponseData.operationFailure("10142");
        }
        if (signalPoolTaskVO.getUserIds() == null || signalPoolTaskVO.getUserIds().size() == 0) {
            return ResponseData.operationFailure("10143");
        }

        FlowInstance flowInstance = this.findLastInstanceByBusinessId(signalPoolTaskVO.getBusinessId());
        if (flowInstance != null && !flowInstance.isEnded()) {
            String actInstanceId = flowInstance.getActInstanceId();
            HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().processInstanceId(actInstanceId).taskDefinitionKey(signalPoolTaskVO.getPoolTaskActDefId()).unfinished().singleResult(); // 创建历史任务实例查询
            if (historicTaskInstance != null) {
                this.poolTaskSignByUserList(historicTaskInstance, signalPoolTaskVO.getUserIds(), signalPoolTaskVO.getMap());
                return ResponseData.operationSuccess();
            } else {
                return ResponseData.operationFailure("10144");
            }
        } else {
            return ResponseData.operationFailure("10145");
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public OperateResult signalPoolTaskByBusinessId(String businessId, String poolTaskActDefId, String userId, Map<String, Object> v) {
        if (StringUtils.isEmpty(poolTaskActDefId)) {
            return OperateResult.operationFailure("10146");
        }
        OperateResult result;
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
                result = OperateResult.operationFailure("10147");
            }
        } else {
            result = OperateResult.operationFailure("10148");
        }
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public OperateResultWithData<FlowTask> signalPoolTaskByBusinessIdWithResult(String businessId, String poolTaskActDefId, String userId, Map<String, Object> v) {
        if (StringUtils.isEmpty(poolTaskActDefId)) {
            return OperateResultWithData.operationFailure("10032");
        }
        OperateResultWithData<FlowTask> result;
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
        OperateResultWithData<FlowStatus> result;
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
            Map<String, FlowInstance> flowInstanceMap = new HashMap<>();
            flowInstanceMap = initAllGulianInstance(flowInstanceMap, flowInstance, force);

            if (flowInstanceMap != null && !flowInstanceMap.isEmpty()) {
                List<FlowInstance> flowInstanceList = new ArrayList<>();
                flowInstanceList.addAll(flowInstanceMap.values());//加入排序，按照创建时候倒序，保证子流程先终止
                Collections.sort(flowInstanceList, new Comparator<FlowInstance>() {
                    @Override
                    public int compare(FlowInstance flowInstance1, FlowInstance flowInstance2) {
                        return timeCompare(flowInstance1.getCreatedDate(), flowInstance2.getCreatedDate());
                    }
                });

                //是否推送信息到 baisc
                Boolean pushBasic = flowTaskService.getBooleanPushTaskToBasic();

                for (FlowInstance fTemp : flowInstanceList) {
                    if (fTemp.isEnded()) {
                        continue;
                    }
                    List<FlowTask> needDelList = new ArrayList<>();

                    Set<FlowTask> flowTaskList = fTemp.getFlowTasks();
                    if (flowTaskList != null && !flowTaskList.isEmpty()) {
                        for (FlowTask flowTask : flowTaskList) {
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
                            if (pushBasic) {//是否推送信息到baisc
                                needDelList.add(flowTask);
                            }
                            flowTaskDao.delete(flowTask);
                        }
                    }

                    String actInstanceId = fTemp.getActInstanceId();
                    String deleteReason;
                    int endSign;
                    if (force) {
                        deleteReason = "10035";//"被管理员强制终止流程";
                        endSign = 2;
                    } else {
                        deleteReason = "10036";// "被发起人终止流程";
                        endSign = 1;
                    }
                    callBeforeEndAndSon(flowInstance, endSign);

                    //保证在并行网关中终止的时候，也能获取到终止的参数
                    Map<String, Object> variables = new HashMap<>();
                    variables.put("deleteReason", deleteReason);
                    runtimeService.setVariables(actInstanceId, variables);
                    //在并行网关中终止原因传不到监听器，导致终止时要调用节点后事件
                    this.deleteActiviti(actInstanceId, deleteReason);

                    fTemp.setEndDate(new Date());
                    fTemp.setEnded(true);
                    fTemp.setManuallyEnd(true);
                    flowInstanceDao.save(fTemp);
                    //重置客户端表单流程状态
                    String businessId = fTemp.getBusinessId();
                    BusinessModel businessModel = flowInstance.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel();
                    ResponseData resetResult = ExpressionUtil.resetState(businessModel, businessId, FlowStatus.INIT);
                    if (!resetResult.getSuccess()) {
                        throw new FlowException(ContextUtil.getMessage("10360", resetResult.getMessage()));
                    }

                    //查看是否为固化流程（如果是固化流程删除固化执行人列表）
                    flowSolidifyExecutorDao.deleteByBusinessId(businessId);

                    //结束后触发
                    try {
                        this.callEndServiceAndSon(flowInstance, endSign);
                    } catch (Exception e) {
                        //轮询修改状态为：流程中
                        ExpressionUtil.pollingResetState(businessModel, businessId, FlowStatus.INPROCESS);
                        throw e;
                    }

                    if (pushBasic) {  //流程终止时，异步推送需要删除待办到baisc
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                flowTaskService.pushToBasic(null, null, needDelList, null);
                            }
                        }).start();
                    }
                }
            } else {
                if (force) {
                    result = OperateResult.operationFailure("10002");//不能终止
                } else {
                    result = OperateResult.operationFailure("10011");//不能终止
                }
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
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
        FlowOperateResult callAfterEndResult = flowListenerTool.callEndService(flowInstance.getBusinessId(), flowDefVersion, endSign, null);
        if (callAfterEndResult != null && !callAfterEndResult.isSuccess()) {
            throw new FlowException(callAfterEndResult.getMessage());
        }
    }


    /**
     * 对包含子流程在内进行终止前服务调用检查
     *
     * @param flowInstance
     * @param endSign
     */
    private void callBeforeEndAndSon(FlowInstance flowInstance, int endSign) {
        FlowDefVersion flowDefVersion = flowInstance.getFlowDefVersion();
        List<FlowInstance> flowInstanceChildren = flowInstanceDao.findByParentId(flowInstance.getId());//针对子流程
        if (flowInstanceChildren != null && !flowInstanceChildren.isEmpty()) {
            for (FlowInstance son : flowInstanceChildren) {
                callBeforeEndAndSon(son, endSign);
            }
        }
        FlowOperateResult callBeforeEndResult = flowListenerTool.callBeforeEnd(flowInstance.getBusinessId(), flowDefVersion, endSign, null);
        if (callBeforeEndResult != null && !callBeforeEndResult.isSuccess()) {
            throw new FlowException(callBeforeEndResult.getMessage());
        }
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
            return ResponseData.operationFailure("10118");
        }
    }

    @Override
    public ResponseData listAllMyBillsHeader() {
        List<TodoBusinessSummaryVO> voList = new ArrayList<>();
        String userID = ContextUtil.getUserId();
        List groupResultList = flowInstanceDao.findBillsByGroup(userID);

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
            return ResponseData.operationFailure("10134", "search");
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
                List<SearchFilter> listFilter;
                if (search.getFilters() == null) {
                    listFilter = new ArrayList<>();
                } else {
                    listFilter = search.getFilters();
                }
                listFilter.add(new SearchFilter("flowDefVersion.flowDefination.flowType.businessModel.id", modelId, SearchFilter.Operator.EQ));
                return this.getMyBills(search);
            } else {
                return ResponseData.operationFailure("10134", "search");
            }
        }
    }

    @Override
    public ResponseData getAllMyBills(UserFlowBillsQueryParam queryParam) {
        //设置流程状态
        List<SearchFilter> searchFilters = queryParam.getFilters();
        if (searchFilters == null) {
            searchFilters = new ArrayList<>();
        } else {
            //前端可能在高级查询的filter中添加该查询
            for (int i = 0; i < searchFilters.size(); i++) {
                SearchFilter filter = searchFilters.get(i);
                if ("flowStatus".equalsIgnoreCase(filter.getFieldName())) {
                    if (filter.getValue() != null) {
                        queryParam.setFlowStatus(filter.getValue().toString());
                        searchFilters.remove(i);
                    }
                }
            }
        }
        if ("inflow".equalsIgnoreCase(queryParam.getFlowStatus())) { //流程中
            SearchFilter filter1 = new SearchFilter("ended", false, SearchFilter.Operator.EQ);
            SearchFilter filter2 = new SearchFilter("manuallyEnd", false, SearchFilter.Operator.EQ);
            searchFilters.add(filter1);
            searchFilters.add(filter2);
        } else if ("ended".equalsIgnoreCase(queryParam.getFlowStatus())) { //正常结束
            SearchFilter filter1 = new SearchFilter("ended", true, SearchFilter.Operator.EQ);
            SearchFilter filter2 = new SearchFilter("manuallyEnd", false, SearchFilter.Operator.EQ);
            searchFilters.add(filter1);
            searchFilters.add(filter2);
        } else if ("abnormalEnd".equalsIgnoreCase(queryParam.getFlowStatus())) { //异常终止
            SearchFilter filter1 = new SearchFilter("ended", true, SearchFilter.Operator.EQ);
            SearchFilter filter2 = new SearchFilter("manuallyEnd", true, SearchFilter.Operator.EQ);
            searchFilters.add(filter1);
            searchFilters.add(filter2);
        }
        return this.getMyBillsByModeId(queryParam.getModelId(), queryParam);
    }

    public ResponseData getMyBills(Search search) {
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

                        //我的单据设置移动端查看单据地址
                        String phoneLookUrl = f.getFlowDefVersion().getFlowDefination().getFlowType().getPhoneLookUrl();
                        if(StringUtils.isEmpty(phoneLookUrl)){
                            phoneLookUrl = f.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel().getPhoneLookUrl();
                        }
                        myBillVO.setPhoneUrl(StringUtils.isEmpty(phoneLookUrl) ? "NotConfig" : phoneLookUrl);
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
                return ResponseData.operationSuccessWithData(results);
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
                return ResponseData.operationFailure(e.getMessage());
            }
        } else {
            return ResponseData.operationFailure("10134", "search");
        }
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
                myBillVO.setFlowInstanceCreatorName(f.getCreatorName());
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
        if (result == null || result.isEmpty()) {
            return ResponseData.operationFailure("10133");
        } else {
            return ResponseData.operationSuccessWithData(result);
        }
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


    @Override
    public ResponseData updateRemarkByBusinessId(UpdateInstanceRemarkVo updateInstanceRemarkVo) {
        if (Objects.isNull(updateInstanceRemarkVo)) {
            return ResponseData.operationFailure("10006");
        }
        if (StringUtils.isEmpty(updateInstanceRemarkVo.getBusinessId())) {
            return ResponseData.operationFailure("10119");
        }
        if (StringUtils.isEmpty(updateInstanceRemarkVo.getUpdateRemark())) {
            return ResponseData.operationFailure("10135");
        }
        if (BooleanUtils.isNotTrue(updateInstanceRemarkVo.getCoverAdditionalRemark())) {
            updateInstanceRemarkVo.setCoverAdditionalRemark(false);
        }

        FlowInstance flowInstance = this.findLastInstanceByBusinessId(updateInstanceRemarkVo.getBusinessId());
        if (flowInstance != null && !flowInstance.isEnded()) { //只考虑还在流程中的流程实例
            if (updateInstanceRemarkVo.getCoverAdditionalRemark()) {
                flowInstance.setBusinessModelRemark(updateInstanceRemarkVo.getUpdateRemark());
            } else {
                String newRemark;
                String oldRemark = flowInstance.getBusinessModelRemark();
                int i = oldRemark.lastIndexOf("【附加说明");
                if (i != -1) { //有附加说明
                    newRemark = updateInstanceRemarkVo.getUpdateRemark() + oldRemark.substring(i);
                } else {
                    newRemark = updateInstanceRemarkVo.getUpdateRemark();
                }
                if (StringUtils.isNotEmpty(newRemark) && newRemark.length() > 2000) {
                    return ResponseData.operationFailure("10136", newRemark.length());
                }
                flowInstance.setBusinessModelRemark(newRemark);
            }
            this.save(flowInstance);
            return ResponseData.operationSuccess("10137");
        }
        return ResponseData.operationFailure("10138");
    }


    @Override
    public ResponseData updateRemarkByInstaceId(UpdateRemarkByInstanceVo updateRemarkByInstanceVo) {
        if (Objects.isNull(updateRemarkByInstanceVo)) {
            return ResponseData.operationFailure("10006");
        }
        if (StringUtils.isEmpty(updateRemarkByInstanceVo.getInstanceId())) {
            return ResponseData.operationFailure("10139");
        }
        if (StringUtils.isEmpty(updateRemarkByInstanceVo.getUpdateRemark())) {
            return ResponseData.operationFailure("10135");
        }
        FlowInstance flowInstance = flowInstanceDao.findOne(updateRemarkByInstanceVo.getInstanceId());
        if (flowInstance != null) {
            flowInstance.setBusinessModelRemark(updateRemarkByInstanceVo.getUpdateRemark());
            this.save(flowInstance);
            return ResponseData.operationSuccess("10137");
        }
        return ResponseData.operationFailure("10131");

    }

    /**
     * 待办生产失败的补偿接口
     *
     * @param instanceId
     * @return
     */
    @Override
    public ResponseData taskFailTheCompensation(String instanceId) {

        Boolean setValue = redisTemplate.opsForValue().setIfAbsent("taskCompensation_" + instanceId, instanceId);
        if (!setValue) {
            Long remainingTime = redisTemplate.getExpire("taskCompensation_" + instanceId, TimeUnit.SECONDS);
            if (remainingTime == -1) {  //说明时间未设置进去
                redisTemplate.expire("taskCompensation_" + instanceId, 10 * 60, TimeUnit.SECONDS);
                remainingTime = 600L;
            }
            throw new FlowException(ContextUtil.getMessage("10132", remainingTime));
        }

        try {

            redisTemplate.expire("taskCompensation_" + instanceId, 10 * 60, TimeUnit.SECONDS);

            if (StringUtils.isEmpty(instanceId)) {
                return ResponseData.operationFailure("10149");
            }
            FlowInstance flowInstance = flowInstanceDao.findOne(instanceId);
            if (flowInstance == null) {
                return ResponseData.operationFailure("10150");
            }
            if (flowInstance.isEnded()) {
                return ResponseData.operationFailure("10151");
            }
            List<FlowTask> flowTaskList = flowTaskDao.findByInstanceIdNoVirtual(instanceId);
            if (!CollectionUtils.isEmpty(flowTaskList)) {
                for (FlowTask flowTask : flowTaskList) {
                    if (flowTask.getTrustState() == null || flowTask.getTrustState() != 1) {
                        return ResponseData.operationFailure("10152");
                    }
                }
            }
            List<FlowHistory> flowHistoryList = flowHistoryDao.findByInstanceIdNoVirtual(instanceId);
            Map<String, Object> variables = runtimeService.getVariables(flowInstance.getActInstanceId());
            FlowHistory preTask = null;
            if (!CollectionUtils.isEmpty(flowHistoryList)) {
                preTask = flowHistoryList.get(flowHistoryList.size() - 1);
            }
            //重新生产待办任务
            flowTaskTool.initTask(flowInstance, preTask, null, variables);
            return ResponseData.operationSuccess("10153");
        } catch (Exception e) {
            LogUtil.error("补偿失败：程序报错--" + e.getMessage(), e);
            return ResponseData.operationFailure("10154");
        } finally {
            //启动的时候设置的检查参数
            redisTemplate.delete("taskCompensation_" + instanceId);
        }
    }


    @Override
    public ResponseData<List<FlowNodeVO>> checkAndGetCanJumpNodeInfos(String instanceId) {
        if (StringUtils.isNotEmpty(instanceId)) {
            FlowInstance flowInstance = flowInstanceDao.findOne(instanceId);
            if (flowInstance != null) {
                if (!flowInstance.isEnded()) {
                    ResponseData responseData = this.checkCurrentFlowWhetherCanJump(flowInstance);
                    if (responseData.successful()) {
                        String currentNodeId = (String) responseData.getData();
                        FlowDefVersion flowDefVersion = flowInstance.getFlowDefVersion();
                        String flowDefJson = flowDefVersion.getDefJson();
                        JSONObject defObj = JSONObject.fromObject(flowDefJson);
                        Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
                        net.sf.json.JSONObject nodeObj = definition.getProcess().getNodes();
                        List<FlowNodeVO> canJumpNodeList = new ArrayList<>();
                        nodeObj.keySet().forEach(key -> {
                            JSONObject currentObj = JSONObject.fromObject(nodeObj.get(key));
                            String nodeType = (String) currentObj.get("nodeType");
                            if ("Normal".equalsIgnoreCase(nodeType)
                                    || "SingleSign".equalsIgnoreCase(nodeType)
                                    || "CounterSign".equalsIgnoreCase(nodeType)
                                    || "Approve".equalsIgnoreCase(nodeType)
                                    || "SerialTask".equalsIgnoreCase(nodeType) //串行任务
                                    || "ParallelTask".equalsIgnoreCase(nodeType) //并行任务
                                    || "PoolTask".equalsIgnoreCase(nodeType)
                                    || "ServiceTask".equalsIgnoreCase(nodeType) //服务任务
                                    || "ReceiveTask".equalsIgnoreCase(nodeType) //接收任务
                            ) {
                                String id = (String) currentObj.get("id");
                                if (!currentNodeId.equals(id)) {
                                    String name = (String) currentObj.get("name");
                                    canJumpNodeList.add(new FlowNodeVO(id, name, nodeType));
                                }
                            }
                        });
                        return ResponseData.operationSuccessWithData(canJumpNodeList);
                    } else {
                        return ResponseData.operationFailure(responseData.getMessage());
                    }
                } else {
                    return ResponseData.operationFailure("10155");
                }
            } else {
                return ResponseData.operationFailure("10156");
            }
        } else {
            return ResponseData.operationFailure("10157");
        }
    }


    /**
     * 检查当前流程是否支持跳转
     *
     * @param flowInstance
     * @return
     */
    public ResponseData checkCurrentFlowWhetherCanJump(FlowInstance flowInstance) {
        List<FlowTask> flowTaskList = flowTaskDao.findByInstanceIdNoVirtual(flowInstance.getId());
        if (!CollectionUtils.isEmpty(flowTaskList)) {
            if (flowTaskList.size() == 1) {
                FlowTask flowTask = flowTaskList.get(0);
                String defJson = flowTask.getTaskJsonDef();
                JSONObject defObj = JSONObject.fromObject(defJson);
                String nodeType = (String) defObj.get("nodeType");
                if ("Normal".equalsIgnoreCase(nodeType) || "SingleSign".equalsIgnoreCase(nodeType)
                        || "Approve".equalsIgnoreCase(nodeType) || "ParallelTask".equalsIgnoreCase(nodeType)
                        || "PoolTask".equalsIgnoreCase(nodeType)) {
                    //（普通、单签、审批、并行、工作池）任务（只有或最后一个执行人）都可以直接跳过
                    return ResponseData.operationSuccessWithData(flowTask.getActTaskDefKey());
                } else if ("CounterSign".equalsIgnoreCase(nodeType) || "SerialTask".equalsIgnoreCase(nodeType)) {
                    //（会签、串行）任务
                    if ("CounterSign".equalsIgnoreCase(nodeType)) {
                        //会签执行策略（true为串行 false为并行）
                        boolean isSequential = defObj.getJSONObject("nodeConfig").getJSONObject("normal").getBoolean("isSequential");
                        if (!isSequential) { //并行会签
                            return ResponseData.operationSuccessWithData(flowTask.getActTaskDefKey());
                        }
                    }
                    //如果是串行会签或者串行任务、需要判断是否为最后一个执行人
                    HistoricTaskInstance currTask = historyService
                            .createHistoricTaskInstanceQuery().taskId(flowTask.getActTaskId()).singleResult();
                    String executionId = currTask.getExecutionId();
                    Map<String, VariableInstance> processVariables = runtimeService.getVariableInstances(executionId);
                    //总循环次数
                    Integer instanceOfNumbers = (Integer) processVariables.get("nrOfInstances").getValue();
                    //完成的次数
                    Integer completeCounter = (Integer) processVariables.get("nrOfCompletedInstances").getValue();
                    //串行最后一个执行人
                    if (completeCounter + 1 == instanceOfNumbers) {
                        return ResponseData.operationSuccessWithData(flowTask.getActTaskDefKey());
                    } else {
                        return ResponseData.operationFailure("10158");
                    }
                } else {
                    return ResponseData.operationFailure("10159");
                }
            } else {
                FlowTask noSameTask = flowTaskList.stream().filter(task -> !flowTaskList.get(0).getActTaskDefKey().equals(task.getActTaskDefKey())).findFirst().orElse(null);
                if (noSameTask == null) {
                    FlowTask flowTask = flowTaskList.get(0);
                    String defJson = flowTask.getTaskJsonDef();
                    JSONObject defObj = JSONObject.fromObject(defJson);
                    String nodeType = (String) defObj.get("nodeType");
                    if ("SingleSign".equalsIgnoreCase(nodeType) || "PoolTask".equalsIgnoreCase(nodeType)) {
                        return ResponseData.operationSuccessWithData(flowTask.getActTaskDefKey());
                    } else if ("CounterSign".equalsIgnoreCase(nodeType) || "ParallelTask".equalsIgnoreCase(nodeType)) {
                        //会签并行CounterSign或者并行任务ParallelTask
                        return ResponseData.operationFailure("10160");
                    } else {
                        return ResponseData.operationFailure("10161");
                    }
                } else {
                    return ResponseData.operationFailure("10162");
                }
            }
        } else {
            return ResponseData.operationFailure("10163");
        }
    }


    @Override
    public ResponseData<TargetNodeInfoVo> getTargetNodeInfo(String instanceId, String targetNodeId) {
        if (StringUtils.isNotEmpty(instanceId)) {
            FlowInstance flowInstance = flowInstanceDao.findOne(instanceId);
            if (flowInstance != null) {
                if (!flowInstance.isEnded()) {
                    List<FlowTask> flowTaskList = flowTaskDao.findByInstanceIdNoVirtual(flowInstance.getId());
                    if (!CollectionUtils.isEmpty(flowTaskList)) {
                        FlowTask flowTask = flowTaskList.get(0);
                        //得到目标节点信息
                        TargetNodeInfoVo targetNodeInfoVo;
                        try {
                            targetNodeInfoVo = this.findTargetNodeInfo(flowInstance, flowTask, targetNodeId);
                        } catch (Exception e) {
                            LogUtil.error("获取目标节点信息错误，详情请查看日志！", e);
                            return ResponseData.operationFailure("10164");
                        }
                        return ResponseData.operationSuccessWithData(targetNodeInfoVo);
                    } else {
                        return ResponseData.operationFailure("10165");
                    }
                } else {
                    return ResponseData.operationFailure("10166");
                }
            } else {
                return ResponseData.operationFailure("10167");
            }
        } else {
            return ResponseData.operationFailure("10168");
        }
    }


    public TargetNodeInfoVo findTargetNodeInfo(FlowInstance flowInstance, FlowTask flowTask, String targetNodeId) {
        //得到目标节点基础信息
        NodeInfo nodeInfo = flowTaskTool.getNodeInfoByTarget(flowTask, targetNodeId, flowInstance.getFlowDefVersion());

        String flowDefJson = flowInstance.getFlowDefVersion().getDefJson();
        JSONObject defObj = JSONObject.fromObject(flowDefJson);
        Definition definition = (Definition) JSONObject.toBean(defObj, Definition.class);
        //是否是固化流程
        boolean solidifyFlow = BooleanUtils.isTrue(flowInstance.getFlowDefVersion().getSolidifyFlow());

        String flowTaskDefJson = flowTask.getTaskJsonDef();
        JSONObject flowTaskDefObj = JSONObject.fromObject(flowTaskDefJson);
        String currentNodeType = flowTaskDefObj.get("nodeType") + "";
        JSONObject normalInfo = flowTaskDefObj.getJSONObject("nodeConfig").getJSONObject("normal");
        Boolean currentSingleTaskAuto = false;
        if (normalInfo != null && normalInfo.has("singleTaskNoChoose") && normalInfo.get("singleTaskNoChoose") != null) {
            currentSingleTaskAuto = normalInfo.getBoolean("singleTaskNoChoose");
        }

        nodeInfo.setCurrentTaskType(currentNodeType);
        nodeInfo.setCurrentSingleTaskAuto(currentSingleTaskAuto);

        if ("serviceTask".equalsIgnoreCase(nodeInfo.getType())) {
            nodeInfo.setUserVarName(nodeInfo.getId() + "_ServiceTask");
            nodeInfo.setUiType("radiobox");
            nodeInfo.setFlowTaskType("serviceTask");
            String userId = ContextUtil.getSessionUser().getUserId();
            Executor executor = flowCommonUtil.getBasicUserExecutor(userId);
            if (executor != null) {//服务任务封装执行人信息（ID为操作人）
                executor.setName("系统自动");
                executor.setCode(Constants.ADMIN);
                executor.setOrganizationName("系统自动执行的任务");
                Set<Executor> employeeSet = new HashSet<>();
                employeeSet.add(executor);
                nodeInfo.setExecutorSet(employeeSet);
            }
        } else if ("receiveTask".equalsIgnoreCase(nodeInfo.getType())) {
            nodeInfo.setUserVarName(nodeInfo.getId() + "_ReceiveTask");
            nodeInfo.setUiType("radiobox");
            nodeInfo.setFlowTaskType("receiveTask");
            String userId = ContextUtil.getSessionUser().getUserId();
            Executor executor = flowCommonUtil.getBasicUserExecutor(userId);
            if (executor != null) {//接收任务封装执行人信息（ID为操作人）
                executor.setName("系统触发");
                executor.setCode(Constants.ADMIN);
                executor.setOrganizationName("等待系统触发后执行");
                Set<Executor> employeeSet = new HashSet<>();
                employeeSet.add(executor);
                nodeInfo.setExecutorSet(employeeSet);
            }
        } else {
            JSONObject currentNode = definition.getProcess().getNodes().getJSONObject(nodeInfo.getId());

            try {
                JSONObject normal = currentNode.getJSONObject(Constants.NODE_CONFIG).getJSONObject("normal");
                Boolean allowChooseInstancy = normal.getBoolean("allowChooseInstancy");
                nodeInfo.setAllowChooseInstancy(allowChooseInstancy);
            } catch (Exception e) {
            }

            JSONObject executor = null;
            net.sf.json.JSONArray executorList = null;//针对两个条件以上的情况
            if (currentNode.getJSONObject(Constants.NODE_CONFIG).has(Constants.EXECUTOR)) {
                try {
                    executor = currentNode.getJSONObject(Constants.NODE_CONFIG).getJSONObject(Constants.EXECUTOR);
                } catch (Exception e) {
                    try {
                        executorList = currentNode.getJSONObject(Constants.NODE_CONFIG).getJSONArray(Constants.EXECUTOR);
                    } catch (Exception e2) {
                    }
                    if (executorList != null && executorList.size() == 1) {
                        executor = executorList.getJSONObject(0);
                    }
                }
            }

            UserTask userTaskTemp = (UserTask) JSONObject.toBean(currentNode, UserTask.class);
            if (StringUtils.isEmpty(nodeInfo.getUserVarName())) {
                if ("Normal".equalsIgnoreCase(userTaskTemp.getNodeType())) {
                    nodeInfo.setUserVarName(userTaskTemp.getId() + "_Normal");
                } else if ("SingleSign".equalsIgnoreCase(userTaskTemp.getNodeType())) {
                    nodeInfo.setUserVarName(userTaskTemp.getId() + "_SingleSign");
                    nodeInfo.setUiType("checkbox");
                } else if ("Approve".equalsIgnoreCase(userTaskTemp.getNodeType())) {
                    nodeInfo.setUserVarName(userTaskTemp.getId() + "_Approve");
                } else if ("CounterSign".equalsIgnoreCase(userTaskTemp.getNodeType()) || "ParallelTask".equalsIgnoreCase(userTaskTemp.getNodeType()) || "SerialTask".equalsIgnoreCase(userTaskTemp.getNodeType())) {
                    nodeInfo.setUserVarName(userTaskTemp.getId() + "_List_CounterSign");
                    nodeInfo.setUiType("checkbox");
                }
            }


            if (!solidifyFlow) {
                if (!CollectionUtils.isEmpty(executor)) {
                    String userType = (String) executor.get("userType");
                    String ids = (String) executor.get("ids");
                    List<Executor> employees = null;
                    nodeInfo.setUiUserType(userType);
                    if ("StartUser".equalsIgnoreCase(userType)) {//获取流程实例启动者
                        while (flowInstance.getParent() != null) { //以父流程的启动人为准
                            flowInstance = flowInstance.getParent();
                        }
                        String startUserId;
                        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(flowInstance.getActInstanceId()).singleResult();
                        if (historicProcessInstance == null) {//当第一个任务为服务任务的时候存在为空的情况发生
                            startUserId = ContextUtil.getUserId();
                        } else {
                            startUserId = historicProcessInstance.getStartUserId();
                        }
                        //根据用户的id列表获取执行人
                        employees = flowCommonUtil.getBasicUserExecutors(Arrays.asList(startUserId));
                    } else {
                        String selfDefId = (String) executor.get("selfDefId");
                        if (StringUtils.isNotEmpty(ids) || StringUtils.isNotEmpty(selfDefId)) {
                            if ("SelfDefinition".equalsIgnoreCase(userType)) {//通过业务ID获取自定义用户
                                FlowExecutorConfig flowExecutorConfig = flowExecutorConfigDao.findOne(selfDefId);
                                String path = flowExecutorConfig.getUrl();
                                AppModule appModule = flowExecutorConfig.getBusinessModel().getAppModule();
                                String appModuleCode = appModule.getApiBaseAddress();
                                String businessId = flowTask.getFlowInstance().getBusinessId();
                                String param = flowExecutorConfig.getParam();
                                FlowInvokeParams flowInvokeParams = new FlowInvokeParams();
                                flowInvokeParams.setId(businessId);
                                flowInvokeParams.setJsonParam(param);
                                String nodeCode;
                                try {
                                    JSONObject normal = currentNode.getJSONObject(Constants.NODE_CONFIG).getJSONObject("normal");
                                    nodeCode = normal.getString("nodeCode");
                                    if (StringUtils.isNotEmpty(nodeCode)) {
                                        Map<String, String> map = new HashMap<>();
                                        map.put("nodeCode", nodeCode);
                                        flowInvokeParams.setParams(map);
                                    }
                                } catch (Exception e) {
                                }
                                employees = flowCommonUtil.getExecutorsBySelfDef(appModuleCode, flowExecutorConfig.getName(), path, flowInvokeParams);
                            } else {
                                //岗位或者岗位类型（Position、PositionType、AnyOne）、组织机构都改为单据的组织机构
                                String currentOrgId = flowTaskService.getOrgIdByFlowTask(flowTask);
                                employees = flowTaskTool.getExecutors(userType, ids, currentOrgId);
                            }
                        }
                    }
                    if (employees != null && !employees.isEmpty()) {
                        Set<Executor> employeeSet = new HashSet<>(employees);
                        nodeInfo.setExecutorSet(employeeSet);
                    }
                } else if (executorList != null && executorList.size() > 1) {
                    List<Executor> employees;
                    String selfDefId = null;
                    List<String> orgDimensionCodes = null;//组织维度代码集合
                    List<String> positionIds = null;//岗位代码集合
                    List<String> orgIds = null; //组织机构id集合
                    List<String> positionTypesIds = null;//岗位类别id集合
                    for (Object executorObject : executorList.toArray()) {
                        JSONObject executorTemp = (JSONObject) executorObject;
                        String userType = executorTemp.get("userType") + "";
                        String ids = executorTemp.get("ids") + "";
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
                    // 取得当前任务
                    String currentOrgId = flowTaskService.getOrgIdByFlowTask(flowTask);
                    if (StringUtils.isNotEmpty(selfDefId) && !Constants.NULL_S.equalsIgnoreCase(selfDefId)) {
                        FlowExecutorConfig flowExecutorConfig = flowExecutorConfigDao.findOne(selfDefId);
                        String path = flowExecutorConfig.getUrl();
                        AppModule appModule = flowExecutorConfig.getBusinessModel().getAppModule();
                        String appModuleCode = appModule.getApiBaseAddress();
                        String param = flowExecutorConfig.getParam();
                        FlowInvokeParams flowInvokeParams = new FlowInvokeParams();
                        flowInvokeParams.setId(flowTask.getFlowInstance().getBusinessId());

                        flowInvokeParams.setOrgId(currentOrgId);
                        flowInvokeParams.setPositionIds(positionIds);
                        flowInvokeParams.setPositionTypeIds(positionTypesIds);
                        flowInvokeParams.setOrganizationIds(orgIds);
                        flowInvokeParams.setOrgDimensionCodes(orgDimensionCodes);

                        flowInvokeParams.setJsonParam(param);
                        try {
                            JSONObject normal = currentNode.getJSONObject(Constants.NODE_CONFIG).getJSONObject("normal");
                            String nodeCode = normal.getString("nodeCode");
                            if (StringUtils.isNotEmpty(nodeCode)) {
                                Map<String, String> map = new HashMap<>();
                                map.put("nodeCode", nodeCode);
                                flowInvokeParams.setParams(map);
                            }
                        } catch (Exception e) {
                        }
                        employees = flowCommonUtil.getExecutorsBySelfDef(appModuleCode, flowExecutorConfig.getName(), path, flowInvokeParams);

                    } else {
                        if (positionTypesIds != null && orgIds != null) {
                            //新增根据（岗位类别+组织机构）获得执行人
                            employees = flowCommonUtil.getExecutorsByPostCatIdsAndOrgs(positionTypesIds, orgIds);
                        } else {
                            //通过岗位ids、组织维度ids和组织机构id来获取执行人
                            employees = flowCommonUtil.getExecutorsByPositionIdsAndorgDimIds(positionIds, orgDimensionCodes, currentOrgId);
                        }
                    }
                    if (employees != null && !employees.isEmpty()) {
                        Set<Executor> employeeSet = new HashSet<>(employees);
                        nodeInfo.setExecutorSet(employeeSet);
                    }
                }
            } else {
                if (!CollectionUtils.isEmpty(executor)) {
                    String userType = (String) executor.get("userType");
                    nodeInfo.setUiUserType(userType);


                    List<NodeInfo> nodeInfoList = new ArrayList<>();
                    nodeInfoList.add(nodeInfo);
                    //设置固化执行人信息(只是前台展示使用)
                    nodeInfoList = flowSolidifyExecutorService.
                            setNodeExecutorByBusinessId(nodeInfoList, flowTask.getFlowInstance().getBusinessId());
                    nodeInfo = nodeInfoList.get(0);
                }
            }
        }
        return new TargetNodeInfoVo(solidifyFlow, nodeInfo);
    }


    @Override
    public ResponseData jumpToTargetNode(JumpTaskVo jumpTaskVo) {
        String businessId;
        String approved = null;
        String instanceId = jumpTaskVo.getInstanceId();
        FlowTask currentTask;
        if (StringUtils.isNotEmpty(instanceId)) {
            FlowInstance flowInstance = flowInstanceDao.findOne(instanceId);
            if (flowInstance != null) {
                List<FlowTask> flowTaskList = flowTaskDao.findByInstanceIdNoVirtual(flowInstance.getId());
                if (!CollectionUtils.isEmpty(flowTaskList)) {
                    currentTask = flowTaskList.get(0);
                    businessId = flowInstance.getBusinessId();
                    String defJson = flowTaskList.get(0).getTaskJsonDef();
                    JSONObject defObj = JSONObject.fromObject(defJson);
                    String nodeType = (String) defObj.get("nodeType");
                    if ("CounterSign".equalsIgnoreCase(nodeType) || "Approve".equalsIgnoreCase(nodeType)) {
                        approved = "true";
                    }
                } else {
                    return ResponseData.operationFailure("10169");
                }
            } else {
                return ResponseData.operationFailure("10170");
            }
        } else {
            return ResponseData.operationFailure("10171");
        }

        //获取节点跳转的提交参数
        ResponseData responseData = this.getJumpCompleteProperties(currentTask, jumpTaskVo, businessId, approved);
        if (responseData.successful()) {
            Map<String, Object> properties = (Map<String, Object>) responseData.getData();
            try {
                StringBuilder jumpString = new StringBuilder("【");
                if (jumpTaskVo.isTargetNodeBeforeEvent()) {
                    jumpString.append("-");
                }
                jumpString.append("节点跳转");
                if (jumpTaskVo.isCurrentNodeAfterEvent()) {
                    jumpString.append("-");
                }
                jumpString.append("】");
                String opinion = jumpString.toString() + jumpTaskVo.getJumpDepict();
                return flowTaskService.jumpToTarget(currentTask, jumpTaskVo.getTargetNodeId(), properties, opinion);
            } catch (Exception e) {
                LogUtil.error("跳转失败:" + e.getMessage(), e);
                return ResponseData.operationFailure("10172", e.getMessage());
            }
        } else {
            return responseData;
        }
    }

    public ResponseData getJumpCompleteProperties(FlowTask currentTask, JumpTaskVo jumpTaskVo, String businessId, String approved) {
        String taskList = jumpTaskVo.getTaskList();
        Long loadOverTime = null;
        Boolean mobileApprove = false;

        //completeTask获取参数逻辑，保持一致（暂时先不合并获取参数部分）
        List<FlowTaskCompleteWebVO> flowTaskCompleteList = null;
        if (StringUtils.isNotEmpty(taskList)) {
            JSONArray jsonArray = JSONArray.fromObject(taskList);//把String转换为json
            flowTaskCompleteList = (List<FlowTaskCompleteWebVO>) JSONArray.toCollection(jsonArray, FlowTaskCompleteWebVO.class);
        }
        Map<String, Object> v = new HashMap<>();
        if (flowTaskCompleteList != null && !flowTaskCompleteList.isEmpty()) {
            //如果是固化流程的提交，设置参数里面的紧急状态和执行人列表
            FlowTaskCompleteWebVO firstBean = flowTaskCompleteList.get(0);
            if (firstBean.getSolidifyFlow() != null && firstBean.getSolidifyFlow() && StringUtils.isEmpty(firstBean.getUserIds())) {
                ResponseData solidifyData = flowSolidifyExecutorService.setInstancyAndIdsByTaskList(flowTaskCompleteList, businessId);
                if (solidifyData.getSuccess() == false) {
                    return solidifyData;
                }
                flowTaskCompleteList = (List<FlowTaskCompleteWebVO>) solidifyData.getData();
                JSONArray jsonArray2 = JSONArray.fromObject(flowTaskCompleteList.toArray());
                flowTaskCompleteList = (List<FlowTaskCompleteWebVO>) JSONArray.toCollection(jsonArray2, FlowTaskCompleteWebVO.class);
                v.put("manageSolidifyFlow", true); //需要维护固化表
            } else {
                v.put("manageSolidifyFlow", false);
            }

            Map<String, Boolean> allowChooseInstancyMap = new HashMap<>();//选择任务的紧急处理状态
            Map<String, List<String>> selectedNodesUserMap = new HashMap<>();//选择的用户信息
            for (FlowTaskCompleteWebVO f : flowTaskCompleteList) {
                allowChooseInstancyMap.put(f.getNodeId(), f.getInstancyStatus());
                String flowTaskType = f.getFlowTaskType();
                String callActivityPath = f.getCallActivityPath();
                List<String> userList = new ArrayList<>();
                if (StringUtils.isNotEmpty(callActivityPath)) {
                    List<String> userVarNameList = (List) v.get(callActivityPath + "_sonProcessSelectNodeUserV");
                    if (userVarNameList != null) {
                        userVarNameList.add(f.getUserVarName());
                    } else {
                        userVarNameList = new ArrayList<>();
                        userVarNameList.add(f.getUserVarName());
                        v.put(callActivityPath + "_sonProcessSelectNodeUserV", userVarNameList);//选择的变量名,子流程存在选择了多个的情况
                    }
                    String userIds = f.getUserIds() == null ? "" : f.getUserIds();
                    if ("common".equalsIgnoreCase(flowTaskType) || "approve".equalsIgnoreCase(flowTaskType)) {
                        v.put(callActivityPath + "/" + f.getUserVarName(), userIds);
                    } else {
                        String[] idArray = userIds.split(",");
                        v.put(callActivityPath + "/" + f.getUserVarName(), Arrays.asList(idArray));
                    }
                    //注意：针对子流程选择的用户信息-待后续进行扩展--------------------------
                } else {
                    //如果不是工作池任务，又没有选择用户的，提示错误
                    if (!"poolTask".equalsIgnoreCase(flowTaskType) && (StringUtils.isEmpty(f.getUserIds()) || "null".equalsIgnoreCase(f.getUserIds()))) {
                        return ResponseData.operationFailure("10076");
                    }

                    if (f.getUserIds() == null) {
                        selectedNodesUserMap.put(f.getNodeId(), new ArrayList<>());
                    } else {
                        String userIds = f.getUserIds();
                        String[] idArray = userIds.split(",");
                        userList = Arrays.asList(idArray);
                        if ("common".equalsIgnoreCase(flowTaskType) || "approve".equalsIgnoreCase(flowTaskType)) {
                            v.put(f.getUserVarName(), userIds);
                        } else if (!"poolTask".equalsIgnoreCase(flowTaskType)) {
                            v.put(f.getUserVarName(), userList);
                        }
                    }
                    selectedNodesUserMap.put(f.getNodeId(), userList);
                }
            }
            v.put("allowChooseInstancyMap", allowChooseInstancyMap);
            v.put("selectedNodesUserMap", selectedNodesUserMap);
        } else {
            Map<String, Boolean> allowChooseInstancyMap = new HashMap<>();//选择任务的紧急处理状态
            Map<String, List<String>> selectedNodesUserMap = new HashMap<>();//选择的用户信息
            v.put("selectedNodesUserMap", selectedNodesUserMap);
            v.put("allowChooseInstancyMap", allowChooseInstancyMap);
            v.put("manageSolidifyFlow", false); //会签未完成和结束节点不需要维护固化流程执行人列表
        }
        if (loadOverTime != null) {
            v.put("loadOverTime", loadOverTime);
        }
        v.put("mobileApprove", BooleanUtils.isTrue(mobileApprove));

        v.put("approved", approved);//针对会签时同意、不同意、弃权等操作


        v.put(currentTask.getActTaskDefKey() + "currentNodeAfterEvent", jumpTaskVo.isCurrentNodeAfterEvent());
        v.put(jumpTaskVo.getTargetNodeId() + "targetNodeBeforeEvent", jumpTaskVo.isTargetNodeBeforeEvent());

        return ResponseData.operationSuccessWithData(v);
    }


    @Override
    public ResponseData getSolidifyFlowInstanceByUserId(String userId) {
        if (StringUtils.isEmpty(userId)) {
            return ResponseData.operationFailure("10405");
        }
        //查询执行人列表中有该用户的固化数据
        List<FlowSolidifyExecutor> list = flowSolidifyExecutorService.getFlowSolidifyExecutorInfoByUserId(userId);
        if (CollectionUtils.isEmpty(list)) {
            return ResponseData.operationSuccessWithData(new ArrayList<FlowInstance>());
        }
        //业务单据ID去重
        Set<String> businessIdSet = new HashSet<>();
        list.forEach(a -> {
            businessIdSet.add(a.getBusinessId());
        });
        List<String> businessIdList = new ArrayList<>();
        businessIdList.addAll(businessIdSet);
        List<FlowInstance> flowInstanceList = flowInstanceDao.findByBusinessIdListNoEndAndSolidify(businessIdList);
        return ResponseData.operationSuccessWithData(flowInstanceList);
    }


    @Override
    public ResponseData<ApprovalHeaderVO> getHislHeaderVoOfGateway(String instanceId) {
        FlowInstance flowInstance = findOne(instanceId);
        ApprovalHeaderVO approvalHeaderVO = new ApprovalHeaderVO();
        approvalHeaderVO.setFlowName(flowInstance.getFlowName());
        approvalHeaderVO.setCreateTime(flowInstance.getCreatedDate());
        approvalHeaderVO.setCreateUser(flowInstance.getCreatorName());
        approvalHeaderVO.setBusinessId(flowInstance.getBusinessId());
        approvalHeaderVO.setWorkAndAdditionRemark(flowInstance.getBusinessModelRemark());
        BusinessModel businessModel = flowInstance.getFlowDefVersion().getFlowDefination().getFlowType().getBusinessModel();
        if (businessModel != null) {
            approvalHeaderVO.setBusinessModelName(businessModel.getName());
            //已办和我的单据抬头信息设置移动端查看单据地址
            String phoneLookUrl = flowInstance.getFlowDefVersion().getFlowDefination().getFlowType().getPhoneLookUrl();
            if(StringUtils.isEmpty(phoneLookUrl)){
                phoneLookUrl = businessModel.getPhoneLookUrl();
            }
            approvalHeaderVO.setPhoneUrl(StringUtils.isEmpty(phoneLookUrl) ? "NotConfig" : phoneLookUrl);
        }
        return ResponseData.operationSuccessWithData(approvalHeaderVO);
    }
}
