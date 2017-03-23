package com.ecmp.flow.dao;

import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.flow.entity.AppModule;
import com.ecmp.flow.entity.FlowType;
import org.springframework.stereotype.Repository;

@Repository
public interface FlowTypeDao extends BaseDao<FlowType, String> {

}