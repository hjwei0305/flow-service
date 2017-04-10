package com.ecmp.flow.dao;

import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.entity.FlowDefination;
import com.ecmp.flow.entity.FlowType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlowDefVersionDao extends BaseDao<FlowDefVersion, String> {

    @Query("select fv from FlowDefVersion fv where fv.flowDefination.id  = :defId and fv.versionCode = :versionCode ")
    public FlowDefVersion findByDefIdAndVersionCode(@Param("defId")String defId,@Param("versionCode")String versionCode);
    public List<FlowDefVersion> findByFlowDefinationId(String flowDefinationId);

    }