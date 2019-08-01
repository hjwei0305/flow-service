package com.ecmp.flow.service;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchFilter;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.flow.api.IFlowSolidifyExecutorService;
import com.ecmp.flow.basic.vo.Executor;
import com.ecmp.flow.dao.FlowSolidifyExecutorDao;
import com.ecmp.flow.entity.FlowInstance;
import com.ecmp.flow.entity.FlowSolidifyExecutor;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.flow.util.FlowCommonUtil;
import com.ecmp.flow.vo.*;
import com.ecmp.flow.vo.bpmn.Definition;
import com.ecmp.log.util.LogUtil;
import com.ecmp.vo.ResponseData;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class FlowSolidifyExecutorService extends BaseEntityService<FlowSolidifyExecutor> implements IFlowSolidifyExecutorService {

    @Autowired
    private FlowSolidifyExecutorDao flowSolidifyExecutorDao;

    @Autowired
    private FlowTaskService flowTaskService;

    @Autowired
    private DefaultFlowBaseService defaultFlowBaseService;

    @Autowired
    private FlowInstanceService flowInstanceService;

    @Autowired
    private FlowCommonUtil flowCommonUtil;

    protected BaseEntityDao<FlowSolidifyExecutor> getDao() {
        return this.flowSolidifyExecutorDao;
    }


    public ResponseData getExecuteInfoByBusinessId(String businessId) {
        ResponseData responseData = new ResponseData();
        if (StringUtils.isNotEmpty(businessId)) {
            FlowInstance flowInstance = flowInstanceService.findLastInstanceByBusinessId(businessId);
            if (flowInstance == null) {
                responseData.setSuccess(false);
                responseData.setMessage("流程实例不存在！");
                return responseData;
            }
            List<FlowSolidifyExecutor> solidifylist = flowSolidifyExecutorDao.findListByProperty("businessId", businessId);
            if (solidifylist == null || solidifylist.size() == 0) {
                responseData.setSuccess(false);
                responseData.setMessage("固化流程配置数据不存在！");
                return responseData;
            }
            Definition definitionP = flowCommonUtil.flowDefinition(flowInstance.getFlowDefVersion());
            List<NodeAndExecutes> list = new ArrayList<NodeAndExecutes>();
            for (FlowSolidifyExecutor bean : solidifylist) {
                String currNodeId = bean.getActTaskDefKey();
                JSONObject currentNode = definitionP.getProcess().getNodes().getJSONObject(currNodeId);
                //节点信息
                FlowNodeVO flowNodeVO = new FlowNodeVO();
                flowNodeVO.setId(currentNode.getString("id"));
                flowNodeVO.setType(currentNode.getString("type"));
                flowNodeVO.setNodeType(currentNode.getString("nodeType"));
                flowNodeVO.setName(currentNode.getString("name"));
                //执行人信息
                String userIds = bean.getExecutorIds();
                String[] idArray = userIds.split(",");
                List<String> userList = Arrays.asList(idArray);
                List<Executor> executorList = flowCommonUtil.getBasicUserExecutors(userList);
                //节点和执行人汇总
                NodeAndExecutes nodeAndExecutes = new NodeAndExecutes(flowNodeVO, executorList);
                list.add(nodeAndExecutes);
            }
            responseData.setData(list);
        } else {
            responseData.setSuccess(false);
            responseData.setMessage("参数不能为空！");
        }
        return responseData;
    }


    /**
     * 通过执行人VO集合保存固化流程的执行人信息
     *
     * @param executorVoList    固化执行人VO集合
     * @param businessModelCode
     * @param businessId
     * @return
     */
    @Transactional
    public ResponseData saveByExecutorVoList(List<FlowSolidifyExecutorVO> executorVoList, String businessModelCode, String businessId) {
        ResponseData responseData = new ResponseData();
        if (executorVoList == null || executorVoList.size() == 0 || StringUtils.isEmpty(businessModelCode) || StringUtils.isEmpty(businessId)) {
            return this.writeErrorLogAndReturnData(null, "请求参数不能为空！");
        }
        //新启动流程时，清除以前的数据
        List<FlowSolidifyExecutor> list = flowSolidifyExecutorDao.findListByProperty("businessId", businessId);
        if (list != null && list.size() > 0) {
            list.forEach(bean -> flowSolidifyExecutorDao.delete(bean));
        }

        try {
            executorVoList.forEach(executorVo -> {
                FlowSolidifyExecutor bean = new FlowSolidifyExecutor();
                bean.setBusinessCode(businessModelCode);
                bean.setBusinessId(businessId);
                bean.setActTaskDefKey(executorVo.getActTaskDefKey());
                bean.setNodeType(executorVo.getNodeType());
                bean.setInstancyStatus(executorVo.getInstancyStatus());
                bean.setExecutorIds(executorVo.getExecutorIds());
                flowSolidifyExecutorDao.save(bean);
            });
        } catch (Exception e) {
            return this.writeErrorLogAndReturnData(e, "节点信息、紧急状态、执行人不能为空！");
        }
        return responseData;
    }

    /**
     * 给FlowTaskCompleteWebVO设置执行人和紧急状态
     *
     * @param list       FlowTaskCompleteWebVO集合
     * @param businessId 业务表单id
     * @return
     */
    public ResponseData setInstancyAndIdsByTaskList(List<FlowTaskCompleteWebVO> list, String businessId) {
        ResponseData responseData = new ResponseData();
        if (list == null || list.size() == 0 || StringUtils.isEmpty(businessId)) {
            return this.writeErrorLogAndReturnData(null, "参数不能为空！");
        }

        for (FlowTaskCompleteWebVO webvO : list) {
            Search search = new Search();
            search.addFilter(new SearchFilter("businessId", businessId));
            search.addFilter(new SearchFilter("actTaskDefKey", webvO.getNodeId()));
            List<FlowSolidifyExecutor> solidifyExecutorlist = flowSolidifyExecutorDao.findByFilters(search);
            if (solidifyExecutorlist == null || solidifyExecutorlist.size() == 0) {
                return this.writeErrorLogAndReturnData(null, "固化执行人未设置！");
            }
            webvO.setInstancyStatus(solidifyExecutorlist.get(0).getInstancyStatus());
            webvO.setUserIds(solidifyExecutorlist.get(0).getExecutorIds());
        }
        responseData.setData(list);
        return responseData;
    }


    /**
     * 通过BusinessId删除固化流程执行人列表
     *
     * @param businessId 业务表单id
     * @return
     */
    public ResponseData deleteByBusinessId(String businessId) {
        ResponseData responseData = new ResponseData();
        if (StringUtils.isEmpty(businessId)) {
            return this.writeErrorLogAndReturnData(null, "参数不能为空！");
        }
        flowSolidifyExecutorDao.deleteByBusinessId(businessId);
        return responseData;
    }


    /**
     * 记录固化逻辑执行顺序
     *
     * @param businessId 表单id
     * @param task
     * @return
     */
    public void manageSolidifyFlowByBusinessIdAndTaskKey(String businessId, FlowTask task) {
        if (StringUtils.isNotEmpty(businessId) && StringUtils.isNotEmpty(task.getActTaskDefKey())) {
            String taskKey = task.getActTaskDefKey();
            String taskUserId = task.getExecutorId();
            List<FlowSolidifyExecutor> list = flowSolidifyExecutorDao.findListByProperty("businessId", businessId);
            if (list != null && list.size() > 0) {
                int maxInt = list.stream().mapToInt(FlowSolidifyExecutor::getTaskOrder).max().getAsInt();
                FlowSolidifyExecutor bean = list.stream().filter(a -> taskKey.equalsIgnoreCase(a.getActTaskDefKey())).findFirst().orElse(null);
                if (bean != null && bean.getExecutorIds().indexOf(taskUserId) != -1) { //转办和委托（实际执行人不是启动时设置的人的情况）
                    if ("SingleSign".equalsIgnoreCase(bean.getNodeType())) {  //单签任务设置实际执行人
                        bean.setTrueExecutorIds(task.getExecutorId());
                    }
                    bean.setTaskOrder(maxInt + 1);
                    flowSolidifyExecutorDao.save(bean);
                }
            }
        }
    }


    /**
     * 固化流程中自动执行待办
     *
     * @param businessId
     * @return
     */
    @Transactional
    public void selfMotionExecuteTask(String businessId) {
        if (StringUtils.isNotEmpty(businessId)) {
            List<FlowSolidifyExecutor> solidifylist = flowSolidifyExecutorDao.findListByProperty("businessId", businessId);
            if (solidifylist != null && solidifylist.size() > 0) {//说明该单据走的固化流程
                //根据业务id查询待办
                ResponseData responseData = flowTaskService.findTasksByBusinessId(businessId);
                if (responseData.getSuccess()) {
                    List<FlowTask> taskList = (List<FlowTask>) responseData.getData();
                    if (taskList != null && taskList.size() > 0) {
                        List<FlowTask> needLsit = new ArrayList<FlowTask>();//需要自动跳过的任务
                        for (FlowTask flowTask : taskList) {
                            FlowSolidifyExecutor bean = solidifylist.stream().
                                    filter(a -> a.getActTaskDefKey().equalsIgnoreCase(flowTask.getActTaskDefKey())).findFirst().orElse(null);
                            //说明是可以自动跳过的任务（普通、单签、会签、审批、并行、串行）
                            if (bean != null) {
                                if (bean.getTaskOrder() == 0) { //该节点未执行过
                                    //检查已经执行过的任务中，待执行人相同的数据
                                    List<FlowSolidifyExecutor> executeList = solidifylist.stream().
                                            filter(a -> (a.getTaskOrder() > 0 && a.getExecutorIds().indexOf(flowTask.getExecutorId()) != -1)).collect(Collectors.toList());
                                    if (executeList != null && executeList.size() > 0) {
                                        //检查相同数据中有多少是单签（单签待办很多，实际执行人只有一个）
                                        List<FlowSolidifyExecutor> singleSignList = executeList.stream().
                                                filter(a -> a.getNodeType().equalsIgnoreCase("SingleSign")).collect(Collectors.toList());
                                        if (singleSignList != null && singleSignList.size() > 0) {
                                            if (executeList.size() != singleSignList.size()) {
                                                //执行过的不全是单签，该任务需要自动跳过
                                                needLsit.add(flowTask);
                                            } else {
                                                //单签里面实际执行人中有没有当前任务的执行人
                                                List<FlowSolidifyExecutor> trueExecuteList = singleSignList.stream().
                                                        filter(a -> (a.getTrueExecutorIds() != null && a.getTrueExecutorIds().indexOf(flowTask.getExecutorId()) != -1)).collect(Collectors.toList());
                                                if (trueExecuteList != null & trueExecuteList.size() > 0) {
                                                    //单签实际执行人有当前的执行人，该任务需要自动跳过
                                                    needLsit.add(flowTask);
                                                }
                                            }
                                        } else {
                                            //执行过的没有单签，该任务需要自动跳过
                                            needLsit.add(flowTask);
                                        }
                                    }
                                } else {//该节点已经执行过(执行过的节点，回到该节点，无论前后都不自动执行)
                                    List<FlowSolidifyExecutor> updateList = solidifylist.stream().
                                            filter(a -> a.getTaskOrder() >= bean.getTaskOrder()).collect(Collectors.toList());
                                    if (updateList != null && updateList.size() > 0) {
                                        updateList.forEach(a -> {
                                            a.setTrueExecutorIds(null);
                                            a.setTaskOrder(0);
                                            flowSolidifyExecutorDao.save(a);
                                        });
                                        return;
                                    }
                                }
                            }
                        }

                        //需要自动跳过的任务
                        if (needLsit != null && needLsit.size() > 0) {
                            this.needSelfMotionTaskList(needLsit, solidifylist);
                        }

                    }
                } else {
                    LogUtil.error("自动执行待办-查询待办失败！");
                }
            }
        } else {
            LogUtil.error("自动执行待办-参数为空！");
        }
    }


    /**
     * 需要自动执行的待办任务
     *
     * @param taskList
     * @param solidifylist
     */
    @Transactional
    public void needSelfMotionTaskList(List<FlowTask> taskList, List<FlowSolidifyExecutor> solidifylist) {
        if (taskList != null && taskList.size() > 0 && solidifylist != null && solidifylist.size() > 0) {
            for (int i = 0; i < taskList.size(); i++) {
                FlowTask task = taskList.get(i);
                Boolean canMobile = task.getCanMobile() == null ? false : task.getCanMobile();
                if (!canMobile) {
                    continue;
                }
                FlowSolidifyExecutor bean = solidifylist.stream().filter(a -> a.getActTaskDefKey().equalsIgnoreCase(task.getActTaskDefKey())).findFirst().orElse(null);
                if (bean != null) {
                    String approved = null; //是否同意
                    if ("CounterSign".equalsIgnoreCase(bean.getNodeType()) || "Approve".equalsIgnoreCase(bean.getNodeType())) { //跳过处理默认同意
                        approved = "true";
                    }
                    ResponseData responseData = null;
                    try {
                        //模拟请求下一步数据
                        responseData = this.simulationGetSelectedNodesInfo(task.getId(), approved, true);
                    } catch (Exception e) {
                        LogUtil.error("模拟请求下一步数据报错：" + e.getMessage(), e);
                    }
                    //模拟下一不节点信息成功
                    if (responseData.getSuccess()) {
                        String taskListString = "";
                        String endEventId = null;
                        if ("CounterSignNotEnd".equalsIgnoreCase(responseData.getData().toString())) { //会签未结束
                            taskListString = "[]";
                        } else if ("EndEvent".equalsIgnoreCase(responseData.getData().toString())) { //结束节点
                            taskListString = "[]";
                            endEventId = "true";
                        } else {
                            List<NodeInfo> nodeInfoList = (List<NodeInfo>) responseData.getData();
                            List<FlowTaskCompleteWebVO> flowTaskCompleteList = new ArrayList<FlowTaskCompleteWebVO>();
                            nodeInfoList.forEach(nodeInfo -> {
                                FlowTaskCompleteWebVO taskWebVO = new FlowTaskCompleteWebVO();
                                taskWebVO.setNodeId(nodeInfo.getId());
                                taskWebVO.setUserVarName(nodeInfo.getUserVarName());
                                taskWebVO.setFlowTaskType(nodeInfo.getFlowTaskType());
                                taskWebVO.setUserIds("");
                                taskWebVO.setSolidifyFlow(true); //固化
                                flowTaskCompleteList.add(taskWebVO);
                            });
                            JSONArray jsonArray = JSONArray.fromObject(flowTaskCompleteList);
                            taskListString = jsonArray.toString();
                        }


                        try {
                            long time = 1; //默认1秒后执行，防止和前面节点执行时间一样，在历史里面顺序不定
                            try {
                                Thread.sleep(1000 * time);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            //自动执行待办
                            long currentTime = System.currentTimeMillis();
                            defaultFlowBaseService.completeTask(task.getId(), bean.getBusinessId(),
                                    "同意【自动执行】", taskListString.toString(),
                                    endEventId, false, approved, currentTime);
                        } catch (Exception e) {
                            LogUtil.error("自动执行待办报错：" + e.getMessage(), e);
                        }

                    }
                }
            }
        }
    }


    /**
     * 模拟请求下一步数据flow-web/flowClient/getSelectedNodesInfo
     *
     * @param taskId
     * @param approved
     * @param solidifyFlow
     * @return
     */
    public ResponseData simulationGetSelectedNodesInfo(String taskId, String approved, Boolean solidifyFlow) throws Exception {
        ResponseData responseData = new ResponseData();
        if (StringUtils.isEmpty(approved)) {
            approved = "true";
        }
        List<NodeInfo> nodeInfoList = flowTaskService.findNexNodesWithUserSet(taskId, approved, null);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            if (nodeInfoList.size() == 1 && "EndEvent".equalsIgnoreCase(nodeInfoList.get(0).getType())) {//只存在结束节点
                responseData.setData("EndEvent");
            } else if (nodeInfoList.size() == 1 && "CounterSignNotEnd".equalsIgnoreCase(nodeInfoList.get(0).getType())) {
                responseData.setData("CounterSignNotEnd");
            } else {
                if (solidifyFlow != null && solidifyFlow == true) { //表示为固化流程（不返回下一步执行人信息）
                    nodeInfoList.forEach(nodeInfo -> nodeInfo.setExecutorSet(null));
                }
                responseData.setData(nodeInfoList);
            }
        } else if (nodeInfoList == null) {
            responseData = this.writeErrorLogAndReturnData(null, "任务不存在，可能已经被处理");
        } else {
            responseData = this.writeErrorLogAndReturnData(null, "当前表单规则找不到符合条件的分支");
        }
        return responseData;
    }


    public ResponseData writeErrorLogAndReturnData(Exception e, String msg) {
        if (e != null) {
            LogUtil.error(e.getMessage());
        }
        ResponseData responseData = new ResponseData();
        responseData.setSuccess(false);
        responseData.setMessage(msg);
        return responseData;
    }


}
