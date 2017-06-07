package com.ecmp.flow.dao;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.flow.entity.FlowHistory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlowHistoryDao extends BaseEntityDao<FlowHistory> {

    @Query("select fh from com.ecmp.flow.entity.FlowHistory fh where fh.flowInstance.id  = :instanceId order by fh.actEndTime asc")
    public List<FlowHistory> findByInstanceId(@Param("instanceId")String instanceId);
}