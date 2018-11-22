package com.ecmp.flow.dao;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.flow.dto.FlowTaskExecutorIdAndCount;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.flow.vo.CanAddOrDelNodeInfo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlowTaskDao extends BaseEntityDao<FlowTask>, CustomFlowTaskDao {
    /**
     * 删除没有进行任务签收的任务
     *
     * @param actTaskId 关联流程引擎实际的任务ID
     * @param idExclude 排除的ID,一般是指当前进行任务签收的任务
     * @return
     */
    @Modifying
    @Query("delete from FlowTask fv where (fv.actTaskId = :actTaskId) and (fv.id <> :idExclude) ")
    public Integer deleteNotClaimTask(@Param("actTaskId") String actTaskId, @Param("idExclude") String idExclude);

    public long deleteByActTaskId(String actTaskId);

    public long deleteByFlowInstanceId(String flowInstanceId);

    public FlowTask findByActTaskId(String actTaskId);

    /**
     * 根据流程实例id，节点key查询待办
     *
     * @param actInstanceId
     * @return
     */
    @Query("select ft from com.ecmp.flow.entity.FlowTask ft where ft.flowInstance.actInstanceId  = :actInstanceId and ft.actTaskDefKey = :actTaskDefKey")
    public List<FlowTask> findByActTaskDefKeyAndActInstanceId(@Param("actTaskDefKey") String actTaskDefKey, @Param("actInstanceId") String actInstanceId);


    /**
     * 通过实例id、节点key、执行人筛选任务
     * @param actTaskDefKey
     * @param actInstanceId
     * @param executorId
     * @return
     */
    @Query("select ft from com.ecmp.flow.entity.FlowTask ft where ft.flowInstance.actInstanceId  = :actInstanceId and ft.actTaskDefKey = :actTaskDefKey and ft.executorId = :executorId")
    public List<FlowTask> findByActTaskDefKeyAndActInstanceIdAndExecutorId(@Param("actTaskDefKey") String actTaskDefKey, @Param("actInstanceId") String actInstanceId, @Param("executorId") String executorId);


    /**
     * 根据流程实例id查询待办
     *
     * @param actInstanceId
     * @return
     */
    @Query("select count(ft.id) from com.ecmp.flow.entity.FlowTask ft where ft.flowInstance.actInstanceId  = :actInstanceId and ft.actTaskDefKey = :actTaskDefKey")
    public Integer findCountByActTaskDefKeyAndActInstanceId(@Param("actTaskDefKey") String actTaskDefKey, @Param("actInstanceId") String actInstanceId);

    /**
     * 根据流程实例id查询待办
     *
     * @param instanceId
     * @return
     */
    @Query("select ft from com.ecmp.flow.entity.FlowTask ft where ft.flowInstance.id  = :instanceId")
    public List<FlowTask> findByInstanceId(@Param("instanceId") String instanceId);

    /**
     * 根据执行人账号归类查询
     *
     * @param executorId
     * @return
     */
    @Query("select count(ft.id),ft.flowDefinitionId from com.ecmp.flow.entity.FlowTask ft where ft.executorId  = :executorId group by ft.flowDefinitionId")
    public List findByExecutorIdGroup(@Param("executorId") String executorId);

    /**
     * 根据执行人账号归类查询,可批量审批
     *
     * @param executorId
     * @return
     */
    @Query("select count(ft.id),ft.flowDefinitionId from com.ecmp.flow.entity.FlowTask ft where ft.executorId  = :executorId and ft.canBatchApproval = true  group by ft.flowDefinitionId")
    public List findByExecutorIdGroupCanBatchApproval(@Param("executorId") String executorId);

    /**
     * 查询可以加签的待办-针对启动
     *
     * @return
     */
    @Query("select  new com.ecmp.flow.vo.CanAddOrDelNodeInfo( ft.flowInstance.actInstanceId,ft.actTaskDefKey,ft.taskName,ft.flowInstance.businessId,ft.flowInstance.businessCode,ft.flowInstance.businessName,ft.flowInstance.businessModelRemark,ft.flowInstance.flowName,ft.flowInstance.flowDefVersion.defKey) from FlowTask ft where ft.allowAddSign  = true and (ft.preId is null and ft.flowInstance.creatorId = :executorId)")
    public List<CanAddOrDelNodeInfo> findByAllowAddSignStart(@Param("executorId") String executorId);

    /**
     * 查询可以加签的待办-针对非启动
     *
     * @return
     */
    @Query("select  new com.ecmp.flow.vo.CanAddOrDelNodeInfo( ft.flowInstance.actInstanceId,ft.actTaskDefKey,ft.taskName,ft.flowInstance.businessId,ft.flowInstance.businessCode,ft.flowInstance.businessName,ft.flowInstance.businessModelRemark,ft.flowInstance.flowName,ft.flowInstance.flowDefVersion.defKey) from FlowTask ft inner join  FlowHistory fh on ft.preId = fh.id where ft.allowAddSign  = true and  fh.executorId = :executorId")
    public List<CanAddOrDelNodeInfo> findByAllowAddSign(@Param("executorId") String executorId);

    /**
     * 查询可以减签的待办-针对启动
     *
     * @return
     */
    @Query("select  new com.ecmp.flow.vo.CanAddOrDelNodeInfo( ft.flowInstance.actInstanceId,ft.actTaskDefKey,ft.taskName,ft.flowInstance.businessId,ft.flowInstance.businessCode,ft.flowInstance.businessName,ft.flowInstance.businessModelRemark,ft.flowInstance.flowName,ft.flowInstance.flowDefVersion.defKey) from FlowTask ft where ft.allowSubtractSign  = true and (ft.preId is null and ft.flowInstance.creatorId = :executorId)")
    public List<CanAddOrDelNodeInfo> findByAllowSubtractSignStart(@Param("executorId") String executorId);

    /**
     * 查询可以减签的待办-针对非启动
     *
     * @return
     */
    @Query("select  new com.ecmp.flow.vo.CanAddOrDelNodeInfo( ft.flowInstance.actInstanceId,ft.actTaskDefKey,ft.taskName,ft.flowInstance.businessId,ft.flowInstance.businessCode,ft.flowInstance.businessName,ft.flowInstance.businessModelRemark,ft.flowInstance.flowName,ft.flowInstance.flowDefVersion.defKey) from FlowTask ft inner join  FlowHistory fh on ft.preId = fh.id where ft.allowSubtractSign  = true and  fh.executorId = :executorId")
    public List<CanAddOrDelNodeInfo> findByAllowSubtractSign(@Param("executorId") String executorId);


    @Query("select new com.ecmp.flow.dto.FlowTaskExecutorIdAndCount(ft.executorId,count(ft.id)) from com.ecmp.flow.entity.FlowTask ft group by ft.executorId")
    public List<FlowTaskExecutorIdAndCount> findAllExecutorIdAndCount();

//    @Query("select new com.ecmp.flow.dto.FlowTaskExecutorIdAndCount(ft.flowDefinitionId,ft.flowName,ft.taskKey,ft.taskName,ft.executorId,count(ft.id)) from com.ecmp.flow.entity.FlowTask ft group by ft.executorId,ft.flowDefinitionId,ft.taskKey")
//    public List<FlowTaskExecutorIdAndCount> findAllExecutorIdTaskKeyAndCount();

    @Query("select new com.ecmp.flow.dto.FlowTaskExecutorIdAndCount(ft.executorId,ft.executorName,ft.flowDefinitionId,ft.flowName,ft.actTaskDefKey,ft.taskName,count(ft.id)) from com.ecmp.flow.entity.FlowTask ft where ft.executorId = :executorId group by ft.executorId,ft.executorName,ft.flowDefinitionId,ft.flowName,ft.actTaskDefKey,ft.taskName")
    public List<FlowTaskExecutorIdAndCount> findAllTaskKeyAndCountByExecutorId(@Param("executorId") String executorId);
}
