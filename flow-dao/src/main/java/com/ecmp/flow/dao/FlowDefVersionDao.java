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

    @Query("select fv from com.ecmp.flow.entity.FlowDefVersion fv where fv.flowDefination.id  = :defId and fv.versionCode = :versionCode ")
    public FlowDefVersion findByDefIdAndVersionCode(@Param("defId")String defId,@Param("versionCode")Integer versionCode);

    public List<FlowDefVersion> findByFlowDefinationId(String flowDefinationId);

    public FlowDefVersion findByActDefId(String actDefId);

    }