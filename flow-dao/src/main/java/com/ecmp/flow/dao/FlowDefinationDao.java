package com.ecmp.flow.dao;

import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.flow.entity.FlowDefination;
import com.ecmp.flow.entity.FlowType;
import org.springframework.stereotype.Repository;

@Repository
public interface FlowDefinationDao extends BaseDao<FlowDefination, String> {
    public FlowDefination findByDefKey(String defKey);
}