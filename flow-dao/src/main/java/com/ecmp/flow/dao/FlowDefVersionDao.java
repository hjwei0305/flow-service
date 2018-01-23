package com.ecmp.flow.dao;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.flow.entity.FlowDefVersion;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlowDefVersionDao extends BaseEntityDao<FlowDefVersion> {

    @Cacheable(cacheNames="findByDefIdAndVersionCode")
    @Query("select fv from com.ecmp.flow.entity.FlowDefVersion fv where fv.flowDefination.id  = :defId and fv.versionCode = :versionCode")
    public FlowDefVersion findByDefIdAndVersionCode(@Param("defId")String defId,@Param("versionCode")Integer versionCode);

    @Cacheable(cacheNames="findByFlowDefinationId")
    public List<FlowDefVersion> findByFlowDefinationId(String defId);

    @Cacheable(cacheNames="findByFlowDefinationIdActivate")
    @Query("select fv from com.ecmp.flow.entity.FlowDefVersion fv where fv.flowDefination.id  = :defId and fv.flowDefinationStatus = 1 order by fv.versionCode desc")
    public List<FlowDefVersion> findByFlowDefinationIdActivate(@Param("defId")String defId);

    @Cacheable(cacheNames="findByKeyActivate")
    @Query("select fv from com.ecmp.flow.entity.FlowDefVersion fv where fv.defKey  = :defKey and fv.flowDefinationStatus = 1 order by fv.versionCode desc")
    public List<FlowDefVersion> findByKeyActivate(@Param("defKey")String defKey);

    @Cacheable(cacheNames="findByActDefId")
    public FlowDefVersion findByActDefId(String actDefId);

    @Cacheable(cacheNames="findByKeyAndVersionCode")
    @Query("select fv from com.ecmp.flow.entity.FlowDefVersion fv where fv.defKey  = :defKey and fv.versionCode = :versionCode")
    public FlowDefVersion findByKeyAndVersionCode(@Param("defKey")String defKey,@Param("versionCode")Integer versionCode);

    }