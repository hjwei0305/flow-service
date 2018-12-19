package com.ecmp.flow.dao;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.flow.entity.FlowType;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.List;

@Repository
public interface FlowTypeDao extends BaseEntityDao<FlowType> {

    /**
     * 根据业务实体的id来查询流程类型
     *
     */
//    @Cacheable(cacheNames="findByBusinessModelId")
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    List<FlowType> findByBusinessModelId(String businessModelId);

//    @Cacheable(cacheNames="findOne")
//    FlowType findOne(String id);

    /**
     * 通过业务实体类名获取流程类型
     * @param className 业务实体类名
     * @return 流程类型清单
     */
    List<FlowType> findByBusinessModel_ClassName(String className);
}