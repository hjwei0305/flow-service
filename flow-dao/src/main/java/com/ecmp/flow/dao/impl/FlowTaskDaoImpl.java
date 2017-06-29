package com.ecmp.flow.dao.impl;

import com.ecmp.core.dao.impl.BaseEntityDaoImpl;
import com.ecmp.core.search.PageInfo;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchFilter;
import com.ecmp.flow.dao.CustomFlowTaskDao;
import com.ecmp.flow.entity.FlowTask;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

/**
 * *************************************************************************************************
 * <br>
 * 实现功能：
 * <br>
 * ------------------------------------------------------------------------------------------------
 * <br>
 * 版本          变更时间             变更人                     变更原因
 * <br>
 * ------------------------------------------------------------------------------------------------
 * <br>
 * 1.0.00      2017/5/9 20:26      马超(Vision.Mac)                新建
 * <br>
 * *************************************************************************************************
 */
public class FlowTaskDaoImpl extends BaseEntityDaoImpl<FlowTask> implements CustomFlowTaskDao {
    public FlowTaskDaoImpl(EntityManager entityManager) {
        super(FlowTask.class, entityManager);
    }

    public PageResult<FlowTask> findByPageByBusinessModelId(String businessModelId,String executorId, Search searchConfig) {
        PageInfo pageInfo = searchConfig.getPageInfo();

        TypedQuery<Long> queryTotal = entityManager.createQuery("select count(ft.id) from com.ecmp.flow.entity.FlowTask ft where ft.executorId  = :executorId and ft.flowDefinitionId in(select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id in( select bm.id from com.ecmp.flow.entity.BusinessModel bm where bm.id = :businessModelId)) ) ", Long.class);
        queryTotal.setParameter("executorId",executorId);
        queryTotal.setParameter("businessModelId",businessModelId);
        Long total = queryTotal.getSingleResult();

        TypedQuery<FlowTask> query = entityManager.createQuery("select ft from com.ecmp.flow.entity.FlowTask ft where ft.executorId  = :executorId and ft.flowDefinitionId in(select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id  in( select bm.id from com.ecmp.flow.entity.BusinessModel bm where bm.id = :businessModelId)) ) order by ft.lastEditedDate desc", FlowTask.class);
        query.setParameter("executorId",executorId);
        query.setParameter("businessModelId",businessModelId);
        query.setFirstResult( (pageInfo.getPage()-1) * pageInfo.getRows() );
        query.setMaxResults( pageInfo.getRows() );
        List<FlowTask>  result = query.getResultList();

        PageResult<FlowTask> pageResult = new PageResult<>();
        pageResult.setPage(pageInfo.getPage());
        pageResult.setRows(result);
        pageResult.setRecords(total.intValue());
        pageResult.setTotal((total.intValue()+pageInfo.getRows()-1)/pageInfo.getRows());

        return pageResult;
    }



}
