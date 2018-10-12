package com.ecmp.flow.dao;

import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.flow.entity.FlowTask;
import org.springframework.transaction.annotation.Transactional;


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
     */
    @Transactional(readOnly = true)
    PageResult<FlowTask> findByPageByBusinessModelId(String businessModelId,String executorId, Search searchConfig);

    @Transactional(readOnly = true)
    PageResult<FlowTask> findByPage(String executorId, String appSign, Search searchConfig);

    @Transactional(readOnly = true)
    PageResult<FlowTask> findByPageCanBatchApproval(String executorId, Search searchConfig);

    @Transactional(readOnly = true)
    PageResult<FlowTask> findByPageCanBatchApprovalByBusinessModelId(String businessModelId ,String executorId, Search searchConfig);

    @Transactional(readOnly = true)
    Long findCountByExecutorId(String executorId, Search searchConfig);

    @Transactional(readOnly = true)
    PageResult<FlowTask> findByPageByTenant(String appModuleId, String businessModelId, String flowTypeId, Search searchConfig);

    /**
     * 通过Id获取一个待办任务(设置了办理任务URL)
     * @param taskId 待办任务Id
     * @return 待办任务
     */
    @Transactional(readOnly = true)
    FlowTask findTaskById(String taskId);
}
