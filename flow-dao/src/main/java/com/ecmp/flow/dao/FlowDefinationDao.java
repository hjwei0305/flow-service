package com.ecmp.flow.dao;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.flow.entity.FlowDefination;
import com.ecmp.flow.entity.FlowType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlowDefinationDao extends BaseEntityDao<FlowDefination> {
    public FlowDefination findByDefKey(String defKey);

    @Query("select f from com.ecmp.flow.entity.FlowDefination f where f.flowType.id = (select ft.id from com.ecmp.flow.entity.FlowType ft where ft.code  = :typeCode)  order by f.priority desc,f.lastEditedDate desc")
    public List<FlowDefination> findByTypeCode(@Param("typeCode")String typeCode);

    @Query("select f from com.ecmp.flow.entity.FlowDefination f where f.flowType.id = (select ft.id from com.ecmp.flow.entity.FlowType ft where ft.code  = :typeCode) and f.orgId = :orgId  order by f.priority desc,f.lastEditedDate desc")
    public List<FlowDefination> findByTypeCodeAndOrgId(@Param("typeCode")String typeCode,@Param("orgId")String orgId);

    @Query("select f from com.ecmp.flow.entity.FlowDefination f where f.flowType.id = (select ft.id from com.ecmp.flow.entity.FlowType ft where ft.code  = :typeCode) and f.orgCode = :orgCode  order by f.priority desc,f.lastEditedDate desc")
    public List<FlowDefination> findByTypeCodeAndOrgCode(@Param("typeCode")String typeCode,@Param("orgCode")String orgCode);

    @Query("select f from com.ecmp.flow.entity.FlowDefination f where f.flowType.id = (select ft.id from com.ecmp.flow.entity.FlowType ft where ft.code  = :typeCode) and f.orgId = :orgId  order by f.priority desc,f.lastEditedDate desc")
    public List<FlowDefination> findByTypeCodeAndOrgCodePath(@Param("typeCode")String typeCode,@Param("orgCodePath")String orgCodePath);
}