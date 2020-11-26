package com.ecmp.flow.dao;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.flow.entity.DisagreeReason;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface DisagreeReasonDao extends BaseEntityDao<DisagreeReason> {


    @Query("select k from com.ecmp.flow.entity.DisagreeReason k where k.flowTypeId = :flowTypeId and  k.tenantCode = :tenantCode order by k.rank  ")
    List<DisagreeReason> findByFlowTypeIdAndTenantCode(@Param("flowTypeId") String flowTypeId, @Param("tenantCode") String tenantCode);

}
