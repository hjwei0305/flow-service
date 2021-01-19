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
     * 通过业务实体ID和用户获取待办信息（包括共同查看模式的转授权待办）
     */
    @Transactional(readOnly = true)
    PageResult<FlowTask> findByPageByBusinessModelIdOfPower(String businessModelId, String executorId, List<TaskMakeOverPower> powerList, Search searchConfig);

    /**
     * 通过业务实体和用户查询批量审批待办（包括共同查看模式的转授权待办）
     */
    @Transactional(readOnly = true)
    PageResult<FlowTask> findByPageCanBatchApprovalByBusinessModelIdOfPower(String businessModelId, String executorId, List<TaskMakeOverPower> powerList, Search searchConfig);


    @Transactional(readOnly = true)
    Long findCountByExecutorId(String executorId, Search searchConfig);


    @Transactional(readOnly = true)
    PageResult<FlowTask> findByPageByTenant(String appModuleId, String businessModelId, String flowTypeId, Search searchConfig);

    /**
     * 通过Id获取一个待办任务(设置了办理任务URL)
     */
    @Transactional(readOnly = true)
    FlowTask findTaskById(String taskId);

}
