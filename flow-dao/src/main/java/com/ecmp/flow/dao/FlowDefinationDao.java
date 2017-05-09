package com.ecmp.flow.dao;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.flow.entity.FlowDefination;
import com.ecmp.flow.entity.FlowType;
import org.springframework.stereotype.Repository;

@Repository
public interface FlowDefinationDao extends BaseEntityDao<FlowDefination> {
    public FlowDefination findByDefKey(String defKey);
}