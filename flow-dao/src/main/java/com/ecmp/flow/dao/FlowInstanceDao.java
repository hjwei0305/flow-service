package com.ecmp.flow.dao;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.flow.entity.FlowInstance;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FlowInstanceDao extends BaseEntityDao<FlowInstance> {

    public  FlowInstance findByActInstanceId(String actInstanceId);

    public  FlowInstance findByBusinessId(String businessId);



}