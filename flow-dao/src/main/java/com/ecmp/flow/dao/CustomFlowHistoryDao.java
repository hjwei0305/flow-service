package com.ecmp.flow.dao;

import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.flow.entity.FlowHistory;
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
public interface CustomFlowHistoryDao {
    /**
     * 通过业务实体类型id,基于动态组合条件对象和分页(含排序)对象查询数据集合
     */
    @Transactional(readOnly = true)
    public PageResult<FlowHistory> findByPageByBusinessModelId(String businessModelId, String executorAccount, Search searchConfig);

    /**
     * 根据业务实体类型id，业务单据id，获取最新流程实体执行的待办，不包括撤销之前的历史任务
//     * @param businessModelId
     * @param businessId
     * @return
     */
//    @Query("select ft from com.ecmp.flow.entity.FlowHistory ft where  ft.flowDefinitionId in(select  fd.id from FlowDefination fd where fd.id in(select fType.id from FlowType fType where fType.id in( select bm.id from BusinessModel bm where bm.id = :businessModelId)) ) and fd.flowInstance.id = (select fi.id from FlowInstance fi where fi.businessId = :businessId ) order by ft.lastEditedDate desc")
    public List<FlowHistory> findLastByBusinessId(String businessId);
}
