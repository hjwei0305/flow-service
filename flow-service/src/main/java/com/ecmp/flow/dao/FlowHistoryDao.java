package com.ecmp.flow.dao;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.flow.entity.FlowHistory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlowHistoryDao extends BaseEntityDao<FlowHistory>, CustomFlowHistoryDao {


    long deleteByFlowInstanceId(String flowInstanceId);

    /**
     * 根据执行人ID归类查询历史
     *
     * @param executorId
     * @return
     */
    @Query("select count(ft.id),ft.flowDefId from com.ecmp.flow.entity.FlowHistory ft where ft.executorId  = :executorId  group by ft.flowDefId ")
    List findHisByExecutorIdGroup(@Param("executorId") String executorId);


    /**
     * 根据执行人ID归类查询历史(有效数据)
     *
     * @param executorId
     * @return
     */
    @Query("select count(ft.id),ft.flowDefId from com.ecmp.flow.entity.FlowHistory ft where ft.executorId  = :executorId  and  ( ft.flowExecuteStatus in ('submit','agree','disagree','turntodo','entrust','recall','reject','haveRead') or  ft.flowExecuteStatus is null )  group by ft.flowDefId ")
    List findHisByExecutorIdGroupValid(@Param("executorId") String executorId);

    /**
     * 根据执行人ID归类查询历史(有效数据，排除发起人跳过情况)
     *
     * @param executorId
     * @return
     */
    @Query("select count(ft.id),ft.flowDefId from com.ecmp.flow.entity.FlowHistory ft where ft.executorId  = :executorId and (ft.executorId != ft.flowInstance.creatorId or ft.depict not like '%【自动执行】%' )    and  ( ft.flowExecuteStatus in ('submit','agree','disagree','turntodo','entrust','recall','reject','haveRead') or  ft.flowExecuteStatus is null )  group by ft.flowDefId ")
    List findHisByExecutorIdGroupValidHideNode(@Param("executorId") String executorId);

    /**
     * 根据执行人ID归类查询历史(记录数据)
     *
     * @param executorId
     * @return
     */
    @Query("select count(ft.id),ft.flowDefId from com.ecmp.flow.entity.FlowHistory ft where ft.executorId  = :executorId and   ft.flowExecuteStatus in ('end','auto')   group by ft.flowDefId")
    List findHisByExecutorIdGroupRecord(@Param("executorId") String executorId);


    /**
     * 根据执行人ID归类查询历史(根据流程状态)
     *
     * @param executorId
     * @return
     */
    @Query("select count(ft.id),ft.flowDefId from com.ecmp.flow.entity.FlowHistory ft where ft.executorId  = :executorId and   ft.flowInstance.ended = :ended and ft.flowInstance.manuallyEnd = :manuallyEnd   group by ft.flowDefId")
    List findHisByExecutorIdAndFlowStatusGroup(@Param("executorId") String executorId, @Param("ended") Boolean ended, @Param("manuallyEnd") Boolean manuallyEnd);


    @Query("select fh from com.ecmp.flow.entity.FlowHistory fh where fh.flowInstance.id  = :instanceId order by fh.actEndTime asc")
    List<FlowHistory> findByInstanceId(@Param("instanceId") String instanceId);

    @Query("select fh from com.ecmp.flow.entity.FlowHistory fh where fh.flowInstance.id  = :instanceId and fh.taskStatus != 'VIRTUAL'  order by fh.actEndTime asc")
    List<FlowHistory> findByInstanceIdNoVirtual(@Param("instanceId") String instanceId);


    /**
     * 根据业务实体类型id，业务单据id，获取所有业务执行的待办，
     * 包括撤销之前的历史任务以及不同流程类型、不同流程定义、不同版本的数据
     * //     * @param businessModelId
     *
     * @param businessId
     * @return
     */
//    @Query("select ft from com.ecmp.flow.entity.FlowHistory ft where  ft.flowDefinitionId in(select fd.id from FlowDefination fd where fd.id in(select fType.id from FlowType fType where fType.id in( select bm.id from BusinessModel bm where bm.id = :businessModelId)) ) and ft.flowInstance.id in(select fi.id from FlowInstance fi where fi.businessId = :businessId ) order by ft.lastEditedDate desc")
    @Query("select ft from com.ecmp.flow.entity.FlowHistory ft where ft.flowInstance.id in(select fi.id from FlowInstance fi where fi.businessId = :businessId ) order by ft.lastEditedDate desc")
    List<FlowHistory> findAllByBusinessId(@Param("businessId") String businessId);


//    /**
//     * 根据业务实体类型id，业务单据id，获取所有业务执行的待办，
//     * 包括撤销之前的历史任务以及不同流程类型、不同流程定义、不同版本的数据
//     //     * @param businessModelId
//     * @param businessId
//     * @return
//     */
////    @Query("select ft from com.ecmp.flow.entity.FlowHistory ft where  ft.flowDefinitionId in(select fd.id from FlowDefination fd where fd.id in(select fType.id from FlowType fType where fType.id in( select bm.id from BusinessModel bm where bm.id = :businessModelId)) ) and ft.flowInstance.id in(select fi.id from FlowInstance fi where fi.businessId = :businessId ) order by ft.lastEditedDate desc")
//    @Query("select ft from com.ecmp.flow.entity.FlowHistory ft where ft.flowInstance.id in(select fi.id from FlowInstance fi where fi.businessId = :businessId and fi.flowDefVersion.flowDefination.flowType.businessModel.className = :businessModelCode) order by ft.lastEditedDate desc")
//    public List<FlowHistory> findAllByBusinessIdAndBusinessModelCode(@Param("businessId")String businessId,@Param("businessModelCode")String businessModelCode);


    /**
     * 根据流程实例id查询历史
     *
     * @param actInstanceId
     * @return
     */
    @Query("select ft from com.ecmp.flow.entity.FlowHistory ft where ft.flowInstance.actInstanceId  = :actInstanceId and ft.actTaskDefKey = :actTaskDefKey")
    List<FlowHistory> findByActTaskDefKeyAndActInstanceId(@Param("actTaskDefKey") String actTaskDefKey, @Param("actInstanceId") String actInstanceId);


    /**
     * 根据流程实例id查询历史
     *
     * @param actInstanceId
     * @return
     */
    @Query("select count(ft.id) from com.ecmp.flow.entity.FlowHistory ft where ft.flowInstance.actInstanceId  = :actInstanceId and ft.actTaskDefKey = :actTaskDefKey order by ft.createdDate desc")
    Integer findCountByActTaskDefKeyAndActInstanceId(@Param("actTaskDefKey") String actTaskDefKey, @Param("actInstanceId") String actInstanceId);
}