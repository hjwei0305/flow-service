package com.ecmp.flow.dao;

import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.flow.entity.TaskMakeOverPower;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * *************************************************************************************************
 * <p/>
 * 实现功能：
 * <p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/6/15 20:32      谭军(tanjun)                    新建
 * <p/>
 * *************************************************************************************************
 */
public interface CustomFlowTaskDao {

    /**
     * 通过业务实体类型id,基于动态组合条件对象和分页(含排序)对象查询数据集合
     * @param businessModelId
     * @param executorId
     * @param searchConfig
     * @return
     */
    @Transactional(readOnly = true)
    PageResult<FlowTask> findByPageByBusinessModelId(String businessModelId, String executorId, Search searchConfig);

    //转授权
    @Transactional(readOnly = true)
    PageResult<FlowTask> findByPageByBusinessModelIdOfPower(String businessModelId, List<String> executorIdList, Search searchConfig);

    @Transactional(readOnly = true)
    PageResult<FlowTask> findByPage(String executorId, String appSign, Search searchConfig);

    //转授权
    @Transactional(readOnly = true)
    PageResult<FlowTask> findByPageOfPower(List<String> executorIdList, String appSign, Search searchConfig);

    /**
     * 通过用户查询可批量审批待办（包括共同查看模式的转授权待办）
     * @param executorId
     * @param powerList
     * @param searchConfig
     * @return
     */
    @Transactional(readOnly = true)
    PageResult<FlowTask> findByPageCanBatchApprovalOfPower(String executorId, List<TaskMakeOverPower> powerList, Search searchConfig);

    /**
     * 通过业务实体和用户查询批量审批待办（包括共同查看模式的转授权待办）
     *
     * @param businessModelId
     * @param executorId
     * @param powerList
     * @param searchConfig
     * @return
     */
    @Transactional(readOnly = true)
    PageResult<FlowTask> findByPageCanBatchApprovalByBusinessModelIdOfPower(String businessModelId, String executorId, List<TaskMakeOverPower> powerList, Search searchConfig);

    @Transactional(readOnly = true)
    Long findCountByExecutorId(String executorId, Search searchConfig);

    @Transactional(readOnly = true)
    PageResult<FlowTask> findByPageByTenant(String appModuleId, String businessModelId, String flowTypeId, Search searchConfig);

    /**
     * 通过Id获取一个待办任务(设置了办理任务URL)
     *
     * @param taskId 待办任务Id
     * @return 待办任务
     */
    @Transactional(readOnly = true)
    FlowTask findTaskById(String taskId);

}
