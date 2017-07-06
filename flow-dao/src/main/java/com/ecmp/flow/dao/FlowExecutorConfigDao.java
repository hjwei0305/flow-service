package com.ecmp.flow.dao;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.flow.entity.DefaultBusinessModel2;
import com.ecmp.flow.entity.FlowExecutorConfig;
import org.springframework.stereotype.Repository;

@Repository
public interface FlowExecutorConfigDao extends BaseEntityDao<FlowExecutorConfig> {
}