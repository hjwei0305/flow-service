package com.ecmp.flow.dao;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.flow.entity.FlowServiceUrl;
import com.ecmp.flow.entity.FlowType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlowServiceUrlDao extends BaseEntityDao<FlowServiceUrl> {
    /**
     * 通过流程类型id查找可配置工作页面
     * @param flowTypeId 流程类型id
     * @return
     */
    @Query("select w from com.ecmp.flow.entity.FlowServiceUrl w where w.businessModel.id =  (select t.businessModel.id from com.ecmp.flow.entity.FlowType t  where t.id = :flowTypeId) ")
    List<com.ecmp.flow.entity.FlowServiceUrl> findByFlowTypeId(@Param("flowTypeId")String flowTypeId);

    /**
     * 通过业务实体id查找可配置工作页面
     * @param businessModelId 流程类型id
     * @return
     */
    @Query("select w from com.ecmp.flow.entity.FlowServiceUrl w where w.businessModel.id =  :businessModelId ")
    List<com.ecmp.flow.entity.FlowServiceUrl> findByBusinessModelId(@Param("businessModelId")String businessModelId);
}