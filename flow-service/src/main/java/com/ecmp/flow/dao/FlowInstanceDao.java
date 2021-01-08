package com.ecmp.flow.dao;

import com.ecmp.core.dao.BaseEntityDao;
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

    /**
     * 通过业务单据id查询没有结束并且没有挂起的流程实例
     *
     * @param businessId 业务单据id
     * @return 流程实例集合
     */
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    @Query("select ft from com.ecmp.flow.entity.FlowInstance ft where ft.suspended=false  and ft.ended=false  and  ft.businessId = :businessId  order by ft.lastEditedDate desc")
    List<FlowInstance> findNoEndByBusinessIdOrder(@Param("businessId") String businessId);

    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    @Query("select ft from com.ecmp.flow.entity.FlowInstance ft where ft.actInstanceId = :actInstanceId")
    FlowInstance findByActInstanceId(@Param("actInstanceId") String actInstanceId);

    List<FlowInstance> findByBusinessId(String businessId);

    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    List<FlowInstance> findByParentId(String parentId);

    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    @Query("select ft from com.ecmp.flow.entity.FlowInstance ft where ft.businessId = :businessId  order by ft.lastEditedDate desc")
    List<FlowInstance> findByBusinessIdOrder(@Param("businessId") String businessId);

    @Query("select ft from com.ecmp.flow.entity.FlowInstance ft where  ft.businessId = :businessId   and  ft.ended=false  ")
    FlowInstance findByBusinessIdNoEnd(@Param("businessId") String businessId);

    @Query("select ft from com.ecmp.flow.entity.FlowInstance ft where  ft.businessId in  :businessIds  and  ft.ended=false  ")
    List<FlowInstance> findByBusinessIdListNoEnd(@Param("businessIds") List<String> businessIds);

    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    List<FlowInstance> findByFlowDefVersionId(String flowDefVersionId);

    @Query("select count(ft.id),ft.flowDefVersion.flowDefination.id from com.ecmp.flow.entity.FlowInstance ft where ft.creatorId  = :creatorId   group by ft.flowDefVersion.flowDefination.id")
    List findBillsByGroup(@Param("creatorId") String creatorId);

    @Query("select count(ft.id),ft.flowDefVersion.flowDefination.id from com.ecmp.flow.entity.FlowInstance ft where ft.creatorId  = :creatorId and  ft.flowDefVersion.flowDefination.flowType.businessModel.appModule.code = :appModelCode   group by ft.flowDefVersion.flowDefination.id")
    List findBillsByGroupAndAppCode(@Param("creatorId") String creatorId, @Param("appModelCode") String appModelCode);

    @Query("select count(ft.id),ft.flowDefVersion.flowDefination.id from com.ecmp.flow.entity.FlowInstance ft where ft.creatorId  = :creatorId and ft.ended = :ended  and  ft.manuallyEnd = :manuallyEnd  group by ft.flowDefVersion.flowDefination.id")
    List findBillsByExecutorIdGroup(@Param("creatorId") String creatorId, @Param("ended") Boolean ended, @Param("manuallyEnd") Boolean manuallyEnd);

    @Query("select count(ft.id),ft.flowDefVersion.flowDefination.id from com.ecmp.flow.entity.FlowInstance ft where ft.creatorId  = :creatorId and ft.ended = :ended  and  ft.manuallyEnd = :manuallyEnd and ft.flowDefVersion.flowDefination.flowType.businessModel.appModule.code = :appModelCode  group by ft.flowDefVersion.flowDefination.id")
    List findBillsByExecutorIdGroupAndAppCode(@Param("creatorId") String creatorId, @Param("ended") Boolean ended, @Param("manuallyEnd") Boolean manuallyEnd, @Param("appModelCode") String appModelCode);

    @Query("select count(ft.id) from com.ecmp.flow.entity.FlowInstance ft where ft.creatorId  = :creatorId and ft.ended = :ended and ft.startDate>= :startDate and ft.endDate<= :endDate")
    Integer getBillsSum(@Param("creatorId") String creatorId, @Param("ended") Boolean ended, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

}