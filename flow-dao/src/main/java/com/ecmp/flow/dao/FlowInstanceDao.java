package com.ecmp.flow.dao;

import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.flow.entity.FlowInstance;
import org.springframework.stereotype.Repository;

@Repository
public interface FlowInstanceDao extends BaseDao<FlowInstance, String> {

}