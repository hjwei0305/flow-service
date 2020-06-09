package com.ecmp.flow.service;

import com.ecmp.core.search.PageInfo;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.flow.api.IFlowIntegrateService;
import com.ecmp.flow.basic.vo.Executor;
import com.ecmp.flow.dto.PortalFlowHistory;
import com.ecmp.flow.dto.PortalFlowTask;
import com.ecmp.flow.dto.PortalFlowTaskParam;
import com.ecmp.flow.entity.FlowHistory;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.flow.vo.*;
import com.ecmp.log.util.LogUtil;
import com.ecmp.vo.OperateResult;
import com.ecmp.vo.OperateResultWithData;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * <strong>实现功能:</strong>
 * <p>工作流业务集成服务</p>
 *
 * @author 王锦光 wangj
 * @version 1.0.1 2018-12-19 10:54
 */
@Service
public class FlowIntegrateService implements IFlowIntegrateService {
    @Autowired
    private FlowDefinationService flowDefinationService;
    @Autowired
    private FlowTaskService flowTaskService;
    @Autowired
    private FlowHistoryService flowHistoryService;

    /**
     * 使用默认值启动业务流程
     *
     * @param startParam 启动参数
     * @return 操作结果
     */
    @Override
    public OperateResult startDefaultFlow(DefaultStartParam startParam) {
        // 获取流程类型
        FlowStartVO startVO = new FlowStartVO();
        startVO.setBusinessModelCode(startParam.getBusinessModelCode());
        startVO.setBusinessKey(startParam.getBusinessKey());
        OperateResultWithData<FlowStartResultVO> flowStartTypeResult;
        try {
            flowStartTypeResult = flowDefinationService.startByVO(startVO);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            LogUtil.error("获取流程类型异常！", e);
            // 获取流程类型异常！{0}
            return OperateResult.operationFailure("10066", e.getMessage());
        }
        if (flowStartTypeResult.notSuccessful()) {
            return OperateResultWithData.converterNoneData(flowStartTypeResult);
        }
        FlowStartResultVO flowStartResultVO = flowStartTypeResult.getData();
        if (CollectionUtils.isEmpty(flowStartResultVO.getFlowTypeList()) && CollectionUtils.isEmpty(flowStartResultVO.getNodeInfoList())) {
            // 业务实体类型【{0}】没有配置默认的流程定义！
            return OperateResult.operationFailure("10067", startParam.getBusinessModelCode());
        }
        // 调试代码
        // System.out.println(JsonUtils.toJson(flowStartResultVO));
        // 使用默认流程参数启动
        StartFlowTypeVO flowType = flowStartResultVO.getFlowTypeList().get(0);
        NodeInfo nodeInfo = flowStartResultVO.getNodeInfoList().get(0);
        startVO.setFlowTypeId(flowType.getId());
        startVO.setFlowDefKey(flowType.getFlowDefKey());
        startVO.setPoolTask(Boolean.FALSE);
        // 判断是否为工作池节点
        if (nodeInfo.getType().equalsIgnoreCase("PoolTask")) {
            startVO.setPoolTask(Boolean.TRUE);
        }
        // 确定默认的下一步执行人
        Map<String, Object> userMap = new HashMap<>();
        Map<String, List<String>> selectedNodesUserMap = new HashMap<>();
        Map<String, Object> variables = new HashMap<>();
        if (startVO.getPoolTask()) {
            // 工作池节点不用设置具体的执行人
            userMap.put("anonymous", "anonymous");
            selectedNodesUserMap.put(nodeInfo.getId(), new ArrayList<>());
        } else {
            // 使用默认执行人
            Set<Executor> executors = nodeInfo.getExecutorSet();
            if (!CollectionUtils.isEmpty(executors)) {
                String  uiType  =  nodeInfo.getUiType();
                List<String> userList = new ArrayList<String>();
                if(uiType.equalsIgnoreCase("checkbox")){
                    for(Executor  executor:executors){
                        userList.add(executor.getId());
                    }
                    userMap.put(nodeInfo.getUserVarName(), userList);
                }else{
                    Executor executor = executors.iterator().next();
                    String userIds =  executor.getId();
                    userList.add(userIds);
                    userMap.put(nodeInfo.getUserVarName(), userIds);
                }
                selectedNodesUserMap.put(nodeInfo.getUserVarName(), userList);
            }
        }
        startVO.setUserMap(userMap);
        variables.put("selectedNodesUserMap", selectedNodesUserMap);
        startVO.setVariables(variables);
        // 调试代码
        // System.out.println(JsonUtils.toJson(startVO));
        // 尝试启动流程
        try {
            flowStartTypeResult = flowDefinationService.startByVO(startVO);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            LogUtil.error("业务流程启动异常！", e);
            // 业务流程启动异常！{0}
            return OperateResult.operationFailure("10068", e.getMessage());
        }
        if (flowStartTypeResult.notSuccessful()) {
            return OperateResultWithData.converterNoneData(flowStartTypeResult);
        }
        // 流程启动成功！
        return OperateResult.operationSuccess("10065");
    }

    /**
     * 获取当前用户门户待办信息
     *
     * @param portalFlowTaskParam 门户待办查询参数
     * @return 门户待办信息清单
     */
    @Override
    public List<PortalFlowTask> getPortalFlowTask(PortalFlowTaskParam portalFlowTaskParam) {
        List<PortalFlowTask> result = new ArrayList<>();
        // 构造查询参数
        Search search = new Search();
        PageInfo pageInfo = new PageInfo();
        pageInfo.setRows(portalFlowTaskParam.getRecordCount());
        search.setPageInfo(pageInfo);
        FlowTaskPageResultVO<FlowTask> resVo = flowTaskService.findByBusinessModelIdWithAllCount(portalFlowTaskParam.getModelId(), "", search);
        if (CollectionUtils.isNotEmpty(resVo.getRows())) {
            resVo.getRows().forEach(flowTask -> result.add(new PortalFlowTask(flowTask)));
        }
        return result;
    }

    /**
     * 获取当前用户门户已办信息
     *
     * @param recordCount 获取条目数
     * @return 门户已办信息清单
     */
    @Override
    public List<PortalFlowHistory> getPortalFlowHistory(Integer recordCount) {
        List<PortalFlowHistory> result = new ArrayList<>();
        // 构造查询参数
        Search search = new Search();
        PageInfo pageInfo = new PageInfo();
        pageInfo.setRows(recordCount);
        search.setPageInfo(pageInfo);
        PageResult<FlowHistory> pageResult = flowHistoryService.findByBusinessModelId(null, search);
        if (CollectionUtils.isNotEmpty(pageResult.getRows())) {
            pageResult.getRows().forEach(flowHistory -> result.add(new PortalFlowHistory(flowHistory)));
        }
        return result;
    }
}
