package com.ecmp.flow.dao;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.entity.FlowTask;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface FlowTaskDao extends BaseEntityDao<FlowTask>,CustomFlowTaskDao {
    /**
     * 删除没有进行任务签收的任务
     * @param actTaskId  关联流程引擎实际的任务ID
     * @param idExclude  排除的ID,一般是指当前进行任务签收的任务
     * @return
     */
    @Modifying
    @Query("delete from FlowTask fv where (fv.actTaskId = :actTaskId) and (fv.id <> :idExclude) ")
    public Integer deleteNotClaimTask(@Param("actTaskId")String actTaskId, @Param("idExclude")String idExclude);

    public long  deleteByActTaskId(String actTaskId);

    public  FlowTask findByActTaskId(String actTaskId);

    /**
     * 根据流程实例id查询待办
     * @param instanceId
     * @return
     */
    @Query("select ft from com.ecmp.flow.entity.FlowTask ft where ft.flowInstance.id  = :instanceId")
    public List<FlowTask> findByInstanceId(@Param("instanceId")String instanceId);

    /**
     * 根据执行人账号归类查询
     * @param executorAccount
     * @return
     */
    @Query("select count(ft.id),ft.flowDefinitionId from com.ecmp.flow.entity.FlowTask ft where ft.executorAccount  = :executorAccount group by ft.flowDefinitionId")
    public List findByexecutorAccountGroup(@Param("executorAccount")String executorAccount);




}