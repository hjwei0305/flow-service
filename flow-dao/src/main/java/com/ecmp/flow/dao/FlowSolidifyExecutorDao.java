package com.ecmp.flow.dao;


import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.flow.entity.FlowSolidifyExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface FlowSolidifyExecutorDao  extends BaseEntityDao<FlowSolidifyExecutor> {

    @Transactional
    public long deleteByBusinessId(String actTaskId);


}
