package com.ecmp.flow.dao.impl;

import com.ecmp.context.ContextUtil;
import com.ecmp.core.dao.impl.BaseEntityDaoImpl;
import com.ecmp.core.search.PageInfo;
import com.ecmp.core.search.PageResult;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchOrder;
import com.ecmp.flow.dao.CustomFlowTaskDao;
import com.ecmp.flow.entity.FlowTask;
import com.ecmp.flow.entity.TaskMakeOverPower;
import org.apache.commons.lang.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

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


    String hqlQueryOrder = "   order by ft.priority desc,ft.createdDate asc ";


    public PageResult<FlowTask> findByPageByTenant(String appModuleId, String businessModelId, String flowTypeId, Search searchConfig) {
        PageInfo pageInfo = searchConfig.getPageInfo();
        if (Objects.isNull(pageInfo)) {
            pageInfo = new PageInfo();
        }
        Collection<String> quickSearchProperties = searchConfig.getQuickSearchProperties();
        String quickSearchValue = searchConfig.getQuickSearchValue();
        List<SearchOrder> sortOrders = searchConfig.getSortOrders();

        String tenantCode = ContextUtil.getTenantCode();
        String hqlCount = "select count(ft.id) from com.ecmp.flow.entity.FlowTask ft where ft.tenantCode = '" + tenantCode + "' and   (ft.trustState !=1  or ft.trustState is null ) ";
        String hqlQuery = "select ft           from com.ecmp.flow.entity.FlowTask ft where ft.tenantCode = '" + tenantCode + "' and   (ft.trustState !=1  or ft.trustState is null ) ";

        if (StringUtils.isNotEmpty(flowTypeId) && !"".equals(flowTypeId)) {
            hqlCount += " and ft.flowDefinitionId in (select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.id  = '" + flowTypeId + "' ))";
            hqlQuery += " and ft.flowDefinitionId in (select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.id  = '" + flowTypeId + "' ))";
        } else if (StringUtils.isNotEmpty(businessModelId) && !"".equals(businessModelId)) {
            hqlCount += " and ft.flowDefinitionId in (select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id = '" + businessModelId + "' ) )";
            hqlQuery += " and ft.flowDefinitionId in (select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id = '" + businessModelId + "' ) )";
        } else if (StringUtils.isNotEmpty(appModuleId) && !"".equals(appModuleId)) {
            hqlCount += " and ft.flowDefinitionId in (select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id in( select bm.id from com.ecmp.flow.entity.BusinessModel bm where bm.appModule.id='" + appModuleId + "'   )) )";
            hqlQuery += " and ft.flowDefinitionId in (select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id in( select bm.id from com.ecmp.flow.entity.BusinessModel bm where bm.appModule.id='" + appModuleId + "'   )) )";
        }

        if (StringUtils.isNotEmpty(quickSearchValue) && quickSearchProperties != null && !quickSearchProperties.isEmpty()) {
            StringBuffer extraHql = new StringBuffer(" and (");
            boolean first = true;
            for (String s : quickSearchProperties) {
                if (first) {
                    extraHql.append("  ft." + s + " like '%" + quickSearchValue + "%'");
                    first = false;
                } else {
                    extraHql.append(" or  ft." + s + " like '%" + quickSearchValue + "%'");
                }
            }
            extraHql.append(" )");
            hqlCount += extraHql.toString();
            hqlQuery += extraHql.toString();
        }
        if (sortOrders != null && sortOrders.size() > 0) {
            for (int i = 0; i < sortOrders.size(); i++) {
                SearchOrder searchOrder = sortOrders.get(i);
                if (i == 0) {
                    hqlQuery += " order by  ft." + searchOrder.getProperty() + " " + searchOrder.getDirection();
                } else {
                    hqlQuery += ", ft." + searchOrder.getProperty() + " " + searchOrder.getDirection();
                }
            }
        } else {
            hqlQuery += " order by ft.createdDate desc";
        }
        TypedQuery<Long> queryTotal = entityManager.createQuery(hqlCount, Long.class);
        Long total = queryTotal.getSingleResult();

        TypedQuery<FlowTask> query = entityManager.createQuery(hqlQuery, FlowTask.class);
        query.setFirstResult((pageInfo.getPage() - 1) * pageInfo.getRows());
        query.setMaxResults(pageInfo.getRows());
        List<FlowTask> result = query.getResultList();
        PageResult<FlowTask> pageResult = new PageResult<>();
        pageResult.setPage(pageInfo.getPage());
        pageResult.setRows(result);
        pageResult.setRecords(total.intValue());
        pageResult.setTotal((total.intValue() + pageInfo.getRows() - 1) / pageInfo.getRows());

        return pageResult;
    }

    /**
     * 通过Id获取一个待办任务(设置了办理任务URL)
     *
     * @param taskId 待办任务Id
     * @return 待办任务
     */
    @Override
    public FlowTask findTaskById(String taskId) {
        return findOne(taskId);
    }

    public PageResult<FlowTask> findByPageByBusinessModelId(String businessModelId, String executorId, Search searchConfig) {
        PageInfo pageInfo = searchConfig.getPageInfo();
        Collection<String> quickSearchProperties = searchConfig.getQuickSearchProperties();
        String quickSearchValue = searchConfig.getQuickSearchValue();
        List<SearchOrder> sortOrders = searchConfig.getSortOrders();
        String hqlCount = " select count(ft.id) from com.ecmp.flow.entity.FlowTask ft " +
                " where ft.executorId  = :executorId " +
                " and (ft.trustState !=1  or ft.trustState is null ) " +
                " and ft.flowDefinitionId " +
                " in (select fd.id    from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id " +
                " in (select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id " +
                " in (select bm.id    from com.ecmp.flow.entity.BusinessModel bm where bm.id = :businessModelId)))";
        String hqlQuery = " select ft from com.ecmp.flow.entity.FlowTask ft " +
                " where ft.executorId  = :executorId " +
                " and (ft.trustState !=1  or ft.trustState is null ) " +
                " and ft.flowDefinitionId " +
                " in (select fd.id    from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id " +
                " in (select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id " +
                " in ( select bm.id   from com.ecmp.flow.entity.BusinessModel bm where bm.id = :businessModelId)))";
        if (StringUtils.isNotEmpty(quickSearchValue) && quickSearchProperties != null && !quickSearchProperties.isEmpty()) {
            StringBuffer extraHql = new StringBuffer(" and (");
            boolean first = true;
            for (String s : quickSearchProperties) {
                if (first) {
                    extraHql.append("  ft." + s + " like '%" + quickSearchValue + "%'");
                    first = false;
                } else {
                    extraHql.append(" or  ft." + s + " like '%" + quickSearchValue + "%'");
                }
            }
            extraHql.append(" )");
            hqlCount += extraHql.toString();
            hqlQuery += extraHql.toString();
        }
        if (sortOrders != null && sortOrders.size() > 0) {
            hqlQuery += " order by ft.priority desc ";
            for (int i = 0; i < sortOrders.size(); i++) {
                SearchOrder searchOrder = sortOrders.get(i);
                if (!"priority".equalsIgnoreCase(searchOrder.getProperty())) {
                    hqlQuery += ", ft." + searchOrder.getProperty() + " " + searchOrder.getDirection();
                }
            }
        } else {
            hqlQuery += hqlQueryOrder;
        }
        TypedQuery<Long> queryTotal = entityManager.createQuery(hqlCount, Long.class);
        queryTotal.setParameter("executorId", executorId);
        queryTotal.setParameter("businessModelId", businessModelId);
        Long total = queryTotal.getSingleResult();

        TypedQuery<FlowTask> query = entityManager.createQuery(hqlQuery, FlowTask.class);
        query.setParameter("executorId", executorId);
        query.setParameter("businessModelId", businessModelId);
        query.setFirstResult((pageInfo.getPage() - 1) * pageInfo.getRows());
        query.setMaxResults(pageInfo.getRows());
        List<FlowTask> result = query.getResultList();
        PageResult<FlowTask> pageResult = new PageResult<>();
        pageResult.setPage(pageInfo.getPage());
        pageResult.setRows(result);
        pageResult.setRecords(total.intValue());
        pageResult.setTotal((total.intValue() + pageInfo.getRows() - 1) / pageInfo.getRows());

        return pageResult;
    }


    public PageResult<FlowTask> findByPageByBusinessModelIdOfPower(String businessModelId, String executorId, List<TaskMakeOverPower>  powerList, Search searchConfig) {
        PageInfo pageInfo = searchConfig.getPageInfo();
        Collection<String> quickSearchProperties = searchConfig.getQuickSearchProperties();
        String quickSearchValue = searchConfig.getQuickSearchValue();
        List<SearchOrder> sortOrders = searchConfig.getSortOrders();
        String hqlCount = " select count(ft.id) from com.ecmp.flow.entity.FlowTask ft " +
                " where   (ft.trustState !=1  or ft.trustState is null ) " +
                " and ft.flowDefinitionId " +
                " in (select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id " +
                " in (select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id " +
                " in (select bm.id from com.ecmp.flow.entity.BusinessModel bm where bm.id = :businessModelId)))";
        String hqlQuery = "select ft from com.ecmp.flow.entity.FlowTask ft " +
                " where  (ft.trustState !=1  or ft.trustState is null ) " +
                " and ft.flowDefinitionId " +
                " in (select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id " +
                " in (select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id " +
                " in ( select bm.id from com.ecmp.flow.entity.BusinessModel bm where bm.id = :businessModelId)))";

        if (powerList != null && !powerList.isEmpty()) { //共同查看模式转授权信息不为空
            hqlCount += " and  (  (ft.executorId  = :executorId )    ";
            hqlQuery += " and  (  (ft.executorId  = :executorId )    ";
            for (int i = 0; i < powerList.size(); i++) {
                TaskMakeOverPower bean = powerList.get(i);
                hqlCount += "  or  ( ft.executorId = '" + bean.getUserId() + "'  and  ft.flowInstance.flowDefVersion.flowDefination.flowType.id = '" + bean.getFlowTypeId() + "'    )   ";
                hqlQuery += "  or  ( ft.executorId = '" + bean.getUserId() + "'  and  ft.flowInstance.flowDefVersion.flowDefination.flowType.id = '" + bean.getFlowTypeId() + "'    )   ";
            }
            hqlCount += "  )   ";
            hqlQuery += "  )   ";
        } else {
            hqlCount += " and  ft.executorId  = :executorId  ";
            hqlQuery += " and  ft.executorId  = :executorId  ";
        }

        if (StringUtils.isNotEmpty(quickSearchValue) && quickSearchProperties != null && !quickSearchProperties.isEmpty()) {
            StringBuffer extraHql = new StringBuffer(" and (");
            boolean first = true;
            for (String s : quickSearchProperties) {
                if (first) {
                    extraHql.append("  ft." + s + " like '%" + quickSearchValue + "%'");
                    first = false;
                } else {
                    extraHql.append(" or  ft." + s + " like '%" + quickSearchValue + "%'");
                }
            }
            extraHql.append(" )");
            hqlCount += extraHql.toString();
            hqlQuery += extraHql.toString();
        }
        if (sortOrders != null && sortOrders.size() > 0) {
            hqlQuery += " order by ft.priority desc ";
            for (int i = 0; i < sortOrders.size(); i++) {
                SearchOrder searchOrder = sortOrders.get(i);
                if (!"priority".equalsIgnoreCase(searchOrder.getProperty())) {
                    hqlQuery += ", ft." + searchOrder.getProperty() + " " + searchOrder.getDirection();
                }
            }
        } else {
            hqlQuery += hqlQueryOrder;
        }
        TypedQuery<Long> queryTotal = entityManager.createQuery(hqlCount, Long.class);
        queryTotal.setParameter("executorId", executorId);
        queryTotal.setParameter("businessModelId", businessModelId);
        Long total = queryTotal.getSingleResult();

        TypedQuery<FlowTask> query = entityManager.createQuery(hqlQuery, FlowTask.class);
        query.setParameter("executorId", executorId);
        query.setParameter("businessModelId", businessModelId);
        query.setFirstResult((pageInfo.getPage() - 1) * pageInfo.getRows());
        query.setMaxResults(pageInfo.getRows());
        List<FlowTask> result = query.getResultList();
        PageResult<FlowTask> pageResult = new PageResult<>();
        pageResult.setPage(pageInfo.getPage());
        pageResult.setRows(result);
        pageResult.setRecords(total.intValue());
        pageResult.setTotal((total.intValue() + pageInfo.getRows() - 1) / pageInfo.getRows());
        return pageResult;
    }


    public PageResult<FlowTask> findByPage(String executorId, String appSign, Search searchConfig) {
        PageInfo pageInfo = searchConfig.getPageInfo();
        Collection<String> quickSearchProperties = searchConfig.getQuickSearchProperties();
        String quickSearchValue = searchConfig.getQuickSearchValue();
        List<SearchOrder> sortOrders = searchConfig.getSortOrders();
        String hqlCount = " select count(ft.id) from com.ecmp.flow.entity.FlowTask ft " +
                " where ft.executorId  = :executorId " +
                " and (ft.trustState !=1  or ft.trustState is null) ";
        String hqlQuery = "select ft from com.ecmp.flow.entity.FlowTask ft " +
                " where ft.executorId  = :executorId " +
                " and (ft.trustState !=1  or ft.trustState is null) ";
        if (StringUtils.isNotEmpty(quickSearchValue) && quickSearchProperties != null && !quickSearchProperties.isEmpty()) {
            StringBuffer extraHql = new StringBuffer(" and (");
            boolean first = true;
            for (String s : quickSearchProperties) {
                if (first) {
                    extraHql.append("  ft." + s + " like '%" + quickSearchValue + "%'");
                    first = false;
                } else {
                    extraHql.append(" or  ft." + s + " like '%" + quickSearchValue + "%'");
                }
            }
            extraHql.append(" )");
            hqlCount += extraHql.toString();
            hqlQuery += extraHql.toString();
        }
        // 限制应用标识
        if (!StringUtils.isBlank(appSign)) {
            String appSignSql = " and ft.flowDefinitionId " +
                    " in (select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id " +
                    " in (select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id " +
                    " in (select bm.id from com.ecmp.flow.entity.BusinessModel bm where bm.appModule.code like :appSign)))";
            hqlCount += appSignSql;
            hqlQuery += appSignSql;
        }

        if (sortOrders != null && sortOrders.size() > 0) {
            hqlQuery += " order by ft.priority desc ";
            for (int i = 0; i < sortOrders.size(); i++) {
                SearchOrder searchOrder = sortOrders.get(i);
                if (!"priority".equalsIgnoreCase(searchOrder.getProperty())) {
                    hqlQuery += ", ft." + searchOrder.getProperty() + " " + searchOrder.getDirection();
                }
            }
        } else {
            hqlQuery += hqlQueryOrder;
        }
        TypedQuery<Long> queryTotal = entityManager.createQuery(hqlCount, Long.class);
        queryTotal.setParameter("executorId", executorId);
        if (!StringUtils.isBlank(appSign)) {
            queryTotal.setParameter("appSign", appSign + "%");
        }
        Long total = queryTotal.getSingleResult();

        TypedQuery<FlowTask> query = entityManager.createQuery(hqlQuery, FlowTask.class);
        query.setParameter("executorId", executorId);
        if (!StringUtils.isBlank(appSign)) {
            queryTotal.setParameter("appSign", appSign + "%");
        }
        query.setFirstResult((pageInfo.getPage() - 1) * pageInfo.getRows());
        query.setMaxResults(pageInfo.getRows());
        List<FlowTask> result = query.getResultList();
        PageResult<FlowTask> pageResult = new PageResult<>();
        pageResult.setPage(pageInfo.getPage());
        pageResult.setRows(result);
        pageResult.setRecords(total.intValue());
        pageResult.setTotal((total.intValue() + pageInfo.getRows() - 1) / pageInfo.getRows());
        return pageResult;
    }

    public PageResult<FlowTask> findByPageOfPower(String executorId,List<TaskMakeOverPower>  powerList, String appSign, Search searchConfig) {
        PageInfo pageInfo = searchConfig.getPageInfo();
        Collection<String> quickSearchProperties = searchConfig.getQuickSearchProperties();
        String quickSearchValue = searchConfig.getQuickSearchValue();
        List<SearchOrder> sortOrders = searchConfig.getSortOrders();
        String hqlCount = " select count(ft.id) from com.ecmp.flow.entity.FlowTask ft  where  (ft.trustState !=1  or ft.trustState is null) ";
        String hqlQuery = " select ft           from com.ecmp.flow.entity.FlowTask ft  where  (ft.trustState !=1  or ft.trustState is null) ";

        if (powerList != null && !powerList.isEmpty()) { //共同查看模式转授权信息不为空
            hqlCount += " and  (  (ft.executorId  = :executorId )    ";
            hqlQuery += " and  (  (ft.executorId  = :executorId )    ";
            for (int i = 0; i < powerList.size(); i++) {
                TaskMakeOverPower bean = powerList.get(i);
                hqlCount += "  or  ( ft.executorId = '" + bean.getUserId() + "'  and  ft.flowInstance.flowDefVersion.flowDefination.flowType.id = '" + bean.getFlowTypeId() + "'    )   ";
                hqlQuery += "  or  ( ft.executorId = '" + bean.getUserId() + "'  and  ft.flowInstance.flowDefVersion.flowDefination.flowType.id = '" + bean.getFlowTypeId() + "'    )   ";
            }
            hqlCount += "  )   ";
            hqlQuery += "  )   ";
        } else {
            hqlCount += " and  ft.executorId  = :executorId  ";
            hqlQuery += " and  ft.executorId  = :executorId  ";
        }



        if (StringUtils.isNotEmpty(quickSearchValue) && quickSearchProperties != null && !quickSearchProperties.isEmpty()) {
            StringBuffer extraHql = new StringBuffer(" and (");
            boolean first = true;
            for (String s : quickSearchProperties) {
                if (first) {
                    extraHql.append("  ft." + s + " like '%" + quickSearchValue + "%'");
                    first = false;
                } else {
                    extraHql.append(" or  ft." + s + " like '%" + quickSearchValue + "%'");
                }
            }
            extraHql.append(" )");
            hqlCount += extraHql.toString();
            hqlQuery += extraHql.toString();
        }
        // 限制应用标识
        if (!StringUtils.isBlank(appSign)) {
            String appSignSql = " and ft.flowDefinitionId " +
                    " in (select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id " +
                    " in (select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id " +
                    " in (select bm.id from com.ecmp.flow.entity.BusinessModel bm where bm.appModule.code like :appSign)))";
            hqlCount += appSignSql;
            hqlQuery += appSignSql;
        }

        if (sortOrders != null && sortOrders.size() > 0) {
            hqlQuery += " order by ft.priority desc ";
            for (int i = 0; i < sortOrders.size(); i++) {
                SearchOrder searchOrder = sortOrders.get(i);
                if (!"priority".equalsIgnoreCase(searchOrder.getProperty())) {
                    hqlQuery += ", ft." + searchOrder.getProperty() + " " + searchOrder.getDirection();
                }
            }
        } else {
            hqlQuery += hqlQueryOrder;
        }
        TypedQuery<Long> queryTotal = entityManager.createQuery(hqlCount, Long.class);
        queryTotal.setParameter("executorId", executorId);
        if (!StringUtils.isBlank(appSign)) {
            queryTotal.setParameter("appSign", appSign + "%");
        }
        Long total = queryTotal.getSingleResult();

        TypedQuery<FlowTask> query = entityManager.createQuery(hqlQuery, FlowTask.class);
        query.setParameter("executorId", executorId);
        if (!StringUtils.isBlank(appSign)) {
            queryTotal.setParameter("appSign", appSign + "%");
        }
        query.setFirstResult((pageInfo.getPage() - 1) * pageInfo.getRows());
        query.setMaxResults(pageInfo.getRows());
        List<FlowTask> result = query.getResultList();
        PageResult<FlowTask> pageResult = new PageResult<>();
        pageResult.setPage(pageInfo.getPage());
        pageResult.setRows(result);
        pageResult.setRecords(total.intValue());
        pageResult.setTotal((total.intValue() + pageInfo.getRows() - 1) / pageInfo.getRows());
        return pageResult;
    }

    public PageResult<FlowTask> findByPageCanBatchApproval(String executorId, Search searchConfig) {
        PageInfo pageInfo = searchConfig.getPageInfo();
        Collection<String> quickSearchProperties = searchConfig.getQuickSearchProperties();
        String quickSearchValue = searchConfig.getQuickSearchValue();
        String hqlCount = "select count(ft.id) from com.ecmp.flow.entity.FlowTask ft where ft.executorId  = :executorId and ft.canBatchApproval = true ";
        String hqlQuery = "select ft from com.ecmp.flow.entity.FlowTask ft where ft.executorId  = :executorId and ft.canBatchApproval = true ";
        if (StringUtils.isNotEmpty(quickSearchValue) && quickSearchProperties != null && !quickSearchProperties.isEmpty()) {
            StringBuffer extraHql = new StringBuffer(" and (");
            boolean first = true;
            for (String s : quickSearchProperties) {
                if (first) {
                    extraHql.append("  ft." + s + " like '%" + quickSearchValue + "%'");
                    first = false;
                } else {
                    extraHql.append(" or  ft." + s + " like '%" + quickSearchValue + "%'");
                }
            }
            extraHql.append(" )");
            hqlCount += extraHql.toString();
            hqlQuery += extraHql.toString();
        }
        hqlQuery += hqlQueryOrder;
        TypedQuery<Long> queryTotal = entityManager.createQuery(hqlCount, Long.class);
        queryTotal.setParameter("executorId", executorId);
        Long total = queryTotal.getSingleResult();

        TypedQuery<FlowTask> query = entityManager.createQuery(hqlQuery, FlowTask.class);
        query.setParameter("executorId", executorId);
        query.setFirstResult((pageInfo.getPage() - 1) * pageInfo.getRows());
        query.setMaxResults(pageInfo.getRows());
        List<FlowTask> result = query.getResultList();
        PageResult<FlowTask> pageResult = new PageResult<>();
        pageResult.setPage(pageInfo.getPage());
        pageResult.setRows(result);
        pageResult.setRecords(total.intValue());
        pageResult.setTotal((total.intValue() + pageInfo.getRows() - 1) / pageInfo.getRows());

        return pageResult;
    }


    public PageResult<FlowTask> findByPageCanBatchApprovalOfPower(String executorId, List<TaskMakeOverPower> powerList, Search searchConfig) {
        PageInfo pageInfo = searchConfig.getPageInfo();
        Collection<String> quickSearchProperties = searchConfig.getQuickSearchProperties();
        String quickSearchValue = searchConfig.getQuickSearchValue();
        String hqlCount = "select count(ft.id) from com.ecmp.flow.entity.FlowTask ft where  ft.canBatchApproval = true and (ft.trustState !=1  or ft.trustState is null )  ";
        String hqlQuery = "select ft from com.ecmp.flow.entity.FlowTask ft where  ft.canBatchApproval = true and (ft.trustState !=1  or ft.trustState is null )  ";
        if (powerList != null && !powerList.isEmpty()) { //共同查看模式转授权信息不为空
            hqlCount += " and  (  (ft.executorId  = :executorId )    ";
            hqlQuery += " and  (  (ft.executorId  = :executorId )    ";
            for (int i = 0; i < powerList.size(); i++) {
                TaskMakeOverPower bean = powerList.get(i);
                hqlCount += "  or  ( ft.executorId = '" + bean.getUserId() + "'  and  ft.flowInstance.flowDefVersion.flowDefination.flowType.id = '" + bean.getFlowTypeId() + "'    )   ";
                hqlQuery += "  or  ( ft.executorId = '" + bean.getUserId() + "'  and  ft.flowInstance.flowDefVersion.flowDefination.flowType.id = '" + bean.getFlowTypeId() + "'    )   ";
            }
            hqlCount += "  )   ";
            hqlQuery += "  )   ";
        } else {
            hqlCount += " and  ft.executorId  = :executorId  ";
            hqlQuery += " and  ft.executorId  = :executorId  ";
        }


        if (StringUtils.isNotEmpty(quickSearchValue) && quickSearchProperties != null && !quickSearchProperties.isEmpty()) {
            StringBuffer extraHql = new StringBuffer(" and (");
            boolean first = true;
            for (String s : quickSearchProperties) {
                if (first) {
                    extraHql.append("  ft." + s + " like '%" + quickSearchValue + "%'");
                    first = false;
                } else {
                    extraHql.append(" or  ft." + s + " like '%" + quickSearchValue + "%'");
                }
            }
            extraHql.append(" )");
            hqlCount += extraHql.toString();
            hqlQuery += extraHql.toString();
        }
        hqlQuery += hqlQueryOrder;
        TypedQuery<Long> queryTotal = entityManager.createQuery(hqlCount, Long.class);
        queryTotal.setParameter("executorId", executorId);
        Long total = queryTotal.getSingleResult();

        TypedQuery<FlowTask> query = entityManager.createQuery(hqlQuery, FlowTask.class);
        query.setParameter("executorId", executorId);
        query.setFirstResult((pageInfo.getPage() - 1) * pageInfo.getRows());
        query.setMaxResults(pageInfo.getRows());
        List<FlowTask> result = query.getResultList();
        PageResult<FlowTask> pageResult = new PageResult<>();
        pageResult.setPage(pageInfo.getPage());
        pageResult.setRows(result);
        pageResult.setRecords(total.intValue());
        pageResult.setTotal((total.intValue() + pageInfo.getRows() - 1) / pageInfo.getRows());

        return pageResult;
    }


    public Long findCountByExecutorId(String executorId, Search searchConfig) {
        PageInfo pageInfo = searchConfig.getPageInfo();
        Collection<String> quickSearchProperties = searchConfig.getQuickSearchProperties();
        String quickSearchValue = searchConfig.getQuickSearchValue();
        String hqlCount = "select count(ft.id) from com.ecmp.flow.entity.FlowTask ft where ft.executorId  = :executorId  and  (ft.trustState !=1  or ft.trustState is null ) ";
        if (StringUtils.isNotEmpty(quickSearchValue) && quickSearchProperties != null && !quickSearchProperties.isEmpty()) {
            StringBuffer extraHql = new StringBuffer("and (");
            boolean first = true;
            for (String s : quickSearchProperties) {
                if (first) {
                    extraHql.append("  ft." + s + " like '%" + quickSearchValue + "%'");
                    first = false;
                } else {
                    extraHql.append(" or  ft." + s + " like '%" + quickSearchValue + "%'");
                }
            }
            extraHql.append(" )");
            hqlCount += extraHql.toString();
        }
        TypedQuery<Long> queryTotal = entityManager.createQuery(hqlCount, Long.class);
        queryTotal.setParameter("executorId", executorId);
        Long total = queryTotal.getSingleResult();
        return total;
    }

    public PageResult<FlowTask> findByPageCanBatchApprovalByBusinessModelId(String businessModelId, String executorId, Search searchConfig) {
        PageInfo pageInfo = searchConfig.getPageInfo();
        Collection<String> quickSearchProperties = searchConfig.getQuickSearchProperties();
        String quickSearchValue = searchConfig.getQuickSearchValue();
        String hqlCount = "select count(ft.id) from com.ecmp.flow.entity.FlowTask ft where ft.executorId  = :executorId and ft.canBatchApproval = true and ft.flowDefinitionId in(select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id in( select bm.id from com.ecmp.flow.entity.BusinessModel bm where bm.id = :businessModelId)) ) ";
        String hqlQuery = "select ft from com.ecmp.flow.entity.FlowTask ft where ft.executorId  = :executorId and ft.canBatchApproval = true and ft.flowDefinitionId in(select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id in( select bm.id from com.ecmp.flow.entity.BusinessModel bm where bm.id = :businessModelId)) )";
        if (StringUtils.isNotEmpty(quickSearchValue) && quickSearchProperties != null && !quickSearchProperties.isEmpty()) {
            StringBuffer extraHql = new StringBuffer(" and (");
            boolean first = true;
            for (String s : quickSearchProperties) {
                if (first) {
                    extraHql.append("  ft." + s + " like '%" + quickSearchValue + "%'");
                    first = false;
                } else {
                    extraHql.append(" or  ft." + s + " like '%" + quickSearchValue + "%'");
                }
            }
            extraHql.append(" )");
            hqlCount += extraHql.toString();
            hqlQuery += extraHql.toString();
        }
        hqlQuery += hqlQueryOrder;
        TypedQuery<Long> queryTotal = entityManager.createQuery(hqlCount, Long.class);
        queryTotal.setParameter("executorId", executorId);
        queryTotal.setParameter("businessModelId", businessModelId);
        Long total = queryTotal.getSingleResult();

        TypedQuery<FlowTask> query = entityManager.createQuery(hqlQuery, FlowTask.class);
        query.setParameter("executorId", executorId);
        query.setParameter("businessModelId", businessModelId);
        query.setFirstResult((pageInfo.getPage() - 1) * pageInfo.getRows());
        query.setMaxResults(pageInfo.getRows());
        List<FlowTask> result = query.getResultList();
        PageResult<FlowTask> pageResult = new PageResult<>();
        pageResult.setPage(pageInfo.getPage());
        pageResult.setRows(result);
        pageResult.setRecords(total.intValue());
        pageResult.setTotal((total.intValue() + pageInfo.getRows() - 1) / pageInfo.getRows());

        return pageResult;
    }


    public PageResult<FlowTask> findByPageCanBatchApprovalByBusinessModelIdOfPower(String businessModelId, String executorId, List<TaskMakeOverPower> powerList, Search searchConfig) {
        PageInfo pageInfo = searchConfig.getPageInfo();
        Collection<String> quickSearchProperties = searchConfig.getQuickSearchProperties();
        String quickSearchValue = searchConfig.getQuickSearchValue();
        String hqlCount = "select count(ft.id) from com.ecmp.flow.entity.FlowTask ft where  ft.canBatchApproval = true and (ft.trustState !=1  or ft.trustState is null )    and ft.flowDefinitionId in(select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id in( select bm.id from com.ecmp.flow.entity.BusinessModel bm where bm.id = :businessModelId)) )  ";
        String hqlQuery = "select ft from com.ecmp.flow.entity.FlowTask ft where  ft.canBatchApproval = true and (ft.trustState !=1  or ft.trustState is null )  and ft.flowDefinitionId in(select fd.id from com.ecmp.flow.entity.FlowDefination fd where fd.flowType.id in(select fType.id from com.ecmp.flow.entity.FlowType fType where fType.businessModel.id in( select bm.id from com.ecmp.flow.entity.BusinessModel bm where bm.id = :businessModelId)) )  ";
        if (powerList != null && !powerList.isEmpty()) { //共同查看模式转授权信息不为空
            hqlCount += " and  (  (ft.executorId  = :executorId )    ";
            hqlQuery += " and  (  (ft.executorId  = :executorId )    ";
            for (int i = 0; i < powerList.size(); i++) {
                TaskMakeOverPower bean = powerList.get(i);
                hqlCount += "  or  ( ft.executorId = '" + bean.getUserId() + "'  and  ft.flowInstance.flowDefVersion.flowDefination.flowType.id = '" + bean.getFlowTypeId() + "'    )   ";
                hqlQuery += "  or  ( ft.executorId = '" + bean.getUserId() + "'  and  ft.flowInstance.flowDefVersion.flowDefination.flowType.id = '" + bean.getFlowTypeId() + "'    )   ";
            }
            hqlCount += "  )   ";
            hqlQuery += "  )   ";
        } else {
            hqlCount += " and  ft.executorId  = :executorId  ";
            hqlQuery += " and  ft.executorId  = :executorId  ";
        }

        if (StringUtils.isNotEmpty(quickSearchValue) && quickSearchProperties != null && !quickSearchProperties.isEmpty()) {
            StringBuffer extraHql = new StringBuffer(" and (");
            boolean first = true;
            for (String s : quickSearchProperties) {
                if (first) {
                    extraHql.append("  ft." + s + " like '%" + quickSearchValue + "%'");
                    first = false;
                } else {
                    extraHql.append(" or  ft." + s + " like '%" + quickSearchValue + "%'");
                }
            }
            extraHql.append(" )");
            hqlCount += extraHql.toString();
            hqlQuery += extraHql.toString();
        }
        hqlQuery += hqlQueryOrder;
        TypedQuery<Long> queryTotal = entityManager.createQuery(hqlCount, Long.class);
        queryTotal.setParameter("executorId", executorId);
        queryTotal.setParameter("businessModelId", businessModelId);
        Long total = queryTotal.getSingleResult();

        TypedQuery<FlowTask> query = entityManager.createQuery(hqlQuery, FlowTask.class);
        query.setParameter("executorId", executorId);
        query.setParameter("businessModelId", businessModelId);
        query.setFirstResult((pageInfo.getPage() - 1) * pageInfo.getRows());
        query.setMaxResults(pageInfo.getRows());
        List<FlowTask> result = query.getResultList();
        PageResult<FlowTask> pageResult = new PageResult<>();
        pageResult.setPage(pageInfo.getPage());
        pageResult.setRows(result);
        pageResult.setRecords(total.intValue());
        pageResult.setTotal((total.intValue() + pageInfo.getRows() - 1) / pageInfo.getRows());

        return pageResult;
    }
}
