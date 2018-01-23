package com.ecmp.flow.dao;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.flow.entity.FlowDefination;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlowDefinationDao extends BaseEntityDao<FlowDefination> {
    @Cacheable(cacheNames="findByDefKey")
    public FlowDefination findByDefKey(String defKey);

    @Cacheable(cacheNames="findByTypeCode")
    @Query("select f from com.ecmp.flow.entity.FlowDefination f where f.flowType.id = (select ft.id from com.ecmp.flow.entity.FlowType ft where ft.code  = :typeCode)  order by f.priority desc,f.lastEditedDate desc")
    public List<FlowDefination> findByTypeCode(@Param("typeCode")String typeCode);

    @Cacheable(cacheNames="findByTypeCodeAndOrgId")
    @Query("select f from com.ecmp.flow.entity.FlowDefination f where f.flowType.id = (select ft.id from com.ecmp.flow.entity.FlowType ft where ft.code  = :typeCode) and f.orgId = :orgId  order by f.priority desc,f.lastEditedDate desc")
    public List<FlowDefination> findByTypeCodeAndOrgId(@Param("typeCode")String typeCode,@Param("orgId")String orgId);

    @Cacheable(cacheNames="findByTypeCodeAndOrgCode")
    @Query("select f from com.ecmp.flow.entity.FlowDefination f where f.flowDefinationStatus =1 and f.flowType.id = (select ft.id from com.ecmp.flow.entity.FlowType ft where ft.code  = :typeCode) and f.orgCode = :orgCode  order by f.priority desc,f.lastEditedDate desc")
    public List<FlowDefination> findByTypeCodeAndOrgCode(@Param("typeCode")String typeCode,@Param("orgCode")String orgCode);
}