package com.ecmp.flow.dao;

import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.entity.FlowTask;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FlowTaskDao extends BaseDao<FlowTask, String> {
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

}