package com.ecmp.flow.dao;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.flow.entity.FlowDefination;
import com.ecmp.flow.entity.FlowInstance;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.List;

@Repository
public interface FlowDefinationDao extends BaseEntityDao<FlowDefination> {

    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
     FlowDefination findByDefKey(String defKey);

    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    @Query("select f from com.ecmp.flow.entity.FlowDefination f where f.flowType.id = (select ft.id from com.ecmp.flow.entity.FlowType ft where ft.code  = :typeCode)  order by f.priority desc,f.lastEditedDate desc")
     List<FlowDefination> findByTypeCode(@Param("typeCode") String typeCode);

    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    @Query("select f from com.ecmp.flow.entity.FlowDefination f where f.flowType.id = (select ft.id from com.ecmp.flow.entity.FlowType ft where ft.code  = :typeCode) and f.orgId = :orgId  order by f.priority desc,f.lastEditedDate desc")
     List<FlowDefination> findByTypeCodeAndOrgId(@Param("typeCode") String typeCode, @Param("orgId") String orgId);

    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    @Query("select f from com.ecmp.flow.entity.FlowDefination f where f.flowDefinationStatus =1 and f.flowType.id = (select ft.id from com.ecmp.flow.entity.FlowType ft where ft.code  = :typeCode) and f.orgCode = :orgCode  order by f.priority desc,f.lastEditedDate desc")
     List<FlowDefination> findByTypeCodeAndOrgCode(@Param("typeCode") String typeCode, @Param("orgCode") String orgCode);


    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    @Query("select ft from com.ecmp.flow.entity.FlowDefination ft where  ft.id in  :flowDefinationIds and ft.lastDeloyVersionId is null ")
    List<FlowDefination> findListByDefIds(@Param("flowDefinationIds") List<String> flowDefinationIds);
}