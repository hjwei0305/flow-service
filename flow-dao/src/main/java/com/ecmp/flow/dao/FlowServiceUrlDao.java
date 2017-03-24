package com.ecmp.flow.dao;

import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.flow.entity.FlowServiceUrl;
import com.ecmp.flow.entity.FlowType;
import org.springframework.stereotype.Repository;

@Repository
public interface FlowServiceUrlDao extends BaseDao<FlowServiceUrl, String> {

}