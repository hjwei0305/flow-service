package com.ecmp.flow.dao;

import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.flow.entity.FlowTask;
import org.springframework.stereotype.Repository;

@Repository
public interface FlowTaskDao extends BaseDao<FlowTask, String> {

}