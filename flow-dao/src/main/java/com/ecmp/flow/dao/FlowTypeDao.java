package com.ecmp.flow.dao;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.flow.entity.FlowType;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlowTypeDao extends BaseEntityDao<FlowType> {

    /**
     * 根据业务实体的id来查询流程类型
     *
     */
//    @Cacheable(cacheNames="findByBusinessModelId")
    List<FlowType> findByBusinessModelId(String businessModelId);

//    @Cacheable(cacheNames="findOne")
//    FlowType findOne(String id);
}