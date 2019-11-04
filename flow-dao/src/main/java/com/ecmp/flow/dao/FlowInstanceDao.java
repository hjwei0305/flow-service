package com.ecmp.flow.dao;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.flow.entity.FlowInstance;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.Date;
import java.util.List;

@Repository
public interface FlowInstanceDao extends BaseEntityDao<FlowInstance> {

    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    @Query("select ft from com.ecmp.flow.entity.FlowInstance ft where ft.actInstanceId = :actInstanceId")
    public  FlowInstance findByActInstanceId(@Param("actInstanceId") String actInstanceId);
//    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    public  List<FlowInstance> findByBusinessId(String businessId);
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    public   List<FlowInstance> findByParentId(String parentId);

    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    @Query("select ft from com.ecmp.flow.entity.FlowInstance ft where ft.businessId = :businessId  order by ft.lastEditedDate desc")
    public List<FlowInstance> findByBusinessIdOrder(@Param("businessId") String businessId);


    /**
     * 通过业务单据id查询没有结束并且没有挂起的流程实例
     * @param businessId  业务单据id
     * @return  流程实例集合
     */
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    @Query("select ft from com.ecmp.flow.entity.FlowInstance ft where ft.suspended=false  and ft.ended=false  and  ft.businessId = :businessId  order by ft.lastEditedDate desc")
    public List<FlowInstance> findNoEndByBusinessIdOrder(@Param("businessId") String businessId);

    /**
     * 根据启动人id查询流程实例
     * @param creatorId
     * @return
     */
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    @Query("select ft from com.ecmp.flow.entity.FlowInstance ft where ft.creatorId  = :creatorId and ft.ended = :ended ")
    public List<FlowInstance> findByCreatorId(@Param("creatorId") String creatorId, @Param("ended") Boolean ended);
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    public  List<FlowInstance> findByFlowDefVersionId(String flowDefVersionId);

    @Query("select fv from com.ecmp.flow.entity.FlowInstance fv where fv.id  = :id")
    public FlowInstance findByIdNoF(@Param("id") String id);

    @Query("select count(ft.id),ft.flowDefVersion.flowDefination.id from com.ecmp.flow.entity.FlowInstance ft where ft.creatorId  = :creatorId and ft.ended = :ended  and  ft.manuallyEnd = :manuallyEnd and ft.startDate>= :startDate and ft.endDate<= :endDate group by ft.flowDefVersion.flowDefination.id")
    List findBillsByExecutorIdGroup(@Param("creatorId") String creatorId, @Param("ended") Boolean ended, @Param("manuallyEnd") Boolean manuallyEnd, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query("select count(ft.id),ft.flowDefVersion.flowDefination.id from com.ecmp.flow.entity.FlowInstance ft where ft.creatorId  = :creatorId and ft.startDate>= :startDate and ft.endDate<= :endDate group by ft.flowDefVersion.flowDefination.id")
    List findBillsByGroup(@Param("creatorId") String creatorId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);


    @Query("select count(ft.id) from com.ecmp.flow.entity.FlowInstance ft where ft.creatorId  = :creatorId and ft.ended = :ended and ft.startDate>= :startDate and ft.endDate<= :endDate")
    Integer getBillsSum(@Param("creatorId") String creatorId, @Param("ended") Boolean ended, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

}