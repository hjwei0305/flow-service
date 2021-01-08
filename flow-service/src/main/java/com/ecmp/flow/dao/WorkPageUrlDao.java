package com.ecmp.flow.dao;

import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.dao.jpa.BaseDao;
import com.ecmp.flow.entity.FlowDefVersion;
import com.ecmp.flow.entity.WorkPageUrl;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.List;

@Repository
public interface WorkPageUrlDao extends BaseEntityDao<WorkPageUrl> {
    /**
     * 根据应用模块的id来查询工作界面
     *
     * @param appModuleId 应用模块Id
     * @return 岗位清单
     */
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    List<WorkPageUrl> findByAppModuleId(String appModuleId);


    /**
     * 查看业务实体已经选中的工作界面
     * @param businessModelId  业务实体ID
     * @return 已选中的工作界面
     */
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    @Query("select w from com.ecmp.flow.entity.WorkPageUrl w where  w.id  in( select workPageUrlId  from com.ecmp.flow.entity.BusinessWorkPageUrl where businessModuleId = :businessModelId) ")
    List<WorkPageUrl> findSelectEdByBusinessModelId(@Param("businessModelId") String businessModelId);


    /**
     * 查看业务实体已经选中的工作界面
     * @param appModuleId 业务模块应用ID
     * @param businessModelId  业务实体ID
     * @return 已选中的工作界面
     */
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    @Query("select w from com.ecmp.flow.entity.WorkPageUrl w where w.appModuleId  = :appModuleId and w.id  in( select workPageUrlId  from com.ecmp.flow.entity.BusinessWorkPageUrl where businessModuleId = :businessModelId) ")
    List<WorkPageUrl> findSelectEd(@Param("appModuleId") String appModuleId, @Param("businessModelId") String businessModelId);


    /**
     * 查看业务实体未选中的工作界面
     * @param appModuleId 业务模块应用ID
     * @param businessModelId  业务实体ID
     * @return 未选中的工作界面
     */
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    @Query("select w from com.ecmp.flow.entity.WorkPageUrl w where w.appModuleId  = :appModuleId and w.id not in( select workPageUrlId  from com.ecmp.flow.entity.BusinessWorkPageUrl where businessModuleId = :businessModelId) ")
    List<WorkPageUrl> findNotSelectEd(@Param("appModuleId") String appModuleId, @Param("businessModelId") String businessModelId);


    /**
     * 通过流程类型id查找可配置工作页面
     * @param flowTypeId 流程类型id
     * @return
     */
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    @Query("select w from com.ecmp.flow.entity.WorkPageUrl w where w.id  in( select workPageUrlId  from com.ecmp.flow.entity.BusinessWorkPageUrl where businessModuleId = (select t.businessModel.id from com.ecmp.flow.entity.FlowType t  where t.id = :flowTypeId)) ")
    List<WorkPageUrl> findByFlowTypeId(@Param("flowTypeId") String flowTypeId);
}