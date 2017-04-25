package com.ecmp.flow.dao;

import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.flow.entity.FlowVariable;
import com.ecmp.flow.entity.WorkPageUrl;
import org.springframework.stereotype.Repository;

@Repository
public interface FlowVariableDao extends BaseDao<FlowVariable, String> {

}