package com.ecmp.flow.dao;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.entity.FlowDefination;
import com.ecmp.flow.entity.FlowType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlowDefVersionDao extends BaseEntityDao<FlowDefVersion> {

    @Query("select fv from com.ecmp.flow.entity.FlowDefVersion fv where fv.flowDefination.id  = :defId and fv.versionCode = :versionCode")
    public FlowDefVersion findByDefIdAndVersionCode(@Param("defId")String defId,@Param("versionCode")Integer versionCode);

    public List<FlowDefVersion> findByFlowDefinationId(String defId);

    @Query("select fv from com.ecmp.flow.entity.FlowDefVersion fv where fv.flowDefination.id  = :defId and fv.flowDefinationStatus = 1 order by fv.versionCode desc")
    public List<FlowDefVersion> findByFlowDefinationIdActivate(@Param("defId")String defId);

    @Query("select fv from com.ecmp.flow.entity.FlowDefVersion fv where fv.defKey  = :defKey and fv.flowDefinationStatus = 1 order by fv.versionCode desc")
    public List<FlowDefVersion> findByKeyActivate(@Param("defKey")String defKey);

    public FlowDefVersion findByActDefId(String actDefId);

    @Query("select fv from com.ecmp.flow.entity.FlowDefVersion fv where fv.defKey  = :defKey and fv.versionCode = :versionCode")
    public FlowDefVersion findByKeyAndVersionCode(@Param("defKey")String defKey,@Param("versionCode")Integer versionCode);

    }